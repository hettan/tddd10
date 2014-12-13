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

import communication.CommunicationDevice;
import communication.CommunicationFactory;
import communication.CommunicationGroup;
import communication.CommunicationType;
import communication.Message;

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
StandardAgent<rescuecore2.standard.entities.FireStation>
{
	private Collection<StandardEntity> agentsInit = new ArrayList<StandardEntity>();
	private List<FireArea> handledFires = new ArrayList<FireArea>();
	private List<FireArea> unHandledFires = new ArrayList<FireArea>();
	private List<FireBrigadeAgent> agents = new ArrayList<FireBrigadeAgent>();
	private Map<FireArea, EntityID> fireBrigadeForArea = new HashMap<FireArea, EntityID>();
	private List<FireArea> fireAreas;
	private FireKnowledgeStore fireKnowledgeStore;
	private DumbRREFPrediction dumbRREFPrediction;
	boolean busy;
	private FireBrigadeAgent fireBrigadeAgent;
	private boolean _isInitialized = false;
	
	private CommunicationDevice communication;
	
	protected void initialize(int time) {
		communication = CommunicationFactory.createCommunicationDevice();
		communication.register(CommunicationGroup.FIRESTATION);
		agentsInit = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
		for(StandardEntity agent2 : agentsInit)
		{
			EntityID agentID = agent2.getID();
			FireBrigade agentFB = (FireBrigade) model.getEntity(agentID);
			FireBrigadeAgent agentFBA = new FireBrigadeAgent(agentID, agentFB.getWater() , false);
			agents.add(agentFBA);
		}
		fireKnowledgeStore = new FireKnowledgeStoreImpl(model);
		dumbRREFPrediction = new DumbRREFPrediction();
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() 
	{
		return EnumSet.of(StandardEntityURN.FIRE_STATION);
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) 
	{
		if(!_isInitialized){
			initialize(time);
			_isInitialized = true;
		}
		
		if (time == config
				.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			sendSubscribe(time, 3);
		}
		
		fireAreas = fireKnowledgeStore.getFireAreas();
		
		//Communication
		for(Message msg : communication.getMessages(CommunicationType.NOTIFICATION, time)) {
			
	//	}
	//	for (Command next : heard) 
	//	{
			//byte[] content = ((AKSpeak) next).getContent();
			String txt = msg.data;
			//Logger.error("Heard " + next + txt);
			String[] parts = txt.split(" ");
			System.out.println("Parts0 = " + parts[0]);
			switch (parts[0]) 
			{
				case "informations":
				{
					int posX = Integer.parseInt(parts[1]);
					int posY = Integer.parseInt(parts[2]);
					int agentID = Integer.parseInt(parts[3]);
					int waterLevel = Integer.parseInt(parts[4]);
					int busyInt = Integer.parseInt(parts[5]);
					System.out.println(parts[0]);
					for(int k = 0; k < agents.size(); k++)
					{
						FireBrigadeAgent agent = agents.get(k);
						if(agent.getID().getValue() == agentID)
						{
							agent.setPosX(posX);
							agent.setPosY(posY);
							agent.setWaterAmount(waterLevel);
							if(busyInt == 0)
							{
								agent.setBusy(false);
							}
							else
							{
								agent.setBusy(true);
							}
						}
					}
					break;
				}
				case "fireseen": 
				{
					System.out.println("In FireSeen");
					int seenFireID = Integer.parseInt(parts[1]);
					((Building)(model.getEntity(new EntityID(seenFireID)))).setTemperature(Integer.parseInt(parts[2]));
					fireKnowledgeStore.foundFire(seenFireID);
					System.out.println(seenFireID);
					break;
				}
				case "extinguishedfire":
				{
					int extinguishedFireID = Integer.parseInt(parts[1]);
					fireKnowledgeStore.extinguishedFire(extinguishedFireID);
					break;
				}
			}
		}
		
		System.out.println("FireAreas" + fireAreas.size());
		Map<FireArea, Map<FireBrigade, Double>> costFireArea = new HashMap<FireArea, Map<FireBrigade, Double>>();
		for(int i = 0; i < fireAreas.size(); i++)
		{
			Map<FireBrigade, Double> costForAgent = new HashMap<FireBrigade, Double>();
			FireArea area = fireAreas.get(i);
			if(!handledFires.contains(area))
			{
				int fireBrigadesNeeded = dumbRREFPrediction.getPrediction(area);
				for (FireBrigadeAgent agent : agents) 
				{
					EntityID agent2 = agent.getID();
					double agentCost = 0;
					FireBrigade agentEntity = (FireBrigade) model.getEntity(agent2);
					agentCost = calculateUtility(agent,fireBrigadesNeeded,area);
					costForAgent.put(agentEntity, agentCost);
					System.out.println("Agent: " + agentCost);
				}
				costFireArea.put(area, costForAgent);
				double utilityAreaTemp = 0;
				double utilityArea = 999999999;
				EntityID areaAgent = null;
				List<EntityID> areaAgents = new ArrayList<EntityID>();
				
				for(int k = 0; k < agents.size(); k++)
				{
					/*EntityID agentID = new EntityID(k);
					Iterator<FireBrigade> iterator = costForAgent.keySet().iterator();
					while(iterator.hasNext())
					{*/
					for(FireBrigade agentID : costForAgent.keySet()) {
						for(int l = 0; l < fireBrigadesNeeded; l++)
						{
							//utilityAreaTemp = costForAgent.get(agentID);
							utilityAreaTemp = costForAgent.get(agentID);
							if (utilityAreaTemp <= utilityArea)
							{
								utilityArea = utilityAreaTemp;
								areaAgent = agentID.getID();
								if(!areaAgents.contains(areaAgent))
								{
									areaAgents.add(areaAgent);
								}
							}
						}
					}
					
					if(areaAgents != null)
					{
						for(int j = 0; j < areaAgents.size(); j++)
						{
							fireBrigadeForArea.put(area,areaAgent);
							handledFires.add(area);
							String msg = "mission " + String.valueOf(areaAgent.getValue()) + " " + String.valueOf(fireAreas.get(i));
							Logger.debug("Send extinguish Fires " + msg);
							//sendSpeak(time, 2, msg.getBytes());
							Message message = new Message();
							message.sender = getID();
							message.destGroup = CommunicationGroup.FIREBRIGADE;
							message.time = time;
							message.type = CommunicationType.REQUEST;
							message.data = msg;
							communication.sendMessage(message);
						}
					}
				}
			}
		}
	}
	
	private double calculateUtility(FireBrigadeAgent agent,int fireBrigadesNeeded,FireArea area) 
	{
		double cost = 0;
		double temperature = 0;
		double temperatureTemp = 0;
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
			int x = buildingEntityB.getX() - agent.getPosX();
			int y = buildingEntityB.getY() - agent.getPosY();
			tempDistance = Math.hypot(x, y) / Math.hypot(bounds.getWidth(), bounds.getHeight());
			distance =+ tempDistance;
			System.out.println(buildingID);
			if(buildingEntityB != null){
				System.out.println(buildingEntityB.getID());
				temperatureTemp = buildingEntityB.getTemperature();
			}
			temperature =+ temperatureTemp;	
		}
		double waterLevel = agent.getWaterAmount();

		if(fireBrigadeForArea.containsValue(agent.getID()));
		{
			busy =+100;
		}
		
		cost = temperature / (temperature +(distance + waterLevel + busy));
		return cost;
	}
}
