// Bleach Correction
// TODO - Make Channel Specific

// For each slice in a stack...
// 1.  Do an automatic (intensity independent) segmentation and measure avg intensity within the mask
// 2.  If the % area of the mask is 'sufficiently large', use the intensity measurement for fitting
// 3.  Fit a line according to the data points
// 4.  Adjust the data so that the slope of the fit line is zero.

title = getTitle();
getDimensions(width, height, channels, slices, frames);

Image.removeScale;
setBatchMode(true);

// Determine usable dimension
if (slices > 1 && frames > 1) {
	showMessage("Error", "Only one of slices (Z) or frames (T) can be >1.");
	exit();
}

if (slices > 1) {
	useSlices = true;
	depth = slices;
} else if (frames > 1) {
	useSlices = false;
	depth = frames;
} else {
	showMessage("Error", "This macro requires either Z-slices or T-frames.");
	exit();
}

// Handle channel selection
if (channels > 1) {
	Stack.getPosition(c, z, t);
	activeChannel = c;
} else {
	activeChannel = 1;
}


getMeasurements(title); // returns a 'Measurements' table containing 'Area','Mean','Slice'

// --- Get Measurement table data ---
selectWindow("Measurements");
areas = Table.getColumn("Area"); 
means = Table.getColumn("Median"); // avg intensity - TODO: Better to use median
planes = Table.getColumn("Plane"); // can't use 'slices' since that is a fiji variable

if (isOpen("Measurements")) {
    selectWindow("Measurements");
    run("Close");
}



// --- Select data to be used to estimate decay ---
positions = newArray();
values = newArray();

selectWindow(title);
imgarea = width*height;
	
p = 0; //counter
for (i = 0; i < planes.length; i++) {
	// >5% of slice must be signal to be included
	if ( (areas[i]/imgarea) > 0.02 ) {
		values[p] = means[i];
		positions[p] = planes[i];
		p = p+1;
	} // EO if
} // EO for

/*// TESTING
Table.create("PV Data");
Table.setColumn("Position", positions);
Table.setColumn("Values", values);
Table.update();
*/

if (positions.length > 9) {  // Must have at least 11 qualifying points
	
	//--- Global Decay Correction ----
	// 'Flatten' a fit curve that is based on all data points
	// Fixes global trends such as bleaching or light scatter
	
	// Global fitting function - usually linear or exponential
		// Linear fit extrapolates more robustly and fits as well as exp on real data
		// Real data can stray very far from fit curve due to changes in staining patterns
	//Fit.doFit("Straight Line", positions, values); // must be monotonic
	Fit.doFit("2nd Degree Polynomial", positions, values);
	// Finds 'brightest' side of fit curve and will scale up other slices to match
	a = Fit.f(planes[0]);
	b = Fit.f(planes[planes.length-1]);
	scalings = newArray(planes.length); // scaling factor for each plane
	if (a >= b) {
		bsv = a;
		scalings[0] = 1.0; // no scaling of brightest slice  
		// Find global slice scaling factors computationally (since only linear and exponential have nice analytical solutions)	
		for (i = 1; i < planes.length; i++) { // array indices
			csv = Fit.f(planes[i]); // fit uses slice indexes
			scalings[i] = bsv/csv;
			print(scalings[i]);
		}
	} else {
		bsv = b;
		scalings[planes.length-1] = 1.0; // no scaling of brightest slice  
		for (i = 0; i < (planes.length-1); i++) { // array indices
			csv = Fit.f(planes[i]);
			scalings[i] = bsv/csv; // always >= 1
			//print(scalings[i]);
		}
	}
	
	// Apply global correction to stack
	// Apply global correction to stack
	correctedMeans = newArray(depth);
	correctedAreas = newArray(depth);
	selectWindow(title);
	//Stack.setDisplayMode("composite");
	for (i = 0; i < planes.length; i++) {
		
		//setSlice(i+1); // slice index
		if (useSlices) {
			Stack.setPosition(activeChannel, planes[i], 1);
		} else {
			Stack.setPosition(activeChannel, 1, planes[i]);
		}
		run("Multiply...", "value="+scalings[i]+" slice");
		updateDisplay();
		
		run("Duplicate...", "title=slice");
		input = "slice";
		segment(input);  // creates 'mask'
		run("Create Selection");
		selectWindow(title);
		run("Restore Selection");
		correctedMeans[i] = getValue("Median");
		run("Select None");
		close("mask");
		close("slice");
		
		//print((i+1)+"  "+scalings[i]); // TESTING
		
	} // EO for

	
	Plot.create("Global method Intensity vs Frame", "Frame", "Median Signal ROI Intensity");
	Plot.setLineWidth(3);
	Plot.setColor("black");
	Plot.add("line", planes, means);
	Plot.setColor("red");
	Plot.add("line", planes, correctedMeans);
	Plot.setLegend("pre-correction\tpost-correction...", "bottom-left");
	Plot.show();
	
	// --- Local decay correction -----
	// Adjust based on a local smoothing of (a neighborhood of) the data
	// Fixes local issues such as light source power fluctuations or bleaching at specific slices	
	

} else {
	showMessage("Bleach Correction", "Bleach correction is not possible due to insufficient data points.\nOnly " + positions.length + " usable slices detected.");
}
setBatchMode("exit and display");


// ----------  FUNCTIONS ----------------

function segment(input) { 

	// User inputs a feature size, say btwn 5->55 which is used to create the low pass
	
	// Find background offset from MIN
	selectWindow(input);
	run("32-bit");
	run("Gaussian Blur...", "sigma=1");
	run("Duplicate...", "title=LP");
	run("Gaussian Blur...", "sigma=11");  // USER INPUT
	imageCalculator("Subtract create", input, "LP");
	rename("HP");
	//makeThreshold(1.0);  // fixed should be ok for this purpose
	th = 0.25*getValue("StdDev");
	makeThreshold(th);
	rename("mask"); // RETURNS MASK
	//cleanup
	close(input);
	close("LP");

}  // eo funciton segment


function makeThreshold(thresh) {
	// Ensures background of final result will be zeros and be displayed black
	run("Options...", "iterations=1 count=1 black");

	// Executes threshold as needed for bit depth
	bits = bitDepth();
	if (bits == 8) {
		setThreshold(thresh, 255);
		setOption("BlackBackground", true);
		run("Convert to Mask");
	}
	if (bits == 16) {
		setMinAndMax(thresh, 65535);
		run("Apply LUT");
		setOption("ScaleConversions", true);
		run("8-bit");
		setThreshold(1, 255);
		setOption("BlackBackground", true);
		run("Convert to Mask");
	}
	if (bits == 32) {
		getMinAndMax(min, max);
		setMinAndMax(thresh, max);
		setOption("ScaleConversions", true);
		run("8-bit");
		setThreshold(1, 255);
		setOption("BlackBackground", true);
		run("Convert to Mask");
	}	
	if (bits == 24) { // RGB color - thresh is interpretted as a luminance
		run("8-bit");
		setThreshold(thresh, 255);
		setOption("BlackBackground", true);
		run("Convert to Mask");
	}
	
} // EO makeThreshold



function getMeasurements(title) { 
// Measures image intensity above an automatic threshold (intensity independent)

	areas = newArray(depth);
	means = newArray(depth);
	planenums = newArray(depth);

	for (i = 1; i <= depth; i++) {
    	
    		selectWindow(title);
    		// setSlice(i);
    		// Handle channel selection
			if (useSlices) {
				Stack.setPosition(activeChannel,i, 1);
			} else {
				Stack.setPosition(activeChannel, 1, i);
			}
    		run("Duplicate...", "title=slice");
    		input = "slice";
    		segment(input);  // returns 'mask' image

			// transfer mask selection to stack
			run("Create Selection");
			selectWindow(title);
			run("Restore Selection");
			
			// Take measurements
			areas[i-1] = getValue("Area");
			means[i-1] = getValue("Median"); // median less sensitve changes in staining patterns
			planenums[i-1] = i;
			
//			if (i==200) {
//				exit();
//			}
			//cleanup
			run("Select None");
			close("mask");
			close("slice");
			updateDisplay();
			
	}

	Table.create("Measurements");
	
	Table.setColumn("Plane", planenums);
	Table.setColumn("Median", means);
	Table.setColumn("Area", areas);
	//Table.update(); // for testing only

} // EO getMeasurements