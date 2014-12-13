package navigation_emil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import navigation_emil.AStarAlgorithm.PathLenTuple;
import navigation_emil.GridRelaxation.SearchArea;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import sample.SearchAlgorithm;

/**
 * A concrete SearchAlgorithm that performs A* search in a StandardWorldModel to find the best path between nodes of type EntityID.
 * Its special feature is that it avoids path that are blocked by Blockades.
 * 
 * @author emiol791
 *
 */
public class AvoidBlockadeSearch implements SearchAlgorithm {

	StandardWorldModel model;
	Map<EntityID, Set<EntityID>> graph;
	
	public AvoidBlockadeSearch(StandardWorldModel world) {
		model = world;
		
		//Create the graph in the same way as sampleSearch
		Map<EntityID, Set<EntityID>> neighbours = new LazyMap<EntityID, Set<EntityID>>() {
			@Override
			public Set<EntityID> createValue() {
				return new HashSet<EntityID>();
			}
		};
		for (Entity next : model) {
			if (next instanceof Area) {
				Collection<EntityID> areaNeighbours = ((Area)next).getNeighbours();
				neighbours.get(next.getID()).addAll(areaNeighbours);
			}
		}
		setGraph(neighbours);
	}
	
	@Override
	public void setGraph(Map<EntityID, Set<EntityID>> newGraph) {
		graph = newGraph;

	}

	@Override
	public Map<EntityID, Set<EntityID>> getGraph() {
		return graph;
	}

	@Override
	public List<EntityID> performSearch(EntityID start, EntityID... goals) {
		StandardEntity startEntity = model.getEntity(start);
		StandardEntity goalEntity = model.getEntity(getClosestID(start, goals));
		if(!(startEntity instanceof Area)) return null;
		if(!(goalEntity instanceof Area)) return null;
		PathLenTuple<Area> path = doAreaSearch((Area)startEntity, (Area)goalEntity);
		
		List<EntityID> result = new ArrayList<EntityID>();
		for(Area area : path.getPath()) {
			result.add(area.getID());
		}
	
		if(result.isEmpty() || result.size() == 1) return null;
		
		return result;
	}

	@Override
	public List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals) {
		EntityID[] arrGoals = new EntityID[goals.size()];
		
		return performSearch(start, goals.toArray(arrGoals));
	}

	@Override
	public List<EntityID> getPriorityNodes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private PathLenTuple<Area> doAreaSearch(Area start, Area goal) {
		SearchArea startArea = new SearchArea(start, model, null);
		startArea.setAvoidBlocks(true);
		SearchArea goalArea = new SearchArea(goal, model, null);
		goalArea.setAvoidBlocks(true);
		
		return AStarAlgorithm.PerformSearch(startArea, goalArea);
	}
	
	private EntityID getClosestID(EntityID start, EntityID[] candidates) {
		EntityID result = null;
		double dist = Double.MAX_VALUE;
		StandardEntity startEntity = model.getEntity(start);
		if(!(startEntity instanceof Area))
			return start;
		Area startArea = (Area) startEntity;
		if(candidates == null || candidates.length == 0)
			return start;
		for(EntityID candidate : candidates) {
			StandardEntity entity = model.getEntity(candidate);
				if(entity instanceof Area) {
					Area candidateArea = (Area) entity;
					double d = MathUtils.distance(startArea.getX(), startArea.getY(),
							candidateArea.getX(), candidateArea.getY());
					if(d < dist) {
						dist = d;
						result = candidate;
					}
				}
			}
		return result;
	}

	@Override
	public List<EntityID> getRemainingPath(List<EntityID> path,
			EntityID currentArea) {
		
		// The standard approach, saves a little time
		/*
		List<EntityID> result = new ArrayList<EntityID>();
		boolean passedCurrent = false;
		for(int i = 0; i < path.size(); i++) {
			if(path.get(i).getValue() == currentArea.getValue())
				passedCurrent = true;
			if(passedCurrent)
				result.add(path.get(i));
		}
		*/
		
		// Calculates a new path, which avoids any blockades that might have been found.
		EntityID goal = path.get(path.size() - 1);
		List<EntityID> result = performSearch(currentArea, goal);
		
		return result;
	}

}
