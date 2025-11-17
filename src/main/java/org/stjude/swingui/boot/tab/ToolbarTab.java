package org.stjude.swingui.boot.tab;

import org.stjude.swingui.boot.panel.ProcessPanel;
import org.stjude.swingui.boot.panel.RecordPanel;
import org.stjude.swingui.boot.panel.VisualizePanel;
import org.stjude.swingui.boot.event.TfButtonListener;
import org.stjude.swingui.boot.panel.InfoPanel;
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
	ProcessPanel processPanel; // Store reference to ProcessPanel

    public ToolbarTab() {
        frameWidth = 370;
        frameHeight = 450;
		setup();
    }

    private void setup(){

		try {
        // Classic, consistent across all Java versions (recommended for ImageJ plugins)
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        // Optional alternative: Modern look (safe from Java 6–21)
        // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			IJ.log("Failed to set Look & Feel: " + e.getMessage());
		}


        // Sets up the frame
		f = new JFrame("Easy Fiji");
		f.setSize(frameWidth, frameHeight);
        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // soft close opertaions handled by windowClosing() below...
		f.addWindowListener(this); // 'this' should recieve WindowEvents from the JFrame

		// --- "Keep Top" toggle button ---
		JToggleButton keepOnTopButton = new JToggleButton("Top");
		keepOnTopButton.setFocusable(false);
		keepOnTopButton.setSelected(true);
		keepOnTopButton.setFont(new Font("Calibri", Font.PLAIN, 12));  // smaller text
		keepOnTopButton.updateUI(); // reset
		keepOnTopButton.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI()); // remove native Aqua constraints
		keepOnTopButton.setMargin(new Insets(1, 4, 1, 4));             // tight padding
		keepOnTopButton.addItemListener(e -> {
			boolean selected = keepOnTopButton.isSelected();
			f.setAlwaysOnTop(selected);
			keepOnTopButton.setText(selected ? "Top" : "Float");
		});
        
		// Provides a hondle for the VisualizePanel, which is used in response to windowActivated events below
		vizpanel = new VisualizePanel();
		processPanel = new ProcessPanel(); // Store reference

        // Sets up the tabbed panes
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0,0,frameWidth+30,frameHeight+30);
        tabbedPane.add("Display", vizpanel); // Contrast class is instantiated w/in VisualizePanel
        tabbedPane.add("Process", processPanel);
        tabbedPane.add("Save", new RecordPanel());
		tabbedPane.add("Image info", new InfoPanel());
		JPanel emptyPanel = new JPanel();
		tabbedPane.add("", emptyPanel);
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, keepOnTopButton);
		// Adds the tabbed pane to the frame
        f.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
		// Pass ProcessPanel to TfButtonListener
        TfButtonListener.setProcessPanel(processPanel); // Inject existing ProcessPanel

		// Finalizes layout
        //f.add(tabbedPane);
		f.setLayout(null);

		// Notifies IJ about this frame and makes its title appear in the IJ Window menu
		WindowManager.addWindow(f);
		// Centers on IJ screen
		GUI.centerOnImageJScreen(f);
		GUI.scale(f);
		// Displays the frame on the screen
		// // --- "Keep Top" toggle button ---
		// JToggleButton keepOnTopButton = new JToggleButton("Keep Top");
		// keepOnTopButton.setBounds(280, 5, 90, 22); // Adjust for your layout
		// keepOnTopButton.setSelected(true); // default ON
		// keepOnTopButton.setToolTipText("Toggle whether this window stays on top of other ImageJ windows");
		// keepOnTopButton.setVisible(true);
		// // Listen for toggle state changes
		// keepOnTopButton.addItemListener(e -> {
		// 	boolean selected = keepOnTopButton.isSelected();
		// 	f.setAlwaysOnTop(selected);
		// 	keepOnTopButton.setText(selected ? "Keep Top" : "Float"); // optional: change label dynamically
		// });
		// // Add to frame
		// f.add(keepOnTopButton);

		f.setAlwaysOnTop(true);
        f.setVisible(true);
		// tooltips waittime longer
		ToolTipManager.sharedInstance().setDismissDelay(12000);
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
