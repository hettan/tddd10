package firebrigade;

import java.util.List;

/*
 * An object where all the knowledge about existing fires
 * are stored and transformed into useable knowledge.
 */
public interface FireKnowledgeStore {
	
	/*
	 * Updates the knowledge store with the fact that
	 * the specified building has been found burning
	 * 
	 *  @param buildingID: The EntityID of the building.
	 */
	void foundFire(int buildingID);
	
	/*
	 * Updates the knowledge store with the new fact that
	 * the specified building has been extinguished.
	 * 
	 * @param buildingID: The EntityID of the building.
	 */
	void extinguishedFire(int buildingID);
	
	/*
	 * Updates the knowledge store with the new fact that
	 * the specified building has burnt to the ground.
	 */
	void burntOutFire(int buildingID);
	
	/*
	 * Gets the FireArea in which a building belongs to.
	 * If the building doesn't belong to an area, it returns null.
	 * 
	 * @param buildingID: The EntityID of the belonging building.
	 * @see FireArea
	 */
	FireArea getFireArea(int buildingID);
	
	/*
	 * Gets all burning buildings (which the knowledge store knows of).
	 */
	List<Integer> getBurningBuildings();
	
	/*
	 * Gets all the fire areas. (which the knowledge store knows of).
	 */
	List<FireArea> getFireAreas();
}
