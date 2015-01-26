package com.fabrika.ImageProcessor;

public class IPTask implements Runnable {

	public enum PROCESS_TYPE {
		CONVERT_TO_GRAYSCALE,
		SATURATION_CORRECTION,
		TINT_CORRECTION,
		TINT_SATURATION_CORRECTION,
		CONVOLUTION
	}

	private static final int MASK_WIDTH = 3;
	private static final int MASK_HEIGHT = 3;
	//private double [] mask = {1.0, 2.0, 1.0, 2.0, 4.0, 2.0, 1.0, 2.0, 1.0};
	//private double [] mask = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
	
	//private double [] mask = {-1.0, -1.0, -1.0, -1.0, 9.0, -1.0, -1.0, -1.0, -1.0};
	private double [] mask = {-0.0, -1.0, -0.0, -1.0, 5, -1.0, -0.0, -1.0, -0.0};
//	private double [] mask = {-1.0, -1.0, -1.0, -1.0, -1.0,
//							  -1.0,  2.0,  2.0,  2.0, -1.0,
//							  -1.0,  2.0,  8.0,  2.0, -1.0,
//							  -1.0,  2.0,  2.0,  2.0, -1.0,
//							  -1.0, -1.0, -1.0, -1.0, -1.0};
	
	private final PROCESS_TYPE ptype;
	
	private int[] image;
	private int width;
	private int height;
	

	IPTask(int[] image, int width, int height, PROCESS_TYPE ptype){
		this.image = image;
		this.width = width;
		this.height = height;
		this.ptype = ptype;
	}

	@Override
	public void run() {
		switch(ptype){
		case CONVERT_TO_GRAYSCALE:
			convertToGray();
			break;
		case SATURATION_CORRECTION:
			correctSaturation(150);
			break;
		case TINT_CORRECTION:
			correctTint(50);
		break;
		case TINT_SATURATION_CORRECTION:
			correctSaturation(150);
			correctTint(20);
		break;
		case CONVOLUTION:
			convolve();
		break;
		}
	}
		
	private void convertToGray() {
		for (int x = 0; x < image.length; ++x) {
			image[x] = convertPixelToGray(image[x]);
		}
	}

	private int convertPixelToGray(int rgbPixel) {

		int r = redValue(rgbPixel);
		int g = greenValue(rgbPixel);
		int b = blueValue(rgbPixel);

		int grayValue = (int) (r * .3 + g * .59 + b * .11);

		return (grayValue << 16) + (grayValue << 8) + grayValue;
	}
	
	private void correctTint(int tint) {
		for (int x = 0; x < image.length; ++x) {
			image[x] = correctPixelTint(image[x], tint);
		}
	}

	private int correctPixelTint(int rgb, int tint) {
		int r = redValue(rgb);
		int g = greenValue(rgb);
		int b = blueValue(rgb);

		double theta = Math.PI * tint/180.0;
		int c = (int) (256*Math.cos(theta));
		int s = (int) (256*Math.sin(theta));
		
		int ry1 = ( 70 * r - 59 * g - 11 * b) / 100;
		int by1 = (-30 * r - 59 * g + 89 * b) / 100;

		int y = (30 * r + 59 * g + 11 * b) / 100;

		int ry = (s*by1 + c*ry1) / 256;
		int by = (c*by1 - s*ry1) / 256;

		int gy = (-51*ry - 19*by) / 100;
		
		r = ry + y;
		g = gy + y;
		b = by + y;

		if (r < 0) r = 0;
		if (g < 0) g = 0;
		if (b < 0) b = 0;

		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;

		return (r << 16) + (g << 8) + b;
	}
	
	private void correctSaturation(int saturation) {
		for (int x = 0; x < image.length; ++x) {
			image[x] = correctPixelSaturation(image[x], saturation);
		}
	}

	private int correctPixelSaturation(int rgb, int saturation) {
		int r = redValue(rgb);
		int g = greenValue(rgb);
		int b = blueValue(rgb);

		int ry1 = (70 * r - 59 * g - 11 * b) / 100;
		int gy1 = (-30 * r + 41 * g - 11 * b) / 100;
		int by1 = (-30 * r - 59 * g + 89 * b) / 100;

		int y = (30 * r + 59 * g + 11 * b) / 100;

		int ry = (ry1 * saturation) / 100;
		int gy = (gy1 * saturation) / 100;
		int by = (by1 * saturation) / 100;

		r = ry + y;
		g = gy + y;
		b = by + y;

		if (r < 0) r = 0;
		if (g < 0) g = 0;
		if (b < 0) b = 0;

		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;

		return (r << 16) + (g << 8) + b;
	}
	
	private void convolve(){
		
		int[] tmp_image = new int[image.length];
		
		final int INDEX_OFFSET = MASK_WIDTH / 2;
		
		for(int p = 0; p < 1; p++){
		
			for(int x = 0; x < width; ++x){
				for(int y = 0; y < height; ++y){
					double r = 0.0, g = 0.0, b = 0.0;
					double normFactor = 0.0;
					for(int xm = 0; xm < MASK_WIDTH; ++xm){
						for(int ym = 0; ym < MASK_HEIGHT; ++ym){
							int dx = x + xm - INDEX_OFFSET;
							int dy = y + ym - INDEX_OFFSET;
							int im = 0;
							if(dx >= 0 && dy >= 0 && dx < width && dy < height){
								double m = mask[xm + ym*MASK_WIDTH];
								normFactor += m;
								im = image[dx + dy*width];
								r += m*redValue(im);
								g += m*greenValue(im);
								b += m*blueValue(im);
							}
						}
					}
					
					r /= normFactor; g /= normFactor; b /= normFactor;
					
					if (r < 0) r = 0.0;
					if (g < 0) g = 0.0;
					if (b < 0) b = 0.0;

					
					if (r > 255) r = 255.0;
					if (g > 255) g = 255.0;
					if (b > 255) b = 255.0;
					
					tmp_image[x + y*width] = (((int)r) << 16) + (((int)g) << 8) + ((int)b);
				}
			}
			System.arraycopy(tmp_image, 0, image, 0, image.length);
		}
		
	}
	
	private int redValue(int rgb){
		return (rgb & 0x00FF0000) >> 16;
	}
	
	private int greenValue(int rgb){
		return (rgb & 0x0000FF00) >> 8;
	}
	
	private int blueValue(int rgb){
		return (rgb & 0x000000FF);
	}
	
}