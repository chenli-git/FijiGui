package org.stjude.swingui.boot.event;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CustomChangeListener implements ChangeListener {

    private final JTextField jTextField;

    public CustomChangeListener(JTextField jTextField) {
        this.jTextField = jTextField;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider) e.getSource();
        int value = slider.getValue();
		
		// Converts int from slider into a scaled unit
		Integer s = (Integer) slider.getClientProperty("scale");  // returns an object in general.  Casting to specific object type is required
		int scale = s.intValue(); // converts integer object to int
		double sval = value/(new Double(scale));  // must cast int to double so result will be double
        
		// Reversal of units for 'gamma' slider...
		// FIX: Non-ideal since scale is not nec a unique ID, but it saves having to define yet another ClientProperty
		if (scale == 100) {
			sval = Math.round((1-sval)*100)/100d; //rounding a double
		}			
		
		// Only output of a slider is to update the corresponding text field...
		jTextField.setText(String.valueOf(sval));
    }
}
