"""
TASK 3: Task Automation with Python Scripts
Automates small, real-life repetitive tasks. Includes all three suggested ideas,
selectable from a simple menu.

  1. Move all .jpg files from a folder to a new folder.
  2. Extract all email addresses from a .txt file and save them to another file.
  3. Scrape the title of a fixed webpage and save it.

Concepts used: os, shutil, re, requests, file handling
"""

import os
import shutil
import re

try:
    import requests
except ImportError:
    requests = None


def move_jpg_files():
    """Move all .jpg files from a source folder to a destination folder."""
    source = input("Enter the source folder path: ").strip()
    destination = input("Enter the destination folder path: ").strip()

    if not os.path.isdir(source):
        print(f"Error: Source folder '{source}' does not exist.")
        return

    if not os.path.isdir(destination):
        os.makedirs(destination)
        print(f"Created destination folder: {destination}")

    moved_count = 0
    for filename in os.listdir(source):
        if filename.lower().endswith(".jpg"):
            src_path = os.path.join(source, filename)
            dst_path = os.path.join(destination, filename)
            shutil.move(src_path, dst_path)
            print(f"Moved: {filename}")
            moved_count += 1

    if moved_count == 0:
        print("No .jpg files found in the source folder.")
    else:
        print(f"\nDone! Moved {moved_count} .jpg file(s) to '{destination}'.")


def extract_emails():
    """Extract all email addresses from a .txt file and save them to another file."""
    input_file = input("Enter the path to the input .txt file: ").strip()

    if not os.path.isfile(input_file):
        print(f"Error: File '{input_file}' does not exist.")
        return

    output_file = input("Enter the path for the output file (e.g., emails.txt): ").strip()
    if not output_file:
        output_file = "emails_extracted.txt"

    email_pattern = r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"

    with open(input_file, "r", encoding="utf-8", errors="ignore") as f:
        content = f.read()

    emails = re.findall(email_pattern, content)
    unique_emails = sorted(set(emails))

    with open(output_file, "w") as f:
        for email in unique_emails:
            f.write(email + "\n")

    print(f"\nFound {len(unique_emails)} unique email address(es).")
    print(f"Saved to '{output_file}'.")


def scrape_webpage_title():
    """Scrape the title of a webpage and save it to a file."""
    if requests is None:
        print("Error: the 'requests' library is not installed. Install it with:")
        print("  pip install requests beautifulsoup4")
        return

    try:
        from bs4 import BeautifulSoup
    except ImportError:
        print("Error: the 'beautifulsoup4' library is not installed. Install it with:")
        print("  pip install beautifulsoup4")
        return

    url = input("Enter the webpage URL (e.g., https://example.com): ").strip()

    if not url.startswith(("http://", "https://")):
        url = "https://" + url

    try:
        response = requests.get(url, timeout=10, headers={"User-Agent": "Mozilla/5.0"})
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print(f"Error fetching the webpage: {e}")
        return

    soup = BeautifulSoup(response.text, "html.parser")
    title = soup.title.string.strip() if soup.title and soup.title.string else "No title found"

    output_file = input("Enter output filename (e.g., title.txt): ").strip()
    if not output_file:
        output_file = "page_title.txt"

    with open(output_file, "w") as f:
        f.write(f"URL: {url}\n")
        f.write(f"Title: {title}\n")

    print(f"\nPage title: {title}")
    print(f"Saved to '{output_file}'.")


def main():
    while True:
        print("\n" + "=" * 50)
        print("TASK AUTOMATION MENU")
        print("=" * 50)
        print("1. Move all .jpg files to a new folder")
        print("2. Extract emails from a .txt file")
        print("3. Scrape the title of a webpage")
        print("4. Exit")

        choice = input("\nSelect an option (1-4): ").strip()

        if choice == "1":
            move_jpg_files()
        elif choice == "2":
            extract_emails()
        elif choice == "3":
            scrape_webpage_title()
        elif choice == "4":
            print("Goodbye!")
            break
        else:
            print("Invalid option. Please choose 1, 2, 3, or 4.")


if __name__ == "__main__":
    main()
