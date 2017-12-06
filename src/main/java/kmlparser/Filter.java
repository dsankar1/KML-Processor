package kmlparser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;

public class Filter extends JPanel {
	
	private static final long serialVersionUID = 1L;
	public static final String[] SOUTH_VALUES = new String[] { "GA", "TN", "NC", "SC", "MS", "AL" };
	public static final String[] NORTHEAST_VALUES = new String[] { "CT", "NY", "NJ" };
	public static final String[] MIDWEST_VALUES = new String[] { "IL", "IN", "IA", "MI", "MN", "MO", "NA", "WI" };
	public static final String[] NORTH_ATLANTIC_VALUES = new String[] { "CT", "MA", "ME", "NH", "RI" };
	public static final String[] MID_ATLANTIC_VALUES = new String[] { "KY", "MD", "NJ", "PA", "OH", "VA", "DC" };
	public static final String[] SOUTHWEST_VALUES = new String[] { "LA", "TX", "AR", "OK" };
	public static final String[] ROCKY_MOUNTAIN_VALUES = new String[] { "CO", "ID", "KS", "NM", "UT" };
	public static final String[] PACIFIC_NORTHWEST_VALUES = new String[] { "OR", "WA" };
	public static final String[] FLORIDA_VALUES = new String[] { "FL" };
	
	private JLabel srcTrgBorder, srcLabel, srcText, 
		trgKmlLabel, trgLabel, filterBorder, storeLabel, latitudeLabel, longitudeLabel, radiusBorder, radiusLabel, kmLabel;
	private JButton srcBtn, filterBtn;
	private JTextField trgText, storeText, latitudeText, longitudeText, radiusText;
	private JCheckBox clean;
	private JCheckBox southBox, northeastBox, midwestBox, 
		northAtlanticBox, midAtlanticBox, southwestBox,
		rockyMountainBox, pacificNorthwestBox, floridaBox;
	private File source, target;
	private volatile boolean loading = false;
	
	public Filter() {
		this.setLayout(null);
		organizeUIComponents();
		setComponentEventListeners();
	}
	
	private void addSouth(Set<String> filter) {
		filter.addAll(Arrays.asList(SOUTH_VALUES));
	}
	
	private void addNortheast(Set<String> filter) {
		filter.addAll(Arrays.asList(NORTHEAST_VALUES));
	}
	
	private void addMidwest(Set<String> filter) {
		filter.addAll(Arrays.asList(MIDWEST_VALUES));
	}
	
	private void addNorthAtlantic(Set<String> filter) {
		filter.addAll(Arrays.asList(NORTH_ATLANTIC_VALUES));
	}
	
	private void addMidAtlantic(Set<String> filter) {
		filter.addAll(Arrays.asList(MID_ATLANTIC_VALUES));
	}
	
	private void addSouthwest(Set<String> filter) {
		filter.addAll(Arrays.asList(SOUTHWEST_VALUES));
	}
	
	private void addRockyMountain(Set<String> filter) {
		filter.addAll(Arrays.asList(ROCKY_MOUNTAIN_VALUES));
	}
	
	private void addPacificNorthwest(Set<String> filter) {
		filter.addAll(Arrays.asList(PACIFIC_NORTHWEST_VALUES));
	}
	
	private void addFlorida(Set<String> filter) {
		filter.addAll(Arrays.asList(FLORIDA_VALUES));
	}
	
	private void setComponentEventListeners() {
		final Filter parent = this;
		srcBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (loading) return;
				FileNameExtensionFilter filter = new FileNameExtensionFilter("KML Files", "kml");
				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(System.getProperty("user.home")));
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(filter);
				int returnVal = fc.showOpenDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
	                source = fc.getSelectedFile();
	                srcText.setText(source.getName());   
	            }
			}
			
		});
		
		filterBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (loading) return;
				if (trgText.getText().equals("") || srcText.getText().equals("")) {
					JOptionPane.showMessageDialog(parent,
						    "Please specify a source and target file.",
						    "Missing Field",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				target = new File(source.getParent(), trgText.getText() + ".kml");
				if (target.exists()) {
					JOptionPane.showMessageDialog(parent,
						    "Target file already exists.",
						    "File Exists Error",
						    JOptionPane.ERROR_MESSAGE);
				} else {
					loading = true;
					try {
						Kml kml;
						if (clean.isSelected()) {
							kml = KmlUtility.cleanKml(Kml.unmarshal(source));
						} else {
							kml = Kml.unmarshal(source);
						}
						Document document = (Document) kml.getFeature();
						List<Feature> placemarks = document.getFeature();
						
						// Regions are filtered here
						/*Set<String> filter = new HashSet<String>();
						if (southBox.isSelected()) addSouth(filter);
						if (northeastBox.isSelected()) addNortheast(filter);
						if (midwestBox.isSelected()) addMidwest(filter);
						if (northAtlanticBox.isSelected()) addNorthAtlantic(filter);
						if (midAtlanticBox.isSelected()) addMidAtlantic(filter);
						if (southwestBox.isSelected()) addSouthwest(filter);
						if (rockyMountainBox.isSelected()) addRockyMountain(filter);
						if (pacificNorthwestBox.isSelected()) addPacificNorthwest(filter);
						if (floridaBox.isSelected()) addFlorida(filter);*/
						Double latitude = null, longitude = null, radius = 0.0;
						String storeName = storeText.getText().trim();
						if (!latitudeText.getText().trim().equals("")) latitude = Double.parseDouble(latitudeText.getText().trim());
						if (!longitudeText.getText().trim().equals("")) longitude = Double.parseDouble(longitudeText.getText().trim());
						if (!radiusText.getText().trim().equals("")) radius = Double.parseDouble(radiusText.getText().trim());
						
						List<Feature> filtered = new ArrayList<Feature>();
						if (!storeName.equals("")) {
							for (int i = 0; i < placemarks.size(); i++) {
								Placemark placemark = (Placemark) placemarks.get(i);
								if (placemark.getName().equals(storeName)) {
									Point point = (Point) placemark.getGeometry();
									Coordinate coordinate = point.getCoordinates().get(0);
									latitude = coordinate.getLatitude();
									longitude = coordinate.getLongitude();
									filtered.add(placemark);
									placemarks.remove(i);
									break;
								}
							}
						}
						
						if (latitude != null && longitude != null) {
							for (Feature feature : placemarks) {
								Placemark placemark = (Placemark) feature;
								Point point = (Point) placemark.getGeometry();
								Coordinate coordinate = point.getCoordinates().get(0);
								
								double tempLatitude = coordinate.getLatitude();
								double tempLongitude = coordinate.getLongitude();
								if (withinRadius(latitude, longitude, tempLatitude, tempLongitude, radius)) {
									filtered.add(placemark);
								}
							}
						} else {
							filtered = placemarks;
						}
						
						document.setFeature(filtered);
						KmlUtility.marshalWithoutPrefix(kml, target, false);
						Runtime.getRuntime().exec("explorer.exe /select," + target.getPath());
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						loading = false;
					}
				}
			}
			
		});
	}
	
	private boolean withinRadius(double lat1, double lon1, double lat2, double lon2, double radius) {
		final double EARTHS_RADIUS = 6371.0; //Kilometers
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double latDelta = Math.toRadians(Math.abs(lat2) - Math.abs(lat1));
		double lonDelta = Math.toRadians(Math.abs(lon2) - Math.abs(lon1));
		
		double a = (Math.sin(latDelta/2D) * Math.sin(latDelta/2D))
				+ (Math.cos(lat1Rad) * Math.cos(lat2Rad))
				* (Math.sin(lonDelta/2D) * Math.sin(lonDelta/2D));
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		
		double distance = EARTHS_RADIUS * c;
		
		System.out.println(distance);
		
		return (distance <= radius);
	}
	
	private void organizeUIComponents() {
		// Border around the file related fields
		srcTrgBorder = new JLabel();
		srcTrgBorder.setBorder(BorderFactory.createTitledBorder("KML Files"));
		srcTrgBorder.setLocation(10, 10);
		srcTrgBorder.setSize(380, 130);
		
		srcBtn = new JButton("Browse");
		srcBtn.setLocation(346, 40);
		srcBtn.setSize(24, 24);
		
		srcLabel = new JLabel("Source:");
		srcLabel.setLocation(24, 40);
		srcLabel.setSize(50, 25);
		
		clean = new JCheckBox("Normalize KML");
		clean.setSize(120, 25);
		clean.setLocation(264, 100);
		
		srcText = new JLabel();
		srcText.setLocation(74, 40);
		srcText.setSize(264, 25);
		srcText.setBorder(BorderFactory.createEtchedBorder());
		
		trgKmlLabel = new JLabel(".kml");
		trgKmlLabel.setLocation(344, 70);
		trgKmlLabel.setSize(24, 24);
		
		trgLabel = new JLabel("Target:");
		trgLabel.setLocation(28, 70);
		trgLabel.setSize(50, 25);
		
		trgText = new JTextField();
		trgText.setLocation(74, 70);
		trgText.setSize(264, 25);
		
		// Border around the regional filter fields
		filterBorder = new JLabel();
		filterBorder.setBorder(BorderFactory.createTitledBorder("Target Location"));
		filterBorder.setLocation(10, 150);
		filterBorder.setSize(380, 130);
		
		storeLabel = new JLabel("Store Name:");
		storeLabel.setSize(120, 25);
		storeLabel.setLocation(24, 176);
		
		storeText = new JTextField();
		storeText.setSize(274, 25);
		storeText.setLocation(102, 176);
		
		latitudeLabel = new JLabel("Latitude:");
		latitudeLabel.setSize(120, 25);
		latitudeLabel.setLocation(44, 206);
		
		latitudeText = new JTextField();
		latitudeText.setSize(260, 25);
		latitudeText.setLocation(102, 206);
		
		JLabel latDegree = new JLabel("°");
		latDegree.setSize(25, 25);
		latDegree.setLocation(366, 206);
		
		longitudeLabel = new JLabel("Longitude:");
		longitudeLabel.setSize(120, 25);
		longitudeLabel.setLocation(34, 236);
		
		longitudeText = new JTextField();
		longitudeText.setSize(260, 25);
		longitudeText.setLocation(102, 236);
		
		JLabel lonDegree = new JLabel("°");
		lonDegree.setSize(25, 25);
		lonDegree.setLocation(366, 236);
		
		radiusBorder = new JLabel();
		radiusBorder.setBorder(BorderFactory.createTitledBorder("Target Radius"));
		radiusBorder.setLocation(10, 290);
		radiusBorder.setSize(380, 70);
		
		radiusLabel = new JLabel("Radius:");
		radiusLabel.setSize(80, 25);
		radiusLabel.setLocation(32, 316);
		
		radiusText = new JTextField();
		radiusText.setSize(270, 25);
		radiusText.setLocation(82, 316);
		
		kmLabel = new JLabel("km");
		kmLabel.setSize(40, 25);
		kmLabel.setLocation(356, 316);
		
		/*southBox = new JCheckBox("South");
		southBox.setLocation(30, 170);
		southBox.setSize(100, 25);
		southBox.setSelected(true);
		
		northeastBox = new JCheckBox("Northeast");
		northeastBox.setLocation(30, 200);
		northeastBox.setSize(100, 25);
		
		midwestBox = new JCheckBox("Midwest");
		midwestBox.setLocation(30, 230);
		midwestBox.setSize(100, 25);
		
		northAtlanticBox = new JCheckBox("North Atlantic");
		northAtlanticBox.setLocation(130, 170);
		northAtlanticBox.setSize(110, 25);
		
		midAtlanticBox = new JCheckBox("Mid-Atlantic");
		midAtlanticBox.setLocation(130, 200);
		midAtlanticBox.setSize(100, 25);
		
		southwestBox = new JCheckBox("Southwest");
		southwestBox.setLocation(130, 230);
		southwestBox.setSize(100, 25);
		
		rockyMountainBox = new JCheckBox("Rocky Mountains");
		rockyMountainBox.setLocation(240, 170);
		rockyMountainBox.setSize(130, 25);
		
		pacificNorthwestBox = new JCheckBox("Pacific Northwest");
		pacificNorthwestBox.setLocation(240, 200);
		pacificNorthwestBox.setSize(140, 25);
		
		floridaBox = new JCheckBox("Florida");
		floridaBox.setLocation(240, 230);
		floridaBox.setSize(100, 25);*/
		
		filterBtn = new JButton("Filter");
		filterBtn.setLocation(288, 374);
		filterBtn.setSize(100, 25);
		
		this.add(trgText);
		this.add(radiusText);
		this.add(longitudeText);
		this.add(latitudeText);
		this.add(storeText);
		this.add(srcLabel);
		this.add(srcText);
		this.add(clean);
		this.add(srcBtn);
		this.add(trgLabel);
		this.add(trgKmlLabel);
		this.add(srcTrgBorder);
		this.add(filterBorder);
		this.add(filterBtn);
		this.add(radiusLabel);
		this.add(radiusBorder);
		this.add(longitudeLabel);
		this.add(latitudeLabel);
		this.add(storeLabel);
		this.add(kmLabel);
		this.add(lonDegree);
		this.add(latDegree);
		
		/*this.add(northeastBox);
		this.add(southBox);
		this.add(midwestBox);
		this.add(northAtlanticBox);
		this.add(midAtlanticBox);
		this.add(southwestBox);
		this.add(rockyMountainBox);
		this.add(pacificNorthwestBox);
		this.add(floridaBox);*/
	}
	
}
