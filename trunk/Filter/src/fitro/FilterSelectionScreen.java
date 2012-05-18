package fitro;

import processing.core.PApplet;
import controlP5.ControlP5;

public class FilterSelectionScreen extends PApplet{
	 
	private static final long serialVersionUID = 4115104460264236445L;
	private ControlP5 cp5;

	public FilterSelectionScreen() {
		super();
		System.out.println(this);
		cp5 = new ControlP5(this);
	}

	
}
