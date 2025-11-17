package org.stjude.swingui.boot.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.stjude.swingui.boot.panel.ProcessPanel;
import org.stjude.swingui.boot.proc.*;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;


public class TfButtonListener implements ActionListener {
	
	double param1, param2;
	JTextField jTextField1, jTextField2;
	JCheckBox checkBox;
	
	private static ModifySliders ms = null; // Shared instance
	private static ClickRecorder recorder = ClickRecorder.getInstance(); // **Shared recorder instance**
	private static ProcessPanel processPanel;
	
	// For each instance of this class only one contrustor is used
    public TfButtonListener(JTextField jTextField1) {
		// Initializes vars and objects
		this.jTextField1 = jTextField1; // a specific text field paired with a specific OK button
		this.jTextField2 = null; // not used in context of this constructor
		param1 = 0; // will be assigned a double corresponding to the string in the textfield WHEN the OK button is pressed
		param2 = -1; // not used in context of this constructor
    }

    public TfButtonListener(JTextField jTextField1, JTextField jTextField2) {
		this.jTextField1 = jTextField1; // a specific text field paired with a specific OK button
		this.jTextField2 = jTextField2; // a specific text field paired with a specific OK button
		param1 = 0; // will be assigned a double corresponding to the string in the textfield WHEN the OK button is pressed
		param2 = 0; // will be assigned a double corresponding to the string in the textfield WHEN the OK button is pressed
    }

    public TfButtonListener(JCheckBox checkBox) {
		this.checkBox = checkBox;
		param1 = 0; // will encode checkbox state
		param2 = -1; // not used in context of this constructor
    }

	// Static method to set ProcessPanel externally (Inject the instance)
    public static void setProcessPanel(ProcessPanel panel) {
        processPanel = panel;
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
		// Gets button ID
		JButton button = (JButton) actionEvent.getSource();
		String id = (String) button.getClientProperty("ID"); 
		button.setBackground(new java.awt.Color(255, 255, 255));
		// **Initialize ModifySliders Only When Needed**
        // if (ms == null) {
        //     ms = ModifySliders.getInstance();
        //     if (ms == null) {
        //         IJ.showMessage("No active image available for processing.");
        //         return;
        //     }
        // }
		ms  = ModifySliders.getInstance();
		if (ms == null) {
			IJ.showMessage("No active image available for processing.");
			return;
		}

		//IJ.showMessage("working");
		
		ImagePlus imp = WindowManager.getCurrentImage(); // active image
		if (imp == null) {
			IJ.error("Error", "No active image to process.");
			return;
		}
		
		// Declares proc classes called
		//ModifySliders ms;
		SaveButtons sb;

		

		// Calls a response based on button ID
		switch (id) {
			// logic for Process panel
			case "smooth": 
				param1 = Double.valueOf(jTextField1.getText());  // value of this textfield WHEN the OK button is pressed
				//ms = new ModifySliders();
				ms.smooth(param1);
				//IJ.showMessage("smooth");
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[]{param1}, ms.getActiveChannel());
				}
				//recorder.recordAction(id, param1, ms.getActiveChannel());
				break;
				
			case "denoise":
				param1 = Double.valueOf(jTextField1.getText()); 
				//ms = new ModifySliders();
				ms.denoise(param1);
				//recorder.recordAction(id, param1, ms.getActiveChannel());
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[]{param1}, ms.getActiveChannel());
				}
				break;			
				
			case "sharpen":
				param1 = Double.valueOf(jTextField1.getText()); 
				//ms = new ModifySliders();
				ms.sharpen(param1);
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[]{param1}, ms.getActiveChannel());
				}
				//recorder.recordAction(id,  param1, ms.getActiveChannel());
				break;
				
			case "subbkgd":
				param1 = Double.valueOf(jTextField1.getText()); 
				//ms = new ModifySliders();

				ms.subtBkgd(param1);
				IJ.showProgress(1.0);
				//ms.subtBkgd(param1);
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[]{param1}, ms.getActiveChannel());
				}
				//recorder.recordAction(id, param1,ms.getActiveChannel());
				break;
				
			case "gamma":
				param1 = Double.valueOf(jTextField1.getText()); 
				//ms = new ModifySliders();
				ms.gamma(param1);
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[]{param1}, ms.getActiveChannel());
				}
				//recorder.recordAction(id, param1, ms.getActiveChannel());
				break;					
				
			case "multiply":

				//param1 = Double.valueOf(jTextField1.getText()); 
				param1 = 5;
				//ms = new ModifySliders();
				double[] result = ms.multiply(param1);
				if (result[0] == 9999 && result[1] == 9999) {
					// User canceled — skip any follow-up logic
					System.out.println("working or not");
					break;
				}
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, result, ms.getActiveChannel());
				}
				//recorder.recordAction(id, param1, ms.getActiveChannel());
				break;
			
			case "applyLutsToCh":
				param1 = 5;
				double[] chresult = ms.applyLutsToCh(param1);
				if (chresult[0] == 9999 && chresult[1] == 9999) {
					System.out.println("working or not");
					break;
				}
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, chresult, ms.getActiveChannel());
				}
				break;
			
			case "applyLutsToAll":
				param1 = 5;
				double[] allresult = ms.applyLutsToAll(param1);
				if (allresult[0] == 9999 && allresult[1] == 9999) {
					System.out.println("working or not");
					break;
				}
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, allresult, -1);// -1 indicates all channels
				}
				break;
			
			case "local corr":
				if (isSliceOrFrame() == false) {
					// If the check fails, an error message is already shown in isSliceOrFrame()
					break;
				}

				ms.localcorr(getbleachMode());
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[0], ms.getActiveChannel());
				}
				break;

			case "global corr":
				if (isSliceOrFrame() == false) {
					// If the check fails, an error message is already shown in isSliceOrFrame()
					break;
				}

				String[] options = {"GlobalL", "GlobalP", "Cancel"};
				int choice = JOptionPane.showOptionDialog(
					null,
					"Choose Global Correction Method:",
					"Global Bleach Correction",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]
				);
				System.out.println(choice);
				if (choice == JOptionPane.CLOSED_OPTION || choice == 2) {
					// User canceled the dialog
					System.out.println(choice);
					break;
				}

				ms.globalcorr(String.valueOf(choice));
				if (processPanel != null && processPanel.isRecording()) {
					recorder.recordAction(id, new double[0], ms.getActiveChannel());
				}
				break;
			
			case "equalize corr":
				if (isSliceOrFrame() == false) {
					// If the check fails, an error message is already shown in isSliceOrFrame()
					break;
				}
				try {
					ms.equalizecorr(getbleachMode());
					if (processPanel != null && processPanel.isRecording()) {
						recorder.recordAction(id, new double[0], ms.getActiveChannel());
					}
				} catch (Exception e) {
					IJ.error("Equalize Correction Error", "An error occurred: " + e.getMessage());
					e.printStackTrace();
				}
				break;

			case "undo":
				ms.undoLastAction();
				break;
			
			// logic for Save panel
			case "stif":
				boolean state = checkBox.isSelected();
				param1 = state ? 1 : 0; // ternary if/then.  Converts logical to binary (double) to avoid passing around even more variable types
				sb =  new SaveButtons();
				sb.tiff(param1);
				break;	
			
			case "sjpg":
				param1 = Double.valueOf(jTextField1.getText()); 
				sb =  new SaveButtons();
				sb.jpeg(param1);
				break;					
			
			case "smovie":
				param1 = Double.valueOf(jTextField1.getText()); 
				param2 = Double.valueOf(jTextField2.getText()); 
				sb =  new SaveButtons();
				sb.movie(param1, param2);
				
		}			
    }

	private boolean isProcessingAction(String action) {
		return action.equals("smooth") || action.equals("denoise") || action.equals("sharpen") || action.equals("subbkgd") || action.equals("gamma") || action.equals("multiply");
	}

	private boolean isSliceOrFrame() {
		ImagePlus imp = IJ.getImage();
		if (imp == null) {
			IJ.showMessage("No active image available for local correction.");
			return false;
		}
		int slices = imp.getNSlices();   // Z dimension
		int frames = imp.getNFrames();   // T dimension
		if ((slices > 1 && frames > 1) || (slices == 1 && frames == 1)) {
			IJ.error("Bleach Correction Error", "Bleach correction only supports either Z (slices) or T (frames), not both.");
			return false;
		}
		return true;
	}

	private String getbleachMode() {
		ImagePlus imp = IJ.getImage();
		int slices = imp.getNSlices();   // Z dimension
		int frames = imp.getNFrames();   // T dimension
		String mode = (slices > 1) ? "z" : "t";
		return mode;
	}
}
