package navigation_emil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import navigation_emil.AStarAlgorithm.PathLenTuple;
import navigation_emil.GridRelaxation.Gate;
import navigation_emil.GridRelaxation.GridBox;
import navigation_emil.GridRelaxation.Path;
import navigation_emil.GridRelaxation.SearchArea;
import navigation_emil.GridRelaxation.SearchPath;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import sample.SearchAlgorithm;

public class GridBasedSearch implements SearchAlgorithm {

	private StandardWorldModel model;
	private GridRelaxation worldRelax;
	private final int gridSize = 5;
	
	public List<Area> getGates() {
		return worldRelax.getGateAreas();
	}
	
	public int getGridSize() {
		return gridSize;
	}
	
	public GridBasedSearch(StandardWorldModel world) {
		model = world;
		worldRelax = new GridRelaxation(model, gridSize);
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
		
		PathLenTuple<Area> path = doGridSearch((Area)startEntity, (Area)goalEntity);
		
		List<EntityID> result = new ArrayList<EntityID>();
		for(Area area : path.getPath()) {
			result.add(area.getID());
		}
		return result;
	}
	
	// This one is still kept here to add possibility to quickly find short paths
	@SuppressWarnings("unused")
	private PathLenTuple<Area> doAreaSearch(Area start, Area goal) {
		SearchArea startArea = new SearchArea(start, model, null);
		startArea.setAvoidBlocks(true);
		SearchArea goalArea = new SearchArea(goal, model, null);
		goalArea.setAvoidBlocks(true);
		
		return AStarAlgorithm.PerformSearch(startArea, goalArea);
	}
	
	private PathLenTuple<Area> doGridSearch(Area start, Area goal) {
		
		GridBox limit = worldRelax.getBoxAtWorldCoord(goal.getX(), goal.getY());
		SearchPath goalArea =  SearchPath.CreateGoalPath(new Gate(goal, limit), model, limit.getRectangle());
		
		limit = worldRelax.getBoxAtWorldCoord(start.getX(), start.getY());
		SearchPath startArea =  SearchPath.CreateStartPath(new Gate(start, limit), model, limit.getRectangle(), goalArea);

		PathLenTuple<Path> res = AStarAlgorithm.PerformSearch(startArea, goalArea);
		PathLenTuple<Area> convertedRes = new PathLenTuple<Area>();
		
		double length = res.getLength();
		for(Path p : res.getPath()) {
			if(p.hasPath()) {
				if(convertedRes.getPath().size() > 0 &&
						convertedRes.getPath().get(convertedRes.getPath().size() - 1).getID().getValue() ==
						p.getPath().get(0).getID().getValue()) {
					if(p.getPath().size() > 1) {
						convertedRes.getPath().addAll(p.getPath().subList(1, p.getPath().size()));
					}
				} else {
					convertedRes.getPath().addAll(p.getPath());
				}
			}
		}
		convertedRes.setLength(length);
		return convertedRes;
	}

	@Override
	public List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals) {
		return null;
	}

	@Override
	public List<EntityID> getPriorityNodes() {
		// TODO Auto-generated method stub
		return null;
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
		//TODO: Implement this!
		return path;
	}

}
