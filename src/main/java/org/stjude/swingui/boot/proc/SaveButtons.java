package org.stjude.swingui.boot.proc;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 
import ij.measure.*;
import java.io.*;

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
		
		if (param == 1) { // int representation of checkbox state
			// Save processing steps AND file		
		
			// TO IMPLEMENT - save processing steps as some sort of text file... use existing fiji tools as possible

			IJ.doCommand("Tiff...");

		} else {
			// save file only
			IJ.doCommand("Tiff...");
		
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