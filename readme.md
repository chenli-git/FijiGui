# EasyFiji:  A Graphical User Interface for User-Friendly Bioimage Processing in Fiji
A graphical user interface for Fiji that provides access to a curated suite of fluorescence image visualization and processing tools commonly needed by life scientists.  

## Installation
Copy the compressed **.jar** (target/EasyFiji_2-0.2.0.jar) file into into **Fiji.app/plugins/** (MAC, similar for Windows/Linux) and restart Fiji.
### System plugin specifications
This plugin has been tested and verified on Mac(Apple silicon), linux and windows to ensure cross-platform compatibility and ease of setup. And also compatiable with ImageJ and Fiji.
### Dependencies
Detailed dependencies can be found in pom.xml

## Development environment
This Fiji GUI plugin is developed using the **[SciJava](https://scijava.org)** framework and follows the ImageJ plugin architecture.  

| Component | Version / Notes |
|------------|-----------------|
| **Java JDK** | JavaSE-1.8 (Java 8) |
| **Maven** | 3.9.9 |
| **IDE** | VScode (1.104.2) |
| **Build Tool** | Maven (builds `.jar` under `/target`) |
| **ImageJ** | 1.54p |

## How to build
Developers can easily extend it by adding new panels, button listeners, or processing classes following the same event-driven design pattern. after modify, type "mvn clean install" to generate new .jar file. 
The compiled **.jar** file will appear in target folder.

## Troubleshooting Guide

### EasyFiji can not be found in the **Plugins** menu.
Make sure EasyFiji_2-0.2.0.jar is insdie the plugins folder of Fiji and restart.
### Buttons/Sliders Don't respond
Images must be opened and selected (active). 
### Fiji Freezes or GUI Becomes Unresponsive
Long-running process (e.g., filters, intensity correction, large image files) running on the single thread, any interactiion with the images and fiji commands (change channel, adjusting contrast, ...) could interrupt the process causing Fiji freeze. Restart Fiji can help!
### Image display doesnt update after click **apply LUTs**
Duo to Fiji internal bugs, sometimes the image won't auto updates when clicking **apply luts** under Process Tab. The image pixel value are actually modified (function works!) and switch color channel can see fix the display issue.
### Reporting Issues & Getting Support
If you encounter a bug, installation problem, or unexpected behavior, please create a issue through the project’s GitHub page. (please include operating system, fiji version, steps to reproduce the error, possible example image, error messages, screenshots if available)