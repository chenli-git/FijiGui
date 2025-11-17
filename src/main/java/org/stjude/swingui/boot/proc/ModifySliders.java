package org.stjude.swingui.boot.proc;

import java.util.Stack;

import org.checkerframework.checker.units.qual.h;

import ij.*;
import ij.process.*;
//import javafx.scene.control.Dialog;
//import javassist.bytecode.analysis.Frame;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*;
import ij.plugin.frame.ContrastAdjuster;
import ij.plugin.frame.Recorder;
import ij.gui.*;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.*;
import java.awt.*;

public class ModifySliders {

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
	private static ModifySliders instance = null;
	private static Stack<ImagePlus> historyStack = new Stack<>(); // Multi-Step Undo History
	double defaultMin = 0;
	double defaultMax = 255;
	double min = 0;
	double max = 255;
	static final int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};

	
// FIX - Don't use the same var names for different things, even if the scope is local - Too confusing!!!!	
	
	public ModifySliders() {
		imp = WindowManager.getCurrentImage(); // active image
		// Pulls current channel of active image
		if (imp == null) {
			IJ.showMessage("No image open.");
			return;
		}
		// Save initial state for Undo
		historyStack.clear();
		System.out.println("New image detected. Undo history cleared.");
        //saveState();
		
        System.out.println("New image detected. Undo history cleared.");

		this.curch = imp.getC();
		this.chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, curch));
		
	}

	// **Singleton Pattern - Ensures only one instance is used**
    public static ModifySliders getInstance() {
		ImagePlus currentImage = WindowManager.getCurrentImage();

        if (instance == null || (instance.imp != null && instance.imp != currentImage)) {
            // if (WindowManager.getCurrentImage() == null) {
            //     System.out.println("No image is open. ModifySliders will not initialize.");
            //     return null;
			// }
			instance = new ModifySliders();
        }

        return instance;
    }

	// **Check if a New Image is Opened**
    private boolean isNewImageOpened() {
        return imp != WindowManager.getCurrentImage(); // Checks if the active image has changed
    }

	private boolean checkImage() {
        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage("No active image found.");
            return false;
        }
        return true;
    }

	// **Save Image State Before Modification**
    private void saveState() {
		long maxMemory = IJ.maxMemory(); // in bytes
		System.out.println("Max memory (MB): " + (maxMemory / (1024 * 1024)) + " MB");
		
		int bytesPerPixel = imp.getBytesPerPixel();  // 1 (8-bit), 2 (16-bit), 4 (32-bit)
		long totalSize = (long) imp.getWidth() * imp.getHeight() * imp.getNSlices() * imp.getNChannels() * imp.getNFrames() * bytesPerPixel;
		System.out.println("Current image size (MB): " + (totalSize*1.0 / (1024 * 1024)) + " MB");
		int maximum_images = 1;
		if (totalSize == 0) {
			maximum_images = 1; // Avoids memory issues with empty images
		} else {
			double ratio = (double) maxMemory / totalSize;
			maximum_images = ratio >= 6.0 ? 1 : 1;
		}

        if (checkImage()) {
			if (historyStack.size() > maximum_images) {
				// **Remove oldest entry when stack exceeds limit**
				historyStack.remove(0);  
				System.out.println("🗑 Oldest state discarded. History size: " + historyStack.size());
			}
            ImagePlus backup = new Duplicator().run(imp);
            historyStack.push(backup);
            System.out.println("State saved. History size: " + historyStack.size());
        }
    }

	// **Public method to save state (for use by other classes)**
    public void saveImageState() {
        saveState();
    }

	// **Undo Last Action**
    public void undoLastAction() {
        if (!historyStack.isEmpty() && checkImage()) {
            //ImagePlus previousState = historyStack.pop();
			ImagePlus previousState = historyStack.pop();
            imp.setStack(previousState.getStack()); // ✅ Restore full stack
			imp.setTitle(previousState.getTitle()); // ✅ Maintain title
			imp.setCalibration(previousState.getCalibration()); // ✅ Restore metadata
			curch = imp.getC();
			chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, curch));
	
			imp.updateAndDraw(); // ✅ Redraw image
			
			// Remove last record from action table
			org.stjude.swingui.boot.event.ClickRecorder.removeLastRecord();
			
            System.out.println("Undo performed. Remaining history: " + historyStack.size());
        } else {
            IJ.showMessage("No previous state to undo.");
        }
    }

	public void smooth(double param) {

		saveState(); 

		if (chimp == null) {  // Check if chimp is null
			IJ.showMessage("Error", "No active image channel found!");
			return;
		}
		this.curch = imp.getC();
		this.chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, curch));
		double sx = param; double sy = param; double sz = 0.3*param; // FIX - Make z scaling smarter
		GaussianBlur3D.blur(this.chimp, sx, sy, sz); // directly modifies chimp (no new instance created)
		
		
		this.replace();
		forceChannelRefresh(imp);
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
				
	}
	
	public void denoise(double param) { 
		saveState();
		float sx = (float) param; float sy = (float) param; float sz = (float) (0.3*param); // FIX - Make z scaling smarter
		// ImageStack chimgstk = chimp.getStack();
		// ImageStack dnchimgstck = Filters3D.filter(chimgstk, Filters3D.MEDIAN, sx, sy, sz);
		// chimp.setStack(dnchimgstck);
		// this.replace();
		// forceChannelRefresh(imp);
		int nSlices = chimp.getNSlices();
		int nFrames = chimp.getNFrames();
		int channel = chimp.getC(); // Get active channel
		// for (int t = 1; t <= nFrames; t++) {  // Time (T)
		// 	for (int z = 1; z <= nSlices; z++) {  // Depth (Z)
		// 		chimp.setPositionWithoutUpdate(channel, z, t); // ✅ Move to correct slice without UI flicker
		// 		IJ.run(chimp, "Median...", "radius=" + param);
		// 	}
		// }
		this.curch = imp.getC();
		this.chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, curch));
		IJ.run(this.chimp, "Median...", "radius=" + param + " stack");
		this.replace();
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
	}
	

	public void sharpen(double param) { 
		// Would be cool to implement the 'gradient subtratction' sharpen here...
		// For now, does unsharpmasking
		// Background subtraction is inherently 2D; slice by slice
		saveState();
		if (chimp == null) {
			IJ.showMessage("No active image found.");
			return;
		}
		float radius = (float) param;
    	String options = "radius=" + radius + " mask=0.60";
		// Store current position
		int currentC = imp.getC();
		int currentZ = imp.getZ();
		int currentT = imp.getT();
		// Get image dimensions
		int nSlices = chimp.getNSlices();
		int nFrames = chimp.getNFrames();
		int channel = chimp.getC(); // Get active channel
	
		// Process all slices in the active channel
		for (int t = 1; t <= nFrames; t++) {  // Time (T)
			for (int z = 1; z <= nSlices; z++) {  // Depth (Z)
				chimp.setPositionWithoutUpdate(channel, z, t); // ✅ Move to correct slice without UI flicker
				IJ.run(chimp, "Unsharp Mask...", options);  // ✅ Apply sharpening to the current slice
			}
		}
		this.curch = imp.getC();
		this.chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, curch));
		// Restore original position
		imp.setPosition(currentC, currentZ, currentT);
		this.chimp.updateAndDraw();
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		this.replace();
		//forceChannelRefresh(imp);
	}
// **** LOTS TO WORK OUT HERE...TEST STANDALONE FIRST ****		
/*		
		// FIX - Need to know imp type to decide if normalization is needed
		// FIX - Need to know if imp is a stack or just a plane
	
		ImageStack chimgstk = chimp.getStack();
		
		// syntax... new StackConverter(imp).convertToGray32();
		
		UnsharpMask usm = new UnsharpMask();
		// Loops over stack
		for (int i=1; i<=chimgstk.size(); i++) {
			ImageProcessor chip = chimgstk.getProcessor(i); // input slice
			usm.sharpenFloat(chfp, radius, 0.5); // weight is fixed; modifies chfp directly
			chimgstk.setProcessor(chfp, i); // in place replacement	
		}
		chimp.setStack(chimgstk);
		this.replace();
*/
	
	
		
	public void subtBkgd(double param) { 

		// Background subtraction is inherently 2D; slice by slice
		saveState();
		double radius = param;
		ImageStack chimgstk = chimp.getStack();
	
		BackgroundSubtracter bs = new BackgroundSubtracter();
		// Loops over stack
		for (int i=1; i<=chimgstk.size(); i++) {
			ImageProcessor chip = chimgstk.getProcessor(i); // input
			bs.rollingBallBackground(chip, radius, false, false, false, true, false); // modifies chip directly
			chimgstk.setProcessor(chip, i); // in place replacement	
		}
		chimp.setStack(chimgstk);
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		this.replace();
		forceChannelRefresh(imp);
	}
	
	public void gamma(double param) { 
		saveState();
		double gamma = param;
		ImageStack chimgstk = chimp.getStack();
	
		// Loops over stack
		for (int i=1; i<=chimgstk.size(); i++) {
			ImageProcessor chip = chimgstk.getProcessor(i); // input
			chip.gamma(gamma);
			chimgstk.setProcessor(chip, i); // in place replacement	
		}
		chimp.setStack(chimgstk);
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		this.replace();
		forceChannelRefresh(imp);
	}
	
	public double[] multiply(double param) { 
		ImageProcessor ip = imp.getProcessor();
		saveState();
		double[] result = apply(chimp, ip);
		int min = (int)imp.getDisplayRangeMin();
		int max = (int)imp.getDisplayRangeMax();
		return result;
		// if (true) return;
		// saveState();
		// double k = param;
		// ImageStack chimgstk = chimp.getStack();
	
		// // Loops over stack
		// for (int i=1; i<=chimgstk.size(); i++) {
		// 	ImageProcessor chip = chimgstk.getProcessor(i); // input
		// 	chip.multiply(k);
		// 	chimgstk.setProcessor(chip, i); // in place replacement	
		// }
		// chimp.setStack(chimgstk);
		// this.replace();
		// forceChannelRefresh(imp);
	}

	public double[] applyLutsToCh(double param) {
		ImageProcessor ip = imp.getProcessor();
		//imp = WindowManager.getCurrentImage(); // active image
		saveState();
		// IJ.run(imp, "Apply LUT", "");
		// imp.resetDisplayRange();   // equivalent to ip.resetMinAndMax()
		// imp.updateAndDraw();
		//double[] result = apply(chimp, ip);
		
		//IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		//double[] result = apply(chimp, ip);
		//return result;
		double[] result = apply(chimp, ip);
		new Thread(() -> {
		ImagePlus imp = WindowManager.getCurrentImage();
			if (imp == null) return;
			IJ.run(chimp, "Apply LUT", "");
			IJ.run(imp, "Enhance Contrast", "saturated=0.35");
			chimp.updateAndDraw();
		}).start();

		//return new double[]{0,245};
		return result;
	}

	public double[] applyLutsToAll(double param) {
		ImageProcessor ip = imp.getProcessor();
		saveState();
		int numChannels = imp.getNChannels();
		int originalC = imp.getC();   // 🟢 remember which channel was active
		double[] range = new double[2];
		// loop through all channels
		for (int c = 1; c <= numChannels; c++) {
			imp.setC(c);                        // activate channel c
			ImagePlus chimp = new ImagePlus("Channel_" + c, imp.getProcessor().duplicate());
			IJ.run(chimp, "Apply LUT", "");
			imp.setProcessor(chimp.getProcessor());
			IJ.run(imp, "Enhance Contrast", "saturated=0.35");
			chimp.close();
		}

		double[] channelRanges = new double[numChannels * 2]; // [c1min,c1max,c2min,c2max,...]
		for (int c = 1; c <= numChannels; c++) {
                        imp.setC(c);
                        channelRanges[(c - 1) * 2] = imp.getDisplayRangeMin();
                        channelRanges[(c - 1) * 2 + 1] = imp.getDisplayRangeMax();
                    }
		
		imp.setC(originalC);
		imp.updateAndDraw();
		this.chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, originalC));
		System.out.println(range);
		//IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		//return range;
		//return new double[]{0,245};
		return channelRanges;
	}

	public void localcorr(String mode) {
		System.out.println("Local correlation mode: " + mode);
		saveState();

		JDialog dialog = new JDialog((Frame) null, "Bleach Correction Processing", false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLayout(new BorderLayout());

		JLabel label = new JLabel("Processing image...");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		dialog.add(label, BorderLayout.CENTER);

		dialog.setSize(300, 50);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);



		// String macroPath = extractResourceToTemp("/scripts/Bleach_Correction_Local_ZT.ijm");
		// if (macroPath != null) {
		// 	IJ.runMacroFile(macroPath);
		// } else {
		// 	IJ.error("Error", "Failed to load macro from resources.");
		// }

		// Run the macro in a background thread
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				String macroPath = extractResourceToTemp("/scripts/Bleach_Correction_Local_ZT.ijm");
				if (macroPath != null) {
					IJ.runMacroFile(macroPath);
				} else {
					IJ.error("Error", "Failed to load macro from resources.");
				}
				return null;
			}

				@Override
				protected void done() {
					dialog.dispose();  // close status window when macro is done
				}
			};

			worker.execute();  // Start macro in background
			IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		}

	public void globalcorr(String mode) {
		System.out.println("Global correlation mode: " + mode);
		saveState();

		JDialog dialog = new JDialog((Frame) null, "Bleach Correction Processing", false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLayout(new BorderLayout());

		JLabel label = new JLabel("Processing image...");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		dialog.add(label, BorderLayout.CENTER);

		dialog.setSize(300, 50);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

		// String macroPath = extractResourceToTemp("/scripts/Bleach_Correction_Global_ZT.ijm");
		// if (macroPath != null) {
		// 	IJ.runMacroFile(macroPath);
		// } else {
		// 	IJ.error("Error", "Failed to load macro from resources.");
		// }

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {
            String macroPath = extractResourceToTemp("/scripts/Bleach_Correction_Global_ZT.ijm");
			String macroPath2 = extractResourceToTemp("/scripts/Bleach_Correction_Global_2nd_ZT.ijm");
			System.out.println(mode);
            if (macroPath != null) {
				if (mode.equals("0")) {
					IJ.runMacroFile(macroPath);
				} else if (mode.equals("1")) {
					IJ.runMacroFile(macroPath2);
				}
                
            } else {
                IJ.error("Error", "Failed to load macro from resources.");
            }
            return null;
        }

			@Override
			protected void done() {
				dialog.dispose();  // close status window when macro is done
			}
		};

		worker.execute();  // Start macro in background
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		//dialog.dispose(); // close it when done
	}

	public void equalizecorr(String mode) {
		System.out.println("Equalize correlation mode: " + mode);
		saveState();

		JDialog dialog = new JDialog((Frame) null, "Bleach Correction Processing", false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLayout(new BorderLayout());

		JLabel label = new JLabel("Processing image...");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		dialog.add(label, BorderLayout.CENTER);

		dialog.setSize(300, 50);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {
            String macroPath = extractResourceToTemp("/scripts/Bleach_Correction_Equalize_ZT.ijm");
            if (macroPath != null) {
                IJ.runMacroFile(macroPath);
            } else {
                IJ.error("Error", "Failed to load macro from resources.");
            }
            return null;
        }

			@Override
			protected void done() {
				dialog.dispose();  // close status window when macro is done
			}
		};

		worker.execute();  // Start macro in background
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
	}



	private double[] apply(ImagePlus imp, ImageProcessor ip) {
		// if (imp.isComposite())
		// 	return;
		int bitDepth = imp.getBitDepth();
		if ((bitDepth==8||bitDepth==16) && !IJ.isMacro()) {
			String msg = "WARNING: the pixel values will\nchange if you click \"OK\".";
			if (!IJ.showMessageWithCancel("Apply Lookup Table?", msg))
				return new double[]{9999,9999};
		}
		// String option = null;
		// int type = imp.getType();
		// boolean	RGBImage = type==ImagePlus.COLOR_RGB;
		// System.out.println("RGBImage: " + RGBImage);
		// if (RGBImage)
		// 	imp.unlock();
		// if (!imp.lock())
		// 	return new double[]{0,255};
		
		// if (bitDepth==32) {
		// 	IJ.beep();
		// 	IJ.error("\"Apply\" does not work with 32-bit images");
		// 	imp.unlock();
		// 	return new double[]{0,255};
		// }
		// int range = 256;
		// if (bitDepth==16) {
		// 	range = 65536;
		// 	int defaultRange = imp.getDefault16bitRange();
		// 	System.out.println("defaultRange: " + defaultRange);
		// 	if (defaultRange>0)
		// 		range = (int)Math.pow(2,defaultRange)-1;
		// }
		// int tableSize = bitDepth==16?65536:256;
		// int[] table = new int[tableSize];
		int min = (int)chimp.getDisplayRangeMin();
		int max = (int)chimp.getDisplayRangeMax();
		return new double[]{min,max};
		// System.out.println("Min: " + min + " Max: " + max);
		// System.out.println("Table size: " + tableSize + " Min: " + min + " Max: " + max + " Range: " + range);
		// if (IJ.debugMode) IJ.log("Apply: mapping "+min+"-"+max+" to 0-"+(range-1));
		// for (int i=0; i<tableSize; i++) {
		// 	if (i<=min)
		// 		table[i] = 0;
		// 	else if (i>=max)
		// 		table[i] = range-1;
		// 	else
		// 		table[i] = (int)(((double)(i-min)/(max-min))*range);
		// }
		// ip.setRoi(imp.getRoi());
		// if (imp.getStackSize()> 1 && !imp.isComposite()) {
		// 	ImageStack stack = imp.getStack();
		// 	YesNoCancelDialog d = new YesNoCancelDialog(new Frame(),
		// 		"Entire Stack?", "Apply LUT to all "+stack.size()+" stack slices?");
		// 	if (d.cancelPressed())
		// 		{imp.unlock(); return new double[]{9999,9999};}
		// 	if (d.yesPressed()) { 
		// 		if (imp.getStack().isVirtual()) {
		// 			imp.unlock();
		// 			IJ.error("\"Apply\" does not work with virtual stacks. Use\nImage>Duplicate to convert to a normal stack.");
		// 			return new double[]{min,max};
		// 		}
		// 		IJ.run(imp, "Apply LUT", "stack");
		// 		System.out.println("Applied to entire stack.");

		// 	} else {
		// 		IJ.run(imp, "Apply LUT", "");
		// 		System.out.println("Applied to current slice.");
		// 	}
		// } else {
		// 	ip.snapshot();
		// 	ip.applyTable(table);
		// 	ip.reset(ip.getMask());
		// 	System.out.println("Applied table to current image.");
		// }
		// //reset(imp, ip);
		// imp.changes = true;
		// imp.unlock();
		// imp.updateAndDraw();
		// return new double[]{min,max};
	}
	
	void reset(ImagePlus imp, ImageProcessor ip) {
		int type = imp.getType();
		boolean	RGBImage = type==ImagePlus.COLOR_RGB;
		if (RGBImage)
		   ip.reset();
	   int bitDepth = imp.getBitDepth();
	   if (bitDepth==16 || bitDepth==32) {
		   imp.resetDisplayRange();
		   defaultMin = imp.getDisplayRangeMin();
		   defaultMax = imp.getDisplayRangeMax();
	   }
	   min = defaultMin;
	   max = defaultMax;
	   setMinAndMax(imp, min, max);
   }

   void setMinAndMax(ImagePlus imp, double min, double max) {
	boolean rgb = imp.getType()==ImagePlus.COLOR_RGB;
	int channel = imp.getChannel();
    int channels = channelConstants[channel-1];
	if (channels!=7 && rgb)
		imp.setDisplayRange(min, max, channels);
	else
		imp.setDisplayRange(min, max);
}

	private void applyRGBStack(ImagePlus imp) {
		double min = imp.getDisplayRangeMin();
		double max = imp.getDisplayRangeMax();
		System.out.println("Min: " + min + " Max: " + max);
	}


	public int getActiveChannel() {
		imp = WindowManager.getCurrentImage(); // active image
		// Pulls current channel of active image
		if (imp == null) {
			IJ.showMessage("No image open.");
			return -1;
		}
		return imp.getC();
	}
	
	
	private void replace() { 

		imp = WindowManager.getCurrentImage();
    
		if (imp == null || chimp == null) {
			IJ.showMessage("Error: No active image found.");
			return;
		}

		// **Ensure chimp is updated to match the new image**
		curch = imp.getC();
		chimp = new ImagePlus("active ch stk", ChannelSplitter.getChannel(imp, curch));

		// **Get original image stack and active channel**
		ImageStack originalStack = imp.getStack();
		ImageStack modifiedStack = chimp.getStack();

		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		int nchannels = imp.getNChannels();

		// **Check if modifiedStack has the correct number of slices**
		System.out.println("Modified stack size: " + modifiedStack.getSize());
		System.out.println("Expected size: " + (nslices * nframes));
		if (modifiedStack.getSize() != nslices * nframes) {
			IJ.showMessage("Error: Modified stack has incorrect number of slices.");
			return;
		}

		int modifiedStackIndex = 1; // Modified stack indices start at 1

		//**Replace only the active channel in the original stack**
		for (int frame = 1; frame <= nframes; frame++) {
			for (int slice = 1; slice <= nslices; slice++) {
				int originalIndex = imp.getStackIndex(curch, slice, frame);
				int modifiedIndex = (frame - 1) * nslices + slice;

				if (modifiedIndex > modifiedStack.getSize()) {
					System.out.println("Error: Modified stack index out of bounds.");
					return;
				}

				ImageProcessor modifiedProcessor = modifiedStack.getProcessor(modifiedIndex);
				originalStack.setProcessor(modifiedProcessor, originalIndex);
			}
		}

		imp.setStack(originalStack);
		imp.updateAndDraw();
	}

	private void forceChannelRefresh(ImagePlus imp) {
		if (imp == null || imp.getNChannels() == 1) {
			return;  // No need to switch channels if there's only one
		}
	
		int originalChannel = imp.getC();  // Store current channel
	
		// **Find an alternative channel to switch to**
		int tempChannel = (originalChannel == 1) ? 2 : 1;  
	
		// **Switch to a different channel**
		imp.setC(tempChannel);
		imp.updateAndDraw();  
	
		// **Switch back to the original channel**
		imp.setC(originalChannel);
		imp.updateAndDraw();  
	
		System.out.println("✅ Channel switched to " + tempChannel + " and back to " + originalChannel);
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


