package navigation_emil;

import java.util.ArrayList;
import java.util.List;

public abstract class Searchable<T> implements Comparable<Searchable<T>> {
	private int id;
	private double distance;
	private double heuristic;
	private T nodeObject;
	private List<T> children;
	private Searchable<T> parentNode;
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getHeuristic() {
		return heuristic;
	}
	public void setHeuristic(double heuristic) {
		this.heuristic = heuristic;
	}
	public List<T> getChildren() {
		return children;
	}
	public void setChildren(List<T> children) {
		this.children = children;
	}
	public void addChild(T child) {
		this.children.add(child);
	}
	public Searchable(T baseObject, int uniqueId, double distance, double heuristic, Searchable<T> parentNode) {
		this.distance = distance;
		this.heuristic = heuristic;
		this.parentNode = parentNode;
		this.id = uniqueId;
		this.nodeObject = baseObject;
		this.children = new ArrayList<T>();
	}
	public int getUniqueId() {
		return id;
	}
	public Searchable<T> getParent() {
		return parentNode;
	}
	public void setParent(Searchable<T> parent) {
		parentNode = parent;
	}
	public T getNodeObject() {
		return nodeObject;
	}
	@Override
	public int compareTo(Searchable<T> o) {
		return (int)(this.distance + this.heuristic - o.distance - o.heuristic);
	}
	public abstract Searchable<T> createNode(T baseObject, Searchable<T> parentObject, Searchable<T> goalObject);
}
