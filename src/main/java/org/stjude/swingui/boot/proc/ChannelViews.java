package org.stjude.swingui.boot.proc;

import ij.*;
import ij.io.OpenDialog;
import ij.process.*;
import net.imglib2.algorithm.componenttree.Component;
import sc.fiji.i5d.Image5D;
import ij.plugin.*;
import ij.plugin.filter.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;

import org.stjude.swingui.boot.panel.VisualizePanel;
import java.text.SimpleDateFormat;
import java.util.Date;
import ij.io.FileInfo;
import ij.io.FileSaver;


public class ChannelViews {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class ChannelViews__ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	ImagePlus imp; // class wide scope
	VisualizePanel vp;
	public ChannelViews() {
		imp = WindowManager.getCurrentImage(); // active image
	}
	
	public void showPanels() {
		// See sc/fiji/i5d/Image5D.java
		// FIX: Make way to get out of Image5D -> Could simply make Panelize be a toggle button and then call IJ.doCommand("Image5D to Stack");
		// FIX: Make luts transfer back/forth btwn standard FIJI hyperstack and Image5D
		// FIX: Make way to 'snapshot' entire frame, ie the entire panelized view
		//IJ.run("Duplicate...", "title=Panelized_Image");
		//IJ.selectWindow("Panelized_Image");
		// Duplicate the image while preserving LUT, overlays, and metadata
		ImagePlus imp = IJ.getImage();
		ImagePlus dupImp = imp.duplicate();
		// dupImp.show();
		// IJ.doCommand("Stack to Image5D");

		// Extract dimensions
		int channels = dupImp.getNChannels();
		int slices = dupImp.getNSlices();
		int frames = dupImp.getNFrames();
		ImageStack stack = dupImp.getStack();

		Image5D i5d = new Image5D(
        dupImp.getTitle(),
        stack,
        channels,
        slices,
        frames);
		i5d.setCalibration(dupImp.getCalibration());
		// Set display mode to TILED (mode = 3)
		i5d.setDefaultColors();
		// Transfer LUTs from the original ImagePlus
		for (int c = 1; c <= channels; c++) {
			dupImp.setC(c); // Set channel to get correct LUT
			ColorModel cm = dupImp.getProcessor().getColorModel();
			i5d.setChannelColorModel(c, cm);
			i5d.setDisplayedInOverlay(c, true); // 🔑 Needed for TILED display
		}
		i5d.setDisplayMode(3);
		i5d.setDisplayGrayInTiles(false);  // Optional: if you're using grayscale channels
		// Show the image
		i5d.updateAndDraw();
		i5d.show();
		

	}

	public void reOrder() {
		ChannelArranger ca = new ChannelArranger();  
		ca.run(""); // Opens dialog
	}

	public void resetMax() {
		if (imp == null) return;
		ImageProcessor ip = imp.getProcessor();
    	double min = ip.getMin(); // Preserve current min
    	double autoMax = ip.getStatistics().max;
        ip.setMinAndMax(min, autoMax);
		imp.updateAndRepaintWindow();
	}

	public void resetMin() {
		if (imp == null) return;
		ImageProcessor ip = imp.getProcessor();
		double max = ip.getMax(); // Preserve current max
		double autoMin = ip.getStatistics().min;
        ip.setMinAndMax(autoMin, max);
		imp.updateAndRepaintWindow();
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
		ImagePlus originalImp = WindowManager.getCurrentImage();
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		//IJ.run("Capture Image"); // from Plugins->Utilities
		ImagePlus img = IJ.getImage();
		img.show();
		imp = WindowManager.getCurrentImage();
		String title = imp.getTitle();
		int dotIndex = title.lastIndexOf(".");
		if (dotIndex > 0) {
			title = title.substring(0, dotIndex); // remove extension
		}
		//ImagePlus copy = imp.duplicate();
		ImagePlus copy = new Duplicator().run(imp, 1, imp.getNChannels(), 1, imp.getNSlices(), 1, imp.getNFrames());
		//IJ.run(copy, "Make Composite", "");
		IJ.run(copy, "8-bit", "");
		//IJ.run(copy, "Flatten", ""); 
		// Show a Save As dialog
		FileDialog fd = new FileDialog(IJ.getInstance(), "Save As PNG", FileDialog.SAVE);
		fd.setFile(title + "_" + timestamp +".png");
		fd.setVisible(true);

		String directory = fd.getDirectory();
		String filename = fd.getFile();
		// ❌ If user clicked Cancel, both filename or directory could be null
		if (filename == null || directory == null) {
			IJ.log("❌ Save canceled by user.");
			return; // Cancelled – exit the method immediately
		}
		int type = img.getType();
		
		String savePath = directory + title + "_snap_" + timestamp +".png";
		FileSaver fileSaver = new FileSaver(copy);
		boolean success = fileSaver.saveAsPng(savePath);
		if (success) {
			System.out.println("✅ Image saved to: " + savePath);
		} else {
			System.err.println("❌ Error saving PNG image.");
			return;
		}
	}

	public void copyToSys() {
		imp.copyToSystem();
	}

	public void duplicate() {
		//IJ.run("Duplicate...","duplicate");
		IJ.run("Duplicate...");
	}

	public void sync() {
		IJ.run("Synchronize Windows", "");
	}

	public void saveMovie() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.showMessage("No image is open.");
			return;
		}
		ImagePlus copy = imp.duplicate();
		IJ.run(copy, "RGB Color", "slices keep");

		String title = imp.getTitle();
		int dotIndex = title.lastIndexOf(".");
		if (dotIndex > 0) {
			title = title.substring(0, dotIndex); // remove extension
		}
		// Ask user where to save
		FileDialog fd = new FileDialog(IJ.getInstance(), "Save Movie As AVI", FileDialog.SAVE);
		fd.setFile(title + ".avi");
		fd.setVisible(true);

		String dir = fd.getDirectory();
		String file = fd.getFile();

		if (dir == null || file == null) {
			IJ.log("❌ Save canceled by user.");
			return;
		}

		// Build full path
		String savePath = dir + file;
		// Save using Bio-Formats Exporter
    	IJ.run(copy, "Bio-Formats Exporter", "save=" + savePath);
	}

}