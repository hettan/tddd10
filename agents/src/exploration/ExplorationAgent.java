package exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import communication.CommunicationDevice;
import communication.CommunicationFactory;
import communication.CommunicationGroup;
import communication.CommunicationType;
import communication.Message;

import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.AbstractSampleAgent;
import rescuecore2.standard.entities.Area;


public class ExplorationAgent<E extends StandardEntity> extends AbstractSampleAgent<E> {
	
	private List<EntityID> assignedEntities;
	private List<EntityID> unexploredEntities;
	private Queue<EntityID> explorationCircuit;
	private HashMap<EntityID, Float> utility;
	private HashMap<EntityID, Float> costs;
	private CommunicationDevice communication;
	private float reduceUtilDist = 20000;
	private List<EntityID> currDst;
	
	public ExplorationAgent() {
		assignedEntities = new ArrayList<EntityID>();
		explorationCircuit = new LinkedList<EntityID>();
		utility = new HashMap<EntityID, Float>();
		communication = CommunicationFactory.createCommunicationDevice();
		communication.register(CommunicationGroup.ALL);
		
		currDst = new ArrayList<EntityID>();
	}

	@Override
    protected void postConnect() {
            super.postConnect();
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return null;
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {	
		List<Message> assignmentMessages = communication.getMessages(CommunicationType.ASSIGNMENT, time);		
		handleAssignmentMessages(assignmentMessages);
		
		if(atDestination()) {
			
			//Re-add to the circuit
			
			if(unexploredEntities.size() > 0) {
				EntityID done = currDst.get(0);
				explorationCircuit.add(done);
				unexploredEntities.remove(done);	
				currDst.clear();
			}
			else {
				System.out.println("in circuit mode");
			}
			
		}
		
		//System.out.println("UnexploredEntities = " + unexploredEntities.size());
		
	}
	
	private void handleAssignmentMessages(List<Message> messages) {
		for(Message msg : messages) {
			if(msg.dest.contains(getID())) {
				System.out.println(msg.data);	
				assignedEntities = parseAssignmentData(msg.data);		
			}	
		}
		unexploredEntities = assignedEntities;
		updateCosts();
		for(EntityID entity : assignedEntities) {
			utility.put(entity, 1.0f);
		}
	}
	
	private List<EntityID> parseAssignmentData(String data) {
		List<EntityID> assignedEntities = new ArrayList<EntityID>();
		String[] split_data = data.split("_");
		for(String str_id : split_data) {
			assignedEntities.add(new EntityID(Integer.parseInt(str_id)));
		}
		return assignedEntities;
	}
	
	//Scan the surroundings *NOT DONE*
	private HashMap<ScanResultType,EntityID> scanVisual() {
		HashMap<ScanResultType,EntityID> scanResult = new HashMap<ScanResultType,EntityID>();
		
		return scanResult;
	}
	
	//Should be replaced with something from search algorithm
	private boolean atDestination() {
		if(currDst.size() > 0) {
			int maxDiff = 10;
			Pair<Integer, Integer> dstPos = model.getEntity(currDst.get(0)).getLocation(model);
			Pair<Integer, Integer> myPos = me().getLocation(model);
			
			if(Math.abs(dstPos.first() - myPos.first()) < maxDiff 
					&& Math.abs(dstPos.second() - myPos.second()) < maxDiff) {
				return true;
			}
		}
		return false;
	}
	
	//Return a path to where to explore
	protected List<EntityID> explore() {
		List<EntityID> path = null;
		if(currDst.size() == 0) {
				
			List<EntityID> checked = new ArrayList<EntityID>();
		
			//Make sure the path is reachable otherwise pick a new destination
			while(path == null) {
				currDst.clear();
				currDst.add(getNextExplore(checked));
				checked.add(currDst.get(0));
				path = search.performSearch(((Human)me()).getPosition(), currDst);
			}
			//System.out.println("dst to " +currDst.get(0).getValue());
			//Reduce utility of close entities <(reduceUtilDist)
			if(unexploredEntities.size() > 0) {
				for(EntityID entity : assignedEntities) {
					if ((model.getDistance(currDst.get(0), entity) < reduceUtilDist)) {                                                                                                                                                                        
						updateUtility(entity, currDst.get(0));                                                                                                                                                                                         
					}
				}
			}
		}
		else {
			//System.out.println("Still finding dst " + model.getEntity(currDst.get(0)).getLocation(model).toString() + "   -   " + me().getLocation(model).toString());
			path = search.performSearch(((Human)me()).getPosition(), currDst);
		}
		return path;
	}
	
	//Next entity to explore, will need some kind of time factor for this to work. I don't want to stop exploring an entity after a one time visit.
	private EntityID getNextExplore(List<EntityID> checked) {
		EntityID next = null;
		
		//Poll from the circuit 
        if(unexploredEntities.size() == 0) {
        	next = explorationCircuit.poll();
        	explorationCircuit.add(next);
        	return next;
        }
        //If none of the entities in unexploredEntities can be reached, start walking on the circuit instead.
        if(checked.size() == unexploredEntities.size()) {
         	next = explorationCircuit.poll();
        	explorationCircuit.add(next);
        	return next;
        }
        
        //Circuit not yet done, need to find the next best dst entity
        Float bestTotal = Float.NEGATIVE_INFINITY;
        for(EntityID entity: unexploredEntities) {
        	//Util - cost
        	if (!checked.contains(entity)) {
        		Float total =  utility.get(entity) - costs.get(entity);
        		if (total > bestTotal) {
        			bestTotal = total;
        			next = entity;
        		}
        	}
        }
        return next;
	}
	
	private void updateCosts() {
		costs = new HashMap<EntityID, Float>();                                                                                                                                                    
        for(EntityID entityID : assignedEntities) {                                                                                                                                                                       
            Float cost = model.getDistance(getID(), entityID) / (reduceUtilDist/3);                                                                                                                                
            costs.put(entityID, cost);                                                                                                                                                                                
        }                                                                                                                                                                                                                      
	}
	
    private void updateUtility(EntityID buildingInRange, EntityID startBuilding) {
        Float distance = 0f + model.getDistance(buildingInRange, startBuilding);
        Float util = utility.get(buildingInRange) * (1 - (distance / reduceUtilDist));
        utility.put(buildingInRange, util);
     }

}
