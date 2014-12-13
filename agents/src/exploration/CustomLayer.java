

package exploration;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;
//./start.sh -m ../maps/Kobe2013-all-ambulances/map -c ../maps/Kobe2013-all-ambulances/config/ --disable-traffic-sim --disable-fire-sim --use-static-civilians
public class CustomLayer extends StandardViewLayer {

	private StandardWorldModel model;
	private List<List<EntityID>> clusters;
	private List<EntityID> agentsAssignment;
	List<Color> colors;
	
	public CustomLayer(StandardWorldModel model) {
		this.model = model;

		//Partitioning part = new Partitioning(model);
		//clusters = part.getClustersKMeans();
		//agentsAssignment = part.getAgentAssigment();
		colors = new ArrayList<Color>();
		colors.add(Color.pink);
		colors.add(Color.yellow);
		colors.add(Color.blue);
		colors.add(Color.red);
		colors.add(Color.green);
		colors.add(Color.black);
		colors.add(Color.cyan);
		colors.add(Color.darkGray);
		//colors.add(Color.gray);
		colors.add(Color.lightGray);
		colors.add(Color.magenta);
		colors.add(Color.orange);
		colors.add(Color.white);
	}
	

	@Override
	public String getName() { return "CustomLayer"; } 

	@Override
	public Collection<RenderedObject> render(Graphics2D g,
			ScreenTransform arg1, int arg2, int arg3) { 

		clusters = KMeansPartitioning.clusters;
		agentsAssignment = HungarianAssignment.lastAgentAssignment;
		Collection<RenderedObject> objects = new HashSet<RenderedObject>(); 
		if(clusters != null && agentsAssignment != null) {	
			for(int i=0; i<clusters.size(); i++) {
				g.setColor(colors.get(i % colors.size()));
				List<EntityID> cluster = clusters.get(i);
				for(EntityID eID : cluster) {
					Pair<Integer, Integer> pos = model.getEntity(eID).getLocation(model);
					Ellipse2D.Double dot = new Ellipse2D.Double(
							arg1.xToScreen(pos.first()),
							arg1.yToScreen(pos.second()), 10, 10);
					g.fill(dot);
					objects.add(new RenderedObject(null, dot));
				}
			}
			
			for(int i=0; i<agentsAssignment.size(); i++) {
				g.setColor(colors.get(i % colors.size()));
				Pair<Integer, Integer> pos = model.getEntity(agentsAssignment.get(i)).getLocation(model);
				Ellipse2D.Double dot = new Ellipse2D.Double(
						arg1.xToScreen(pos.first()-15000),
						arg1.yToScreen(pos.second()+15000), 30, 30);
				g.fill(dot);
				objects.add(new RenderedObject(null, dot));
			}
		}
		

		/*for (StandardEntity entity : 
        	model.getEntitiesOfType(StandardEntityURN.POLICE_FORCE, 
        			StandardEntityURN.FIRE_BRIGADE,
					StandardEntityURN.AMBULANCE_TEAM)) {
			g.setColor(Color.red);
			Ellipse2D.Double dot = new Ellipse2D.Double(
					arg1.xToScreen(entity.getLocation(model).first()),
					arg1.yToScreen(entity.getLocation(model).second()), 10, 10);
			g.fill(dot);
			objects.add(new RenderedObject(null, dot));
        }*/
		
		/*
		for (CrossingNode crossing : crossingThread.crossings.values()) {
			Road road = crossing.getRoad();
			g.setColor(Color.red);
			Ellipse2D.Double dot = new Ellipse2D.Double(arg1.xToScreen(road.getX()), arg1.yToScreen(road.getY()), 10, 10);
			g.fill(dot);
			objects.add(new RenderedObject(null, dot)); 
		}
		
		for (CrossingNode crossing : crossingThread.crossings.values()) {
			Road road = crossing.getRoad();
			g.setColor(Color.blue);
			for(ArrayList<Road> path1 : crossing.getAllPaths()) {
				Road prevRoad1 = null;
				for(Road r : path1) {
					if(prevRoad1 != null) {
						g.drawLine(
								arg1.xToScreen(r.getX()),
								arg1.yToScreen(r.getY()),
								arg1.xToScreen(prevRoad1.getX()),
								arg1.yToScreen(prevRoad1.getY()));	
					}
					prevRoad1 = r;
				}
			}
		}
		
		EntityID start = new EntityID(3462);
		Collection<EntityID> goals = new ArrayList<EntityID>();
		goals.add(new EntityID(3984));
		goals.add(new EntityID(9402));
		goals.add(new EntityID(5127));
		
		List<EntityID> path = this.s.search_to_crossings(start, goals);
		Road prevRoad = null;
		if(path != null) {
			g.setColor(Color.red);
			for(EntityID road_id : path) {
				Road r = (Road)model.getEntity(road_id);
				if(prevRoad != null && r != null) {
					g.drawLine(
							arg1.xToScreen(r.getX()),
							arg1.yToScreen(r.getY()),
							arg1.xToScreen(prevRoad.getX()),
							arg1.yToScreen(prevRoad.getY()));	
				}
				prevRoad = r;
			}
		}
		*/
		return objects;
	}
/*
	@Override
	public Collection<RenderedObject> render(Graphics2D arg0,
			ScreenTransform arg1, int arg2, int arg3) {
		return new HashSet<RenderedObject>(); 
		// TODO Auto-generated method stu
	}*/
}