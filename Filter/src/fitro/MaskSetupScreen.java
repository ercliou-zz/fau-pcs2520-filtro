package fitro;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class MaskSetupScreen extends PApplet {

	private static final long serialVersionUID = 4984442284518242701L;

	private PImage img;
	private PImage filtered;
	private PFont font;

	int w = 11;
	int h = 11;

	int cellSize = 20;

	private int[][] mask = new int[w][h];

	@Override
	public void setup() {
		size(800, 500);
		font = loadFont("C://ArialMT-16.vlw");
		textAlign(CENTER);
		textFont(font, 16);
		img = loadImage("C://file.jpg");
		img.resize(500, 500);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				//mask[i][j] = 1;
			}
		}
	}

	@Override
	public void draw() {
		background(255);
		filtered = applyConvolution(this.normalizeMatrix(mask, w, h), w, h,
				w / 2, h / 2, img);
		image(filtered, 300, 0, 500, 500);
		this.drawMatrix(mask, w, h);
	}

	public PImage applyConvolution(float[][] mask, int maskWidth,
			int maskHeight, int maskCenterWidth, int maskCenterHeight,
			PImage img) {

		PImage resultImg = createImage(img.width, img.height, RGB);
		resultImg.loadPixels();

		for (int j = maskCenterHeight; j < img.height + maskCenterHeight
				- maskHeight + 1; j++) {
			for (int i = maskCenterWidth; i < img.width + maskCenterWidth
					- maskWidth + 1; i++) {
				resultImg.pixels[j * img.width + i] = calculateFilteredPixel(
						img, mask, i, j, maskWidth, maskHeight,
						maskCenterWidth, maskCenterHeight);
			}
		}
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
				rtotal += red(original[(heightOffset + j) * img.width
						+ (widthOffset + i)])
						* mask[i][j];
				gtotal += green(original[(heightOffset + j) * img.width
						+ (widthOffset + i)])
						* mask[i][j];
				btotal += blue(original[(heightOffset + j) * img.width
						+ (widthOffset + i)])
						* mask[i][j];
			}
		}
		return color((int) rtotal, (int) gtotal, (int) btotal);
	}

	private void drawMatrix(int[][] matrix, int w, int h) {
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (matrix[i][j] == 0) {
					fill(255);
				} else {
					fill(168, 244, 149);
				}
				rect(i * cellSize, j * cellSize, cellSize, cellSize);
				fill(0);
				text((int) matrix[i][j], i * cellSize + cellSize / 2, j
						* cellSize + cellSize / 2 + 7);
			}
		}
	}

	private float[][] normalizeMatrix(int[][] matrix, int w, int h) {
		int total = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				total += matrix[i][j];
			}
		}
		float[][] normalized = new float[w][h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				normalized[i][j] = ((float) matrix[i][j]) / total;
			}
		}
		return normalized;
	}

	public void mouseClicked() {
		if (mouseX < cellSize * w && mouseY < cellSize * h) {
			if (mouseButton == 37) {
				// left click
				mask[mouseX / cellSize][mouseY / cellSize]++;
			} else if (mouseButton == 39) {
				mask[mouseX / cellSize][mouseY / cellSize]--;
			}
		}
	}
}
