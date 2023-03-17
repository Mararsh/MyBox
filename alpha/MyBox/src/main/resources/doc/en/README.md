# [中文ReadMe](https://github.com/Mararsh/MyBox)   ![ReadMe](https://mara-mybox.sourceforge.io/iconGo.png)

# MyBox: Set of Easy Tools
This is desktop application based on JavaFx to provide simple and easy functions. It's free and open sources.

## Latest Versions       
Contents of this document may be expired, but its links refer to the latest versions.     

The latest versions and all archived versions can be downloaded in following addresses:             

| address | dowloads | readme | 
| --- | --- | --- | 
| github | [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  | [English](https://mararsh.github.io/readme-en.html)   [Chinese](https://mararsh.github.io/readme-zh.html) |
| sourceforge | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)  | [English](https://mara-mybox.sourceforge.io/readme-en.html)  [Chinese](https://mara-mybox.sourceforge.io/readme-zh.html) |
| cloud | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)   |  |


## What's New                       
2023-3-13 v6.7.1                
                
* Added:                                   
     - Edit mode of html: DOM.                                                             
     - Manufacture image: blend colors against selected scope.                    
     - Set http-equiv in batch.                                                           
     - Can customized colors of buttons.                                                                                                                                               
* Improved:                                   
     - Manufacture image: select whether replace hue/saturation/brightness when replace colors.                                                              
     - New attributes of color: RYB angle, RGB invert, and RYB complementary. And more examples of color palettes.                                                                         
     - Can set scroll size for selectors.                                                            
     - Link menus work before html page is loaded completely.                                                             
     - Parse html with jsoup.                                                                                   
     - Option about whether pop menu/window when hover over buttons.                                                                                   
     - Links in ReadMe refer to latest versions.                                                                                
     - Set JAVA_HOME in packing scripts.                                                                                
* Removed:                                 
     - Not support setting interface styles any more.                                                             
     - Remove unuseful sliders in some interfaces.                                                             
     - Names of packages do not contain version number any more.                                                                                                                                             
* Both Java and JavaFx are upgraded to v19.                                                             
* Solved problems:                                      
     - All file backups may be cleared when add file backup.                                                             
     - Fail to load map when image file of points is not existed.                                                             
     - Fail to add address as favorite.                                                             
     - Visit histories of paths do not work in some interfaces.                                                             
     - Handle location incorrectly when paste images in batch.                                                             
     - Shorcuts dot not work when edit colors of image.                                                                              
     - Edit histories are not saved for Javascript.                                                                              
 
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.7.1)                           
   
## Download and Execution
Packages of each version have been uploaded at [Releases](https://github.com/Mararsh/MyBox/releases?) directory. 


### Source Codes
[MyBox-src.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-src.zip)   65MB-         
[Developement Guide](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.pdf)     [Packing Steps](https://mara-mybox.sourceforge.io/pack_steps_en.html)        


### Self-contain packages
Self-contain packages include all files and need not java env nor installation.      

| Platform | Link | Size  | Launcher |        
| --- | --- | ---  | ---  |
| win10 x64 | [MyBox-win-x64.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win10-x64.zip)  | 260MB- | MyBox.exe |
| CentOS 7 x64 | [MyBox-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-CentOS7-x64.tar.gz)  | 280MB-  | MyBox  |
| mac | [MyBox-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.dmg)  | 280MB-  |  MyBox.app   |        

User can double click the launcher to start MyBox or run it by command line. The default "Open Method" of image/text/PDF files can be associated to MyBox and a file can be opened directly by MyBox by double clicking the file's name.        

### Jar
When JRE or JDK([Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or [open jdk](http://jdk.java.net/)) is installed, jar can run:        
   
| Platform | Link | Size  | Requirements |        
| --- | --- | ---  | ---  |
| win | [MyBox-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win-jar.zip)  | 190MB- | Java 19 or higher |
| linux | [MyBox-linux.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-linux.jar.gz)  | 200MB-  | Java 19 or higher |
| mac | [MyBox-mac.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.jar.gz)  |  200MB-  | Java 19 or higher |        


Run following command to launch this program with Jar package:        
<PRE><CODE>     java   -jar   MyBox.jar</CODE></PRE>        

A file path can follow the command as argument to be opened directly by MyBox. Example, following command will open the image:        
<PRE><CODE>     java   -jar   MyBox.jar   /tmp/a1.jpg</CODE></PRE>        


## Limitation        
* Without enough testings, MyBox has low reliability. Data damage happened in some versions.       
* New functions being added and codes being improved continually, MyBox has bad stability. Unexpected function failures happened in some versions.      
* MyBox windows may often be blocked when some Input Method is running. Workaround is to disable/uninstall this Input Method.        

## Migration
1. Each version has itself's configuration file. New version can copy parameters from existed versions.             
2. Data handled in each version are under "Data Directory" referred by it. Multiple versions can refer to same data directory.
3. MyBox is backward compatibility: Later version can work on data of previous versions.
While forward compatibility is not supported: Wrong may happen when old version handles data of new version.


## Configuration 
Configuration file is under "User Home":        

| Platform | Path of MyBox Configuration File |        
| --- | --- |
| win | `C:\users\UserName\mybox\MyBox_vVERVION.ini`  |
| linux | `/home/UserName/mybox/MyBox_vVERVION.ini` |
| mac | `/Users/UserName/mybox/MyBox_vVERVION.ini` |        

Add parameter "config=\"FilePath\"" when run jar to change configuration file temporarily.        
Function "Settings" can be used to change configuration values.        

# Resource Addresses        
| Contents | Link |        
| --- | --- |
| Project Main Page | [https://github.com/Mararsh/MyBoxl](https://github.com/Mararsh/MyBox)   |
| Source Codes and Compiled Packages |  [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  |
| Submit Software Requirements and Problem Reports | [https://github.com/Mararsh/MyBox/issues](https://github.com/Mararsh/MyBox/issues) |
| Data | [https://github.com/Mararsh/MyBox_data](https://github.com/Mararsh/MyBox_data) |
| Documents | [https://github.com/Mararsh/MyBoxDoc](https://github.com/Mararsh/MyBoxDoc) |
| Mirror Site | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/) |
| Cloud Storage | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F) |       
   
# Documents        
|              Name              | Version |   Time     |                                                                                                                                            English                                                                                                                                            |                                                                                                                                            Chinese                                                                                                                                            |
|--------------------------------|---------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Development Logs               | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_devLogs.html)                                                                                                                                                                                                                                    |
| Shortcuts                      | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts.html)                                                                                                                                                                                                                                  |
| Functions list                 | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_functions_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_functions.html)                                                                                                                                                                                                                                  |
| Packing Steps                  | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mara-mybox.sourceforge.io/pack_steps.html)                                                                                                                                                                                                                                       |
| Development Guide              | 2.1     | 2020-8-27  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.odt)                                                                                                                                                                                  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.pdf)  [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.odt)                                                                                                                                                                                                                |
| User Guide - Overview          | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-Overview-en/MyBox-Overview-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-Overview-zh/MyBox-Overview-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.odt)                     |
| User Guide - Data Tools        | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DataTools-en/MyBox-DataTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DataTools-zh/MyBox-DataTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.odt)                 |
| User Guide - Document Tools    | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DocumentTools-en/MyBox-DocumentTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.odt) | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DocumentTools-zh/MyBox-DocumentTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.odt) |
| User Guide - Image Tools       | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-ImageTools-en/MyBox-ImageTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-ImageTools-zh/MyBox-ImageTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.odt)             |
| User Guide - File Tools        | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-FileTools-en/MyBox-FileTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-FileTools-zh/MyBox-FileTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.odt)                 |
| User Guide - Network Tools     | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-NetworkTools-en/MyBox-NetworkTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.odt)     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-NetworkTools-zh/MyBox-NetworkTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.odt)     |
| User Guide - Media Tools       | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-MediaTools-en/MyBox-MediaTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-MediaTools-zh/MyBox-MediaTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.odt)             |
| User Guide - Development Tools | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DevTools-en/MyBox-DevTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DevTools-zh/MyBox-DevTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.odt)                     |
| Examples - Information in Tree | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_tree_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_tree.html)                                                                                                                                                                                                                                    |
| Examples - Favorite Address    | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_web_favorite_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_web_favorite.html)                                                                                                                                                                                                                                    |
| Examples - Notes               | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/mybox_examples_notes_en.html)                                                                                                                                                                                                                          | [html](https://mara-mybox.sourceforge.io/mybox_examples_notes.html)                                                                                                                                                                                                                                    |
| Examples - SQL                 | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_sql_en.html)                                                                                                                                                                                                                            | [html](https://mara-mybox.sourceforge.io/mybox_examples_jshell.html)                                                                                                                                                                                                                                    |
| Examples - JShell              | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_jshell_en.html)                                                                                                                                                                                                                         | [html](https://mara-mybox.sourceforge.io/mybox_examples_jexl.html)                                                                                                                                                                                                                                    |
| Examples - JEXL                | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_jexl_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_javascript.html)                                                                                                                                                                                                                                    |
| Examples - JavaScript          | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_javascript_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_javascript.html)                                                                                                                                                                                                                                    |              
| Examples - Math Function       | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_math_funtion_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_math_funtion.html)                                                                                                                                                                                                                                    |              
| Examples - Row Filter          | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_row_filter_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_row_filter.html)                                                                                                                                                                                                                                    |              
| About - data grouping     | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping.html)                                                                                                                                                                                                                                    |
| About - Color                  | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_color_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_color.html)                                                                                                                                                                                                                                    |              
| About - Coordinate System      | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system_en.html)                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system.html)                                                                                                                                                                                                                                    |              
| About - Media                  | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_media_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_media.html)                                                                                                                                                                                                                                    |              
| About - Data Analysis          | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis.html)                                                                                                                                                                                                                                    |              
| About - Items in one sentence about java        | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/mybox_about_java_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_java.html)                                                                                                                                                                                                                                    |
| About - Items in one sentence about javafx      | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx.html)                                                                                                                                                                                                                                    |
| Palette - Common Web Colors                     | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all.html)                                                                                                                                                                                                                                   |
| Palette - Chinese Traditional Colors            | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all_en.html)                                                                                                                          | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese.html) [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all.html)                                                                                                                                                                                                                                    |
| Palette - Japanese Traditional Colors           | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all_en.html)                                                                                                                        | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all.html)                                                                                                                                                                                                                                  |
| Palette - Colors from colorhexa.com             | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all_en.html)                                                                                                                      | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all.html)                                                                                                                                                                                                                                   |
| Palette - Art hues wheel(RYB) - 12 colors       | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all.html)                                                                                                                                                                                                                                   |
| Palette - Art hues wheel(RYB) - 24 colors       | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all.html)                                                                                                                                                                                                                                   |
| Palette - Art hues wheel(RYB) - 360 colors      | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all.html)                                                                                                                                                                                                                                   |
| Palette - Optical hues wheel(RGB) - 12 colors   | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all.html)                                                                                                                                                                                                                                   |
| Palette - Optical hues wheel(RGB) - 24 colors   | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all.html)                                                                                                                                                                                                                                   |
| Palette - Optical hues wheel(RGB) - 360 colors  | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all.html)                                                                                                                                                                                                                                   |
| Palette - Art paints                            | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all.html)                                                                                                                                                                                                                                   |
| Palette - MyBox Colors                          | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all_en.html)                                                                                                                              | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all.html)                                                                                                                                                                                                                                   |
| Palette - Gray scale                            | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all.html)                                                                                                                                                                                                                                   |
| Stories of Images                               | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-StoriesOfImages-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-StoriesOfImages-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-zh.odt)                     |   


# Implementation        
MyBox is based on following open sources:        

| Name | Role | Link |
| --- | --- | --- |
| JDK | Java | http://jdk.java.net/   |
|   |   | https://www.oracle.com/technetwork/java/javase/downloads/index.html  |
|   |   | https://docs.oracle.com/en/java/javase/19/docs/api/index.html  |
|  JavaFx | GUI |  https://gluonhq.com/products/javafx/ |
|   |   |  https://docs.oracle.com/javafx/2/  |
|   |   |  https://gluonhq.com/products/scene-builder/  |
|   |   |  https://openjfx.io/javadoc/19/ |
| Derby | Database | http://db.apache.org/derby/ |
| NetBeans | IDE| https://netbeans.org/ |
| jpackage | pack | https://docs.oracle.com/en/java/javase/19/docs/specs/man/jpackage.html |
| maven | build | https://maven.apache.org/ |
| jai-imageio | Image manufacture | https://github.com/jai-imageio/jai-imageio-core |
| PDFBox | PDF manufacture | https://pdfbox.apache.org/ |
| PDF2DOM | PDF to html | http://cssbox.sourceforge.net/pdf2dom/ |
| GifDecoder | Decode broken gif  | https://github.com/DhyanB/Open-Imaging/ |
| EncodingDetect | Determine encoding of text file | https://www.cnblogs.com/ChurchYim/p/8427373.html |
| Lindbloom | Color theory| http://brucelindbloom.com/index.html |
| Free Icons | Icon | https://icons8.com/icons/set/home |
| tess4j | OCR | https://github.com/nguyenq/tess4j |
| tesseract | OCR | https://github.com/tesseract-ocr/tesseract |
| barcode4j | Create barcodes | http://barcode4j.sourceforge.net |
| zxing | Create/Decode barcodes | https://github.com/zxing/zxing |
| flexmark-java | Convert Markdown | https://github.com/vsch/flexmark-java |
| commons-compress | archive/compress | https://commons.apache.org/proper/commons-compress |
| XZ for Java | archive/compress | https://tukaani.org/xz/java.html |
| ffmpeg| convert/create medias | http://ffmpeg.org |
| image4j | ico format | https://github.com/imcdonagh/image4j |
| AutoCommitCell | Submit updates | https://stackoverflow.com/questions/24694616 （Ogmios） |
| GaoDe | Map | https://lbs.amap.com/api/javascript-api/summary |
| GaoDe | Coordinate | https://lbs.amap.com/api/webservice/guide/api/georegeo |
| WeiBo | Image materials| https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665 |
| poi | Microsoft Documents | https://poi.apache.org |
| LabeledBarChart | Javafx charts | https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland) |
| commons-csv | CSV | https://commons.apache.org/proper/commons-csv/ |
| geonames | Location data | https://www.geonames.org/countries/ |
| world-area | Location data | https://github.com/wizardcode/world-area |
| China National Bureau of Statistics | Data | http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/ |
| JHU | COVID-19 data | https://github.com/CSSEGISandData/COVID-19 |
| Website | Color data | https://tool.lanrentuku.com/color/china.html |
| Book | Materials | https://book.douban.com/subject/3894923/ |
| National Geomatics Center of China | Map | http://lbs.tianditu.gov.cn/api/js4.0/guide.html |
| movebank | Location data | https://www.datarepository.movebank.org |
| CoordinateConverter | convert coordinates | https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage |
| JavaMail | email | https://javaee.github.io/javamail/ |
| Commons IO | File IO | https://commons.apache.org/proper/commons-io/ |
| colorhexa | Color data | https://www.colorhexa.com/color-names |
| WQY | Font file | http://wenq.org/wqy2/ |
| ttc2ttf | Extract ttf | https://github.com/fermi1981/TTC_TTF |
| sfds | Handwriting | http://www.sfds.cn/725B/ |
| Book | Materials | https://book.douban.com/subject/10465940/ |
| PaginatedPdfTable | PDF | https://github.com/eduardohl/Paginated-PDFBox-Table-Sample |
| jsoup | DOM | https://jsoup.org/ |        
| ZhiHu | Materials | https://www.zhihu.com/question/41580677/answer/1300242801 |             
| commons-math | Calculation | https://commons.apache.org/proper/commons-math/index.html |
| JEXL | Calculation | https://commons.apache.org/proper/commons-jexl |
| OpenOffice | Document | [http://www.openoffice.org/](http://www.openoffice.org/) |
| nashorn | JavaScript | [https://openjdk.org/projects/nashorn/](https://openjdk.org/projects/nashorn/) |
| echarts-gl | WebGL | [https://github.com/ecomfe/echarts-gl](https://github.com/ecomfe/echarts-gl) |
| RYB hues | art colors | [https://blog.csdn.net/weixin_44938037/article/details/90599711](https://blog.csdn.net/weixin_44938037/article/details/90599711) |
| jsch | sftp | [http://www.jcraft.com/jsch/](http://www.jcraft.com/jsch/) |

# Features        
## Cross Platforms        

MyBox is implemented in pure Java and only based on open sources, and it can run on platforms which support Java 19.        
Versions before v5.3 are based on Java 8.        

## Internationalized       

1. All codes of MyBox are internationalized. Language can be switched in time.
2. Each langauge is defined in one resource file, like: "Messages_NAME.properties".
3. Support adding new languages online. Table is provided with English as comparison. New langauges take effects at once.        
Example,  the new language is named as "aa", and its resource file is "Messages_aa.properties".        
4. Translated languages can be shared with others: Put the files under "mybox_languages" of data path and MyBox is aware of them immediately.        
5. Embed Chinese and English, whose files are under path `MyBox/src/main/resources/bundles/`.        

| Language | Resource file of interface |
| --- | --- | 
| Chinese | Messages_zh_CN.properties | 
| English | Messages_en.properties |   


## Personal       

1. All functions are executed in local host.        
2. All data are saved in local host.    
3. No register/login/DataCenter/Cloud.     
4. Access network only when user requests.    
5. Only read/write data which user need.       


## Friendly       

1. Provide tips of current function/interface.       
2. Remember user's inputs or selections, and provide histories list.
3. Interfaces can be adjusted and some panes can be hidden.
4. Behaviours of some controls can be chosen. 
5. Provide context menu and function menu to texts/image/html/data.
6. Current texts/image/html/data can be popped.
7. Clipboards of texts/image/data can be managed.
8. Provide examples and useful information. 
9. Appearance of interface and controls can be chosen and modified.    
       

## Data Compatible    

1. Exported data are in common text formats, like txt/csv/xml/json/html.
2. Imported data are in common text format, like txt/csv.       
3. At least one exported format can be imported. 
4. Imported data are self-contain, that original data can be rebuilt without extra data.       


## Embedded Database System            

1. Derby is free and open sources:            
    - When start MyBox for the first time, some seconds are cost to create internal database tables and write initial data.            
    - All database files are under data path, so different target data sets can be handled by switching data paths.            
    - Multiple MyBox instances can share data by visiting same data path.            
    - When database files not found under data path(damaged or deleted unexpectedly), MyBox can create database automatically.            
2. Derby runs in embedded mode by default:             
    - External processes can not access the database.            
    - Only one MyBox instance can use the database at the same time.            
3. Derby can run in network mode:            
    - The first visitor becomes the server.            
    - Processes of local host can visit the database in port 1527 and becomes its clients.                
    - Processes out of local host can not visit the database directly.                
4. MyBox internal tables save the data required by all functions. User can access and modify the internal data online.            
5. User can define customized database tables.            
6. User can view and edit definitons and data of database tables in paginated tables, and execute SQL statements.                   
7. To implement data sorting, tranposing, statistic, and grouping, MyBox will create temporary database tables, and clear them in time.            

## Assit Coding            

1. Monitor memory/CPU usage in real time.
2. Adjust logging online.       
3. Generate icons automatically.
4. Edit and apply languages online.
5. Test automatically.
6. All kinds of documents.   

# Snapshots            

![Snap-cover](https://mara-mybox.sourceforge.io/snap-cover-en.jpg)       

![Snap-interface](https://mara-mybox.sourceforge.io/snap-interface-en.jpg)  

![Snap-table](https://mara-mybox.sourceforge.io/snap-table-en.jpg)       


    