// Code grafted from ImageJ ContrastAdjuster.java.  See Contrast_.java for a plugin version of this graft than can be used for testing purposes.
// Only responds to requests for constrast changes.  (No gui functionality)
package org.stjude.swingui.boot.proc;
	 
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;

// Hack of ContrastAdjuster.java to implement RESPONSES TO Min and Max sliders, and Auto and Reset buttons, which are now rendered in FIJI GUI
// RELIES CONSTANTLY ON IJ CLASSES, SO CAN ONLY BE INSTANTIATED AS PART OF A PLUGIN

public class Contrast implements Runnable, ActionListener, AdjustmentListener {

    static final int AUTO_THRESHOLD = 5000; // used in autoAdjust()
    static final int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};

    Thread thread;

	private static Contrast instance;
	
	JFrame jf; // the gui window 
	JButton autoB, resetB; // button instances - FIJI GUI uses JButton, not Button
    Scrollbar minSlider, maxSlider;  // scrollbar instances

	// Scrollbar setup vars
    int minSliderValue=-1, maxSliderValue=-1;
    int sliderRange = 256;
    double min, max; // min and max scrollbar positions
    boolean doAutoAdjust,doReset; // indicates if button has been pressed

	int previousImageID;
    int previousType;
    int previousSlice = 1;

    double previousMin, previousMax;
    double defaultMin, defaultMax;
    boolean RGBImage;

    boolean done;
    int autoThreshold;
    int channels = 7; // RGB

	// Provides initial state of gui elements
	public Contrast(Scrollbar minSlider, Scrollbar maxSlider, JButton autoB, JButton resetB) {
		// Ensures instances of gui elements are not null once slider events are triggered.  Notice logic in listener methods.
		this.minSlider = minSlider; 
		this.maxSlider = maxSlider;
		this.autoB = autoB;
		this.resetB = resetB;
	}
	
	//  Initialization.  Analogous to the run(String arg) method of a plugin, but here is called from FIJI GUI
		// Could have done through a constructor, but this way keeps the code closer to the plugin version
	public void run(String arg) { 	
	    instance = this;

		IJ.register(Contrast.class); // protects from garbage collection, needed due to the multithreading?
		
		// Starts a new thread to handle the execution of events
		thread = new Thread(this, "Contrast"); // opens 'this' in a new thread.  string is just an id
		thread.start(); // tiggers a call to Runnable run() method below...
		setup();
	}


	// ---- RESPONSES TO EVENTS	----
    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {  // scrollbar events 		
		Object source = e.getSource();
        if (source==minSlider) // expects minSlider to not be null
            minSliderValue = minSlider.getValue();
        if (source==maxSlider) // expects maxSlider to not be null
            maxSliderValue = maxSlider.getValue();
        notify(); // inhereted from java Object.  'Wakes up' the thread that does processing by kicking off the run() method (below...)
    }

	public synchronized void actionPerformed(ActionEvent e) {  // button events
        JButton b = (JButton)e.getSource();
        if (b==null) return;
        if (b==resetB) // expects resetB to not be null
            doReset = true; // Note a response is not direct but rather a boolean is set to execute an IF later...
        else if (b==autoB)  // expects autoB to not be null
            doAutoAdjust = true;
        notify();
    }

	// ---- Set method for previousImageID ----
	// Event calls to windowActivated() have to trigger a call to setup() and reset of 'previousImageID' var.
	// This is now done from the FIJI GUI ToolbarTab class
	public void resetPreviousImageID() { // gets called in response to windowActived events on the FIJI GUI JFrame
		previousImageID = 0;
	}
	
	
	// -------- Initialization of scrollbar properties section ---------	
	// #1 Kickoff method. Gets current image, error checks it, and initializes the gui display and scrollbar scaling based on image properties
	//  Do NOT handle any user interaction... (that all happens in run())
    public void setup() {  // public because it also gets called in response to windowActived events on the FIJI GUI JFrame
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp!=null) {
            if (imp.getType()==ImagePlus.COLOR_RGB && imp.isLocked())
                return;
            setup(imp);
            imp.updateAndDraw();
        }
    }

    ImageProcessor setup(ImagePlus imp) {
        Roi roi = imp.getRoi();
        if (roi!=null) roi.endPaste();
        ImageProcessor ip = imp.getProcessor(); // current plane shown on screen
        int type = imp.getType();
        int slice = imp.getCurrentSlice();
        RGBImage = type==ImagePlus.COLOR_RGB;
        if (imp.getID()!=previousImageID || type!=previousType || slice!=previousSlice)
            setupNewImage(imp, ip);
        previousImageID = imp.getID();
        previousType = type;
        previousSlice = slice;
        return ip;
    }

	// Sets up scrollbar range based on image properties
    void setupNewImage(ImagePlus imp, ImageProcessor ip)  {
        Undo.reset();
        previousMin = min; // -> provided by scrollbar
        previousMax = max; // -> provided by scrollbar
        boolean newRGBImage = RGBImage && !((ColorProcessor)ip).caSnapshot();
        if (newRGBImage) {
            ip.snapshot();
            ((ColorProcessor)ip).caSnapshot(true);
        }
        double min2 = imp.getDisplayRangeMin();
        double max2 = imp.getDisplayRangeMax();
        if (newRGBImage) {
            min2=0.0;
            max2=255.0;
        }
        int bitDepth = imp.getBitDepth();
        if (bitDepth==16 || bitDepth==32) {
            Roi roi = imp.getRoi();
            imp.deleteRoi();
            ImageStatistics stats = imp.getRawStatistics();
            defaultMin = stats.min;
            defaultMax = stats.max;
            imp.setRoi(roi);
        } else {
            defaultMin = 0;
            defaultMax = 255;
        }
        setMinAndMax(imp, min2, max2);
        min = imp.getDisplayRangeMin();
        max = imp.getDisplayRangeMax();
        int valueRange = (int)(defaultMax-defaultMin);
        int newSliderRange = valueRange;
        if (newSliderRange>640 && newSliderRange<1280)
            newSliderRange /= 2;
        else if (newSliderRange>=1280)
            newSliderRange /= 5;
        if (newSliderRange<256) newSliderRange = 256; // slider range ultimately becomes either 256 or 1024
        if (newSliderRange>1024) newSliderRange = 1024;
        double displayRange = max-min;
        if (valueRange>=1280 && valueRange!=0 && displayRange/valueRange<0.25)
            newSliderRange *= 1.6666;
        if (newSliderRange!=sliderRange) {
            sliderRange = newSliderRange;
            updateScrollBars(null, true);
        } else
            updateScrollBars(null, false);
        if (!doReset)
			autoThreshold = 0;
        if (imp.isComposite())
            IJ.setKeyUp(KeyEvent.VK_SHIFT);
    }

    void setMinAndMax(ImagePlus imp, double min, double max) {
        boolean rgb = imp.getType()==ImagePlus.COLOR_RGB;
        if (channels!=7 && rgb)
            imp.setDisplayRange(min, max, channels);
        else
            imp.setDisplayRange(min, max);
    }

   
	// Makes scrollbars also respond to each other
    void updateScrollBars(Scrollbar sb, boolean newRange) {
        if (minSlider!=null && (sb==null || sb!=minSlider)) {
            if (newRange)
                minSlider.setValues(scaleDown(min), 1, 0,  sliderRange);
            else
                minSlider.setValue(scaleDown(min));
        }
        if (maxSlider!=null && (sb==null || sb!=maxSlider)) {
            if (newRange)
                maxSlider.setValues(scaleDown(max), 1, 0,  sliderRange);
            else
                maxSlider.setValue(scaleDown(max));
        }
    }

    int scaleDown(double v) {
        if (v<defaultMin) v = defaultMin;
        if (v>defaultMax) v = defaultMax;
        return (int)((v-defaultMin)*(sliderRange-1.0)/(defaultMax-defaultMin));
    }




// ---- EXECUTE RESPONSES TO USER INTERACTION SECTION ----

   static final int RESET=0, AUTO=1, MIN=5, MAX=6;

    // run() defined by interface Runnable and is called when new thread starts (Note no relationship to run(String arg), which takes an arguement and is called by imagej)
    public void run() {
        while (!done) {
            synchronized(this) {
                try {wait();}
                catch(InterruptedException e) {}
            }
            doUpdate();
        }
    }

	// #2 Kickoff method.  Responds to user gui events.
    void doUpdate() {
        ImagePlus imp;
        ImageProcessor ip;
        int action;
        int minvalue = minSliderValue; // -> provided by min scrollbar
        int maxvalue = maxSliderValue; // -> provided by max scrollbar
        if (doReset) action = RESET; // -> doReset provided by press of Reset button
        else if (doAutoAdjust) action = AUTO; // -> doAutoAdjust provided by press of Auto button
        else if (minSliderValue>=0) action = MIN;
        else if (maxSliderValue>=0) action = MAX;
        else return;
        minSliderValue = maxSliderValue = -1;
        doReset = doAutoAdjust = false;
        imp = WindowManager.getCurrentImage(); // current image
        if (imp==null) {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        } else if (imp.getOverlay()!=null && imp.getOverlay().isCalibrationBar()) {
            IJ.beep();
            IJ.showStatus("Has calibration bar");
            return;
        }
        ip = imp.getProcessor();
        if (RGBImage && !imp.lock())
            {imp=null; return;}
        switch (action) {
            case RESET:
                reset(imp, ip);
                break;
            case AUTO: autoAdjust(imp, ip); break;
            case MIN: adjustMin(imp, ip, minvalue); break;
            case MAX: adjustMax(imp, ip, maxvalue); break;
        }
        if ((IJ.shiftKeyDown()||(channels==7)) && imp.isComposite()) // removed test of 'balance', which is for Color Balance gui
            ((CompositeImage)imp).updateAllChannelsAndDraw();
        else
            imp.updateChannelAndDraw();
        if (RGBImage)
            imp.unlock();
    }

    void adjustMin(ImagePlus imp, ImageProcessor ip, double minvalue) {
        resetRGB(ip);
        min = defaultMin + minvalue*(defaultMax-defaultMin)/(sliderRange-1.0);
        if (max>defaultMax)
            max = defaultMax;
        if (min>max)
            max = min;
        setMinAndMax(imp, min, max);
        if (min==max)
            setThreshold(ip);
        if (RGBImage) doMasking(imp, ip);
        updateScrollBars(minSlider, false);
    }

    void adjustMax(ImagePlus imp, ImageProcessor ip, double maxvalue) {
        resetRGB(ip);
        max = defaultMin + maxvalue*(defaultMax-defaultMin)/(sliderRange-1.0);
        //IJ.log("adjustMax: "+maxvalue+"  "+max);
        if (min<defaultMin)
            min = defaultMin;
        if (max<min)
            min = max;
        setMinAndMax(imp, min, max);
        if (min==max)
            setThreshold(ip);
        if (RGBImage) doMasking(imp, ip);
        updateScrollBars(maxSlider, false);
    }
    
	// Restores image outside non-rectangular roi.
	// Always called in context of an RGB image
    void doMasking(ImagePlus imp, ImageProcessor ip) {
        ImageProcessor mask = imp.getMask();
        if (mask!=null) {
            Rectangle r = ip.getRoi();
            if (mask.getWidth()!=r.width||mask.getHeight()!=r.height) {
                ip.setRoi(imp.getRoi());
                mask = ip.getMask();
            }
            ip.reset(mask);
        }
    }	
	
    private void resetRGB(ImageProcessor ip) {
        if (!(ip instanceof ColorProcessor))
            return;
        if (ip.getMin()==0 && ip.getMax()==255 && !((ColorProcessor)ip).caSnapshot()) {
            ip.snapshot();
            ((ColorProcessor)ip).caSnapshot(true);
        }
    }

	// Creates a binary dispay when min==max
    void setThreshold(ImageProcessor ip) {
        if (!(ip instanceof ByteProcessor))
            return;
        if (((ByteProcessor)ip).isInvertedLut())
            ip.setThreshold(max, 255, ImageProcessor.NO_LUT_UPDATE);
        else
            ip.setThreshold(0, max, ImageProcessor.NO_LUT_UPDATE);
    }

	//  Implements Reset button
	void reset(ImagePlus imp, ImageProcessor ip) { 
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
        updateScrollBars(null, false);
        autoThreshold = 0;
    }

	// Implements Auto contrast button
    void autoAdjust(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            ip.reset();
        ImageStatistics stats = imp.getRawStatistics();
        int limit = stats.pixelCount/10;
        int[] histogram = stats.histogram;
        if (autoThreshold<10)
            autoThreshold = AUTO_THRESHOLD;
        else
            autoThreshold /= 2;
        int threshold = stats.pixelCount/autoThreshold;
        int i = -1;
        boolean found = false;
        int count;
        do {
            i++;
            count = histogram[i];
            if (count>limit) count = 0;
            found = count> threshold;
        } while (!found && i<255);
        int hmin = i;
        i = 256;
        do {
            i--;
            count = histogram[i];
            if (count>limit) count = 0;
            found = count > threshold;
        } while (!found && i>0);
        int hmax = i;
        Roi roi = imp.getRoi();
        if (hmax>=hmin) {
            if (RGBImage) imp.deleteRoi();
            min = stats.histMin+hmin*stats.binSize;
            max = stats.histMin+hmax*stats.binSize;
            if (min==max)
                {min=stats.min; max=stats.max;}
            setMinAndMax(imp, min, max);
            if (RGBImage && roi!=null) imp.setRoi(roi);
        } else {
            reset(imp, ip);
            return;
        }
        updateScrollBars(null, false);
    }


} // EO Contrast class


