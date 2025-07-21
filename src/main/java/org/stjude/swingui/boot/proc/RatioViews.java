package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 
import ij.text.*; // testing
// 	new TextWindow("Test", "Selected "+Integer.toString(testcount), 300, 200);

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Creates various RGB views of 2-channel data, including various ratio displays, a colocalization display, and a PUP merge display
// It is up to the user to ensure that...
	// Intensities on at least one of the channels fills the full bitdepth available. 
	// For quantitative ratiometric uses, intensities on one channel should be scales such that the pixel ints across channels have are 1:1 at the probes'isobestic point'

// To call this code headlessly (without the gui)
	// #1. Instantiate the class
	// #2. Call setup(String ratiomethod, ImagePlus imp, int numch, int dench)
	// #3. Call getRatioImage(String ratiomethod) 
	// #4. It is the responsibility of calling class to display the final result.

public class RatioViews implements ActionListener {

	ImagePlus imp; // multichannel input
	String[] chnames;
	JDialog jd;
	int numch, dench;
	ImageStack numimgstk, denimgstk;
	String ratiomethod = null; // holds ratiomethod constants
	
	double numval, denval, ratio; // ratio calc vars; double is MORE precise than float
	ColorDisplay cd;

	// Ratiomethod constants used to call each display type
	public static final String RAW = "RAW";
	public static final String IMD = "IMD";
	public static final String SNR = "SNR";
	public static final String COL = "COL";
	public static final String PUP = "PUP";

	public RatioViews() {
	}

	public void run(String ratiomethod) {  // Pass in ratiomethod constant here when running from FIJI_GUI
		
		this.ratiomethod = ratiomethod;
		//ratiomethod = this.SNR; // Hardcode ratiomethod here if desired for testing purposes

		if (check()) {
			
			buildGui(); // display gui

		}
	}
	
	public boolean check() { // error checking
		
		boolean pass = false;
		
		imp = WindowManager.getCurrentImage();
				
		if (imp == null) {
			JFrame f = new JFrame();  
			JOptionPane.showMessageDialog(f,"ERROR: No Image Open.");  
			pass = false;
		} else {	
			// Checks that >=2 chs are present
			int nchs = imp.getNChannels();
			if (nchs < 2) {
				JFrame f = new JFrame();  
				JOptionPane.showMessageDialog(f,"ERROR: Active Image Must Have >=2 Channels.");  
				pass = false;
			} else {
				// Checks bit depth
				if (imp.getBitDepth() == 24 || imp.getBitDepth() == 32) {
					JFrame f = new JFrame();  
					JOptionPane.showMessageDialog(f,"ERROR: Must be type 8 or 16 bit");  
					pass = false;
				} else {
					// Rest of code is now safe to execute...
					// Create list of image channels
					chnames = new String[nchs];
					for (int i=0; i<nchs; i++) { //array indexes start at 0
						chnames[i] = "Ch"+String.valueOf(i+1);
					}	
					pass = true; // suitable image is open
				}
			}		
		}		
		return pass; 
	}

	public void buildGui() {
	
		// Creates dialog to get numerator and denominator channels...
		jd = new JDialog(); // many useful methods inhereted from Dialog and Window
		jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		jd.setTitle("Create Ratio View");
		// Layout
			// Might get in trouble here by doing add and layout on the JDialog rather than directly on the contentPane...
		jd.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Will use a 3x3 cell grid

      	// Adds text
		c.gridx = 0; c.gridy = 0; // layout; upper left grid location
		c.gridwidth = 2; // layout; span two colums
		c.insets = new Insets(10,10,10,10); // border 
		jd.add(new JLabel ("Numerator:"), c);

		// Adds numerator Combobox
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox njcb = new JComboBox(chnames);
		njcb.setSelectedIndex(0);
		c.gridx = 2; c.gridy = 0;
		c.insets = new Insets(10,10,10,10); // border 
		jd.add(njcb, c); // at component index 1 in the content pane

      	// Adds text
		c.gridx = 0; c.gridy = 1; 
		c.gridwidth = 2;
		c.insets = new Insets(10,10,10,10); // border 
		jd.add(new JLabel ("Denominator:"), c);	
	
		// Adds denominator Combobox
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox djcb = new JComboBox(chnames);
		djcb.setSelectedIndex(1);
		c.gridx = 2; c.gridy = 1; 
		c.insets = new Insets(10,10,10,10); // border 
		jd.add(djcb, c); // at component index 3 in the content pane
	
		// Adds OK button
		JButton b = new JButton ("OK");  
        b.addActionListener(this);
		c.gridx = 1; c.gridy = 2; 
		c.insets = new Insets(10,10,10,10); // border 
		jd.add(b);  
		
		jd.pack(); // auto-window sizing
		GUI.centerOnImageJScreen(jd);
		jd.setVisible(true);  

		// Processing begins in response to button press and kicks off within ActionListener method below....
	
	}

	public void actionPerformed(ActionEvent e) {  
		// OK button is the only component with an action listener, so any call to this method must be from the OK button
		
		Container cp = jd.getContentPane(); // all 'added' components belong to the content pane
		JComboBox njcb = (JComboBox) cp.getComponent(1); // 1 = numerator combobox index in the content pane - returns a Component
		numch = njcb.getSelectedIndex()+1; // the index of the choice within the combobox
		
		JComboBox djcb = (JComboBox) cp.getComponent(3); // 3 = denom combobox index in the content pane
		dench = djcb.getSelectedIndex()+1;
		
		// Destroys the GUI
		jd.setVisible(false);
		jd.dispose();
		
		// Calls computational methods...
		ImagePlus finalresult = getRatioImage(ratiomethod);
		finalresult.show(); // END - Displays final result
		if (ratiomethod == RAW) {
			cd = new ColorDisplay();
			cd.setLUT("phase");
		}

    }

	public ImagePlus getRatioImage(String ratiomethod) {  // Call this method directly if running headlessly.
	
		this.ratiomethod = ratiomethod;
		ImagePlus ratioimage = null;
		
		getChannels(); // get channels and assigns to globals

		// apply lut first
		IJ.run(imp, "Apply LUT", "");

		// FIX - assumes one of the constants was passed
		if (ratiomethod == this.RAW) {
			ratioimage = rawRatio();
		}
		if (ratiomethod == this.IMD) {
			ratioimage = imdRatio();
		}
		if (ratiomethod == this.SNR) {
			ratioimage = snrMDRatio();
		}
		if (ratiomethod == this.COL) {
			ratioimage = coLoc();
		}
		if (ratiomethod == this.PUP) {
			ratioimage = pupMerge();
		}

		return ratioimage;
			
	}
	
// --- Computational methods ----

	private ImagePlus rawRatio() {
		
		// Best to keep as a 1D image stack until all processing is finished since then any required looping is easier
		int stkw = numimgstk.getWidth(); // denimgstk guaranteed to have same dimensions
		int stkh = numimgstk.getHeight();
		int stks = numimgstk.getSize();
		
		double b2log;

		// Holds result as float
		ImageStack ratioimgstk = ImageStack.create(stkw, stkh, stks, 32);	// 32 bit, all pixels set to black by default

		// Logic and division
		for (int z=0; z<stks; z++) {
			for (int x=0; x<stkw; x++) {
				for (int y=0; y<stkh; y++) {

					// Pixel vals on each channel
					numval = numimgstk.getVoxel(x, y, z);
					denval = denimgstk.getVoxel(x, y, z);
					
					if (numval > 0 && denval > 0) {  // solves divide by zero issue
						ratio = numval / denval;
						b2log = (double)(Math.log(ratio)/Math.log(2)); // base2 log, since this makes fold and fractional changes spaced the same
						ratioimgstk.setVoxel(x, y, z, b2log);
					} 
				}
			}
		}
		
		// Convert 1D indexed ImageStack back into an ND hyperstack...
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		ImagePlus ratioimp = new ImagePlus("Raw Ratio Image", ratioimgstk);
		
		if (ratioimp.getStackSize() > 1) { // required for HyperStackConverter to work
			ratioimp = HyperStackConverter.toHyperStack(ratioimp, 1, nslices, nframes, "grayscale"); // one channel by definition
		}
		
		return ratioimp;
	
	}

	private ImagePlus imdRatio() {
		
		// Drawback of this method is it is biased against visualization of ratio > 1 b/c small denominator is always displayed dimmly
		
		double angle, refang, absrefang, normrabsefang, theta; // polar values
		double intnorm = 1; // a normalizer
		double savgint;

		// imgstk is a 1D indexed ImageStack
		int stkw = numimgstk.getWidth(); // denimgstk guaranteed to have same dimensions
		int stkh = numimgstk.getHeight();
		int stks = numimgstk.getSize(); // 1D indexed! - includes z and t (single channel by definition)
		
		ImageProcessor hsbplaneip;
		
		// A plane of HSB space.  See ImageStack.isHSB32() for definition
		ImageStack hsbplanestk = ImageStack.create(stkw, stkh, 3, 32);	// 3 plane (one for each color axis), 32 bit, all pixels set to black by default
		hsbplanestk.setSliceLabel("Hue", 1); // required
		hsbplanestk.setSliceLabel("Saturation", 2); // for clarity only
		hsbplanestk.setSliceLabel("Brightness", 3); // for clarity only
		// ImagePlus of the HSB plane
		ImagePlus hsbplaneimp = new ImagePlus("HSB plane", hsbplanestk);
				
		// A 'single channel' 24-bit RGB stack to hold the final output
		ImageStack rgboutstk = ImageStack.create(stkw, stkh, stks, 24); // 24-bit color
		
		// Normalizes by the bit depth so radius can never exceed sqrt(2).
		int bitdepth = imp.getBitDepth();
		if (bitdepth == 8) {
			intnorm = 255; //2^8 - 1
		}
		if (bitdepth == 16) {
			intnorm = 65535; //2^16 - 1
		}
		
		// Logic and division
		for (int z=0; z<stks; z++) {
			for (int x=0; x<stkw; x++) {
				for (int y=0; y<stkh; y++) {

					// Pixel vals on each channel
					numval = numimgstk.getVoxel(x, y, z);
					denval = denimgstk.getVoxel(x, y, z);
					
					if (numval > 0 && denval > 0) {  // solves divide by zero issue

						// Ratio Calculations
						ratio = numval / denval; // the ratio
						angle = Math.atan(ratio); // 0 - pi/2 radians
						refang = angle - (Math.PI/4); // -pi/4 - +pi/4 -> angle in radian from the line y=x
						absrefang = Math.abs(refang);
						normrabsefang = absrefang / (Math.PI/4); // angle expressed as a value from 0-1
						theta = 1 - normrabsefang;
						
				// Sets Hue - type of ratio is encoded by hue (>1 or <1)
						if (refang > 0) {
								// Equation describes 32-bit hues ranging from orange on edge to lime near diagonal
								hsbplanestk.setVoxel(x, y, 0, (0.1667*theta+0.0833) ); // voxels are indexed starting from 0, so z vox 0 = slice 1
						} else {
								// Equation describes 32-bit hues ranging from light blue on edge to aquamarine near diagonal
								hsbplanestk.setVoxel(x, y, 0, (-0.1667*theta+0.5833) );
						}	
							
				// Sets Saturation - 'magnitude' of ratio is encoded by saturation. Least saturated along y=x.
						hsbplanestk.setVoxel(x, y, 1, 0.95*normrabsefang); // saturation << 1 keeps the luminosity of the hues more uniform
						
				// Sets Brightness - 'intensity modulated' according to the 'amount' of intramolecular sensor, assuming user has scaled intensities so that 'isobestic point' is 1:1 in intensity
					// Follows concepts in Hinman & Sammak, Biotechniques, 1998
						savgint = 1.1*(((numval/intnorm) + (denval/intnorm))/2); // scaling was done to make image brigher overall. Values >1 are then clipped...
						// clipping
						if (savgint > 1) {
								savgint=1;
						}	
						hsbplanestk.setVoxel(x, y, 2, savgint); // set brightness according to 'combined' SNR

					} 
				}
			}
			
			// Converts HSB plane to a 24-bit RGB plane
			hsbplaneimp.setStack(hsbplanestk);  // sets stack to current hsb stack
			new ImageConverter(hsbplaneimp).convertHSB32ToRGB();
			hsbplaneip = hsbplaneimp.getProcessor(); // the ColorProcessor for the RGB image
			// Places the RGB rendering into a slice of the final 'single channel' RGB stack....
			rgboutstk.setProcessor(hsbplaneip, z+1); // slice #ing starts at 1
		}
		
		// Converts the 1D indexed RGB ImageStack into an ND hyperstack...
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		ImagePlus ratioimp = new ImagePlus("IMD Ratio Image", rgboutstk); // end result if single plane image
		
		if (ratioimp.getStackSize() > 1) { // required for HyperStackConverter to work
			ratioimp = HyperStackConverter.toHyperStack(ratioimp, 1, nslices, nframes, "grayscale"); // one channel by definition
		}

		return ratioimp;
	}
	
	
	private ImagePlus snrMDRatio() {
		
		double angle, refang, absrefang, normrabsefang, theta; // polar values
		double nnumval, ndenval; // normalized cartesian units
		double exp, qsnr, nqsnr; // brightness vars
		double intnorm = 1; // a normalizer

		// imgstk is a 1D indexed ImageStack
		int stkw = numimgstk.getWidth(); // denimgstk guaranteed to have same dimensions
		int stkh = numimgstk.getHeight();
		int stks = numimgstk.getSize(); // 1D indexed! - includes z and t (single channel by definition)
		
		ImageProcessor hsbplaneip;
		
		// A plane of HSB space.  See ImageStack.isHSB32() for definition
		ImageStack hsbplanestk = ImageStack.create(stkw, stkh, 3, 32);	// 3 plane (one for each color axis), 32 bit, all pixels set to black by default
		hsbplanestk.setSliceLabel("Hue", 1); // required
		hsbplanestk.setSliceLabel("Saturation", 2); // for clarity only
		hsbplanestk.setSliceLabel("Brightness", 3); // for clarity only
		// ImagePlus of the HSB plane
		ImagePlus hsbplaneimp = new ImagePlus("HSB plane", hsbplanestk);
				
		// A 'single channel' 24-bit RGB stack to hold the final output
		ImageStack rgboutstk = ImageStack.create(stkw, stkh, stks, 24); // 24-bit color
			
		// Used to normal intensities by the bit depth
		int bitdepth = imp.getBitDepth();
		if (bitdepth == 8) {
			intnorm = 255; //2^8 - 1
		}
		if (bitdepth == 16) {
			intnorm = 65535; //2^16 - 1
		}
		
		// Logic and division
		for (int z=0; z<stks; z++) {
			for (int x=0; x<stkw; x++) {
				for (int y=0; y<stkh; y++) {

					// Pixel vals on each channel
					numval = numimgstk.getVoxel(x, y, z);
					denval = denimgstk.getVoxel(x, y, z);
					
					if (numval > 0 && denval > 0) {  // solves divide by zero issue

						// Ratio Calculations
						ratio = numval / denval; // the ratio
						angle = Math.atan(ratio); // 0 - pi/2 radians
						refang = angle - (Math.PI/4); // -pi/4 - +pi/4 -> angle in radian from the line y=x
						absrefang = Math.abs(refang);
						normrabsefang = absrefang / (Math.PI/4); // 0 near x=y -> 1 when y>>x or x>>y
						theta = 1 - normrabsefang;
						
						// Numerator and denominator values normalized to >0-100.  Used to set brightness below
						nnumval = 100*numval/intnorm; 
						ndenval = 100*denval/intnorm; 
						
				// Sets Hue - type of ratio is encoded by hue (>1 or <1)
						if (refang > 0) {
								// Equation describes 32-bit hues ranging from orange on edge to yellow near diagonal
								hsbplanestk.setVoxel(x, y, 0, (0.1667*theta+0.0833) ); // voxels are indexed starting from 0, so z vox 0 = slice 1
						} else {
								// Equation describes 32-bit hues ranging from light blue on edge to cyan near diagonal
								hsbplanestk.setVoxel(x, y, 0, (-0.1667*theta+0.5833) );
						}	
							
				// Sets Saturation - magnitude of ratio is encoded by saturation
						hsbplanestk.setVoxel(x, y, 1, 0.95*normrabsefang); // saturation << 1 keeps the luminosity of the hues more uniform
						
				// Sets Brightness - 'intensity modulated' according to the SNR OF THE RATIO
						// SNR of THE RATIO.  In a ratio image really the goal is to ignore regions where VARIABILIIY is poor in general
							// Relationship and parameters were found via Matlab simulations and assume nnumval and ndenval range 0-100.  See SnrMD_Fit_Calcs.xlsx for summary of results
						exp = 0.1*Math.pow(ndenval,0.27);
						qsnr = 2.33*Math.pow(nnumval,exp); // qsnr max = ~11.5 for nnumval and ndenumval = 100; qsnr min = 2.33 for nnumval and ndenval > 0
						nqsnr = (qsnr-2.3)/8.5; // Shift of 2.3 and scaling of 9.3 (=11.5-2.3) keeps the  32-bit brightness 0-1
												// Scaling was then reduced to brighten image overall... Values >1 are then clipped...
						// Clipping
						if (nqsnr > 1) {
							nqsnr = 1;
						}						
						// Removes spurious values. Somehow (due to rounding?) nqsnr becomess negative when demoninator is very small.
						if (nqsnr < 0) {
							nqsnr = 0;
						}	
															
						hsbplanestk.setVoxel(x, y, 2, nqsnr); 
					} 
				}
			}
			
			// Converts HSB plane to a 24-bit RGB plane
			hsbplaneimp.setStack(hsbplanestk);  // sets stack to current hsb stack

			new ImageConverter(hsbplaneimp).convertHSB32ToRGB();
			hsbplaneip = hsbplaneimp.getProcessor(); // the ColorProcessor for the RGB image
			// Places the RGB rendering into a slice of the final 'single channel' RGB stack....
			rgboutstk.setProcessor(hsbplaneip, z+1); // slice #ing starts at 1
		}
		
		// Converts the 1D indexed RGB ImageStack into an ND hyperstack...
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		ImagePlus ratioimp = new ImagePlus("SnrMD Ratio Image", rgboutstk); // end result if single plane image
		
		if (ratioimp.getStackSize() > 1) { // required for HyperStackConverter to work
			ratioimp = HyperStackConverter.toHyperStack(ratioimp, 1, nslices, nframes, "grayscale"); // one channel by definition
		}

		return ratioimp;
	}
		
	
	private ImagePlus coLoc() {
		
		// A colocalization view that highlights in yellow pixels along y=x while keeping rest grayscale.
			// Avoids confusing bright green and yellow in traditional 'merge' image.
			// Also avoids weak 'murky' yellows that are very hard to see
			// Used a single hue b/c by mathemeatical definitions of coloc it does not matter it num or demon is a bit larger
			// By making a dedicated coloc display, you can still discourage users from looking at their 'merge' images.	
			
		double angle, refang, absrefang, normrabsefang, radius, theta; // polar values
		double intnorm = 1; // a normalizer

		// imgstk is a 1D indexed ImageStack
		int stkw = numimgstk.getWidth(); // denimgstk guaranteed to have same dimensions
		int stkh = numimgstk.getHeight();
		int stks = numimgstk.getSize(); // 1D indexed! - includes z and t (single channel by definition)
		
		ImageProcessor hsbplaneip;
		
		// A plane of HSB space.  See ImageStack.isHSB32() for definition
		ImageStack hsbplanestk = ImageStack.create(stkw, stkh, 3, 32);	// 3 plane (one for each color axis), 32 bit, all pixels set to black by default
		hsbplanestk.setSliceLabel("Hue", 1); // required
		hsbplanestk.setSliceLabel("Saturation", 2); // for clarity only
		hsbplanestk.setSliceLabel("Brightness", 3); // for clarity only
		// ImagePlus of the HSB plane
		ImagePlus hsbplaneimp = new ImagePlus("HSB plane", hsbplanestk);
				
		// A 'single channel' 24-bit RGB stack to hold the final output
		ImageStack rgboutstk = ImageStack.create(stkw, stkh, stks, 24); // 24-bit color
		
		int bitdepth = imp.getBitDepth();
		if (bitdepth == 8) {
			intnorm = 255; //2^8 - 1
		}
		if (bitdepth == 16) {
			intnorm = 65535; //2^16 - 1
		}
		
		// Logic and division
		for (int z=0; z<stks; z++) {
			for (int x=0; x<stkw; x++) {
				for (int y=0; y<stkh; y++) {

					// Pixel vals on each channel
					numval = numimgstk.getVoxel(x, y, z);
					denval = denimgstk.getVoxel(x, y, z);
					
					if (numval > 0 && denval > 0) {  // solves divide by zero issue

						// Ratio Calculations
						ratio = numval / denval; // the ratio
						angle = Math.atan(ratio); // 0 - pi/2 radians
						refang = angle - (Math.PI/4); // -pi/4 - +pi/4 -> angle in radian from the line y=x
						absrefang = Math.abs(refang);
						normrabsefang = absrefang / (Math.PI/4); // 0 near x=y -> 1 when y>>x or x>>y
						theta = 1 - normrabsefang;
						
						// Distance Calculations
						radius = Math.sqrt( Math.pow(numval/intnorm, 2) + Math.pow(denval/intnorm, 2) ); // length of the radius 0 -> sqrt(2)
						
				// Sets Hue - x,y near y=x as yellow (0.16667 hue)
					// Tried doing more reddish or greenish as a function of angle, but that distracted from the simplicity of yellow = coloc
						hsbplanestk.setVoxel(x, y, 0, 0.16667); // voxels are indexed starting from 0, so z vox 0 = slice 1
							
				// Sets Saturation - x,y near y=x are most saturated
						// Equation is a logistic function with maximum at y=x	
						hsbplanestk.setVoxel(x, y, 1, 1/(1 + Math.exp(-15*(theta-0.80))) ); // 15 is the growth rate, 0.60 is the sigmoid midpoint
						
				// Sets Brightness - 'intensity modulated' according to the radius
						if (radius < 1) {
							hsbplanestk.setVoxel(x, y, 2, radius);
						} else {
							hsbplanestk.setVoxel(x, y, 2, 1); // asymtotic approximation to keep the image brighter over all
						}
					} 
				}
			}
			
			// Converts HSB plane to a 24-bit RGB plane
			hsbplaneimp.setStack(hsbplanestk);  // sets stack to current hsb stack
			new ImageConverter(hsbplaneimp).convertHSB32ToRGB();
			hsbplaneip = hsbplaneimp.getProcessor(); // the ColorProcessor for the RGB image
			// Places the RGB rendering into a slice of the final 'single channel' RGB stack....
			rgboutstk.setProcessor(hsbplaneip, z+1); // slice #ing starts at 1
		}
		
		// Converts the 1D indexed RGB ImageStack into an ND hyperstack...
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		ImagePlus ratioimp = new ImagePlus("Colocalization Image", rgboutstk); // end result if single plane image
		
		if (ratioimp.getStackSize() > 1) { // required for HyperStackConverter to work
			ratioimp = HyperStackConverter.toHyperStack(ratioimp, 1, nslices, nframes, "grayscale"); // one channel by definition
		}

		return ratioimp;
		
	}
		
	
	private ImagePlus pupMerge() {
		// Pup is only fundamentally useful as a 'merge' display where the 'overlap' color has to be colored and spectrally ordered between the two 'alone' colors. 
			// Ratio displays need not be spectrally ordered or even always colored.... 

		// Radius of circle with center at a,b = 0,0 inscribed within the RGB gamut for each of 100 luminosity values, where luminosity ~= array index.
			// Each radius was found over the portion of the gamut where b>0 and the sector where the angle to the b axis was < pi/3, and the 2D gamut was defined all rgb values in a 'luminosity slab' one luminosity unit thick
			// This sector spans roughly green to red	
			// See matlab code for calculations
		int[] cieradii = {1,2,4,5,7,8,9,10,12,14,15,16,18,19,21,22,23,24,26,27,28,30,31,32,33,34,35,35,37,38,39,
		40,40,41,42,43,44,45,46,46,47,48,49,50,50,51,52,53,54,55,56,56,57,58,59,60,61,61,62,63,64,65,66,66,63,61,
		58,56,54,51,49,47,45,43,41,39,37,35,33,31,30,28,26,24,22,21,19,17,16,15,13,11,10,9,7,6,4,3,2,0};
		
		// These values are for all b>0. This sector was too large and included alot of magenta and cyan
		//int[] cieradii = {1,2,4,5,7,8,9,10,12,13,14,14,16,16,16,17,18,18,18,19,20,20,20,22,22,22,23,24,24,24,25,
		//26,26,27,27,28,28,29,29,30,31,31,31,32,33,33,34,34,35,35,36,36,36,37,38,38,39,39,40,41,41,41,42,43,43,44,
		//44,45,45,46,46,45,43,41,40,38,36,34,32,30,29,27,25,24,22,20,19,17,15,14,13,11,10,8,7,6,4,3,2,0};
		
		double angle, refang, srefang, hypot; // polar values
		double intnorm = 1; // a normalizer
		double l,a,b; // Lab coor values

		// imgstk is a 1D indexed ImageStack
		int stkw = numimgstk.getWidth(); // denimgstk guaranteed to have same dimensions
		int stkh = numimgstk.getHeight();
		int stks = numimgstk.getSize(); // 1D indexed! - includes z and t (single channel by definition)
		
		ImageProcessor labplaneip;
		
		// A plane of HSB space.  See ImageStack.isLab() for definition
		ImageStack labplanestk = ImageStack.create(stkw, stkh, 3, 32);	// 3 plane (one for each color axis), 32 bit, all pixels set to black by default
		labplanestk.setSliceLabel("L*", 1); // required
		labplanestk.setSliceLabel("a*", 2); // for clarity only
		labplanestk.setSliceLabel("b*", 3); // for clarity only
		// ImagePlus of the Lab plane
		ImagePlus labplaneimp = new ImagePlus("LAB plane", labplanestk);
				
		// A 'single channel' 24-bit RGB stack to hold the final output
		ImageStack rgboutstk = ImageStack.create(stkw, stkh, stks, 24); // 24-bit color
		
		int bitdepth = imp.getBitDepth();
		if (bitdepth == 8) {
			intnorm = 255; //2^8 - 1
		}
		if (bitdepth == 16) {
			intnorm = 65535; //2^16 - 1
		}
		
		// Logic and division
		for (int z=0; z<stks; z++) {
			for (int x=0; x<stkw; x++) {
				for (int y=0; y<stkh; y++) {

					// Pixel vals on each channel
					numval = numimgstk.getVoxel(x, y, z);
					denval = denimgstk.getVoxel(x, y, z);
					
					if (numval > 0 && denval > 0) {  // solves divide by zero issue

						// Ratio Calculations
						ratio = numval / denval; // the ratio
						angle = Math.atan(ratio); // 0 - pi/2 radians
						refang = angle - (Math.PI/4); // -pi/4 - +pi/4 -> angle in radian from the line y=x
						srefang = 1.333*refang; // 2pi/3 range of hues.  A full pi range is too large - introduces too many magenta and cyan colors 
						
				// Sets Luminosity - Using greater of num or denum follow the original PUP publication
					// Also consistent with the spirit of a traditional merge image where colors along the diagonal where radius > 1 are not intentionally brighter, only a different color
					if (numval > denval) {
						l = 90*(numval/intnorm); // 90 b/c empirically luminosities upto ~90 are still reasonably 'colorful'. If l is too high, it just looks white
					} else {
						l = 90*(denval/intnorm);
					}
					labplanestk.setVoxel(x, y, 0, l);  // sets the L coordinate

				// Sets a,b coordinates by mapping the ratio angle (pi/2 radians total) proportionally over a sector of the a,b plane (see cieradii notes above)					
					// The length of the radius in the ab plane to the nearest edge of the rgb gamut at the given luminosity
					// Larger radius implies greater saturation
					hypot = cieradii[(int)(Math.floor(l)+1)]; 
							
					a = (double)hypot*Math.sin(srefang);
					b = (double)hypot*Math.cos(srefang);

					labplanestk.setVoxel(x, y, 1, a); // sets the a coordinate
					labplanestk.setVoxel(x, y, 2, b); // sets the a coordinate	

					} 
				}
			}
			
			// Converts Lab plane to a 24-bit RGB plane
			labplaneimp.setStack(labplanestk);  // sets stack to current lab stack
			
			new ImageConverter(labplaneimp).convertLabToRGB();
			labplaneip = labplaneimp.getProcessor(); // the ColorProcessor for the RGB image
			// Places the RGB rendering into a slice of the final 'single channel' RGB stack....
			rgboutstk.setProcessor(labplaneip, z+1); // slice #ing starts at 1
		}
		
		// Converts the 1D indexed RGB ImageStack into an ND hyperstack...
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		ImagePlus ratioimp = new ImagePlus("Perceptually Uniform (PUP) Image", rgboutstk); // end result if single plane image
		
		if (ratioimp.getStackSize() > 1) { // required for HyperStackConverter to work
			ratioimp = HyperStackConverter.toHyperStack(ratioimp, 1, nslices, nframes, "grayscale"); // one channel by definition
		}

		return ratioimp;
	
	}

	private void getChannels() {
		// Gets num and denom channels as ImageStacks (a 3D object with 1D indexing)
		ChannelSplitter chsplitter = new ChannelSplitter();
		numimgstk = chsplitter.getChannel(imp, numch); // numerator
		denimgstk = chsplitter.getChannel(imp, dench); // denominator
			
	}

	// Use in place of run() to call this class headlessly
	public boolean setup(String ratiomethod, ImagePlus imp, int numch, int dench) {
		this.ratiomethod = ratiomethod; // display type
		this.imp = imp; // image to act on
		this.numch = numch; // chs to act on, where first channel == 1 (follows ImageJ convention)
		this.dench = dench;
		
		return check(); // returns true if imp passes the check(), i.e. is suitable for a ratio display
		// Note GUI-based errors are thrown if image does not pass check()	
	}
	

	// TODO - Need a method than will build a 'Scale Bar' image for the 2D displays
		// Final image should be 1024x1024 so text does not get too pixelated
		// Color scale is 512x512 and built by applying the view to two, 8-bit ramps rotated by 90 degrees
		// Font is 24, 32, or 44
		// Build it out on the fly
			// Will likely involve using Overlays and then flattening them
		
		// Separately, could also make a simple calibration bar image for raw ratios.
			// The FIJI Calibration Bar is an overlay on the image which is not ideal.
		

	
}
	
	
