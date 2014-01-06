package webSocket;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jsonParser.JSONObject;
import models.SnifferMessage;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketConnection;
import org.eclipse.jetty.websocket.WebSocketServlet;

import database.DBMethods;

/**
 * Servlet
 * @author dennis, timo
 */
public class SnifferServlet extends WebSocketServlet {
	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 240208181680508987L;
	
	/**
	 * Speicherung der onlineUser
	 */
	private static final Set<NewMessageWebSocket> onlineUsers = new CopyOnWriteArraySet<NewMessageWebSocket>();
	
	/**
	 * Weitergabe der Upgrade-Anfrage
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getServletContext().getNamedDispatcher("default").forward(request, response);	
	}

	/**
	 * Handshake, R�ckgabe: ChatWebSocket
	 */
	protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new NewMessageWebSocket();
	}

	/**
	 * NewMessageWebSocket, Kernfunktionalit�t
	 * @author dennis
	 */
	private class NewMessageWebSocket implements WebSocket {
		
		/**
		 * Speicherung der Connection
		 */
		private WebSocketConnection connection;
		
		/**
		 * Speicherung der Latitude
		 */
		private double lat;
		
		/**
		 * Speicherung der Longitude
		 */
		private double lng;
		
		/**
		 * Speichern des Positionsstrings
		 */
		private String positionString;

		/**
		 * Speichern des Radius
		 */
		private int ratio;
		
		/*
		 * 
		 */
		private String userName = "User"+this.hashCode();
		
		/**
		 * Initialisierung, eintragen in onlineUsers-Tabelle
		 */
		public void onConnect(Outbound connection) {
			NewMessageWebSocket.this.connection = (WebSocketConnection)connection;
			onlineUsers.add(this);
			System.out.println("Ich ("+this.hashCode()+") habe mich angemeldet in "+onlineUsers.hashCode());
		
			//An alle user connect eines users mitteilen
			JSONObject tempObject = new JSONObject();
			try {
				tempObject.put("type", 5);
				tempObject.put("value", onlineUsers.size());
				for (NewMessageWebSocket user : onlineUsers) {
					user.connection.sendMessage(tempObject.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Leer
		 */
		public void onMessage(byte frame, byte[] data, int offset, int length) {}

		/**
		 * Bei neuer Nachricht
		 */
		public void onMessage(byte frame, String data) {
			int id;
			String name, text;
			int type = -1;
			
			System.out.println("Nachricht empfangen: " + data);
			
			try {
				/*
				 * JSon auslesen
				 */
				JSONObject object = new JSONObject(data);
				JSONObject anObject;
				type = object.getInt("type");
			
				/*
				 * Differenzierung der Art der Nachricht 
				 */
				switch(type) {
				/*
				 * Positionsangabe
				 */
				case 0:
					System.out.println("Positionseingabe eingegangen.");
					this.lat = object.getDouble("lat");
					this.lng = object.getDouble("lng");
					this.positionString = object.getString("positionString");
					this.ratio = object.getInt("ratio");
				break;
					
				/*
				 * Nachricht
				 */
				case 1:
					System.out.println("Nachricht eingegangen.");
					name = object.getString("name");
					text = object.getString("text");
					text = prepareText(text);
					
					/*
					 * Nachrichten aus data in Datenbank eintragen, R�ckgabe: id
					 */
					id = DBMethods.getInstance().insertMessage(name, text, this.positionString, this.lat, this.lng);
					
					/*
					 * Nachrichten an User im Umkreis senden
					 */
					System.out.println("Senden der Nachricht an " +onlineUsers.size()+ " User");
					Calendar calendar = Calendar.getInstance();
					JSONObject newObject = new JSONObject();
					newObject.put("type", 1);
					newObject.put("id", id);
					newObject.put("name", name);
					newObject.put("text", text);
					newObject.put("time", calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + " Uhr");
					newObject.put("date", formatDate(calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH)+1) + "." + String.valueOf(calendar.get(Calendar.YEAR)).substring(2)));
					newObject.put("yourOwn", false);
					newObject.put("lat", this.lat);
					newObject.put("lng", this.lng);
					newObject.put("position", this.positionString);
					for (NewMessageWebSocket user : onlineUsers) {
						if(user != this) {	
							int distanceInMeters = getDistanceInMeters(this.lat, this.lng, user.getLat(), user.getLng());
							newObject.put("distance", distanceInMeters);
								
							if(user.getRatio() < 0
							|| distanceInMeters <= user.getRatio()) {
								user.getConnection().sendMessage(frame, newObject.toString());
								System.out.println("Senden an " + user.hashCode());
							} else {
								System.out.println("KEIN Senden an " + user.hashCode() + " wegen user.getRatio>=0 OR distanceInMeters>=user.getRatio()");
								System.out.println("Test: (Eins von beiden muss <<true>> sein!)");
								System.out.println("(user.getRatio() < 0) : " + (user.getRatio() < 0) + ". user.getRatio()="+user.getRatio());
								System.out.println("(distanceInMeters <= user.getRatio()) : " + (distanceInMeters <= user.getRatio())+". distanceInMeters="+distanceInMeters);
							}
							
						} else {
							System.out.println("Kein senden (bin ich selbst)");
						}
					}
					newObject.put("yourOwn", true);
					newObject.put("distance", 0);
					this.getConnection().sendMessage(frame, newObject.toString());
					System.out.println("Senden an mich selbst");
				break;
				/*
				 * Ratio ge�ndert
				 */
				case 2:
					System.out.println("Radius ge�ndert");
					this.ratio = object.getInt("ratio");
				break;
				/*
				 * Anforderung von Messages aus der Datenbank
				 */
				case 3:
					System.out.println("Nachrichtenanfrage von Datenbank");
					List<SnifferMessage> result;
					if(this.ratio < 0) {
						result = DBMethods.getInstance().getAllMessages(this.lat, this.lng, 10);
					} else {
						result = DBMethods.getInstance().getMessagesInRadius(this.lat, this.lng, this.ratio, 10);
					}
					anObject = new JSONObject();
					
					this.connection.sendMessage(frame, "{\"type\": 0}");
					for(SnifferMessage message : result) {
						anObject.put("type", 1);
						anObject.put("id", message.getID());
						anObject.put("name", message.getNickname());
						anObject.put("text", message.getText());
						anObject.put("time", getTime(message.getTimestamp()));
						anObject.put("date", getDate(message.getTimestamp()));
						anObject.put("distance", (int)(message.getDistance()));
						anObject.put("yourOwn", true);
						anObject.put("lat", message.getLat());
						anObject.put("lng", message.getLng());
						anObject.put("position", message.getLocation());
						this.connection.sendMessage(frame, anObject.toString());
					}
					
					// Gibt es weitere Nachrichten?
					this.connection.sendMessage(frame, "{\"type\": 2, \"more\": "+ existsMoreMessages(10) +"}");
				break;
				/*
				 * Mehr Messages angefordert
				 */
				case 4:
					System.out.println("Mehr-Button wurde gedr�ckt");
					// Die nächsten 10 Nachrichten holen
					List<SnifferMessage> result2;
					if(this.ratio < 0) {
						result2 = DBMethods.getInstance().getAllMessagesWithOffset(this.lat, this.lng, 10, object.getInt("offset"));
					} else {
						result2 = DBMethods.getInstance().getMessagesInRadiusWithOffset(this.lat, this.lng, this.ratio, 10, object.getInt("offset"));
					}
					anObject = new JSONObject();
					
					for(SnifferMessage message : result2) {
						anObject.put("type", 3);
						anObject.put("id", message.getID());
						anObject.put("name", message.getNickname());
						anObject.put("text", message.getText());
						anObject.put("time", getTime(message.getTimestamp()));
						anObject.put("date", getDate(message.getTimestamp()));
						anObject.put("distance", (int)(message.getDistance()));
						anObject.put("yourOwn", true);
						anObject.put("lat", message.getLat());
						anObject.put("lng", message.getLng());
						anObject.put("position", message.getLocation());
						this.connection.sendMessage(frame, anObject.toString());
					}
					
					// Gibt es mehr Nachrichten?
					this.connection.sendMessage(frame, "{\"type\": 2, \"more\": " +existsMoreMessages(object.getInt("offset") +10) +"}");
					
				break;
				
				/*
				 * Ist der neue Standort mehr als 50m vom alten entfernt?
				 */
				case 5:
					System.out.println("Standortaktualisierungs�berpr�fung");
					anObject = new JSONObject();
					anObject.put("type", 4);
					anObject.put("result", isInDistance(object.getInt("distance"), object.getDouble("oldLat"), object.getDouble("oldLng"), object.getDouble("newLat"), object.getDouble("newLng")));
					this.connection.sendMessage(frame, anObject.toString());
					
				break;
				
				/*
				 * Name empfangen
				 */
				case 6:
					System.out.println("Name gespeichert");
					this.userName = object.getString("name");
				break;
				
				/*
				 * OnlineUsers angefordert
				 */
				case 7:
					System.out.println("Useranforderung wird bearbeitet");
					Object[] onlineUsersCopy = onlineUsers.toArray();
					
					JSONObject onlineUsersObject = new JSONObject();
					onlineUsersObject.put("type", 6);
					NewMessageWebSocket tempSocket;
					for(int i=0; i<onlineUsersCopy.length; i++) {
						tempSocket = (NewMessageWebSocket)onlineUsersCopy[i];
						if(!tempSocket.equals(this)) {
							onlineUsersObject.put("name", tempSocket.userName);
							onlineUsersObject.put("lat", tempSocket.lat);
							onlineUsersObject.put("lng", tempSocket.lng);
							onlineUsersObject.put("distance", getDistanceInMeters(this.lat, this.lng, tempSocket.lat, tempSocket.lng));
							onlineUsersObject.put("id", i);
							onlineUsersObject.put("hash", tempSocket.hashCode());
							onlineUsersObject.put("you", "false");
						} else {
							onlineUsersObject.put("name", "Du");
							onlineUsersObject.put("lat", tempSocket.lat);
							onlineUsersObject.put("lng", tempSocket.lng);
							onlineUsersObject.put("distance", 0);
							onlineUsersObject.put("id", i);
							onlineUsersObject.put("hash", tempSocket.hashCode());
							onlineUsersObject.put("you", "true");
						}
						this.connection.sendMessage(frame, onlineUsersObject.toString());
					}
				break;
				/*
				 * ChatNachricht eingegangen
				 */
				case 8:
					JSONObject chatObject = new JSONObject();
					Calendar chatCal = Calendar.getInstance();
					for(NewMessageWebSocket socket : onlineUsers) {
						if(socket.hashCode() == object.getInt("hash")) {
							chatObject.put("type", 7);
							chatObject.put("from", this.userName);
							chatObject.put("message", object.getString("message"));
							chatObject.put("time", chatCal.get(Calendar.HOUR_OF_DAY) + ":" + chatCal.get(Calendar.MINUTE) + " Uhr");
							chatObject.put("date", formatDate(chatCal.get(Calendar.DAY_OF_MONTH) + "." + (chatCal.get(Calendar.MONTH)+1) + "." + String.valueOf(chatCal.get(Calendar.YEAR)).substring(2)));
							socket.connection.sendMessage(frame, chatObject.toString());
						}
					}
				break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Errechnet die Distanz zwischen zwei Punkten (in m)
		 */
		private int getDistanceInMeters(double myLat, double myLng, double hisLat, double hisLng) {
			double f = 1 / 298.257223563;
			double a = 6378.137;
			double F = ((myLat + hisLat)/2)*(Math.PI/180);
			double G = ((myLat - hisLat)/2)*(Math.PI/180);
			double l = ((myLng - hisLng)/2)*(Math.PI/180);
			double S = Math.pow(Math.sin(G), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.cos(F), 2) * Math.pow(Math.sin(l), 2);
			double C = Math.pow(Math.cos(G), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.sin(F), 2) * Math.pow(Math.sin(l), 2);
			double w = Math.atan(Math.sqrt(S/C));
			double D = 2 * w * a;
			double R = Math.sqrt(S*C)/w;
			double H1 = (3*R-1)/(2*C);
			double H2 = (3*R+1)/(2*S);
		
			double distance = D * (1 + f*H1*Math.pow(Math.sin(F), 2) * Math.pow(Math.cos(G), 2) - f*H2*Math.pow(Math.cos(F), 2)*Math.pow(Math.sin(G), 2)); 
			
			return (int)(distance*1000);
		}
		
		/**
		 * gibt zurück ob der neue Standort mehr als distance vom
		 * alten Standort entfernt ist.
		 * @param distance
		 * @param oldLat
		 * @param oldLng
		 * @param newLat
		 * @param newLng
		 * @return
		 */
		private boolean isInDistance(int distance, double oldLat, double oldLng, double newLat, double newLng) {
			return getDistanceInMeters(oldLat, oldLng, newLat, newLng) <= distance;
		}
		
		/**
		 * Schickt dem Client Informationen darüber
		 * ob es weitere Nachrichten gibt.
		 * @param int
		 * @throws IOException
		 */
		private boolean existsMoreMessages(int offset) throws IOException {
			// Gibt es weitere Nachrichten?
			if(this.ratio < 0) {
				if(DBMethods.getInstance().getAllMessagesWithOffset(this.lat, this.lng, 1, offset).size() > 0)
					return true;;
			} else {
				if(DBMethods.getInstance().getMessagesInRadiusWithOffset(this.lat, this.lng, this.ratio, 1, offset).size() > 0)
					return true;
			}
			
			return false;
		}
	
		private String getTime(String timestamp) {
			return timestamp.substring(11, 16) + " Uhr";
		}
		
		private String getDate(String timestamp) {
			return timestamp.substring(8, 10) + "." + timestamp.substring(5, 7) + "." + timestamp.substring(2, 4);
		}
		
		private String formatDate(String unformattedDate) {
			int p1 = unformattedDate.indexOf(".");
			int p2 = unformattedDate.indexOf(".", p1+1);
			String day = unformattedDate.substring(0, p1);
			String month = unformattedDate.substring(p1+1, p2);
			String year = unformattedDate.substring(p2+1);
			if(day.length()<2) day="0"+day;
			if(month.length()<2) month="0"+month;
			if(year.length()<2) year="0"+year;
			return (day + "." + month + "." + year);
		}
		
		/**
		 * Fügt Links in den Text ein
		 * @param text
		 */
		private String prepareText(String text) {
			
			// Links einfügen http://
			if(text.contains("http://")) {
				int linkIndex = 0;
				String tmpText = new String();
				while(linkIndex < text.length() && text.substring(linkIndex).contains("http://")) {				
					
					tmpText += text.substring(linkIndex, text.indexOf("http://", linkIndex));
					linkIndex = text.indexOf("http://", linkIndex);
					
					if(text.substring(linkIndex).contains(" ")) { // Link steht mitten im Text

						tmpText	+= "<a href=\""
						+ text.substring(linkIndex, text.indexOf(" ", linkIndex))
						+ "\" target=\"_blank\">LINK</a>";
						linkIndex = text.indexOf(" ", linkIndex);

					} else { // Wenn der Link am Ende steht

						tmpText	+= "<a href=\""
							+ text.substring(text.indexOf("http://", linkIndex))
							+ "\" target=\"_blank\">LINK</a>";
						linkIndex = text.length();
					}

				}
				// Text mit html Tags versehen
				text = tmpText +text.substring(linkIndex);

			}
			
			return text;
		}

		/**
		 * Bei Verlassen des Users
		 */
		public void onDisconnect() {
			onlineUsers.remove(this);
			
			//An alle user connect eines users mitteilen
			JSONObject tempObject = new JSONObject();
			try {
				tempObject.put("type", 5);
				tempObject.put("value", onlineUsers.size());
				for (NewMessageWebSocket user : onlineUsers) {
					user.connection.sendMessage(tempObject.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Leer
		 */
		public void onFragment(boolean arg0, byte arg1, byte[] arg2, int arg3, int arg4) {}

		public double getLat() {
			return this.lat;
		}

		public double getLng() {
			return this.lng;
		}
		
		public int getRatio() {
			return this.ratio;
		}

		public WebSocketConnection getConnection() {
			return connection;
		}
	}
}