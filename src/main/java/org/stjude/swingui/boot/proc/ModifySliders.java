package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 

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
	
// FIX - Don't use the same var names for different things, even if the scope is local - Too confusing!!!!	
	
	public ModifySliders() {
		
		imp = WindowManager.getCurrentImage(); // active image
		// Pulls current channel of active image
		curch = imp.getC();
		ChannelSplitter chsplitter = new ChannelSplitter();
		ImageStack chimgstk = chsplitter.getChannel(imp, curch);
		chimp = new ImagePlus("active ch stk", chimgstk);
		
		// FIX? - Will above work if image is a single plane (not a multichannel stack)?
		
	}
	
	public void smooth(double param) { 
		
		double sx = param; double sy = param; double sz = 0.3*param; // FIX - Make z scaling smarter
		GaussianBlur3D.blur(chimp, sx, sy, sz); // directly modifies chimp (no new instance created)
		this.replace();
				
	}
	
	public void denoise(double param) { 

		float sx = (float) param; float sy = (float) param; float sz = (float) (0.3*param); // FIX - Make z scaling smarter
		ImageStack chimgstk = chimp.getStack();
		ImageStack dnchimgstck = Filters3D.filter(chimgstk, Filters3D.MEDIAN, sx, sy, sz);
		chimp.setStack(dnchimgstck);
		this.replace();
	}

	public void sharpen(double param) { 
		// Would be cool to implement the 'gradient subtratction' sharpen here...
		// For now, does unsharpmasking
		// Background subtraction is inherently 2D; slice by slice
		double radius = param;
		
		
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
	}
	
		
	public void subtBkgd(double param) { 

		// Background subtraction is inherently 2D; slice by slice
		double radius = param;
		ImageStack chimgstk = chimp.getStack();
	
		BackgroundSubtracter bs = new BackgroundSubtracter();
		// Loops over stack
		for (int i=1; i<=chimgstk.size(); i++) {
			ImageProcessor chip = chimgstk.getProcessor(i); // input
			bs.rollingBallBackground(chip, radius, false, false, false, true, true); // modifies chip directly
			chimgstk.setProcessor(chip, i); // in place replacement	
		}
		chimp.setStack(chimgstk);
		this.replace();
		
	}
	
	public void gamma(double param) { 
	
		double gamma = param;
		ImageStack chimgstk = chimp.getStack();
	
		// Loops over stack
		for (int i=1; i<=chimgstk.size(); i++) {
			ImageProcessor chip = chimgstk.getProcessor(i); // input
			chip.gamma(gamma);
			chimgstk.setProcessor(chip, i); // in place replacement	
		}
		chimp.setStack(chimgstk);
		this.replace();
	
	}
	
	public void multiply(double param) { 
	
		double k = param;
		ImageStack chimgstk = chimp.getStack();
	
		// Loops over stack
		for (int i=1; i<=chimgstk.size(); i++) {
			ImageProcessor chip = chimgstk.getProcessor(i); // input
			chip.multiply(k);
			chimgstk.setProcessor(chip, i); // in place replacement	
		}
		chimp.setStack(chimgstk);
		this.replace();
	
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
			ImageProcessor chip = chimgstk.getProcessor((i+1)); // FIX? if a ch has z and t would slices be strictly sequential??
			imgstk.setProcessor(chip, stkidxs[i]); //stkidx is zero-based
		}	
		
		// Updates display to show final result...
		imp.setStack(imgstk);
		imp.updateAndDraw();  // Redraws image with new output (Not show() since the image is already shown).
		
	}
	
}