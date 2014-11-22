package firebrigade;

import java.util.ArrayList;
import java.util.List;

public class FireKnowledgeStoreImpl implements FireKnowledgeStore {

	private List<FireArea> _fireAreas;
	
	public FireKnowledgeStoreImpl()
	{
		_fireAreas = new ArrayList<FireArea>();
	}
	
	@Override
	public void foundFire(int buildingID) {
		FireArea belongsTo = belongsToKnownFireArea(buildingID);
		
		if(belongsTo == null)
		{ 
			//The building doesn't belong to an exisiting FA. Create new
			belongsTo = new FireAreaImpl();
		}
		
		belongsTo.addFire(buildingID);
	}

	@Override
	public void extinguishedFire(int buildingID) {
		FireArea area = getFireArea(buildingID);
		if(area != null)
			area.removeFire(buildingID);
	}

	@Override
	public void burntOutFire(int buildingID) {
		extinguishedFire(buildingID); //Do the same as with extinguished fire now, remove it.
	}

	@Override
	public FireArea getFireArea(int buildingID) {
		for(int i = 0; i < _fireAreas.size(); i++)
		{
			if(_fireAreas.get(i).contains(buildingID))
				return _fireAreas.get(i);
		}
		return null;		
	}

	@Override
	public List<Integer> getBurningBuildings() {
		ArrayList<Integer> buildings = new ArrayList<Integer>();
		for(int i = 0; i < _fireAreas.size(); i++)
			buildings.addAll(_fireAreas.get(i).getBuildingsInArea());
		return buildings;
	}

	@Override
	public List<FireArea> getFireAreas() {
		return _fireAreas;
	}
	
	//Checks if the specified building belongs to an exisiting fire area.
	private FireArea belongsToKnownFireArea(int buildingID)
	{
		return null;
	}

}
