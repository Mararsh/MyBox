# [中文ReadMe](https://github.com/Mararsh/MyBox)   ![ReadMe](https://mararsh.github.io/MyBox/iconGo.png)

# MyBox: Set of Easy Tools
This is desktop application based on JavaFx to provide simple and easy functions. It's free and open sources.

## What's New                       
2023-2-28 v6.7.1                
                                
* Added features:                  
     - Group data by same values of century/year/month/day/hour/minute/second of a time column.                                               
     - Group data by same results of row expressions.                                                                    
     - Charset can be set for "Run System Command".                                                                     
     - Document "Stories of Images".                                                                  
* Improved codes:                  
     - Grouping parameters are saved in database instead of in memory.                                               
     - In data formats, hour/minute/second are always long values.                                                                     
     - Rename internal images.                                                                  
 * Solved problems:                       
     - When group by value ranges of time column, ranges should be displayed as time rather than as long.                                               
     - When group by conditions, all values should be imported in temprory table, or else results may be wrong.                                                           
     - When group by rows number, split number is wrongly picked as split size.                                                         
 
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.7.1)             

Happy and healthy Chinese New Year!                 
   
## Download and Execution
Packages of each version have been uploaded at [Releases](https://github.com/Mararsh/MyBox/releases?) directory now. 
You can find them by clicking `releases` tab in main page of this project.        


### Source Codes
[MyBox-6.7.1-src.zip](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-src.zip)   62MB-        

About structure, editing, and building of source codes, please refer to 
[Developement Guide](https://sourceforge.net/projects/mara-mybox/files/documents/dev_guide_2.1/MyBox-DevGuide-2.1-en.pdf) and
[Packing Steps](https://mararsh.github.io/MyBox/pack_steps_en.html)        


### Self-contain packages
Self-contain packages include all files and need not java env nor installation.      

| Platform | Link | Size  | Launcher |        
| --- | --- | ---  | ---  |
| win10 x64 | [MyBox-6.7.1-win-x64.zip](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-win10-x64.zip)  | 260MB- | MyBox.exe |
| CentOS 7 x64 | [MyBox-6.7.1-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-CentOS7-x64.tar.gz)  | 280MB-  | bin/MyBox  |
| mac | [MyBox-6.7.1-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-mac.dmg)  | 280MB-  |  MyBox-6.7.1.app   |        

User can double click the launcher to start MyBox or run it by command line. The default "Open Method" of image/text/PDF files can be associated to MyBox and a file can be opened directly by MyBox by double clicking the file's name.        

### Jar
When JRE or JDK([Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or [open jdk](http://jdk.java.net/)) is installed, jar can run:        
   
| Platform | Link | Size  | Requirements |        
| --- | --- | ---  | ---  |
| win | [MyBox-6.7.1-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-win-jar.zip)  | 190MB- | Java 18 or higher |
| linux | [MyBox-6.7.1-linux-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-linux-jar.zip)  | 200MB-  | Java 18 or higher |
| mac | [MyBox-6.7.1-mac-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.7.1/MyBox-6.7.1-mac-jar.zip)  |  200MB-  | Java 18 or higher |        


Run following command to launch this program with Jar package:        
<PRE><CODE>     java   -jar   MyBox-6.7.1.jar</CODE></PRE>        

A file path can follow the command as argument to be opened directly by MyBox. Example, following command will open the image:        
<PRE><CODE>     java   -jar   MyBox-6.7.1.jar   /tmp/a1.jpg</CODE></PRE>        

### Other addresses to download
Download from cloud storage: [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)               
Download from sourceforge: [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)             

## Limitation        
* Without enough testings, MyBox has low reliability. Data damage even happened in some versions.       
* New functions being added and codes being improved continually, MyBox has bad stability. Unexpected function failures happened sometimes.       
* MyBox windows may often be blocked when some Input Method is running. Workaround is to disable/uninstall this Input Method.        

## Migration
1. Each version has itself's configuration file. New version can copy parameters from existed versions.             
2. Data handled in each version are under "Data Directory" referred by it. Multiple versions can refer to same data directory.
3. MyBox is backward compatibility: Later version can work on data of previous versions.
While forward compatibility is not supported: Wrong may happen when old version handles data of new version.


## Configuration <a id="Config"></a>
Configuration file is under "User Home":        

| Platform | Path of MyBox Configuration File |        
| --- | --- |
| win | `C:\users\UserName\mybox\MyBox_v6.7.1.ini`  |
| linux | `/home/UserName/mybox/MyBox_v6.7.1.ini` |
| mac | `/Users/UserName/mybox/MyBox_v6.7.1.ini` |        

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
| Development Logs               | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBox/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mararsh.github.io/MyBox/mybox_devLogs.html)                                                                                                                                                                                                                                    |
| Shortcuts                      | 6.5.6   | 2022-6-11  | [html](https://mararsh.github.io/MyBox/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mararsh.github.io/MyBox/mybox_shortcuts.html)                                                                                                                                                                                                                                  |
| Functions list                 | 6.6.2   | 2022-11-30 | [html](https://mararsh.github.io/MyBox/mybox_functions_en.html)                                                                                                                                                                                                                               | [html](https://mararsh.github.io/MyBox/mybox_functions.html)                                                                                                                                                                                                                                  |
| Packing Steps                  | 6.3.3   | 2020-9-27  | [html](https://mararsh.github.io/MyBox/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mararsh.github.io/MyBox/pack_steps.html)                                                                                                                                                                                                                                       |
| Development Guide              | 2.1     | 2020-8-27  | [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevGuide-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevGuide-en.odt)                                                                                                                                                                                  | [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevGuide-zh.pdf)  [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevGuide-zh.odt)                                                                                                                                                                                                                |
| User Guide - Overview          | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-Overview-en/MyBox-Overview-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-Overview-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-Overview-en.odt)                     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-Overview-zh/MyBox-Overview-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-Overview-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-Overview-zh.odt)                     |
| User Guide - Data Tools        | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-DataTools-en/MyBox-DataTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DataTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DataTools-en.odt)                 | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-DataTools-zh/MyBox-DataTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DataTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DataTools-zh.odt)                 |
| User Guide - Document Tools    | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-DocumentTools-en/MyBox-DocumentTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DocumentTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DocumentTools-en.odt) | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-DocumentTools-zh/MyBox-DocumentTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DocumentTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DocumentTools-zh.odt) |
| User Guide - Image Tools       | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-ImageTools-en/MyBox-ImageTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-ImageTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-ImageTools-en.odt)             | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-ImageTools-zh/MyBox-ImageTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-ImageTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-ImageTools-zh.odt)             |
| User Guide - File Tools        | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-FileTools-en/MyBox-FileTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-FileTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-FileTools-en.odt)                 | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-FileTools-zh/MyBox-FileTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-FileTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-FileTools-zh.odt)                 |
| User Guide - Network Tools     | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-NetworkTools-en/MyBox-NetworkTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-NetworkTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-NetworkTools-en.odt)     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-NetworkTools-zh/MyBox-NetworkTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-NetworkTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-NetworkTools-zh.odt)     |
| User Guide - Media Tools       | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-MediaTools-en/MyBox-MediaTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-MediaTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-MediaTools-en.odt)             | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-MediaTools-zh/MyBox-MediaTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-MediaTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-MediaTools-zh.odt)             |
| User Guide - Development Tools | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-DevTools-en/MyBox-DevTools-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevTools-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevTools-en.odt)                     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-DevTools-zh/MyBox-DevTools-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevTools-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-DevTools-zh.odt)                     |
| Examples - Notes               | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_notes_en.html)                                                                                                                                                                                                                          | [html](https://mararsh.github.io/MyBox/mybox_examples_notes.html)                                                                                                                                                                                                                                    |
| Examples - Information in Tree | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_tree_en.html)                                                                                                                                                                                                                           | [html](https://mararsh.github.io/MyBox/mybox_examples_tree.html)                                                                                                                                                                                                                                    |
| Examples - Favorite Address    | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_web_favorite_en.html)                                                                                                                                                                                                                   | [html](https://mararsh.github.io/MyBox/mybox_examples_web_favorite.html)                                                                                                                                                                                                                                    |
| Examples - SQL                 | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_sql_en.html)                                                                                                                                                                                                                            | [html](https://mararsh.github.io/MyBox/mybox_examples_jshell.html)                                                                                                                                                                                                                                    |
| Examples - JShell              | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_jshell_en.html)                                                                                                                                                                                                                         | [html](https://mararsh.github.io/MyBox/mybox_examples_jexl.html)                                                                                                                                                                                                                                    |
| Examples - JEXL                | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_jexl_en.html)                                                                                                                                                                                                                           | [html](https://mararsh.github.io/MyBox/mybox_examples_javascript.html)                                                                                                                                                                                                                                    |
| Examples - JavaScript          | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_javascript_en.html)                                                                                                                                                                                                                     | [html](https://mararsh.github.io/MyBox/mybox_examples_javascript.html)                                                                                                                                                                                                                                    |              
| Examples - Math Function       | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_math_funtion_en.html)                                                                                                                                                                                                                   | [html](https://mararsh.github.io/MyBox/mybox_examples_math_funtion.html)                                                                                                                                                                                                                                    |              
| Examples - Row Filter          | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_examples_row_filter_en.html)                                                                                                                                                                                                                     | [html](https://mararsh.github.io/MyBox/mybox_examples_row_filter.html)                                                                                                                                                                                                                                    |              
| About - Color                  | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_about_color_en.html)                                                                                                                                                                                                                             | [html](https://mararsh.github.io/MyBox/mybox_about_color.html)                                                                                                                                                                                                                                    |              
| About - Coordinate System      | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_about_coordinate_system_en.html)                                                                                                                                                                                                                 | [html](https://mararsh.github.io/MyBox/mybox_about_coordinate_system.html)                                                                                                                                                                                                                                    |              
| About - Media                  | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_about_media_en.html)                                                                                                                                                                                                                             | [html](https://mararsh.github.io/MyBox/mybox_about_media.html)                                                                                                                                                                                                                                    |              
| About - Data Analysis          | 6.6.1   | 2022-11-16 | [html](https://mararsh.github.io/MyBox/mybox_about_data_analysis_en.html)                                                                                                                                                                                                                     | [html](https://mararsh.github.io/MyBox/mybox_about_data_analysis.html)                                                                                                                                                                                                                                    |              
| Palette - Common Web Colors                    | 6.7.1   | 2023-2-28 | [rgba](https://mararsh.github.io/MyBox/mybox_palette_web_en.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_web_all_en.html)                                                                                                                                  | [rgba](https://mararsh.github.io/MyBox/mybox_palette_web.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_web_all.html)                                                                                                                                                                                                                                   |
| Palette - Chinese Traditional Colors           | 6.7.1   | 2023-2-28 | [rgba](https://mararsh.github.io/MyBox/mybox_palette_chinese_en.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_chinese_all_en.html)                                                                                                                          | [rgba](https://mararsh.github.io/MyBox/mybox_palette_chinese.html) [all](https://mararsh.github.io/MyBox/mybox_palette_chinese_all.html)                                                                                                                                                                                                                                    |
| Palette - Japanese Traditional Colors          | 6.7.1   | 2023-2-28 | [rgba](https://mararsh.github.io/MyBox/mybox_palette_japanese_en.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_japanese_all_en.html)                                                                                                                        | [rgba](https://mararsh.github.io/MyBox/mybox_palette_japanese.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_japanese_all.html)                                                                                                                                                                                                                                  |
| Palette - Colors from colorhexa.com            | 6.7.1   | 2023-2-28 | [rgba](https://mararsh.github.io/MyBox/mybox_palette_colorhexa_en.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_colorhexa_all_en.html)                                                                                                                      | [rgba](https://mararsh.github.io/MyBox/mybox_palette_colorhexa.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_colorhexa_all.html)                                                                                                                                                                                                                                   |
| Palette - MyBox Colors                         | 6.7.1   | 2023-2-28 | [rgba](https://mararsh.github.io/MyBox/mybox_palette_mybox_en.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_mybox_all_en.html)                                                                                                                              | [rgba](https://mararsh.github.io/MyBox/mybox_palette_mybox.html)  [all](https://mararsh.github.io/MyBox/mybox_palette_mybox_all.html)                                                                                                                                                                                                                                   |
| Stories of Images              | 6.7.1   | 2023-2-28  | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-StoriesOfImages-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-StoriesOfImages-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-StoriesOfImages-en.odt)                     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-StoriesOfImages-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-StoriesOfImages-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/current/MyBox-StoriesOfImages-zh.odt)                     |   


# Implementation        
MyBox is based on following open sources:        

| Name | Role | Link |
| --- | --- | --- |
| JDK | Java | http://jdk.java.net/   |
|   |   | https://www.oracle.com/technetwork/java/javase/downloads/index.html  |
|   |   | https://docs.oracle.com/en/java/javase/18/docs/api/index.html  |
|  JavaFx | GUI |  https://gluonhq.com/products/javafx/ |
|   |   |  https://docs.oracle.com/javafx/2/  |
|   |   |  https://gluonhq.com/products/scene-builder/  |
|   |   |  https://openjfx.io/javadoc/18/ |
| Derby | Database | http://db.apache.org/derby/ |
| NetBeans | IDE| https://netbeans.org/ |
| jpackage | pack | https://docs.oracle.com/en/java/javase/18/docs/specs/man/jpackage.html |
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

# Features        
## Cross Platforms        

MyBox is implemented in pure Java and only based on open sources, and it can run on platforms which support Java 18.        
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

1. All functions are executed locally.        
2. No register/login/DataCenter/Cloud.     
3. Access network only when user requests.    
4. Only read/write data which user need.       


## Friendly       

1. Provide tips of current function.       
2. Remember user's inputs or selections, and provide histories list.
3. Interfaces can be adjusted and some panes can be hidden.
4. Pop current texts, image, or data.
5. Provide buttons or menu items to execute relative functions easily.
6. Provide examples and useful information.       

## Embedded Database System            

1. Embedded derby which is free and open sources.
2. Derby can run in embedded mode or in network mode(Only visible to local processes).       
3. User can access and modify MyBox internal data.
4. User can define and use database tables, and execute SQL statements.       

## Data Compatible    

1. Exported data are in common text formats, like txt/csv/xml/json/html.
2. Imported data are in common text format, like txt/csv.       
3. At least one exported format can be imported. 
4. Imported data are self-contain, that original data can be rebuilt without extra data.       



![Snap-cover](https://mararsh.github.io/MyBox/snap-cover-en.jpg)       

![Snap-interface](https://mararsh.github.io/MyBox/snap-interface-en.jpg)  

![Snap-table](https://mararsh.github.io/MyBox/snap-table-en.jpg)       


    