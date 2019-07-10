package visualizeFourierSeries;

import java.awt.Point;
import java.awt.Polygon;

public class arrowAndCircleRenderData {
	
	private int x_pixel_coordinate;
	private int y_pixel_coordinate;
	private Point arrow_end_point;
	private Polygon arrow_head_polygon;
	private Point body_arrow_connection;
	private float arrow_body_stroke;
	private int circle_radius;
	
	public arrowAndCircleRenderData(int x, int y, Point arrow_end_pnt, Polygon arrow_head_poly, Point body_arrow_con, float arrow_body_str, int circ_radius){
		x_pixel_coordinate = x;
		y_pixel_coordinate = y;
		arrow_end_point = arrow_end_pnt;
		arrow_head_polygon = arrow_head_poly;
		body_arrow_connection = body_arrow_con;
		arrow_body_stroke = arrow_body_str;
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
		return arrow_head_polygon;
	}
	
	public Point getBodyArrowConnection(){
		return body_arrow_connection;
	}
	
	public float getArrowBodyStroke(){
		return arrow_body_stroke;
	}
	
	public int getCircleRadius(){
		return circle_radius;
	}

}
