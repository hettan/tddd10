package exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.AbstractSampleAgent;

import communication.CommunicationDevice;
import communication.CommunicationFactory;
import communication.CommunicationGroup;
import communication.CommunicationType;
import communication.Message;


public class ExplorationAgent<E extends StandardEntity> extends AbstractSampleAgent<E> {
	
	private List<EntityID> assignedEntities;
	private List<EntityID> unexploredEntities;
	private Queue<EntityID> explorationCircuit;
	private HashMap<EntityID, Float> utility;
	private HashMap<EntityID, Float> costs;
	private CommunicationDevice communication;
	private float reduceUtilDist = 20000;
	//private List<EntityID> currDst;
	private EntityID currDst;
	private EntityID[] checked;
	
	private static boolean first = true;
	private static boolean gotCommunication = true;
	//private List<List<EntityID>> clusters;
	private KMeansPartitioning partitioning;
	private HungarianAssignment assignment;
	
	public ExplorationAgent() {
		assignedEntities = new ArrayList<EntityID>();
		explorationCircuit = new LinkedList<EntityID>();
		utility = new HashMap<EntityID, Float>();
		communication = CommunicationFactory.createCommunicationDevice();
		communication.register(CommunicationGroup.ALL);
		
		//currDst = new ArrayList<EntityID>();
		currDst = null;
		checked = new EntityID[10];
	}

	@Override
    protected void postConnect() {
        super.postConnect();
		if (gotCommunication && first) {
        	first = false;
        	partitioning = new KMeansPartitioning(model);
        	List<Tuple<Integer,Integer>> clusters = partitioning.getClustersKMeans();
        	
        	assignment = new HungarianAssignment(model);
    		List<EntityID> agentsAssignment = assignment.getAgentAssigment(clusters);
    		sendAssignments(agentsAssignment, 0);
    		System.out.println("Exploration init done!");
    		
    		//Garbage collection
    		partitioning = null;
    		assignment = null;
        }
		else if(!gotCommunication) {
			partitioning = new KMeansPartitioning(model);
			partitioning.communication = false;
        	List<Tuple<Integer,Integer>> clusters = partitioning.getClustersKMeans();
        	System.out.println("Done with partitioning");
        	assignment = new HungarianAssignment(model);
    		List<EntityID> agentsAssignment = assignment.getAgentAssigment(clusters);
    		System.out.println("Done with assignment");
    		
    		int myIndex = agentsAssignment.indexOf(getID());
    		assignedEntities = partitioning.clusters.get(myIndex);
    		unexploredEntities = assignedEntities;
    		
    		updateCosts();
    		for(EntityID entity : assignedEntities) {
    			utility.put(entity, 1.0f);
    		}
    		
    		//Garbage collection
    		partitioning = null;
    		assignment = null;
		}
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
			
			//Add to the circuit			
			if(!explorationCircuit.contains(currDst)) {
				explorationCircuit.add(currDst);
				unexploredEntities.remove(currDst);	
				currDst = null;
			}
		}
	}
	
	
	private void sendAssignments(List<EntityID> agentsAssignment, int time) {
		for(int i=0; i<agentsAssignment.size(); i++){
			Message msg = createAssignmentMessage(agentsAssignment.get(i), partitioning.clusters.get(i), time);
			communication.sendMessage(msg);
		}
	}
		
	private Message createAssignmentMessage(EntityID dst, List<EntityID> assignedBuildings, int time) {
		Message msg = new Message();
		msg.sender = getID();
		msg.dest.add(dst);
		msg.time = 0;
		msg.type = CommunicationType.ASSIGNMENT;
		
		String data = "";
		for(EntityID building : assignedBuildings) {
			data += building.getValue()+"_";
		}
		if(data.length() > 0) {
			data = data.substring(0, data.length()-2);
		}
		msg.data = data;
		return msg;
	}
	
	private void handleAssignmentMessages(List<Message> messages) {
		for(Message msg : messages) {
			if(msg.dest.contains(getID())) {
				//System.out.println(msg.data);	
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
		String[] splitData = data.split("_");

		for(String strId : splitData) {
			if(strId.length() > 0) {
				assignedEntities.add(new EntityID(Integer.parseInt(strId)));
			}
			else {
				System.out.println("splitData size="+splitData.length);
				System.out.println("strId="+strId);
			}
		}
		return assignedEntities;
	}
	
	//Should be replaced with something from search algorithm
	private boolean atDestination() {
		//if(currDst.size() > 0 && currDst.get(0) != null) {
		if(currDst != null) {
			//if(((Human)me()).getPosition().getValue() == currDst.get(0).getValue()) {
			if(((Human)me()).getPosition().getValue() == currDst.getValue()) {
				return true;
			}
		}
		return false;
	}
	
	//Return a path to where to explore
	protected List<EntityID> explore() {
		if(assignedEntities.size() == 0) {
			return randomWalk();
		}
		
		int counter = 0;
		List<EntityID> path = null;
		//if(currDst.size() == 0) {
		if(currDst == null) {
			//Make sure the path is reachable otherwise pick a new destination
			while(path == null) {
				//currDst.clear();
				//currDst.add(getNextExplore(checked));
				currDst = getNextExplore();
				checked[counter] = currDst;
				//checked.add(currDst.get(0));
				//checked.add(currDst);
				path = search.performSearch(((Human)me()).getPosition(), currDst);
				counter++;
				if(counter == 10) {
					System.out.println("wtf?");
					System.out.println(unexploredEntities.size());
					System.out.println(explorationCircuit.size());
					return null;
				}
			}
			for(int i=0; i<counter; i++) {
				checked[i] = null;
			}
			//Reduce utility of close entities <(reduceUtilDist)
			if(unexploredEntities.size() > 0) {
				for(EntityID entity : assignedEntities) {
					//if ((model.getDistance(currDst.get(0), entity) < reduceUtilDist)) {                                                                                                                                                                        
					//	updateUtility(entity, currDst.get(0));                         
					if ((model.getDistance(currDst, entity) < reduceUtilDist)) {                                                                                                                                                                        
						updateUtility(entity, currDst);       
					}
				}
			}
		}
		else {
			path = search.performSearch(((Human)me()).getPosition(), currDst);
			if (path == null) {
				if(unexploredEntities.contains(currDst)) {
					unexploredEntities.add(currDst);
				}
				currDst = null;
				
				System.out.println("hmmm");
				if(unexploredEntities.size() == 0)
					System.out.println("unexplored entities=0");
				return explore();		
			}
		}
		return path;
	}
	
	//Next entity to explore, will need some kind of time factor for this to work. I don't want to stop exploring an entity after a one time visit.
	private EntityID getNextExplore() {
		EntityID next = null;
		
		//Poll from the circuit 
        if(unexploredEntities.size() == 0) {
    		next = explorationCircuit.poll();
    		explorationCircuit.add(next);
        	return next;
        }
        //If none of the entities in unexploredEntities can be reached, start walking on the circuit instead.
        if(checked.length == unexploredEntities.size()) {
    		next = explorationCircuit.poll();
    		explorationCircuit.add(next);
        	return next;
        }
        
        //Circuit not yet done, need to find the next best dst entity
        Float bestTotal = Float.NEGATIVE_INFINITY;
        for(EntityID entity: unexploredEntities) {
        	//Util - cost
        	if (!inChecked(entity)) {
        		Float total =  utility.get(entity) - costs.get(entity);
        		if (total > bestTotal) {
        			bestTotal = total;
        			next = entity;
        		}
        	}
        }
        return next;
	}
	
	private boolean inChecked(EntityID id) {
		for(EntityID check : checked) {
			if(check != null && check.getValue() == id.getValue()) {
				return true;
			}
		}
		return false;
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
