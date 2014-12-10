package navigation_emil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.view.AbstractViewLayer;
import rescuecore2.view.RenderedObject;

public class GBSViewLayer extends AbstractViewLayer {

	List<Area> gates;
	Rectangle worldSize;
	int gridSize;
	
	
	public GBSViewLayer(GridBasedSearch gbSearch, Rectangle worldSize) {
		super();
		gridSize = gbSearch.getGridSize();
		gates = gbSearch.getGates();
		this.worldSize = worldSize;
		System.out.println("World: " + worldSize.width + ":" + worldSize.height);
	}

	@Override
	public String getName() {
		return "GBSViewLayer";
	}

	@Override
	public Collection<RenderedObject> render(Graphics2D g,
			ScreenTransform t, int w, int h) {
		Collection<RenderedObject> result = new ArrayList<RenderedObject>();
		
		int x1 = t.xToScreen(worldSize.x);
		int x2 = t.xToScreen(worldSize.x + worldSize.width);
		int y1 = t.yToScreen(worldSize.y);
		int y2 = t.yToScreen(worldSize.y + worldSize.height);
		
		g.setColor(Color.red);
		for(int n = 0; n <= gridSize; n++) {
			int xp = t.xToScreen(worldSize.x + n * worldSize.width / gridSize);
			int yp = t.yToScreen(worldSize.y + n * worldSize.height / gridSize);
			g.drawLine(xp, y1, xp, y2);
			g.drawLine(x1, yp, x2, yp);
		}
		
		g.setColor(Color.cyan);
		Rectangle gateRect;
		for(Area gate : gates) {
			gateRect = new Rectangle(t.xToScreen(gate.getX()),t.yToScreen(gate.getY()),5,5);
			g.fill(gateRect);
			result.add(new RenderedObject("Gate (" + gate.getID().getValue() + ")", gateRect));
		}
		
		return result;
	}

	@Override
	public Rectangle2D view(Object... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void viewObject(Object arg0) {
		// TODO Auto-generated method stub
		
	}

}
