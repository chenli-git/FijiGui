package org.stjude.swingui.boot.panel;

import javax.swing.*;
import java.awt.*;
import ij.*;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.*;

import org.checkerframework.checker.units.qual.m;
import org.stjude.swingui.boot.proc.Contrast; // the Contrast class is a Listener for the Channel Contrast Display gui elements created below...
import javax.swing.plaf.basic.BasicSliderUI;

// Builds out the Visual Panel

public class VisualizePanel extends BasePanel {

	// Initialization of the vars that interact directly with the Contrast class and are grafted from the IJ ContrastAdjuster plugin code
	// DO NOT CHANGE THESE IN ANY WAY
	int sliderRange = 256;  
	Scrollbar maxSlider, minSlider;
	JButton autoB, resetB;
	static final int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};
	Contrast contrast;
	int channels = 7; // RGB
	static final int AUTO_THRESHOLD = 5000;
	boolean RGBImage = false;
	static int autoThreshold = 0;
	double min, max;
	double defaultMin, defaultMax;
    //constructor
    public VisualizePanel() {
        init();
    }

    //Visualization Panel is getting invoked by this method
    private void init(){

        //setting up the background color
        this.setBackground(Color.lightGray);

        // --- Pseudo Color Channel Label: ---
        JLabel l1 = addFirstLabel(this, new Rectangle(10, 5, 200,20), "Channel Color:", 14);

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
		
		//addButton(this, "Reset", "reset to original setting", "reset_color", new Rectangle(150, 58, 50, 25), 12);
		// adding another line
		// Show channels display group
		addButton(this, "AllChs", "Show all channels", "showall", new Rectangle(150, 60, 50, 20), 12);
		
		addButton(this, "EachCh", "Show each channel", "showch", new Rectangle(200, 60, 55, 20), 12);
		
		//addButton(this, "Order", "Change channel order", "reorder", new Rectangle(225, 60, 40, 20), 12);
		
		
        // --- Channel Contrast label: ---
// **** Channel Contrast gui elements are built out directly here because they are not reusealbe and they must interact with the Contrast class *****
		// A textfield is no longer paired with the scroll bars because it takes up space and user rarely understands those numbers
		// Could in future add the textfields back along with OK buttons to mimick the 'Set' functionality of ContrastAdjuster

		// The Channel Contrast GUI elements use dedicated methods and listeners because they have unique requirements and are a graft from the FIJI ContrastAdjuster plugin

        addLabel(this, "Channel Contrast:", "", new Rectangle(10, 90, 200, 20), 14);

        // Creates the Auto button
		Rectangle autobRect = new Rectangle(10, 165, 45, 20);   // use to position button 
		autoB = new JButton();
        autoB.setText("Auto");
        autoB.setBounds(autobRect);
		autoB.setMargin(new Insets(2, 2, 2, 2));
        autoB.setFont(new Font("Calibri", Font.PLAIN, 12));
        autoB.setToolTipText("Automatically adjusts contrast via LUT");
		//autoB.putClientProperty("ID", "auto"); // unused


	
        // Creates the Reset button
		Rectangle resetbRect = new Rectangle(55, 165, 45, 20);
		resetB = new JButton();
        resetB.setText("Reset");
        resetB.setBounds(resetbRect);
		resetB.setMargin(new Insets(2, 2, 2, 2));
        resetB.setFont(new Font("Calibri", Font.PLAIN, 12));
        resetB.setToolTipText("Resets LUT");
		//resetB.putClientProperty("ID", "reset"); // unused

		JButton autoch = new JButton("AutoCh");autoch.setBounds(new Rectangle(10, 165, 60, 20));autoch.setFont(new Font("Calibri", Font.PLAIN, 12));autoch.setMargin(new Insets(2, 2, 2, 2));autoch.setToolTipText("Auto contrast active channel");
		JButton autoall = new JButton("AutoAll");autoall.setBounds(new Rectangle(70, 165, 60, 20));autoall.setFont(new Font("Calibri", Font.PLAIN, 12));autoall.setMargin(new Insets(2, 2, 2, 2));autoall.setToolTipText("Auto contrast all channels");
		JButton propgate = new JButton("Propagate");propgate.setBounds(new Rectangle(130, 165, 70, 20));propgate.setFont(new Font("Calibri", Font.PLAIN, 12));propgate.setMargin(new Insets(2, 2, 2, 2));propgate.setToolTipText("Apply same contrast to all open images");
		this.add(autoch);this.add(autoall);this.add(propgate);

		


       // label on gain slider
        addLabel(this, "Gain:", "Increase brightness", new Rectangle(10, 115, 50, 20), 12);
		//addButton(this, "Reset", "reset gain value", "resetMax", new Rectangle(260, 115, 45, 20), 12);
		JButton resetMax = new JButton("Reset");
		resetMax.setBounds(new Rectangle(220, 115, 45, 20));
		resetMax.setFont(new Font("Calibri", Font.PLAIN, 12));
		resetMax.setMargin(new Insets(2, 2, 2, 2)); 
		resetMax.setToolTipText("Set display max to max image intensity");
		this.add(resetMax);

	

		// // label on offset slider
        addLabel(this, "Offset:", "Increase blackness", new Rectangle(10, 140, 50, 20), 12);
		//addButton(this, "Reset", "reset offset value", "resetMin", new Rectangle(260, 140, 45, 20), 12);
		JButton resetMin = new JButton("Reset");
		resetMin.setBounds(new Rectangle(220, 140, 45, 20));
		resetMin.setFont(new Font("Calibri", Font.PLAIN, 12));
		resetMin.setMargin(new Insets(2, 2, 2, 2)); 
		resetMin.setToolTipText("Set display min to min image intensity");
		this.add(resetMin);


		// adding gain and offset sliders
		JSlider maxslider = new JSlider(); maxslider.setMaximum(sliderRange); maxslider.setValue(sliderRange/2);
		JSlider minslider = new JSlider(); minslider.setMaximum(sliderRange); minslider.setValue(sliderRange/2);
		maxslider.setBounds(new Rectangle(57, 115, 160, 20));
		minslider.setBounds(new Rectangle(57, 140, 160, 20));
		maxslider.setInverted(true);
		

		JLabel minValueLabel = new JLabel(String.valueOf(minslider.getValue())); // Label for min slider
		minValueLabel.setBounds(new Rectangle(270, 140, 40, 20)); // Position label
		minValueLabel.setToolTipText("Lower limit of display range");
		minslider.addChangeListener(e -> {
			//int value = minslider.getValue();
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp == null) return;
			double Min = imp.getDisplayRangeMin();
			//minValueLabel.setText(String.valueOf((int) Min)); // Update label
			minValueLabel.setText(String.format("%.0f", (double) Min));
		});

		JLabel maxValueLabel = new JLabel(String.valueOf(maxslider.getValue())); // Label for max slider
		maxValueLabel.setBounds(new Rectangle(270, 115, 40, 20)); // Position label
		maxValueLabel.setToolTipText("Upper limit of display range");
		maxslider.addChangeListener(e -> {
			//int value = maxslider.getMaximum() - maxslider.getValue();
			//int value = maxslider.getValue();
			//System.out.println(maxslider.getMaximum());
			//int value = maxslider.getValue();
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp == null) return;
			double max = imp.getDisplayRangeMax();
			double min = imp.getDisplayRangeMin();
			int type = imp.getType();
			Calibration cal = imp.getCalibration();
			boolean realValue = type==ImagePlus.GRAY32;
			if (cal.calibrated()) {
				min = cal.getCValue((int)min);
				max = cal.getCValue((int)max);
				realValue = true;
			}

			//maxValueLabel.setText(String.valueOf((int) Max)); // Update label
			//maxValueLabel.setText(String.format("%.0f",  maxslider.getMaximum()- (double) value));
			maxValueLabel.setText(String.format("%.0f",  (double) max));
		});

		minslider.setUI(new BasicSliderUI(minslider));
		maxslider.setUI(new BasicSliderUI(maxslider));
		

		resetMax.addActionListener(e -> {
			resetMax.setBackground(new java.awt.Color(255, 255, 255));
			ImagePlus imp = WindowManager.getCurrentImage(); // active image
			if (imp == null) return;
			imp.updateAndDraw();
			ImageProcessor ip = imp.getProcessor();
			ip.resetMinAndMax();
			double min = ip.getMin(); // Preserve current min
			double autoMax = ip.getStatistics().max;
			double globalMax = ip.getMax();
			ip.setMinAndMax(min, globalMax);
			System.out.println("Max: " + autoMax);
			System.out.println("Min: " + min);
			System.out.println("Global Max: " + globalMax);
			System.out.println("sliderMax: " + maxslider.getMaximum());
			imp.updateAndRepaintWindow();
			//maxslider.setValue((int) autoMax);
			int bitMaxValue = (int) Math.pow(2, imp.getBitDepth()) - 1; // 8-bit = 255, 16-bit = 65535, etc.
    		int sliderRange = maxslider.getMaximum() - maxslider.getMinimum();
			System.out.println("bitMaxValue: " + bitMaxValue);
			System.out.println("sliderRange" + sliderRange);
			System.out.println("globalMax: " + globalMax);
			int scaledMax = (int) ((globalMax / bitMaxValue) * sliderRange);
			System.out.println("scaledMax" + scaledMax);
			maxslider.setValue((int) globalMax);
            maxValueLabel.setText(String.valueOf((int) globalMax));
		});

		resetMin.addActionListener(e -> {
			resetMin.setBackground(new java.awt.Color(255, 255, 255));
			ImagePlus imp = WindowManager.getCurrentImage(); // active image
			if (imp == null) return;

			imp.updateAndDraw();
			ImageProcessor ip = imp.getProcessor();
			ip.resetMinAndMax();
			double max = ip.getMax(); // Preserve current ma
			double globalMin = ip.getMin();
			double autoMin = ip.getStatistics().min;
			System.out.println("Max: " + max);
			System.out.println("Min: " + autoMin);
			ip.setMinAndMax(globalMin, max);
			imp.updateAndRepaintWindow();
			//minslider.setValue((int) globalMin);
			int bitMaxValue = (int) Math.pow(2, imp.getBitDepth()) - 1; // 8-bit = 255, 16-bit = 65535, etc.
    		int sliderRange = maxslider.getMaximum() - maxslider.getMinimum();
			int scaledMin = (int) ((globalMin / bitMaxValue) * sliderRange);
			minslider.setValue((int) globalMin);
            minValueLabel.setText(String.valueOf((int) globalMin));
		});

		autoch.addActionListener(e -> {	
			autoch.setBackground(new java.awt.Color(255, 255, 255));
			ImagePlus imp = WindowManager.getCurrentImage(); // active image
			if (imp == null) return;
			IJ.run(imp, "Enhance Contrast", "saturated=0.35");
			imp.updateAndDraw();
			min = imp.getDisplayRangeMin();
			max = imp.getDisplayRangeMax();
			minValueLabel.setText(String.format("%.0f", min));
			maxValueLabel.setText(String.format("%.0f", max));

			ImageProcessor ip = imp.getProcessor();
			double newMin = imp.getDisplayRangeMin();
			double newMax = imp.getDisplayRangeMax();
			ImageStatistics stats = imp.getRawStatistics();
			defaultMin = stats.min;
			defaultMax = stats.max;
			int sliderRange = maxslider.getMaximum(); // usually 256–1024
			int minSliderValue = (int)((newMin - defaultMin) * (sliderRange - 1) / (defaultMax - defaultMin));
			int maxSliderValue = (int)((newMax - defaultMin) * (sliderRange - 1) / (defaultMax - defaultMin));
			minslider.setValue(minSliderValue);
			maxslider.setValue(maxSliderValue);
			System.out.println("Min: " + newMin);
			System.out.println("Max: " + newMax);
			System.out.println("defaultMin: " + defaultMin);
			System.out.println("defaultMax: " + defaultMax);
			System.out.println(maxSliderValue);

		});

		

		autoall.addActionListener(e -> {	
			autoall.setBackground(new java.awt.Color(255, 255, 255));
			ImagePlus imp = WindowManager.getCurrentImage(); // Get active image	
			if (imp == null) return;
			int originalChannel = imp.getC();
			int nChannels = imp.getNChannels();
			for (int c = 1; c <= nChannels; c++) {
				imp.setC(c);
				IJ.run(imp, "Enhance Contrast", "saturated=0.35");
				if (c == originalChannel) {
					min = imp.getDisplayRangeMin();
					max = imp.getDisplayRangeMax();
					minValueLabel.setText(String.format("%.0f", min));
					maxValueLabel.setText(String.format("%.0f", max));
					ImageProcessor ip = imp.getProcessor();
					double newMin = imp.getDisplayRangeMin();
					double newMax = imp.getDisplayRangeMax();
					ImageStatistics stats = imp.getRawStatistics();
					defaultMin = stats.min;
					defaultMax = stats.max;
					int sliderRange = maxslider.getMaximum(); // usually 256–1024
					int minSliderValue = (int)((newMin - defaultMin) * (sliderRange - 1) / (defaultMax - defaultMin));
					int maxSliderValue = (int)((newMax - defaultMin) * (sliderRange - 1) / (defaultMax - defaultMin));
					minslider.setValue(minSliderValue);
					maxslider.setValue(maxSliderValue);
					System.out.println("Min: " + newMin);
					System.out.println("Max: " + newMax);
					System.out.println("defaultMin: " + defaultMin);
					System.out.println("defaultMax: " + defaultMax);
					System.out.println(maxSliderValue);

				}
			}
			imp.setC(originalChannel);
			imp.updateAndDraw();
		});

		propgate.addActionListener(e -> {
			propgate.setBackground(new java.awt.Color(255, 255, 255));	
			ImagePlus img = WindowManager.getCurrentImage(); // Get active image
			if (img == null) return;


			if (img.getBitDepth()==24) {
			GenericDialog gd = new GenericDialog("Contrast Adjuster");
			gd.addMessage( "Propagation of RGB images not supported. As a work-around,\nconvert images to multi-channel composite color.");
			gd.hideCancelButton();
			gd.showDialog();
			return;
			}
			int[] list = WindowManager.getIDList();
			if (list==null) return;
			int nImages = list.length;
			if (nImages<=1) return;

			int refChannels = img.getNChannels(); // Reference number of channels

			 // Step 1: Check if all images have the same number of channels
			 for (int i = 0; i < nImages; i++) {
				ImagePlus imgCheck = WindowManager.getImage(list[i]);
				if (imgCheck != null && imgCheck.getNChannels() != refChannels) {
					GenericDialog gd = new GenericDialog("Error");
					gd.addMessage("All open images must have the same number of channels for contrast propagation.");
					gd.hideCancelButton();
					gd.showDialog();
					return;
				}
			}

			ImageProcessor ip = img.getProcessor();
			double min = ip.getMin();
			double max = ip.getMax();
			int depth = img.getBitDepth();
			if (depth==24) return;
			int id = img.getID();
			if (img.isComposite()) {
				int nChannels = img.getNChannels();
				for (int i=0; i<nImages; i++) {
					ImagePlus img2 = WindowManager.getImage(list[i]);
					if (img2==null) continue;
					int nChannels2 = img2.getNChannels();
					if (img2.isComposite() && img2.getBitDepth()==depth && img2.getID()!=id
					&& img2.getNChannels()==nChannels && img2.getWindow()!=null) {
						int channel = img2.getChannel();
						for (int c=1; c<=nChannels; c++) {
							LUT  lut = ((CompositeImage)img).getChannelLut(c);
							img2.setPosition(c, img2.getSlice(), img2.getFrame());
							img2.setDisplayRange(lut.min, lut.max);
							img2.updateAndDraw();
						}
						img2.setPosition(channel, img2.getSlice(), img2.getFrame());
					}
				}
			} else {
				for (int i=0; i<nImages; i++) {
					ImagePlus img2 = WindowManager.getImage(list[i]);
					if (img2!=null && img2.getBitDepth()==depth && img2.getID()!=id
					&& img2.getNChannels()==1 && img2.getWindow()!=null) {
						ImageProcessor ip2 = img2.getProcessor();
						ip2.setMinAndMax(min, max);
						img2.updateAndDraw();
					}
				}
			}
			
		});

		this.add(maxslider);
		this.add(minslider);
		this.add(minValueLabel);
		this.add(maxValueLabel);


		// Instantiates and initializes Contrast listener
			// Analogous to how IJ starts a plugin
		contrast = new Contrast(minslider, maxslider, autoB, resetB); // informs Contrast about the initial state of the related gui elements
		contrast.run(new String());  // intializes Contrast
		
		// There is a getContrast method below...

		// Adds Contrast as the listenr on the Channel Contrast Display gui elements
		autoB.addActionListener(contrast);
		resetB.addActionListener(contrast);
		//maxSlider.addAdjustmentListener(contrast); 
		//minSlider.addAdjustmentListener(contrast);
		minslider.addChangeListener(contrast);
		maxslider.addChangeListener(contrast);
		
		// Displays the Channel Contrast Display gui elements on this panel
		//this.add(autoB);
		//this.add(resetB);	
		//this.add(maxSlider);
		//this.add(minSlider);


        // --- Channel Views Label ---
		// Paragraph tooltip texts
		String coloc_tt = "<html><p style="+"width:300px;"+">Highlight colocalized pixels across two fluorescence channels</p></html>";
		String pup_tt = "<html><p style="+"width:300px;"+">Merge display of two fluorescence channels</p></html>";
		String fluogray_tt = "<html><p style="+"width:300px;"+">Merge display of a fluorescence and grayscale channel</p></html>";
        
		addLabel(this, "Channel Views:", "", new Rectangle(10, 195, 140, 20), 14);
		JLabel l2 = new JLabel();
		String htmlText = "<html><span style='color:#8B0000;'>Pre-Apply Proper Gain and Offset</span></html>";
		l2.setText(htmlText);
		l2.setBounds(new Rectangle(150, 195, 200, 20));
		l2.setFont(new Font("Calibri", Font.PLAIN, 11));
		this.add(l2);
		//addButton(this, "Panelize", "Displays each channel in its own panel(default: tiled view)", "panelize", new Rectangle(10, 220, 60, 20), 12);

		addButton(this, "FFColoc", coloc_tt, "coloc", new Rectangle(10, 220, 70, 20), 12);
		
		addButton(this, "FFMerge", pup_tt, "pup", new Rectangle(80, 220, 70, 20), 12);

		addButton(this, "FGMerge", fluogray_tt, "ColorFusion", new Rectangle(150, 220, 70, 20), 12);
		addButton(this, "Montage", "Array channels individually", "montage", new Rectangle(10, 240, 70, 20), 12);
		addButton(this, "SyncWins", "Sync actions across image windows", "sync", new Rectangle(80, 240, 70, 20), 12);

		
		
		

        // --- Ratio Views Label ---
		// Paragraph tooltip texts
		String ratioviews_tt = "<html><p style="+"width:300px;"+">For all ratio displays, pre-processing of both channels via the Process tab is recommended: 1. 'Subtract Background' so that background on boths channels is near zero. 2. 'Multiply' one channel to fill the dynamic range. 3. If absolute values matter, 'Multiply' the other channel such that pixel intesities are 1:1 where ratio should be 1:1.</p></html>";
		String raw_tt = "<html><p style="+"width:300px;"+">The quotient of two channels as 32-bit. Use when absolute values are required.</p></html>";
		String imd_tt =  "<html><p style="+"width:300px;"+">A merge display that encodes the quotient as hue and modulates brightness according to the intensity of the denominator. See: Hinman, Biotechniques. 25:124–128. 1998.</p></html>";
		String snrmd_tt =  "<html><p style="+"width:300px;"+">A merge display that encodes the quotient as hue and modulates brightness according to the variability of the quotient. See: XXX.</p></html>";
		
        //addLabel(this, "Ratio Views:", ratioviews_tt, new Rectangle(10, 245, 150, 20), 14);

		//addButton(this, "RawRatio", raw_tt, "raw", new Rectangle(175, 220, 60, 20), 12);

        //addButton(this, "IMDRatio", imd_tt, "imd", new Rectangle(235, 220, 60, 20), 12);

        //addButton(this, "SnrMD", snrmd_tt, "snrmd", new Rectangle(90, 270, 50, 20), 12);
		
		
		
		// ---- Stack Views label: ----  
		addLabel(this, "Stack Views:", "", new Rectangle(10, 265, 120, 20), 14);
        //button MIP, SIP, Ortho., D3, Kymo
        addButton(this, "MIP", "Maximum intensity projection", "mip", new Rectangle(10, 290, 40, 20), 12);
        addButton(this, "SIP","Sum slices projection", "sip", new Rectangle(50, 290, 40, 20), 12);
		addButton(this, "Ortho", "Orthogonal slices view", "ortho", new Rectangle(90, 290, 50, 20), 12);
        //addButton(this, "3D", "A basic 3D viewer", "3D", new Rectangle(80, 320, 30, 20), 12);
        addButton(this, "Kymo", "Kymograph (requires plugin)", "kymo", new Rectangle(140, 290, 50, 20), 12);
		//addButton(this, "Montage", "Montage of slices", "montage", new Rectangle(255, 290, 60, 20), 12);
		
		// // --- Scale Bar ----
		addLabel(this, "Scale:", "Adds scale bar as overlay", new Rectangle(290, 265, 80, 20), 14);
		addButton(this, "|- um -|", "Display scalebar", "um", new Rectangle(290, 290, 60, 20), 12);

		// --- Snap View ----
		addLabel(this, "Copy:", "", new Rectangle(190, 265, 80, 20), 14);
		addButton(this, "Dup", "Duplicate image", "dup", new Rectangle(190, 290, 50, 20), 12);
		addButton(this, "ToClip", "Copy display to clipboard", "copy", new Rectangle(240, 290, 50, 20), 12);


		addLabel(this, "VERSION: 1.2.0", "version number", new Rectangle(10, 320, 150, 20), 11);


		
        //Added line between label N and Label D
        this.add(l1);
    }
	
	// Enables the JFrame windowActivated listener in ToolbarTab to access Contrast
	public Contrast getContrast() {
			return contrast;
	}

	

}
