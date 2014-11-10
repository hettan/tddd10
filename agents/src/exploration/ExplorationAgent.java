package exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.AbstractSampleAgent;

public class ExplorationAgent<E extends StandardEntity> extends AbstractSampleAgent<E> {
	
	private List<EntityID> assignedEntities;
	
	public ExplorationAgent() {
		assignedEntities = new ArrayList<EntityID>();
	}

	@Override
    protected void postConnect() {
            super.postConnect();
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return null;
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		sendSubscribe(time, 2);		
		if(assignedEntities.size() == 0) {
			
		}
	}
}
