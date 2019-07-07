package visualizeFourierSeries;

import java.awt.Point;
import java.awt.Polygon;

public class arrowAndCircleRenderData {
	
	private int x_pixel_coordinate;
	private int y_pixel_coordinate;
	private Polygon arrow_head_polygon;
	private Point body_arrow_connection;
	private int arrow_body_stroke;
	private int circle_radius;
	
	public arrowAndCircleRenderData(int x, int y, Polygon arrow_head_poly, Point body_arrow_con, int arrow_body_str, int circ_radius){
		x_pixel_coordinate = x;
		y_pixel_coordinate = y;
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
	
	public Polygon getArrowHeadPolygon(){
		return arrow_head_polygon;
	}
	
	public Point getBodyArrowConnection(){
		return body_arrow_connection;
	}
	
	public int getArrowBodyStroke(){
		return arrow_body_stroke;
	}
	
	public int getCircleRadius(){
		return circle_radius;
	}

}
