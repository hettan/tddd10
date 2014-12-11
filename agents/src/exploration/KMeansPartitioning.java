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

public class KMeansPartitioning {
	private StandardWorldModel model;
	private List<Tuple<Integer,Integer>> clusterCenters;
	public static List<List<EntityID>> clusters;
	public boolean communication = true;
	
	public KMeansPartitioning(StandardWorldModel model) {
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

	private int getNumClusters() {
		return  this.model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE,
	       		StandardEntityURN.POLICE_FORCE, 
	       		StandardEntityURN.AMBULANCE_TEAM).size();
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
	
	public List<Tuple<Integer,Integer>> getClustersKMeans() {
		int numClusters = getNumClusters();
		List<EntityID> entities = getBuildingEntities();
		
		if(communication) {
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
		}
		else {
			System.out.println("No communication using constantClusters");
			clusterCenters = getConstantClusterCenters();
		}
		
		int maxItr = 10;
		int itrCounter = 0;
		List<List<EntityID>> newClusters = clusterEntities(entities, clusterCenters);		
		//printClusters(newClusters, clusterCenters);
		do {
			//System.out.println("New clustering! #"+itrCounter +"size="+newClusters.size()+"x"+newClusters.get(0).size());
			clusters = newClusters;
			clusterCenters = recalculateClusterCenters(clusters, clusterCenters);			
			newClusters = clusterEntities(entities, clusterCenters);
			//printClusters(newClusters, clusterCenters);
		}
		while(!(sameAssignment(clusters, newClusters)) && ++itrCounter < maxItr);
		return clusterCenters;
	}
	
	private List<Tuple<Integer,Integer>> recalculateClusterCenters(List<List<EntityID>> clusters, List<Tuple<Integer,Integer>> oldClusterCenters) {
		List<Tuple<Integer,Integer>> clusterCenters = new ArrayList<Tuple<Integer,Integer>>();

		for(int i=0; i<clusters.size(); i++) {
			List<EntityID> cluster = clusters.get(i);
			if (cluster.size() == 0) {
				clusterCenters.add(oldClusterCenters.get(i));
				continue;
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
	
	public List<Tuple<Integer, Integer>> getConstantClusterCenters() {
		//Get most top,bot,left,right location of buildings
		int top = Integer.MIN_VALUE;
		int bot = Integer.MAX_VALUE;
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;
		
		for(EntityID eID: getBuildingEntities()) {
			Pair<Integer, Integer> pos = model.getEntity(eID).getLocation(model);
			if(pos.first() < left) {
				left = pos.first();
			}
			else if(pos.first() > right) {
				right = pos.first();
			}
			if(pos.second() < bot) {
				bot = pos.second();
			}
			else if(pos.second() > top) {
				
			}	top = pos.second();
		}

		int clusterCount = getNumClusters();
		List<Tuple<Integer, Integer>> centers = new ArrayList<Tuple<Integer, Integer>>();
		Tuple<Integer, Integer> circleCenter = new Tuple<Integer, Integer>();
		circleCenter.first =213;
		circleCenter.first = Math.abs(right-left)/2; //x
		circleCenter.second = Math.abs(top-bot)/2; //y
		centers.add(circleCenter);

		Tuple<Integer, Integer> prevPos = new Tuple<Integer, Integer>();
		prevPos.first = right-circleCenter.first; //x=1 * "factor"
		prevPos.second = circleCenter.second; //y=0
		for(int i=1; i<=(clusterCount-1); i++) {
		    int x = (int) (Math.cos( (2 * i * 2 * Math.PI) / (clusterCount-1)) * (Math.abs(right-left) / 2)); 
		    int y = (int) (Math.sin( (2 * i * 2 * Math.PI) / (clusterCount-1)) * (Math.abs(top-bot) / 2));
		    Tuple<Integer, Integer> circleSliceCenter = new Tuple<Integer, Integer>();
		    circleSliceCenter.first = (circleCenter.first + prevPos.first + x) / 3;
		    circleSliceCenter.second = (circleCenter.second + prevPos.second + y) / 3;
		    centers.add(circleSliceCenter);
		}
		return centers;
	}
	
	private int eclideanDistance(int ax, int ay, int bx, int by){
		int a = ax - bx;
		int b = ay - by;
		int c = (int)Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return c;
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

}
