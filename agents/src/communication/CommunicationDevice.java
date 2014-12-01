package communication;

import java.util.List;

public interface CommunicationDevice {

	/*
	 * Registers this communication device to listen
	 * to messages from the specified groups.
	 * 
	 *@param groups the groups to listen for messages
	 */
	void register(CommunicationGroup... groups);
	
	/*
	 * Gets all available messages of the specified 
	 * type. Only gets messages from the groups
	 * the device is listening to.
	 * 
	 * @param type the type of message to fetch
	 * @param time the current simulation time (maybe used to maintain caches)
	 */
	List<Message> getMessages(CommunicationType type, int time);
	
	/*
	 * Transmits the specified message to the network.
	 * 
	 * @param message the message to transmit
	 */
	boolean sendMessage(Message message);
}
