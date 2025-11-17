package org.stjude.swingui.boot.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import org.stjude.swingui.boot.event.ButtonListener;
import org.stjude.swingui.boot.event.ClickRecorder;
import org.stjude.swingui.boot.event.RecorderTableModel;

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
        // Register this panel with ButtonListener so it can check recording state
        ButtonListener.setProcessPanel(this);
    }

    //method to initialize the panel
    private void init(){
        //setup background color
        this.setBackground(Color.lightGray);


        //Added label and its respective fields with slider and text box for Features
        JLabel featuresLabel = addFirstLabel(this, new Rectangle(10, 5, 220, 20), "Modify Channel Features:", 14);
        //added elements to panel
        this.add(featuresLabel);
        addLabel(this, "UndoLast", "", new Rectangle(220, 5, 80, 20), 12);
        // add a revert button to unprocess image
        //addButton(this, "Revert", "reset to original setting", "revert", new Rectangle(200, 5, 55, 20), 12);

        //share a button for redo
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 30, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 55, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 80, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 135, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 160, 30, 20), 24);
        // addButton(this, "\u27F2", "Reverts last step", "undo", new Rectangle(275, 185, 30, 20), 24);

        addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 292, 5);
        //addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 290, 55);
        //addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 290, 80);
        //addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 290, 135);
        //addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 290, 160);
        //addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 290, 185);
        //addIconButton(this, "undo.png", "Revert last step, unavailable if image is too large.", "undo", 290, 210);

        //Blur
        addLabel(this, "Smooth", "Gaussian blur", new Rectangle(10, 30, 60, 20), 12);
        JTextField blurTextField = addTextField(this, "1.0", "XY radius in pixels. Z radius = 0.3*X", new Rectangle(240, 30, 50, 20), 12);
        //slider ints are later DIVIDED BY 2 to normalize into half unit increments
		addSlider(this, blurTextField, 16, 2, 2, sliderwidth, 80, 30);  // last two ints are x_pos, y_pos
        addTfButton(this, blurTextField, "OK", "Alters data", "smooth", new Rectangle(290, 30, 27, 20), 12);

        


        //median
        addLabel(this, "Denoise", "Median filter", new Rectangle(10, 55, 60, 25), 12);
        JTextField medianTextField = addTextField(this, "0.5", "Radius in pixels", new Rectangle(240, 55, 50, 20), 12);
        addSlider(this, medianTextField, 16, 1, 2, sliderwidth, 80, 55);
        addTfButton(this, medianTextField, "OK", "Alters data", "denoise", new Rectangle(290, 55, 27, 20), 12);

        //sharpen
        addLabel(this, "Sharpen", "Unsharp mask", new Rectangle(10, 80, 60, 20), 12);
        JTextField sharpenTextField = addTextField(this, "3.0", "Radius in pixels", new Rectangle(240, 80, 50, 20), 12);
        addSlider(this, sharpenTextField, 20, 2, 2, sliderwidth, 80, 80);
        addTfButton(this, sharpenTextField, "OK", "Alters data", "sharpen", new Rectangle(290, 80, 27, 20),12);

        //Added label and its respective fields with slider and text box for Intensities
        addLabel(this, "Modify Channel Intensities:", "", new Rectangle(10, 110, 220, 20), 14);

        // Sub Bkgd
        addLabel(this, "Sub. Bkgd", "Rolling ball. Larger values remove less background.", new Rectangle(10, 135, 80, 20), 12);
        JTextField subBkgdTextField = addTextField(this, "20.0", "Radius in pixels", new Rectangle(240, 135, 50, 20),12);
        addSlider(this, subBkgdTextField, 100, 20, 1, sliderwidth, 80, 135);
        addTfButton(this, subBkgdTextField, "OK", "Alters data", "subbkgd", new Rectangle(290, 135, 27, 20),12);

        //Gamma
        addLabel(this, "Gamma", "Accentuates dim signal", new Rectangle(10, 160, 70, 20), 12);
        JTextField gammaTextField = addTextField(this, "0.8", "0-1; smaller values equate to larger effect", new Rectangle(240, 160, 50, 20), 12);
        // This slider value will have to be further normalized to get doubles over range 0-1...
		addSlider(this, gammaTextField, 100, 20, 100, sliderwidth, 80, 160);
        addTfButton(this, gammaTextField, "OK", "Alters data", "gamma", new Rectangle(290, 160, 27, 20), 12);

        //Multiply
        addLabel(this, "Apply LUTs", "Apply LUTs", new Rectangle(10, 185, 90, 20), 12);
        //JTextField multiplyTextField = addTextField(this, "1.0", "0-10; fold change", new Rectangle(200, 185, 50, 20), 12);
        // This slider value will have to be further normalized to get doubles over range 0-1...
		//addSlider(this, multiplyTextField, 100, 10, 10, sliderwidth, 70, 185);
        //addTfButton(this, multiplyTextField, "OK", "Alters data", "multiply", new Rectangle(245, 185, 30, 20), 12);
        addTfButton(this, null, "ToCh", "Apply Luts to the active channel", "applyLutsToCh", new Rectangle(215, 185, 50, 20), 12);
        addTfButton(this, null, "ToAll", "Apply Luts to all channels", "applyLutsToAll", new Rectangle(265, 185, 50, 20), 12);
        //addTfButton(this, null, "OK", "Apply LUTs", "multiply", new Rectangle(250, 185, 25, 20), 12);

        // bleach correction
        addLabel(this, "Intensity Correction", "Bleach or scatter correction across z-slices", new Rectangle(10, 210, 150, 20), 12);
        addTfButton(this, null, "Global", "Systematic slice intensity adjustment", "global corr", new Rectangle(150, 210, 55, 20), 12);
        addTfButton(this, null, "Local", "Local slice intensity adjustment", "local corr", new Rectangle(205, 210, 50, 20), 12);
        addTfButton(this, null, "Equalize", "Makes slice intensities equal", "equalize corr", new Rectangle(255, 210, 60, 20), 12);

        


		// Intensity correction line
        //addLabel(this, "Correct Intensity:", "Automatic intensity correction along 3rd dimension", new Rectangle(10, 210, 150, 20), 14);
		
		//addButton(this, "Histo Match", "Best when image content remains similar across slices", "hmatch", new Rectangle(10, 235, 80, 20), 12);
	
		//addButton(this, "Exponential", "Best when image content differs across slices", "exponential", new Rectangle(95, 235, 80, 20), 12);


		// ---- Modify Dimensions Label ------- 
        addLabel(this, "Modify Dimensions:", "", new Rectangle(10, 240, 170, 20), 14);
        addButton(this, "Rotate", "Uses bilinear interpolation", "rotate", new Rectangle(165, 240, 55, 20), 12);
        addButton(this, "Crop", "Use Subset to crop in Z", "crop", new Rectangle(220, 240, 40, 20), 12);
        addButton(this, "Subset", "Reshape non-XY dimensions (ch,z,t)", "subset", new Rectangle(260, 240, 55, 20), 12);
        //addButton(this, "ChOrder", "Change channel order", "reorder", new Rectangle(260, 240, 55, 20), 12);
        // ---- record action listeners ----
        addLabel(this, "Record Actions:", "", new Rectangle(10, 265, 120, 20), 14);
        
        recToggleButton = new JToggleButton("REC");
        recToggleButton.setBounds(165, 265, 55, 20);
        recToggleButton.setFont(new Font("Calibri", Font.PLAIN, 12));
        recToggleButton.setMargin(new Insets(2, 2, 2, 2));
        recToggleButton.setBackground(Color.WHITE); // Default to stopped state
        recToggleButton.setOpaque(true);
        recToggleButton.setBorderPainted(false);
        recToggleButton.setToolTipText("Start recording actions");
        this.add(recToggleButton);
        
        addButton(this, "CLR", "Clear recorded history", "clear", new Rectangle(220, 265, 40, 20), 12);
        addButton(this, "SAVE", "Export the action table as CSV", "export", new Rectangle(260, 265, 55, 20), 12);
        //addButton(this,"RUN", "Run recorded actions", "run", new Rectangle(275, 265, 40, 20), 12);

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

        // **Add JTable for action recording**
        RecorderTableModel tableModel = new RecorderTableModel();
        JTable actionTable = new JTable(tableModel);
        
        // Customize table appearance
        actionTable.setFont(new Font("Monospaced", Font.PLAIN, 10));
        actionTable.setRowHeight(18);
        actionTable.setShowGrid(true);
        actionTable.setGridColor(Color.LIGHT_GRAY);
        actionTable.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11));
        
        // Set column widths
        actionTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // Channel
        actionTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Action
        actionTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Parameters
        
        // Center align the channel column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        actionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        // Create scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(actionTable);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setBounds(10, 290, 305, 90);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Action Table"));
        this.add(tableScrollPane);

        // Register the table model with ClickRecorder
        ClickRecorder.setTableModel(tableModel);



    }

    // Public method to access the toggle button state from another class
    public boolean isRecording() {
        return recToggleButton.isSelected();
    }


}
