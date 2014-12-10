package navigation_emil;

import java.awt.Shape;
import java.awt.geom.PathIterator;

public class MathUtils {
	
	public static double areaOfTriangle(double sideA, double sideB, double sideC) {
		double s = (sideA + sideB + sideC) / 2.0;
		return Math.sqrt(s*(s-sideA)*(s-sideB)*(s-sideC));
	}
	
	public static double distance(double x1, double y1, double x2, double y2) {
		double dx = x2-x1;
		double dy = y2-y1;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static double areaOfShape(Shape shape) {
		double cX = 0, x1 = 0, x2 = 0;
		double cY = 0, y1 = 0, y2 = 0;
		double aSum = 0;
		boolean isTriangle = false;
		double[] coords = new double[6];
		for(PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
			int coordType = iterator.currentSegment(coords);
			switch(coordType) {
			case PathIterator.SEG_CLOSE:
				isTriangle = false;
				// we're done
				break;
			case PathIterator.SEG_LINETO:
				if(isTriangle) {
					x2 = x1;
					y2 = y1;
					x1 = coords[0];
					y1 = coords[1];
					double a = distance(cX, cY, x1, y1);
					double b = distance(cX, cY, x2, y2);
					double c = distance(x1, y1, x2, y2);
					aSum += areaOfTriangle(a, b, c);
					
				} else {
					x1 = coords[0];
					y1 = coords[1];
					isTriangle = true;
				}
				break;
			case PathIterator.SEG_MOVETO:
				cX = coords[0];
				cY = coords[1];
				break;
			}
		}
		return aSum;
	}
}
