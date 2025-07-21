// ----- Creates a brightness modulated color display in HSB space ------
// Works with multi-channel, multi-slice, multi-frame images
// Assumes co-registered Fluo and Grayscale images are in the same image window
// User must first adjust the display range for each channel
// User selects the grayscale channel (DIC, Phase, EM, etc)

setBatchMode(true);

title = getTitle();
getDimensions(width, height, channels, slices, frames);
Stack.setDisplayMode("composite"); // has to start in composite display mode

// List channels as string
chnums = Array.getSequence(channels+1);
chnums = Array.deleteIndex(chnums, 0); // whole number array
chstrings = newArray();  // list of channels as strings
for (i = 0; i < chnums.length; i++) {
	chnum = chnums[i];
	chstring = toString(chnum, 0);
	chstrings[i] = chstring;
}

Dialog.create("Select Grayscale Channel");
Dialog.addChoice("Gray Channel:", chstrings);
Dialog.show();
chstring = Dialog.getChoice();  // gray channel

// --- Split into grayscale and color images ---
// Doing it this way preserves existing LUT colors and is also compatible with stacks
// Create 'arrange channels' string, (super kludgy)
acstring = ""; //ini
for (j = 0; j < chstrings.length; j++) {
	if (parseInt(chstrings[j]) != parseInt(chstring)) { // concat each color channel ID
		acstring = acstring+chstrings[j];
	}
}
// append gray channel ID to end
acstring = acstring+chstring; // string of channel order for Arrange Channels command

// Put grayscale image last ('Duplicate' only works with contiguous ranges)
run("Arrange Channels...", "new="+acstring); 

// Get color channels - modified to handle slices and frames
selectWindow(title); // must be in composite display mode
if (slices > 1 && frames > 1) {
    // Multi-slice, multi-frame
    run("Duplicate...", "title=ColorChs duplicate channels=1-"+(channels-1)+" slices=1-"+slices+" frames=1-"+frames);
} else if (slices > 1) {
    // Multi-slice, single frame
    run("Duplicate...", "title=ColorChs duplicate channels=1-"+(channels-1)+" slices=1-"+slices);
} else if (frames > 1) {
    // Single slice, multi-frame
    run("Duplicate...", "title=ColorChs duplicate channels=1-"+(channels-1)+" frames=1-"+frames);
} else {
    // Single slice, single frame (original behavior)
    run("Duplicate...", "title=ColorChs duplicate channels=1-"+(channels-1));
}

// Convert color channels to RGB
selectWindow("ColorChs");
if (channels > 2) { // Only convert to RGB if we have multiple color channels
    run("RGB Color");
} else {
    // If only one color channel, convert to RGB anyway for consistency
    run("RGB Color");
}
rename("Color"); // as RGB
close("ColorChs");

// Get grayscale channel - modified to handle slices and frames
selectWindow(title); // must be in composite display mode
if (slices > 1 && frames > 1) {
    // Multi-slice, multi-frame
    run("Duplicate...", "title=GrayCh duplicate channels="+channels+" slices=1-"+slices+" frames=1-"+frames);
} else if (slices > 1) {
    // Multi-slice, single frame
    run("Duplicate...", "title=GrayCh duplicate channels="+channels+" slices=1-"+slices);
} else if (frames > 1) {
    // Single slice, multi-frame
    run("Duplicate...", "title=GrayCh duplicate channels="+channels+" frames=1-"+frames);
} else {
    // Single slice, single frame (original behavior)
    run("Duplicate...", "title=GrayCh duplicate channels="+channels);
}

run("RGB Color");
rename("Gray"); // as RGB

// ------- Color fusion -------------
// Process each slice and frame individually to preserve dimensions
totalSlices = slices;
totalFrames = frames;

// Create result image with same dimensions as original
selectWindow("Gray");
run("Duplicate...", "title=Result duplicate");

// Process each frame and slice combination
for (t = 1; t <= totalFrames; t++) {
    for (z = 1; z <= totalSlices; z++) {
        // Get current slice from Gray image
        selectWindow("Gray");
        if (totalSlices > 1 && totalFrames > 1) {
            Stack.setPosition(1, z, t);
        } else if (totalSlices > 1) {
            Stack.setSlice(z);
        } else if (totalFrames > 1) {
            Stack.setFrame(t);
        }
        run("Duplicate...", "title=GraySlice");
        run("HSB Stack");
        
        // Get current slice from Color image
        selectWindow("Color");
        if (totalSlices > 1 && totalFrames > 1) {
            Stack.setPosition(1, z, t);
        } else if (totalSlices > 1) {
            Stack.setSlice(z);
        } else if (totalFrames > 1) {
            Stack.setFrame(t);
        }
        run("Duplicate...", "title=ColorSlice");
        run("HSB Stack");
        
        // Transfer H and B channels from Color to Gray
        // Copy Hue (H) channel
        selectWindow("ColorSlice");
        Stack.setChannel(1); // H channel
        run("Copy");
        selectWindow("GraySlice");
        Stack.setChannel(1); // H channel
        run("Paste");
        
        // Copy Brightness (B) channel to Saturation (S) channel
        selectWindow("ColorSlice");
        Stack.setChannel(3); // B channel
        run("Copy");
        selectWindow("GraySlice");
        Stack.setChannel(2); // S channel
        run("Paste");
        
        // Convert back to RGB
        selectWindow("GraySlice");
        run("RGB Color");
        
        // Copy result back to main result stack
        run("Copy");
        selectWindow("Result");
        if (totalSlices > 1 && totalFrames > 1) {
            Stack.setPosition(1, z, t);
        } else if (totalSlices > 1) {
            Stack.setSlice(z);
        } else if (totalFrames > 1) {
            Stack.setFrame(t);
        }
        run("Paste");
        
        // Clean up temporary images
        close("GraySlice");
        close("ColorSlice");
    }
}

// Final cleanup and rename
selectWindow("Result");
rename("Color_Gray_Fusion");
close("Color");
close("Gray");

setBatchMode("exit and display");
//print("Color-brightness fusion completed for " + slices + " slices and " + frames + " frames.");