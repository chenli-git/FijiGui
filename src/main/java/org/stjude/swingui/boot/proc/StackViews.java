package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Arrays;

public class StackViews implements ActionListener  {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class StackViews_ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	ImagePlus imp; // class wide scope
	JDialog jd;
	private java.util.List<ImagePlus> hiddenImages;

	public StackViews() {
		imp = WindowManager.getCurrentImage(); // active image
	}
	
	public void mip() { 
		// IJ.doCommand("Z Project...");  this does not work since MIP method is not guaranteed to be the default
		ZProjector zp = new ZProjector();
		ImagePlus zpimp = zp.run(imp, "max all"); // TEST - Not clear that zpimp will have the same LUTs etc as imp... May have to transfer those over...
		zpimp.setTitle("Max Projection");
		zpimp.show();		
	}

	public void sip() {
		ZProjector zp = new ZProjector();
		ImagePlus zpimp = zp.run(imp, "sum all"); // TEST - Not clear that zpimp will have the same LUTs etc as imp... May have to transfer those over...
		zpimp.setTitle("Sum slices");
		zpimp.show();
	}
	
	public void ortho() { 
		IJ.doCommand("Orthogonal Views");
	}

	public void threeD() { 
		IJ.doCommand("3D Viewer");
	}
	
	public void kymo() { 
		new Thread (() -> {
		imp = WindowManager.getCurrentImage(); 
		if (imp == null) {
			return;
		}
		String imageTitle = imp.getTitle();
		Roi currentRoi = imp.getRoi();
		if (currentRoi == null || !(currentRoi instanceof Line)) {
			int width = imp.getWidth();
			int height = imp.getHeight();

			// **Set default ROI as a horizontal line at mid-height**
			int x1 = width / 4;
			int y1 = height / 2;
			int x2 = (3 * width) / 4;
			int y2 = height / 2;

			Line defaultLine = new Line(x1, y1, x2, y2);
			imp.setRoi(defaultLine);

			System.out.println("No line ROI detected. Setting default ROI: " +
							"Start (" + x1 + "," + y1 + ") -> End (" + x2 + "," + y2 + ")");
		} else {
			System.out.println("Using existing ROI: " + currentRoi.getBounds());
		}
		System.out.println(imageTitle);
		IJ.run("KymographBuilder", "input=" + imageTitle);
	}).start();
		
	}
	
	public void montage() { 

		ImagePlus activeImage = IJ.getImage();
		if (activeImage == null) {
			IJ.showMessage("No image is currently active");
			return;
		}
		// Store all other images and temporarily close them
		int[] existingIDs = WindowManager.getIDList();
		hiddenImages = new java.util.ArrayList<>();
		
		if (existingIDs != null) {
			for (int id : existingIDs) {
				ImagePlus img = WindowManager.getImage(id);
				if (img != null && img != activeImage) {
					// Store the image reference
					hiddenImages.add(img);
					// Hide the window but keep image in memory
					img.hide();
					System.out.println("Hiding image: " + img.getTitle());
				}
			}
		}

		IJ.run("Duplicate...","duplicate");
		IJ.run("Split Channels");
		IJ.run("Tile");

		IJ.run("Synchronize Windows", "");
		
		showSplitChannelCleanupPrompt();
		restoreHiddenImages();
		// new javax.swing.Timer(400, new ActionListener() {
		// 	public void actionPerformed(ActionEvent evt) {
		// 		((javax.swing.Timer) evt.getSource()).stop();
		// 		showSplitChannelCleanupPrompt();
		// 		restoreHiddenImages();
		// 	}
		// }).start();
		
		// //IJ.run("Make Montage...");

		

	}
	
	
	// Responses to JDialog sub-gui buttons...
	public void actionPerformed(ActionEvent e) {  
		// FIX - Have to getSource to decide if multiple buttons are present...
        jd.setVisible(false);
		jd.dispose();
		
		// The response
		IJ.run("Reslice [/]...", "output=1.000 start=Top avoid");
		
    }
	
	public void restoreHiddenImages() {
		// Restore all hidden images
		if (hiddenImages != null) {
        for (ImagePlus img : hiddenImages) {
            img.show();
			ImageWindow win = img.getWindow();
			win.toBack();
        }
        hiddenImages.clear();
    	}
	}


	public void showSplitChannelCleanupPrompt() {
		JDialog dialog = new JDialog((Frame) null, "Split Channel Cleanup", false); // non-modal
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLayout(new BorderLayout());

		JLabel label = new JLabel("Close all split-channel images?");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		dialog.add(label, BorderLayout.CENTER);
		dialog.setSize(300, 100);
		dialog.setAlwaysOnTop(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton yesBtn = new JButton("Yes");
		JButton noBtn = new JButton("No");
		buttonPanel.add(yesBtn);
		buttonPanel.add(noBtn);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		yesBtn.addActionListener(e -> {
			closeSplitChannelImages();
			dialog.dispose();
			
		});

		noBtn.addActionListener(e -> dialog.dispose());

		
	}

	private void closeSplitChannelImages() {
		int[] ids = WindowManager.getIDList();
		if (ids != null) {
			for (int id : ids) {
				ImagePlus img = WindowManager.getImage(id);
				if (hiddenImages != null && hiddenImages.contains(img)) continue;
				if (img != null && img.getTitle().matches("C\\d-.*")) { // Adjust if needed
					img.close();
				}
			}
		}
		
	}
}