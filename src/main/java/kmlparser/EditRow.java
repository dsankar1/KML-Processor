package kmlparser;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EditRow extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField valueText;
	private String name;
	
	public EditRow(String name) {
		this.name = name;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		JLabel nameLabel = new JLabel(name + ":");
		nameLabel.setAlignmentX(LEFT_ALIGNMENT);
		valueText = new JTextField();
		valueText.setAlignmentX(LEFT_ALIGNMENT);
		valueText.setPreferredSize(new Dimension(this.getWidth(), 25));
		this.add(nameLabel);
		this.add(valueText);
	}

	public void clearValue() {
		valueText.setText("");
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return valueText.getText().trim();
	}
	
}
