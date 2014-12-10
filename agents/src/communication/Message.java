package communication;

import java.util.ArrayList;
>>>>>>> 3c6f29d8e3cc67455c5a574b5a1ef9a42a15b281
import java.util.List;

import rescuecore2.worldmodel.EntityID;

public class Message {
	public String data;
	public CommunicationType type;
	public int time;
	public EntityID sender;
	public List<EntityID> dest = new ArrayList<EntityID>();
	public CommunicationGroup destGroup = CommunicationGroup.ALL; 	
}
