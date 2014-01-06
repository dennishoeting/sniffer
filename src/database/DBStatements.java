package database;

/**
 * Enhält alle DB Statements
 * @author timo
 *
 */
public enum DBStatements {
	/**
	 * Liefert alle Daten der Tabelle
	 */
	SELECT_ALL("SELECT * FROM (SELECT id, nickname, text, timestamp, location, lat, lng, " +
			"(SELECT earth_distance(ll_to_earth(?, ?), " +
			"ll_to_earth(lat, lng))) AS distance " +
			"FROM sniffertable "
			+"ORDER BY id DESC "
			+"LIMIT ?) AS \"bla\" "
			+" ORDER BY id ASC;"),
		
		/**
		 * Liefert alle Daten der Tabelle mit Offset	
		 */
		SELECT_ALL_WITH_OFFSET("SELECT * FROM (SELECT id, nickname, text, timestamp, location, lat, lng, " +
				"(SELECT earth_distance(ll_to_earth(?, ?), " +
				"ll_to_earth(lat, lng))) AS distance " +
				"FROM sniffertable "
				+"ORDER BY id DESC "
				+"LIMIT ? OFFSET ?) AS \"bla\" "
				+" ORDER BY id DESC;"),		
	
	/** 
	 * Liefert die Nachricht zur ID
	 * 1) lat
	 * 2) lng
	 * 3) id
	 */
	SELECT_ID(
			"SELECT id, nickname, text, timestamp, location, lat, lng, "
			+"(SELECT earth_distance(ll_to_earth(?, ?), ll_to_earth(lat, lng)))  AS distance "
			+"FROM sniffertable "
			+"WHERE id = ?;"),
	
	/**
	 * Liefert das letzte Tupel in der DB
	 */
	SELECT_LATEST("SELECT * FROM sniffertable WHERE id = (SELECT MAX(id) FROM sniffertable);"),
	
	/**
	 * Holt alle Nachrichten mit Entfernung in einem Radius:
	 * 1) Lat
	 * 2) Lng
	 * 3) Lat
	 * 4) Lng
	 * 5) Radius in Meter
	 * 6) Lat
	 * 7) Lng
	 * 8) Radius in Meter
	 * 9) Maximale Anzahl an Nachrichten
	 * 
	 */
	SELECT_IN_RADIUS(
			
			"SELECT * FROM (" +
			"SELECT id, nickname, text, timestamp, location, lat, lng, "
			+"(SELECT earth_distance(ll_to_earth(?, ?), ll_to_earth(lat, lng)))  AS distance "
			+"FROM sniffertable "
			+"WHERE ("
			+" earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng) "
			+" AND "
			+"earth_distance(ll_to_earth(?, ?), ll_to_earth(lat, lng)) <= ?) "
			+"ORDER BY id DESC "
			+"LIMIT ? ) AS \"blub\"" 
			+"ORDER BY id ASC;"
			),
			
		/**
		 * Wie SELECT_IN_RADIUS nur mit Offset	
		 */
		SELECT_IN_RADIUS_WITH_OFFSET(
				"SELECT * FROM (" +
				"SELECT id, nickname, text, timestamp, location, lat, lng, "
				+"(SELECT earth_distance(ll_to_earth(?, ?), ll_to_earth(lat, lng)))  AS distance "
				+"FROM sniffertable "
				+"WHERE ("
				+" earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng) "
				+" AND "
				+"earth_distance(ll_to_earth(?, ?), ll_to_earth(lat, lng)) <= ?) "
				+"ORDER BY id DESC "
				+"LIMIT ? OFFSET ?) AS \"blub\"" 
				+"ORDER BY id DESC;"
		),
			
	/**
	 * Fügt eine neue Message in die DB ein.
	 * nickname, text, location, lat, lng.
	 */
	INSERT_MESSAGE(
			"INSERT INTO sniffertable(nickname, text, location, lat, lng) VALUES (?, ?, ?, ?, ?);"
	);
	
	/**
	 * SQL String für das zugehörige Enum.
	 */
	private String stmt;
	
	/**
	 * Konstruktor
	 * 
	 * @param stmt
	 *            String
	 */
	DBStatements(String stmt) {
		this.stmt = stmt;
	}
	
	/**
	 * Liefert den SQL String
	 * 
	 * @return stmt
	 */
	public String getStmt() {
		return this.stmt;
	}
	
	/**
	 * Schreibt den SQL-String
	 * 
	 * @return stmt
	 */
	@Override
	public String toString() {
		return this.stmt;
	}
}

