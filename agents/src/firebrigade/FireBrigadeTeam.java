package firebrigade;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.io.UnsupportedEncodingException;
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
import sample.AbstractSampleAgent;

public class FireBrigadeTeam extends AbstractSampleAgent<FireBrigade> {
	private static final String MAX_WATER_KEY = "fire.tank.maximum";
	private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";

	List<EntityID> internSeenFires = new ArrayList<EntityID>();
	List<Integer> internToHandleFires = new ArrayList<Integer>();

	EntityID targetBuilding = null;
	Building burningBuilding;
	FireKnowledgeStore fireKnowledgeStore;
	boolean nearBuilding;
	
	private int maxDistance;
    private int maxWater;
	
    @Override
	protected void postConnect() {
		super.postConnect();
		model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,
				StandardEntityURN.HYDRANT, StandardEntityURN.GAS_STATION);
		maxWater = config.getIntValue(MAX_WATER_KEY);
		maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
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
			// Subscribe to channel 1
			sendSubscribe(time, 3);
		}
		
		//Communication position
		try {
			String msg = "position "
					+ String.valueOf(me().getPosition().getValue());
			Logger.debug("Send my position on channel 3 " + msg);
			sendSpeak(time, 3, msg.getBytes("UTF-8"));
		} catch (java.io.UnsupportedEncodingException uee) 
		{
			Logger.error(uee.getMessage());
		}
		
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
					try 
					{
						int nextID = next.getValue();
						String msg = "fireseen " + String.valueOf(nextID);
						sendSpeak(time, 3, msg.getBytes("UTF-8"));
					} 
						catch (java.io.UnsupportedEncodingException uee) 
					{
						Logger.error(uee.getMessage());
					}
					nearBuilding = true;
				}
			}
		}
		
		//Get Messages from Station
		for (Command next2 : heard) {
			Logger.debug("Heard " + next2);
			byte[] content = ((AKSpeak) next2).getContent();
			String txt = null;
			try {
				txt = new String(content, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			String[] parts = txt.split(" ");
			if (parts.length != 2) {
				Logger.warn("Ignoring " + txt);
				continue;
			}
			
			//Agent and fireArea he has to handle
			int agent = Integer.parseInt(parts[0]);
			int fireAreaID = Integer.parseInt(parts[1]);
			List<FireArea> fireAreas = fireKnowledgeStore.getFireAreas();
			FireArea fireArea = fireAreas.get(fireAreaID);
			
			
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
				// TODO new path
				List<EntityID> path = search.breadthFirstSearch(me()
						.getPosition(), refugeIDs);
				if (path != null) {
					Logger.info("Moving to refuge");
					sendMove(time, path);
					return;
				}
			}
			
			
			int fieryness = 999999999;
			//Search for building to extinguish
			if (internToHandleFires.size() != 0) {
				for (int i = 0; i < internToHandleFires.size(); i++) {
					int burningBuildingID = internToHandleFires.get(i);
					EntityID burningBuildingEID = new EntityID(
							burningBuildingID);
					Building burningBuilding = (Building) model
							.getEntity(burningBuildingEID);
					int fierynessTemp = burningBuilding.getFieryness();
					if (fierynessTemp == 0) {
						internToHandleFires.remove(i);
						fierynessTemp = 999999999;
						//SEND EXTINGUISHED FIRE
						try {
							String msg = "extinguishedfire " + String.valueOf(burningBuildingID);
							Logger.debug("Send my position on channel 3 " + msg);
							sendSpeak(time, 3, msg.getBytes("UTF-8"));
						} catch (java.io.UnsupportedEncodingException uee) 
						{
							Logger.error(uee.getMessage());
						}
						
						
					}
					if (fierynessTemp <= fieryness) {
						fieryness = fierynessTemp;
						targetBuilding = burningBuildingEID;
					}
				}
				
				//Go to Building and Extinguish if you are there
				if (targetBuilding != null) 
				{
					if (model.getDistance(me.getID(), targetBuilding) <= maxDistance)
					{	
						sendExtinguish(time, targetBuilding, maxWater);
						return;
					}
					else
					{
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
							sendExtinguish(time, next3, maxWater);
							return;
						}
					}
				}
			}
			
			// TODO Explore
			sendRest(time);
		}
	}

	private List<EntityID> planPathToFire(EntityID targetBuilding) {
        Collection<StandardEntity> targets = model.getObjectsInRange(targetBuilding, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(me().getPosition(), targetBuilding);
	}
}