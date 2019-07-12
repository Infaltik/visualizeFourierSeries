package visualizeFourierSeries;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

public class mathematics {
	
	public static int nbr_of_fourier_terms = 2001;
	public static complexNumber[] complexFunctionToApproximate;
	public static int originPixelX = appWindow.rendering_panel_width/2;
	public static int originPixelY = appWindow.rendering_panel_height/2;
	public static int pixelNormalizingFactor = originPixelY;
	public static int[] shifting_indices_array;
	
	public static complexNumber fourier_series_coefficients[] = new complexNumber[nbr_of_fourier_terms];
	public static complexNumber fourier_series_terms[] = new complexNumber[nbr_of_fourier_terms];
	
	
	public static Point calculateEndPoint(){
		complexNumber end_point_complex_number = new complexNumber(0,0);
		for(int i = 0; i < nbr_of_fourier_terms; i++){
			end_point_complex_number = complexAddition(end_point_complex_number, fourier_series_terms[i]);
		}
		Point end_point_pixel = complexNumberToPixel(end_point_complex_number);
		return end_point_pixel;
	}
	
	public static void calculateFourierSeriesTerms(double independent_variable){
		for(int i = 0; i < nbr_of_fourier_terms; i++){
			complexNumber exponential_factor = complexExponentialFunction(shifting_indices_array[i]*2*Math.PI*independent_variable);
			complexNumber current_series_term = complexMultiplication(fourier_series_coefficients[i], exponential_factor);
			fourier_series_terms[i] = current_series_term;
		}
	}
	
	public static void calculateFourierSeriesCoefficients(){
		int[] shift_indices_array = createShiftIndices();
		for(int i = 0; i < shift_indices_array.length; i++){
			complexNumber current_coefficient = calculateIntegralNumerically(shift_indices_array[i]);
			fourier_series_coefficients[i] = current_coefficient;
		}
	}
	
	public static complexNumber calculateIntegralNumerically(int shifting_index){
		int array_length = complexFunctionToApproximate.length;
		double delta_t = 1.0/array_length;
		
		complexNumber res = new complexNumber(0, 0);
		for(int i = 0; i < array_length; i++){
			complexNumber function_value = complexFunctionToApproximate[i];
			complexNumber shifting_exponential = complexExponentialFunction(shifting_index*2*Math.PI*delta_t*i);
			complexNumber mult_result = complexMultiplication(function_value, shifting_exponential);
			
			res = complexAddition(res, mult_result);
		}
		
		// Multiply by the infinitesimal factor in the integral
		res = new complexNumber(res.getRealPart()*delta_t, res.getImagPart()*delta_t);
		
		return res;
	}
	
	public static void addMoreSamplesToFunction() {
		int previous_array_length = complexFunctionToApproximate.length;
		complexNumber[] new_function_array = new complexNumber[2*previous_array_length-1];
		
		int k = 0;
		for(int i = 0; i < previous_array_length-1; i++) {
			complexNumber prev_value = complexFunctionToApproximate[i];
			complexNumber next_value = complexFunctionToApproximate[i+1];
			new_function_array[k] = prev_value;
			k++;
			double delta_real = next_value.getRealPart() - prev_value.getRealPart();
			double delta_imag = next_value.getImagPart() - prev_value.getImagPart();
			new_function_array[k] = new complexNumber(prev_value.getRealPart() + delta_real/2, prev_value.getImagPart() + delta_imag/2);
			k++;
		}
		
		new_function_array[k] = complexFunctionToApproximate[previous_array_length-1];
		
		complexFunctionToApproximate = new_function_array;
	}
	
	public static int[] createShiftIndices(){
		if(nbr_of_fourier_terms%2 != 1){
			System.err.println("\n Error, the number of fourier terms needs to be odd");
			return null;
		}
		
		shifting_indices_array = new int[nbr_of_fourier_terms];
		shifting_indices_array[0] = 0;
		
		int count = 0;
		for(int i=1; i<=(nbr_of_fourier_terms-1)/2; i++){
			count++;
			shifting_indices_array[count] = i;
			count++;
			shifting_indices_array[count] = -i;
		}
		
		return shifting_indices_array;
	}
	
	public static int shiftIndexToVectorSumIndex(int shift_index){
		// Shift index to the index the vector has in the vector sum. E.g. Shift index
		// -1 should be added as the third term in the vector sum and therefore has
		// vector sum index 2.
		
		int vector_sum_index = 0;
		if(shift_index > 0){
			vector_sum_index = shift_index*2-1;
		}
		else if(shift_index < 0){
			vector_sum_index = Math.abs(shift_index)*2;
		}
		
		return vector_sum_index;
	}
	
	public static complexNumber complexExponentialFunction(double input){
		return new complexNumber(Math.cos(input), Math.sin(input));
	}
	
	public static complexNumber complexMultiplication(complexNumber a, complexNumber b){
		double real_part = a.getRealPart()*b.getRealPart() - a.getImagPart()*b.getImagPart();
		double imag_part = a.getRealPart()*b.getImagPart() + b.getRealPart()*a.getImagPart();
		
		return new complexNumber(real_part, imag_part);
	}
	
	public static complexNumber complexAddition(complexNumber a, complexNumber b){
		double real_part = a.getRealPart() + b.getRealPart();
		double imag_part = a.getImagPart() + b.getImagPart();
		
		return new complexNumber(real_part, imag_part);
	}
	
	public static complexNumber calculateFourierSeriesTerm(complexNumber coefficient, int index, double input){
		return complexMultiplication(coefficient, mathematics.complexExponentialFunction(index*2*Math.PI*input));
	}
	
	public static void convertToComplexAndStoreFunction(ArrayList<Point> image_pixels_array){
		complexFunctionToApproximate = pixelArrayToComplexNumberArray(image_pixels_array);
	}
	
	
	public static complexNumber[] pixelArrayToComplexNumberArray(ArrayList<Point> image_pixels){
		// Take in the array of image pixels and return a coordinate value (complex number) for each pixel
		
		int array_length = image_pixels.size();
		complexNumber[] complex_numbers_array = new complexNumber[array_length];
		
		for(int i = 0; i < array_length; i++){
			complex_numbers_array[i] = pixelToComplexNumber(image_pixels.get(i));
		}
		
		return complex_numbers_array;
	}
	
	public static complexNumber pixelToComplexNumber(Point pixel){
		
		double real_part = ( (double)((int) pixel.getX()) - originPixelX ) / pixelNormalizingFactor;
		double imag_part = -( (double)((int) pixel.getY()) - originPixelY ) / pixelNormalizingFactor;
		
		return new complexNumber(real_part, imag_part);
	}
	
	public static Point complexNumberToPixel(complexNumber complex_number){
		
		int x_pixel = (int) Math.round(complex_number.getRealPart()*pixelNormalizingFactor + originPixelX);
		int y_pixel = (int) Math.round(-complex_number.getImagPart()*pixelNormalizingFactor + originPixelY);
		
		return new Point(x_pixel, y_pixel);
	}
	
	public static double complexMagnitudeToPixelMagnitude(double complex_magnitude){
		return complex_magnitude*pixelNormalizingFactor;
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
	
	public static Point2D.Double pointDouble2x2MatrixMult(double[][] matrix, Point2D.Double point){
		double x = matrix[0][0]*point.getX() + matrix[0][1]*point.getY();
		double y = matrix[1][0]*point.getX() + matrix[1][1]*point.getY();
		
		return new Point2D.Double(x, y);
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
