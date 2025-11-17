package org.stjude.swingui.boot.event;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stjude.swingui.boot.proc.ModifySliders;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.image.IndexColorModel;

public class ClickRecorder implements ActionListener {
    private static ClickRecorder instance = new ClickRecorder(); // shared instance
    private static List<JSONObject> recordedClicks = new ArrayList<>();
    private static List<JSONObject> savedActions = new ArrayList<>(); // Store recent actions
    private static RecorderTableModel tableModel; // **Table model for JTable display**
    private static ModifySliders ms = null; 
    
    private ClickRecorder() {}
    // **Get the shared instance of ClickRecorder**
    public static ClickRecorder getInstance() {
        return instance;
    }

    // **Set the table model globally**
    public static void setTableModel(RecorderTableModel model) {
        tableModel = model;
    }

    // **Remove last record from table (for undo functionality)**
    public static void removeLastRecord() {
        if (tableModel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> tableModel.removeLastRecord());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand(); // Get the button name


        JSONObject action = new JSONObject();
        action.put("button", command);
        recordedClicks.add(action); // Store in the list
        // **Update JTextArea Display**
        //displayArea.append("▶ " + command + "\n");

        System.out.println("📌 Recorded: " + action.toString());
    }

    public void recordAction(String command, double[] parameters, int channel) {


        JSONObject action = new JSONObject();
        action.put("button", command);
        action.put("channel", channel);
        JSONArray paramsArray = new JSONArray();
        for (double p : parameters) {
            paramsArray.put(p);
        }
        action.put("params", paramsArray);

        recordedClicks.add(action);
        System.out.println("📌 Recorded: " + action.toString());

        // **Update table model**
        if (tableModel != null) {
            ActionRecord record = new ActionRecord(command, parameters, channel);
            javax.swing.SwingUtilities.invokeLater(() -> tableModel.addRecord(record));
        }

    }

    

    public void saveRecentActions() {
        int size = recordedClicks.size();
        if (size == 0) {
            System.out.println("⚠ No recent action to save.");
            return;
        }
        
        JSONObject lastAction = recordedClicks.get(size - 1); // Get the most recent action

        // **Retrieve Image Contrast Min/Max**
        ImagePlus imp = WindowManager.getCurrentImage();
        double min = 0;
        double max = 0;
        LUT lut = null;  // Default if LUT not found
        String lutName = "NA";  // Default if LUT not found
        String lutDetails = "NA";  // Default if LUT not found
        if (imp != null) {
            ImageProcessor ip = imp.getProcessor();
            if (ip != null) {
                min = ip.getMin();  // ✅ Read Min contrast
                max = ip.getMax();  // ✅ Read Max contrast
                // **Get LUT Name**
                if (imp.getProcessor().getLut() != null) {
                    lut = ip.getLut();
                    lutDetails = imp.getProcessor().getLut().toString();
                }
            }

            
        }

        if (lut != null) {
            IndexColorModel cm = lut.getColorModel();
            int mapSize = cm.getMapSize();

            JSONArray lutArray = new JSONArray();
            for (int i = 0; i < mapSize; i++) {
                JSONObject colorEntry = new JSONObject();
                colorEntry.put("r", cm.getRed(i));
                colorEntry.put("g", cm.getGreen(i));
                colorEntry.put("b", cm.getBlue(i));
                lutArray.put(colorEntry);
            }

            JSONObject lutInfo = new JSONObject();
            lutInfo.put("min", min);
            lutInfo.put("max", max);
            lutInfo.put("description", lut.toString());  // ✅ Save LUT description
            lutInfo.put("colors", lutArray);  // ✅ Store LUT as an array

            lastAction.put("LUT", lutInfo);  // ✅ Save as structured JSON object
        }

        
        

        // **Add Min/Max to Action**
        lastAction.put("Min", min);
        lastAction.put("Max", max);

        savedActions.add(lastAction); // Add to saved actions

        System.out.println("✅ Last action saved: " + lastAction.toString());
    }

    // **Clear recorded actions**
    public void clearRecords() {
        recordedClicks.clear();
        savedActions.clear(); // Clear saved actions
        if (tableModel != null) {
            javax.swing.SwingUtilities.invokeLater(() -> tableModel.clear());
        }
        System.out.println("🗑 Click history cleared.");
    }

    // **Export actions to a JSON file**
    public void exportToJson() {
        if (savedActions.isEmpty()) {
            System.out.println("⚠ No saved actions to export.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = "recorded_actions_" + timestamp + ".json";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Recorded Actions");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".json")) {
                filePath += ".json";
            }

            try (FileWriter writer = new FileWriter(filePath)) {
                JSONArray jsonArray = new JSONArray(savedActions);  // ✅ Export list, not raw clicks
                writer.write(jsonArray.toString(4));
                //displayArea.append("\nActions exported to: " + filePath + "\n");
                System.out.println("Exported to: " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //displayArea.append("\n⚠ Export canceled by user.\n");
            System.out.println("⚠ Export canceled.");
        }
    
    }

    // **NEW: Export table to CSV file**
    public void exportTableToCsv() {
        if (tableModel == null || tableModel.getRowCount() == 0) {
            IJ.showMessage("No recorded actions to export.");
            return;
        }
    
        // Get active image name
        ImagePlus imp = WindowManager.getCurrentImage();
        String imageName = (imp != null) ? imp.getTitle().replaceAll("\\s+", "_") : "NoImage";
    
        // Generate timestamped filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = imageName + "_actions_" + timestamp + ".csv";
    
        // Setup file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Action Table as CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setSelectedFile(new File(defaultFileName));
    
        int userSelection = fileChooser.showSaveDialog(null);
    
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
    
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.write("Channel,Action,Parameters\n");
                
                // Write data rows
                java.util.List<ActionRecord> records = tableModel.getRecords();
                for (ActionRecord record : records) {
                    writer.write(String.format("%s,%s,\"%s\"\n",
                        record.getChannelLabel(),
                        record.getActionId(),
                        record.getParamsAsString()));
                }
                
                System.out.println("✅ Table exported to: " + filePath);
                IJ.showMessage("Success", "Table exported to:\n" + filePath);
            } catch (IOException e) {
                System.err.println("❌ Error saving file.");
                IJ.error("Export Error", "Failed to save file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Export canceled.");
        }
    }

    // **NEW: Export table to CSV file with specified path (used by SaveButtons)**
    public void exportTableToCsvWithPath(String filePath) {
        if (tableModel == null || tableModel.getRowCount() == 0) {
            System.out.println("⚠ No recorded actions to export.");
            return;
        }
    
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write("Channel,Action,Parameters\n");
            
            // Write data rows
            java.util.List<ActionRecord> records = tableModel.getRecords();
            for (ActionRecord record : records) {
                writer.write(String.format("%s,%s,\"%s\"\n",
                    record.getChannelLabel(),
                    record.getActionId(),
                    record.getParamsAsString()));
            }
            
            System.out.println("✅ Table exported to: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error saving file: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void runRecordedActions() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select JSON File to Run Actions");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        
        ms = ModifySliders.getInstance();
        if (ms == null) {
            IJ.showMessage("No active image available for processing.");
            return;
        }

        int userSelection = fileChooser.showOpenDialog(null);
        boolean changedLUT = false; // Flag to check if LUT was changed

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
    
            try {
                // **Read JSON file**
                String content = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath()));
                JSONArray jsonArray = new JSONArray(content);
    
                // **Execute Actions**
                System.out.println("📜 Number of actions: " + jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject action = jsonArray.getJSONObject(i);
                    String button = action.optString("button", "NA");
                    double params = action.optDouble("params", 0);
                    int channel = action.optInt("channel", 1);
                    double min = action.optDouble("Min", 0);
                    double max = action.optDouble("Max", 255);
                    String lut = action.optString("LUT", "NA");
    
                    System.out.println("\n▶ Running action: " + button + " | Params: " + params + " | Channel: " + channel);
    
                    // **Get the active image**
                    ImagePlus imp = WindowManager.getCurrentImage();
                    if (imp == null) {
                        System.out.println("⚠ No active image found. Skipping action: " + button);
                        continue;
                    }
                    
                    // **Set Active Channel and Contrast**
                    imp.setC(channel);
                    imp.getProcessor().setMinAndMax(min, max);

                    System.out.println("✅ Contrast range set: Min = " + min + ", Max = " + max);

                    String id = button.toLowerCase();
                    switch (id) {
                        case "smooth":
                            ms.smooth(params);
                            break;
                        case "denoise":
                            ms.denoise(params);
                            break;
                        case "sharpen":
                            ms.sharpen(params);
                            break;
                        case "subbkgd":
                            ms.subtBkgd(params);
                            break;
                        case "gamma":
                            ms.gamma(params);
                            break;
                        case "multiply":
                            ms.multiply(params);
                            break;
                    }

                    if (action.has("LUT") && changedLUT == false) {
                        //changedLUT = true; // Set flag to true
                        JSONObject lutInfo = action.getJSONObject("LUT"); // ✅ Read as JSON object
                        double minLut = lutInfo.getDouble("min");
                        double maxLut = lutInfo.getDouble("max");
                        JSONArray lutArray = lutInfo.getJSONArray("colors");
                        applyLUT(imp, lutArray, minLut, maxLut, channel);
                    }
    
                    // **Refresh the Image Display**
                    imp.updateAndDraw();
                }
    
                System.out.println("\n✅ All actions completed successfully.");
            } catch (IOException e) {
                System.err.println("❌ Error reading the JSON file.");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("❌ Error processing the actions.");
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Action run canceled by user.");
        }
    }

    private void applyLUT(ImagePlus imp, JSONArray lutArray, double min, double max, int channel) {
        if (lutArray.length() == 0) {
            System.out.println("⚠ No LUT data available.");
            return;
        }

        try {

            if (imp.getDisplayMode() == IJ.GRAYSCALE) {
                imp.setDisplayMode(IJ.COLOR); // ✅ Ensure color display mode
            }
            // **Prepare LUT Arrays**
            int size = lutArray.length();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
    
            for (int i = 0; i < size; i++) {
                JSONObject colorEntry = lutArray.getJSONObject(i);
                reds[i] = (byte) colorEntry.getInt("r");
                greens[i] = (byte) colorEntry.getInt("g");
                blues[i] = (byte) colorEntry.getInt("b");
            }
            
            // **Create LUT with Min/Max Values**
            LUT restoredLUT = new LUT(reds, greens, blues);
            restoredLUT.min = min;
            restoredLUT.max = max;

            String lutKey = "LUT_Channel_" + channel;
            imp.setProperty(lutKey, restoredLUT); // ✅ Save LUT for the channel
    
            // **Retrieve Image Dimensions**
            int nSlices = imp.getNSlices();
            int nFrames = imp.getNFrames();

            // Store current position
            int currentC = imp.getC();
            int currentZ = imp.getZ();
            int currentT = imp.getT();
    
            if (imp.isComposite()) {
                System.out.println("Applying LUT to composite image, channel " + channel);
                // Get the CompositeImage
                ij.CompositeImage ci = (ij.CompositeImage)imp;
                
                // Get the channel processor and set its LUT
                ci.setC(channel);
                ci.setChannelLut(restoredLUT);
                
                // This is critical - update the display
                ci.updateChannelAndDraw();
                
                System.out.println("✅ LUT applied to composite image channel " + channel);
            } else {
                // For non-composite images with multiple channels
                System.out.println("Applying LUT to standard image, channel " + channel);
                
                // Apply to all slices where this channel appears
                for (int t = 1; t <= nFrames; t++) {
                    for (int z = 1; z <= nSlices; z++) {
                        // Set position to target the right channel
                        imp.setPosition(channel, z, t);
                        
                        // Get and modify the processor
                        ImageProcessor ip = imp.getProcessor();
                        if (ip != null) {
                            ip.setMinAndMax(min, max);
                            ip.setLut(restoredLUT);
                        }
                    }
                }
                System.out.println("✅ LUT applied to standard image channel " + channel);
            }
            // Restore original position
            imp.setPosition(currentC, currentZ, currentT);
            // **Update Image Display**
            imp.changes = true;  
            imp.updateAndDraw();
            System.out.println("✅ LUT applied to Channel " + channel + " successfully!");
    
        } catch (Exception e) {
            System.err.println("❌ Error applying LUT.");
            e.printStackTrace();
        }

    }

}
