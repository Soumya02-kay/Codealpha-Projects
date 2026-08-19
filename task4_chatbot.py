"""
TASK 4: Basic Chatbot
A simple rule-based chatbot that responds to predefined user inputs.

Concepts used: if-elif, functions, loops, input/output
"""

import random
from datetime import datetime

# Predefined responses. Each key phrase maps to one or more possible replies.
RESPONSES = {
    "hello": ["Hi there!", "Hello!", "Hey! How can I help you today?"],
    "hi": ["Hi there!", "Hello!", "Hey! How can I help you today?"],
    "how are you": ["I'm fine, thanks! How about you?", "Doing great, thanks for asking!"],
    "what is your name": ["I'm a simple rule-based chatbot.", "You can call me ChatBot."],
    "what time is it": [],  # handled dynamically below
    "help": ["I can chat about greetings, how you're doing, the time, and more. Try saying 'bye' to exit."],
    "thank you": ["You're welcome!", "No problem at all!"],
    "thanks": ["You're welcome!", "Happy to help!"],
    "bye": ["Goodbye!", "See you later!", "Bye! Have a great day!"],
}

EXIT_KEYWORDS = {"bye", "exit", "quit", "goodbye"}


def get_response(user_input):
    """Return an appropriate chatbot reply based on simple keyword matching."""
    text = user_input.lower().strip().strip("!?.")

    if not text:
        return "I didn't catch that. Could you say something?"

    if "time" in text:
        current_time = datetime.now().strftime("%I:%M %p")
        return f"The current time is {current_time}."

    for keyword, replies in RESPONSES.items():
        if keyword in text and replies:
            return random.choice(replies)

    # Fallback responses if no keyword matched
    fallback_replies = [
        "I'm not sure I understand. Could you rephrase that?",
        "Sorry, I don't know how to respond to that yet.",
        "Interesting! Tell me more, or try asking me something else.",
    ]
    return random.choice(fallback_replies)


def is_exit_command(user_input):
    text = user_input.lower().strip().strip("!?.")
    return any(keyword in text for keyword in EXIT_KEYWORDS)


def chat():
    print("=" * 50)
    print("SIMPLE CHATBOT")
    print("=" * 50)
    print("Type 'bye' to end the conversation.\n")

    while True:
        user_input = input("You: ")

        if is_exit_command(user_input):
            print(f"Bot: {random.choice(RESPONSES['bye'])}")
            break

        response = get_response(user_input)
        print(f"Bot: {response}")


if __name__ == "__main__":
    chat()
