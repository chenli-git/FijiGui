package org.stjude;
import ij.*;
import net.imagej.ImageJ;
import ij.ImagePlus;
import ij.IJ;
import fiji.util.gui.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
       
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        String imagePath = "/Users/cli74/Downloads/FluorescentCells.tif"; 
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