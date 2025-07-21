package org.stjude.swingui.boot.panel;

import javax.swing.*;

import org.checkerframework.checker.units.qual.t;
import org.stjude.swingui.boot.event.ClickRecorder;

import java.awt.*;


/**
 * This class hold second tab "process" paint logic.
 * Panel is getting invoked by init() method, which is called from constructor
 */
public class ProcessPanel extends BasePanel {
	
	int sliderwidth = 130;
    private JToggleButton recToggleButton; // Store as instance variable
    //constructor
    public ProcessPanel() {
        init();
    }

    //method to initialize the panel
    private void init(){
        //setup background color
        this.setBackground(Color.lightGray);


        //Added label and its respective fields with slider and text box for Features
        JLabel featuresLabel = addFirstLabel(this, new Rectangle(10, 5, 200, 20), "Modify Channel Features:", 14);
        //added elements to panel
        this.add(featuresLabel);

        // add a revert button to unprocess image
        //addButton(this, "Revert", "reset to original setting", "revert", new Rectangle(200, 5, 55, 20), 12);

        //share a button for redo
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 30, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 55, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 80, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 135, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 160, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 185, 30, 20), 24);

        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 30);
        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 55);
        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 80);
        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 135);
        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 160);
        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 185);
        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 275, 210);

        //Blur
        addLabel(this, "Smooth", "Gaussian blur", new Rectangle(10, 30, 50, 20), 12);
        JTextField blurTextField = addTextField(this, "1.0", "XY radius in pixels. Z radius = 0.3*X", new Rectangle(200, 30, 50, 20), 12);
        //slider ints are later DIVIDED BY 2 to normalize into half unit increments
		addSlider(this, blurTextField, 16, 2, 2, sliderwidth, 70, 30);  // last two ints are x_pos, y_pos
        addTfButton(this, blurTextField, "OK", "Alters data", "smooth", new Rectangle(250, 30, 25, 20), 12);

        


        //median
        addLabel(this, "Denoise", "Median filter", new Rectangle(10, 55, 50, 20), 12);
        JTextField medianTextField = addTextField(this, "0.5", "Radius in pixels", new Rectangle(200, 55, 50, 20), 12);
        addSlider(this, medianTextField, 16, 1, 2, sliderwidth, 70, 55);
        addTfButton(this, medianTextField, "OK", "Alters data", "denoise", new Rectangle(250, 55, 25, 20), 12);

        //sharpen
        addLabel(this, "Sharpen", "Unsharp mask", new Rectangle(10, 80, 60, 20), 12);
        JTextField sharpenTextField = addTextField(this, "3.0", "Radius in pixels", new Rectangle(200, 80, 50, 20), 12);
        addSlider(this, sharpenTextField, 20, 2, 2, sliderwidth, 70, 80);
        addTfButton(this, sharpenTextField, "OK", "Alters data", "sharpen", new Rectangle(250, 80, 25, 20),12);

        //Added label and its respective fields with slider and text box for Intensities
        addLabel(this, "Modify Channel Intensities:", "", new Rectangle(10, 110, 200, 20), 14);

        // Sub Bkgd
        addLabel(this, "Sub. Bkgd", "Rolling ball. Larger values remove less background.", new Rectangle(10, 135, 80, 20), 12);
        JTextField subBkgdTextField = addTextField(this, "20.0", "Radius in pixels", new Rectangle(200, 135, 50, 20),12);
        addSlider(this, subBkgdTextField, 100, 20, 1, sliderwidth, 70, 135);
        addTfButton(this, subBkgdTextField, "OK", "Alters data", "subbkgd", new Rectangle(250, 135, 25, 20),12);

        //Gamma
        addLabel(this, "Gamma", "Accentuates dim signal", new Rectangle(10, 160, 70, 20), 12);
        JTextField gammaTextField = addTextField(this, "0.8", "0-1; smaller values equate to larger effect", new Rectangle(200, 160, 50, 20), 12);
        // This slider value will have to be further normalized to get doubles over range 0-1...
		addSlider(this, gammaTextField, 100, 20, 100, sliderwidth, 70, 160);
        addTfButton(this, gammaTextField, "OK", "Alters data", "gamma", new Rectangle(250, 160, 25, 20), 12);

        //Multiply
        addLabel(this, "Apply LUTs", "Apply LUTs", new Rectangle(10, 185, 90, 20), 12);
        //JTextField multiplyTextField = addTextField(this, "1.0", "0-10; fold change", new Rectangle(200, 185, 50, 20), 12);
        // This slider value will have to be further normalized to get doubles over range 0-1...
		//addSlider(this, multiplyTextField, 100, 10, 10, sliderwidth, 70, 185);
        //addTfButton(this, multiplyTextField, "OK", "Alters data", "multiply", new Rectangle(245, 185, 30, 20), 12);
        addTfButton(this, null, "ToCh", "Apply Luts to the active channel", "applyLutsToCh", new Rectangle(200, 185, 38, 20), 12);
        addTfButton(this, null, "ToAll", "Apply Luts to all channels", "applyLutsToAll", new Rectangle(238, 185, 37, 20), 12);
        //addTfButton(this, null, "OK", "Apply LUTs", "multiply", new Rectangle(250, 185, 25, 20), 12);

        // bleach correction
        addLabel(this, "Intensity Correction", "Bleach or scatter correction across z-slices", new Rectangle(10, 210, 125, 20), 12);
        addTfButton(this, null, "Global", "Systematic slice intensity adjustment", "global corr", new Rectangle(140, 210, 45, 20), 12);
        addTfButton(this, null, "Local", "Local slice intensity adjustment", "local corr", new Rectangle(185, 210, 40, 20), 12);
        addTfButton(this, null, "Equalize", "Makes slice intensities equal", "equalize corr", new Rectangle(225, 210, 50, 20), 12);

        


		// Intensity correction line
        //addLabel(this, "Correct Intensity:", "Automatic intensity correction along 3rd dimension", new Rectangle(10, 210, 150, 20), 14);
		
		//addButton(this, "Histo Match", "Best when image content remains similar across slices", "hmatch", new Rectangle(10, 235, 80, 20), 12);
	
		//addButton(this, "Exponential", "Best when image content differs across slices", "exponential", new Rectangle(95, 235, 80, 20), 12);


		// ---- Modify Dimensions Label ------- 
        addLabel(this, "Modify Dimensions:", "", new Rectangle(10, 240, 150, 20), 14);
        addButton(this, "Rotate", "Uses bilinear interpolation", "rotate", new Rectangle(175, 240, 45, 20), 12);
        addButton(this, "Crop", "Use Subset to crop in Z", "crop", new Rectangle(220, 240, 35, 20), 12);
        addButton(this, "Subset", "Reshape non-XY dimensions (ch,z,t)", "subset", new Rectangle(255, 240, 45, 20), 12);
        //addButton(this, "ChOrder", "Change channel order", "reorder", new Rectangle(260, 240, 55, 20), 12);
        // ---- record action listeners ----
        addLabel(this, "Record Actions:", "", new Rectangle(10, 265, 120, 20), 14);
        //addButton(this, "REC", "Save recorded clicks", "save", new Rectangle(10, 330, 40, 20), 12);
        addButton(this, "CLR", "Clear recorded history", "clear", new Rectangle(40, 345, 30, 20), 12);
        addButton(this, "SAVE", "Export the action list", "export", new Rectangle(70, 345, 35, 20), 12);
        //addButton(this,"RUN", "Run recorded actions", "run", new Rectangle(130, 330, 45, 20), 12);

        recToggleButton = new JToggleButton("REC");
        recToggleButton.setBounds(10, 345, 30, 20);
        recToggleButton.setFont(new Font("Calibri", Font.PLAIN, 12));
        recToggleButton.setMargin(new Insets(2, 2, 2, 2));
        recToggleButton.setBackground(Color.WHITE); // Default to stopped state
        recToggleButton.setOpaque(true);
        recToggleButton.setBorderPainted(false);
        recToggleButton.setToolTipText("Start recording actions");
        this.add(recToggleButton);

        // Add event listener to toggle color
        recToggleButton.addItemListener(e -> {
            if (recToggleButton.isSelected()) {
                recToggleButton.setBackground(new Color(255, 222, 222)); // Recording ON
                //recToggleButton.setBackground(Color.RED); // Recording ON
                //Color.WHITE
                recToggleButton.setText("ON");
                System.out.println("Recording started...");
            } else {
                recToggleButton.setBackground(Color.WHITE); // Recording OFF
                recToggleButton.setText("REC");
                System.out.println("Recording stopped.");
            }
        });

        JLabel filesLabel = addFirstLabel(this, new Rectangle(180, 250, 120, 20), "Actions:", 14);
        //this.add(filesLabel);
        JTextArea actionHistory = addTextArea(this, "", "", new Rectangle(10, 285, 290, 55), 12);
        actionHistory.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(actionHistory);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(10, 285, 290, 55);
        this.add(scrollPane);

        ClickRecorder.setTextArea(actionHistory);
    }

    // Public method to access the toggle button state from another class
    public boolean isRecording() {
        return recToggleButton.isSelected();
    }


}
