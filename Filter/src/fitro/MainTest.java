package fitro;
import processing.core.PApplet;
import config.Configurations;


public class MainTest extends PApplet{


	private static final long serialVersionUID = 1L;

	@Override
	public void setup() {
		//super.setup();
		size(Configurations.WIDTH, Configurations.HEIGHT);
		
	}
	
	@Override
	public void draw() {
		super.draw();
		background(255);
		rect(10, 10, 10, 10);
	}
	
}
