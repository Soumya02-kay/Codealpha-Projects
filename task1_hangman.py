"""
TASK 1: Hangman Game
A text-based Hangman game where the player guesses a word one letter at a time.

Concepts used: random, while loop, if-else, strings, lists
"""

import random

WORDS = ["python", "hangman", "developer", "keyboard", "programming"]
MAX_WRONG_GUESSES = 6

HANGMAN_STAGES = [
    """
       -----
       |   |
       |
       |
       |
       |
    ---------
    """,
    """
       -----
       |   |
       |   O
       |
       |
       |
    ---------
    """,
    """
       -----
       |   |
       |   O
       |   |
       |
       |
    ---------
    """,
    """
       -----
       |   |
       |   O
       |  /|
       |
       |
    ---------
    """,
    """
       -----
       |   |
       |   O
       |  /|\\
       |
       |
    ---------
    """,
    """
       -----
       |   |
       |   O
       |  /|\\
       |  /
       |
    ---------
    """,
    """
       -----
       |   |
       |   O
       |  /|\\
       |  / \\
       |
    ---------
    """,
]


def choose_word(word_list):
    """Randomly select a word from the list."""
    return random.choice(word_list).lower()


def display_word(word, guessed_letters):
    """Show the word with guessed letters revealed and others as underscores."""
    return " ".join([letter if letter in guessed_letters else "_" for letter in word])


def get_guess(guessed_letters):
    """Prompt the user for a single valid letter guess."""
    while True:
        guess = input("Guess a letter: ").lower().strip()

        if len(guess) != 1:
            print("Please enter exactly one letter.")
        elif not guess.isalpha():
            print("Please enter a valid alphabet letter.")
        elif guess in guessed_letters:
            print(f"You already guessed '{guess}'. Try a different letter.")
        else:
            return guess


def play_hangman():
    print("=" * 50)
    print("WELCOME TO HANGMAN")
    print("=" * 50)
    print(f"You have {MAX_WRONG_GUESSES} incorrect guesses allowed. Good luck!\n")

    word = choose_word(WORDS)
    guessed_letters = set()
    wrong_guesses = 0

    while wrong_guesses < MAX_WRONG_GUESSES:
        print(HANGMAN_STAGES[wrong_guesses])
        print("Word: " + display_word(word, guessed_letters))
        print(f"Wrong guesses: {wrong_guesses}/{MAX_WRONG_GUESSES}")
        print(f"Guessed letters: {', '.join(sorted(guessed_letters)) if guessed_letters else 'None'}")

        guess = get_guess(guessed_letters)
        guessed_letters.add(guess)

        if guess in word:
            print(f"Good guess! '{guess}' is in the word.\n")
            if all(letter in guessed_letters for letter in word):
                print(HANGMAN_STAGES[wrong_guesses])
                print("Word: " + display_word(word, guessed_letters))
                print(f"\nCongratulations! You guessed the word: '{word}'")
                break
        else:
            wrong_guesses += 1
            print(f"Sorry, '{guess}' is not in the word.\n")
    else:
        print(HANGMAN_STAGES[wrong_guesses])
        print(f"\nGame over! You've run out of guesses. The word was: '{word}'")

    print("\nThanks for playing!")


def main():
    play_again = "y"
    while play_again == "y":
        play_hangman()
        play_again = input("\nWould you like to play again? (y/n): ").lower().strip()

    print("Goodbye!")


if __name__ == "__main__":
    main()
