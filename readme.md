# 🔬 EasyFiji: A Graphical User Interface for User-Friendly Bioimage Processing in Fiji 🖼️
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A graphical user interface for Fiji that provides access to a curated suite of fluorescence image visualization and processing tools commonly needed by life scientists.  

## 📥 Installation
Please consult the [wiki page](https://github.com/stjude/EasyFiji/wiki).

### 💻 System specifications
EasyFiji has been tested on Mac, Linux and Windows to ensure cross-platform compatibility. 

### 📦 Dependencies
Software dependencies can be found in pom.xml

## 🛠️ Development environment
This Fiji GUI plugin is developed using the **[SciJava](https://scijava.org)** framework and follows the ImageJ plugin architecture.  

| Component | Version / Notes |
|------------|-----------------|
| **Java JDK** | JavaSE-1.8 (Java 8) |
| **Maven** | 3.9.9 |
| **IDE** | VScode (1.104.2) |
| **Build Tool** | Maven (builds `.jar` under `/target`) |
| **ImageJ** | 1.54p |

## 🔨 How to build
Developers can easily extend it by adding new panels, button listeners, or processing classes following the same event-driven design pattern. after modify, type "mvn clean install" to generate new .jar file. 
The compiled **.jar** file will appear in target folder.

## 🐛 Troubleshooting Guide

### EasyFiji can not be found in the **Plugins** menu.
Make sure EasyFiji_2-*.*.*.jar is insdie the plugins folder of Fiji and restart.
### Buttons/Sliders Don't respond
Images must be opened and selected (active window). 
### Fiji Freezes or GUI Becomes Unresponsive
Long-running process (e.g., filters, intensity correction, large image files) could run slow, any interactiion with the images and fiji commands (change channel, adjusting contrast, ...) could potentially interrupt the process causing Fiji freeze. Restart Fiji can help!
### Image display doesnt update after click **apply LUTs**
Duo to Fiji internal bugs, sometimes the image won't auto updates when clicking **apply luts** under Process Tab. The image pixel value are actually modified (function works!) and switch color channel can see fix the display issue.
### Standalone use
Some buttons (color fusion and ) call the macro scripts (reside in resources/scripts/) to perform the functions, people can also use these scripts directly without EasyFiji. More custom scripts can be easily further added to extend the community usability.
### Unrecognzed command
EasyFiji use features that that are bundled with newer version of Fiji like Kymograph and BioFormats, Please install these plugins if want to use these features. 
### 📢 Reporting Issues & Getting Support
If you encounter a bug, installation problem, or unexpected behavior, please create a issue through the project's GitHub page. (please include operating system, fiji version, steps to reproduce the error, possible example image, error messages, screenshots if available)

## 📄 License
Released under the MIT License. See [LICENSE](LICENSE) file for details.


