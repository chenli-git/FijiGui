package org.stjude.swingui.boot.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.stjude.swingui.boot.proc.*;
import org.stjude.swingui.boot.panel.ProcessPanel;

import ij.ImagePlus;
import ij.WindowManager;
import ij.*;

import org.checkerframework.checker.units.qual.s;
import org.stjude.swingui.boot.event.ClickRecorder;

public class ButtonListener implements ActionListener
{
	private static ModifySliders ms = null; // Use shared instance
	private static ClickRecorder recorder = ClickRecorder.getInstance();
	private static ProcessPanel processPanel;

	// Static method to set ProcessPanel externally (Inject the instance)
	public static void setProcessPanel(ProcessPanel panel) {
		processPanel = panel;
	}

    public ButtonListener() {}

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        // Gets button ID
		JButton button = (JButton) actionEvent.getSource();
		String id = (String) button.getClientProperty("ID"); 
		button.setBackground(new java.awt.Color(255, 255, 255));
		// Declares proc classes called
		ColorDisplay cd;
		ChannelViews cv;
		StackViews sv;
		ModifyButtons mb;
		RatioViews rv;
		// **Record the button click**
        if (isProcessingAction(id)) {
			System.out.println("Button clicked: " + id);
			recorder.actionPerformed(actionEvent);
		}

		ImagePlus imp = WindowManager.getCurrentImage(); // active image
		if (imp == null) {
			IJ.error("Error", "No active image to process.");
			return;
		}


		// Calls a response based on button ID
		switch (id) {
			
			// logic for Color Display buttons
			case "red": //System.out.println(id); //for TESTING
				cd = new ColorDisplay();
				cd.setLUT("Red");
				break;
				
			case "green":
				cd = new ColorDisplay();
				cd.setLUT("Green");
				break;
				
			case "blue":
				cd = new ColorDisplay();
				cd.setLUT("Blue");
				break;
				
			case "cyan":
				cd = new ColorDisplay();
				cd.setLUT("Cyan");
				break;
				
			case "yellow":
				cd = new ColorDisplay();
				cd.setLUT("Yellow");
				break;
				
			case "magenta":
				cd = new ColorDisplay();
				cd.setLUT("Magenta");
				break;
				
			case "white":
				cd = new ColorDisplay();
				cd.setLUT("Grays");
				break;
				
			case "black":
				cd = new ColorDisplay();
				cd.setLUT("black"); // custom lut
				break;
				
			case "inferno":
				cd = new ColorDisplay();
				cd.setLUT("mpl-inferno");
				break;
				
			case "viridis":
				cd = new ColorDisplay();
				cd.setLUT("mpl-viridis");
				break;	
				
			case "ohot":
				cd = new ColorDisplay();
				cd.setLUT("Orange Hot");
				break;
				
			case "chot":
				cd = new ColorDisplay();
				cd.setLUT("Cyan Hot");
				break;
				
			case "phase":
				cd = new ColorDisplay();
				cd.setLUT("phase");
				break;

			case "reset_color":
				mb = new ModifyButtons();
				mb.Revert();
				break;
				
			case "showall":
				cd = new ColorDisplay();
				cd.showAll();
				break;	
				
			case "showch":
				cd = new ColorDisplay();
				cd.showCh();
				break;
			
			case "ColorFusion":
				cd = new ColorDisplay();
				cd.colorFusion();
				break;
			
			case "reorder":
				cv = new ChannelViews();
				cv.reOrder();
				break;
			
			case "resetMax":
				cv = new ChannelViews();
				cv.resetMax();
				break;
			
			case "resetMin":
				cv = new ChannelViews();
				cv.resetMin();
				break;

			// Channel Contrast Display button logic is handled directly within VisualizePanel
				
			// logic for Channel Views buttons	
			case "panelize":
				cv = new ChannelViews();
				cv.showPanels();
				break;
			
			case "pup":
				rv = new RatioViews();
				rv.run(RatioViews.PUP);
				break;

			case "coloc":
				rv = new RatioViews();
				rv.run(RatioViews.COL);
				break;					

				
			// logic for Scale Bar buttons	
			case "um":
				cv = new ChannelViews();
				cv.scaleBar();
				break;
			
			// logic for Ratio Views buttons
			case "raw":
				rv = new RatioViews();
				rv.run(RatioViews.RAW);
				break;
			
			case "imd":
				rv = new RatioViews();
				rv.run(RatioViews.IMD);
				break;
			
			case "snrmd":
				rv = new RatioViews();
				rv.run(RatioViews.SNR);
				break;				
				
			// logic for Snap View buttons	
			case "snap":
				cv = new ChannelViews();
				cv.snapShot();
				break;
			
			case "copy":
				cv = new ChannelViews();
				cv.copyToSys();
				break;
			
			case "dup":
				cv = new ChannelViews();
				cv.duplicate();
				break;
			
			case "sync":
				cv = new ChannelViews();
				cv.sync();
				break;
				
			// logic for Stack Views buttons	
			case "mip":
				sv = new StackViews();
				sv.mip();
				break;
				
			case "sip":
				sv = new StackViews();
				sv.sip();
				break;

			case "ortho":
				sv = new StackViews();
				sv.ortho();
				break;	
				
			case "3D":
				sv = new StackViews();
				sv.threeD();
				break;
				
			case "kymo":
				sv = new StackViews();
				sv.kymo();
				break;	
				
			case "montage":
				sv = new StackViews();
				sv.montage();
				break;	
				
			// logic for Intensity Correction buttons	

			// case "revert":
			// 	mb = new ModifyButtons();
			// 	mb.Revert();
			// 	break;

			case "undo":
				//System.out.println("Undo button clicked.");
				ms = ModifySliders.getInstance();
                ms.undoLastAction();
                break;

			case "hmatch": 
				mb = new ModifyButtons();
				mb.histoMatch();
				break;
				
			case "exponential":
				mb = new ModifyButtons();
				mb.expFit();
				break;		
				
			// logic for Intensity Correction buttons	
			case "rotate":
				
				mb = new ModifyButtons();
				mb.rotate();
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[0], -1);
				}
				break;
				
			case "crop":
				mb = new ModifyButtons();
				boolean cropSuccess = mb.crop();
				if (cropSuccess && processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[0], -1);
				}
				break;		
				
			case "subset":
				mb = new ModifyButtons();
				mb.subset();
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[0], -1);
				}
				break;
			// buttons for imageinfo 
			case "info":
				mb = new ModifyButtons();
				mb.info();
				break;

			case "meta":
				mb = new ModifyButtons();
				mb.meta();
				break;

			// logic for Load buttons	
			case "appsteps": System.out.println(id);
				break;
				
			case "batchsteps": System.out.println(id);
				break;	

			
			case "save":
				recorder.saveRecentActions();
				break;	
			
			case "saveMovie":
				cv = new ChannelViews();
				cv.saveMovie();
				break; // TODO: Implement saveMovie logic
			
			case "clear":
				recorder.clearRecords();
				break;

			case "export":
				// Export table as CSV instead of text
				recorder.exportTableToCsv();
				break;

			case "run":
				recorder.runRecordedActions();
				break;
				
		}		
    }

	private boolean isProcessingAction(String action) {
		return action.equals("hmatch") || action.equals("exponential") || action.equals("rotate") || action.equals("crop") || action.equals("subset");
	}

}
