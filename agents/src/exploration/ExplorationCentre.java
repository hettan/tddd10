package exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

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
	public List<List<EntityID>> clusters;

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
            
            for (StandardEntity entity : 
            	model.getEntitiesOfType(StandardEntityURN.POLICE_FORCE, 
            			StandardEntityURN.FIRE_BRIGADE,
						StandardEntityURN.AMBULANCE_TEAM)) {
            	agents.add(entity.getID());
            }
            
            if (first) {
            	first = false;
            	for (EntityID id : agents) {
	            	System.out.println((model.getEntity(id)).getLocation(model));
	            }
            }
            List<List<EntityID>> clusters = getClusters(agents, 4);
    }
	
	private List<List<EntityID>> getClusters(List<EntityID> entities, int numClusters) {
		List<Tuple<Integer,Integer>> clusterCenters = new ArrayList<Tuple<Integer,Integer>>();
		
		//Create random position centers for each cluster
		int maxX = 409164; //Change these constants later
		int minX = 0;
		int maxY = 273881; 
		int minY = 0;
		
		Random rand = new Random();
		for(int i=0; i<numClusters;i++){
			Tuple<Integer, Integer> center = new Tuple<Integer, Integer>();
			center.first = rand.nextInt(maxX) + minX;
			center.second = rand.nextInt(maxY) + minY;
			clusterCenters.add(center);
		}
		
		int maxItr = 10;
		int itrCounter = 0;
		System.out.println("first clustering");
		List<List<EntityID>> newClusters = clusterEntities(entities, clusterCenters);		
		printClusters(newClusters, clusterCenters);
		List<List<EntityID>> clusters;
		do {
			System.out.println("New clustering! #"+itrCounter);
			clusters = newClusters;
			clusterCenters = recalculateClusterCenters(clusters, clusterCenters);			
			newClusters = clusterEntities(entities, clusterCenters);
			printClusters(newClusters, clusterCenters);
		}
		while(!(sameAssignment(clusters, newClusters)) && ++itrCounter < maxItr);

		System.out.println("Clustering done!");
		return newClusters;
	}
	
	private void printClusters(List<List<EntityID>> clusters, List<Tuple<Integer,Integer>> clusterCenters) {
		for(int i=0; i<clusters.size(); i++) {
			System.out.println("Cluster #"+i+" - pos: "+clusterCenters.get(i).first+", "+
								clusterCenters.get(i).second);
			for(EntityID eID : clusters.get(i)) {
				Pair<Integer, Integer> entityPos = model.getEntity(eID).getLocation(model);
				System.out.println("entityID: "+eID.toString()+" - pos: "+
									entityPos.first()+", "+entityPos.second());
			}
			System.out.println("\n");
		}
	}
	
	private List<Tuple<Integer,Integer>> recalculateClusterCenters(List<List<EntityID>> clusters, List<Tuple<Integer,Integer>> oldClusterCenters) {
		List<Tuple<Integer,Integer>> clusterCenters = new ArrayList<Tuple<Integer,Integer>>();

		for(int i=0; i<clusters.size(); i++) {
			List<EntityID> cluster = clusters.get(i);
			if (cluster.size() == 0) {
				clusterCenters.add(oldClusterCenters.get(i));
				break;
			}
			
			int totalX = 0;
			int totalY = 0;	
			for(EntityID eID : cluster) {
				Pair<Integer, Integer> entityPos = model.getEntity(eID).getLocation(model);
				totalX += entityPos.first();
				totalY += entityPos.second();
			}
			
			int meanX = totalX / cluster.size();
			int meanY = totalY / cluster.size();
			clusterCenters.add(new Tuple<Integer, Integer>(meanX, meanY));
		}
		
		return clusterCenters;
	}
	
	//Returns true if the assignment is the same
	private boolean sameAssignment(List<List<EntityID>> clusters1, List<List<EntityID>> clusters2) {
		for(int i=0; i<clusters1.size(); i++) {
			if (!(clusters1.get(i).size() == clusters2.get(i).size()) ||
					!clusters1.get(i).containsAll(clusters2.get(i))) {
				return false;
			}
		}
		return true;
	}

	//Assigning entities to the closest clusterCenter
	private List<List<EntityID>> clusterEntities(List<EntityID> entities,
			List<Tuple<Integer, Integer>> clusterCenters) {
		
		List<List<EntityID>> clusters = new ArrayList<List<EntityID>>();
		for(int i=0; i<clusterCenters.size(); i++){
			clusters.add(new ArrayList<EntityID>());
		}
			
		for(EntityID eID : entities) {
			int minDistance = Integer.MAX_VALUE;
			int minCluster = 0;
			
			Pair<Integer, Integer> entityPos = model.getEntity(eID).getLocation(model);
			for(int i=0; i<clusterCenters.size(); i++) {
				Tuple<Integer, Integer> center = clusterCenters.get(i);
				int distance = eclideanDistance(center.first, center.second,
						entityPos.first().intValue(), entityPos.second().intValue());
				
				if(distance < minDistance) {
					minDistance = distance;
					minCluster = i;
				}
			}
			clusters.get(minCluster).add(eID);
		}
		
		return clusters;
	}
	
	private int eclideanDistance(int ax, int ay, int bx, int by){
		int a = ax - bx;
		int b = ay - by;
		int c = (int)Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return c;
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
