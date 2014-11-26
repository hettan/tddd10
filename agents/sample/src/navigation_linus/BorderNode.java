package navigation_linus;

import java.util.ArrayList;

import rescuecore2.standard.entities.Road;

public class BorderNode {

	public ArrayList<Path> neighbors;
	public int cluster;
	public Road road;
	
	BorderNode(int clusterIn, Road r) {
		neighbors = new ArrayList<Path>();
		cluster = clusterIn;
		road = r;
	}
}
