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
	public static List<List<EntityID>> clusters;
	public static List<EntityID> lastAgentAssignment;
	
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
	
	
	public List<EntityID> getAgentAssigment() {
		//Get all agents that can be assigned
		List<EntityID> agents = new ArrayList<EntityID>();
		for(StandardEntity entity : this.model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE,
	       		StandardEntityURN.POLICE_FORCE, 
	       		StandardEntityURN.AMBULANCE_TEAM)) {
			Human h = (Human)entity;
			
			//Buried agents can't move, ignore them
			//if(h.getBuriedness() == 0) {
				agents.add(entity.getID());
			//}
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
		
		List<Integer> agentIndexAssignment = hungarianAlgorithm(distanceMatrix);
		System.out.println("Assignment done!");
		
		List<EntityID> agentAssignment = new ArrayList<EntityID>();
		for(int i=0; i<agentIndexAssignment.size(); i++) {
			agentAssignment.add(null);
		}
		
		for(int i=0; i<agentIndexAssignment.size(); i++){
			System.out.println("Agent: "+agents.get(i)+"  -  " + agentIndexAssignment.get(i));
			agentAssignment.set(agentIndexAssignment.get(i), agents.get(i));		
		}
		
		lastAgentAssignment = agentAssignment;
		return agentAssignment;
	}
	
	private List<Integer> hungarianAlgorithm(List<List<Integer>> distanceMatrix) {
		//List<Tuple> assignment = new ArrayList<Tuple>();
		//Step 1 substract from row
		for(int i=0; i<distanceMatrix.size(); i++) {
			distanceMatrix.set(i, subtractLowest(distanceMatrix.get(i)));
		}
		
		//if(matrixDone(distanceMatrix)) {
			//Create the hashmap
		//}
		
		List<Integer> assignment = getAssignment(distanceMatrix);
		/*for(int i=0; i<assignment.size(); i++){
			System.out.println("Agent: "+i+"  -  " + assignment.get(i));	
		}*/
		System.out.println("Step 1 done!");
		if(checkAssignment(assignment)) {
			System.out.println("Assignment OK after step 1");
			return assignment;
		}
		else {
			//Step 2 substract from column
			distanceMatrix = substractLowestColumn(distanceMatrix);
			assignment = getAssignment(distanceMatrix);
			/*for(int i=0; i<assignment.size(); i++){
				System.out.println("Agent: "+i+"  -  " + assignment.get(i));	
			}*/
			System.out.println("Step 2 done!");
			int counter = 0;
			while(!checkAssignment(assignment)){
				// Step 3
				List<List<Integer>> markedMatrix = getMarkingMatrix(assignment, distanceMatrix);
				//System.out.println("Step 3 done!");
				distanceMatrix = markedMatrixupdate(distanceMatrix, markedMatrix);
				//System.out.println("Step 4 done!");
				assignment = getAssignment(distanceMatrix);
				
				/*for(int i=0; i<assignment.size(); i++){
					System.out.println("Agent: "+i+"  -  " + assignment.get(i));	
				}*/
				counter++;
		
			}		
			System.out.println("Assignment found after "+counter+" times");
			return assignment;
		}
	}
	
	private List<List<Integer>> markedMatrixupdate(List<List<Integer>> distanceMatrix, List<List<Integer>> markedMatrix) {
		int lowestVal = Integer.MAX_VALUE;
		for(int i=0; i<markedMatrix.size(); i++) {
			for(int z=0; z<markedMatrix.get(i).size(); z++) {
				if(markedMatrix.get(i).get(z) == 0 &&
						lowestVal > distanceMatrix.get(i).get(z)) {
					lowestVal = distanceMatrix.get(i).get(z);
				}
			}
		}
		//System.out.println("lowestVal = "+lowestVal);
		for(int i=0; i<markedMatrix.size(); i++) {
			for(int z=0; z<markedMatrix.size(); z++) {
				if(markedMatrix.get(i).get(z) == 0) {
					int val = distanceMatrix.get(i).get(z);
					distanceMatrix.get(i).set(z, val-lowestVal);
				}
				else if(markedMatrix.get(i).get(z) == 2) {
					int val = distanceMatrix.get(i).get(z);
					distanceMatrix.get(i).set(z, val+lowestVal);
				}
			}
		}
		return distanceMatrix;
	}
	
	private List<List<Integer>> getMarkingMatrix(List<Integer> assignment, List<List<Integer>> distanceMatrix) {
		List<Integer> markedRows = new ArrayList<Integer>();
		List<Integer> markedColumns = new ArrayList<Integer>();
		
		for(int i=0; i<assignment.size(); i++) {
			markedRows.add(0);
			markedColumns.add(0);
		}
		
		//System.out.print("#1 - Marked rows: ");
		//Mark rows without an assignment
		for(int i=0; i<assignment.size(); i++) {
			if(assignment.get(i) == -1) {
				markedRows.set(i, 1);
				//System.out.print(i+", ");
			}
		}
		//System.out.println();
		
		//Get the columns containing a marked zero
		List<Integer> newMarkedColumns = new ArrayList<Integer>();
		for(int row=0; row<markedRows.size(); row++) {
			if(markedRows.get(row) == 1) {
				for(int col=0; col<distanceMatrix.get(row).size(); col++) {
					if (distanceMatrix.get(row).get(col) == 0) {
						newMarkedColumns.add(col);
						markedColumns.set(col, 1);
					}
				}
			}
		}

	/*	System.out.print("newMarkedColumns: ");
		for(Integer x : newMarkedColumns)
			System.out.print(x+", ");
		System.out.println();
		
		System.out.print("markedColumns: ");
		for(Integer x : markedColumns)
			System.out.print(x+", ");
		System.out.println();
		
		//Mark the rows with assignment and containing a column in newMarkedColumns
		for(int row=0; row<assignment.size(); row++) {
			if(assignment.get(row) != -1 && newMarkedColumns.contains(row)) {
				markedRows.set(row, 1);
			}
		}*/
		
		//Create a matrix with number of markings on element (min 0, max 2)
		List<List<Integer>> markedMatrix = new ArrayList<List<Integer>>();
		for(int row=0; row<markedRows.size(); row++) {
			List<Integer> matrixRow = new ArrayList<Integer>();
			int rowMarked = markedRows.get(row);
			for(int col=0; col<markedColumns.size(); col++) {
				int colMarked = markedColumns.get(col);
				if(rowMarked == 1) {
					matrixRow.add(colMarked);
				}
				else {
					matrixRow.add(colMarked+1);
				}
			}
			markedMatrix.add(matrixRow);			
		}
		/*System.out.println("MarkedMatrix done!");
		for(int y=0; y<markedMatrix.size(); y++) {
			System.out.print("|");
			for(int x=0; x<markedMatrix.get(y).size(); x++) {
				if(markedMatrix.get(y).get(x) == 0)
					System.out.print(String.format("%1$7s",distanceMatrix.get(y).get(x)+"|"));
				else {
					System.out.print(String.format("%1$7s","  X   |"));
				}		
			}
			System.out.println();
		}*/
		return markedMatrix;
	}
	
	private List<List<Integer>> substractLowestColumn(List<List<Integer>> matrix)
	{
		for(int i=0; i<matrix.size(); i++) {
			int lowestVal = Integer.MAX_VALUE;
			for(int z=0; z<matrix.size(); z++) {
				if(matrix.get(z).get(i) < lowestVal) {
					lowestVal = matrix.get(z).get(i);
				}
			}
			for(int z=0; z<matrix.size(); z++){
				matrix.get(z).set(i, matrix.get(z).get(i) - lowestVal);
			}

		}
		return matrix;
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
	
	private List<Tuple> completeAssignment(List<Integer> columnAssignment, List<EntityID> agents) {
		List<Tuple> assignment = new ArrayList<Tuple>();
		for(int i=0; i<columnAssignment.size(); i++){
			Tuple<EntityID, Integer> agentAssignment = new Tuple<EntityID, Integer>();
			agentAssignment.first = agents.get(i);
			agentAssignment.second = columnAssignment.get(i);
			assignment.add(agentAssignment);
		}
		return assignment;
	}
	
	private boolean checkAssignment(List<Integer> assignment) {
		return !assignment.contains(-1);
	}
	
	private List<Integer> getAssignment(List<List<Integer>> matrix) {
		List<Integer> assignedColumns = new ArrayList<Integer>();
		//System.out.println("rows = " + matrix.size()+", cols = " + matrix.get(0).size());
		
		for(int rowIndex=0; rowIndex<matrix.size(); rowIndex++) {
			boolean foundOpt = false;
			for(int columnIndex=0; columnIndex<matrix.size(); columnIndex++) {
				if(matrix.get(rowIndex).get(columnIndex) == 0 &&
						!assignedColumns.contains(columnIndex)) {
					assignedColumns.add(columnIndex);
					foundOpt = true;
					break;
				}
			}
			//Set assignedColumn to -1 to keep the correct order of rows
			if(!foundOpt) {
				//System.out.println("assignment not found for row "+rowIndex);
				assignedColumns.add(-1);
			}
		}
		//printMatrix(matrix);
		return assignedColumns;
	}
	
	private void printMatrix(List<List<Integer>> matrix) {
		for(int x=0; x<matrix.size(); x++) {
			System.out.print(String.format("%1$5s","#"+x+" |"));
			for(int y=0; y<matrix.get(x).size(); y++) {
				System.out.print(String.format("%1$8s", matrix.get(x).get(y)+"|"));
			}
			System.out.println();
		}
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
			System.out.println(entity.getID());
			//Human h = (Human)entity;
			
			//if(h.getBuriedness() == 0) {
				numActiveAgents++;
			//}
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
		List<List<EntityID>> newClusters = clusterEntities(entities, clusterCenters);		
		System.out.println("first clustering done "+"size="+newClusters.size()+"x"+newClusters.get(0).size());
		//printClusters(newClusters, clusterCenters);
		do {
			System.out.println("New clustering! #"+itrCounter +"size="+newClusters.size()+"x"+newClusters.get(0).size());
			clusters = newClusters;
			clusterCenters = recalculateClusterCenters(clusters, clusterCenters);			
			newClusters = clusterEntities(entities, clusterCenters);
			//printClusters(newClusters, clusterCenters);
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
	
	private int eclideanDistance(int ax, int ay, int bx, int by){
		int a = ax - bx;
		int b = ay - by;
		int c = (int)Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return c;
	}

}
