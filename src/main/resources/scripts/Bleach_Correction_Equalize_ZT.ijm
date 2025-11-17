// Bleach Correction
// TODO - Make Channel Specific

// For each slice in a stack...
// 1.  Do an automatic (intensity independent) segmentation and measure avg intensity within the mask
// 2.  If the % area of the mask is 'sufficiently large', use the intensity measurement for fitting
// 3.  Fit a 4th order polynomial according to the data points
// 4.  Adjust the data to match the fit at each point 
// May cancel local intensity variations due to genuine staining pattern variations

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
	
// Find max median brightest of qualifying slices
p = 0; //counter
for (i = 0; i < areas.length; i++) {
	// >5% of slice must be signal to be corrected
	if ( (areas[i]/imgarea) > 0.02 ) {
		values[p] = means[i];
		p = p+1;
	} // EO if

	// Find slice with max brighness
	Array.getStatistics(values, min, max);
	maxval = max;
} // EO for

/*// TESTING
Table.create("PV Data");
Table.setColumn("Position", positions);
Table.setColumn("Values", values);
Table.update();
*/

	
// --- Median Intensity Equalization -----
	/// No modeling.  Uses simple ratio method to make all slices have the same median int as the brighest slice
	
scalings = newArray(planes.length); // scaling factor for each plane

// Find local scaling factors computationally	
for (i = 0; i < planes.length; i++) {

	if ( (areas[i]/imgarea) > 0.05 ) {
		cv = means[i];
		scalings[i] = maxval/cv;
	} else {  	// Do not compute a scaling factor if area is insufficient
		scalings[i] = 1;	
	}
} // eo for

// Apply scalings to stack
correctedMeans = newArray(depth);
selectWindow(title);
for (i = 0; i < scalings.length; i++) {
	
	//setSlice(i+1); // slice index
	if (useSlices) {
		Stack.setPosition(activeChannel, planes[i], 1);
	} else {
		Stack.setPosition(activeChannel, 1, planes[i]);
	}
	run("Multiply...", "value="+scalings[i]+" slice");
	updateDisplay();
	//print((i+1)+"  "+scalings[i]); // TESTING
	
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
	
} // end for loop

// Create plot after loop completes
Plot.create("Equalize method Intensity vs Frame", "Frame", "Median Signal ROI Intensity");
Plot.setLineWidth(3);
Plot.setColor("black");
Plot.add("line", planes, means);
Plot.setColor("red");
Plot.add("line", planes, correctedMeans);
Plot.setLegend("pre-correction\tpost-correction...", "bottom-left");
Plot.show();



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
	// HP is fluctuations with ~0.0 mean and central tendency
	// Thresholding at > 0.25 stdev tosses lowest 10% of fluctuations
	th = 0.25*getValue("StdDev");
	makeThreshold(th);  // fixed should be ok for this purpose
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
    		//setSlice(i);
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
			
			//cleanup
			run("Select None");
			close("mask");
			close("slice");
			
	}

	Table.create("Measurements");
	Table.setColumn("Area", areas);
	Table.setColumn("Median", means);
	Table.setColumn("Plane", planenums);
	//Table.update(); // for testing only

} // EO getMeasurements