package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 
import emblcmci.*; // add jar of package to class path on compile
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ModifyButtons implements ActionListener {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class ModifySliders_ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	// class wide scope
	ImagePlus imp; // input imp
	ImagePlus chimp; // active channel of input imp
	int curch; // active channel index
	JDialog jd;
	
	public ModifyButtons() {
		
		imp = WindowManager.getCurrentImage(); // active image
		// Pulls current channel of active image
		int curch = imp.getC();
		ChannelSplitter chsplitter = new ChannelSplitter();
		ImageStack chimgstk = chsplitter.getChannel(imp, curch);
		chimp = new ImagePlus("active ch stk", chimgstk);
		
	}
	
	public void histoMatch() { 
		// Directly modifies the ip's of single ch, 3D or 4D stacks
		BleachCorrection_MH bchm = new BleachCorrection_MH(chimp);
		bchm.doCorrection();
		this.replace();
				
	}

	public void expFit() { 
		// Directly modifies the ip's of single ch, 3D or 4D stacks
		BleachCorrection_ExpoFit bcef = new BleachCorrection_ExpoFit(chimp);
		bcef.core();
		this.replace();
				
	}
	
	public void rotate() { 
		// Applies to all channels
		// IJ.doCommand("Rotate...") does not work b/c there are two menu commands called 'Rotate...'!
		// FIX: Would be nice to eventually drive this with a FIJI GUI slider, but a good preview is essential here	
		Rotator rot = new Rotator();
		PlugInFilterRunner pfr = new PlugInFilterRunner(rot,"",""); // String command and String arg are not used by rot in this case
		
	}

	public void crop() { 
		// Applies to all channels
		IJ.setTool(Toolbar.RECTANGLE); // Preselects polyline tool	
		
		// Build GUI.  Ideally this would be written as its own class.
		jd = new JDialog(); // many useful methods inhereted from Dialog and Window
		jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		jd.setTitle("Crop Image");
		
		// Layout
		Container contentPane = jd.getContentPane(); // Best practice to 'add' directly to the JDialog's contentPane which is explicitly a Container
		// Panel to hold elements
		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // blank space
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS)); // a vertical layout on a Container
   
   		// Adds text
		JLabel jla = new JLabel ("Click on Image to Draw Crop Rectangle."); // JLabels are single line only
		jla.setAlignmentX(Component.CENTER_ALIGNMENT); // centers within whatever space is alloted by Layout
		jla.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
		jp.add(jla);  
		
		JLabel jlb = new JLabel ("When Finished, Press OK.");
		jlb.setAlignmentX(Component.CENTER_ALIGNMENT);
		jlb.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 		
		jp.add(jlb);
		
		// Adds OK button
		JButton b = new JButton ("OK");
		b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(this);
		jp.add(b);
		
		jd.add(jp); 
		
		jd.pack();
		GUI.centerOnImageJScreen(jd);
		jd.setVisible(true); 

		// Processing begins in response to button press and resides within ActionListener method below....
	}	
	
	public void subset() { 
		// Applies to all channels
		IJ.doCommand("Make Substack...");
		
	}
	
	
	private void replace() { 
		// Replaces input channel with new output...
		// Works at the level of the ImageStack to avoid updating display until all slices have been modified
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		int[] stkidxs = new int[nframes*nslices];
		int c=0;
		// Gets set of 1D ImageStack indices in input that correspond to all planes of given channel		
		for (int frame=1; frame<=nframes; frame++) { 
			for (int slice=1; slice<=nslices; slice++) {	
				stkidxs[c] = imp.getStackIndex(curch, slice, frame); // returns ImageStack 1D index at this location
				c = c+1;
			}
		}
										
		//  Replaces appropriate input slices with corresponding output slices
		ImageStack chimgstk = chimp.getStack(); // ch-specific output slices
		ImageStack imgstk = imp.getStack();  // all input imp slices
		for (int i = 0; i<stkidxs.length; i++) {
			ImageProcessor chip = chimgstk.getProcessor((i+1));
			imgstk.setProcessor(chip, stkidxs[i]); //stkidx is zero-based
		}	
		
		// Updates display to show final result...
		imp.setStack(imgstk);
		imp.updateAndDraw();  // Redraws image with new output (Not show() since the image is already shown).
		
	}

// Responses to JDialog sub-gui buttons...
	public void actionPerformed(ActionEvent e) {  
		// FIX - Have to getSource to decide if multiple buttons are present...
        jd.setVisible(false);
		jd.dispose();
		
		// The response
		IJ.doCommand("Crop");
		
    } 


	
}