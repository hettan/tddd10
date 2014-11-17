package sample;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.worldmodel.EntityID;

/**
 * An interface that should be implemented by all
 * search algorithms. Additional functionality should be declared
 * in this interface and refactored into the different concrete
 * search algorithms.
 */
public interface SearchAlgorithm {

	/**
	   Set the neighbourhood graph.
	   @param newGraph The new neighbourhood graph.
	 */
	public abstract void setGraph(Map<EntityID, Set<EntityID>> newGraph);

	/**
	   Get the neighbourhood graph.
	   @return The neighbourhood graph.
	 */
	public abstract Map<EntityID, Set<EntityID>> getGraph();

	/**
	   Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
	   @param start The location we start at.
	   @param goals The set of possible goals.
	   @return The path from start to one of the goals, or null if no path can be found.
	 */
	public abstract List<EntityID> performSearch(EntityID start,
			EntityID... goals);

	/**
	   Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
	   @param start The location we start at.
	   @param goals The set of possible goals.
	   @return The path from start to one of the goals, or null if no path can be found.
	 */
	public abstract List<EntityID> performSearch(EntityID start,
			Collection<EntityID> goals);

}