package navigation_emil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import navigation_emil.GridRelaxation.SearchArea;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import sample.SearchAlgorithm;

public class GridBasedSearch implements SearchAlgorithm{

	private StandardWorldModel model;
	private GridRelaxation worldRelax;
	private final int gridSize = 5;
	
	public List<Area> getGates() {
		return worldRelax.getGates();
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
		StandardEntity goalEntity = model.getEntity(goals[0]);
		if(!(startEntity instanceof Area)) return new ArrayList<EntityID>();
		if(!(goalEntity instanceof Area)) return new ArrayList<EntityID>();
		SearchArea startArea = worldRelax.new SearchArea((Area) startEntity);
		SearchArea goalArea = worldRelax.new SearchArea((Area) goalEntity);
		List<Area> path = AStarAlgorithm.PerformSearch(startArea, goalArea);
		List<EntityID> result = new ArrayList<EntityID>();
		for(Area area : path) {
			result.add(area.getID());
		}
		return result;
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

}
