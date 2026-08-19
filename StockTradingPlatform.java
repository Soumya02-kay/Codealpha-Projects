import java.io.*;
import java.util.*;

/**
 * TASK 2: Stock Trading Platform
 * Console-based simulation with OOP design:
 *   Stock       -> market data (symbol, price) with random-walk price updates
 *   User        -> cash balance + owned holdings
 *   Transaction -> record of a buy/sell
 *   Portfolio   -> aggregates holdings + transaction history, persisted to file
 *
 * Data is saved to "portfolio.txt" in the current working directory (simple
 * text-based File I/O, no external DB required).
 */
public class StockTradingPlatform {

    // ---------- Stock ----------
    static class Stock {
        String symbol;
        String companyName;
        double price;

        Stock(String symbol, String companyName, double price) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.price = price;
        }

        // Simulate market movement with a small random fluctuation (+/-3%)
        void updatePrice(Random rnd) {
            double changePercent = (rnd.nextDouble() * 6) - 3; // -3% to +3%
            price = Math.max(0.5, price * (1 + changePercent / 100.0));
        }
    }

    // ---------- Transaction ----------
    static class Transaction {
        String type; // BUY or SELL
        String symbol;
        int quantity;
        double pricePerShare;
        double total;
        String timestamp;

        Transaction(String type, String symbol, int quantity, double pricePerShare) {
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.pricePerShare = pricePerShare;
            this.total = quantity * pricePerShare;
            this.timestamp = new Date().toString();
        }

        @Override
        public String toString() {
            return String.format("%-6s %-6s qty:%-5d @ $%-10.2f total: $%-10.2f (%s)",
                    type, symbol, quantity, pricePerShare, total, timestamp);
        }

        String toFileLine() {
            return type + "|" + symbol + "|" + quantity + "|" + pricePerShare + "|" + total + "|" + timestamp;
        }

        static Transaction fromFileLine(String line) {
            String[] p = line.split("\\|");
            Transaction t = new Transaction(p[0], p[1], Integer.parseInt(p[2]), Double.parseDouble(p[3]));
            t.timestamp = p[5];
            return t;
        }
    }

    // ---------- Portfolio (holdings + history for one user) ----------
    static class Portfolio {
        Map<String, Integer> holdings = new HashMap<>(); // symbol -> quantity
        List<Transaction> history = new ArrayList<>();

        void applyBuy(String symbol, int qty, double price) {
            holdings.merge(symbol, qty, Integer::sum);
            history.add(new Transaction("BUY", symbol, qty, price));
        }

        boolean applySell(String symbol, int qty, double price) {
            int owned = holdings.getOrDefault(symbol, 0);
            if (owned < qty) return false;
            int remaining = owned - qty;
            if (remaining == 0) holdings.remove(symbol);
            else holdings.put(symbol, remaining);
            history.add(new Transaction("SELL", symbol, qty, price));
            return true;
        }

        double marketValue(Map<String, Stock> market) {
            double total = 0;
            for (Map.Entry<String, Integer> e : holdings.entrySet()) {
                Stock s = market.get(e.getKey());
                if (s != null) total += s.price * e.getValue();
            }
            return total;
        }
    }

    // ---------- User ----------
    static class User {
        String username;
        double cashBalance;
        Portfolio portfolio = new Portfolio();

        User(String username, double startingCash) {
            this.username = username;
            this.cashBalance = startingCash;
        }

        double netWorth(Map<String, Stock> market) {
            return cashBalance + portfolio.marketValue(market);
        }
    }

    // ---------- Application state ----------
    private static final Map<String, Stock> market = new LinkedHashMap<>();
    private static final Scanner sc = new Scanner(System.in);
    private static final Random rnd = new Random();
    private static final String SAVE_FILE = "portfolio.txt";
    private static User user;
    private static final List<Double> netWorthHistory = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== Stock Trading Platform Simulator ===");
        initMarket();

        System.out.print("Enter your username: ");
        String name = sc.nextLine().trim();
        user = new User(name.isEmpty() ? "trader1" : name, 10000.00);
        loadPortfolio();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": displayMarket(); break;
                case "2": buyStock(); break;
                case "3": sellStock(); break;
                case "4": viewPortfolio(); break;
                case "5": simulateMarketTick(); break;
                case "6": viewTransactionHistory(); break;
                case "7": viewNetWorthTrend(); break;
                case "8": savePortfolio(); break;
                case "0":
                    savePortfolio();
                    running = false;
                    System.out.println("Session saved. Goodbye, " + user.username + "!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        sc.close();
    }

    private static void initMarket() {
        market.put("AAPL", new Stock("AAPL", "Apple Inc.", 190.00));
        market.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 145.00));
        market.put("TSLA", new Stock("TSLA", "Tesla Inc.", 250.00));
        market.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 178.00));
        market.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 420.00));
    }

    private static void printMenu() {
        System.out.println("\n--- Menu (" + user.username + " | Cash: $" + String.format("%.2f", user.cashBalance) + ") ---");
        System.out.println("1. View Market Data");
        System.out.println("2. Buy Stock");
        System.out.println("3. Sell Stock");
        System.out.println("4. View Portfolio");
        System.out.println("5. Simulate Market Tick (prices move)");
        System.out.println("6. View Transaction History");
        System.out.println("7. View Net Worth Trend");
        System.out.println("8. Save Portfolio Now");
        System.out.println("0. Exit & Save");
        System.out.print("Choose an option: ");
    }

    private static void displayMarket() {
        System.out.println("\n--- Market Data ---");
        System.out.printf("%-6s %-20s %-10s%n", "Sym", "Company", "Price");
        for (Stock s : market.values()) {
            System.out.printf("%-6s %-20s $%-10.2f%n", s.symbol, s.companyName, s.price);
        }
    }

    private static void buyStock() {
        Stock s = selectStock();
        if (s == null) return;
        System.out.print("Quantity to buy: ");
        int qty = readInt();
        if (qty <= 0) { System.out.println("Quantity must be positive."); return; }

        double cost = qty * s.price;
        if (cost > user.cashBalance) {
            System.out.printf("Insufficient funds. Cost: $%.2f, Available: $%.2f%n", cost, user.cashBalance);
            return;
        }
        user.cashBalance -= cost;
        user.portfolio.applyBuy(s.symbol, qty, s.price);
        System.out.printf("Bought %d shares of %s at $%.2f (total $%.2f)%n", qty, s.symbol, s.price, cost);
    }

    private static void sellStock() {
        Stock s = selectStock();
        if (s == null) return;
        System.out.print("Quantity to sell: ");
        int qty = readInt();
        if (qty <= 0) { System.out.println("Quantity must be positive."); return; }

        boolean success = user.portfolio.applySell(s.symbol, qty, s.price);
        if (!success) {
            System.out.println("You don't own enough shares of " + s.symbol + ".");
            return;
        }
        double proceeds = qty * s.price;
        user.cashBalance += proceeds;
        System.out.printf("Sold %d shares of %s at $%.2f (total $%.2f)%n", qty, s.symbol, s.price, proceeds);
    }

    private static void viewPortfolio() {
        System.out.println("\n--- Portfolio for " + user.username + " ---");
        System.out.printf("Cash Balance: $%.2f%n", user.cashBalance);
        if (user.portfolio.holdings.isEmpty()) {
            System.out.println("No holdings yet.");
        } else {
            System.out.printf("%-6s %-10s %-12s %-12s%n", "Sym", "Qty", "Price", "Value");
            for (Map.Entry<String, Integer> e : user.portfolio.holdings.entrySet()) {
                Stock s = market.get(e.getKey());
                double value = s.price * e.getValue();
                System.out.printf("%-6s %-10d $%-11.2f $%-11.2f%n", e.getKey(), e.getValue(), s.price, value);
            }
        }
        System.out.printf("Total Net Worth: $%.2f%n", user.netWorth(market));
    }

    private static void simulateMarketTick() {
        for (Stock s : market.values()) {
            s.updatePrice(rnd);
        }
        netWorthHistory.add(user.netWorth(market));
        System.out.println("Market prices updated!");
        displayMarket();
    }

    private static void viewTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        if (user.portfolio.history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : user.portfolio.history) {
            System.out.println(t);
        }
    }

    private static void viewNetWorthTrend() {
        System.out.println("\n--- Net Worth Trend (per market tick) ---");
        if (netWorthHistory.isEmpty()) {
            System.out.println("No history yet. Run a market tick first (option 5).");
            return;
        }
        for (int i = 0; i < netWorthHistory.size(); i++) {
            System.out.printf("Tick %d: $%.2f%n", i + 1, netWorthHistory.get(i));
        }
        System.out.printf("Current Net Worth: $%.2f%n", user.netWorth(market));
    }

    // ---------- File I/O persistence ----------
    private static void savePortfolio() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            pw.println("USER|" + user.username + "|" + user.cashBalance);
            for (Map.Entry<String, Integer> e : user.portfolio.holdings.entrySet()) {
                pw.println("HOLDING|" + e.getKey() + "|" + e.getValue());
            }
            for (Transaction t : user.portfolio.history) {
                pw.println("TX|" + t.toFileLine());
            }
            System.out.println("Portfolio saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    private static void loadPortfolio() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("USER|")) {
                    String[] p = line.split("\\|");
                    if (p[1].equals(user.username)) {
                        user.cashBalance = Double.parseDouble(p[2]);
                    }
                } else if (line.startsWith("HOLDING|")) {
                    String[] p = line.split("\\|");
                    user.portfolio.holdings.put(p[1], Integer.parseInt(p[2]));
                } else if (line.startsWith("TX|")) {
                    user.portfolio.history.add(Transaction.fromFileLine(line.substring(3)));
                }
            }
            System.out.println("Loaded existing portfolio from " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Error loading portfolio: " + e.getMessage());
        }
    }

    // ---------- Helpers ----------
    private static Stock selectStock() {
        displayMarket();
        System.out.print("Enter stock symbol: ");
        String sym = sc.nextLine().trim().toUpperCase();
        Stock s = market.get(sym);
        if (s == null) {
            System.out.println("Unknown symbol.");
            return null;
        }
        return s;
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid whole number: ");
            }
        }
    }
}
