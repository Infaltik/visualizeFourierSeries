package visualizeFourierSeries;

import java.awt.Point;
import java.util.ArrayList;

public class mathematics {
	
	int nbr_of_fourier_terms = 101;
	
	
	public complexNumber[] image_pixels_to_coordinate_values(ArrayList<Point> image_pixels){
		
		complexNumber[] complex_values_array = new complexNumber[image_pixels.size()];
		
		// Take in the array of image pixels and return a coordinate value for each pixel
		return null; // Temporary
	}
	
	public static complexNumber pixel_to_complex_value(Point pixel){
		int originPixelX = appWindow.rendering_panel_width/2;
		int originPixelY = appWindow.rendering_panel_height/2;
		
		double real_part = ( (double)((int) pixel.getX()) - originPixelX ) / originPixelX;
		double imag_part = -( (double)((int) pixel.getY()) - originPixelY ) / originPixelY;
		
		return new complexNumber(real_part, imag_part);
	}
	
	public static complexNumber complexNumber2x2MatrixMult(double[][] matrix, complexNumber complex_number) {
		// Views the complex number as a 2x1 vector and does matrix*vector
		
		double real_part = matrix[0][0]*complex_number.getRealPart() + matrix[0][1]*complex_number.getImagPart();
		double imag_part = matrix[1][0]*complex_number.getRealPart() + matrix[1][1]*complex_number.getImagPart();
		
		return new complexNumber(real_part, imag_part);
	}
	
	public static Point point2x2MatrixMult(double[][] matrix, Point point) {
		
		int x = (int) (matrix[0][0]*point.getX() + matrix[0][1]*point.getY());
		int y = (int) (matrix[1][0]*point.getX() + matrix[1][1]*point.getY());
		
		return new Point(x, y);
	}
	
	public static double[][] matrixMultiplication2x2(double[][] A, double[][] B) {
		double[][] result = new double[2][2];
		
		for(int x = 0; x < 2; x++) {
			for(int i = 0; i < 2; i++) {
				for(int y = 0; y < 2; y++) {
					result[x][i] += A[x][y]*B[y][i];
				}
			}
		}
		
		return result;
	}
	
	public static void print2x2MatrixToConsole(double[][] matrix) {
		System.out.println(matrix[0][0] + "   " + matrix[0][1] + "\n" + matrix[1][0] + "   " + matrix[1][1]);
	}
	
	public static double[][] rotationMatrix(double angle_in_radians){
		double[][] rotation_matrix = {{Math.cos(angle_in_radians), -Math.sin(angle_in_radians)},
									  {Math.sin(angle_in_radians), Math.cos(angle_in_radians)}};
		
		return rotation_matrix;
	}
	
	public static double[][] similarityTransformationMatrix(double angle_in_radians, double scale){
		double[][] similarity_matrix = {{scale*Math.cos(angle_in_radians), -scale*Math.sin(angle_in_radians)},
				  						{scale*Math.sin(angle_in_radians), scale*Math.cos(angle_in_radians)}};
		
		return similarity_matrix;
	}
	
	

}
