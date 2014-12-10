package navigation_emil;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract class to make any object A* searchable
 * @author emiol791
 *
 * @param <T>
 * 		The class of the searchable object
 */
public abstract class Searchable<T> implements Comparable<Searchable<T>> {
	private int id;
	private double distance;
	private double heuristic;
	private T nodeObject;
	private List<T> children;
	private Searchable<T> parentNode;
	
	/**
	 * 
	 * @return
	 * 		The accumulated distance from start node
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * 
	 * @param distance
	 * 		The distance from the start node
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	/**
	 * 
	 * @return
	 * 		The expected remaining distance to goal node
	 */
	public double getHeuristic() {
		return heuristic;
	}
	
	/**
	 * 
	 * @param heuristic
	 * 		The expected remaining distance to goal node
	 */
	public void setHeuristic(double heuristic) {
		this.heuristic = heuristic;
	}
	
	/**
	 * 
	 * @return
	 * 		A list of all children connected to this node
	 */
	public List<T> getChildren() {
		return children;
	}
	
	/**
	 * 
	 * @param children
	 * 		Set the children nodes of this node
	 */
	public void setChildren(List<T> children) {
		this.children = children;
	}
	
	/**
	 * 
	 * @param child
	 * 		Add a child node to this node
	 */
	public void addChild(T child) {
		this.children.add(child);
	}

	/**
	 * 
	 * @return
	 * 		The unique ID of this node
	 */
	public int getUniqueId() {
		return id;
	}
	
	/**
	 * 
	 * @return
	 * 		The node before this node in path
	 */
	public Searchable<T> getParent() {
		return parentNode;
	}
	
	/**
	 * 
	 * @param parent
	 * 		The node before this node in path
	 */
	public void setParent(Searchable<T> parent) {
		parentNode = parent;
	}
	
	/**
	 * 
	 * @return
	 * 		The base object associated with this node
	 */
	public T getNodeObject() {
		return nodeObject;
	}
	
	@Override
	public int compareTo(Searchable<T> o) {
		return (int)(this.distance + this.heuristic - o.distance - o.heuristic);
	}
	
	/**
	 * The constructor of a Searchable object.
	 * 
	 * @param baseObject
	 * 		The base object associated with this node 
	 * 
	 * @param uniqueId
	 * 		A unique ID of this node must be set
	 * 
	 * @param distance
	 * 		The distance from the start node
	 * 
	 * @param heuristic
	 * 		The expected remaining distance to goal node
	 * 
	 * @param parentNode
	 * 		The node before this node in path. Null if first
	 */
	public Searchable(T baseObject, int uniqueId, double distance, double heuristic, Searchable<T> parentNode) {
		this.distance = distance;
		this.heuristic = heuristic;
		this.parentNode = parentNode;
		this.id = uniqueId;
		this.nodeObject = baseObject;
		this.children = new ArrayList<T>();
	}
	
	/**
	 * An abstract factory method used by the search algorithm to instantiate the children of this node. Remember to add its children.
	 * 
	 * @param baseObject
	 * 		The base object associated with this node 
	 * 
	 * @param parentObject
	 * 		The node before this node in path
	 * 
	 * @param goalObject
	 * 		The goal node in path
	 * 
	 * @return
	 * 		A new Searchable<T> object.
	 */
	public abstract Searchable<T> createNode(T baseObject, Searchable<T> parentObject, Searchable<T> goalObject);
}
