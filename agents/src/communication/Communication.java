package communication;

import java.util.ArrayList;
import java.util.List;


public class Communication {
	
	List<Byte> buffer = new ArrayList<Byte>();
	public static enum Type {request, notification, assignment};
	public static enum Group {fire, police, ambulance, all};
	List<Group> groups;
	
	public Communication(List<Group> groups) {
		this.groups = groups; 
	}
	
	public List<Message> getMessages(Type type, List<Group> groups) {
		
		return null;
	}
}
