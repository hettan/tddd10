package communication;

import java.util.ArrayList;
import java.util.List;


public class Communication implements ICommunication {
	
	List<Byte> buffer = new ArrayList<Byte>();
	//public static enum Type {request, notification, assignment};
	//public static enum Group {fire, police, ambulance, all};
	//List<Group> groups;
	
	@Override
	public void register(CommunicationGroup... groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Message> getMessages(CommunicationType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean sendMessage(Message message) {
		// TODO Auto-generated method stub
		return false;
	}
}
