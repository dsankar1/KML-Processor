package kmlparser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.micromata.opengis.kml.v_2_2_0.Kml;

public class KMZ extends JPanel {

	private static final long serialVersionUID = 1L;

	private volatile boolean loading = false;
	private JLabel kmzBorder, srcLabel, srcText, imgLabel, imgText;
	private JButton srcBtn, imgBtn, createBtn;
	private JCheckBox clean;
	private File source, image;
	
	public KMZ() {
		this.setLayout(null);
		organizeUIComponents();
		setComponentEventListeners();
	}
	
	private void setComponentEventListeners() {
		final KMZ parent = this;
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
		
		imgBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (loading) return;
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "png", "jpg");
				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(System.getProperty("user.home")));
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(filter);
				int returnVal = fc.showOpenDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
	                image = fc.getSelectedFile();
	                imgText.setText(image.getName());   
	            }
			}
			
		});
		
		createBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (loading) return;
				if (srcText.getText().equals("")) {
					JOptionPane.showMessageDialog(parent,
						    "Please specify a source file.",
						    "Missing Field",
						    JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						File zip;
						if (clean.isSelected()) {
							final Kml clean = KmlUtility.cleanKml(Kml.unmarshal(source));
							File temp = new File(source.getParent(), "TEMP_" + source.getName());
							KmlUtility.marshalWithoutPrefix(clean, temp, false);
							String zipName = source.getName().substring(0, source.getName().lastIndexOf(".")) + ".kmz";
							zip = KmlUtility.createKmz(temp, image, zipName);
							temp.delete();
						} else {
							String zipName = source.getName().substring(0, source.getName().lastIndexOf(".")) + ".kmz";
							zip = KmlUtility.createKmz(source, image, zipName);
						}
						Runtime.getRuntime().exec("explorer.exe /select," + zip.getPath());
					} catch(Exception ex) {
						ex.printStackTrace();
					} finally {
						loading = false;
					}
				}
			}
			
		});
	}
	
	private void organizeUIComponents() {
		kmzBorder = new JLabel();
		kmzBorder.setBorder(BorderFactory.createTitledBorder("KMZ Resources"));
		kmzBorder.setLocation(10, 10);
		kmzBorder.setSize(380, 130);
		
		srcLabel = new JLabel("KML:");
		srcLabel.setLocation(40, 40);
		srcLabel.setSize(50, 25);
		
		clean = new JCheckBox("Normalize KML");
		clean.setSize(120, 25);
		clean.setLocation(264, 100);
		
		srcText = new JLabel();
		srcText.setLocation(74, 40);
		srcText.setSize(264, 25);
		srcText.setBorder(BorderFactory.createEtchedBorder());
		
		srcBtn = new JButton("Browse");
		srcBtn.setLocation(346, 40);
		srcBtn.setSize(24, 24);
		
		imgLabel = new JLabel("Image:");
		imgLabel.setLocation(30, 70);
		imgLabel.setSize(50, 25);
		
		imgText = new JLabel();
		imgText.setLocation(74, 70);
		imgText.setSize(264, 25);
		imgText.setBorder(BorderFactory.createEtchedBorder());
		
		imgBtn = new JButton("Browse");
		imgBtn.setLocation(346, 70);
		imgBtn.setSize(24, 24);
		
		createBtn = new JButton("Create");
		createBtn.setLocation(288, 374);
		createBtn.setSize(100, 25);
		
		this.add(srcLabel);
		this.add(clean);
		this.add(srcText);
		this.add(srcBtn);
		this.add(imgLabel);
		this.add(imgText);
		this.add(imgBtn);
		this.add(kmzBorder);
		this.add(createBtn);
	}
	
}
