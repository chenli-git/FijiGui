package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


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
	
	public StackViews() {
		imp = WindowManager.getCurrentImage(); // active image
	}
	
	public void mip() { 
		// IJ.doCommand("Z Project...");  this does not work since MIP method is not guaranteed to be the default
		ZProjector zp = new ZProjector();
		ImagePlus zpimp = zp.run(imp, "max"); // TEST - Not clear that zpimp will have the same LUTs etc as imp... May have to transfer those over...
		zpimp.setTitle("Max Projection");
		zpimp.show();		
	}
	
	public void ortho() { 
		IJ.doCommand("Orthogonal Views");
	}

	public void threeD() { 
		IJ.doCommand("3D Viewer");
	}
	
	public void kymo() { 
		IJ.setTool(Toolbar.POLYLINE); // Preselects polyline tool	
	
		// Build GUI.  Ideally this would be written as its own class.
		jd = new JDialog(); // many useful methods inhereted from Dialog and Window
		jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		jd.setTitle("Make KymoGraph");
		
		// Layout
		Container contentPane = jd.getContentPane(); // Best practice to 'add' directly to the JDialog's contentPane which is explicitly a Container
		// Panel to hold elements
		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // blank space
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS)); // a vertical layout on a Container
   
   		// Adds text
		JLabel jla = new JLabel ("Click on Stack to Draw Line."); // JLabels are single line only
		jla.setAlignmentX(Component.CENTER_ALIGNMENT); // centers within whatever space is alloted by Layout
		jla.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
		jp.add(jla);  
		
		JLabel jlb = new JLabel ("When Finished, Press OK.");
		jlb.setAlignmentX(Component.CENTER_ALIGNMENT);
		jlb.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 		
		jp.add(jlb);
		
		// Adds OK button
		JButton b = new JButton ("OK");
		b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(this);
		jp.add(b);
		
		jd.add(jp); 
		
		jd.pack();
		GUI.centerOnImageJScreen(jd);
		jd.setVisible(true);    

		// Processing begins in response to button press and resides within ActionListener method below....
	
	}
	
	public void montage() { 
		IJ.run("Make Montage...");
	}
	
	
// Responses to JDialog sub-gui buttons...
	public void actionPerformed(ActionEvent e) {  
		// FIX - Have to getSource to decide if multiple buttons are present...
        jd.setVisible(false);
		jd.dispose();
		
		// The response
		IJ.run("Reslice [/]...", "output=1.000 start=Top avoid");
		
    } 
	
}