package navigation_emil;

import java.awt.Shape;
import java.awt.geom.PathIterator;

/**
 * An extension to the original Math library with some useful functions
 * 
 * @author emiol791
 *
 */
public class MathUtils {
	
	/**
	 * Given the length of the three sides of a triangle calculate its area
	 * 
	 * @param sideA
	 * 		Length of side A
	 * 
	 * @param sideB
	 * 		Length of side B
	 * 
	 * @param sideC
	 * 		Length of side C
	 * 
	 * @return
	 * 		The area of the triangle
	 */
	public static double areaOfTriangle(double sideA, double sideB, double sideC) {
		double s = (sideA + sideB + sideC) / 2.0;
		return Math.sqrt(s*(s-sideA)*(s-sideB)*(s-sideC));
	}
	
	/**
	 * Calculate the 2-norm (Euclidian distance) between two points
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 * 		The euclidian distance
	 */
	public static double distance(double x1, double y1, double x2, double y2) {
		double dx = x2-x1;
		double dy = y2-y1;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	/**
	 * Calculate the area of a java.awt.Shape by subdividing its boundaries into triangles
	 * 
	 * @param shape
	 * @return
	 * 		The area of shape
	 */
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
