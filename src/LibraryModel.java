
/*
 * LibraryModel.java
 * Author:
 * Created on:
 */

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.swing.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class LibraryModel {

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;
    private DBConnect dbConnect;

    public LibraryModel(JFrame parent, String userid, String password) {
        dialogParent = parent;
        this.dbConnect = new DBConnect(userid, password);
//        this.dbConnect.getConn().setAutoCommit(false);
    }

    public String authorIsbn = "select isbn, string_agg(full_name, ', ') agg_author \n" +
            "from ( \n" +
            "select ba.isbn, authorseqno , ltrim(concat(coalesce(rtrim(name), ''), ' ', rtrim(surname))) full_name \n" +
            "from author a \n" +
            "left join book_author ba \n" +
            "on ba.authorid = a.authorid \n" +
            " where a.authorid != 0 " +
            "order by ba.isbn, authorseqno ) jpa \n" +
            "group by isbn ";

    public String authorTitle = "select ba.authorid, string_agg(rtrim(title), ', ') titles \n" +
            "from book_author ba  \n" +
            "left join book b \n" +
            "on ba.isbn = b.isbn  \n" +
            " where ba.isbn > 0 and ba.authorid > 0 " +
            "group by ba.authorid ";

    public String custBookTitle = "select cbs.customerid, string_agg(rtrim(title), ', ') titles \n" +
            "from cust_book cbs\n" +
            "left join book b\n" +
            "on cbs.isbn = b.isbn\n" +
            "where cbs.isbn > 0 and cbs.customerid > 0\n" +
            "group by cbs.customerid ";

    public String customerSelect = "select c.customerid, " +
            " ltrim(concat(coalesce(rtrim(f_name), ''), ' ', rtrim(l_name))) full_name, " +
            " coalesce(city, '') city, cb.titles \n" +
            " from customer c left join \n" +
            " ( " +  custBookTitle +" ) cb on cb.customerid = c.customerid \n";

    public String bookLookup(int isbn) {

        String stmtStr = "select b.*, ai.agg_author from book b " +
                "left join ( " + this.authorIsbn + ") as ai " +
                "on ai.isbn = b.isbn " +
                "where b.isbn = ? " ;
        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(stmtStr);
            stmt.setInt(1, isbn);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            List<Book> bookList = new ArrayList<>();
            StringBuilder sb = new StringBuilder(createHeader("Book Lookup for ID " + isbn));

            //TODO Check if rs is null for an ISBN present another statement
            while(rs.next()) {
                Book book = new Book(
                        rs.getInt("isbn"),
                        rs.getString("title").strip(),
                        rs.getInt("edition_no"),
                        rs.getInt("numofcop"),
                        rs.getInt("numleft"),
                        rs.getString("agg_author")
                );

                bookList.add(book);
            }
             String result = bookList.isEmpty() ? String.format("No Record for ISBN %s", isbn)
                     : bookList.get(0).toString();
            sb.append(result).append("\n");
            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Book Look up not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Look up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String showCatalogue() {


        String stmtStr = "select b.*, ai.agg_author from book b " +
                    "left join ( " + this.authorIsbn + ") as ai " +
                    "on ai.isbn = b.isbn  order by b.isbn";
        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(stmtStr);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            StringBuilder sb = new StringBuilder(createHeader("Book Catalogue"));

            while(rs.next()) {
                Book book = new Book(
                        rs.getInt("isbn"),
                        rs.getString("title").strip(),
                        rs.getInt("edition_no"),
                        rs.getInt("numofcop"),
                        rs.getInt("numleft"),
                        rs.getString("agg_author")
                );

                sb.append(book.toString()).append("\n");
            }

            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Book Catalogue not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Catalogue up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String showLoanedBooks() {

        String stmtStr = "select b.*, ai.agg_author from book b " +
                "left join ( " + this.authorIsbn + ") as ai " +
                "on ai.isbn = b.isbn  " +
                "where (b.numofcop - b.numleft) > 0 " +
                "order by b.isbn";

        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(stmtStr);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            StringBuilder sb = new StringBuilder(createHeader("Loaned Books"));

            while(rs.next()) {
                Book book = new Book(
                        rs.getInt("isbn"),
                        rs.getString("title").strip(),
                        rs.getInt("edition_no"),
                        rs.getInt("numofcop"),
                        rs.getInt("numleft"),
                        rs.getString("agg_author")
                );

                sb.append(book.toString()).append("\n");

            }

            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Loaned Book Look up not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Look up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String showAuthor(int authorID) {

        String stmtStr = "select a.authorid, " +
                "ltrim(concat(coalesce(rtrim(a.name), ''), ' ', rtrim(a.surname))) full_name, " +
                " ab.titles \n" +
                "from author a\n" +
                "left join \n" +
                "(\n" + authorTitle +
                " ) ab on ab.authorid = a.authorid \n" +
                "where a.authorid = ? ";

        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(stmtStr);
            stmt.setInt(1, authorID);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            List<Author> authorList = new ArrayList<>();
            StringBuilder sb = new StringBuilder(createHeader("Author Lookup for ID " + authorID));

            while(rs.next()) {
                Author author = new Author(
                        rs.getInt("authorid"),
                        rs.getString("full_name"),
                        rs.getString("titles")
                );

                authorList.add(author);
            }
            String result = authorList.isEmpty() ? String.format("No Record for Author of ID %s", authorID)
                    : authorList.get(0).toString();
            sb.append(result).append("\n");
            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Author Look up not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Look up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String showAllAuthors() {

        String stmtStr = "select a.authorid, " +
                "ltrim(concat(coalesce(rtrim(a.name), ''), ' ', rtrim(a.surname))) full_name, " +
                " ab.titles \n" +
                "from author a\n" +
                "left join \n" +
                "(\n" + authorTitle +
                " ) ab on ab.authorid = a.authorid\n" ;

        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(stmtStr);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            StringBuilder sb = new StringBuilder(createHeader("Authors List"));

            while(rs.next()) {
                Author author = new Author(
                        rs.getInt("authorid"),
                        rs.getString("full_name"),
                        rs.getString("titles")
                );

                sb.append(author.toString()).append("\n");
            }

            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Author Look up not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Look up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String showCustomer(int customerID) {

        String stmtStr = customerSelect + " where c.customerid = ? ";
        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(stmtStr);
            stmt.setInt(1, customerID);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            List<Customer> custList = new ArrayList<>();
            StringBuilder sb = new StringBuilder(createHeader("Customer Lookup for ID " + customerID));

            while(rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customerid"),
                        rs.getString("full_name"),
                        rs.getString("city").strip(),
                        rs.getString("titles")
                );

                custList.add(customer);
            }
            String result = custList.isEmpty() ? String.format("No Record for Customer of ID %s", customerID)
                    : custList.get(0).toString();
            sb.append(result).append("\n");
            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Customer look up not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Look up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String showAllCustomers() {

        PreparedStatement stmt = null;

        try {
            stmt = this.dbConnect.getConn().prepareStatement(customerSelect);
            stmt.execute();

            ResultSet rs = stmt.getResultSet();
            StringBuilder sb = new StringBuilder(createHeader("Customers List"));

            while(rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customerid"),
                        rs.getString("full_name"),
                        rs.getString("city").strip(),
                        rs.getString("titles")
                );

                sb.append(customer.toString()).append("\n");
            }

            return sb.toString();

        } catch (SQLException e) {
            System.out.println("The Customer look up not working with Exception " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                System.out.println("The Book Look up not closed with Exception " + ex.getMessage());
            }
        }

        return "Nothing to Show";
    }

    public String borrowBook(int isbn, int customerID,
                             int day, int month, int year) {

        Connection conn = this.dbConnect.getConn();
        PreparedStatement checkCustomerStmt = null;
        PreparedStatement lockBookStmt = null;
        PreparedStatement checkCustBookStmt = null;
        PreparedStatement insertCustBookStmt = null;
        PreparedStatement updateBookStmt = null;

        StringBuilder sb = new StringBuilder(createHeader(
                String.format("Borrow Book for CustomerID %s and Book ISBN %s", customerID, isbn)));

        String result = null;

        try {
            conn.setAutoCommit(false);

            // 1) check if the customer exists or not and lock
            String checkCustomerSQL = "select 1 from customer WHERE customerid = ? FOR UPDATE";
            checkCustomerStmt = conn.prepareStatement(checkCustomerSQL);
            checkCustomerStmt.setInt(1, customerID);
            ResultSet rs = checkCustomerStmt.executeQuery();
            if (!rs.next()) {
                result = "Customer does not exist";
                throw new SQLException("Customer does not exist");
            }

            // 2) Lock the book
            String lockBookSQL = "select 1 from book where isbn = ? and numleft > 0 FOR UPDATE";
            lockBookStmt = conn.prepareStatement(lockBookSQL);
            lockBookStmt.setInt(1, isbn);
            rs = lockBookStmt.executeQuery();
            if (!rs.next()) {
                result = "Book is not available";
                throw new SQLException("Book not available");
            }

            // 2a) Check if the customer has the same book as well
            String checkCustBookStmtSql = "select 1 from cust_book where isbn = ? and customerid = ? FOR UPDATE";
            checkCustBookStmt = conn.prepareStatement(checkCustBookStmtSql);
            checkCustBookStmt.setInt(1, isbn);
            checkCustBookStmt.setInt(2, customerID);
            rs = checkCustBookStmt.executeQuery();
            if (rs.next()) {
                result = "The Customer has the same book.";
                throw new SQLException("The Customer has the same book.");
            }

            //3) Insert a tuple in cust_book
            String insertCustBookSQL = "insert into cust_book (isbn, duedate, customerid) values (?, ?, ?)";
            Date dueDate = Date.valueOf(year + "-" + month + "-" + day);
            insertCustBookStmt = conn.prepareStatement(insertCustBookSQL);
            insertCustBookStmt.setInt(1, isbn);
            insertCustBookStmt.setDate(2, dueDate);
            insertCustBookStmt.setInt(3, customerID);
            insertCustBookStmt.executeUpdate();

            JOptionPane.showMessageDialog(this.dialogParent,
                    "Press OK to continue.", "Pause", JOptionPane.INFORMATION_MESSAGE);

            //4) Update the book
            String updateBookSQL = "update book set numleft = numleft - 1 WHERE isbn = ?";
            updateBookStmt = conn.prepareStatement(updateBookSQL);
            updateBookStmt.setInt(1, isbn);
            updateBookStmt.executeUpdate();

            //5) Commit it
            conn.commit();

            result = "The Book is borrowed";

        } catch (SQLException e) {

            // Rollback the transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println(e.getMessage());
        } finally {
            // Clean up and release resources
            try {
                if (checkCustomerStmt != null) checkCustomerStmt.close();
                if (lockBookStmt != null) lockBookStmt.close();
                if (checkCustBookStmt != null) checkCustBookStmt.close();
                if (insertCustBookStmt != null) insertCustBookStmt.close();
                if (updateBookStmt != null) updateBookStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sb.append(result).append("\n");
        return sb.toString();
    }

    public String returnBook(int isbn, int customerid) {

        Connection conn = this.dbConnect.getConn();
        PreparedStatement checkCustomerStmt = null;
        PreparedStatement checkCustBookStmt = null;
        PreparedStatement lockBookStmt = null;
        PreparedStatement deleteCustBookStmt = null;
        PreparedStatement updateBookStmt = null;

        StringBuilder sb = new StringBuilder(createHeader(
                String.format("Return Book for CustomerID %s and Book ISBN %s", customerid, isbn)));

        String result = null;

        try {

            conn.setAutoCommit(false);

            // 1) Check if the customer exist
            String checkCustomerSQL = "select 1 from customer WHERE customerid = ? FOR UPDATE";
            checkCustomerStmt = conn.prepareStatement(checkCustomerSQL);
            checkCustomerStmt.setInt(1, customerid);
            ResultSet rs = checkCustomerStmt.executeQuery();
            if (!rs.next()) {
                result = "Customer does not exist";
                throw new SQLException("Customer does not exist");
            }

            // 2) Lock the book for update
            String lockBookStmtSql = "select 1 from book where isbn = ? FOR UPDATE";
            lockBookStmt = conn.prepareStatement(lockBookStmtSql);
            lockBookStmt.setInt(1, isbn);
            rs = lockBookStmt.executeQuery();
            if (!rs.next()) {
                result = "The book does exist.";
                throw new SQLException("The customer does not have that book");
            }

            // 2a) Check there is record with isbn and customer
            String checkCustBookStmtSql = "select 1 from cust_book where isbn = ? and customerid = ? FOR UPDATE";
            checkCustBookStmt = conn.prepareStatement(checkCustBookStmtSql);
            checkCustBookStmt.setInt(1, isbn);
            checkCustBookStmt.setInt(2, customerid);
            rs = checkCustBookStmt.executeQuery();
            if (!rs.next()) {
                result = "The Customer does not have that book.";
                throw new SQLException("The customer does not have that book");
            }

            // 3) Delete the cust book record
            String deleteCustBookSQL = "delete from cust_book where isbn = ? and customerid = ? ";
            deleteCustBookStmt = conn.prepareStatement(deleteCustBookSQL);
            deleteCustBookStmt.setInt(1, isbn);
            deleteCustBookStmt.setInt(2, customerid);
            deleteCustBookStmt.executeUpdate();

            JOptionPane.showMessageDialog(this.dialogParent,
                    "Press OK to continue.", "Pause", JOptionPane.INFORMATION_MESSAGE);

            //4) Update the book
            String updateBookSQL = "update book set numleft = numleft + 1 WHERE isbn = ? ";
            updateBookStmt = conn.prepareStatement(updateBookSQL);
            updateBookStmt.setInt(1, isbn);
            updateBookStmt.executeUpdate();

            //5) Commit it
            conn.commit();

            result = "The Book is returned";

        } catch (SQLException e) {

            // Rollback the transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println(e.getMessage());
        } finally {
            // Clean up and release resources
            try {
                if (checkCustomerStmt != null) checkCustomerStmt.close();
                if (checkCustBookStmt != null) checkCustBookStmt.close();
                if (lockBookStmt != null) lockBookStmt.close();
                if (deleteCustBookStmt != null) deleteCustBookStmt.close();
                if (updateBookStmt != null) updateBookStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sb.append(result).append("\n");
        return sb.toString();
    }

    public void closeDBConnection() {
        try {
            this.dbConnect.getConn().close();
            System.out.println("Connection is closed");
        } catch (SQLException e) {
            System.out.println("The close connection failed with error " + e.getMessage());
        }
    }

    public String deleteCus(int customerID) {
        Connection conn = this.dbConnect.getConn();
        PreparedStatement checkCustomerStmt = null;
        PreparedStatement checkCustBookStmt = null;
        PreparedStatement deleteCustomerStmt = null;


        StringBuilder sb = new StringBuilder(createHeader(
                String.format("Deleting Customer of CustomerID %s ", customerID)));

        String result = null;

        try {

            conn.setAutoCommit(false);

            // 1) Check if the customer exist
            String checkCustomerSQL = "select 1 from customer WHERE customerid = ? FOR UPDATE";
            checkCustomerStmt = conn.prepareStatement(checkCustomerSQL);
            checkCustomerStmt.setInt(1, customerID);
            ResultSet rs = checkCustomerStmt.executeQuery();
            if (!rs.next()) {
                result = "Customer does not exist";
                throw new SQLException("Customer does not exist");
            }

            // 2) Check if the customer has borrowed the book
            String checkCustBookStmtSql = "select 1 from cust_book where customerid = ? FOR UPDATE";
            checkCustBookStmt = conn.prepareStatement(checkCustBookStmtSql);
            checkCustBookStmt.setInt(1, customerID);
            rs = checkCustBookStmt.executeQuery();
            if (rs.next()) {
                result = "The Customer had borrowed book. Please return before deletion. \n DELETION DENIED\n";
                throw new SQLException("The customer owns a book. Can not delete");
            }

            // 3) Delete the cust book record
            String deleteCustBookSQL = "delete from customer where customerid = ? ";
            deleteCustomerStmt = conn.prepareStatement(deleteCustBookSQL);
            deleteCustomerStmt.setInt(1, customerID);
            deleteCustomerStmt.executeUpdate();

            //5) Commit it
            conn.commit();

            result = "Deleted the customer";

        } catch (SQLException e) {

            // Rollback the transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println(e.getMessage());
        } finally {
            // Clean up and release resources
            try {
                if (checkCustomerStmt != null) checkCustomerStmt.close();
                if (checkCustBookStmt != null) checkCustBookStmt.close();
                if (deleteCustomerStmt != null) deleteCustomerStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sb.append(result).append("\n");
        return sb.toString();
    }

    public String deleteAuthor(int authorID) {
        Connection conn = this.dbConnect.getConn();
        PreparedStatement checkAuthorStmt = null;
        PreparedStatement checkBookAuthorStmt = null;
        PreparedStatement deleteBookAuthorStmt = null;
        PreparedStatement deleteAuthorStmt = null;


        StringBuilder sb = new StringBuilder(createHeader(
                String.format("Deleting Author of AuthorID %s ", authorID)));

        String result = null;

        try {

            conn.setAutoCommit(false);

            // 1) Check if the Author exist
            String checkAuthorSQL = "select 1 from author WHERE authorid = ? FOR UPDATE";
            checkAuthorStmt = conn.prepareStatement(checkAuthorSQL);
            checkAuthorStmt.setInt(1, authorID);
            ResultSet rs = checkAuthorStmt.executeQuery();
            if (!rs.next()) {
                result = "Author does not exist";
                throw new SQLException("Author does not exist");
            }

            // 2) Check if the author has any the book
            String checkBookAuthorSql = "select b.title, b.isbn from book_author ba " +
                    " join book b on ba.isbn = b.isbn where authorid = ? ";
            checkBookAuthorStmt = conn.prepareStatement(checkBookAuthorSql);
            checkBookAuthorStmt.setInt(1, authorID);
            rs = checkBookAuthorStmt.executeQuery();
            String books = "The Author has book(s) \n";
            sb.append(books);
            List<String> bookList = new ArrayList<>();
            while (rs.next()) {
                sb.append("\t").append(rs.getString("title").strip()).append("\n");
                bookList.add(String.valueOf(rs.getInt("isbn")));
            }

            String whereClause = bookList.isEmpty() ? " 1 = 0 "
                    : " isbn in ( " + String.join(", ", bookList) + " ) ";

            JOptionPane.showMessageDialog(this.dialogParent,
                    "Press OK to continue.", "Pause", JOptionPane.INFORMATION_MESSAGE);

            //2a) Delete the entry
            String deleteBookAuthor = "delete from book_author where authorid = ? and " + whereClause;
            deleteBookAuthorStmt = conn.prepareStatement(deleteBookAuthor);
            deleteBookAuthorStmt.setInt(1, authorID);
            deleteBookAuthorStmt.executeUpdate();


            // 3) Delete the author
            String deleteAuthorSQL = "delete from author where authorid = ? ";
            deleteAuthorStmt = conn.prepareStatement(deleteAuthorSQL);
            deleteAuthorStmt.setInt(1, authorID);
            deleteAuthorStmt.executeUpdate();

            //5) Commit it
            conn.commit();

            result = "Deleted the author";

        } catch (SQLException e) {

            // Rollback the transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println(e.getMessage());
        } finally {
            // Clean up and release resources
            try {
                if (checkAuthorStmt != null) checkAuthorStmt.close();
                if (checkBookAuthorStmt != null) checkBookAuthorStmt.close();
                if (deleteBookAuthorStmt != null) deleteBookAuthorStmt.close();
                if (deleteAuthorStmt != null) deleteAuthorStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sb.append(result).append("\n");
        return sb.toString();
    }

    public String deleteBook(int isbn) {

        Connection conn = this.dbConnect.getConn();
        PreparedStatement checkBookStmt = null;
        PreparedStatement checkCustBookStmt = null;
        PreparedStatement deleteBookStmt = null;


        StringBuilder sb = new StringBuilder(createHeader(
                String.format("Deleting Book of ISBN %s ", isbn)));

        String result = null;

        try {

            conn.setAutoCommit(false);

            // 1) Check if the book exist
            String checkCustomerSQL = "select 1 from book WHERE isbn = ? FOR UPDATE";
            checkBookStmt = conn.prepareStatement(checkCustomerSQL);
            checkBookStmt.setInt(1, isbn);
            ResultSet rs = checkBookStmt.executeQuery();
            if (!rs.next()) {
                result = "Book does not exist";
                throw new SQLException("Customer does not exist");
            }

            // 2) Check if the customer has borrowed the book
            String checkCustBookStmtSql = "select 1 from cust_book where isbn = ? FOR UPDATE";
            checkCustBookStmt = conn.prepareStatement(checkCustBookStmtSql);
            checkCustBookStmt.setInt(1, isbn);
            rs = checkCustBookStmt.executeQuery();
            if (rs.next()) {
                result = "The Book had been borrowed by someone. " +
                        "Please return before deletion. \n DELETION DENIED\n";
                throw new SQLException("The customer owns a book. Can not delete");
            }

            // 3) Delete the cust book record
            String deleteBookSQL = "delete from book where isbn = ? ";
            deleteBookStmt = conn.prepareStatement(deleteBookSQL);
            deleteBookStmt.setInt(1, isbn);
            deleteBookStmt.executeUpdate();

            //5) Commit it
            conn.commit();

            result = "Deleted the book";

        } catch (SQLException e) {

            // Rollback the transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println(e.getMessage());
        } finally {
            // Clean up and release resources
            try {
                if (checkBookStmt != null) checkBookStmt.close();
                if (checkCustBookStmt != null) checkCustBookStmt.close();
                if (deleteBookStmt != null) deleteBookStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sb.append(result).append("\n");
        return sb.toString();
    }

    private static final String HEADER_CHAR = "=";
    private static final int HEADER_LENGTH = 90;
    private static final int HEADER_PADDING = 2;

    public String createHeader(String title) {
        StringBuilder sb = new StringBuilder("\n");

        int titleLen = title.length();
        int padding = (HEADER_LENGTH - titleLen - (HEADER_PADDING*2))/2;

        sb.append(HEADER_CHAR.repeat(padding));
        sb.append(" ".repeat(HEADER_PADDING));
        sb.append(title);
        sb.append(" ".repeat(HEADER_PADDING));
        sb.append(HEADER_CHAR.repeat(padding));

        if (sb.length() < HEADER_LENGTH) {
            sb.append(HEADER_CHAR.repeat(HEADER_LENGTH-sb.length()));
        }

        sb.append("\n");
        return sb.toString();
    }

}

//Author Class
class Author {

    private Integer authorId;
    private String name;
    private String books;

    public Author(Integer authorId, String name, String books) {
        this.authorId = authorId;
        this.name = name;
        this.books = books;
    }

    @Override
    public String toString() {
        return "Author Information for Author Id " +
                ": " + authorId +
                "\n\t Name : '" + name + '\'' +
                "\n\t Books : '" + books + '\'' +
                '\n';
    }
}

// Customer class
class Customer {

    private Integer customerid;
    private String name;
    private String city;
    private String titles;

    public Customer(Integer customerid, String name, String city, String titles) {
        this.customerid = customerid;
        this.name = name;
        this.city = city;
        this.titles = titles;
    }

    @Override
    public String toString() {
        return "Customer Information of ID " +
                ": " + customerid +
                "\n\t Name : '" + name + '\'' +
                "\n\t City : '" + city + '\'' +
                "\n\t Books borrowed : '" + titles + '\'' +
                '\n';
    }

}

// Book class
class Book {

    private Integer isbn;
    private String title;
    private Integer editionNo;
    private Integer numofCop;
    private Integer numleft;
    private String authors;

    @Override
    public String toString() {
        return "Book Information for " +
                "ISBN : " + isbn +
                "\n\t Title: '" + title + '\'' +
                "\n\t Edition No : " + editionNo +
                "\n\t Number Copies :" + numofCop +
                "\n\t Number Left: " + numleft +
                "\n\t Authors: '" + authors + '\'' +
                '\n';
    }

    public Book(Integer isbn, String title, Integer editionNo, Integer numofCop, Integer numleft, String authors ) {
        this.isbn = isbn;
        this.title = title;
        this.editionNo=editionNo;
        this.numofCop = numofCop;
        this.numleft = numleft;
        this.authors = authors;
    }

}

class DBConnect {

    private String URL = "jdbc:postgresql://localhost:5432/library";
    private String username;
    private String password;
    private Connection conn;

//    postgres main1234
//    test_user password

    public DBConnect(String username, String password) {
        this.username = username;
        this.password = password;
        Properties props = new Properties();
        props.setProperty("user", this.username);
        props.setProperty("password", this.password);

        try {
            this.conn = DriverManager.getConnection(URL, props);
        } catch (SQLException e) {
            System.out.println("There was Exception " + e.getMessage());
        }

    }


    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Connection getConn() {
        return this.conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
