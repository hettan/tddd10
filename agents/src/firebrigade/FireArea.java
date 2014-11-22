package firebrigade;

import java.util.List;

/*
 * A fire area is a collection of connected (lying close by) 
 * buildings which are on fire.
 * 
 * one burning building is also a fire area.
 */
public interface FireArea {
	/*
	 * Adds the specified building to this fire area.
	 * 
	 * @param buildingID: the building to be added
	 */
	void addFire(int buildingID);
	
	/*
	 * Removes the specified building from this area.
	 * 
	 * @param buildingID: the building to be removed.
	 */
	void removeFire(int buildingID);
	
	/*
	 * Gets all the buildings in this area
	 */
	List<Integer> getBuildingsInArea();
}
