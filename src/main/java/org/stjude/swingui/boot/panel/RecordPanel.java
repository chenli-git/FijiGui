package org.stjude.swingui.boot.panel;

import javax.swing.*;
import java.awt.*;

/**
 * This class hold third tab "record" paint logic.
 * Panel is getting invoked by init() method, which is called from constructor
 */
public class RecordPanel extends BasePanel {

	int sliderwidth = 80;

    //Constructor
    public RecordPanel() {
        init();
    }

    //initial setup for Record panel
    private void init(){
        //set background color
        this.setBackground(Color.lightGray);


		JLabel Label = addFirstLabel(this, new Rectangle(10, 5, 200, 20), "Saving Methods:", 14);
        this.add(Label);
        //--- SAVE Column -----
		// TIF Option
        //JLabel filesLabel = addFirstLabel(this, new Rectangle(55, 30, 120, 20), "as raw data (tif)", 14);
        //this.add(filesLabel);
		addLabel(this, "Save Steps?", "Logs all steps applied to image and export as txt file", new Rectangle(190, 30, 80, 20), 12);
		JCheckBox checkBox = addCheckBox(this, "", 270, 30);
		addCbButton(this, checkBox, "Save as raw data (tif)", "Preserves channels and pixel values as acquired", "stif", new Rectangle(10, 30, 170, 20), 12);
		
		// JPG Option
		//JLabel secondLabel = addFirstLabel(this, new Rectangle(55, 80, 180, 20), "for presenatation (jpeg)", 14);
        //this.add(secondLabel);
		JTextField jpgTextField = addTextField(this, "80", "50-80 recommended", new Rectangle(180, 95, 40, 20), 12);
		jpgTextField.setBackground(Color.LIGHT_GRAY);
		addTfButton(this, jpgTextField, "Save for presentation (jpeg)", "No channels, pixel values of view are approximate", "sjpg", new Rectangle(10, 70, 170, 20), 12);
		addLabel(this, "Quality Level", "JPG compression level", new Rectangle(15, 95, 100, 16), 11);
		addSlider(this, jpgTextField, 100, 80, 1, sliderwidth, 100, 95); // sliders 100 wide

        // PNG Option
		//JLabel thirdLabel = addFirstLabel(this, new Rectangle(55, 130, 120, 20), "for figure (png)", 14);
        //this.add(thirdLabel);
		addButton(this, "Save for figure (png)", "No channels, pixel values of view are exact", "snap", new Rectangle(10, 120, 170, 20), 12);
		
		// Movie Option
		//JLabel FourLabel = addFirstLabel(this, new Rectangle(55, 180, 120, 20), "as movie (avi)", 14);
        //this.add(FourLabel);

		// AVI Option
		addButton(this, "Save as movie (mov)", "Save as .mov movie. Note: NO SPACE in the output file name!, some systems may have trouble opening .mov format files. Please install VLC for playback.", "saveMovie", new Rectangle(10, 160, 170, 20), 12);

		// addLabel(this, "Quality Level:", "AVI compression level", new Rectangle(10, 200, 100, 16), 12);
		// JTextField aviTextField = addTextField(this, "60", "50-80 recommended", new Rectangle(110, 220, 50, 20), 12);
		// aviTextField.setBackground(Color.LIGHT_GRAY);
		// addSlider(this, aviTextField, 100, 60, 1, sliderwidth, 10, 220);

		// addLabel(this, "Frames per second:", "Movie playback frame rate", new Rectangle(160, 200, 120, 16), 12);
		// JTextField fpsTextField = addTextField(this, "4", "4-10 recommended", new Rectangle(260, 220, 50, 20), 12);
		// fpsTextField.setBackground(Color.LIGHT_GRAY);
		// addSlider(this, fpsTextField, 20, 4, 1, sliderwidth, 160, 220); 
		// addTwoTfButton(this, aviTextField, fpsTextField, "Save as movie (avi)", "Media Player compatible", "smovie", new Rectangle(10, 180, 170, 20), 12);

		//addLabel(this, "Save Movie:", "Data must be a stack or sequence", new Rectangle(10, 130, 100, 20), 14);
		
		
		// // ---- LOAD Column ------
        // addLabel(this, "Load:", "", new Rectangle(210, 5, 50, 20), 14);

        // addButton(this, "Apply Steps", "Applies logged steps to active image", "appsteps", new Rectangle(210, 30, 80, 20), 12);

		// addButton(this, "Batch Steps", "Applies logged steps to a folder of images", "batchsteps", new Rectangle(210, 55, 80, 20), 12);

        
    }
}
