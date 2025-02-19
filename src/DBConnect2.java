import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnect2 {

    private String URL = "jdbc:postgresql://localhost:5432/library";
    private String username;
    private String password;
    private Connection conn;

//    postgres main1234
//    test_user password

    public DBConnect2(String username, String password) {
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
