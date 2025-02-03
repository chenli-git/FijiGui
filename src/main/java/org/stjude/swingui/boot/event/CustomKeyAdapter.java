package org.stjude.swingui.boot.event;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CustomKeyAdapter extends KeyAdapter {

    private final JSlider jSlider;

    public CustomKeyAdapter(JSlider jSlider) {
        this.jSlider = jSlider;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            JTextField textField = (JTextField) e.getSource();
            int value = Integer.parseInt(textField.getText());
            jSlider.setValue(value);
        } catch (NumberFormatException ne) {
        }
    }
}
