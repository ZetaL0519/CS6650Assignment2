import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePool {

    private static BasicDataSource ds = new BasicDataSource();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ds.setUrl(DatabaseConnectionConstants.dbUrl);
        ds.setUsername(DatabaseConnectionConstants.username);
        ds.setPassword(DatabaseConnectionConstants.password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Additional configuration settings can be set here:
        ds.setInitialSize(60); // initial number of connections
        ds.setMaxTotal(100);   // max number of connections
    }
    private DatabasePool() {}

    public static BasicDataSource getDataSource() {
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}