package com.mycompany.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // Registered users (kept in memory while the program runs)
    private static final ArrayList<User> users = new ArrayList<>();

    // The user who is currently logged in
    private static User currentUser = null;

    public static void main(String[] args) {

        // 1. Users must register / log in before they can send messages.
        if (!startScreen()) {
            System.out.println("Exiting QuickChat. Goodbye!");
            return;
        }

        // 2. Welcome message
        System.out.println("Welcome to QuickChat, " + currentUser.username + ".");

        // 5. User decides up front how many messages they want to enter
        int maxMessages = readPositiveInt("How many messages do you wish to enter? ");
        int messagesEntered = 0;

        // 4. Run until the user chooses Quit
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.print("Option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    if (messagesEntered >= maxMessages) {
                        System.out.println("You have already entered all " + maxMessages
                                + " messages you planned to send.");
                    } else {
                        // Keep going until the planned number is reached (or the user stops)
                        while (messagesEntered < maxMessages) {
                            messagesEntered++;
                            boolean carryOn = captureMessage(messagesEntered - 1);
                            if (!carryOn) {
                                break;
                            }
                        }
                        if (messagesEntered >= maxMessages) {
                            System.out.println("\nTotal number of messages sent: "
                                    + Message.returnTotalMessages());
                        }
                    }
                    break;
                case "2":
                    System.out.println("Coming Soon.");
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2 or 3.");
            }
        }
    }

    // ==================================================================
    // Registration and login
    // ==================================================================

    // Shows Register / Login / Exit until the user logs in (true) or exits (false)
    private static boolean startScreen() {
        while (true) {
            System.out.println();
            System.out.println("=== QuickChat ===");
            System.out.println("1) Register");
            System.out.println("2) Login");
            System.out.println("3) Exit");
            System.out.print("Option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    register();
                    break;
                case "2":
                    if (login()) {
                        return true;
                    }
                    break;
                case "3":
                    return false;
                default:
                    System.out.println("Invalid option. Please enter 1, 2 or 3.");
            }
        }
    }

    // Registers a new user: username, password and South African cell phone number
    private static void register() {
        System.out.println("\n=== Register ===");

        // Username: keep asking until it is valid and not already taken
        String username;
        while (true) {
            System.out.print("Choose a username (no more than 5 characters, must contain an underscore): ");
            username = scanner.nextLine().trim();

            if (!checkUserName(username)) {
                System.out.println("Username is not correctly formatted. Please ensure that your "
                        + "username contains an underscore and is no more than five characters in length.");
            } else if (findUser(username) != null) {
                System.out.println("That username is already taken. Please choose another one.");
            } else {
                System.out.println("Username successfully captured.");
                break;
            }
        }

        // Password: keep asking until it meets the complexity rules
        String password;
        while (true) {
            System.out.print("Choose a password (at least 8 characters, with a capital letter, "
                    + "a number and a special character): ");
            password = scanner.nextLine().trim();

            if (checkPasswordComplexity(password)) {
                System.out.println("Password successfully captured.");
                break;
            }
            System.out.println("Password is not correctly formatted. Please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number and a special character.");
        }

        // Cell phone number: South African format
        String cell;
        while (true) {
            System.out.print("Enter your South African cell phone number (e.g. +27718693002): ");
            cell = scanner.nextLine().trim();

            if (checkSouthAfricanCell(cell)) {
                System.out.println("Cell phone number successfully added.");
                break;
            }
            System.out.println("Cell phone number is incorrectly formatted. It must start with +27 "
                    + "followed by 9 digits (e.g. +27718693002).");
        }

        users.add(new User(username, password, cell));
        System.out.println("\nRegistration successful. You can now log in.");
    }

    // Logs a user in with username and password. Returns true on success.
    private static boolean login() {
        System.out.println("\n=== Login ===");

        if (users.isEmpty()) {
            System.out.println("No users are registered yet. Please register first.");
            return false;
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = findUser(username);
        if (user != null && user.password.equals(password)) {
            currentUser = user;
            System.out.println("Login successful.");
            return true;
        }

        System.out.println("Username or password incorrect, please try again.");
        return false;
    }

    // Username: contains an underscore and is no more than 5 characters
    private static boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    // Password: at least 8 characters, a capital letter, a number and a special character
    private static boolean checkPasswordComplexity(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasCapital = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (!Character.isLetter(c)) {
                hasSpecial = true;
            }
        }
        return hasCapital && hasNumber && hasSpecial;
    }

    // South African cell number: +27 followed by exactly 9 digits (e.g. +27718693002)
    private static boolean checkSouthAfricanCell(String cell) {
        return cell.matches("^\\+27\\d{9}$");
    }

    // Looks up a registered user by username (case-sensitive); null if not found
    private static User findUser(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return u;
            }
        }
        return null;
    }

    // ==================================================================
    // Messaging
    // ==================================================================

    // Reads one message from the user, then lets them send / disregard / store it.
    // Returns false if the loop should stop early.
    private static boolean captureMessage(int messageNumber) {
        System.out.println("\n--- Message " + (messageNumber + 1) + " ---");

        // Recipient: keep asking until the number is valid
        String recipient;
        while (true) {
            System.out.print("Enter recipient cell number (with international code, e.g. +27718693002): ");
            recipient = scanner.nextLine().trim();
            Message check = new Message(messageNumber, recipient, "check");
            String result = check.checkRecipientCell();
            System.out.println(result);
            if (result.equals("Cell phone number successfully captured.")) {
                break;
            }
        }

        // Message text: keep asking until it is 250 characters or fewer
        String text;
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            text = scanner.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
                continue;
            }
            if (text.length() > 250) {
                System.out.println("Please enter a message of less than 250 characters.");
            } else {
                System.out.println("Message sent");
                break;
            }
        }

        Message message = new Message(messageNumber, recipient, text);
        System.out.println(message.getMessageIDMessage());
        System.out.println("Message Hash: " + message.getMessageHash());

        // Send / Disregard / Store
        while (true) {
            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Option: ");
            String option = scanner.nextLine().trim();

            if (option.equals("1") || option.equals("2") || option.equals("3")) {
                String result = message.sentMessage(Integer.parseInt(option));
                System.out.println(result);

                if (option.equals("1")) {
                    // 7. Show the full details after sending
                    System.out.println("\n" + message.getDetails());
                } else if (option.equals("2")) {
                    // "Press 0 to delete the message"
                    System.out.print("> ");
                    String del = scanner.nextLine().trim();
                    if (del.equals("0")) {
                        System.out.println("Message deleted.");
                    }
                }
                return true;
            }
            System.out.println("Invalid option. Please choose 1, 2 or 3.");
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }

    // ==================================================================
    // User class: holds one registered user's details
    // ==================================================================
    private static class User {
        private final String username;
        private final String password;
        private final String cellNumber;

        User(String username, String password, String cellNumber) {
            this.username = username;
            this.password = password;
            this.cellNumber = cellNumber;
        }
    }

    // ==================================================================
    // Message class (nested so everything lives in the single Main class)
    // ==================================================================
    public static class Message {

        // ---- Shared (static) data: lives for as long as the program runs ----
        private static final ArrayList<Message> sentMessages = new ArrayList<>();
        private static final ArrayList<Message> storedMessages = new ArrayList<>();
        private static int totalSent = 0;

        // ---- Data for ONE message ----
        private final String messageID;
        private final int messageNumber;
        private final String recipient;
        private final String messageText;
        private final String messageHash;

        // Constructor used by the real app: the ID is generated automatically
        public Message(int messageNumber, String recipient, String messageText) {
            this(generateMessageID(), messageNumber, recipient, messageText);
        }

        // Constructor used by the unit tests: lets us supply a known ID
        public Message(String messageID, int messageNumber, String recipient, String messageText) {
            this.messageID = messageID;
            this.messageNumber = messageNumber;
            this.recipient = recipient;
            this.messageText = messageText;
            this.messageHash = createMessageHash();
        }

        // Random ten-digit number (leading zeros allowed, e.g. 0012345678)
        private static String generateMessageID() {
            long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
            return String.format("%010d", number);
        }

        // Boolean: the message ID must be no more than ten characters
        public boolean checkMessageID() {
            return messageID.length() <= 10;
        }

        // String: the recipient must start with "+" (international code) and be digits only.
        // Country code (1-3 digits) followed by no more than ten digits.
        public String checkRecipientCell() {
            if (recipient != null && recipient.matches("^\\+\\d{1,3}\\d{1,10}$")) {
                return "Cell phone number successfully captured.";
            }
            return "Cell phone number is incorrectly formatted or does not contain an "
                    + "international code. Please correct the number and try again.";
        }

        // String: checks the 250 character limit (used by the unit tests)
        public String checkMessageLength() {
            if (messageText.length() <= 250) {
                return "Message ready to send.";
            }
            int tooMany = messageText.length() - 250;
            return "Message exceeds 250 characters by " + tooMany + "; please reduce the size.";
        }

        // Boolean version of the length check (used by the menu)
        public boolean isMessageLengthValid() {
            return messageText.length() <= 250;
        }

        // String: first two digits of ID : message number : FIRSTLAST (all caps)
        public String createMessageHash() {
            String firstTwo = messageID.substring(0, 2);

            String[] words = messageText.trim().split("\\s+");
            String firstWord = cleanWord(words[0]);
            String lastWord = cleanWord(words[words.length - 1]);

            return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
        }

        // Removes punctuation such as "," or "?" so "tonight?" becomes "tonight"
        private String cleanWord(String word) {
            return word.replaceAll("[^A-Za-z0-9]", "");
        }

        // String: handles the user's choice. 1 = send, 2 = disregard, 3 = store
        public String sentMessage(int choice) {
            switch (choice) {
                case 1:
                    sentMessages.add(this);
                    totalSent++;
                    return "Message successfully sent.";
                case 2:
                    return "Press 0 to delete the message.";
                case 3:
                    storeMessage();
                    return "Message successfully stored.";
                default:
                    return "Invalid option. Please choose 1, 2 or 3.";
            }
        }

        // String: all the messages sent while the program is running
        public static String printMessages() {
            if (sentMessages.isEmpty()) {
                return "No messages have been sent yet.";
            }
            StringBuilder sb = new StringBuilder();
            for (Message m : sentMessages) {
                sb.append(m.getDetails()).append("\n");
            }
            return sb.toString();
        }

        // Int: total number of messages sent
        public static int returnTotalMessages() {
            return totalSent;
        }

        // Your own method: stores the message in a JSON file
        public void storeMessage() {
            storedMessages.add(this);

            // Rewrite the whole file each time so it is always valid JSON
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < storedMessages.size(); i++) {
                Message m = storedMessages.get(i);
                json.append("  {\n");
                json.append("    \"messageID\": \"").append(escape(m.messageID)).append("\",\n");
                json.append("    \"messageHash\": \"").append(escape(m.messageHash)).append("\",\n");
                json.append("    \"recipient\": \"").append(escape(m.recipient)).append("\",\n");
                json.append("    \"message\": \"").append(escape(m.messageText)).append("\"\n");
                json.append("  }");
                if (i < storedMessages.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append("]");

            try (FileWriter writer = new FileWriter("messages.json")) {
                writer.write(json.toString());
            } catch (IOException e) {
                System.out.println("Could not save to JSON file: " + e.getMessage());
            }
        }

        // Makes text safe to place inside JSON quotes
        private String escape(String text) {
            return text.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        // Full details, in the order the brief asks for
        public String getDetails() {
            return "Message ID: " + messageID + "\n"
                    + "Message Hash: " + messageHash + "\n"
                    + "Recipient: " + recipient + "\n"
                    + "Message: " + messageText + "\n";
        }

        public String getMessageIDMessage() {
            return "Message ID generated: " + messageID;
        }

        public String getMessageID() { return messageID; }
        public String getMessageHash() { return messageHash; }

        // Used by the unit tests so each test starts fresh
        public static void resetAll() {
            sentMessages.clear();
            storedMessages.clear();
            totalSent = 0;
        }
    }
}