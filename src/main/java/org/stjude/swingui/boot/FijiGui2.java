package org.stjude.swingui.boot;
import ij.plugin.*;
import org.stjude.swingui.boot.tab.ToolbarTab;

public class FijiGui2 implements PlugIn{
    public void run(String arg) {
        //System.out.println("Hello world!");
        new ToolbarTab();
    }

}
