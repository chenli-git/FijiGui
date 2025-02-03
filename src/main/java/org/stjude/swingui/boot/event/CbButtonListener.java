package org.stjude.swingui.boot.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.stjude.swingui.boot.proc.*;

public class CbButtonListener implements ActionListener {
	
	SaveButtons sb;
	double param; // the checkbox state
	
	private final JCheckBox checkBox;
	
    public CbButtonListener(JCheckBox checkBox) {
		this.checkBox = checkBox; 
		boolean cbstate = checkBox.isSelected(); // tells state of checkbox
		param = (cbstate == true) ? 1 : 0; // converts boolean to 1 if checked or 0 if unchecked
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
		// Gets button ID
		JButton button = (JButton) actionEvent.getSource();
		String id = (String) button.getClientProperty("ID"); 

		// Calls a response based on button ID
		switch (id) {
			case "stif": 
				sb = new SaveButtons();
				sb.tiff(param); 
				break;
				
		}			
    }
}
