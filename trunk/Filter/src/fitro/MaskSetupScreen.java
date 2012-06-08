package fitro;

import guicomponents.GAlign;
import guicomponents.GButton;
import guicomponents.GCScheme;
import guicomponents.GComponent;
import guicomponents.GFont;
import guicomponents.GLabel;
import guicomponents.GTextField;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class MaskSetupScreen extends PApplet {

	private static final long serialVersionUID = 4984442284518242701L;

	private PImage img;
	private PImage filtered;
	private PFont font;
	
	GLabel labelMatrixSize;
	GTextField matrixW, matrixH;
	GButton btnClearMatrix;
	
	static final int MAX_W = 11;
	static final int MAX_H = 11;

	int w = MAX_W;
	int h = MAX_H;
	
	int matrixOffsetX = 5;
	int matrixOffsetY = 80;
	
	boolean matrixSizeModified = true;
	boolean matrixWeightModified = true;

	int cellSize = 25;
	int clickModifier = 1;

	private int[][] mask = new int[w][h];
	private boolean isKeyPressed = false;

	@Override
	public void setup() {
		
		GComponent.globalColor = GCScheme.getColor(this,  GCScheme.GREEN_SCHEME);
		GComponent.globalFont = GFont.getFont(this, "Arial", 16);
		matrixH = new GTextField(this, Integer.toString(h), 15, 40, 30, 20, false); // x,y,width,height
		matrixW = new GTextField(this, Integer.toString(w), 60, 40, 30, 20, false);
		labelMatrixSize = new GLabel(this, "Tamanho da Matriz", 10,10,170,30);
		btnClearMatrix = new GButton(this, "Limpar", 200, 40, 80, 25);
		btnClearMatrix.setTextAlign(GAlign.CENTER | GAlign.MIDDLE);
		
		size(800, 500);
		font = loadFont("C://ArialMT-16.vlw");
		textAlign(CENTER);
		textFont(font, 16);
		img = loadImage("C://file.jpg");
		img.resize(500, 500);

	}

	@Override
	public void draw() {
		
		if(matrixWeightModified || matrixSizeModified) {
			background(255);
			drawSizeInput();
			btnClearMatrix.draw();
			
			filtered = applyConvolution(this.normalizeMatrix(mask, w, h), w, h,
					w / 2, h / 2, img);
			image(filtered, 300, 0, 500, 500);
			this.drawMatrix(mask, w, h);
			matrixWeightModified = matrixSizeModified = false;
		}
		
		handleMatrixSizeEvents();
		handleButtonEvents();
		
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
	
	private void drawSizeInput(){
		matrixH.draw();
		matrixW.draw();
		labelMatrixSize.draw();
	}

	private void drawMatrix(int[][] matrix, int w, int h) {
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (matrix[i][j] == 0) {
					fill(255);
				} else {
					fill(168, 244, 149);
				}
				rect(matrixOffsetX + i * cellSize, matrixOffsetY + j * cellSize, cellSize, cellSize);
				fill(0);
				text((int) matrix[i][j], matrixOffsetX + i * cellSize + cellSize / 2, matrixOffsetY + j
						* cellSize + cellSize / 2 + 7);
			}
		}
	}

	private float[][] normalizeMatrix(int[][] matrix, int w, int h) {
		int total = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				total += Math.abs(matrix[i][j]);
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
		if (mouseInsideMatrix()) {
			if (mouseButton == 37) {
				// left click
				mask[(mouseX - matrixOffsetX) / cellSize][(mouseY - matrixOffsetY) / cellSize]+=clickModifier;
				matrixWeightModified = true;
			} else if (mouseButton == 39) {
				mask[(mouseX - matrixOffsetX) / cellSize][(mouseY - matrixOffsetY) / cellSize]-=clickModifier;
				matrixWeightModified = true;
			}
		}
	}
	
	private boolean mouseInsideMatrix(){
		return mouseX < matrixOffsetX + cellSize * w && mouseX > matrixOffsetX && 
				mouseY < matrixOffsetY + cellSize * h && mouseY > matrixOffsetY;
	}
	
	// CHANGED     The text has been changed
	// SET         The text has been set programmatically using setText()
	//	             this will not generate a CHANGED event as well
	// ENTERED     The enter key has been pressed
	private void handleMatrixSizeEvents(){
		matrixSizeModified = false;
		
		if(!matrixW.getText().equals("") && !matrixH.equals("")) {
		
			if(matrixW.getEventType() == GTextField.CHANGED)  {
				int newW = Integer.parseInt(matrixW.getText());
				if(newW <= MAX_W) {
					matrixSizeModified = true;
					w = newW;
				}
				
			} else if (matrixH.getEventType() == GTextField.CHANGED) {
				int newH = Integer.parseInt(matrixH.getText());
				if(newH <= MAX_H) {
					matrixSizeModified = true;
					h = newH;
				}
			}
		}
	}
	
	private void handleButtonEvents() {
		  if (btnClearMatrix.eventType == GButton.PRESSED) {
			  for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						mask[i][j] = 0;
					}
				}
			  matrixWeightModified = true;
		  }
	}
	
	public void keyPressed(){
		if(keyCode>=49 && key<=57 && !isKeyPressed){
			isKeyPressed=true;
			clickModifier=keyCode-48;
		}
	}
	
	public void keyReleased(){
		isKeyPressed=false;
		clickModifier=1;
	}

	
}
