package navigation_emil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.view.AbstractViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;

public class GBSViewLayer extends AbstractViewLayer {

	List<Area> gates;
	Rectangle worldSize;
	int gridSize;
	List<EntityID> gbPath = null;
	List<EntityID> sPath = null;
	StandardWorldModel model;
	
	
	public GBSViewLayer(GridBasedSearch gbSearch, Rectangle worldSize, StandardWorldModel model) {
		super();
		this.model = model;
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
		
		if(gbPath != null) {
			drawPath(g, t, w, h, -2, gbPath, Color.GREEN);
		}
		
		if(sPath != null) {
			drawPath(g, t, w, h, 2, sPath, Color.ORANGE);
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
	
	public void SetPaths(List<EntityID> gb, List<EntityID> s) {
		gbPath = gb;
		sPath = s;
	}
	
	private void drawPath(Graphics2D g, ScreenTransform t, int w, int h, int offset,
			List<EntityID> path, Color color) {
		
		int x, y, oldX=0, oldY=0;
		boolean isFirst = true;
		DecimalFormat formatter = new DecimalFormat("#0.000");
		StandardEntity entity;
		Area area;
		g.setColor(color);
		for(EntityID id : path) {
			entity = model.getEntity(id);
			if(entity instanceof Area) {
				area = (Area) entity;
				x = t.xToScreen(area.getX()) + offset;
				y = t.yToScreen(area.getY()) + offset;
				if(!isFirst) {
					g.drawLine(oldX, oldY, x, y);
				}
				isFirst = false;
				oldX = x;
				oldY = y;
				double blockadeArea = 0;
				double[] coords = new double[6];
				if(id.getValue() == path.get(path.size() - 1).getValue()) {
					if(area.getBlockades() != null) {
						for(EntityID bId : area.getBlockades()) {
							Shape bShape = ((Blockade) model.getEntity(bId)).getShape();
							double bArea = MathUtils.areaOfShape(bShape);
							blockadeArea += bArea;
							System.out.println("Area of blockade " + bId.getValue() + " is " + formatter.format(bArea));
							PathIterator bPI = bShape.getPathIterator(null);
							int sX=0, sY=0;
							while(!bPI.isDone()) {
								x = bPI.currentSegment(coords);
								switch(x) {
								case PathIterator.SEG_CLOSE:
									//System.out.println("SEG_CLOSE");
									g.drawLine(oldX, oldY, sX, sY);
									break;
								case PathIterator.SEG_CUBICTO:
									//System.out.println("SEG_CUBICTO");
									break;
								case PathIterator.SEG_LINETO:
									//System.out.println("SEG_LINETO");
									x = t.xToScreen(coords[0]);
									y = t.yToScreen(coords[1]);
									g.drawLine(oldX, oldY, x, y);
									oldX = x;
									oldY = y;
									break;
								case PathIterator.SEG_MOVETO:
									//System.out.println("SEG_MOVETO");
									oldX = sX = t.xToScreen(coords[0]);
									oldY = sY = t.yToScreen(coords[1]);
									break;
								case PathIterator.SEG_QUADTO:
									//System.out.println("SEG_QUADTO");
									break;
								}
								bPI.next();
							}
						}
						//double aArea = MathUtils.areaOfShape(area.getShape());
						//System.out.println("Area of area " + area.getID().getValue() + " is " + formatter.format(aArea));
						//System.out.println("Area is " + formatter.format(100.0*(blockadeArea/aArea)) + "% blocked.");
					}
				}
			}
		}
	}

}
