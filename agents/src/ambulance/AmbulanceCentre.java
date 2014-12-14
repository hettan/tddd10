package ambulance;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;

public class AmbulanceCentre extends
StandardAgent<rescuecore2.standard.entities.AmbulanceCentre> {
	private List<Integer> agentsWithGoal = new ArrayList<Integer>();
	private List<Integer> BuriedCivilians = new ArrayList<Integer>();
	private Set<EntityID> frontier = null;
	private List<Integer> Listbuilding= new ArrayList<Integer>();
	private List<Integer> Listhp = new ArrayList<Integer>();
	private List<Integer> Listburiedness = new ArrayList<Integer>();
	private List<Integer> Listdamage = new ArrayList<Integer>();
	private List<Integer> ListX = new ArrayList<Integer>();
	private List<Integer> ListY = new ArrayList<Integer>();
	
	
	private int buildingId;
	private int hp;
	private int buriedness;
	private int damage;
	private int distanceX;
	private int distanceY;

	protected AtTargetSelection Target;
	
	@Override
	public String toString() {
		return "Ambulance centre";
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
			// Subscribe to channel 1
			sendSubscribe(time, 2);
		//System.out.println("Ambulance Center");
		if (frontier == null) {
			frontier = new HashSet<EntityID>();
			for (StandardEntity entity : model
					.getEntitiesOfType(StandardEntityURN.BUILDING)) {
				frontier.add(entity.getID());
			}
		}
		List<EntityID> agentsAvailable = new ArrayList<EntityID>();
		List<EntityID> agentsburied = new ArrayList<EntityID>();
		for (Command next : heard) {
			try {
				byte[] content = ((AKSpeak) next).getContent();
				String txt = new String(content, "UTF-8");
				Logger.error("Heard " + next + txt);
				//System.out.println("Heard" + next + txt);
				String[] parts = txt.split(" ");
				if (parts.length == 0) {
					Logger.warn("Ignoring " + txt);
					continue;
				}
				switch (parts[0]) {
				case "Civilian": {
					Logger.error("Agent has reached a building with civilian");
					buildingId = Integer.parseInt(parts[1]);
					boolean foundSomeone = Boolean.parseBoolean(parts[2]);
					buriedness = Integer.parseInt(parts[3]);
					damage = Integer.parseInt(parts[4]);				
					hp = Integer.parseInt(parts[5]);
					distanceX = Integer.parseInt(parts[6]);
					distanceY = Integer.parseInt(parts[7]);
					if (foundSomeone) {
						Listbuilding.add(buildingId);
						Listburiedness.add(buriedness);
						Listdamage.add(damage);
						Listhp.add(hp);
						ListX.add(distanceX);
						ListY.add(distanceY);
						Logger.error("Found " + BuriedCivilians.size()
								+ " buildings with civilians so far!");
					}
					agentsWithGoal.remove((Integer) next.getAgentID()
							.getValue());
				}
				case "NO_TASK": {
					agentsAvailable.add(next.getAgentID());
					break;
				}
				case "buried": {
					int locationAgent = Integer.parseInt(parts[1]);
					agentsburied.add(next.getAgentID());
					break;
				}
				default:
					//throw new RuntimeException("Unknown: " + txt);
				}

			} catch (UnsupportedEncodingException ex) {
				//Logger.error(ex.getMessage());
			}
		}		
//		if(agentsburied !=null) {
//			Pair<List<EntityID>, List<EntityID>> bestAgent = Target.rescueAgentSelection(agentsburied,agentsAvailable);
//			String msg = "SaveAgent " + buildingId;
//			sendSpeak(time, 1, msg.getBytes());
//		}
//		
//		
//		if(BuriedCivilians != null) {
//			//Pair<List<Integer>, List<EntityID>> bestCivilians = Target.rescueSelection(BuriedCivilians,agentsAvailable);
//			int bestCivilians = Target.rescueSelectionCivilian(Listbuilding,Listburiedness,Listdamage,Listhp, ListX,ListY ,agentsAvailable);
//			String msg = "SaveCivilian " + bestCivilians + " " + buildingId;
//			sendSpeak(time, 1, msg.getBytes());
//		}
//		else
		{
			//TO DO explore
			if(agentsAvailable !=null) {
				String msg = "Explore " + agentsAvailable.add(getID());
				//sendSpeak(time, 1, msg.getBytes());
			}
		}
		sendRest(time);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
	}
}