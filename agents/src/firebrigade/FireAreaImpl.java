package firebrigade;

import java.util.ArrayList;
import java.util.List;

public class FireAreaImpl implements FireArea {

	private ArrayList<Integer> _buildings;
	
	public FireAreaImpl()
	{
		_buildings = new ArrayList<Integer>();
	}
	
	@Override
	public void addFire(int buildingID) {
		// TODO Auto-generated method stub
		if(!_buildings.contains(buildingID))
			_buildings.add(buildingID);
	}

	@Override
	public void removeFire(int buildingID) {
		// TODO Auto-generated method stub
		if(_buildings.contains(buildingID))
			_buildings.remove((Integer)buildingID);
	}
	
	@Override
	public boolean contains(int buildingID)
	{
		return _buildings.contains(buildingID);
	}
	

	@Override
	public List<Integer> getBuildingsInArea() {
		// TODO Auto-generated method stub
		return _buildings;
	}

}
