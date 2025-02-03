package org.stjude.swingui.boot.tab;

import org.stjude.swingui.boot.panel.ProcessPanel;
import org.stjude.swingui.boot.panel.RecordPanel;
import org.stjude.swingui.boot.panel.VisualizePanel;
import org.stjude.swingui.boot.proc.Contrast;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import ij.*;
import ij.gui.*;

public class ToolbarTab extends WindowAdapter {
    private final int frameWidth;
    private final int frameHeight;

	JFrame f; 
	VisualizePanel vizpanel;

    public ToolbarTab() {
        frameWidth = 330;
        frameHeight = 435;
		setup();
    }

    private void setup(){
        // Sets up the frame
		f = new JFrame("FIJI GUI");
		f.setSize(frameWidth, frameHeight);
        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // soft close opertaions handled by windowClosing() below...
		f.addWindowListener(this); // 'this' should recieve WindowEvents from the JFrame
        
		// Provides a hondle for the VisualizePanel, which is used in response to windowActivated events below
		vizpanel = new VisualizePanel();

        // Sets up the tabbed panes
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0,0,frameWidth,frameHeight);
        tabbedPane.add("Display", vizpanel); // Contrast class is instantiated w/in VisualizePanel
        tabbedPane.add("Process", new ProcessPanel());
        tabbedPane.add("Save", new RecordPanel());
		
		// Adds the tabbed pane to the frame
        f.add(tabbedPane, BorderLayout.NORTH);

		// Finalizes layout
        //f.add(tabbedPane);
		f.setLayout(null);

		// Notifies IJ about this frame and makes its title appear in the IJ Window menu
		WindowManager.addWindow(f);
		// Centers on IJ screen
		GUI.centerOnImageJScreen(f);
		GUI.scale(f);
		// Displays the frame on the screen
        f.setVisible(true);
		
    }

    // WindowActivated event is generated anytime user clicks ANYWHERE on the GUI window
	// For full FIJI GUI, many Window clicks will be irrelevant since many of the buttons have nothing to do with contrast.  
	// FIX? - Could the listening be made only on a panel or something so this is only called if users click a RELEVANT area?
    
    public synchronized void windowActivated(WindowEvent e) {
        //super.windowActivated(e);
        Window owin = e.getOppositeWindow();
		
        if (owin==null || !(owin instanceof ImageWindow))
            return;
		// Gets the contrast instance that is part of the VisualizePanel and acts on it
			// Necessary for when multiple image windows are open and user clicks back/forth btwn different images and the FIJI GUI window
		Contrast contrast = vizpanel.getContrast();
		contrast.resetPreviousImageID(); //sets previousImageID = 0
        contrast.setup(); // leads to a reset of sliderRange to match properties (e.g. bit depth) of current imp
        
		WindowManager.setWindow(f); // Brings GUI window to front

    }

    // Handles closing of the frame
	public void windowClosing(WindowEvent we) { // recieves ANY window closing event
		if (we.getSource()==f) {  // checks the JFrame above generated the window closing event 
			f.dispose(); // following PlugInDialog.close(). Inhereted from Window.  Releases all resources related to graphical display of the window.

			WindowManager.removeWindow(f);
		}
	}

}
