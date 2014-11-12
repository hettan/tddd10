package ambulance;

import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

public class AmbulanceCenter extends
StandardAgent<rescuecore2.standard.entities.AmbulanceCentre> {
	private List<Integer> agentsWithGoal = new ArrayList<Integer>();
	private List<Integer> civiliansLocations = new ArrayList<Integer>();

	private Set<EntityID> frontier = null;

	@Override
	public String toString() {
		return "Ambulance centre 45";
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		sendSubscribe(time, 1);
		if (frontier == null) {
			frontier = new HashSet<EntityID>();
			for (StandardEntity entity : model
					.getEntitiesOfType(StandardEntityURN.BUILDING)) {
				frontier.add(entity.getID());
			}
		}
		List<EntityID> agents = new ArrayList<EntityID>();
		for (Command next : heard) {
			try {
				byte[] content = ((AKSpeak) next).getContent();
				String txt = new String(content, "UTF-8");
				Logger.error("Heard " + next + txt);

				String[] parts = txt.split(" ");
				switch (parts[0]) {
				case "reached": {
					Logger.error("Agent has reached a building");
					int buildingId = Integer.parseInt(parts[1]);
					boolean foundSomeone = Boolean.parseBoolean(parts[2]);
					if (foundSomeone) {
						civiliansLocations.add(buildingId);
						Logger.error("Found " + civiliansLocations.size()
								+ " buildings with civilians so far!");
					}
					agentsWithGoal.remove((Integer) next.getAgentID()
							.getValue());
				}
				case "position": {
					agents.add(next.getAgentID());
					break;
				}
				default:
					throw new RuntimeException("Unknown: " + txt);
				}

			} catch (UnsupportedEncodingException ex) {
				Logger.error(ex.getMessage());
			}
		}

		// Coordination
		Rectangle2D bounds = model.getBounds();

		Map<EntityID, Map<EntityID, Double>> frontierEstimation = new HashMap<EntityID, Map<EntityID, Double>>();
		for (EntityID agent : agents) {

			AmbulanceTeam agentEntity = (AmbulanceTeam)model.getEntity(agent);
			
			Map<EntityID, Double> costs = new HashMap<EntityID, Double>();
			for (EntityID frontierID : frontier) {
				Building building = (Building) model.getEntity(frontierID);

				int x = building.getX() - agentEntity.getX();
				int y = building.getY() - agentEntity.getY();
				double cost = Math.hypot(x, y) / Math.hypot(bounds.getWidth(), bounds.getHeight());

				costs.put(frontierID, cost);
			}
			frontierEstimation.put(agent, costs);
		}

		Map<EntityID, Double> utility = new HashMap<EntityID, Double>();
		for (EntityID frontierID : frontier) {
			utility.put(frontierID, 1.0);
		}

		Iterator<EntityID> iter = agents.iterator();
		while (iter.hasNext()) {
			EntityID agentId = iter.next();

			double best = Double.NEGATIVE_INFINITY;
			EntityID bestFrontier = null;
			for (Map.Entry<EntityID, Double> entry : frontierEstimation.get(
					agentId).entrySet()) {
				EntityID frontier = entry.getKey();
				double cost = entry.getValue();

				double value = utility.get(frontier) - cost;
				//Logger.error("value: " + value + " utility: " + utility.get(frontier) + " cost: " + cost);
				if (best < value) {
					//Logger.error("HELLO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					best = value;
					bestFrontier = frontier;
				}
			}

			if (bestFrontier == null) {
				throw new RuntimeException("bestFrontier");
			}

			Building bestBuilding = (Building) model.getEntity(bestFrontier);

			for (Map.Entry<EntityID, Double> entry : utility.entrySet()) {
				EntityID frontierId = entry.getKey();
				double value = entry.getValue();

				Building building = (Building) model.getEntity(frontierId);

				if (building == null) {
					throw new RuntimeException("building");
				}
				if (bestBuilding == null) {
					throw new RuntimeException("bestBuilding");
				}

				int x = building.getX() - bestBuilding.getX();
				int y = building.getY() - bestBuilding.getY();
				double distance = Math.hypot(x, y);

				double v = Math.hypot(bounds.getWidth(), bounds.getHeight());
				//Logger.error("hypot: " + v);
				
				double propability = 1 - distance
						/ v;
				//Logger.error("propability: " + propability);
				if(0 > propability || propability > 1) {
					throw new RuntimeException("propability");
				}

				value *= (1 - propability);

				utility.put(frontierId, value);
			}

			String msg = agentId.getValue() + " " + bestFrontier.getValue();
			sendSpeak(time, 2, msg.getBytes());

			frontier.remove(bestFrontier);
			utility.remove(bestFrontier);
			for (Map.Entry<EntityID, Map<EntityID, Double>> entry : frontierEstimation.entrySet()) {
				entry.getValue().remove(bestFrontier);
			}
			iter.remove();
		}

		sendRest(time);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
	}
}