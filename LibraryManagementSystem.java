package LibraryManagementSystem;

import java.sql.*;
import java.util.*;

public class LibraryManagementSystem {
    // HashMap for storing books (bookId -> BookDetails)
    private static HashMap<Integer, String> books = new HashMap<>();

    // HashSet for storing unique member IDs
    private static HashSet<Integer> members = new HashSet<>();

    // TreeSet for storing available books in sorted order
    private static TreeSet<Integer> availableBooks = new TreeSet<>();

    // TreeMap for storing transactions (issueDate -> TransactionDetails)
    private static TreeMap<String, String> transactions = new TreeMap<>();

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/lbs_db";
    private static final String USER = "root";
    private static final String PASSWORD = "mangesh";

    public static void main(String[] args) {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to the database
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("Connected to the database.");

                // Initialize the system with existing data from the database
                loadBooks(conn);
                loadMembers(conn);

                // Menu
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.println("\n--- Library Management System ---");
                    System.out.println("1. Add Book");
                    System.out.println("2. Add Member");
                    System.out.println("3. Issue Book");
                    System.out.println("4. Return Book");
                    System.out.println("5. View Available Books");
                    System.out.println("6. View Transactions");
                    System.out.println("7. Exit");
                    System.out.print("Choose an option: ");

                    int choice = scanner.nextInt();
                    switch (choice) {
                        case 1 -> addBook(conn, scanner);
                        case 2 -> addMember(conn, scanner);
                        case 3 -> issueBook(conn, scanner);
                        case 4 -> returnBook(conn, scanner);
                        case 5 -> viewAvailableBooks();
                        case 6 -> viewTransactions();
                        case 7 -> {
                            System.out.println("Exiting...");
                            return;
                        }
                        default -> System.out.println("Invalid option. Try again.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadBooks(Connection conn) throws SQLException {
        String query = "SELECT book_id, title FROM books";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                books.put(bookId, title);
                availableBooks.add(bookId);
            }
        }
    }

    private static void loadMembers(Connection conn) throws SQLException {
        String query = "SELECT member_id FROM members";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                members.add(memberId);
            }
        }
    }

    private static void addBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();

        if (books.containsKey(bookId)) {
            System.out.println("Book ID already exists.");
        } else {
            String query = "INSERT INTO books (book_id, title) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, bookId);
                pstmt.setString(2, title);
                pstmt.executeUpdate();

                books.put(bookId, title);
                availableBooks.add(bookId);
                System.out.println("Book added successfully.");
            }
        }
    }

    private static void addMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Member ID: ");
        int memberId = scanner.nextInt();

        if (members.contains(memberId)) {
            System.out.println("Member ID already exists.");
        } else {
            String query = "INSERT INTO members (member_id) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, memberId);
                pstmt.executeUpdate();

                members.add(memberId);
                System.out.println("Member added successfully.");
            }
        }
    }

    private static void issueBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter Member ID: ");
        int memberId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Issue Date (YYYY-MM-DD): ");
        String issueDate = scanner.nextLine();

        if (!books.containsKey(bookId)) {
            System.out.println("Invalid Book ID.");
        } else if (!members.contains(memberId)) {
            System.out.println("Invalid Member ID.");
        } else if (!availableBooks.contains(bookId)) {
            System.out.println("Book is not available.");
        } else {
            String query = "INSERT INTO transactions (book_id, member_id, issue_date) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, bookId);
                pstmt.setInt(2, memberId);
                pstmt.setString(3, issueDate);
                pstmt.executeUpdate();

                availableBooks.remove(bookId);
                transactions.put(issueDate, "Book ID: " + bookId + ", Member ID: " + memberId);
                System.out.println("Book issued successfully.");
            }
        }
    }

    private static void returnBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();

        if (availableBooks.contains(bookId)) {
            System.out.println("This book was not issued.");
        } else {
            String query = "DELETE FROM transactions WHERE book_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, bookId);
                pstmt.executeUpdate();

                availableBooks.add(bookId);
                System.out.println("Book returned successfully.");
            }
        }
    }

    private static void viewAvailableBooks() {
        System.out.println("\nAvailable Books:");
        for (int bookId : availableBooks) {
            System.out.println("Book ID: " + bookId + ", Title: " + books.get(bookId));
        }
    }

    private static void viewTransactions() {
        System.out.println("\nTransactions:");
        for (Map.Entry<String, String> entry : transactions.entrySet()) {
            System.out.println("Date: " + entry.getKey() + ", Details: " + entry.getValue());
        }
    }

}
