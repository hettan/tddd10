package communication;

import java.util.List;

import rescuecore2.worldmodel.EntityID;

public class Message {
	public String data;
	public CommunicationType type;
	public int time;
	public EntityID sender;
	public List<EntityID> dest;
	public CommunicationGroup destGroup; 	
}
