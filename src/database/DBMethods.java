package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.SnifferMessage;

/**
 * Enthält alle Methoden, die wir in Bezug auf die DB brauchen.
 * @author timo
 *
 */
public class DBMethods {

	private static DBMethods instance = null;
	
	/**
	 * Private
	 */
	private DBMethods() {
		
	}
	
	public static DBMethods getInstance() {
		if(instance != null) 
			return instance;

		instance = new DBMethods();
		return instance;
	}
	
	/**
	 * Trägt eine neue Nachricht in die DB ein und gibt
	 * die ID zurück.
	 * @param nickname
	 * @param text
	 * @param location
	 * @param lat
	 * @param lng
	 * @return
	 */
	public synchronized int insertMessage(String nickname, String text, String location, double lat, double lng) {
		
		ResultSet rs = null;
		int returnID = -1;
		
		try {
			PreparedStatement ps = DBConnection.prepareStatement(DBStatements.INSERT_MESSAGE.getStmt());
			
			// PS Vorbereiten
			ps.setString(1, nickname);
			ps.setString(2, text);
			ps.setString(3, location);
			ps.setDouble(4, lat);
			ps.setDouble(5, lng);
			
			// Insert ausführen
			ps.execute();
			
			// Letzte ID holen
			ps = DBConnection.prepareStatement(DBStatements.SELECT_LATEST.getStmt());
			rs = ps.executeQuery();
			
			while(rs.next()) {
				returnID = rs.getInt("id");
			}
		
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}	
			} catch(Exception e) {
				
			}
		}
		
		// ID zurück geben
		return returnID;
	}
	
	/**
	 * Liefert eine Nachricht, zur passenden ID
	 * @param id, lat, lng
	 * @return
	 */
	public SnifferMessage getMessageByID(int id, double lat, double lng) {
		
		SnifferMessage returnMessage = null;
		ResultSet rs = null;
		
		try {
			PreparedStatement ps = DBConnection.prepareStatement(DBStatements.SELECT_ID.getStmt());
			
			// PS Vorbereiten
			ps.setDouble(1, lat);
			ps.setDouble(2, lng);
			ps.setInt(3, id);
			
			// Ausführen
			rs = ps.executeQuery();
			
			// SnifferMessage füllen
			while(rs.next()) {
				returnMessage = new SnifferMessage(
						rs.getInt("id"),
						rs.getString("nickname"),
						rs.getString("text"),
						rs.getString("timestamp"),
						rs.getString("location"),
						rs.getDouble("lat"),
						rs.getDouble("lng"),
						rs.getDouble("distance")
				);
			}
			
		} catch(SQLException e) {
			
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
			} catch(SQLException e) {
				
			}
		}
		
		return returnMessage;
	}
	
	/**
	 * Liefert alle geforderten Nachrichten zu den IDs
	 * @param ids, lat, lng
	 * @return
	 */
	public Map<Integer, SnifferMessage> getMessagesByIDs(int[] ids, double lat, double lng) {
		
		// Rückgabe Map
		Map<Integer, SnifferMessage> messages = new HashMap<Integer, SnifferMessage>();
		
		// Alle Nachrichten holen
		for(int i : ids) {
			SnifferMessage tmpMessage = this.getMessageByID(i, lat, lng);
			if(tmpMessage != null) {
				messages.put(i, tmpMessage);
			}
		}
		
		return messages;
	}
	
	public List<SnifferMessage> getAllMessages(double lat, double lng, int limit) {
		ResultSet rs = null;
		List<SnifferMessage> tmpMessages = new ArrayList<SnifferMessage>();
		
		try {
			// Prepared Statement vorbereiten
			PreparedStatement ps = DBConnection.prepareStatement(DBStatements.SELECT_ALL.getStmt());
			ps.setDouble(1, lat);
			ps.setDouble(2, lng);
			ps.setInt(3, limit);
			// Ausführen
			rs = ps.executeQuery();
			
			// In die Map
			while(rs.next()) {
				tmpMessages.add(new SnifferMessage(
						rs.getInt("id"),
						rs.getString("nickname"),
						rs.getString("text"),
						rs.getString("timestamp"),
						rs.getString("location"),
						rs.getDouble("lat"),
						rs.getDouble("lng"),
						rs.getDouble("distance")
				));
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally{
			try{
				if(rs != null) {
					rs.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		return tmpMessages;
	}
	
	public List<SnifferMessage> getAllMessagesWithOffset(double lat, double lng, int limit, int offset) {
		
		ResultSet rs = null;
		
		List<SnifferMessage> tmpMessages = new ArrayList<SnifferMessage>();
		
		try {
			// Prepared Statement vorbereiten
			PreparedStatement ps = DBConnection.prepareStatement(DBStatements.SELECT_ALL_WITH_OFFSET.getStmt());
			ps.setDouble(1, lat);
			ps.setDouble(2, lng);
			ps.setInt(3, limit);
			ps.setInt(4, offset);
			// Ausführen
			rs = ps.executeQuery();
			
			// In die Map
			while(rs.next()) {
				tmpMessages.add(new SnifferMessage(
						rs.getInt("id"),
						rs.getString("nickname"),
						rs.getString("text"),
						rs.getString("timestamp"),
						rs.getString("location"),
						rs.getDouble("lat"),
						rs.getDouble("lng"),
						rs.getDouble("distance")
				));
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally{
			try{
				if(rs != null) {
					rs.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		return tmpMessages;
	}
	
	/**
	 * Liefert alle Nachrichten in einem Umkreis
	 * @param lat
	 * @param lng
	 * @param radius in Meter
	 * @param answerLimit
	 * @return
	 */
	public List<SnifferMessage> getMessagesInRadius(double lat, double lng, int radius, int answerLimit) {
		
		ResultSet rs = null;
		List<SnifferMessage> tmpMessages = new ArrayList<SnifferMessage>();
		
		try {
			// Prepared Statement vorbereiten
			PreparedStatement ps = DBConnection.prepareStatement(DBStatements.SELECT_IN_RADIUS.getStmt());

			ps.setDouble(1, lat);
			ps.setDouble(2, lng);
			ps.setDouble(3, lat);
			ps.setDouble(4, lng);
			ps.setDouble(5, radius);
			ps.setDouble(6, lat);
			ps.setDouble(7, lng);
			ps.setDouble(8, radius);
			ps.setInt(9, answerLimit);
			
			// Ausführen
			rs = ps.executeQuery();
			
			// In die Map
			while(rs.next()) {
				tmpMessages.add(new SnifferMessage(
						rs.getInt("id"),
						rs.getString("nickname"),
						rs.getString("text"),
						rs.getString("timestamp"),
						rs.getString("location"),
						rs.getDouble("lat"),
						rs.getDouble("lng"),
						rs.getDouble("distance")
				));
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally{
			try{
				if(rs != null) {
					rs.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		return tmpMessages;
	}
	
	/**
	 * Liefert alle Nachrichten in einem Umkreis mit Offset
	 * Wie getMessagesInRadius nur mit Offset.
	 * @param lat
	 * @param lng
	 * @param radius in Meter
	 * @param answerLimit
	 * @param offset
	 * @return
	 */
	public List<SnifferMessage> getMessagesInRadiusWithOffset(double lat, double lng, int radius, int answerLimit, int offset) {
		
		ResultSet rs = null;
		List<SnifferMessage> tmpMessages = new ArrayList<SnifferMessage>();
		
		try {
			// Prepared Statement vorbereiten
			PreparedStatement ps = DBConnection.prepareStatement(DBStatements.SELECT_IN_RADIUS_WITH_OFFSET.getStmt());

			ps.setDouble(1, lat);
			ps.setDouble(2, lng);
			ps.setDouble(3, lat);
			ps.setDouble(4, lng);
			ps.setDouble(5, radius);
			ps.setDouble(6, lat);
			ps.setDouble(7, lng);
			ps.setDouble(8, radius);
			ps.setInt(9, answerLimit);
			ps.setInt(10, offset);
			
			// Ausführen
			rs = ps.executeQuery();
			
			// In die Map
			while(rs.next()) {
				tmpMessages.add(new SnifferMessage(
						rs.getInt("id"),
						rs.getString("nickname"),
						rs.getString("text"),
						rs.getString("timestamp"),
						rs.getString("location"),
						rs.getDouble("lat"),
						rs.getDouble("lng"),
						rs.getDouble("distance")
				));
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally{
			try{
				if(rs != null) {
					rs.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		return tmpMessages;
	}	
	
}
