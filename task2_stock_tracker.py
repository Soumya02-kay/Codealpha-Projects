"""
TASK 2: Stock Portfolio Tracker
Calculates total investment value based on manually defined stock prices.

Concepts used: dictionary, input/output, basic arithmetic, file handling (optional)
"""

import csv
from datetime import datetime

# Hardcoded stock prices (in USD)
STOCK_PRICES = {
    "AAPL": 180,
    "TSLA": 250,
    "GOOGL": 140,
    "AMZN": 175,
    "MSFT": 420,
    "META": 480,
    "NFLX": 650,
}


def display_available_stocks():
    print("\nAvailable stocks and current prices:")
    print("-" * 35)
    for symbol, price in STOCK_PRICES.items():
        print(f"  {symbol:<8} ${price:,.2f}")
    print("-" * 35)


def get_portfolio_input():
    """Collect stock symbols and quantities from the user."""
    portfolio = {}

    while True:
        symbol = input("\nEnter stock symbol (or 'done' to finish): ").upper().strip()

        if symbol == "DONE":
            break

        if symbol not in STOCK_PRICES:
            print(f"'{symbol}' not found in our price list. Please choose from the available stocks.")
            continue

        while True:
            qty_input = input(f"Enter quantity of {symbol} shares: ").strip()
            try:
                quantity = int(qty_input)
                if quantity < 0:
                    print("Quantity cannot be negative.")
                    continue
                break
            except ValueError:
                print("Please enter a valid whole number.")

        portfolio[symbol] = portfolio.get(symbol, 0) + quantity
        print(f"Added {quantity} shares of {symbol}.")

    return portfolio


def calculate_investment(portfolio):
    """Calculate per-stock value and total investment."""
    breakdown = []
    total = 0.0

    for symbol, quantity in portfolio.items():
        price = STOCK_PRICES[symbol]
        value = price * quantity
        total += value
        breakdown.append((symbol, quantity, price, value))

    return breakdown, total


def display_summary(breakdown, total):
    print("\n" + "=" * 55)
    print("PORTFOLIO SUMMARY")
    print("=" * 55)
    print(f"{'Symbol':<10}{'Qty':<8}{'Price':<12}{'Total Value':<15}")
    print("-" * 55)

    for symbol, quantity, price, value in breakdown:
        print(f"{symbol:<10}{quantity:<8}${price:<11,.2f}${value:<14,.2f}")

    print("-" * 55)
    print(f"{'TOTAL INVESTMENT:':<30}${total:,.2f}")
    print("=" * 55)


def save_to_file(breakdown, total):
    """Optionally save the portfolio summary to a .txt or .csv file."""
    choice = input("\nWould you like to save this summary to a file? (y/n): ").lower().strip()

    if choice != "y":
        return

    file_format = input("Choose format - 'txt' or 'csv': ").lower().strip()
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    if file_format == "csv":
        filename = f"portfolio_summary_{timestamp}.csv"
        with open(filename, "w", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(["Symbol", "Quantity", "Price", "Total Value"])
            for symbol, quantity, price, value in breakdown:
                writer.writerow([symbol, quantity, price, value])
            writer.writerow([])
            writer.writerow(["", "", "TOTAL", total])
        print(f"Saved to {filename}")

    elif file_format == "txt":
        filename = f"portfolio_summary_{timestamp}.txt"
        with open(filename, "w") as f:
            f.write("PORTFOLIO SUMMARY\n")
            f.write("=" * 55 + "\n")
            f.write(f"{'Symbol':<10}{'Qty':<8}{'Price':<12}{'Total Value':<15}\n")
            f.write("-" * 55 + "\n")
            for symbol, quantity, price, value in breakdown:
                f.write(f"{symbol:<10}{quantity:<8}${price:<11,.2f}${value:<14,.2f}\n")
            f.write("-" * 55 + "\n")
            f.write(f"TOTAL INVESTMENT: ${total:,.2f}\n")
        print(f"Saved to {filename}")

    else:
        print("Unrecognized format. Skipping file save.")


def main():
    print("=" * 55)
    print("STOCK PORTFOLIO TRACKER")
    print("=" * 55)

    display_available_stocks()
    portfolio = get_portfolio_input()

    if not portfolio:
        print("\nNo stocks entered. Exiting.")
        return

    breakdown, total = calculate_investment(portfolio)
    display_summary(breakdown, total)
    save_to_file(breakdown, total)

    print("\nThank you for using the Stock Portfolio Tracker!")


if __name__ == "__main__":
    main()
