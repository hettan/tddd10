package navigation_linus;

import java.util.ArrayList;
import rescuecore2.standard.entities.Area;

/**
 * Represents a bordernode in the abstract map
 * @author linus
 *
 */
public class BorderNode {

	public ArrayList<Path> neighbors;
	public int cluster;
	public Area road;
	
	BorderNode(int clusterIn, Area r) {
		neighbors = new ArrayList<Path>();
		cluster = clusterIn;
		road = r;
	}
}
