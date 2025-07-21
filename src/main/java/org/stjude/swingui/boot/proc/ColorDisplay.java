package org.stjude.swingui.boot.proc;

import ij.*;
import ij.gui.GenericDialog;
import ij.process.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.net.URL;
import ij.io.FileInfo;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.text.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Paths;
import java.io.*;
public class ColorDisplay {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class ColorDisplay_ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	ImagePlus imp; // class wide scope
	String bid;
	ImagePlus original_imp;
	ImageProcessor originalProcessor;
	private String imagePath;

	public ColorDisplay() {
		imp = WindowManager.getCurrentImage(); // active image
		if (imp == null) {
            IJ.showMessage("No image open.");
            return;
        }
		FileInfo fileInfo = imp.getOriginalFileInfo();
		if (fileInfo != null && fileInfo.directory != null && fileInfo.fileName != null) {
            imagePath = fileInfo.directory + fileInfo.fileName;
        } else {
            imagePath = null;
            //IJ.showMessage("Error: Unable to retrieve image path.");
        }
	}
	
	public void setLUT(String lut_button_id) { 
	
		bid = lut_button_id; // Must be a FIJI Lookup Tables menu system option - case matters (except for "black")
	
		if (imp.getDisplayMode() == IJ.COLOR || imp.getDisplayMode() == IJ.COMPOSITE) {
			this.setLUT();
		}
		
		if (imp.getDisplayMode() == IJ.GRAYSCALE) {  // Type GRAYSCALE implies an absence of an RGB lut (a 'grays' lut is still an RGB lut)
			imp.setDisplayMode(IJ.COLOR); // all channels pseudocolored by default
			// IJ.doCommand("grays"); // keeps other channels APPEARING as grayscale if desired
			this.setLUT();
		}
		
		// Tests on single channel images...
		if (imp.getDisplayMode() == 0) { // indicates only 'not multichannel'
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				TextWindow tx = new TextWindow("Notice", "RGB is not a scientific image format.  Channels undefined.", 500, 500);
			} else {
				this.setLUT();
			}		
		}
	}
	
	private void setLUT() {
		if (bid == "black") {
			this.blackLUT(); // applies a custom black LUT to turn off display of a channel	
		} else {	
			if (bid.equals("mpl-inferno") || bid.equals("mpl-viridis")|| bid.equals("phase") || bid.equals("Orange Hot") || bid.equals("Cyan Hot")) {
				try {
					String lutName = bid + ".lut";
					//URL lutURL = ColorDisplay.class.getClassLoader().getResource("luts/" + lutName);
					URL lutURL = IJ.getClassLoader().getResource("luts/" + lutName);

					// LutLoader lut = new LutLoader();
					// lut.run(Paths.get(lutURL.toURI()).toString());

					File tempLUT = File.createTempFile("lut_", ".lut");
            		tempLUT.deleteOnExit();

					try (InputStream input = lutURL.openStream();
						FileOutputStream output = new FileOutputStream(tempLUT)) {
						byte[] buffer = new byte[1024];
						int length;
						while ((length = input.read(buffer)) > 0) {
							output.write(buffer, 0, length);
						}
					}
					LutLoader lut = new LutLoader();
            		lut.run(tempLUT.getAbsolutePath());


				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error loading LUT.");
				}
				return;
			}	
			IJ.run(bid);  // Works for any lut in menu system - case matters
			
		}
	}
	
	private void blackLUT() {

		LUT blklut = LUT.createLutFromColor(Color.BLACK); // a black LUT
		((CompositeImage)imp).setChannelLut(blklut); // casting existing imp ensure the lut change acts on the existing imp window
		imp.updateAndDraw(); // fixes the display immediately
	}

	public void resetColor() {
		if (imagePath == null) {
            IJ.showMessage("Error: No file path available to reload image.");
            return;
        }

        // Close the current modified image
        imp.changes = false; // Prevents ImageJ from asking to save changes
        imp.close();

		// Reopen the original image
        ImagePlus newImp = IJ.openImage(imagePath);
        if (newImp != null) {
            newImp.show(); // Display the reloaded image
            IJ.showStatus("Image reset to original.");
        } else {
            IJ.showMessage("Error: Failed to reload image.");
        }
	}

	
	
	public void showAll() {
		imp.setDisplayMode(IJ.COMPOSITE);  	// Display all channels together
		
	}
	
	public void showCh() {
		imp.setDisplayMode(IJ.COLOR);  	// Display each channel separately
	}

	public void colorFusion() {
		if (imp == null || imp.getNChannels() < 2) {
            IJ.error("Error", "A multi-channel image is required.");
            return;
        }
		
		//IJ.run(imp, "Apply LUT", "");

		String macroPath = extractResourceToTemp("/scripts/GrayScale_Color_Fusion2.ijm");
		if (macroPath != null) {
			IJ.runMacroFile(macroPath);
		} else {
			IJ.error("Error", "Failed to load macro from resources.");
		}

		
	}

	private String extractResourceToTemp(String resourcePath) {
		try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				System.err.println("Resource not found: " + resourcePath);
				return null;
			}

			// Create a temporary file
			File tempFile = File.createTempFile("macro_", ".ijm");
			tempFile.deleteOnExit();

			// Copy resource contents to the temporary file
			try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
			}

			return tempFile.getAbsolutePath(); // Return the extracted file path
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}