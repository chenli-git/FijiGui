package org.stjude.swingui.boot.proc;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*; 
import ij.plugin.filter.*; 
import emblcmci.*; // add jar of package to class path on compile
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.stjude.swingui.boot.panel.InfoPanel;

import loci.common.services.ServiceFactory;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import ome.xml.meta.OMEXMLMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.io.InputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class ModifyButtons implements ActionListener {

/*
// Uncomment to use as FIJI plugin for standalone testing...
public class ModifySliders_ implements PlugIn {

	public void run(String arg) {
			// call below methods from here
	}	
*/
	// class wide scope
	ImagePlus imp; // input imp
	ImagePlus chimp; // active channel of input imp
	int curch; // active channel index
	int nZ; // number of slices
	int nT; // number of frames
	JDialog jd;
	private InfoPanel infoPanel; 

	public void Revert() {
		imp = WindowManager.getCurrentImage(); 
		//IJ.showMessage("Testing testing");
		//System.out.println("Testing testingsssss");
		if (imp == null) {
			IJ.showMessage("No image open.");
			return;
		}
	
		IJ.run(imp, "Revert", "");
	}

	public void Undo() {
		imp = WindowManager.getCurrentImage(); 
		if (imp == null) {
			IJ.showMessage("No image open.");
			return;
		}
		
		Undo.undo();
        imp.updateAndDraw();
	}
	
	public ModifyButtons() {
		
		imp = WindowManager.getCurrentImage(); // active image
		if (imp == null) {
            IJ.showMessage("No image open.");
            return;
        }
		
		// refresh the channel buffer
		int originalChannel = imp.getC();
		// Move to another channel (if there is more than one)
		if (imp.getNChannels() > 1) {
			int tempChannel = (originalChannel == 1) ? 2 : 1; // Switch to a different channel
			imp.setC(tempChannel);
			//imp.updateAndDraw();
		}
		// Move back to the original channel
		imp.setC(originalChannel);

		// Pulls current channel of active image
		curch = imp.getC();
		
		//int[] impdimA = imp.getDimensions();
		nZ = imp.getNSlices();
		nT = imp.getNFrames();
		// ImageStack chimgstk = ChannelSplitter.getChannel(imp, curch);
		// chimp = new ImagePlus("active ch stk", chimgstk);
		
	}



	
	public void histoMatch() { 
		// Directly modifies the ip's of single ch, 3D or 4D stacks
		
		
		new Thread(() -> {
			ImagePlus chimpCopy = new Duplicator().run(imp, curch, curch, 1, nZ, 1, nT); 
		chimpCopy.setTitle("active ch stk");
		ImagePlus chimpdup = new Duplicator().run(chimpCopy);
		BleachCorrection_MH BCMH = null;
		BCMH = new BleachCorrection_MH(chimpdup);
		BCMH.doCorrection();
		chimpdup.show();
		}).start();
				
	}

	public void expFit() { 

		new Thread(() -> {
			ImagePlus chimpCopy = new Duplicator().run(imp, curch, curch, 1, nZ, 1, nT); 
			chimpCopy.setTitle("active ch stk");
			ImagePlus chimpdup = new Duplicator().run(chimpCopy);
			chimpdup.setDimensions(1, this.nZ, this.nT);
			chimpdup.setOpenAsHyperStack(true); 
			chimpdup.setCalibration(imp.getCalibration());
			BleachCorrection_ExpoFit bcef;
			bcef = new BleachCorrection_ExpoFit(chimpdup);
			bcef.core();
			chimpdup.show();
		}).start();

	}
	
	public void rotate() { 
		// Save state for undo
		ModifySliders ms = ModifySliders.getInstance();
		if (ms != null) {
			ms.saveImageState();
		}
		
		// Applies to all channels
		// IJ.doCommand("Rotate...") does not work b/c there are two menu commands called 'Rotate...'!
		// FIX: Would be nice to eventually drive this with a FIJI GUI slider, but a good preview is essential here	
		Rotator rot = new Rotator();
		PlugInFilterRunner pfr = new PlugInFilterRunner(rot,"",""); // String command and String arg are not used by rot in this case

	}

	public boolean crop() { 
		// Applies to all channels
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null || imp.getRoi() == null) {
			IJ.error("No ROI", "Please draw a rectangle selection before cropping.");
			return false;
		}
		
		IJ.setTool(Toolbar.RECTANGLE); // Preselects polyline tool	
		
		// Build GUI.  Ideally this would be written as its own class.
		jd = new JDialog(); // many useful methods inhereted from Dialog and Window
		jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		jd.setTitle("Crop Image");
		
		// Layout
		Container contentPane = jd.getContentPane(); // Best practice to 'add' directly to the JDialog's contentPane which is explicitly a Container
		// Panel to hold elements
		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // blank space
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS)); // a vertical layout on a Container
   
   		// Adds text
		JLabel jla = new JLabel ("Click on Image to Draw Crop Rectangle."); // JLabels are single line only
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
		return true;
	}	
	
	public void subset() { 
		// Applies to all channels
		IJ.doCommand("Make Substack...");
		
	}
	

	public void info() { 
		// Applies to all channels
		//imp = WindowManager.getCurrentImage(); 
		ImagePlus imp = IJ.getImage();
		if (imp == null) {
			return;
		}
		String metadata = imp.getInfoProperty();
		//String metadata = imp.getProperty("Info");
		//String metadta = new Info().getImageInfo(imp, imp.getProcessor());
        if (metadata == null) {
            IJ.showMessage("Error", "No metadata found for this image.");
            return;
        }

		//String filePath = imp.getOriginalFileInfo() != null ? imp.getOriginalFileInfo().directory + imp.getOriginalFileInfo().fileName : null;
        String filePath = imp.getTitle();
		if (filePath == null) {
            JOptionPane.showMessageDialog(null, "Cannot determine file type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

		//IJ.showMessage(filePath);
		// Locate InfoPanel within ToolbarTab dynamically
        SwingUtilities.invokeLater(() -> {
            InfoPanel infoPanel = findInfoPanel();
            if (infoPanel != null) {
                //infoPanel.updateMetadata(metadata);
				infoPanel.extractChannelInfo(metadata, filePath);
            } else {
                IJ.showMessage("Metadata", metadata);
            }
        });

	}

	private InfoPanel findInfoPanel() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame) {
                JFrame frame = (JFrame) window;
                for (Component comp : frame.getContentPane().getComponents()) {
                    if (comp instanceof JTabbedPane) {
                        JTabbedPane tabs = (JTabbedPane) comp;
                        for (int i = 0; i < tabs.getTabCount(); i++) {
                            Component tabComponent = tabs.getComponentAt(i);
                            if (tabComponent instanceof InfoPanel) {
                                return (InfoPanel) tabComponent;
                            }
                        }
                    }
                }
            }
        }
        return null; // Return null if InfoPanel is not found
    }

	public void meta() {
		ImagePlus imp = IJ.getImage();
		if (imp == null) {
			return;
		}
		//String filePath = imp.getTitle();
		String filePath = imp.getOriginalFileInfo().directory + imp.getOriginalFileInfo().fileName;
		System.out.println("File path: " + filePath);
		Map<String, Object> result = new LinkedHashMap<>();

		try {
			ImageReader reader1 = new ImageReader();
        	reader1.setId(filePath);
			Map<String, Object> meta = reader1.getGlobalMetadata();
			//System.out.println("Metadata: " + meta);
			// for (Map.Entry<String, Object> entry : meta.entrySet()) {
			// 	System.out.println(entry.getKey() + " = " + entry.getValue());
			// }
			reader1.close(); // Close the reader after use

			// Set up OME-XML metadata service
			ServiceFactory factory = new ServiceFactory();
        	OMEXMLService service = factory.getInstance(OMEXMLService.class);
        	IMetadata omeMeta = service.createOMEXMLMetadata();
			// Set up reader
			ImageReader reader = new ImageReader();
			reader.setMetadataStore(omeMeta);
			reader.setId(filePath);
			System.out.println(getImageSize(omeMeta));
			System.out.println(getVoxelSize(omeMeta));
			System.out.println(getScanMode(meta));
			System.out.println(getDwellTime(meta));
			System.out.println(getObjective(meta));
			// --- Channel info ---
			reader.close(); // Close the reader after use

		} catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
		

	}
	
	private String getImageSize(IMetadata omeMeta) {
		if (omeMeta == null) {
			return "No metadata available.";
		}
		// --- Basic image info ---
		int sizeX = omeMeta.getPixelsSizeX(0).getValue();
		int sizeY = omeMeta.getPixelsSizeY(0).getValue();
		int sizeZ = omeMeta.getPixelsSizeZ(0).getValue();
		int sizeC = omeMeta.getPixelsSizeC(0).getValue();
		int sizeT = omeMeta.getPixelsSizeT(0).getValue();
		
		//System.out.printf("Dimensions: %d x %d x %d (XYZ), C=%d, T=%d\n", sizeX, sizeY, sizeZ, sizeC, sizeT);
		String dim = String.format("Dimensions: %d x %d x %d (XYZ), C=%d, T=%d", sizeX, sizeY, sizeZ, sizeC, sizeT);
		return dim;
	}
	
	private String getVoxelSize(IMetadata omeMeta) {
		if (omeMeta == null) {
			return "No metadata available.";
		}
		// // --- Voxel size (convert meters to microns) ---
		double voxelX = omeMeta.getPixelsPhysicalSizeX(0).value().doubleValue();
		//System.out.println("Voxel size X: " + voxelX);
		double voxelY = omeMeta.getPixelsPhysicalSizeY(0).value().doubleValue();
		double voxelZ = omeMeta.getPixelsPhysicalSizeZ(0) != null? omeMeta.getPixelsPhysicalSizeZ(0).value().doubleValue(): 1.0;
		String unitX = omeMeta.getPixelsPhysicalSizeX(0).unit().getSymbol();
		//System.out.println("Voxel size unit: " + unitX);
		//System.out.printf("Voxel size: %.3f x %.3f x %.3f micron³\n", voxelX, voxelY, voxelZ);
        String vox = String.format("Voxel size: %.3f x %.3f x %.3f %s³", voxelX, voxelY, voxelZ, unitX);
		return vox;
	}

	private String getScanMode(Map<String, Object> meta) {
		if (meta == null) {
			return "No metadata available.";
		}
		String[] possibleKeys = {
			"Information|Image|Channel|LaserScanInfo|ScanningMode",      // Zeiss
			"Information|Image|Channel|AcquisitionMode",				 // Zeiss
			"Series Mode #1",                                            // Nikon AX
			"{Channel Series Mode}"                                      // Nikon A1
		};
		// // Optional: search loosely
		// for (String key : meta.keySet()) {
		// 	System.out.println("Key: " + key);
		// }
		Set<String> normalizedPossibleKeys = new HashSet<>();
		for (String key : possibleKeys) {
			normalizedPossibleKeys.add(key.replaceAll("\\s+#\\d+$", "").trim());
		}

		// for (String key : possibleKeys) {
		// 	if (meta.containsKey(key)) {
		// 		System.out.println("Scan Mode: " + meta.get(key).toString());
		// 		return meta.get(key).toString();
		// 	}
		// }
		// Now iterate through metadata
		for (Map.Entry<String, Object> entry : meta.entrySet()) {
			String rawKey = entry.getKey();
			String normalizedKey = rawKey.replaceAll("\\s+#\\d+$", "").trim();
	
			if (normalizedPossibleKeys.contains(normalizedKey)) {
				Object value = entry.getValue();
				if (value != null) {
					System.out.println("Scan Mode: " + value.toString());
					return "Scan Mode: " + value.toString();
				}
			}
		}
		return "Scan Mode: No found";
	}

	private String getDwellTime(Map<String, Object> meta) {
		if (meta == null) {
			return "No metadata available.";
		}
		// Known keys for dwell time (pixel time) from Zeiss and Nikon
		String[] possibleKeys = {
			"Information|Image|Channel|LaserScanInfo|PixelTime",   // Zeiss LSM (CZI)
			"Information|Image|Channel|ExposureTime",
			"Dwell Time",                                          // Nikon AX (ND2)
			"{Scan Speed}"                                         // Nikon A1 (ND2)
		};
		for (Map.Entry<String, Object> entry : meta.entrySet()) {
			String rawKey = entry.getKey().trim();
			String normalizedKey = rawKey.replaceAll("\\s+#\\d+$", "");
	
			for (String matchKey : possibleKeys) {
				if (normalizedKey.equalsIgnoreCase(matchKey)) {
					Object val = entry.getValue();
					if (val != null) {
						try {
							double dwellTimeSec = Double.parseDouble(val.toString());
							double dwellTimeUsec = dwellTimeSec * 1e6;
							return String.format("Dwell Time: %.2f µs", dwellTimeUsec);
						} catch (NumberFormatException e) {
							return "Dwell Time (raw): " + val.toString();
						}
					}
				}
			}
		}
		return "Dwell Time: Not found.";
	}

	private String getObjective(Map<String, Object> meta) {
		if (meta == null) {
			return "No metadata available.";
		}
		// Known keys for dwell time (pixel time) from Zeiss and Nikon
		String[] possibleKeys = {
			"Information|Instrument|Objective|Manufacturer|Model",   // Zeiss LSM (CZI)
			"sObjective",                                          // Nikon AX (ND2)
		};
		// Normalize possible keys
		Set<String> normalizedPossibleKeys = new HashSet<>();
		for (String key : possibleKeys) {
			normalizedPossibleKeys.add(key.replaceAll("\\s+#\\d+$", "").trim());
		}
		// Search metadata
		for (Map.Entry<String, Object> entry : meta.entrySet()) {
			String rawKey = entry.getKey().trim();
			String normalizedKey = rawKey.replaceAll("\\s+#\\d+$", "").trim();
	
			if (normalizedPossibleKeys.contains(normalizedKey)) {
				Object value = entry.getValue();
				if (value != null) {
					return "Objective: " + value.toString();
				}
			}
		}
		return "Objective: Not found.";
	}

	private void replace() { 
		// Replaces input channel with new output...
		// Works at the level of the ImageStack to avoid updating display until all slices have been modified
		int nframes = imp.getNFrames();
		int nslices = imp.getNSlices();
		int[] stkidxs = new int[nframes*nslices];
		int c=0;
		// Gets set of 1D ImageStack indices in input that correspond to all planes of given channel		
		for (int frame=1; frame<=nframes; frame++) { 
			for (int slice=1; slice<=nslices; slice++) {	
				stkidxs[c] = imp.getStackIndex(curch, slice, frame); // returns ImageStack 1D index at this location
				c = c+1;
			}
		}
										
		//  Replaces appropriate input slices with corresponding output slices
		ImageStack chimgstk = chimp.getStack(); // ch-specific output slices
		ImageStack imgstk = imp.getStack();  // all input imp slices
		for (int i = 0; i<stkidxs.length; i++) {
			ImageProcessor chip = chimgstk.getProcessor((i+1));
			imgstk.setProcessor(chip, stkidxs[i]); //stkidx is zero-based
		}	
		
		// Updates display to show final result...
		imp.setStack(imgstk);
		imp.updateAndDraw();  // Redraws image with new output (Not show() since the image is already shown).
		
	}

// Responses to JDialog sub-gui buttons...
	public void actionPerformed(ActionEvent e) {  
		// FIX - Have to getSource to decide if multiple buttons are present...
        jd.setVisible(false);
		jd.dispose();
		
		// The response
		IJ.doCommand("Crop");
		
    } 


	
}