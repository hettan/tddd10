package communication;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;


public class CommunicationDeviceTemporaryImpl implements CommunicationDevice {
	
	List<Byte> buffer = new ArrayList<Byte>();
	//public static enum Type {request, notification, assignment};
	//public static enum Group {fire, police, ambulance, all};
	//List<Group> groups;
	List<CommunicationGroup> groups;
	
	
	/*
	 * Temporary solution. Uses static list of all groups, which contains a list of the belonging messages.
	 */
	private Object notificationLock = new Object();
	private Object assignmentLock = new Object();
	private static int REMOVE_TIME = 4; //Keep messages for 4 time steps.
	private static HashMap<CommunicationGroup, List<Message>> notificationMessages = new HashMap<CommunicationGroup, List<Message>>();
	private static HashMap<CommunicationGroup, List<Message>> assignmentMessages = new HashMap<CommunicationGroup, List<Message>>();
	
	private void addNotification(Message message)
	{
		synchronized(notificationLock)
		{
			if(!notificationMessages.containsKey(message.destGroup))
			{
				notificationMessages.put(message.destGroup, new ArrayList<Message>());
			}
			notificationMessages.get(message.destGroup).add(message);
		}
	}
	
	private void addAssignment(Message message)
	{
		synchronized(assignmentLock)
		{
			if(!assignmentMessages.containsKey(message.destGroup))
			{
				assignmentMessages.put(message.destGroup, new ArrayList<Message>());
			}
			assignmentMessages.get(message.destGroup).add(message);
		}
	}
	
	private List<Message> getNotifications(CommunicationGroup group, int time)
	{
		synchronized(notificationLock)
		{
			if(notificationMessages.containsKey(group))
			{
				ArrayList<Message> toRemove = new ArrayList<Message>();
				for(Message message : notificationMessages.get(group))
				{
					if(message.time < time - REMOVE_TIME)
						toRemove.add(message);
				}
				notificationMessages.get(group).removeAll(toRemove);
				
				ArrayList<Message> ret = new ArrayList<Message>();
				for(Message message : notificationMessages.get(group))
					if(message.time < time) //(because of the threads, and static storage, messages can be passed instantly. avoid this.
						ret.add(message); //Only add messages which have been "sent".
				
				return ret;
			}
		}
		return new ArrayList<Message>();
	}
	
	private List<Message> getAssignments(CommunicationGroup group, int time)
	{
		synchronized(assignmentLock)
		{
			if(assignmentMessages.containsKey(group))
			{
				ArrayList<Message> toRemove = new ArrayList<Message>();
				for(Message message : assignmentMessages.get(group))
				{
					if(message.time < time - REMOVE_TIME)
						toRemove.add(message);
				}
				assignmentMessages.get(group).removeAll(toRemove);
				
				ArrayList<Message> ret = new ArrayList<Message>();
				for(Message message : assignmentMessages.get(group))
					if(message.time < time) //(because of the threads, and static storage, messages can be passed instantly. avoid this.
						ret.add(message); //Only add messages which have been "sent".
				
				return ret;
			}
		}
		return new ArrayList<Message>();
	}
	
	public CommunicationDeviceTemporaryImpl()
	{
		groups = new ArrayList<CommunicationGroup>();
	}
	
	@Override
	public void register(CommunicationGroup... groups) {
		// TODO Auto-generated method stub
		for(CommunicationGroup group : groups)
		{
			if(!this.groups.contains(group))
				this.groups.add(group);
		}
	}

	@Override
	public List<Message> getMessages(CommunicationType type, int time) {
		// TODO Auto-generated method stub
		ArrayList<Message> messages = new ArrayList<Message>();
		
		if(type == CommunicationType.NOTIFICATION)
		{
			for(CommunicationGroup group : groups)
			{
				messages.addAll(getNotifications(group, time));
			}
		}
		if(type == CommunicationType.ASSIGNMENT)
		{
			for(CommunicationGroup group : groups)
			{
				messages.addAll(getAssignments(group, time));
			}
		}
		
		return messages;
	}

	@Override
	public boolean sendMessage(Message message) {
		// TODO Auto-generated method stub
		
		if(message.type == CommunicationType.NOTIFICATION)
			addNotification(message);
		
		if(message.type == CommunicationType.ASSIGNMENT)
			addAssignment(message);
		
		return true;
	}
}
