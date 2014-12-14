package ambulance;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import exploration.ExplorationAgent;
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
		map=TypeMap.KOBE;
		System.out.println("Initialization !!!!!!!!!!");
		currentTask=AmbulanceTeamTasks.NO_TASK;
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
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		if (time == config
				.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			// Subscribe to channel 1
			sendSubscribe(time, 1);
		}
		//System.out.println("Time: "+ time);
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
		if(currentTask==AmbulanceTeamTasks.NO_TASK) {
			for (Human next : getTargets()) {
				if (next.getPosition().equals(location().getID())) {
					// Targets in the same place might need rescuing or loading
					if ((next instanceof Civilian) && !(location() instanceof Refuge)) {
						//System.out.println("Ambulance found a civilian!!");
					}
					if ((next instanceof Civilian) && (next.getDamage()>0 && next.getBuriedness() == 0)
							&& !(location() instanceof Refuge)) {
						System.out.println("Ambulance see a civilian in danger !!");
						internSeenCivilian.add(next.getID());
						boolean foundSomeone = someoneInSameBuilding();
						if(foundSomeone) {
							currentTask=AmbulanceTeamTasks.FOUND_HUMAN;
							targetHuman = next;
						}
//						String msg = "Civilian " + goal.getValue() + " " + foundSomeone + " "+ next.getBuriedness() +
//								" " + next.getDamage() + " " + next.getHP() + " " + next.getX() + " " + next.getY();
//						try {
//							sendSpeak(time, 2, msg.getBytes("UTF-8"));
//						} catch (UnsupportedEncodingException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
				}
			}
		}
			
		updateUnexploredBuildings(changed);
		if(me().getBuriedness()>0) {
			//todo send buriedAgentMessage
			String msg2 = "buried " + String.valueOf(me().getPosition().getValue());
			System.out.println("Ambulance is buried and need rescue !");
			try {
				sendSpeak(time, 2, msg2.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Ambulance is buried and need rescue !");
			currentTask = AmbulanceTeamTasks.BURIED;
			return;
		}
		
		
		if(me().getDamage() > 0) {
			List<EntityID> path = search.performSearch(me()
					.getPosition(), refugeIDs);
				Logger.info("Moving to refuge");
				System.out.println("Ambulance move to refuge because damage");
				sendMove(time, path);
				currentTask = AmbulanceTeamTasks.MOVE_TO_REFUGE;
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
			}
			else {
				// Am I at a refuge?
				if (location() instanceof Refuge) {
					// Unload!
					Logger.info("Unloading");
					sendUnload(time);
					currentTask = AmbulanceTeamTasks.NO_TASK;
					targetHuman = null;
					try {
						String msg1 = "NO_TASK "
								+ String.valueOf(me().getPosition().getValue());
						Logger.debug("Send my position on channel 1 " + msg1);
						System.out.println("Ambulance unload and send position");
						sendSpeak(time, 2, msg1.getBytes("UTF-8"));
					} catch (java.io.UnsupportedEncodingException uee) {
						Logger.error(uee.getMessage());
					}
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
		
		for (Command next : heard) {
			byte[] content = ((AKSpeak) next).getContent();
			String txt = null;
			try {
				txt = new String(content, "UTF-8");
				String[] parts = txt.split(" ");
				if (parts.length == 0) {
					Logger.warn("Ignoring " + txt);
					continue;
				}
				switch (parts[0]) {
				case "Explore": {
					currentTask = AmbulanceTeamTasks.NO_TASK;
				}
				case "SaveAgent": {
					int agent = Integer.parseInt(parts[1]);
					int building = Integer.parseInt(parts[2]);
					if(currentTask == AmbulanceTeamTasks.NO_TASK) {
						if (agent == me().getID().getValue()) {
							goal = new EntityID(building);
							List<EntityID> path = search.performSearch(location()
									.getID(), goal);
							sendMove(time, path);
							System.out.println("Moving to an agent");
							Logger.error("Moving to an agent");
							currentTask = AmbulanceTeamTasks.MOVING_TO_HUMAN;
							//targetHuman = goal;
						}
					}
					break;
				}
				case "SaveCivilian": {
					int agent = Integer.parseInt(parts[1]);
					int building = Integer.parseInt(parts[2]);
					if(currentTask == AmbulanceTeamTasks.NO_TASK) {
						if (agent == me().getID().getValue()) {
							goal = new EntityID(building);
							List<EntityID> path = search.performSearch(location()
									.getID(), goal);
							sendMove(time, path);
							System.out.println("Moving to a civilian");
							Logger.error("Moving to a civilian");
							currentTask = AmbulanceTeamTasks.MOVING_TO_HUMAN;
							//targetHuman = goal;
						}
					}
					break;
				}
				default:
					//throw new RuntimeException("Unknown: " + txt);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(currentTask == AmbulanceTeamTasks.MOVING_TO_HUMAN) {
			return;
		}
		
		// Go through targets (sorted by distance) and check for things we can
		// do
		for (Human next : getTargets()) {
			if (next.getPosition().equals(location().getID())) {
				// Targets in the same place might need rescuing or loading
				if ((next instanceof Civilian) && (next.getDamage()>0 && next.getBuriedness() == 0) 
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
		System.out.println("Ambulance have no task and send position");
		try {
			String msg3 = "NO_TASK "
					+ String.valueOf(me().getPosition().getValue());
			Logger.debug("Send my position on channel 1 " + msg3);
			sendSpeak(time, 2, msg3.getBytes("UTF-8"));
		} catch (java.io.UnsupportedEncodingException uee) {
			Logger.error(uee.getMessage());
		}
		
		List<EntityID> path = explore();
		if (path != null) {
			Logger.info("Searching buildings");
			System.out.println("Search building");
			sendMove(time, path);
			currentTask = AmbulanceTeamTasks.NO_TASK;
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
}