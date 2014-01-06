package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * DB Verbindung
 * @author timo
 *
 */
public class DBConnection {
	
	/**
	 * Datenbankverbindung
	 */
	private static Connection connection;

	/**
	 * verwendete Datenbank
	 */
	private static final String DB_DATABASE = "sniffer";

	/**
	 * Datenbanktreiber
	 */
	private static final String DB_DRIVER = "org.postgresql.Driver";

	/**
	 * Datenbankpasswort
	 */
	private static final String DB_PASSWORD = "penis";

	/**
	 * Port zur Datenbank
	 */
	private static final String DB_PORT = "9002";

	/**
	 * Datenbankservername
	 */
	private static final String DB_SERVERNAME = "134.106.56.169";

	/**
	 * Datenbankbenutzer
	 */
	private static final String DB_USERNAME = "user";

	/**
	 * Default Konstruktor
	 */
	private DBConnection() {
		if (DBConnection.connection == null) {
			try {
				Class.forName(DBConnection.DB_DRIVER).newInstance(); // Um korrekten Treiber für die Connection zu laden
				DBConnection.connection = DriverManager.getConnection(getConnectionURL());
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void closeConnection() {
		if (DBConnection.connection != null) {
			try {
				DBConnection.connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Connection getConnection() {
		if (DBConnection.connection == null) {
			new DBConnection();
			System.out.println(DBConnection.getTime()+"Verbindung aufgebaut! (Fkt getConnection)");
		} 
		return DBConnection.connection;
	}
	
	/**
	 * Liefert ein PreparedStatement
	 * @param sql
	 * @return
	 */
	public static PreparedStatement prepareStatement(String sql) {
		PreparedStatement pstmt = null;
		try {
			if (DBConnection.connection == null) {
				DBConnection.getConnection();
			} 
			
			pstmt = DBConnection.connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return pstmt;
	}

	/**
	 * Liefert die Connection String zur Datenbank
	 * 
	 * @return Connection String zur Datenbank
	 */
	private String getConnectionURL() {
		return "jdbc:postgresql://" + DBConnection.DB_SERVERNAME + ":" + DBConnection.DB_PORT + "/" + DBConnection.DB_DATABASE
				+ "?user=" + DBConnection.DB_USERNAME + "&password=" + DBConnection.DB_PASSWORD;// JDBC url
	}
	
	/**
	 * Liefert die aktuelle Zeit
	 */
	private static String getTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(new java.util.Date())+": ";
	}

}
