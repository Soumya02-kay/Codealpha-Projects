import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TASK 4: Hotel Reservation System
 * Console-based Java program using OOP + File I/O.
 *
 * Classes:
 *   Room        -> number, category (Standard/Deluxe/Suite), price/night, availability
 *   Guest       -> simple guest info
 *   Reservation -> links a guest to a room for a date range, with payment status
 *   Hotel       -> manages rooms + reservations, persists to "reservations.txt"
 */
public class HotelReservationSystem {

    enum RoomCategory { STANDARD, DELUXE, SUITE }

    // ---------- Room ----------
    static class Room {
        int roomNumber;
        RoomCategory category;
        double pricePerNight;
        boolean available = true;

        Room(int roomNumber, RoomCategory category, double pricePerNight) {
            this.roomNumber = roomNumber;
            this.category = category;
            this.pricePerNight = pricePerNight;
        }

        @Override
        public String toString() {
            return String.format("Room %-4d | %-8s | $%.2f/night | %s",
                    roomNumber, category, pricePerNight, available ? "Available" : "Booked");
        }
    }

    // ---------- Reservation ----------
    static class Reservation {
        String reservationId;
        String guestName;
        int roomNumber;
        LocalDate checkIn;
        LocalDate checkOut;
        double totalCost;
        boolean paid;

        Reservation(String reservationId, String guestName, int roomNumber,
                    LocalDate checkIn, LocalDate checkOut, double totalCost) {
            this.reservationId = reservationId;
            this.guestName = guestName;
            this.roomNumber = roomNumber;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalCost = totalCost;
            this.paid = false;
        }

        long nights() {
            return checkOut.toEpochDay() - checkIn.toEpochDay();
        }

        @Override
        public String toString() {
            DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
            return String.format("ID:%s | %s | Room %d | %s -> %s (%d nights) | $%.2f | %s",
                    reservationId, guestName, roomNumber, checkIn.format(f), checkOut.format(f),
                    nights(), totalCost, paid ? "PAID" : "UNPAID");
        }

        String toFileLine() {
            return reservationId + "|" + guestName + "|" + roomNumber + "|" +
                    checkIn + "|" + checkOut + "|" + totalCost + "|" + paid;
        }

        static Reservation fromFileLine(String line) {
            String[] p = line.split("\\|");
            Reservation r = new Reservation(p[0], p[1], Integer.parseInt(p[2]),
                    LocalDate.parse(p[3]), LocalDate.parse(p[4]), Double.parseDouble(p[5]));
            r.paid = Boolean.parseBoolean(p[6]);
            return r;
        }
    }

    // ---------- Hotel (manager) ----------
    static class Hotel {
        Map<Integer, Room> rooms = new LinkedHashMap<>();
        List<Reservation> reservations = new ArrayList<>();
        int nextReservationNum = 1;
        static final String SAVE_FILE = "reservations.txt";

        void addRoom(Room r) {
            rooms.put(r.roomNumber, r);
        }

        List<Room> searchAvailable(RoomCategory category) {
            List<Room> results = new ArrayList<>();
            for (Room r : rooms.values()) {
                if (r.available && (category == null || r.category == category)) {
                    results.add(r);
                }
            }
            return results;
        }

        Reservation book(String guestName, int roomNumber, LocalDate in, LocalDate out) {
            Room room = rooms.get(roomNumber);
            if (room == null || !room.available) return null;
            if (!out.isAfter(in)) return null;

            long nights = out.toEpochDay() - in.toEpochDay();
            double total = nights * room.pricePerNight;
            String id = "RES" + String.format("%04d", nextReservationNum++);

            Reservation res = new Reservation(id, guestName, roomNumber, in, out, total);
            reservations.add(res);
            room.available = false;
            return res;
        }

        boolean cancel(String reservationId) {
            Reservation target = null;
            for (Reservation r : reservations) {
                if (r.reservationId.equalsIgnoreCase(reservationId)) {
                    target = r;
                    break;
                }
            }
            if (target == null) return false;
            reservations.remove(target);
            Room room = rooms.get(target.roomNumber);
            if (room != null) room.available = true;
            return true;
        }

        boolean pay(String reservationId) {
            for (Reservation r : reservations) {
                if (r.reservationId.equalsIgnoreCase(reservationId)) {
                    if (r.paid) return false; // already paid
                    r.paid = true;
                    return true;
                }
            }
            return false;
        }

        void save() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
                pw.println("NEXTID|" + nextReservationNum);
                for (Room r : rooms.values()) {
                    pw.println("ROOM|" + r.roomNumber + "|" + r.category + "|" + r.pricePerNight + "|" + r.available);
                }
                for (Reservation r : reservations) {
                    pw.println("RES|" + r.toFileLine());
                }
            } catch (IOException e) {
                System.out.println("Error saving data: " + e.getMessage());
            }
        }

        void load() {
            File f = new File(SAVE_FILE);
            if (!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("NEXTID|")) {
                        nextReservationNum = Integer.parseInt(line.split("\\|")[1]);
                    } else if (line.startsWith("ROOM|")) {
                        String[] p = line.split("\\|");
                        int num = Integer.parseInt(p[1]);
                        Room room = rooms.get(num);
                        if (room != null) room.available = Boolean.parseBoolean(p[4]);
                    } else if (line.startsWith("RES|")) {
                        reservations.add(Reservation.fromFileLine(line.substring(4)));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading data: " + e.getMessage());
            }
        }
    }

    private static final Scanner sc = new Scanner(System.in);
    private static final Hotel hotel = new Hotel();

    public static void main(String[] args) {
        System.out.println("=== Hotel Reservation System ===");
        initRooms();
        hotel.load();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": searchRooms(); break;
                case "2": bookRoom(); break;
                case "3": cancelReservation(); break;
                case "4": viewAllReservations(); break;
                case "5": makePayment(); break;
                case "6": viewRoomStatus(); break;
                case "0":
                    hotel.save();
                    running = false;
                    System.out.println("Data saved. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        sc.close();
    }

    private static void initRooms() {
        int num = 101;
        for (int i = 0; i < 4; i++) hotel.addRoom(new Room(num++, RoomCategory.STANDARD, 89.00));
        for (int i = 0; i < 3; i++) hotel.addRoom(new Room(num++, RoomCategory.DELUXE, 149.00));
        for (int i = 0; i < 2; i++) hotel.addRoom(new Room(num++, RoomCategory.SUITE, 299.00));
    }

    private static void printMenu() {
        System.out.println("\n1. Search Available Rooms");
        System.out.println("2. Book a Room");
        System.out.println("3. Cancel a Reservation");
        System.out.println("4. View All Reservations");
        System.out.println("5. Make Payment for a Reservation");
        System.out.println("6. View All Room Status");
        System.out.println("0. Exit & Save");
        System.out.print("Choose an option: ");
    }

    private static void searchRooms() {
        System.out.println("Filter by category? 1) Standard 2) Deluxe 3) Suite 4) Any");
        String choice = sc.nextLine().trim();
        RoomCategory category = switch (choice) {
            case "1" -> RoomCategory.STANDARD;
            case "2" -> RoomCategory.DELUXE;
            case "3" -> RoomCategory.SUITE;
            default -> null;
        };
        List<Room> results = hotel.searchAvailable(category);
        if (results.isEmpty()) {
            System.out.println("No available rooms match that filter.");
        } else {
            System.out.println("--- Available Rooms ---");
            for (Room r : results) System.out.println(r);
        }
    }

    private static void bookRoom() {
        System.out.print("Guest name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name required."); return; }

        System.out.print("Room number to book: ");
        int roomNum = readInt();

        LocalDate checkIn = readDate("Check-in date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Check-out date (YYYY-MM-DD): ");

        Reservation res = hotel.book(name, roomNum, checkIn, checkOut);
        if (res == null) {
            System.out.println("Booking failed. Room may be unavailable, dates invalid, or room doesn't exist.");
            return;
        }
        System.out.println("Booking confirmed!");
        System.out.println(res);
        hotel.save();
    }

    private static void cancelReservation() {
        System.out.print("Enter reservation ID to cancel: ");
        String id = sc.nextLine().trim();
        boolean ok = hotel.cancel(id);
        System.out.println(ok ? "Reservation cancelled." : "Reservation ID not found.");
        if (ok) hotel.save();
    }

    private static void viewAllReservations() {
        if (hotel.reservations.isEmpty()) {
            System.out.println("No reservations on file.");
            return;
        }
        System.out.println("--- All Reservations ---");
        for (Reservation r : hotel.reservations) System.out.println(r);
    }

    private static void makePayment() {
        System.out.print("Enter reservation ID to pay for: ");
        String id = sc.nextLine().trim();

        Reservation target = null;
        for (Reservation r : hotel.reservations) {
            if (r.reservationId.equalsIgnoreCase(id)) { target = r; break; }
        }
        if (target == null) {
            System.out.println("Reservation not found.");
            return;
        }
        if (target.paid) {
            System.out.println("This reservation is already paid.");
            return;
        }

        System.out.printf("Amount due: $%.2f%n", target.totalCost);
        System.out.print("Simulate payment method (1=Card, 2=Cash): ");
        String method = sc.nextLine().trim();
        String methodName = method.equals("2") ? "Cash" : "Card";

        boolean ok = hotel.pay(id);
        if (ok) {
            System.out.printf("Payment of $%.2f via %s successful. Reservation %s marked PAID.%n",
                    target.totalCost, methodName, id);
            hotel.save();
        } else {
            System.out.println("Payment failed.");
        }
    }

    private static void viewRoomStatus() {
        System.out.println("--- Room Status ---");
        for (Room r : hotel.rooms.values()) System.out.println(r);
    }

    // ---------- Helpers ----------
    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("Invalid format. Please use YYYY-MM-DD.");
            }
        }
    }
}
