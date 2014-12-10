package navigation_linus;

import java.util.ArrayList;
import rescuecore2.standard.entities.Area;

/**
 * Includes a path between start and dest
 * @author linus
 *
 */
public class Path {

	public Area start;
	public Area dest;
	public int length;
	public int heuristic;
	public ArrayList<Area> path;
	
	Path(Area startIn, Area destIn) {
		length = 0;
		start = startIn;
		dest = destIn;
		path = new ArrayList<Area>();
		path.add(start);
		heuristic = 0;
	}
}
