package org.stjude.swingui.boot.panel;

import javax.swing.*;
import java.awt.*;

/**
 * This class hold third tab "record" paint logic.
 * Panel is getting invoked by init() method, which is called from constructor
 */
public class RecordPanel extends BasePanel {

	int sliderwidth = 100;

    //Constructor
    public RecordPanel() {
        init();
    }

    //initial setup for Record panel
    private void init(){
        //set background color
        this.setBackground(Color.lightGray);

        //--- SAVE Column -----

        JLabel filesLabel = addFirstLabel(this, new Rectangle(10, 5, 120, 20), "Save Data:", 14);
		// TIF Option
		addLabel(this, "Save Steps?", "Logs all steps applied to image", new Rectangle(10, 30, 80, 20), 12);
		JCheckBox checkBox = addCheckBox(this, "", 80, 30);

		addCbButton(this, checkBox, "SaveAs TIF", "Preserves exact pixel values", "stif", new Rectangle(10, 50, 80, 20), 12);

        // JPG Option
		addLabel(this, "Save View:", "", new Rectangle(10, 80, 100, 20), 14);

		addLabel(this, "Quality Level:", "JPG compression level", new Rectangle(10, 100, 100, 16), 12);
		JTextField jpgTextField = addTextField(this, "80", "50-80 recommended", new Rectangle(110, 116, 30, 20), 12);
		addSlider(this, jpgTextField, 100, 80, 1, sliderwidth, 10, 116); // sliders 100 wide

		addTfButton(this, jpgTextField, "SaveAs JPG", "Estimates pixel values", "sjpg", new Rectangle(10, 140, 80, 20), 12);
		
		// Movie Option
		addLabel(this, "Save Movie:", "Data must be a stack or sequence", new Rectangle(10, 170, 100, 20), 14);
		
		addLabel(this, "Quality Level:", "AVI compression level", new Rectangle(10, 190, 100, 16), 12);
		JTextField aviTextField = addTextField(this, "60", "50-80 recommended", new Rectangle(110, 206, 30, 20), 12);
		addSlider(this, aviTextField, 100, 60, 1, sliderwidth, 10, 206);
		
		addLabel(this, "Frames per second:", "Movie playback frame rate", new Rectangle(10, 230, 120, 16), 12);
		JTextField fpsTextField = addTextField(this, "4", "4-10 recommended", new Rectangle(110, 246, 30, 20), 12);
		addSlider(this, fpsTextField, 20, 4, 1, sliderwidth, 10, 246); 
		
		addTwoTfButton(this, aviTextField, fpsTextField, "SaveAs AVI", "Media Player compatible", "smovie", new Rectangle(10, 270, 80, 20), 12);
		
		// ---- LOAD Column ------
        addLabel(this, "Load:", "", new Rectangle(210, 5, 50, 20), 14);

        addButton(this, "Apply Steps", "Applies logged steps to active image", "appsteps", new Rectangle(210, 30, 80, 20), 12);

		addButton(this, "Batch Steps", "Applies logged steps to a folder of images", "batchsteps", new Rectangle(210, 55, 80, 20), 12);

        // added files label to the panel
        this.add(filesLabel);
    }
}
