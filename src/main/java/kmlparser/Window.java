package kmlparser;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class Window extends JTabbedPane {

	private static final long serialVersionUID = 1L;
	
	public Window() {
		this.addTab("Filter", null, new Filter(),
                "Filter stores by location");
		this.addTab("Edit", null, new Edit(),
                "Add/Remove store locations");
		this.addTab("KMZ", null, new KMZ(),
                "Convert Kml to Kmz");
		
		createWindow(this);
	}
	
	private void createWindow(JTabbedPane panel) {
		JFrame frame = new JFrame("KML Parser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(410, 470);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setContentPane(this);;
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new Window();
	}

}
