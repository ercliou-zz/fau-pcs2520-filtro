//package fitro;
//
//import processing.core.*;
//
//class Button{
//	
//	PApplet parent;
//	
//	public Button(PApplet parent) {
//		super();
//		this.parent = parent;
//	}
//
//	int x, y;
//	int size;
//	int basecolor, highlightcolor, currentcolor;
//	boolean over = false;
//	boolean pressed = false;
//
//	void update() {
//		if (over()) {
//			currentcolor = highlightcolor;
//		} else {
//			currentcolor = basecolor;
//		}
//	}
//
//	boolean pressed() {
//		if (over) {
//			parent.locked = true;
//			return true;
//		} else {
//			locked = false;
//			return false;
//		}
//	}
//
//	boolean over() {
//		return true;
//	}
//
//	boolean overRect(int x, int y, int width, int height) {
//		if (mouseX >= x && mouseX <= x + width && mouseY >= y
//				&& mouseY <= y + height) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	boolean overCircle(int x, int y, int diameter) {
//		float disX = x - mouseX;
//		float disY = y - mouseY;
//		if (sqrt(sq(disX) + sq(disY)) < diameter / 2) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//}