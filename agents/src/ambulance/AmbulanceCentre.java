package ambulance;

import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import commlib.components.AbstractCSAgent;
import commlib.task.at.RescueAreaTaskMessage;

import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;

public class AmbulanceCentre extends
AbstractCSAgent<rescuecore2.standard.entities.AmbulanceCentre> {
	//private List<Integer> agentsWithGoal = new ArrayList<Integer>();
	private List<Integer> civiliansLocations = new ArrayList<Integer>();
	private List<AmbulanceTeamAgent> AvailableAmbulanceAgent = new ArrayList<AmbulanceTeamAgent>();
	private List<Civilian> BuriedCivilians = new ArrayList<Civilian>();
	private Set<EntityID> frontier = null;

	protected AtTargetSelection Target;
	
	@Override
	public String toString() {
		return "Ambulance centre";
	}
	
	@Override
	protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
			// Subscribe to channel 1
			sendSubscribe(time, 2);
		System.out.println("Ambulance Center");
		if (frontier == null) {
			frontier = new HashSet<EntityID>();
			for (StandardEntity entity : model
					.getEntitiesOfType(StandardEntityURN.BUILDING)) {
				frontier.add(entity.getID());
			}
		}
		List<EntityID> agents = new ArrayList<EntityID>();
		//TO DO: receive information of agents with no task
		//AvailableAmbulanceAgent = getAvailableagent();
		
		//TO DO: receive information of buriedcivilians
		//BuriedCivilians = getburiedCivilians();		
		
		if(BuriedCivilians != null) {
			//Civilian bestCivilians = Target.rescueSelection(BuriedCivilians,AvailableAmbulanceAgent);
		}
		else
		{
			//TO DO explore
		}
		sendRest(time);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
	}
}