package ambulance;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Refuge;
import sample.DistanceSorter;
import rescuecore2.standard.messages.AKSpeak;

public class AmbulanceTeamAgent extends AbstractAmbulanceTeamAgent<AmbulanceTeam> {
	
	/** The target human to rescue or to load */
	protected Human targetHuman ;
	
	private Collection<EntityID> unexploredBuildings;


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
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		// TODO Auto-generated method stub
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		map=TypeMap.KOBE;
		currentTask=AmbulanceTeamTasks.NO_TASK;
	}

	EntityID goal = null;

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		if (time == config
				.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			// Subscribe to channel 1
			sendSubscribe(time, 2);
		}
		System.out.println("Ambulance team");
		try {
			String msg = "position "
					+ String.valueOf(me().getPosition().getValue());
			Logger.debug("Send my position on channel 1 " + msg);
			sendSpeak(time, 1, msg.getBytes("UTF-8"));
		} catch (java.io.UnsupportedEncodingException uee) {
			Logger.error(uee.getMessage());
		}
		for (Command next : heard) {
			byte[] content = ((AKSpeak)next).getContent();
			String txt = null;
			try {
				txt = new String(content, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String[] parts = txt.split(" ");
			if (parts.length != 2) {
				Logger.warn("Ignoring " + txt);
				continue;
			}
			int agent = Integer.parseInt(parts[0]);
			int building = Integer.parseInt(parts[1]);

			if (agent == me().getID().getValue()) {
				goal = new EntityID(building);
				List<EntityID> path = search.breadthFirstSearch(location()
						.getID(), goal);

				sendMove(time, path);
				Logger.error("Moving to some random building");
				return;
			}
		}
		updateUnexploredBuildings(changed);
		// if the agent has already a task where to go
		if (goal != null) {
			System.out.println("Ambulance have a goal");
			List<EntityID> path = search.breadthFirstSearch(location().getID(),
					goal);
			if (path.size() == 1) {
				Logger.error("Done with reaching");
				boolean foundSomeone = someoneInSameBuilding();
				Logger.error("Found someone? " + foundSomeone);
				// We have reached the building
				String msg = "reached " + goal.getValue() + " " + foundSomeone;
				try {
					sendSpeak(time, 1, msg.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(foundSomeone == true){
					System.out.println("Ambulance found someone");
					for (StandardEntity next : model
							.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
						Human human = (Human) next;
						if (human.getPosition(model).getID().equals(location().getID())) {
							targetHuman=human;
							Logger.debug(next + " is in the same building");
						}
					}
					currentTask = AmbulanceTeamTasks.FOUND_HUMAN;
				}
				else
				{
					targetHuman=null;
					currentTask = AmbulanceTeamTasks.NO_TASK;
				}
				
			} else {
				sendMove(time, path);
				return;
			}
		}
		
		//updateCurrentTask();
		// Am I transporting a civilian to a refuge?
		if (someoneOnBoard()) {
			//human is dead ?
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
					List<EntityID> path = search.breadthFirstSearch(me()
							.getPosition(), refugeIDs);
					if (path != null) {
						Logger.info("Moving to refuge" + targetHuman);
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
//		for (Human next : getTargets()) {
//			if (next.getPosition().equals(location().getID())) {
//				// Targets in the same place might need rescuing or loading
//				if ((next instanceof Civilian) && next.getBuriedness() == 0
//						&& !(location() instanceof Refuge)) {
//					// Load
//					Logger.info("Loading " + next);
//					sendLoad(time, next.getID());
//					currentTask = AmbulanceTeamTasks.LOADING;
//					targetHuman = next;
//					return;
//				}
//				if (next.getBuriedness() > 0) {
//					// Rescue
//					Logger.info("Rescueing " + next);
//					sendRescue(time, next.getID());
//					currentTask = AmbulanceTeamTasks.RESCUING;
//					targetHuman = next;
//					return;
//				}
//			} else {
//				// Try to move to the target
//				List<EntityID> path = search.breadthFirstSearch(me()
//						.getPosition(), next.getPosition());
//				if (path != null) {
//					Logger.info("Moving to target");
//					sendMove(time, path);
//					currentTask = AmbulanceTeamTasks.MOVING_TO_HUMAN;
//					targetHuman = next;
//					return;
//				}
//			}
//		}
		
		// Nothing to do
		List<EntityID> path = search.breadthFirstSearch(me().getPosition(),
				unexploredBuildings);
		if (path != null && currentTask == AmbulanceTeamTasks.NO_TASK) {
			Logger.info("Searching buildings");
			sendMove(time, path);
			currentTask = AmbulanceTeamTasks.NO_TASK;
			targetHuman = null;
			return;
		}

		//Logger.info("Moving randomly");
		//sendMove(time, randomWalk());
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
