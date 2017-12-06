package kmlparser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.micromata.opengis.kml.v_2_2_0.Kml;

public class Clean extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private File source;
	private volatile boolean loading = false;
	private JLabel srcBorder, srcText;
	private JButton srcBtn, cleanBtn;
	
	public Clean() {
		this.setLayout(null);
		organizeUIComponents();
		setComponentEventListeners();
	}
	
	private void setComponentEventListeners() {
		final Clean parent = this;
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
		
		cleanBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (loading) return;
				if (srcText.getText().equals("")) {
					JOptionPane.showMessageDialog(parent,
						    "Please specify a source file.",
						    "Missing Field",
						    JOptionPane.ERROR_MESSAGE);
				} else {
					loading = true;
					Kml cleaned = KmlUtility.cleanKml(Kml.unmarshal(source));
					File cleanedFile = new File(source.getParent(), "Clean_" + source.getName());
					try {
						KmlUtility.marshalWithoutPrefix(cleaned, cleanedFile, false);
						Runtime.getRuntime().exec("explorer.exe /select," + cleanedFile.getPath());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(parent,
							    "Unable to create " + cleanedFile.getName(),
							    "File Creation Error",
							    JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					} finally {
						loading = false;
					}
				}
			}
			
		});
	}

	private void organizeUIComponents() {
		srcBorder = new JLabel();
		srcBorder.setBorder(BorderFactory.createTitledBorder("Source"));
		srcBorder.setLocation(10, 110);
		srcBorder.setSize(380, 85);
		
		srcText = new JLabel();
		srcText.setSize(310, 25);
		srcText.setLocation(30, 145);
		srcText.setBorder(BorderFactory.createEtchedBorder());
		
		srcBtn = new JButton("Browse");
		srcBtn.setLocation(346, 145);
		srcBtn.setSize(24, 24);
		
		cleanBtn = new JButton("Clean");
		cleanBtn.setLocation(290, 280);
		cleanBtn.setSize(100, 25);
		
		this.add(srcBorder);
		this.add(srcText);
		this.add(srcBtn);
		this.add(cleanBtn);
	}
	
}
