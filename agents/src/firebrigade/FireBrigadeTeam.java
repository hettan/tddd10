package firebrigade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import communication.CommunicationGroup;
import communication.CommunicationType;
import communication.Message;

import exploration.ExplorationAgent;

public class FireBrigadeTeam extends ExplorationAgent<FireBrigade> {
	private static final String MAX_WATER_KEY = "fire.tank.maximum";
	private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

	List<EntityID> internSeenFires = new ArrayList<EntityID>();
	List<Integer> internToHandleFires = new ArrayList<Integer>();

	EntityID targetBuilding = null;
	Building burningBuilding;
	FireKnowledgeStore fireKnowledgeStore;
	boolean nearBuilding;
	
	private int maxDistance;
    private int maxWater;
	private int counter = 0;
    private int maxPower;
    @Override
	protected void postConnect() {
		super.postConnect();
		model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,
				StandardEntityURN.HYDRANT, StandardEntityURN.GAS_STATION);
		maxWater = config.getIntValue(MAX_WATER_KEY);
		maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
		maxPower = config.getIntValue(MAX_POWER_KEY);
		
		communication.register(CommunicationGroup.FIREBRIGADE);
	}

	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		FireBrigade me = me();
		//Initialize
		if (time == config
				.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			sendSubscribe(time, 2);
		}
		
		
		//Communication position every fifth iteration
		int randomNum = 3 + (int)(Math.random()*5);
		if(counter >= randomNum)
		{
			
				int busy;
				if(targetBuilding != null)
				{
					busy = 1;
				}
				else
				{
					busy = 0;
				}
				String msg = "informations " + String.valueOf(me().getX()) + " " + String.valueOf(me().getY()) + " " 
						+ String.valueOf(me().getID() + " " + String.valueOf(me().getWater()) + " " + String.valueOf(busy));
				Logger.debug("Send my position on channel 3 " + msg);
				//sendSpeak(time, 3, msg.getBytes());
				Message message = new Message();
				message.sender = getID();
				message.destGroup = CommunicationGroup.FIRESTATION;
				message.time = time;
				message.type = CommunicationType.NOTIFICATION;
				message.data = msg;
				communication.sendMessage(message);
				counter = 0;
		}
		counter++;
		
		//Seen burning buildings
		for (EntityID next : changed.getChangedEntities()) 
		{
			if (buildingIDs.contains(next)) 
			{
				Building building = (Building) model.getEntity(next);
				if (building.isOnFire() && !internSeenFires.contains(next)) 
				{
					internSeenFires.add(next);
					//Send seen Fire
					int nextID = next.getValue();
					String msg = "fireseen " + String.valueOf(nextID) + " " + String.valueOf(building.getTemperature());
					System.out.println("FireSeen Message: " + msg);
					System.out.println("In speak: " + msg);
					//sendSpeak(time, 3, msg2.getBytes());
					Message message = new Message();
					message.sender = getID();
					message.destGroup = CommunicationGroup.FIRESTATION;
					message.time = time;
					message.type = CommunicationType.NOTIFICATION;
					message.data = msg;
					communication.sendMessage(message);
					nearBuilding = true;
				}
			}
		}
		
		//Get Messages from Station
		int fireAreaID = 0;
		int agent = 0;
		FireArea fireArea = null;
		
		for(Message msg : communication.getMessages(CommunicationType.REQUEST, time)) {
		//for (Command next : heard) 
		//{
			String txt = msg.data;
			//byte[] content = ((AKSpeak) next).getContent();
			//String txt = new String(content);
			//Logger.error("Heard " + next + txt);
			String[] parts = txt.split(" ");
				
			//if(parts[0] == "mission ")
			//{
				//Agent and fireArea he has to handle
				agent = Integer.parseInt(parts[0]);
				fireAreaID = Integer.parseInt(parts[1]);
				List<FireArea> fireAreas = fireKnowledgeStore.getFireAreas();
				fireArea = fireAreas.get(fireAreaID);	
				break;
			//}
		}
		
			if (me.getID().getValue() == agent) {
				internToHandleFires = fireArea.getBuildingsInArea();
			}

			// Refilling Water
			if (me.getWater() < maxWater && location() instanceof Refuge) {
				sendRest(time);
				return;
			}

			// Go Refill Water
			if (me.getWater() == 0) {
				// TODO new Search
				List<EntityID> path = search.performSearch(me()
						.getPosition(), refugeIDs);
				if (path != null) {
					Logger.info("Moving to refuge");
					sendMove(time, path);
					return;
				}
			}
			
			int fieryness = 999999999;
			//Search for building to extinguish
			if (internToHandleFires.size() != 0) 
			{
				for (int i = 0; i < internToHandleFires.size(); i++) 
				{
					int burningBuildingID = internToHandleFires.get(i);
					EntityID burningBuildingEID = new EntityID(burningBuildingID);
					Building burningBuilding = (Building) model.getEntity(burningBuildingEID);
					int fierynessTemp = burningBuilding.getFieryness();
					if (fierynessTemp == 0) 
					{
						internToHandleFires.remove(i);
						fierynessTemp = 999999999;
						//SEND EXTINGUISHED FIRE
						
						
						String msg = "extinguishedfire " + String.valueOf(burningBuildingID);
						Logger.debug("Send my position on channel 3 " + msg);
						System.out.println("Extinguish Message: " + msg);
						//sendSpeak(time, 3, msg.getBytes());
						Message message = new Message();
						message.sender = getID();
						message.destGroup = CommunicationGroup.FIRESTATION;
						message.time = time;
						message.type = CommunicationType.NOTIFICATION;
						message.data = msg;
						communication.sendMessage(message);

					}
					
					if (fierynessTemp <= fieryness) 
					{
						fieryness = fierynessTemp;
						targetBuilding = burningBuildingEID;
					}
				}

				//Go to Building and Extinguish if you are there
				if (targetBuilding != null) 
				{
					if (model.getDistance(me.getID(), targetBuilding) <= maxDistance)
					{	
						sendExtinguish(time, targetBuilding, maxPower);
						return;
					}
					else
					{
						//TODO new Search
						List<EntityID> path = planPathToFire(targetBuilding);
						if (path != null)
						{
			                sendMove(time, path);
			                return;
						}
					}				
				}
			}
			
			if(nearBuilding)
			{
				for (EntityID next3 : changed.getChangedEntities()) {
					if (buildingIDs.contains(next3)) {
						Building building = (Building) model.getEntity(next3);
						if (building.isOnFire()) {
							sendExtinguish(time, next3, maxPower);
							return;
						}
					}
				}
			}
			// TODO Explore
			List<EntityID> path;
			path = explore();
            sendMove(time, path);
		}
	
	private List<EntityID> planPathToFire(EntityID targetBuilding) {
        Collection<StandardEntity> targets = model.getObjectsInRange(targetBuilding, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.performSearch(me().getPosition(), targetBuilding);
	}
}