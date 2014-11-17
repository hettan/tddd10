package sample;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;
import rescuecore2.misc.collections.LazyMap;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Area;

/**
   A sample search class that uses a connection graph to look up neighbours.
 */
public final class SampleSearch implements SearchAlgorithm {
    private Map<EntityID, Set<EntityID>> graph;

    /**
       Construct a new SampleSearch.
       @param world The world model to construct the neighbourhood graph from.
    */
    public SampleSearch(StandardWorldModel world) {
        Map<EntityID, Set<EntityID>> neighbours = new LazyMap<EntityID, Set<EntityID>>() {
            @Override
            public Set<EntityID> createValue() {
                return new HashSet<EntityID>();
            }
        };
        for (Entity next : world) {
            if (next instanceof Area) {
                Collection<EntityID> areaNeighbours = ((Area)next).getNeighbours();
                neighbours.get(next.getID()).addAll(areaNeighbours);
            }
        }
        setGraph(neighbours);
    }


    /**
       Construct a new ConnectionGraphSearch.
       @param graph The connection graph in the form of a map from EntityID to the set of neighbouring EntityIDs.
     */
    public SampleSearch(Map<EntityID, Set<EntityID>> graph) {
        setGraph(graph);
    }

    /* (non-Javadoc)
	 * @see sample.ISearchAlgorithm#setGraph(java.util.Map)
	 */
    @Override
	public void setGraph(Map<EntityID, Set<EntityID>> newGraph) {
        this.graph = newGraph;
    }

    /* (non-Javadoc)
	 * @see sample.ISearchAlgorithm#getGraph()
	 */
    @Override
	public Map<EntityID, Set<EntityID>> getGraph() {
        return graph;
    }

    private boolean isGoal(EntityID e, Collection<EntityID> test) {
        return test.contains(e);
    }

	/*
	 * (non-Javadoc)
	 * @see sample.ISearchAlgorithm#performSearch(rescuecore2.worldmodel.EntityID, rescuecore2.worldmodel.EntityID[])
	 */
	@Override
	public List<EntityID> performSearch(EntityID start, EntityID... goals) {
		return performSearch(start, Arrays.asList(goals));
	}

	/*
	 * (non-Javadoc)
	 * @see sample.ISearchAlgorithm#performSearch(rescuecore2.worldmodel.EntityID, java.util.Collection)
	 */
	@Override
	public List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals) {
		
		List<EntityID> open = new LinkedList<EntityID>();
        Map<EntityID, EntityID> ancestors = new HashMap<EntityID, EntityID>();
        open.add(start);
        EntityID next = null;
        boolean found = false;
        ancestors.put(start, start);
        do {
            next = open.remove(0);
            if (isGoal(next, goals)) {
                found = true;
                break;
            }
            Collection<EntityID> neighbours = graph.get(next);
            if (neighbours.isEmpty()) {
                continue;
            }
            for (EntityID neighbour : neighbours) {
                if (isGoal(neighbour, goals)) {
                    ancestors.put(neighbour, next);
                    next = neighbour;
                    found = true;
                    break;
                }
                else {
                    if (!ancestors.containsKey(neighbour)) {
                        open.add(neighbour);
                        ancestors.put(neighbour, next);
                    }
                }
            }
        } while (!found && !open.isEmpty());
        if (!found) {
            // No path
            return null;
        }
        // Walk back from goal to start
        EntityID current = next;
        List<EntityID> path = new LinkedList<EntityID>();
        do {
            path.add(0, current);
            current = ancestors.get(current);
            if (current == null) {
                throw new RuntimeException("Found a node with no ancestor! Something is broken.");
            }
        } while (current != start);
        return path;
	}
}
