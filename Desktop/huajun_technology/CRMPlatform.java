
import java.sql.*;

public class CRMPlatform {

    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:crm.db";
        return DriverManager.getConnection(url);
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS customers ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "email TEXT NOT NULL UNIQUE, "
                + "phone TEXT NOT NULL);";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Customer table created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCustomer(String name, String email, String phone) {
        String sql = "INSERT INTO customers(name, email, phone) VALUES(?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
            System.out.println("Customer added: " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listCustomers() {
        String sql = "SELECT * FROM customers";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("name") +
                        ", Email: " + rs.getString("email") +
                        ", Phone: " + rs.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(int id, String name, String email, String phone) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            System.out.println("Customer updated: ID " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Customer deleted: ID " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listCustomersPaged(int pageSize, int pageIndex) {
        String sql = "SELECT * FROM customers LIMIT ? OFFSET ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, pageSize * pageIndex);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("name") +
                        ", Email: " + rs.getString("email") +
                        ", Phone: " + rs.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CRMPlatform crm = new CRMPlatform();

        crm.createTable();

        crm.addCustomer("Alice", "alice@example.com", "1234567890");
        crm.addCustomer("Bob", "bob@example.com", "0987654321");
        crm.addCustomer("Charlie", "charlie@example.com", "5678901234");

        System.out.println("
All Customers:");
        crm.listCustomers();

        crm.updateCustomer(2, "Bob Smith", "bob.smith@example.com", "1112223333");

        System.out.println("
Paginated Customers (Page 1):");
        crm.listCustomersPaged(2, 0);
        System.out.println("
Paginated Customers (Page 2):");
        crm.listCustomersPaged(2, 1);

        crm.deleteCustomer(3);

        System.out.println("
All Customers After Deletion:");
        crm.listCustomers();
    }
}
