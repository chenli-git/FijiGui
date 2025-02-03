package org.stjude.swingui.boot.panel;

import org.stjude.swingui.boot.event.*;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException; //thrown by URL


// Builds all common gui elements according to parameters in abstract so other panels can reuse this code
 
public abstract class BasePanel extends JPanel {

    protected JCheckBox addCheckBox(JPanel panelName, String tooltip, int xAxis, int yAxis) {
        // Add checkbox
        JCheckBox checkBox = new JCheckBox("");
        checkBox.setBounds(new Rectangle(xAxis, yAxis, 20, 20));
        checkBox.setToolTipText(tooltip);
        panelName.add(checkBox);
		return checkBox; 
    }

    protected void addSlider(JPanel panelName, JTextField textField, int maximumValue, int iniValue, int scale, int width, int xAxis, int yAxis) {
        //Add Slider - this is a generic slider. The 'gainSlider' naming is historical artifact
        JSlider gainSlider = new JSlider();
        gainSlider.setMaximum(maximumValue);  // slider only stores ints, so if you want fractions have to divide the int that the slider returns
        gainSlider.setValue(iniValue);
        gainSlider.setBounds(new Rectangle(xAxis, yAxis, width, 20));
		gainSlider.putClientProperty("scale", new Integer(scale)); // a key value pair OF OBJECTS used to scale slider ints into relevant units.  Interger() creates an integer object.
        //gainSlider.setToolTipText(tooltip);
        panelName.add(gainSlider);
        gainSlider.addChangeListener(new CustomChangeListener(textField));  // permits setting of textfield based on slider
        textField.addKeyListener(new CustomKeyAdapter(gainSlider)); // permits setting of slider based on textfield
    }

    protected void addLabel(JPanel panelName, String textField, String tooltip, Rectangle rectangle, int fontSize) {
        // add Label with overridden Font and font size
        JLabel l2 = new JLabel();
        l2.setText(textField);
        l2.setBounds(rectangle);
        l2.setFont(new Font("Calibri", Font.BOLD, fontSize));
        l2.setToolTipText(tooltip);
        panelName.add(l2);
    }

	// ---Named buttons---
    protected void addButton(JPanel panelName, String text, String tooltip, String id, Rectangle rectangle, int fontSize) {
        // Add action button with overridden font and font size
        JButton actionButton = new JButton();
        actionButton.setText(text);
        actionButton.setBounds(rectangle);
		actionButton.setMargin(new Insets(2, 2, 2, 2)); // new
        actionButton.setFont(new Font("Calibri", Font.PLAIN, fontSize));
        actionButton.setToolTipText(tooltip);
		actionButton.putClientProperty("ID", id); // a unique ID to identify the button
        actionButton.addActionListener(new ButtonListener());
        panelName.add(actionButton);
    }
	
	// ---TextField buttons---
		// Buttons that are also passed a corresponding JTextField so their event response can include the text data value
    protected void addTfButton(JPanel panelName, JTextField textField, String label, String tooltip, String id, Rectangle rectangle, int fontSize) {
        // Add action button with overridden font and font size
        JButton actionButton = new JButton();
        actionButton.setText(label);
        actionButton.setBounds(rectangle);
		actionButton.setMargin(new Insets(2, 2, 2, 2)); // Smaller margins than default
        actionButton.setFont(new Font("Calibri", Font.PLAIN, fontSize));
        actionButton.setToolTipText(tooltip);
		actionButton.putClientProperty("ID", id); 
        actionButton.addActionListener(new TfButtonListener(textField));
        panelName.add(actionButton);
    }

	// ---Two TextField buttons---
		// Buttons that are also passed a corresponding JTextField so their event response can include the text data value
    protected void addTwoTfButton(JPanel panelName, JTextField textField1, JTextField textField2, String label, String tooltip, String id, Rectangle rectangle, int fontSize) {
        // Add action button with overridden font and font size
        JButton actionButton = new JButton();
        actionButton.setText(label);
        actionButton.setBounds(rectangle);
		actionButton.setMargin(new Insets(2, 2, 2, 2)); // Smaller margins than default
        actionButton.setFont(new Font("Calibri", Font.PLAIN, fontSize));
        actionButton.setToolTipText(tooltip);
		actionButton.putClientProperty("ID", id); 
        actionButton.addActionListener(new TfButtonListener(textField1, textField2));
        panelName.add(actionButton);
    }

	// ---Checkbox buttons---
		// Buttons that are also passed a corresponding CheckBox so their event response can include the checkbox state
    protected void addCbButton(JPanel panelName, JCheckBox checkBox, String label, String tooltip, String id, Rectangle rectangle, int fontSize) {
        // Add action button with overridden font and font size
        JButton actionButton = new JButton();
        actionButton.setText(label);
        actionButton.setBounds(rectangle);
		actionButton.setMargin(new Insets(2, 2, 2, 2)); // Smaller margins than default
        actionButton.setFont(new Font("Calibri", Font.PLAIN, fontSize));
        actionButton.setToolTipText(tooltip);
		actionButton.putClientProperty("ID", id); 
        actionButton.addActionListener(new CbButtonListener(checkBox));
        panelName.add(actionButton);
    }

	// ---Icon buttons---
    protected void addIconButton(JPanel panelName, String iconfilename, String tooltip, String id, int xAxis, int yAxis) {
        //opens an icon image
		URL imageURL = BasePanel.class.getClassLoader().getResource("icons/"+iconfilename); // path relative to location of BasePanel
		if (imageURL == null) {
            throw new IllegalArgumentException("Icon file not found: icons/" + iconfilename);
        }
        ImageIcon icon = new ImageIcon(imageURL);  // Caution - no error catching on open...
		
        //rectangle to fit icon
		Rectangle rectangle = new Rectangle(xAxis, yAxis, 24, 24); // icon image is 24 x 24
		// Add Button with icon face
        JButton b = new JButton((Icon) icon);
        b.setBounds(rectangle);
        b.setBorderPainted(true);
        b.setToolTipText(tooltip);
		b.putClientProperty("ID", id); 
        b.addActionListener(new ButtonListener());
		panelName.add(b);
    }


    protected JLabel addFirstLabel(JPanel panelName, Rectangle rectangle, String labelText, int fontSize) {
        //Add label
        JLabel l1 = new JLabel();
        l1.setText(labelText);
        l1.setBounds(rectangle);
        l1.setFont(new Font("Calibri", Font.BOLD, fontSize));
        panelName.setLayout(null);
        return l1;
    }


    protected JTextField addTextField(JPanel panelName, String textFieldValue, String tooltip, Rectangle rectangle, int fontSize) {
        // Add text field
        JTextField textField = new JTextField(textFieldValue);
        textField.setBounds(rectangle);
        textField.setFont(new Font("Calibri", Font.BOLD, fontSize));
		textField.setEditable(false); // User input not allowed to avoid validation
        textField.setToolTipText(tooltip);
        panelName.add(textField);
        return textField;
    }
}
