package communication;

import java.util.List;

import lab4.Communication;
import lab4.Communication.Group;
import lab4.Communication.Type;

import rescuecore2.worldmodel.EntityID;

public class Message {
	public String data;
	public Communication.Type type;
	public int time;
	public EntityID sender;
	public List<EntityID> dest;
	public Communication.Group destGroup; 	
}
