package org.stjude.swingui.boot.panel;
import javax.swing.*;
import java.awt.*;
import ij.*;
import ij.plugin.PlugIn;
import ij.gui.GenericDialog;
import ij.io.FileInfo;
import ij.measure.Calibration;

import java.util.regex.*;

public class InfoPanel extends BasePanel {

    //ImagePlus imp;
    private JTextArea infoTextArea;
    private JTextArea channelInfoTextArea;
    private JTextArea acquistionInfoTextArea;

    public InfoPanel() {
        //imp = WindowManager.getCurrentImage(); 
        init();
    }

    private void init() {

        //ImagePlus imp = IJ.getImage();
        //String metadata = imp.getInfoProperty();
        this.setBackground(Color.lightGray);
        // JLabel filesLabel = addFirstLabel(this, new Rectangle(10, 5, 120, 20), "Image Info:", 14);
        // this.add(filesLabel);
        // this.infoTextArea = addTextArea(this, "", "", new Rectangle(10, 30, 300, 100), 12);
        // infoTextArea.setLineWrap(true);

        // JScrollPane scrollPane = new JScrollPane(infoTextArea);
        // scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // scrollPane.setBounds(10, 30, 300, 100);
        // this.add(scrollPane);
        

        JLabel channelLabel = addFirstLabel(this, new Rectangle(10, 5, 200, 20), "Channel Info：", 14);
        this.add(channelLabel);
        this.channelInfoTextArea = addTextArea(this, "", "", new Rectangle(10, 30, 290, 110), 12);
        channelInfoTextArea.setLineWrap(true);

        JScrollPane channelscrollPane = new JScrollPane(channelInfoTextArea);
        channelscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        channelscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        channelscrollPane.setBounds(10, 30, 290, 110);
        this.add(channelscrollPane);

        JLabel acquistionLabel = addFirstLabel(this, new Rectangle(10, 150, 200, 20), "System configuration：", 14);
        this.add(acquistionLabel);
        this.acquistionInfoTextArea = addTextArea(this, "", "", new Rectangle(10, 175, 290, 70), 12);
        acquistionInfoTextArea.setLineWrap(true);

        JScrollPane acquistionscrollPane = new JScrollPane(acquistionInfoTextArea);
        acquistionscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        acquistionscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        acquistionscrollPane.setBounds(10, 175, 290, 70);
        this.add(acquistionscrollPane);                      


        addButton(this, "Get Info", "Get Image Info", "info", new Rectangle(10, 260, 80, 20), 12);
        //addButton(this, "Get Metadata", "Get Image Metadata", "meta", new Rectangle(100, 260, 110, 20), 12);

    }

    // Method to update metadata in JTextArea
    public void updateMetadata(String metadata) {
        //IJ.showMessage(metadata);
        infoTextArea.setText(metadata);
        
    }

    public void extractChannelInfo(String metadata, String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            channelInfoTextArea.setText("Error: File path is null or empty.");
            return;
        }

        //IJ.showMessage(filepath);
        String fileFormat = getFileFormat(filepath);
        String extractedChannelInfo = "";
        String extractedAcquisitionInfo = "";
        System.out.println("File format: " + fileFormat);
        if (fileFormat.equals("CZI")) {
            extractedChannelInfo = extractCZIChannelInfo(metadata);
            extractedAcquisitionInfo = extractAdditionalCZIInfo(metadata);
        } else if (fileFormat.equals("TIFF")) {
            extractedChannelInfo = "TIFF format not yet supported.";
        } else if (fileFormat.equals("ND2")) {
            extractedChannelInfo = "ND2 format not yet supported.";
        } else if (fileFormat.equals("LIF")) {
            extractedChannelInfo = "LIF format not yet supported.";
        } else {
            extractedChannelInfo = "Unknown file format.";
        }
        channelInfoTextArea.setText(extractedChannelInfo);
        acquistionInfoTextArea.setText(extractedAcquisitionInfo);
    }

    public String extractCZIChannelInfo(String metadata) {
        StringBuilder extractedInfo = new StringBuilder();
        // Regular expressions for extracting channel metadata
        Pattern laserPowerPattern = Pattern.compile("Experiment\\|AcquisitionBlock\\|Laser\\|LaserPower #(\\d+) = ([0-9.e-]+)");
        Pattern wavelengthPattern = Pattern.compile("Experiment\\|AcquisitionBlock\\|MultiTrackSetup\\|TrackSetup\\|Attenuator\\|Wavelength #(\\d+) = ([0-9.e-]+)");
        Pattern detectorStartPattern = Pattern.compile("Experiment\\|AcquisitionBlock\\|MultiTrackSetup\\|TrackSetup\\|Detector\\|DetectorWavelengthRange\\|WavelengthStart #(\\d+) = ([0-9.e-]+)");
        Pattern detectorEndPattern = Pattern.compile("Experiment\\|AcquisitionBlock\\|MultiTrackSetup\\|TrackSetup\\|Detector\\|DetectorWavelengthRange\\|WavelengthEnd #(\\d+) = ([0-9.e-]+)");


        Matcher laserMatcher = laserPowerPattern.matcher(metadata);
        Matcher wavelengthMatcher = wavelengthPattern.matcher(metadata);
        Matcher detectorStartMatcher = detectorStartPattern.matcher(metadata);
        Matcher detectorEndMatcher = detectorEndPattern.matcher(metadata);

        while (laserMatcher.find() && wavelengthMatcher.find() && detectorStartMatcher.find() && detectorEndMatcher.find()) {
            int channelNumber = Integer.parseInt(laserMatcher.group(1));
            double powerPercent = Double.parseDouble(laserMatcher.group(2)) * 100;
            double laserWavelength = Double.parseDouble(wavelengthMatcher.group(2)) * 1e9;
            double detectorStart = Double.parseDouble(detectorStartMatcher.group(2)) * 1e9;
            double detectorEnd = Double.parseDouble(detectorEndMatcher.group(2)) * 1e9;

            extractedInfo.append(String.format(
                "Ch%d: %.1f%% of %.0f nm laser, bandpass\n: %.0f-%.0f nm\n",
                channelNumber, powerPercent, laserWavelength, detectorStart, detectorEnd
            ));
        }
        //IJ.showMessage(extractedInfo.toString());
        return extractedInfo.length() > 0 ? extractedInfo.toString() : "No channel metadata found.";

    }

    public String extractAdditionalCZIInfo(String metadata) {
        StringBuilder extractedInfo = new StringBuilder();
        System.out.println("===== Full Metadata Output =====");
        System.out.println(metadata);
        System.out.println("================================");

        // **New Regex Patterns**
        Pattern objectivePattern = Pattern.compile("AcquisitionModeSetup\\|Objective\\s*=\\s*(.+)");
        Pattern dwellTimePattern = Pattern.compile("AcquisitionModeSetup\\|PixelPeriod\\s*=\\s*([0-9.eE-]+)");
        Pattern scanModePattern = Pattern.compile("AcquisitionModeSetup\\|TrackMultiplexType\\s*=\\s*(.+)");


        // **Find Matches**
        Matcher objectiveMatcher = objectivePattern.matcher(metadata);
        Matcher dwellTimeMatcher = dwellTimePattern.matcher(metadata);
        Matcher scanModeMatcher = scanModePattern.matcher(metadata);

        // **Extract or Set "NA" if Missing**
        String objective = objectiveMatcher.find() ? objectiveMatcher.group(1).trim() : "NA";
        String dwellTime = dwellTimeMatcher.find() ? String.format("%.2f µs", Double.parseDouble(dwellTimeMatcher.group(1)) * 1e6) : "NA";
        String scanMode = scanModeMatcher.find() ? scanModeMatcher.group(1).trim() : "NA";
        
        String voxelSize = "";
        ImagePlus imp = IJ.getImage();
        if (imp != null) {
            Calibration cal = imp.getCalibration();
            if (cal != null) {
                voxelSize = String.format("%.4fx%.4fx%.4f %s³",cal.pixelWidth, cal.pixelHeight, cal.pixelDepth, cal.getUnit());
            } else {
                System.out.println("\nNo calibration data found.");
            }

        }

        // **Format Output**
        extractedInfo.append("Objective: ").append(objective).append("\n");
        extractedInfo.append("Dwell Time: ").append(dwellTime).append("\n");
        extractedInfo.append("Scan Mode: ").append(scanMode).append("\n");
        extractedInfo.append("Voxel Size: ").append(voxelSize).append("\n");
        System.out.println("Extracted Info: " + extractedInfo.toString());
        return extractedInfo.toString();
    }



    private String getFileFormat(String filePath) {
        if (filePath.toLowerCase().endsWith(".czi")) {
            //IJ.showMessage("CZI format detected.");
            return "CZI";  // Zeiss CZI format
        } 
        // Placeholder for future formats
        else if (filePath.toLowerCase().endsWith(".tiff") || filePath.toLowerCase().endsWith(".tif")) {
            //IJ.showMessage("TIFF format not yet supported.");
            return "TIFF";  // Placeholder for future TIFF support
        } else if (filePath.toLowerCase().endsWith(".nd2")) {
            return "ND2";  // Placeholder for Nikon ND2 support
        } else if (filePath.toLowerCase().endsWith(".lif")) {
            return "LIF";  // Placeholder for Leica LIF support
        } else {
            return "UNKNOWN"; // Unknown format
        }
        
    }


}
