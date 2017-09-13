package bp.server.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * manage the Connection to PostgresSQL Database
 * Created by skisssbb on 22.08.17.
 */
public class PostgreSQLJDBC {
    private Connection connection;
    private static PostgreSQLJDBC instance = null;

    public static PostgreSQLJDBC getInstance(){
        if(instance == null){
            instance = new PostgreSQLJDBC();
        }
        return instance;
    }

    /**
     * DO NOT FORGET TO CLOSE THE CONNECTION AFTER USING
     * @return the up to date connection object
     */
    public Connection getConnection(){
        // load the driver
        if(connection == null){
            try{
                Class.forName("org.postgresql.Driver");
                connection = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/osm","postgres","password");
                connection.setAutoCommit(false);
                System.out.println("OSM Database opened successfully.");
                return connection;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
