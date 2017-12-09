package kmlparser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class Edit extends JPanel {

	private static final long serialVersionUID = 1L;

	private volatile boolean loading = false;
	private JLabel kmzBorder, srcLabel, srcText;
	private JButton srcBtn, addBtn, deleteBtn;
	private JScrollPane formPane;
	private JPanel formPanel;
	private File source;
	private Kml kml;
	private List<EditRow> extendedData;
	private EditRow name, latitude, longitude;
	
	public Edit() {
		name = new EditRow("Name");
		latitude = new EditRow("Latitude");
		longitude = new EditRow("Longitude");
		extendedData = new ArrayList<EditRow>();
		this.setLayout(null);
		organizeUIComponents();
		setComponentEventListeners();
	}
	
	private void setComponentEventListeners() {
		final Edit parent = this;
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
					loading = true;
	                source = fc.getSelectedFile();
	                srcText.setText(source.getName());
	                try {
	                	kml = Kml.unmarshal(source);
		                List<String> dataNames = KmlUtility.getPlacemarkExtendedDataNames(kml);
		                extendedData.clear();
		                formPanel.removeAll();
		                if (dataNames != null) {
		                	formPanel.add(name);
		                	formPanel.add(latitude);
		                	formPanel.add(longitude);
		                	for (String dataName : dataNames) {
			                	EditRow row = new EditRow(dataName);
			                	extendedData.add(row);
			                	formPanel.add(row);
			                }
		                }
	                } catch(Exception ex) {
	    				ex.printStackTrace();
	                } finally {
	                	loading = false;
	                }
	            }
			}
			
		});
		
		addBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (kml == null || loading) return;
				loading = true;
				Document document = (Document) kml.getFeature();
				List<Feature> placemarks = document.getFeature();
				Placemark placemark = KmlFactory.createPlacemark();
				placemark.setName(name.getValue());
				placemark.setOpen(true);
				placemark.setStyleUrl("#Style1");
				Double lonNum = null, latNum = null;
				try {
					lonNum = Double.parseDouble(longitude.getValue());
					latNum = Double.parseDouble(latitude.getValue());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (lonNum == null || latNum == null) {
					JOptionPane.showMessageDialog(parent,
						    "Invalid entry for longitude/latitude",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
					loading = false;
					return;
				}
				placemark.createAndSetPoint().addToCoordinates(lonNum, latNum);
				ExtendedData exd = KmlFactory.createExtendedData();
				for (EditRow data : extendedData) {
					Data temp = KmlFactory.createData(data.getName());
					temp.setName(data.getName());
					temp.setValue(data.getValue());
					exd.addToData(temp);
				}
				placemark.setExtendedData(exd);
				placemarks.add(placemark);
				document.setFeature(placemarks);
				boolean success;
				try {
					KmlUtility.marshalWithoutPrefix(kml, source, false);
					success = true;
				} catch (Exception ex) {
					ex.printStackTrace();
					success = false;
				}
				if (success) {
					JOptionPane.showMessageDialog(parent,
						    "Entry has successfully been added to " + source.getName(),
						    "Successful Entry",
						    JOptionPane.INFORMATION_MESSAGE);
					for (EditRow data : extendedData) {
						data.clearValue();
					}
					name.clearValue();
					latitude.clearValue();
					longitude.clearValue();
				} else {
					JOptionPane.showMessageDialog(parent,
						    "Could not write entry to " + source.getName(),
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
				}
				loading = false;
			}
			
		});
		
		deleteBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (kml == null || loading) return;
				
			}
			
		});
	}
	
	private void organizeUIComponents() {
		kmzBorder = new JLabel();
		kmzBorder.setBorder(BorderFactory.createTitledBorder("KML Source"));
		kmzBorder.setLocation(10, 10);
		kmzBorder.setSize(380, 78);
		
		srcLabel = new JLabel("Source:");
		srcLabel.setLocation(24, 40);
		srcLabel.setSize(50, 25);
		
		srcText = new JLabel();
		srcText.setLocation(74, 40);
		srcText.setSize(264, 25);
		srcText.setBorder(BorderFactory.createEtchedBorder());
		
		srcBtn = new JButton("Browse");
		srcBtn.setLocation(346, 40);
		srcBtn.setSize(24, 24);
		
		addBtn = new JButton("Add");
		addBtn.setLocation(178, 374);
		addBtn.setSize(100, 25);
		
		deleteBtn = new JButton("Remove");
		deleteBtn.setLocation(288, 374);
		deleteBtn.setSize(100, 25);
		
		formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		
		formPane = new JScrollPane(formPanel);
		formPane.setBounds(10, 100, 380, 260);
		formPane.setBorder(BorderFactory.createTitledBorder("Form"));
		formPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		formPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.add(srcLabel);
		this.add(srcText);
		this.add(srcBtn);
		this.add(kmzBorder);
		this.add(addBtn);
		this.add(deleteBtn);
		this.add(formPane);
	}
	
}
