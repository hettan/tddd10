package firebrigade;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.view.AbstractViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;

public class FireAreaViewLayer  extends AbstractViewLayer {

	private StandardWorldModel _model;
	private FireKnowledgeStore _knowledgeStore;
	
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "FireAreaViewLayer";
	}
	
	Color [] _colors = new Color [] {
		Color.BLUE,
		Color.GREEN,
		Color.CYAN,
		Color.MAGENTA,
		Color.ORANGE,
		Color.RED,
		Color.PINK
	};
	Color getColor(int index)
	{
		int i = index % _colors.length;
		return _colors[i];
	}

	@Override
	public Collection<RenderedObject> render(Graphics2D graphics,
			ScreenTransform transform, int arg2, int arg3) {
		if(_knowledgeStore != null)
		{
			int currentFireArea = 0;
			Ellipse2D circle;
			double circleSize = 10.0;
			for(FireArea fireArea : _knowledgeStore.getFireAreas())
			{
				
				for(Integer id : fireArea.getBuildingsInArea())
				{
					StandardEntity entity = _model.getEntity(new EntityID(id));
					if(entity != null)
					{
						Pair<Integer, Integer> location = entity.getLocation(_model);
						circle = new Ellipse2D.Double(
								transform.xToScreen(location.first() - circleSize/2.0), 
								transform.yToScreen(location.second() - circleSize/2.0), 
								circleSize, circleSize);
						graphics.setColor(getColor(currentFireArea));
						graphics.fill(circle);
					}
				}
				currentFireArea++;
			}
		}
		return new ArrayList<RenderedObject>();
	}

	@Override
	public Rectangle2D view(Object... arg0) {
		_model = (StandardWorldModel)arg0[0];
		if(_knowledgeStore == null)
		{
			_knowledgeStore = new FireKnowledgeStoreImpl(_model);
		}
		
		Collection<StandardEntity> buildings = _model.getEntitiesOfType(StandardEntityURN.BUILDING);
		
		for(StandardEntity building : buildings)
		{
			Building b = (Building)building;
			if(b.isOnFire())
			{ 
				if(!_knowledgeStore.contains(b.getID().getValue()))
				{
					_knowledgeStore.foundFire(b.getID().getValue());
				}
			}
		}
		
		return new Rectangle2D.Double(0.0, 0.0, 500.0, 500.0);
	}

	@Override
	protected void viewObject(Object arg0) {
		// TODO Auto-generated method stub
		
	}

}
