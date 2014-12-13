package ambulance;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.worldmodel.EntityID;

public class AtTargetSelection {
	
	//get the minimum number of AT to save the human
	protected int getMinATToRescue(EntityID humanID){
		
		return 0;
	}
	
	//Return the cost it take to rescue the human
//	public  Pair<List<Integer>, List<EntityID>>  rescueSelection(List<Integer> BuriedCivilians,List<EntityID> AvailableAmbulanceAgent){
//		double rescuingValue=100;
//		List<Civilian> civilianToSave = new ArrayList<Civilian>();
//		List<EntityID> ambulanceToSend = new ArrayList<EntityID>();
//		
//		for(EntityID next : AvailableAmbulanceAgent)
//		{
//			for(Integer next2 : BuriedCivilians)
//			{
//				//double victimLifeTime = Math.ceil(next2 / next2.getDamage());
//				System.out.println("victimLifeTime = "+ victimLifeTime);
//				//double victimTime = timeOnPathToVictim(next,next2);
//				//double loadTime = timeToLoadVictim();
//				//double refugeTime = timeOnPathToRefuge();
//				//double unloadTime = timeToUnloadVictim();
//				//double value = victimLifeTime - (victimTime+loadTime+refugeTime+unloadTime);
//				//if(value<rescuingValue && value >0) {
//					//rescuingValue = value;
//					civilianToSave.add(next2);
//					ambulanceToSend.add(next);
//				//}
//			}
//		}
//		Pair<List<Civilian>, List<EntityID>> resultPair = new Pair<List<Civilian>, List<EntityID>>(civilianToSave, ambulanceToSend);
//		return resultPair;
//	}
	
	public int rescueSelectionCivilian(List<Integer> Listbuilding,List<Integer> Listburiedness,List<Integer> 
	Listdamage,List<Integer> Listhp, List<Integer> ListX, List<Integer> ListY ,List<EntityID> agentsAvailable) {
		for(EntityID next : agentsAvailable)
		{
			
		}
		return 1;
	}
	
	
	public  Pair<List<EntityID>, List<EntityID>>  rescueAgentSelection(List<EntityID> BuriedCivilians,List<EntityID> AvailableAmbulanceAgent){
		double rescuingValue=100;
		List<EntityID> civilianToSave = new ArrayList<EntityID>();
		List<EntityID> ambulanceToSend = new ArrayList<EntityID>();
		
		for(EntityID next : AvailableAmbulanceAgent)
		{
			for(EntityID next2 : BuriedCivilians)
			{
				//BuriedCivilians.
			}
		}
		Pair<List<EntityID>, List<EntityID>> resultPair = new Pair<List<EntityID>, List<EntityID>>(civilianToSave, ambulanceToSend);
		return resultPair;
	}
	
	public int timeOnPathToVictim(Civilian next, AmbulanceTeamAgent next2) {
		int timeVictim=0;
		
		return timeVictim;
	}


//	public int timeOnPathToRefuge(AmbulanceTeamAgent next2) {
//		
//	}
//	
//	public int timeToLoadVictim(EntityID humanID) {
//		
//	}
//	
//	public int timeToUnloadVictim(EntityID humanID) {
//		
//	}
	
}


	
	
