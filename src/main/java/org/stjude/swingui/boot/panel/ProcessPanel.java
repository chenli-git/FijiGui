package org.stjude.swingui.boot.panel;

import javax.swing.*;
import java.awt.*;

/**
 * This class hold second tab "process" paint logic.
 * Panel is getting invoked by init() method, which is called from constructor
 */
public class ProcessPanel extends BasePanel {
	
	int sliderwidth = 130;

    //constructor
    public ProcessPanel() {
        init();
    }

    //method to initialize the panel
    private void init(){
        //setup background color
        this.setBackground(Color.lightGray);

        //Added label and its respective fields with slider and text box for Features
        JLabel featuresLabel = addFirstLabel(this, new Rectangle(10, 5, 150, 20), "Modify Features:", 14);

        //Blur
        addLabel(this, "Smooth:", "Gaussian blur", new Rectangle(10, 30, 50, 20), 12);
        JTextField blurTextField = addTextField(this, "1.0", "XY radius in pixels. Z radius = 0.3*X", new Rectangle(200, 30, 50, 20), 12);
        //slider ints are later DIVIDED BY 2 to normalize into half unit increments
		addSlider(this, blurTextField, 16, 2, 2, sliderwidth, 70, 30);  // last two ints are x_pos, y_pos
        addTfButton(this, blurTextField, "OK", "Alters data", "smooth", new Rectangle(245, 30, 30, 20), 12);

        //median
        addLabel(this, "Denoise:", "Median filter", new Rectangle(10, 55, 50, 20), 12);
        JTextField medianTextField = addTextField(this, "0.5", "Radius in pixels", new Rectangle(200, 55, 50, 20), 12);
        addSlider(this, medianTextField, 16, 1, 2, sliderwidth, 70, 55);
        addTfButton(this, medianTextField, "OK", "Alters data", "denoise", new Rectangle(245, 55, 30, 20), 12);

        //sharpen
        addLabel(this, "Sharpen:", "Unsharp mask", new Rectangle(10, 80, 60, 20), 12);
        JTextField sharpenTextField = addTextField(this, "3.0", "Radius in pixels", new Rectangle(200, 80, 50, 20), 12);
        addSlider(this, sharpenTextField, 32, 6, 2, sliderwidth, 70, 80);
        addTfButton(this, sharpenTextField, "OK", "Alters data", "sharpen", new Rectangle(245, 80, 30, 20),12);

        //Added label and its respective fields with slider and text box for Intensities
        addLabel(this, "Modify Intensities:", "", new Rectangle(10, 110, 150, 20), 14);

        // Sub Bkgd
        addLabel(this, "Sub. Bkgd.:", "Rolling ball with parabola", new Rectangle(10, 135, 80, 20), 12);
        JTextField subBkgdTextField = addTextField(this, "20.0", "Radius in pixels", new Rectangle(200, 135, 50, 20),12);
        addSlider(this, subBkgdTextField, 100, 20, 1, sliderwidth, 70, 135);
        addTfButton(this, subBkgdTextField, "OK", "Alters data", "subbkgd", new Rectangle(245, 135, 30, 20),12);

        //Gamma
        addLabel(this, "Gamma:", "Accentuates dim signal", new Rectangle(10, 160, 70, 20), 12);
        JTextField gammaTextField = addTextField(this, "0.8", "0-1; smaller values equate to larger effect", new Rectangle(200, 160, 50, 20), 12);
        // This slider value will have to be further normalized to get doubles over range 0-1...
		addSlider(this, gammaTextField, 100, 20, 100, sliderwidth, 70, 160);
        addTfButton(this, gammaTextField, "OK", "Alters data", "gamma", new Rectangle(245, 160, 30, 20), 12);

        //Multiply
        addLabel(this, "Multiply:", "Multiplies pixele values", new Rectangle(10, 185, 70, 20), 12);
        JTextField multiplyTextField = addTextField(this, "1.0", "0-10; fold change", new Rectangle(200, 185, 50, 20), 12);
        // This slider value will have to be further normalized to get doubles over range 0-1...
		addSlider(this, multiplyTextField, 100, 10, 10, sliderwidth, 70, 185);
        addTfButton(this, multiplyTextField, "OK", "Alters data", "multiply", new Rectangle(245, 185, 30, 20), 12);

		// Intensity correction line
        addLabel(this, "Correct Intensity:", "Automatic intensity correction along 3rd dimension", new Rectangle(10, 210, 120, 20), 12);
		
		addButton(this, "Histo Match", "Best when image content remains similar across slices", "hmatch", new Rectangle(10, 235, 80, 20), 12);
	
		addButton(this, "Exponential", "Best when image content differs across slices", "exponential", new Rectangle(95, 235, 80, 20), 12);


		// ---- Modify Dimensions Label ------- 
        addLabel(this, "Modify Dimensions:", "", new Rectangle(10, 260, 150, 20), 14);

        addButton(this, "Rotate", "Uses bilinear interpolation", "rotate", new Rectangle(10, 285, 50, 20), 12);

        addButton(this, "Crop", "Use 'Make Subset' to crop in Z", "crop", new Rectangle(60, 285, 40, 20), 12);

        addButton(this, "Make Subset", "Reshape non-XY dimensions (ch,z,t)", "subset", new Rectangle(100, 285, 80, 20), 12);

        //added elements to panel
        this.add(featuresLabel);
    }
}
