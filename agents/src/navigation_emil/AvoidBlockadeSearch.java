package navigation_emil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import navigation_emil.AStarAlgorithm.PathLenTuple;
import navigation_emil.GridRelaxation.SearchArea;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import sample.SearchAlgorithm;

public class AvoidBlockadeSearch implements SearchAlgorithm {

	StandardWorldModel model;
	
	public AvoidBlockadeSearch(StandardWorldModel world) {
		model = world;
	}
	
	@Override
	public void setGraph(Map<EntityID, Set<EntityID>> newGraph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<EntityID, Set<EntityID>> getGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EntityID> performSearch(EntityID start, EntityID... goals) {
		StandardEntity startEntity = model.getEntity(start);
		StandardEntity goalEntity = model.getEntity(getClosestID(start, goals));
		if(!(startEntity instanceof Area)) return new ArrayList<EntityID>();
		if(!(goalEntity instanceof Area)) return new ArrayList<EntityID>();
		
		PathLenTuple<Area> path = doAreaSearch((Area)startEntity, (Area)goalEntity);
		
		List<EntityID> result = new ArrayList<EntityID>();
		for(Area area : path.getPath()) {
			result.add(area.getID());
		}
		return result;
	}

	@Override
	public List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals) {
		// TODO Auto-generated method stub
		return null;
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
		List<EntityID> result = new ArrayList<EntityID>();
		boolean passedCurrent = false;
		for(int i = 0; i < path.size(); i++) {
			if(path.get(i).getValue() == currentArea.getValue())
				passedCurrent = true;
			if(passedCurrent)
				result.add(path.get(i));
		}
		return result;
	}

}
