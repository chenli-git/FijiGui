package org.stjude.swingui.boot.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.stjude.swingui.boot.proc.*;


public class TfButtonListener implements ActionListener {
	
	double param1, param2;
	JTextField jTextField1, jTextField2;
	JCheckBox checkBox;
	
	// For each instance of this class only one contrustor is used
    public TfButtonListener(JTextField jTextField1) {
		// Initializes vars and objects
		this.jTextField1 = jTextField1; // a specific text field paired with a specific OK button
		this.jTextField2 = null; // not used in context of this constructor
		param1 = 0; // will be assigned a double corresponding to the string in the textfield WHEN the OK button is pressed
		param2 = -1; // not used in context of this constructor
    }

    public TfButtonListener(JTextField jTextField1, JTextField jTextField2) {
		this.jTextField1 = jTextField1; // a specific text field paired with a specific OK button
		this.jTextField2 = jTextField2; // a specific text field paired with a specific OK button
		param1 = 0; // will be assigned a double corresponding to the string in the textfield WHEN the OK button is pressed
		param2 = 0; // will be assigned a double corresponding to the string in the textfield WHEN the OK button is pressed
    }

    public TfButtonListener(JCheckBox checkBox) {
		this.checkBox = checkBox;
		param1 = 0; // will encode checkbox state
		param2 = -1; // not used in context of this constructor
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
		// Gets button ID
		JButton button = (JButton) actionEvent.getSource();
		String id = (String) button.getClientProperty("ID"); 
		
		// Declares proc classes called
		ModifySliders ms;
		SaveButtons sb;

		// Calls a response based on button ID
		switch (id) {
			// logic for Process panel
			case "smooth": 
				param1 = Double.valueOf(jTextField1.getText());  // value of this textfield WHEN the OK button is pressed
				ms = new ModifySliders();
				ms.smooth(param1);
				break;
				
			case "denoise":
				param1 = Double.valueOf(jTextField1.getText()); 
				ms = new ModifySliders();
				ms.denoise(param1);
				break;			
				
			case "sharpen":
				param1 = Double.valueOf(jTextField1.getText()); 
				ms = new ModifySliders();
				ms.sharpen(param1);
				break;
				
			case "subbkgd":
				param1 = Double.valueOf(jTextField1.getText()); 
				ms = new ModifySliders();
				ms.subtBkgd(param1);
				break;
				
			case "gamma":
				param1 = Double.valueOf(jTextField1.getText()); 
				ms = new ModifySliders();
				ms.gamma(param1);
				break;					
				
			case "multiply":
				param1 = Double.valueOf(jTextField1.getText()); 
				ms = new ModifySliders();
				ms.multiply(param1);
				break;
			
			// logic for Save panel
			case "stif":
				boolean state = checkBox.isSelected();
				param1 = state ? 1 : 0; // ternary if/then.  Converts logical to binary (double) to avoid passing around even more variable types
				sb =  new SaveButtons();
				sb.tiff(param1);
				break;	
			
			case "sjpg":
				param1 = Double.valueOf(jTextField1.getText()); 
				sb =  new SaveButtons();
				sb.jpeg(param1);
				break;					
			
			case "smovie":
				param1 = Double.valueOf(jTextField1.getText()); 
				param2 = Double.valueOf(jTextField2.getText()); 
				sb =  new SaveButtons();
				sb.movie(param1, param2);
				
		}			
    }
}
