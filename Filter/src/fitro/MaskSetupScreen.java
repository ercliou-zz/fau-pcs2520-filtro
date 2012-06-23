package fitro;

import guicomponents.GAlign;
import guicomponents.GButton;
import guicomponents.GCScheme;
import guicomponents.GComponent;
import guicomponents.GFont;
import guicomponents.GLabel;
import guicomponents.GTextField;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics2D;
import processing.core.PImage;

public class MaskSetupScreen extends PApplet {

	private static final long serialVersionUID = 4984442284518242701L;

	private PImage originalImg;
	private PImage img;
	private PImage filtered;
	private PFont font;

	GLabel labelMatrixSize;
	GTextField matrixW, matrixH;
	GButton btnClearMatrix, btnFilter01, btnFilter02, btnFilter03, btnFilter04, btnFilter05, btnFilter06, btnFilter07, btnFilter08, btnFilter09, btnFilter10,
			btnFilter11;

	PGraphics2D setupScreen = new PGraphics2D();
	PGraphics2D imageScreen = new PGraphics2D();

	static final int MAX_W = 11;
	static final int MAX_H = 11;
	static final int MAX_IMG_W = 500;
	static final int MAX_IMG_H = 500;

	int w = MAX_W;
	int h = MAX_H;

	int maskCenterX = w / 2;
	int maskCenterY = h / 2;

	int matrixOffsetX = 5;
	int matrixOffsetY = 210;

	boolean matrixSizeModified = true;
	boolean matrixWeightModified = true;
	boolean imgModified = false;

	int cellSize = 25;
	int clickModifier = 1;

	private int[][] mask = new int[w][h];
	private boolean isKeyPressed = false;
	private boolean changeMaskCenter = false;
	private boolean isNegative = false;
	private boolean isThreshold = false;
	private boolean isWhiteBlack = false;
	private boolean isDilation = false;
	private boolean isErosion = false;

	private int threshold = 180;

	@Override
	public void setup() {

		GComponent.globalColor = GCScheme.getColor(this, GCScheme.YELLOW_SCHEME);
		GComponent.globalFont = GFont.getFont(this, "Arial", 16);
		matrixH = new GTextField(this, Integer.toString(h), 15, 40, 30, 20, false); // x,y,width,height
		matrixW = new GTextField(this, Integer.toString(w), 60, 40, 30, 20, false);
		labelMatrixSize = new GLabel(this, "Tamanho da Matriz", 10, 10, 170, 30);
		btnClearMatrix = new GButton(this, "Limpar", 10, 520, 80, 25);
		btnClearMatrix.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter01 = new GButton(this, "Blur*", 10, 80, 80, 25);
		btnFilter01.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter02 = new GButton(this, "Negative", 100, 80, 80, 25);
		btnFilter02.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter03 = new GButton(this, "Bordas*", 190, 80, 80, 25);
		btnFilter03.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter04 = new GButton(this, "Sharpen*", 10, 120, 80, 25);
		btnFilter04.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter05 = new GButton(this, "P/B", 100, 120, 80, 25);
		btnFilter05.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter06 = new GButton(this, "Threshold", 190, 120, 80, 25);
		btnFilter06.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter07 = new GButton(this, "Emboss*", 10, 160, 80, 25);
		btnFilter07.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter08 = new GButton(this, "Dilation", 100, 160, 80, 25);
		btnFilter08.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter09 = new GButton(this, "Erosion", 190, 160, 80, 25);
		btnFilter09.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);

		btnFilter10 = new GButton(this, "Browse..", 390, 520, 80, 20);
		btnFilter10.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		btnFilter11 = new GButton(this, "Save", 300, 520, 80, 20);
		btnFilter11.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);

		size(820, 550);
		// font =
		// loadFont("/Users/maryliagutierrez/Documents/projFau/Filtros/lib/ArialMT-16.vlw");
		font = loadFont("C://ArialMT-16.vlw");

		textAlign(CENTER);
		textFont(font, 16);
		// img = loadImage("/Users/maryliagutierrez/Downloads/file.jpg");
		img = loadImage("C://file.jpg");
		try {
			originalImg = (PImage) img.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		this.resizeImage(img);
		initializeMatrixCenter();

	}

	@Override
	public void draw() {

		if (matrixWeightModified || matrixSizeModified || imgModified) {
			background(255);

			filtered = applyConvolution(this.normalizeMatrix(mask, w, h), w, h, img);
			image(filtered, 300 + (MAX_IMG_W - filtered.width) / 2, 0 + (MAX_IMG_H - filtered.height) / 2, filtered.width, filtered.height);
			this.drawMatrix(mask, w, h);
			matrixWeightModified = matrixSizeModified = false;

			drawSizeInput();
		}

		handleMatrixSizeEvents();
		handleButtonEvents();

	}

	public PImage applyConvolution(float[][] mask, int maskWidth, int maskHeight, PImage img) {

		PImage resultImg = createImage(img.width, img.height, RGB);
		resultImg.loadPixels();

		for (int j = maskCenterY; j < img.height + maskCenterY - maskHeight + 1; j++) {
			for (int i = maskCenterX; i < img.width + maskCenterX - maskWidth + 1; i++) {
				resultImg.pixels[j * img.width + i] = calculateFilteredPixel(img, mask, i, j, maskWidth, maskHeight, maskCenterX, maskCenterY);
			}
		}
		return resultImg;
	}

	private int calculateFilteredPixel(PImage img, float[][] mask, int actualWidth, int actualHeight, int maskWidth, int maskHeight, int maskCenterWidth,
			int maskCenterHeight) {

		int[] original = img.pixels;

		int heightOffset = actualHeight - maskCenterHeight;
		int widthOffset = actualWidth - maskCenterWidth;

		float rtotal = 0;
		float gtotal = 0;
		float btotal = 0;

		if (isDilation || isErosion) {
			return dilationCalculation(img, mask, maskWidth, maskHeight, heightOffset, widthOffset);
		}

		for (int j = 0; j < maskHeight; j++) {
			for (int i = 0; i < maskWidth; i++) {
				rtotal += red(original[(heightOffset + j) * img.width + (widthOffset + i)]) * mask[i][j];
				gtotal += green(original[(heightOffset + j) * img.width + (widthOffset + i)]) * mask[i][j];
				btotal += blue(original[(heightOffset + j) * img.width + (widthOffset + i)]) * mask[i][j];
			}
		}

		/**** Point Operations ****/
		if (isNegative) {
			return color(255 - (int) rtotal, 255 - (int) gtotal, 255 - (int) btotal);
		}

		if (isThreshold) {
			return (rtotal + gtotal + btotal) > this.threshold ? color(255) : color(0);
		}

		if (isWhiteBlack) {
			return color((int) (rtotal * 0.2125 + gtotal * 0.7154 + btotal * 0.072));
		}

		return color((int) rtotal, (int) gtotal, (int) btotal);
	}

	private int dilationCalculation(PImage img, float[][] mask, int maskWidth, int maskHeight, int heightOffset, int widthOffset) {

		int[] original = img.pixels;

		float rFinal, gFinal, bFinal;
		float rAux, gAux, bAux;

		if (isErosion) {
			rFinal = gFinal = bFinal = 255;
		} else { // isDilation
			rFinal = gFinal = bFinal = 0;
		}

		for (int j = 0; j < maskHeight; j++) {
			for (int i = 0; i < maskWidth; i++) {
				if (mask[i][j] > 0) {
					rAux = red(original[(heightOffset + j) * img.width + (widthOffset + i)]) + mask[i][j];
					gAux = green(original[(heightOffset + j) * img.width + (widthOffset + i)]) + mask[i][j];
					bAux = blue(original[(heightOffset + j) * img.width + (widthOffset + i)]) + mask[i][j];
					if (isErosion) {
						rFinal = rFinal > rAux ? rAux : rFinal;
						gFinal = gFinal > gAux ? gAux : gFinal;
						bFinal = bFinal > bAux ? bAux : bFinal;
					} else { // isDilation
						rFinal = rFinal < rAux ? rAux : rFinal;
						gFinal = gFinal < gAux ? gAux : gFinal;
						bFinal = bFinal < bAux ? bAux : bFinal;
					}
				}
			}
		}

		return color((int) rFinal, (int) gFinal, (int) bFinal);
	}

	private void drawSizeInput() {
		matrixH.setText(Integer.toString(h));
		matrixH.draw();

		matrixW.setText(Integer.toString(w));
		matrixW.draw();

		labelMatrixSize.draw();
	}

	private void drawMatrix(int[][] matrix, int w, int h) {
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (i == maskCenterX && j == maskCenterY) {
					fill(240, 240, 60);
				} else if (matrix[i][j] == 0) {
					fill(255);
				} else {
					fill(168, 244, 149);
				}
				rect(matrixOffsetX + i * cellSize, matrixOffsetY + j * cellSize, cellSize, cellSize);
				fill(0);
				text((int) matrix[i][j], matrixOffsetX + i * cellSize + cellSize / 2, matrixOffsetY + j * cellSize + cellSize / 2 + 7);
			}
		}
	}

	private float[][] normalizeMatrix(int[][] matrix, int w, int h) {
		int total = 0;
		float[][] normalized = new float[w][h];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				total += matrix[i][j];
			}
		}

		if (total == 0) { // TODO: WORKAROUND
			total = 1;
		}

		if (isDilation || isErosion) { // nao normaliza
			total = 1;
		}

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				normalized[i][j] = ((float) matrix[i][j]) / total;
			}
		}
		return normalized;
	}

	public void mouseClicked() {
		if (mouseInsideMatrix()) {
			matrixWeightModified = true;
			if (changeMaskCenter) {
				maskCenterX = (mouseX - matrixOffsetX) / cellSize;
				maskCenterY = (mouseY - matrixOffsetY) / cellSize;
			} else {

				if (mouseButton == 37) {
					// left click
					mask[(mouseX - matrixOffsetX) / cellSize][(mouseY - matrixOffsetY) / cellSize] += clickModifier;
				} else if (mouseButton == 39) {
					mask[(mouseX - matrixOffsetX) / cellSize][(mouseY - matrixOffsetY) / cellSize] -= clickModifier;
				}
			}
		}
	}

	private boolean mouseInsideMatrix() {
		return mouseX < matrixOffsetX + cellSize * w && mouseX > matrixOffsetX && mouseY < matrixOffsetY + cellSize * h && mouseY > matrixOffsetY;
	}

	// CHANGED The text has been changed
	// SET The text has been set programmatically using setText()
	// this will not generate a CHANGED event as well
	// ENTERED The enter key has been pressed
	private void handleMatrixSizeEvents() {
		matrixSizeModified = false;

		if (!matrixW.getText().equals("") && !matrixH.getText().equals("")) {

			if (matrixW.getEventType() == GTextField.CHANGED) {
				int newW = Integer.parseInt(matrixW.getText());
				if (newW <= MAX_W) {
					matrixSizeModified = true;
					w = newW;
				}

			} else if (matrixH.getEventType() == GTextField.CHANGED) {
				int newH = Integer.parseInt(matrixH.getText());
				if (newH <= MAX_H) {
					matrixSizeModified = true;
					h = newH;
				}
			}
		}
	}

	private void handleButtonEvents() {

		if (anyButtonPressed()) {
			isThreshold = false;
			isNegative = false;
			isWhiteBlack = false;
			isDilation = false;
			isErosion = false;
		}

		if (btnClearMatrix.eventType == GButton.PRESSED) {
			clearMatrix();
			matrixWeightModified = true;
			return;

		}

		if (btnFilter01.eventType == GButton.PRESSED) {
			w = 5;
			h = 5;
			maskCenterX = 2;
			maskCenterY = 2;
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					mask[i][j] = 1;
					if (i == 2 || j == 2) {
						mask[i][j] = 5;
					} else if (i == j || (j == (4 - i))) {
						mask[i][j] = 3;
					}
				}
			}
			matrixWeightModified = true;
			matrixSizeModified = true;
			return;
		}

		if (btnFilter02.eventType == GButton.PRESSED) {
			matrixWeightModified = true;
			isNegative = true;
			return;
		}

		if (btnFilter03.eventType == GButton.PRESSED) { // edges
			w = 3;
			h = 3;
			maskCenterX = 1;
			maskCenterY = 1;
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					if (i != 1 || j != 1)
						mask[i][j] = -1;
				}
			}
			mask[1][1] = 8;
			matrixWeightModified = true;
			matrixSizeModified = true;
			return;
		}

		if (btnFilter04.eventType == GButton.PRESSED) { // sharpen
			w = 3;
			h = 3;
			maskCenterX = 1;
			maskCenterY = 1;

			mask[0][0] = mask[0][2] = mask[2][0] = mask[2][2] = 0;
			mask[0][1] = mask[1][0] = mask[1][2] = mask[2][1] = -1;
			mask[1][1] = 5;

			matrixWeightModified = true;
			matrixSizeModified = true;
			return;
		}

		if (btnFilter05.eventType == GButton.PRESSED) {
			matrixWeightModified = true;
			isWhiteBlack = true;
			return;
		}

		if (btnFilter06.eventType == GButton.PRESSED) {

			matrixWeightModified = true;
			isThreshold = true;
			return;
		}

		if (btnFilter07.eventType == GButton.PRESSED) { // emboss

			w = 3;
			h = 3;
			maskCenterX = 1;
			maskCenterY = 1;

			mask[0][0] = -2;
			mask[0][1] = mask[1][0] = -1;
			mask[0][2] = mask[2][0] = 0;
			mask[1][1] = mask[1][2] = mask[2][1] = 1;
			mask[2][2] = 2;

			matrixSizeModified = true;
			matrixWeightModified = true;
			return;
		}

		if (btnFilter08.eventType == GButton.PRESSED) { // dilation
			w = 11;
			h = 11;
			maskCenterX = 5;
			maskCenterY = 5;

			clearMatrix();
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					if (((i - 5) * (i - 5) + (j - 5) * (j - 5)) < 16)
						mask[i][j] = 1;
				}
			}
			mask[5][5] = 2;
			matrixSizeModified = true;
			matrixWeightModified = true;
			isDilation = true;
			return;
		}

		if (btnFilter09.eventType == GButton.PRESSED) { // erosion
			w = 11;
			h = 11;
			maskCenterX = 5;
			maskCenterY = 5;

			clearMatrix();
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					if (((i - 5) * (i - 5) + (j - 5) * (j - 5)) < 16)
						mask[i][j] = 1;
				}
			}
			mask[5][5] = 2;
			matrixSizeModified = true;
			matrixWeightModified = true;
			isErosion = true;
			return;
		}

		if (btnFilter10.eventType == GButton.PRESSED) {
			img = pickImage();
			this.resizeImage(img);
			return;
		}

		if (btnFilter11.eventType == GButton.PRESSED) {
			exportImage();
			return;
		}
	}

	private PImage pickImage() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (checkExtension(file.getName())) {
				imgModified = true;
				return loadImage(file.getPath());
			}
		}
		return null;
	}

	private boolean checkExtension(String fileName) {
		if (fileName.toUpperCase().endsWith("GIF") || fileName.toUpperCase().endsWith("JPG") || fileName.toUpperCase().endsWith("TGA")
				|| fileName.toUpperCase().endsWith("PNG")) {
			return true;
		}
		return false;
	}

	private boolean anyButtonPressed() {
		return btnClearMatrix.eventType == GButton.PRESSED || btnFilter01.eventType == GButton.PRESSED || btnFilter02.eventType == GButton.PRESSED
				|| btnFilter03.eventType == GButton.PRESSED || btnFilter04.eventType == GButton.PRESSED || btnFilter05.eventType == GButton.PRESSED
				|| btnFilter06.eventType == GButton.PRESSED || btnFilter07.eventType == GButton.PRESSED || btnFilter08.eventType == GButton.PRESSED
				|| btnFilter09.eventType == GButton.PRESSED;
	}

	private void clearMatrix() {
		for (int i = 0; i < MAX_W; i++) {
			for (int j = 0; j < MAX_H; j++) {
				mask[i][j] = 0;
			}
		}
		initializeMatrixCenter();
	}
	private void initializeMatrixCenter(){
		mask[maskCenterX][maskCenterY]=1;
	}

	public void keyPressed() {
		isKeyPressed = true;
		if (keyCode >= 49 && key <= 57 && !isKeyPressed) {
			clickModifier = keyCode - 48;
		} else if (keyCode == KeyEvent.VK_SHIFT) {
			changeMaskCenter = true;
		}
	}

	public void keyReleased() {
		isKeyPressed = false;
		changeMaskCenter = false;
		clickModifier = 1;
	}

	private void resizeImage(PImage image) {
		float ratio = (float) image.width / (float) image.height;
		float fitRatio = (float) MAX_IMG_W / (float) MAX_IMG_H;
		if (ratio > fitRatio) {
			float wratio = (float) MAX_IMG_W / (float) image.width;
			image.resize(MAX_IMG_W, (int) wratio * image.height);
		} else if (ratio < fitRatio) {
			float hratio = (float) MAX_IMG_H / (float) image.height;
			image.resize((int) hratio * image.width, MAX_IMG_H);
		} else {
			image.resize(MAX_IMG_W, MAX_IMG_H);
		}
	}
	
	private void exportImage(){
		PImage originalFiltered = applyConvolution(this.normalizeMatrix(mask, w, h), w, h, originalImg);
		originalFiltered.save("../export.png");
	}
}
