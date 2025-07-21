package org.stjude;
import ij.*;
import net.imagej.ImageJ;
import ij.ImagePlus;
import ij.IJ;
import fiji.util.gui.*;
import java.util.Arrays; 

public class Main {
    public static void main(String[] args) throws Exception {
        // System.out.println("Hello world!");
        // String imagePath1 = "/Users/cli74/Downloads/FluorescentCells.tif"; 
        // String imagePath2 = "/Users/cli74/Downloads/organ-of-corti.tif"; 
        // ImagePlus imp = IJ.openImage(imagePath2);
        // int[] dims = imp.getDimensions();
        // System.out.println(Arrays.toString(dims));
        // System.out.println(imp.getStackSize());


        // return;
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        String imagePath = "/Users/cli74/Downloads/DAPI_secondaries_3.czi"; 
        // Open and display the selected image
        ImagePlus image = IJ.openImage(imagePath);
        if (image != null) {
            image.show();
        } else {
            System.out.println("Unable to open the selected image.");
        }
        
        IJ.runPlugIn("org.stjude.swingui.boot.FijiGui2", "");
    }
}