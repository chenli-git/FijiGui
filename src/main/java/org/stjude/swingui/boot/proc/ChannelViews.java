package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.plugin.*;
import ij.plugin.filter.*; 

public class ChannelViews {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class ChannelViews__ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	ImagePlus imp; // class wide scope

	public ChannelViews() {
		imp = WindowManager.getCurrentImage(); // active image
	}
	
	public void showPanels() {
		// See sc/fiji/i5d/Image5D.java
		// FIX: Make way to get out of Image5D -> Could simply make Panelize be a toggle button and then call IJ.doCommand("Image5D to Stack");
		// FIX: Make luts transfer back/forth btwn standard FIJI hyperstack and Image5D
		// FIX: Make way to 'snapshot' entire frame, ie the entire panelized view
		IJ.doCommand("Stack to Image5D"); //Plugins->Image5D
	}

	public void reOrder() {
		ChannelArranger ca = new ChannelArranger();  
		ca.run(""); // Opens dialog
	}

	public void scaleBar() {
		ScaleDialog sd = new ScaleDialog(); // Opens Set Scale dialog
		sd.setup("", imp);
		ImageProcessor ip = imp.getProcessor(); // all ip in an imp will always have the same scale
		sd.run(ip);
		
		ScaleBar sb = new ScaleBar();  // Opens Scale Bar dialog
		sb.run(""); 
	}
	
	public void snapShot() {
		IJ.run("Capture Image"); // from Plugins->Utilities
		ImagePlus img = IJ.getImage();
		img.show();
		int type = img.getType();
        System.out.println("Captured Image Type: " + type);
	}

	
}