package exploration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rescuecore.objects.World;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

public class Partitioning {
	private StandardWorldModel model;
	private List<Tuple<Integer,Integer>> clusterCenters;
	private List<List<EntityID>> clusters;
	
	public Partitioning(StandardWorldModel model) {
		this.model = model;
		this.model.indexClass(StandardEntityURN.CIVILIAN, 
       		StandardEntityURN.FIRE_BRIGADE,
       		StandardEntityURN.POLICE_FORCE, 
       		StandardEntityURN.AMBULANCE_TEAM, 
       		StandardEntityURN.REFUGE, 
       		StandardEntityURN.HYDRANT,
       		StandardEntityURN.GAS_STATION,
       		StandardEntityURN.BUILDING);		
	}
	
	
	public List<List<EntityID>> getAgentAssigment() {
		//Get all agents that can be assigned
		List<EntityID> agents = new ArrayList<EntityID>();
		for(StandardEntity entity : this.model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE,
	       		StandardEntityURN.POLICE_FORCE, 
	       		StandardEntityURN.AMBULANCE_TEAM)) {
			Human h = (Human)entity;
			
			//Buried agents can't move, ignore them
			if(h.getBuriedness() == 0) {
				agents.add(entity.getID());
			}
		}
		
		//Create the distance-matrix
		List<List<Integer>> distanceMatrix = new ArrayList<List<Integer>>();
		for(int i=0; i<agents.size(); i++) {
			distanceMatrix.add(new ArrayList<Integer>());
		}
		
		System.out.println("Creating matrix");
		//agents x clusterCenters
		for(int i=0; i<agents.size(); i++) {
			System.out.print(String.format("%1$12s","Agent #"+i+" |"));
			Pair<Integer, Integer> agentPos = model.getEntity(agents.get(i)).getLocation(model);
			for(Tuple<Integer, Integer> clusterCenter : clusterCenters) {
				int distance = eclideanDistance(agentPos.first(), agentPos.second(),
												clusterCenter.first, clusterCenter.second);
				
				distanceMatrix.get(i).add(distance);
				System.out.print(String.format("%1$9s", distance+"|"));
			}
			System.out.println();
		}
		
		System.out.println("matrix created!");
		
		return null;
	}
	
	private Map<EntityID, EntityID> hungarianAlgorithm(List<List<Integer>> distanceMatrix) {
		Map<EntityID, EntityID> assignment = new HashMap<EntityID, EntityID>();
		
		for(int i=0; i<distanceMatrix.size(); i++) {
			distanceMatrix.set(i, subtractLowest(distanceMatrix.get(i)));
		}
		
		if(matrixDone(distanceMatrix)) {
			//Create the hashmap
		}
		
		return null;
	}
	
	private List<Integer> subtractLowest(List<Integer> matrixRow) {
		int lowestVal = Integer.MAX_VALUE;
		for(int i=0; i<matrixRow.size(); i++) {
			if(matrixRow.get(i) < lowestVal) {
				lowestVal = matrixRow.get(i);
			}
		}
		for(int i=0; i<matrixRow.size(); i++) {
			matrixRow.set(i, matrixRow.get(i)-lowestVal);
		}
		return matrixRow;
	}
	
	private boolean matrixDone(List<List<Integer>> matrix) {
		//Check rows
		for(List<Integer> row : matrix) {
			int zeroCount = 0;
			for(int i=0; i<row.size(); i++) {
				if (row.get(i) == 0) {
					zeroCount++;
				}
			}
			if (zeroCount != 1) {
				return false;
			}
		}
		
		//Check columns
		for(int i=0; i<matrix.get(0).size(); i++) {
			int zeroCount = 0;
			for(int z=0; z<matrix.size(); z++) {
				if (matrix.get(z).get(i) == 0) {
					zeroCount++;
				}
			}
			if (zeroCount != 1) {
				return false;
			}
		}
		
		return true;
	}
	
	
	private int getNumClusters() {
		int numActiveAgents = 0;
		for(StandardEntity entity : this.model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE,
       		StandardEntityURN.POLICE_FORCE, 
       		StandardEntityURN.AMBULANCE_TEAM)) {
			Human h = (Human)entity;
			if(h.getBuriedness() == 0) {
				numActiveAgents++;
			}
		}
		
		return numActiveAgents;
	}
	
	private List<EntityID> getBuildingEntities() {
		List<EntityID> agents = new ArrayList<EntityID>();
		for (StandardEntity entity : 
	       	this.model.getEntitiesOfType(/*StandardEntityURN.POLICE_FORCE, 
	       			StandardEntityURN.FIRE_BRIGADE,
						StandardEntityURN.AMBULANCE_TEAM*/StandardEntityURN.BUILDING)) {
				agents.add(entity.getID());
			}
		return agents;
	}
	
	public List<List<EntityID>> getClustersKMeans() {
		int numClusters = getNumClusters();
		List<EntityID> entities = getBuildingEntities();
		clusterCenters = new ArrayList<Tuple<Integer,Integer>>();
		
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
		do {
			System.out.println("New clustering! #"+itrCounter);
			clusters = newClusters;
			clusterCenters = recalculateClusterCenters(clusters, clusterCenters);			
			newClusters = clusterEntities(entities, clusterCenters);
			printClusters(newClusters, clusterCenters);
		}
		while(!(sameAssignment(clusters, newClusters)) && ++itrCounter < maxItr);

		System.out.println("Clustering done!");
		getAgentAssigment();
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

}
