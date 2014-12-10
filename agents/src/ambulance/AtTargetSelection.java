package ambulance;

import java.util.List;

import rescuecore2.standard.entities.Civilian;
import rescuecore2.worldmodel.EntityID;

public class AtTargetSelection {
	
	//get the minimum number of AT to save the human
	protected int getMinATToRescue(EntityID humanID){
		
		return 0;
	}
	
	//Return the cost it take to rescue the human
	public  Civilian rescueSelection(List<Civilian> BuriedCivilians){
		int valuefinal=0;
		Civilian best=null;
		for(Civilian next : BuriedCivilians)
		{
			int value = next.getBuriedness() + next.getDamage() + next.getHP();
			if(value<valuefinal && value >0) {
				value = valuefinal;
				best = next;
			}
		}
		return best;
	}
}
