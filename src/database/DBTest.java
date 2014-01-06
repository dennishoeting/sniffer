package database;

import java.util.List;
import java.util.Map;

import models.SnifferMessage;

public class DBTest {
	public static void main(String[] args) {
		try {
			DBMethods test = DBMethods.getInstance();
			List<SnifferMessage> bla = test.getMessagesInRadiusWithOffset(53.1367194, 8.2165357, 10, 10, 10);
			for(SnifferMessage i : bla) {
				System.out.println(i.getText() +" " +i.getLocation() +" " +i.getDistance() +" " +i.getTimestamp());
			}
			
//			System.out.println(test.getMessageByID(14, 53.1367194, 8.2165357).getDistance());
//			
//			System.out.println(test.isMessageInRadius(53.1367194, 8.2165357, 14, 9));
			
		} catch(Exception e) {
			
		}
	}
}
