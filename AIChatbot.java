import java.util.*;
import java.util.regex.*;

/**
 * TASK 3: Artificial Intelligence Chatbot
 * Console-based Java chatbot using simple NLP techniques:
 *   - Text normalization (lowercase, punctuation stripping, tokenization)
 *   - Keyword/intent matching with a scoring algorithm (bag-of-words overlap)
 *   - Rule-based responses trained on a set of FAQ intents
 *
 * The design is modular: intents are stored in a Map<Intent, List<pattern words>>
 * so training data (FAQs) can easily be extended. A confidence score picks the
 * best-matching intent; if nothing scores high enough, a fallback is used.
 */
public class AIChatbot {

    // ---------- Intent model ----------
    static class Intent {
        String name;
        List<String> trainingPhrases; // example user phrases for this intent
        List<String> responses;       // possible bot replies (chosen randomly)

        Intent(String name, List<String> trainingPhrases, List<String> responses) {
            this.name = name;
            this.trainingPhrases = trainingPhrases;
            this.responses = responses;
        }
    }

    private static final List<Intent> intents = new ArrayList<>();
    private static final Random rnd = new Random();
    private static String userName = "Friend";

    public static void main(String[] args) {
        trainBot();
        Scanner sc = new Scanner(System.in);

        System.out.println("=== AI FAQ Chatbot (type 'bye' to exit) ===");
        System.out.println("Bot: Hi there! What's your name?");
        String nameInput = sc.nextLine().trim();
        if (!nameInput.isEmpty()) userName = capitalize(nameInput);
        System.out.println("Bot: Nice to meet you, " + userName + "! Ask me anything (try: hours, pricing, refund, shipping, contact, help, account, technical support).");

        while (true) {
            System.out.print(userName + ": ");
            String input = sc.nextLine();
            if (input == null) continue;
            String normalized = normalize(input);

            if (normalized.equals("bye") || normalized.equals("exit") || normalized.equals("quit")) {
                System.out.println("Bot: Goodbye, " + userName + "! Have a great day.");
                break;
            }

            String response = getResponse(normalized);
            System.out.println("Bot: " + response);
        }
        sc.close();
    }

    // ---------- Training data (FAQs) ----------
    private static void trainBot() {
        intents.add(new Intent("greeting",
                Arrays.asList("hello", "hi", "hey", "good morning", "good afternoon", "greetings", "yo"),
                Arrays.asList("Hello! How can I help you today?", "Hi there! What can I do for you?", "Hey! Ask me a question.")));

        intents.add(new Intent("hours",
                Arrays.asList("what are your hours", "when are you open", "business hours", "opening time", "closing time", "are you open now"),
                Arrays.asList("We're open Monday to Friday, 9 AM to 6 PM.", "Our support hours are 9 AM - 6 PM, Mon-Fri.")));

        intents.add(new Intent("pricing",
                Arrays.asList("how much does it cost", "what is the price", "pricing plans", "how much is the subscription", "cost of the plan"),
                Arrays.asList("Our basic plan starts at $9.99/month, with premium options available. Would you like details?",
                        "Pricing starts at $9.99/month. Check our website for the full plan comparison.")));

        intents.add(new Intent("refund",
                Arrays.asList("how do I get a refund", "refund policy", "cancel and get money back", "want a refund", "return policy"),
                Arrays.asList("We offer a 30-day money-back guarantee. Contact support with your order ID to start a refund.",
                        "Refunds are processed within 5-7 business days after approval.")));

        intents.add(new Intent("shipping",
                Arrays.asList("shipping time", "when will my order arrive", "delivery time", "track my order", "shipping cost"),
                Arrays.asList("Standard shipping takes 3-5 business days. Express options are available at checkout.",
                        "You can track your order using the link sent to your email after purchase.")));

        intents.add(new Intent("contact",
                Arrays.asList("how can I contact support", "customer service number", "email support", "talk to a human", "contact information"),
                Arrays.asList("You can reach our support team at support@example.com or call 1-800-555-0100.",
                        "Our team is available via live chat, email (support@example.com), or phone.")));

        intents.add(new Intent("account",
                Arrays.asList("reset my password", "forgot password", "cannot log in", "update my account", "delete my account"),
                Arrays.asList("To reset your password, click 'Forgot Password' on the login page.",
                        "For account changes, go to Settings > Account, or contact support for help.")));

        intents.add(new Intent("technical_support",
                Arrays.asList("app is not working", "bug report", "error message", "technical issue", "crashing", "not loading"),
                Arrays.asList("Sorry to hear that! Please try restarting the app. If the issue persists, email support@example.com with a screenshot.",
                        "That sounds like a technical issue. Can you tell me the exact error message you're seeing?")));

        intents.add(new Intent("thanks",
                Arrays.asList("thank you", "thanks", "appreciate it", "thanks a lot"),
                Arrays.asList("You're welcome!", "Happy to help!", "Anytime!")));

        intents.add(new Intent("help",
                Arrays.asList("help", "what can you do", "options", "menu", "commands"),
                Arrays.asList("I can help with: business hours, pricing, refunds, shipping, contacting support, account issues, and technical support. Just ask!")));
    }

    // ---------- NLP: normalization ----------
    private static String normalize(String text) {
        String lower = text.toLowerCase().trim();
        lower = lower.replaceAll("[^a-z0-9\\s]", ""); // strip punctuation
        lower = lower.replaceAll("\\s+", " ");
        return lower;
    }

    private static Set<String> tokenize(String text) {
        return new HashSet<>(Arrays.asList(text.split(" ")));
    }

    // Simple set of stopwords to reduce noise in matching
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "do", "does", "i", "my", "me", "to", "of", "for", "you", "your",
            "can", "how", "what", "when", "will", "it", "and", "in", "on", "get", "with"));

    private static Set<String> tokenizeMeaningful(String text) {
        Set<String> tokens = tokenize(text);
        tokens.removeIf(STOPWORDS::contains);
        return tokens;
    }

    // ---------- Intent matching (bag-of-words overlap scoring) ----------
    private static String getResponse(String normalizedInput) {
        Set<String> inputTokens = tokenizeMeaningful(normalizedInput);
        if (inputTokens.isEmpty()) inputTokens = tokenize(normalizedInput); // fallback if all stopwords

        Intent bestIntent = null;
        double bestScore = 0.0;

        for (Intent intent : intents) {
            double score = scoreIntent(intent, normalizedInput, inputTokens);
            if (score > bestScore) {
                bestScore = score;
                bestIntent = intent;
            }
        }

        // Confidence threshold: require reasonable overlap before committing to an intent
        if (bestIntent != null && bestScore >= 0.34) {
            List<String> responses = bestIntent.responses;
            return responses.get(rnd.nextInt(responses.size()));
        }

        return fallbackResponse();
    }

    private static double scoreIntent(Intent intent, String normalizedInput, Set<String> inputTokens) {
        double best = 0.0;
        for (String phrase : intent.trainingPhrases) {
            // exact substring match gets very high confidence
            if (normalizedInput.contains(phrase)) {
                best = Math.max(best, 1.0);
                continue;
            }
            Set<String> phraseTokens = tokenizeMeaningful(phrase);
            if (phraseTokens.isEmpty()) continue;

            Set<String> intersection = new HashSet<>(inputTokens);
            intersection.retainAll(phraseTokens);

            double overlap = (double) intersection.size() / phraseTokens.size();
            best = Math.max(best, overlap);
        }
        return best;
    }

    private static String fallbackResponse() {
        String[] fallbacks = {
                "I'm not sure I understand. Could you rephrase that?",
                "Sorry, I don't have an answer for that yet. Try asking about hours, pricing, refunds, or support.",
                "Hmm, I'm still learning! Can you ask that differently?"
        };
        return fallbacks[rnd.nextInt(fallbacks.length)];
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
