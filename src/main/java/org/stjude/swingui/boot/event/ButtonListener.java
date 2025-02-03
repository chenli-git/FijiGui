package org.stjude.swingui.boot.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.stjude.swingui.boot.proc.*;

public class ButtonListener implements ActionListener
{
    public ButtonListener() {
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        // Gets button ID
		JButton button = (JButton) actionEvent.getSource();
		String id = (String) button.getClientProperty("ID"); 

		// Declares proc classes called
		ColorDisplay cd;
		ChannelViews cv;
		StackViews sv;
		ModifyButtons mb;
		RatioViews rv;
		
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
				
			case "showall":
				cd = new ColorDisplay();
				cd.showAll();
				break;	
				
			case "showch":
				cd = new ColorDisplay();
				cd.showCh();
				break;
			
			case "reorder":
				cv = new ChannelViews();
				cv.reOrder();
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
				
			// logic for Stack Views buttons	
			case "mip":
				sv = new StackViews();
				sv.mip();
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
				break;
				
			case "crop":
				mb = new ModifyButtons();
				mb.crop();
				break;		
				
			case "subset":
				mb = new ModifyButtons();
				mb.subset();
				break;
				
			// logic for Load buttons	
			case "appsteps": System.out.println(id);
				break;
				
			case "batchsteps": System.out.println(id);
				break;					
				
		}		
    }
}
