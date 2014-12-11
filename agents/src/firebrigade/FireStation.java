package firebrigade;
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

import firebrigade.prediction.DumbRREFPrediction;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.components.Agent;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;


public class FireStation extends
StandardAgent<rescuecore2.standard.entities.AmbulanceCentre>
{
	Collection<StandardEntity> agentsInit = new ArrayList<StandardEntity>();
	List<FireArea> handledFires = new ArrayList<FireArea>();
	List<FireArea> unHandledFires = new ArrayList<FireArea>();
	List<FireBrigadeAgent> agents = new ArrayList<FireBrigadeAgent>();
	Map<FireArea, EntityID> fireBrigadeForArea = new HashMap<FireArea, EntityID>();
	private List<FireArea> fireAreas;
	FireKnowledgeStore fireKnowledgeStore;
	DumbRREFPrediction dumbRREFPrediction;
	boolean busy;
	FireBrigadeAgent fireBrigadeAgent;
	
	
	protected void initialize() {
		agentsInit = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
		for(StandardEntity agent2 : agentsInit)
		{
			EntityID agentID = agent2.getID();
			FireBrigade agentFB = (FireBrigade) model.getEntity(agentID);
			FireBrigadeAgent agentFBA = new FireBrigadeAgent(agentID, agentFB.getWater() , false);
			agents.add(agentFBA);
		}
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() 
	{
		return EnumSet.of(StandardEntityURN.FIRE_STATION);
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) 
	{
		fireAreas = fireKnowledgeStore.getFireAreas();
		//Communication
		for (Command next : heard) 
		{
			try 
			{
				byte[] content = ((AKSpeak) next).getContent();
				String txt = new String(content, "UTF-8");
				Logger.error("Heard " + next + txt);
				String[] parts = txt.split(" ");
				switch (parts[0]) 
				{
					case "informations":
					{
						int agentID = Integer.parseInt(parts[1]);
						int waterLevel = Integer.parseInt(parts[2]);
						int busyInt = Integer.parseInt(parts[3]);
						for(int k = 0; k < agents.size(); k++)
						{
							FireBrigadeAgent agent = agents.get(k);
							if(agent.getID().getValue() == agentID)
							{
								agent.setWaterAmount(waterLevel);
								if(busyInt == 0)
								{
									busy = false;
								}
								else
								{
									busy = true;
								}
							}
						}
					}
					case "fireseen ": 
					{
						int seenFireID = Integer.parseInt(parts[1]);
						fireKnowledgeStore.foundFire(seenFireID);
					}
					case "extinguishedfire ":
					{
						int extinguishedFireID = Integer.parseInt(parts[1]);
						fireKnowledgeStore.extinguishedFire(extinguishedFireID);
					}
					default:
						throw new RuntimeException("Unknown: " + txt);
				}
			}
					catch (UnsupportedEncodingException ex) 
				{
					Logger.error(ex.getMessage());
				}
		}
		
		Map<FireArea, Map<FireBrigade, Double>> costFireArea = new HashMap<FireArea, Map<FireBrigade, Double>>();
		for(int i = 0; i < fireAreas.size(); i++)
		{
			Map<FireBrigade, Double> costForAgent = new HashMap<FireBrigade, Double>();
			FireArea area = fireAreas.get(i);
			if(handledFires.contains(area))
			{
				int fireBrigadesNeeded = dumbRREFPrediction.getPrediction(area);
				for (FireBrigadeAgent agent : agents) 
				{
					EntityID agent2 = agent.getID();
					double agentCost = 0;
					FireBrigade agentEntity = (FireBrigade) model.getEntity(agent2);
					agentCost = calculateUtility(agentEntity,fireBrigadesNeeded,area);
					costForAgent.put(agentEntity, agentCost);
				}
				costFireArea.put(area, costForAgent);
				double utilityAreaTemp = 0;
				double utilityArea = 999999999;
				EntityID areaAgent = null;
				
				for(int k = 0; k < agents.size(); k++)
				{
					EntityID agentID = new EntityID(k);
					Iterator<FireBrigade> iterator = costForAgent.keySet().iterator();
					while(iterator.hasNext())
					{
						//TODO ADD FOR MORE AGENTS
						utilityAreaTemp = costForAgent.get(agentID);
						if (utilityAreaTemp <= utilityArea)
						{
							utilityArea = utilityAreaTemp;
							areaAgent = agentID;
						}
					}
					if(areaAgent != null)
					{
						fireBrigadeForArea.put(area,areaAgent);
						handledFires.add(area);
						//Send GO Extinguish
						try {
							String msg = "extinguishedfire " + String.valueOf(areaAgent.getValue()) + " " + String.valueOf(fireAreas.get(i));
							Logger.debug("Send my position on channel 3 " + msg);
							sendSpeak(time, 3, msg.getBytes("UTF-8"));
						} catch (java.io.UnsupportedEncodingException uee) 
						{
							Logger.error(uee.getMessage());
						}
					}
				}
			}
		}
	}
	
	private double calculateUtility(FireBrigade agentEntity,int fireBrigadesNeeded,FireArea area) 
	{
		double tempCost = 0;
		double cost = 0;
		double fieryness = 0;
		double fierynessTemp = 0;
		double busy = 0;
		double distance = 0;
		double tempDistance;
		List<Integer> buildings = new ArrayList<Integer>();
		Rectangle2D bounds = model.getBounds();
		buildings = area.getBuildingsInArea();
		for(int j = 0; j < buildings.size(); j++)
		{
			int buildingID = buildings.get(j);
			EntityID buildingEntity = new EntityID(buildingID);
			Building buildingEntityB = (Building) model.getEntity(buildingEntity);
			int x = buildingEntityB.getX() - agentEntity.getX();
			int y = buildingEntityB.getY() - agentEntity.getY();
			tempDistance = Math.hypot(x, y) / Math.hypot(bounds.getWidth(), bounds.getHeight());
			distance =+ tempDistance;
			
			fierynessTemp = buildingEntityB.getFieryness();
			fieryness =+ fierynessTemp;	
		}
		double waterLevel = agentEntity.getWater();

		if(fireBrigadeForArea.containsValue(agentEntity));
		{
			busy =+100;
		}
		
		cost = fieryness / (fieryness +(distance + waterLevel + busy));
		return cost;
	}
}
