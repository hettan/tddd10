package firebrigade;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class FireKnowledgeStoreImpl implements FireKnowledgeStore {

	private List<FireArea> _fireAreas;
	private StandardWorldModel world;
	
	public FireKnowledgeStoreImpl(StandardWorldModel world)
	{
		this.world = world;
		_fireAreas = new ArrayList<FireArea>();
	}
	
	@Override
	public void foundFire(int buildingID) {
		FireArea belongsTo = belongsToKnownFireArea(buildingID);
		
		if(belongsTo == null)
		{ 
			//The building doesn't belong to an exisiting FA. Create new
			belongsTo = new FireAreaImpl();
			_fireAreas.add(belongsTo);
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
			int firstNearFireArea = -1;
			int secondNearFireArea = -1;
			int counter=0;
			System.out.println("Bin Hier");
			for(int i = 0; i < _fireAreas.size(); i++)
			{
				ArrayList<Integer> buildings = new ArrayList<Integer>();
				buildings.addAll(_fireAreas.get(i).getBuildingsInArea());
				for(int j = 0; j < buildings.size(); j++)
				{
					int buildingID2 = buildings.get(j);
					System.out.println("BuildingID: "+ buildingID);
					System.out.println("BuildingID2: "+ buildingID2);
					EntityID bID1 = new EntityID(buildingID);
					EntityID bID2 = new EntityID(buildingID2);
					int distanceB = world.getDistance(bID1, bID2);
					System.out.println("Distance: " + distanceB);
					//TODO TEST
					if (distanceB <= 35000)
					{
						counter++;
						if (counter==1)
						{
							System.out.println("Counter 1");
							firstNearFireArea = i;
						}
						if (counter>=2 && i != firstNearFireArea && firstNearFireArea != -1)
						{
							System.out.println("Counter 2");
							secondNearFireArea = i;
						}
					}		
				}
			}
			
			
			if (firstNearFireArea != -1)
			{
				return _fireAreas.get(firstNearFireArea);
			}
			
			if (secondNearFireArea != -1)
			{	
				ArrayList<Integer> buildingsFirstFireArea = new ArrayList<Integer>();
				buildingsFirstFireArea.addAll(_fireAreas.get(secondNearFireArea).getBuildingsInArea());
				ArrayList<Integer> buildingsSecondFireArea = new ArrayList<Integer>();
				buildingsSecondFireArea.addAll(_fireAreas.get(secondNearFireArea).getBuildingsInArea());
				
				
				buildingsFirstFireArea.addAll(buildingsSecondFireArea);
				FireArea mergedArea = new FireAreaImpl();
				for(int k = 0; k < buildingsFirstFireArea.size(); k++)
				{
					int buildingIDM = buildingsFirstFireArea.get(k);
					mergedArea.addFire(buildingIDM);
				}
				_fireAreas.remove(firstNearFireArea);
				_fireAreas.remove(secondNearFireArea);
				return mergedArea;
			}
			return null;
		}

	
	@Override
	public boolean contains(int buildingID)
	{
		for(FireArea area : getFireAreas())
			if(area.contains(buildingID))
				return true;
		return false;
	}

}
