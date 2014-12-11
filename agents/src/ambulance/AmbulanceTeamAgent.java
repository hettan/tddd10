package ambulance;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import commlib.message.RCRSCSMessage;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Refuge;
import sample.AbstractSampleAgent;
import sample.DistanceSorter;
import rescuecore2.standard.messages.AKSpeak;

public class AmbulanceTeamAgent extends AbstractAmbulanceTeamAgent<AmbulanceTeam> {
	
	/** The target human to rescue or to load */
	protected Human targetHuman ;
	
	private Collection<EntityID> unexploredBuildings;
	public Collection<EntityID> ListAmbulanceWithoutTasks;
	
	List<EntityID> internSeenFires = new ArrayList<EntityID>();
	List<EntityID> internSeenCivilian = new ArrayList<EntityID>();

   @Override
    public String toString() {
        return "Ambulance team";
    }
	
	@Override
	protected void postConnect() {
		super.postConnect();
		model.indexClass(StandardEntityURN.CIVILIAN,
				StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE,
				StandardEntityURN.HYDRANT, StandardEntityURN.GAS_STATION,
				StandardEntityURN.BUILDING);
		unexploredBuildings = new HashSet<EntityID>(buildingIDs);
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		map=TypeMap.KOBE;
		System.out.println("Initialization !!!!!!!!!!");
		currentTask=AmbulanceTeamTasks.NO_TASK;
	}

	EntityID goal = null;

	@Override
	protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
		if (time == config
				.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			// Subscribe to channel 1
			sendSubscribe(time, 2);
			//setMessageChannel(1);
		}
		//RCRSCSMessage msg1 = receivedMessageList.get(0);
		//System.out.println("Message: " + msg1.getSendTime());
		System.out.println("Time: "+ time);
		// Look at the fire location
		for(EntityID next : changed.getChangedEntities()) {
			if(buildingIDs.contains(next)) {
				Building building = (Building) model.getEntity(next);
				if(building.isOnFire() && !internSeenFires.contains(next)) {
					internSeenFires.add(next);
					//TODO speak add to fireArea
					System.out.println("Send fire location to firebrigade");
				}
			}
		}
		
		//Look at the civilians
		for (Human next : getTargets()) {
			if (next.getPosition().equals(location().getID())) {
				// Targets in the same place might need rescuing or loading
				if ((next instanceof Civilian) && !(location() instanceof Refuge)) {
					System.out.println("Ambulance found a civilian!!");
				}
				if ((next instanceof Civilian) && (next.getBuriedness() > 0
						|| next.getDamage()>0) && !(location() instanceof Refuge)) {
					System.out.println("Ambulance see a civilian in danger !!");
					internSeenCivilian.add(next.getID());
				}
			}
		}
		
		updateUnexploredBuildings(changed);
		if(me().getBuriedness()>0) {
			//todo send help to center
			//sendRescue(time,me().getID());
			System.out.println("Ambulance is buried and need rescue !");
			currentTask = AmbulanceTeamTasks.BURIED;
			return;
		}
		//todo if at is stuck in blockade send message
		//currentTask = AmbulanceTeamTasks.STUCK;
	
		if (someoneOnBoard()) {
		//human is dead ?
		System.out.println("Transporting civilians to refuge");
		if (targetHuman.getHP() == 0){
			sendUnload(time);
			targetHuman = null;
			currentTask = AmbulanceTeamTasks.NO_TASK;
			return;
		}
		else {
			// Am I at a refuge?
			if (location() instanceof Refuge) {
				// Unload!
				Logger.info("Unloading");
				sendUnload(time);
				currentTask = AmbulanceTeamTasks.NO_TASK;
				targetHuman = null;
				return;
			}
			else
			{
				// Move to a refuge
				List<EntityID> path = search.performSearch(me()
						.getPosition(), refugeIDs);
				if (path != null) {
					Logger.info("Moving to refuge" + targetHuman);
					System.out.println("Ambulance move to refuge");
					sendMove(time, path);
					currentTask = AmbulanceTeamTasks.MOVE_TO_REFUGE;
					return;
				}
				// What do I do now? Might as well carry on and see if we can
				// dig someone else out.
				Logger.debug("Failed to plan path to refuge");
			}
		}
				
	}
		
		// Go through targets (sorted by distance) and check for things we can
		// do
		for (Human next : getTargets()) {
			if (next.getPosition().equals(location().getID())) {
				// Targets in the same place might need rescuing or loading
				if ((next instanceof Civilian) && next.getBuriedness() == 0
						&& !(location() instanceof Refuge)) {
					// Load
					Logger.info("Loading " + next);
					sendLoad(time, next.getID());
					System.out.println("Ambulance load a civilian");
					currentTask = AmbulanceTeamTasks.LOADING;
					targetHuman = next;
					return;
				}
				if ((next instanceof Civilian) && next.getDamage()>0
						&& !(location() instanceof Refuge)) {
					// Load
					Logger.info("Loading " + next);
					sendLoad(time, next.getID());
					System.out.println("Ambulance load a civilian");
					currentTask = AmbulanceTeamTasks.LOADING;
					targetHuman = next;
					return;
				}
				if (next.getBuriedness() > 0) {
					// Rescue
					Logger.info("Rescueing " + next);
					sendRescue(time, next.getID());
					System.out.println("Ambulance rescue a civilian");
					currentTask = AmbulanceTeamTasks.RESCUING;
					targetHuman = next;
					return;
				}
			} else {
				// Try to move to the target
				List<EntityID> path = search.performSearch(me()
						.getPosition(), next.getPosition());
				if (path != null) {
					Logger.info("Moving to target");
					sendMove(time, path);
					System.out.println("Ambulance move to civilian");
					currentTask = AmbulanceTeamTasks.MOVING_TO_HUMAN;
					targetHuman = next;
					return;
				}
			}
		}
		
		List<EntityID> path = search.performSearch(me().getPosition(),
				unexploredBuildings);
		if (path != null) {
			Logger.info("Searching buildings");
			//System.out.println("Search building");
			sendMove(time, path);
			currentTask = AmbulanceTeamTasks.EXPLORING;
			targetHuman = null;
			return;
		}
		
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		// TODO Auto-generated method stub
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}

	private boolean someoneOnBoard() {
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                Logger.debug(next + " is on board");
                return true;
            }
        }
        return false;
    }
	
	private boolean someoneInSameBuilding() {
		for (StandardEntity next : model
				.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
			Human human = (Human) next;
			if (human.getPosition(model).getID().equals(location().getID())) {
				Logger.debug(next + " is in the same building");
				return true;
			}
		}
		return false;
	}
	
	// getTargets by distance
	private List<Human> getTargets() {
		List<Human> targets = new ArrayList<Human>();
		for (StandardEntity next : model.getEntitiesOfType(
				StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE,
				StandardEntityURN.POLICE_FORCE,
				StandardEntityURN.AMBULANCE_TEAM)) {
			Human h = (Human) next;
			if (h == me()) {
				continue;
			}
			if (h.isHPDefined() && h.isBuriednessDefined()
					&& h.isDamageDefined() && h.isPositionDefined()
					&& h.getHP() > 0
					&& (h.getBuriedness() > 0 || h.getDamage() > 0)) {
				targets.add(h);
				
			}
		}
		Collections.sort(targets, new DistanceSorter(location(), model));
		return targets;
	}
	
	private void updateUnexploredBuildings(ChangeSet changed) {
		for (EntityID next : changed.getChangedEntities()) {
			unexploredBuildings.remove(next);
		}
	}
	
	protected void printHumanDetails(Human h) {
		Logger.info("Buriedness: " + h.getBuriedness());
		Logger.info("Damage: " + h.getDamage());
		if (h.isPositionDefined())
			Logger.info("h.isPositionDefined()");
		if (h.isDamageDefined() && h.getDamage() != 0)
			Logger.info("Damage: " + h.getDamage());
	}
}