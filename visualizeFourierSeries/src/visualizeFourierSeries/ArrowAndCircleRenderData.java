package visualizeFourierSeries;

import java.awt.Point;
import java.awt.Polygon;

public class ArrowAndCircleRenderData {
	
	private int x_pixel_coordinate;
	private int y_pixel_coordinate;
	private Point arrow_end_point;
	private Polygon arrow_polygon;
	private int circle_radius;
	
	public ArrowAndCircleRenderData(int x, int y, Point arrow_end_pnt, Polygon arrow_poly, int circ_radius){
		x_pixel_coordinate = x;
		y_pixel_coordinate = y;
		arrow_end_point = arrow_end_pnt;
		arrow_polygon = arrow_poly;
		circle_radius = circ_radius;
	}
	
	
	public int getX(){
		return x_pixel_coordinate;
	}
	
	public int getY(){
		return y_pixel_coordinate;
	}
	
	public Point getArrowEndPoint(){
		return arrow_end_point;
	}
	
	public Polygon getArrowHeadPolygon(){
		return arrow_polygon;
	}
	
	public int getCircleRadius(){
		return circle_radius;
	}

}
