// ----- Creates an brightness modulated color display in HSB space ------
// Assumes co-registered Fluo and Grayscale images are in the same image winodw

// User must first adjust the display range for each channel

// User selects the grayscale channel (DIC, Phase, EM, etc)
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
acstring = acstring+chstring; // string of channel order for Arragne Channels command

// Put grayscale image last ('Dupliate' only works with contiguous ranges
run("Arrange Channels...", "new="+acstring); 

// Get color channels
selectWindow(title); // must be in composite display mode
run("Duplicate...", "title=ColorChs duplicate channels=1-"+(channels-1));  // pull out all color images
run("RGB Color");
rename("Color"); // as RGB
close("ColorChs");

// Get grayscale channels 
selectWindow(title); // must be in composite display mode
run("Duplicate...", "title=GrayCh duplicate channels="+channels); // gray channel is last
run("RGB Color");
rename("Gray"); // as RGB

// ------- Color fusion -------------
// Convert Gray to HSB stack
selectWindow("Gray"); 
run("HSB Stack");  // Using HSB because it is good enough and avoid the gamut issues of LAB

// Convert Color to HSB stack
selectWindow("Color"); 
run("HSB Stack");

// Pastes the Color B to Gray S and Color H to Gray H
selectWindow("Color");
Stack.setChannel(3);  // B channel
run("Copy");
selectWindow("Gray");
Stack.setChannel(2);  // S channel
run("Paste");

selectWindow("Color");
Stack.setChannel(1);  // H channel
run("Copy");
selectWindow("Gray");
Stack.setChannel(1);  // H channel
run("Paste");

// Covert to RGB
selectWindow("Gray");
run("RGB Color");
rename("Color_Gray_Fusion");

close("Color");

