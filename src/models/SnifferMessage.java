package models;

import jsonParser.JSONException;
import jsonParser.JSONObject;

/**
 * Eine Sniffer Nachricht
 * Enhält alle wichtigen Informationen
 * @author timo
 *
 */
public class SnifferMessage {
	
	private String name, text, timestamp, location;
	private double lng, lat, distance;
	private int id;
	
	/**
	 * Default
	 */
	public SnifferMessage() {
		
	}
	
	/**
	 * 
	 * @param name
	 * @param text
	 * @param timestamp
	 * @param location
	 * @param lat
	 * @param lnt
	 */
	public SnifferMessage(int id, String name, String text, String timestamp, String location, double lat, double lng, double distance) {
		this.id = id;
		this.name = name;
		this.text = text;
		this.timestamp = timestamp;
		this.location = location;
		this.lat = lat;
		this.lng = lng;
		this.distance = distance;
	}

	public String getNickname() {
		return name;
	}

	public String getText() {
		return text;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getLocation() {
		return location;
	}

	public double getLng() {
		return lng;
	}

	public double getLat() {
		return lat;
	}
	
	public double getDistance() {
		return this.distance;
	}
	
	public int getID() {
		return this.id;
	}
	
	/*
	 * Liefert die SnifferMessage als JSONObject
	 */
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		try {
			object.put("type", 1);
			object.put("id", -1);
			object.put("name", name);
			object.put("text", text);
			object.put("lat", lat);
			object.put("lng", lng);
			object.put("distance", distance);
			object.put("time", -1);
			object.put("date", -1);
			object.put("yourOwn", false);
		} catch(JSONException jse) {
			jse.printStackTrace();
		}
		
		return object;
	}
}
