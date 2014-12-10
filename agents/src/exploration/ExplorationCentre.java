package exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import communication.CommunicationDevice;
import communication.CommunicationFactory;
import communication.CommunicationType;
import communication.Message;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

public class ExplorationCentre extends StandardAgent<Building>{
	private List<EntityID> agents;
	private static boolean first = true;
	private List<List<EntityID>> clusters;
	private CommunicationDevice communication;
	@Override
    protected void postConnect() {
            super.postConnect();
            model.indexClass(StandardEntityURN.CIVILIAN, 
            		StandardEntityURN.FIRE_BRIGADE,
            		StandardEntityURN.POLICE_FORCE, 
            		StandardEntityURN.AMBULANCE_TEAM, 
            		StandardEntityURN.REFUGE, 
            		StandardEntityURN.HYDRANT,
            		StandardEntityURN.GAS_STATION,
            		StandardEntityURN.BUILDING);
            System.out.println("myID = "+this.getID());
            agents = new ArrayList<EntityID>();

            for (StandardEntity entity : 
            	model.getEntitiesOfType(
            			StandardEntityURN.POLICE_FORCE, 
            			StandardEntityURN.FIRE_BRIGADE,
						StandardEntityURN.AMBULANCE_TEAM)) {
            	agents.add(entity.getID());
            }
            
            communication = CommunicationFactory.createCommunicationDevice();
            
            //Only first center need to assign the agents
            if (first) {
            	first = false;
        		Partitioning part = new Partitioning(model);
        		clusters = part.getClustersKMeans();
        		
        		List<EntityID> agentsAssignment = part.getAgentAssigment();
        		sendAssignments(agentsAssignment, 0);
            }
            //clusters = getClusters(agents, 4);
    }
	
	private void sendAssignments(List<EntityID> agentsAssignment, int time) {
		for(int i=0; i<agentsAssignment.size(); i++){
			Message msg = createAssignmentMessage(agentsAssignment.get(i), clusters.get(i), time);
			communication.sendMessage(msg);
			System.out.println("message sent");
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
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return null;
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		sendSubscribe(time, 1);	
		
	}
	
	
}
