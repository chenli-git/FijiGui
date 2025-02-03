package org.stjude.swingui.boot.panel;

import javax.swing.*;
import java.awt.*;

import org.stjude.swingui.boot.proc.Contrast; // the Contrast class is a Listener for the Channel Contrast Display gui elements created below...

// Builds out the Visual Panel

public class VisualizePanel extends BasePanel {

	// Initialization of the vars that interact directly with the Contrast class and are grafted from the IJ ContrastAdjuster plugin code
	// DO NOT CHANGE THESE IN ANY WAY
	int sliderRange = 256;  
	Scrollbar maxSlider, minSlider;
	JButton autoB, resetB;
	
	Contrast contrast;

    //constructor
    public VisualizePanel() {
        init();
    }

    //Visualization Panel is getting invoked by this method
    private void init(){

        //setting up the background color
        this.setBackground(Color.lightGray);

        // --- Pseudo Color Channel Label: ---
        JLabel l1 = addFirstLabel(this, new Rectangle(10, 5, 200,20), "Channel Color Display:", 14);

        // first line color pallette buttons
        addIconButton(this, "red.png", "red", "red", 10, 30); // ints are xAxis, yAxis

        addIconButton(this, "green.png", "green", "green", 38, 30);

        addIconButton(this, "blue.png", "blue", "blue", 66, 30);

        addIconButton(this, "cyan.png", "cyan", "cyan", 94, 30);

        addIconButton(this, "yellow.png", "yellow", "yellow", 122, 30);

        addIconButton(this, "magenta.png", "magenta", "magenta", 150, 30);
		
		addIconButton(this, "white.png", "white", "white", 178, 30);

        addIconButton(this, "black.png", "Ch OFF", "black", 206, 30);
		
        // second line color pallette buttons
        addIconButton(this, "inferno.png", "mpl-inferno", "inferno", 10, 58);

        addIconButton(this, "viridis.png", "mpl-viridis", "viridis", 38, 58);

        addIconButton(this, "orangehot.png", "orange hot", "ohot", 66, 58);

        addIconButton(this, "cyanhot.png", "cyan hot", "chot", 94, 58);
		
		addIconButton(this, "phase.png", "phase", "phase", 122, 58);
		
		// adding another line
		// Show channels display group
		addButton(this, "Merge", "Show All channels together", "showall", new Rectangle(10, 90, 50, 20), 12);
		
		addButton(this, "Chs", "Show each channel individually", "showch", new Rectangle(60, 90, 30, 20), 12);
		
		addButton(this, "Order", "Change channel order", "reorder", new Rectangle(90, 90, 40, 20), 12);
		
		
        // --- Channel Contrast label: ---
// **** Channel Contrast gui elements are built out directly here because they are not reusealbe and they must interact with the Contrast class *****
		// A textfield is no longer paired with the scroll bars because it takes up space and user rarely understands those numbers
		// Could in future add the textfields back along with OK buttons to mimick the 'Set' functionality of ContrastAdjuster

		// The Channel Contrast GUI elements use dedicated methods and listeners because they have unique requirements and are a graft from the FIJI ContrastAdjuster plugin

        addLabel(this, "Channel Contrast Display:", "", new Rectangle(10, 120, 200, 20), 14);

        // Creates the Auto button
		Rectangle autobRect = new Rectangle(10, 145, 45, 20);   // use to position button 
		autoB = new JButton();
        autoB.setText("Auto");
        autoB.setBounds(autobRect);
		autoB.setMargin(new Insets(2, 2, 2, 2));
        autoB.setFont(new Font("Calibri", Font.PLAIN, 12));
        autoB.setToolTipText("Automatically adjusts contrast via LUT");
		//autoB.putClientProperty("ID", "auto"); // unused
	
        // Creates the Reset button
		Rectangle resetbRect = new Rectangle(10, 170, 45, 20);
		resetB = new JButton();
        resetB.setText("Reset");
        resetB.setBounds(resetbRect);
		resetB.setMargin(new Insets(2, 2, 2, 2));
        resetB.setFont(new Font("Calibri", Font.PLAIN, 12));
        resetB.setToolTipText("Resets LUT");
		//resetB.putClientProperty("ID", "reset"); // unused

       // label on gain slider
        addLabel(this, "Gain:", "Sets white-level via LUT", new Rectangle(60, 145, 50, 20), 12);
       // Creates the Max Scrollbar ("gain")
		Rectangle maxRect = new Rectangle(110, 145, 160, 20); // use to position scrollbar 
		maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange); // Grafted from IJ ContrastAdjuster.  DO NOT CHANGE
		maxSlider.setBounds(maxRect); 
		maxSlider.setUnitIncrement(1);
		maxSlider.setBackground(Color.WHITE); // Track color
		maxSlider.setFocusable(false); // prevents blinking on Windows

		// label on offset slider
        addLabel(this, "Offset:", "Sets black-level via LUT", new Rectangle(60, 170, 50, 20), 12);
		// Creates the Min Scrollbar ("offset")
		Rectangle minRect = new Rectangle(110, 170, 160, 20); // use to position scrollbar 
		minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
		minSlider.setBounds(minRect); 
		minSlider.setUnitIncrement(1);
		minSlider.setBackground(Color.WHITE); // Track color
		minSlider.setFocusable(false); 
		

		// Instantiates and initializes Contrast listener
			// Analogous to how IJ starts a plugin
		contrast = new Contrast(minSlider, maxSlider, autoB, resetB); // informs Contrast about the initial state of the related gui elements
		contrast.run(new String());  // intializes Contrast
		
		// There is a getContrast method below...

		// Adds Contrast as the listenr on the Channel Contrast Display gui elements
		autoB.addActionListener(contrast);
		resetB.addActionListener(contrast);
		maxSlider.addAdjustmentListener(contrast); 
		minSlider.addAdjustmentListener(contrast);
		
		// Displays the Channel Contrast Display gui elements on this panel
		this.add(autoB);
		this.add(resetB);	
		this.add(maxSlider);
		this.add(minSlider);


        // --- Channel Views Label ---
		// Paragraph tooltip texts
		String coloc_tt = "<html><p style="+"width:300px;"+">A merge display that highlights pixels of similar intensity across two channels. For best results, use 'Multiply' in Process tab to ensure the intensities on each channel fill the dynamic range. See: Taylor, J Microscopy, 268:73-83. 2017.</p></html>";
		String pup_tt = "<html><p style="+"width:300px;"+">A merge display that preserves the perception of intensity across two channels. For best results, use 'Multiply' in Process tab to ensure the intensities on each channel fill the dynamic range. See: Taylor, J Microscopy, 268:73-83. 2017.</p></html>";

        addLabel(this, "Channel Views:", "", new Rectangle(10, 195, 200, 20), 14);
		
		addButton(this, "Panelize", "Displays each channel in its own panel", "panelize", new Rectangle(10, 220, 60, 20), 12);

		addButton(this, "Coloc", coloc_tt, "coloc", new Rectangle(70, 220, 50, 20), 12);
		
		addButton(this, "PUP", pup_tt, "pup", new Rectangle(120, 220, 40, 20), 12);
		
		// --- Scale Bar ----
		addLabel(this, "Scale Bar:", "Adds scale bar as overlay", new Rectangle(190, 195, 70, 20), 14);
		
		addButton(this, "|-- um --|", "Set scale manually if needed", "um", new Rectangle(190, 220, 65, 20), 12);
		

        // --- Ratio Views Label ---
		// Paragraph tooltip texts
		String ratioviews_tt = "<html><p style="+"width:300px;"+">For all ratio displays, pre-processing of both channels via the Process tab is recommended: 1. 'Subtract Background' so that background on boths channels is near zero. 2. 'Multiply' one channel to fill the dynamic range. 3. If absolute values matter, 'Multiply' the other channel such that pixel intesities are 1:1 where ratio should be 1:1.</p></html>";
		String raw_tt = "<html><p style="+"width:300px;"+">The quotient of two channels as 32-bit. Use when absolute values are required.</p></html>";
		String imd_tt =  "<html><p style="+"width:300px;"+">A merge display that encodes the quotient as hue and modulates brightness according to the intensity of the denominator. See: Hinman, Biotechniques. 25:124–128. 1998.</p></html>";
		String snrmd_tt =  "<html><p style="+"width:300px;"+">A merge display that encodes the quotient as hue and modulates brightness according to the variability of the quotient. See: XXX.</p></html>";
		
        addLabel(this, "Ratio Views: (note)", ratioviews_tt, new Rectangle(10, 245, 150, 20), 14);

		addButton(this, "Raw", raw_tt, "raw", new Rectangle(10, 270, 40, 20), 12);

        addButton(this, "IMD", imd_tt, "imd", new Rectangle(50, 270, 40, 20), 12);

        addButton(this, "SnrMD", snrmd_tt, "snrmd", new Rectangle(90, 270, 50, 20), 12);
		
		// --- Snap View ----
		addLabel(this, "Capture:", "", new Rectangle(190, 245, 80, 20), 14);
		
		addButton(this, "Snap View", "Creates an RGB snapshot of current view", "snap", new Rectangle(190, 270, 75, 20), 12);


		// ---- Stack Views label: ----  
		addLabel(this, "Stack Views:", "", new Rectangle(10, 295, 150, 20), 14);

		// / left line
        //button MIP, SIP, Ortho., D3, Kymo
        addButton(this, "MIP", "Maximum intensity projection", "mip", new Rectangle(10, 320, 30, 20), 12);

        addButton(this, "Ortho", "Orthogonal slices view", "ortho", new Rectangle(40, 320, 40, 20), 12);

        addButton(this, "3D", "A basic 3D viewer", "3D", new Rectangle(80, 320, 30, 20), 12);

        addButton(this, "Kymo", "Kymograph", "kymo", new Rectangle(110, 320, 40, 20), 12);
		
		addButton(this, "Montage", "Montage of slices", "montage", new Rectangle(150, 320, 60, 20), 12);
		
		
        //Added line between label N and Label D
        this.add(l1);
    }
	
	// Enables the JFrame windowActivated listener in ToolbarTab to access Contrast
	public Contrast getContrast() {
			return contrast;
	}
}
