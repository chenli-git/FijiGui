package org.stjude.swingui.boot.proc;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 
import ij.measure.*;

import java.awt.FileDialog;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.stjude.swingui.boot.event.ClickRecorder;

public class SaveButtons {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class ModifySliders_ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	// class wide scope
	ImagePlus imp; // input imp
	
	public SaveButtons() {
		
		imp = WindowManager.getCurrentImage(); // active image
		
	}
	
	public void tiff(double param) { 
		if (imp == null) {
			IJ.error("Error", "No active image to save.");
			return;
		}

		FileInfo fi = imp.getOriginalFileInfo();

		// Handle case where file info is null or incomplete (common with CZI files)
		String directory = null;
		String originalName = "Image";
		

		// Show a Save As dialog
		FileDialog fd = new FileDialog(IJ.getInstance(), "Save As TIFF", FileDialog.SAVE);
		
		// Get the last saved directory from ImageJ
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String title = imp.getTitle();
		int dotIndex = title.lastIndexOf(".");
		if (dotIndex > 0) {
			title = title.substring(0, dotIndex); // remove extension
		}
		fd.setFile(title + "_" + timestamp +".tif");
		fd.setVisible(true);

		directory = fd.getDirectory();
		String filename = fd.getFile();
		// ❌ If user clicked Cancel, both filename or directory could be null
		if (filename == null || directory == null) {
			//IJ.log("❌ Save canceled by user.");
			return; // Cancelled – exit the method immediately
		}
		
		// Build the full save path from dialog results
		String savePath = directory + filename;
		
		FileSaver fileSaver = new FileSaver(imp);
		boolean success = fileSaver.saveAsTiff(savePath);
		if (success) {
			System.out.println("✅ Image saved to: " + savePath);
		} else {
			System.err.println("❌ Error saving TIFF image.");
			return;
		}
		// If param == 1, also save the recorded actions as a CSV file
		if (param == 1) {
			ClickRecorder recorder = ClickRecorder.getInstance();
			
			// Get the table model from ClickRecorder
			// We'll use a static method to access it
			try {
				// Export table to CSV file with same base name as the image
				String csvFilePath = savePath.replace(".tif", "_actions.csv");
				recorder.exportTableToCsvWithPath(csvFilePath);
				System.out.println("✅ Actions table exported to: " + csvFilePath);
			} catch (Exception e) {
				System.err.println("❌ Error saving actions table.");
				e.printStackTrace();
			}
		}

	}
	
	public void jpeg(double param) { 

		int quality = (int)param;
		FileSaver fs = new FileSaver(imp);
		fs.setJpegQuality(quality);
		fs.saveAsJpeg();
	
	}

	public void movie(double param1, double param2) { 

		int quality = (int)param1;
		double fps = param2;
		
		// Sets frames per second
		Calibration cal = imp.getCalibration();
		cal.fps = fps;
		imp.setCalibration(cal);
		
		// Hack of AVI_Writer.run() to save the image while avoiding a call to the showDialog() gui method...
		SaveDialog sd = new SaveDialog("Save as AVI...", imp.getTitle(), ".avi");
        String fileName = sd.getFileName();
        if (fileName == null)
            return;
        String fileDir = sd.getDirectory();
        FileInfo fi = imp.getOriginalFileInfo();
        if (fi!=null && imp.getStack().isVirtual() && fileDir.equals(fi.directory) && fileName.equals(fi.fileName)) {
            IJ.error("AVI Writer", "Virtual stacks cannot be saved in place.");
            return;
        }
        try {
			AVI_Writer aw = new AVI_Writer();
			// FIX: quality does NOT set compression level.
			// FileSaver.setJpegQuality(quality) also has no impact in this case....
			// AVI_Writer.writeCompressedFrame() handles saving w/ jpg compression, which inturn calls ImageIO.write() which is a java class
			// Note the AVI_Writer.jpegQuality constant is commented as'not used' and does appear to be unused in the code
            aw.writeImage(imp, fileDir + fileName, aw.JPEG_COMPRESSION, quality); // FIX
            IJ.showStatus("");
        } catch (IOException e) {
            IJ.error("AVI Writer", "An error occured writing the file.\n \n" + e);
        }
        IJ.showStatus("");
		
	}

	
}