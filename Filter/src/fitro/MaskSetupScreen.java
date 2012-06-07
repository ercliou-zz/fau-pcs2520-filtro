package fitro;

import processing.core.PApplet;
import processing.core.PImage;

public class MaskSetupScreen extends PApplet {

	private static final long serialVersionUID = 4984442284518242701L;

	private PImage img;
	private PImage filtered;

	private float[][] mask = new float[11][11];

	@Override
	public void setup() {
		size(800,500);
		img = loadImage("C://file.jpg");
		img.resize(200, 200);
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				mask[i][j] = 1.0f/121;
			}
		}
	}

	@Override
	public void draw() {
		background(255);
		filtered = applyConvolution(mask, 11, 11, 5, 5, img);
		image(filtered, 0, 0,200,200);
	}

	public PImage applyConvolution(float[][] mask, int maskHeight,
			int maskWidth, int maskCenterHeight, int maskCenterWidth, PImage img) {

		PImage resultImg = createImage(img.width,img.height,RGB);
		resultImg.loadPixels();

		for (int j = maskCenterHeight; j < img.height + maskCenterHeight - maskHeight+1; j++) {
			for (int i = maskCenterWidth; i < img.width + maskCenterWidth - maskWidth+1; i++) {
				resultImg.pixels[j*img.width+i] = calculateFilteredPixel(img, mask, i, j,
						maskWidth, maskHeight, maskCenterWidth,
						maskCenterHeight);
			}
		}
		System.out.println("convoluted");
		return resultImg;
	}
	
	private int calculateFilteredPixel(PImage img, float[][] mask,
			int actualWidth, int actualHeight, int maskWidth, int maskHeight,
			int maskCenterWidth, int maskCenterHeight) {
		
		int[] original = img.pixels;
		
		int heightOffset = actualHeight - maskCenterHeight;
		int widthOffset = actualWidth - maskCenterWidth;

		float rtotal = 0;
		float gtotal = 0;
		float btotal = 0;

		for (int j = 0; j < maskHeight; j++) {
			for (int i = 0; i < maskWidth; i++) {
				rtotal += red(original[(heightOffset + j)*img.width + (widthOffset + i)])
						* mask[j][i];
				gtotal += green(original[(heightOffset + j)*img.width + (widthOffset + i)])
						* mask[j][i];
				btotal += blue(original[(heightOffset + j)*img.width + (widthOffset + i)])
						* mask[j][i];
			}
		}
		return color((int) rtotal, (int) gtotal, (int) btotal);
	}
	
}
