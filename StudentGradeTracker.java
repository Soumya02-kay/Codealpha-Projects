import java.util.*;

/**
 * TASK 1: Student Grade Tracker
 * Console-based Java program to manage student grades using ArrayList.
 * Features: add students, record multiple subject scores, calculate
 * average/highest/lowest per student and class-wide, print summary report.
 */
public class StudentGradeTracker {

    // ---------- Student model ----------
    static class Student {
        String name;
        List<Double> scores = new ArrayList<>();

        Student(String name) {
            this.name = name;
        }

        void addScore(double score) {
            scores.add(score);
        }

        double average() {
            if (scores.isEmpty()) return 0.0;
            double sum = 0;
            for (double s : scores) sum += s;
            return sum / scores.size();
        }

        double highest() {
            return scores.isEmpty() ? 0.0 : Collections.max(scores);
        }

        double lowest() {
            return scores.isEmpty() ? 0.0 : Collections.min(scores);
        }

        String grade() {
            double avg = average();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    // ---------- Core storage ----------
    private static final List<Student> students = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        System.out.println("=== Student Grade Tracker ===");

        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": addStudent(); break;
                case "2": addScores(); break;
                case "3": viewStudentReport(); break;
                case "4": printSummaryReport(); break;
                case "5": removeStudent(); break;
                case "0":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n1. Add Student");
        System.out.println("2. Add Scores to a Student");
        System.out.println("3. View Individual Student Report");
        System.out.println("4. View Class Summary Report");
        System.out.println("5. Remove Student");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void addStudent() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        students.add(new Student(name));
        System.out.println("Added student: " + name);
    }

    private static void addScores() {
        Student s = selectStudent();
        if (s == null) return;

        System.out.print("How many scores do you want to enter? ");
        int count = readInt();
        for (int i = 0; i < count; i++) {
            System.out.print("Enter score #" + (i + 1) + " (0-100): ");
            double score = readDouble();
            while (score < 0 || score > 100) {
                System.out.print("Invalid range. Enter score between 0-100: ");
                score = readDouble();
            }
            s.addScore(score);
        }
        System.out.println("Scores added for " + s.name);
    }

    private static void viewStudentReport() {
        Student s = selectStudent();
        if (s == null) return;

        System.out.println("\n--- Report for " + s.name + " ---");
        System.out.println("Scores: " + s.scores);
        System.out.printf("Average : %.2f%n", s.average());
        System.out.printf("Highest : %.2f%n", s.highest());
        System.out.printf("Lowest  : %.2f%n", s.lowest());
        System.out.println("Grade   : " + s.grade());
    }

    private static void printSummaryReport() {
        if (students.isEmpty()) {
            System.out.println("No students recorded yet.");
            return;
        }

        System.out.println("\n================= CLASS SUMMARY REPORT =================");
        System.out.printf("%-15s %-10s %-10s %-10s %-6s%n", "Name", "Average", "Highest", "Lowest", "Grade");
        System.out.println("----------------------------------------------------------");

        double classTotal = 0;
        double classHighest = Double.MIN_VALUE;
        double classLowest = Double.MAX_VALUE;
        String topStudent = "", bottomStudent = "";

        for (Student s : students) {
            System.out.printf("%-15s %-10.2f %-10.2f %-10.2f %-6s%n",
                    s.name, s.average(), s.highest(), s.lowest(), s.grade());

            double avg = s.average();
            classTotal += avg;
            if (avg > classHighest) { classHighest = avg; topStudent = s.name; }
            if (avg < classLowest) { classLowest = avg; bottomStudent = s.name; }
        }

        double classAverage = classTotal / students.size();
        System.out.println("----------------------------------------------------------");
        System.out.printf("Class Average Score : %.2f%n", classAverage);
        System.out.printf("Top Performer        : %s (%.2f)%n", topStudent, classHighest);
        System.out.printf("Lowest Performer      : %s (%.2f)%n", bottomStudent, classLowest);
        System.out.println("==========================================================");
    }

    private static void removeStudent() {
        Student s = selectStudent();
        if (s == null) return;
        students.remove(s);
        System.out.println("Removed " + s.name);
    }

    // ---------- Helpers ----------
    private static Student selectStudent() {
        if (students.isEmpty()) {
            System.out.println("No students available. Add one first.");
            return null;
        }
        System.out.println("Students:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i).name);
        }
        System.out.print("Select student number: ");
        int idx = readInt() - 1;
        if (idx < 0 || idx >= students.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return students.get(idx);
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid whole number: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
