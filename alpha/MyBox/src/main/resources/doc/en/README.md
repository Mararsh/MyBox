# [中文ReadMe](https://github.com/Mararsh/MyBox)   ![ReadMe](https://mara-mybox.sourceforge.io/iconGo.png)

# MyBox: Set of Easy Tools
This is compute application to provide simple and easy functions. It's free and open sources.

## What's New                       
2025-12-29 v6.9.2               
                                                                                                                          
* Chinese name of MyBox is "吾匣". Icon is updated.                                      
* Improve:                                      
    - Shorten main menu.                                                                                                                                 
    - Data manufacture:                                                                                                                                 
          - Option to clear data definition and reload file.                    
          - Permit to load csv file with duplicated header names.                    
          - Comments in csv/excel/text/html file can be read and write. (Lines started with "#")                    
          - Refine the interfaces of data target.                    
          - New examples about Chinese characters.                    
    - When edit image, option to allow moving the shape .                                                                                                                                 
    - When view or edit image, can navigate to next file or previous file.                                                                                                                                 
    - When sample image, correct values can be set automatically when inputs are beyond the image.                                                                                                                                 
    - When browse files, option about where and how to open files.                                                                                                                                 
* Upgrade to java 25.                                
* Solved problems:                                
     - Shortcuts do not work when the focus is not in top pane.                                                                                                                                                   
     - Data manufacture:                                                                                                                                 
          - Error popped when open file.                    
          - Fail to splice data.                    
          - Interface should be updated after data are cleared.                   
          - Fail to open interface "Export".                    
          - New examples about Chinese characters.                    
          - Fail to select columns when their names are different from their labels(displayed names).                    
          - Attributes of column may be missed when copy and cause wrong definition.                    
          - Data definition is always created and never updated when single quote is in name of file or table.                    
          - File menu should be provided in interfaces of loading data file.                    
     - Html: Special characters should be escaped in tag CODE.                                                                                                                               

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.9.2)         
            
## Notice         
Without enough testings, MyBox has low reliability and bad stability. When use MyBox, following may happen:        
 
* Files/Data are damaged.       
* Output wrong results.      
* Functions fail.      
* Other unexpected behaviors.     


# Download and Execution

## Source Codes          
| Download | Size | Development Guide | Packing Steps |           
| --- | --- | --- | --- | 
| [MyBox-src.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-src.zip)  |  120M- | [pdf](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.pdf) | [html](https://mara-mybox.sourceforge.io/pack_steps_en.html) |
           
## Self-contain packages
Self-contain packages include all files and need not java env nor installation.      

| Platform | Link | Size  | Launcher |        
| --- | --- | ---  | ---  |
| win x64 | [MyBox-win-x64.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win-x64.zip)  | 360MB- | MyBox.exe |
| Mint x64 | [MyBox-Mint-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-Mint-x64.tar.gz)  | 360MB-  | MyBox  |
| mac | [MyBox-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.dmg)  | 360MB-  |  MyBox.app   |        

User can double click the launcher to start MyBox or run it by command line. The default "Open Method" of image/text/PDF files can be associated to MyBox and a file can be opened directly by MyBox by double clicking the file's name.        

## Jar
When JRE or JDK([Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or [open jdk](http://jdk.java.net/)) is installed, jar can run.        
   

| Platform | Link | Size  | Requirements |        
| --- | --- | ---  | ---  |
| win | [MyBox-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win-jar.zip)  | 280MB- | Java 25 or higher |
| linux | [MyBox-linux.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-linux.jar.gz)  | 280MB-  | Java 25 or higher |
| mac | [MyBox-mac.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.jar.gz)  |  280MB-  | Java 25 or higher |        


Run following command to launch this program with Jar package:        
`     java   -jar   MyBox.jar`      

A file path can follow the command as argument to be opened directly by MyBox. Example, following command will open the image:        
`    java   -jar   MyBox.jar   /tmp/a1.jpg`      

## Versions       
Contents of this document may be expired, but its links refer to the latest versions.     

The latest versions and archived versions can be downloaded in following addresses:             

| address | download | readme | 
| --- | --- | --- | 
| github | [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  | [English](https://mararsh.github.io/MyBox/readme_en.html)     [Chinese](https://mararsh.github.io/MyBox/readme_zh.html) |
| sourceforge | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)  | [English](https://mara-mybox.sourceforge.io/readme_en.html)    [Chinese](https://mara-mybox.sourceforge.io/readme_zh.html) |
| cloud | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)   |  |      

## Configuration 
Configuration file is under "User Home":        

| Platform | Path of MyBox Configuration File |        
| --- | --- |
| win | `C:\users\UserName\mybox\MyBox_vVERVION.ini`  |
| linux | `/home/UserName/mybox/MyBox_vVERVION.ini` |
| mac | `/Users/UserName/mybox/MyBox_vVERVION.ini` |        

Add parameter "config=\"FilePath\"" when run jar to change configuration file temporarily.        
Function "Settings" can be used to change configuration values.        

## Migration
1. Each version has itself configuration file. New version can copy parameters from existed versions.             
2. Data handled in each version are under "Data Directory" referred by it. Multiple versions can refer to same data directory.
3. MyBox is backward compatibility: Later version can work on data of previous versions.
While forward compatibility is not supported: Wrong may happen when old version handles data of new version.          

## Backup and Recover         
In MyBox:         
1. To backup, copy directories to other places.       
2. To recover, override current directories with backup directories.         
3. Need not backup each directory under data path.           
   Predefined directories under data path are listed below:                  

|    directory    |         role         | internal referred | read/write automatically | need backup          | comments              |
|-----------------|----------------------|-------------------|--------------------------|----------------------|-----------------------|
| AppTemp         | temporary files      | yes               | yes                      |                      | cleared automatically |
| buttons         | customized buttons   | yes               | yes                      |                      |                       |
| data            | internal data        | yes               | yes                      |                      |                       |
| dataClipboard   | data clipboard       | yes               | yes                      | yes                  |                       |
| managed/matrix  | matrix               | yes               | yes                      | yes                  |                       |
| doc             | internal documents   | yes               | yes                      |                      |                       |
| downloads       | download files       |                   | yes                      |                      |                       |
| fileBackups     | files' backups       | yes               | yes                      | yes                  |                       |
| generated       | generated files      |                   | yes                      | user decides                     |        |
| ICC             | ICC profiles         | yes               | yes                      |                      |                       |
| icons           | icon files           | yes               | yes                      |                      |                       |
| image           | image files          | yes               | yes                      |                      |                       |
| imageClipboard  | image clipbooard     | yes               | yes                      | yes                  |                       |
| imageHistories  | image edit histories | yes               | yes                      | yes                  |                       |
| imageScopes     | image scopes         | yes               | yes                      | yes                  |                       |
| js              | javascript           | yes               | yes                      |                      |                       |
| logs            | database logs        |                   | yes                      |                      |                       |
| map             | map files            | yes               | yes                      |                      |                       |
| mybox_derby     | database             | yes               | yes                      | yes                  |                       |
| mybox_languages | customized languages | yes               | yes                      | yes                  |                       |
| security        | cert files           | yes               | yes                      |                      |                       |
| sound           | sound files          | yes               | yes                      |                      |                       |     

# Resource Addresses        
| Contents | Link |        
| --- | --- |
| Project Main Page | [https://github.com/Mararsh/MyBox](https://github.com/Mararsh/MyBox)   |
| Source Codes and Compiled Packages |  [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  |
| Submit Software Requirements and Problem Reports | [https://github.com/Mararsh/MyBox/issues](https://github.com/Mararsh/MyBox/issues) |
| Data | [https://github.com/Mararsh/MyBox_data](https://github.com/Mararsh/MyBox_data) |
| Documents | [https://github.com/Mararsh/MyBoxDoc](https://github.com/Mararsh/MyBoxDoc) |
| Mirror Site | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/) |
| Cloud Storage | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F) |       
   
# Documents        
|              Name              | Version |   Time     |                                                                                                                                            English                                                                                                                                            |                                                                                                                                            Chinese                                                                                                                                            |
|--------------------------------|---------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Development Logs               | 6.9.2   | 2025-12-29  | [html](https://mara-mybox.sourceforge.io/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_devLogs_zh.html)                                                                                                                                                                                                                                    |
| Shortcuts and Icons            | 6.8.5   | 2025-3-5   | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts_zh.html)                                                                                                                                                                                                                                  |
| Functions List                 | 6.9.2   | 2025-12-29 | [html](https://mara-mybox.sourceforge.io/mybox_functions_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_functions_zh.html)                                                                                                                                                                                                                                  |
| Packing Steps                  | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mara-mybox.sourceforge.io/pack_steps.html)                                                                                                                                                                                                                                       |
| Development Guide              | 2.1     | 2020-8-27  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.odt)                                                                                                                                                                                  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.pdf)  [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.odt)                                                                                                                                                                                                                |
| User Guide - Overview          | 6.9.2   | 2025-12-29  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-Overview-en/MyBox-Overview-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-Overview-zh/MyBox-Overview-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.odt)                     |
| User Guide - Data Tools        | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DataTools-en/MyBox-DataTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DataTools-zh/MyBox-DataTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.odt)                 |
| User Guide - Document Tools    | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DocumentTools-en/MyBox-DocumentTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.odt) | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DocumentTools-zh/MyBox-DocumentTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.odt) |
| User Guide - Image Tools       | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-ImageTools-en/MyBox-ImageTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-ImageTools-zh/MyBox-ImageTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.odt)             |
| User Guide - File Tools        | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-FileTools-en/MyBox-FileTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-FileTools-zh/MyBox-FileTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.odt)                 |
| User Guide - Network Tools     | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-NetworkTools-en/MyBox-NetworkTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.odt)     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-NetworkTools-zh/MyBox-NetworkTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.odt)     |
| User Guide - Media Tools       | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-MediaTools-en/MyBox-MediaTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-MediaTools-zh/MyBox-MediaTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.odt)             |
| User Guide - Development Tools | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DevTools-en/MyBox-DevTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DevTools-zh/MyBox-DevTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.odt)                     |
| Software Testing - Test Environment                   | 6.8.7   | 2025-5-24  | [html](https://mara-mybox.sourceforge.io/mybox_TestEnvironment_en.html)                                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_TestEnvironment_zh.html)                                                                                                                                                                                                                                    |
| Software Testing - Base Functions' Verification List  | 6.8.7   | 2025-5-24  | [html](https://mara-mybox.sourceforge.io/mybox_BaseVerificationList_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_BaseVerificationList_zh.html)                                                                                                                                                                                                                                  |
| Software Testing - Compatibility Testing              | 6.8.7   | 2025-5-24  | [html](https://mara-mybox.sourceforge.io/mybox_CompatibilityTesting_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_CompatibilityTesting_zh.html)                                                                                                                                                                                                                                  |
| Software Testing - Detailed Testing                   | 6.8.7   | 2025-5-24  | [html](https://mara-mybox.sourceforge.io/mybox_DetailedTesting_en.html)                                                                                                                                                                                                                                    | [html](https://mara-mybox.sourceforge.io/mybox_DetailedTesting_zh.html)                                                                                                                                                                                                                                       |
| Tips in Interfaces             | 6.8.5   | 2025-3-5    | [html](https://mara-mybox.sourceforge.io/mybox_interface_tips_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_interface_tips_zh.html)                                                                                                                                                                                                                                    |
| About - Tree Information       | 6.8.2   | 2024-12-24   | [html](https://mara-mybox.sourceforge.io/mybox_about_tree_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_tree_zh.html)                                                                                                                                                                                                                                    |
| About - Data in Two-dimensional Storage Structure | 6.8.6   | 2025-3-30     | [html](https://mara-mybox.sourceforge.io/mybox_about_data2d_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_data2d_zh.html)                                                                                                                                                                                                                                    |
| About - Row Expression and Row Filter             | 6.8.2   | 2024-12-24    | [html](https://mara-mybox.sourceforge.io/mybox_about_row_expression_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_row_expression_zh.html)                                                                                                                                                                                                                                    |
| About - Data Grouping          | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping_zh.html)                                                                                                                                                                                                                                    |
| About - Image Scope            | 6.8.5   | 2025-3-5   | [html](https://mara-mybox.sourceforge.io/mybox_about_image_scope_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_image_scope_zh.html)                                                                                                                                                                                                                                    |
| About - Macro Commands    | 6.9.1   | 2025-10-1   | [html](https://mara-mybox.sourceforge.io/mybox_about_macro_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_macro_zh.html)                                                                                                                                                                                                                                    |
| About - Data Analysis          | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis_zh.html)                                                                                                                                                                                                                                    |              
| About - Coordinate System      | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system_en.html)                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system_zh.html)                                                                                                                                                                                                                                    |              
| About - Color                  | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_color_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_color_zh.html)                                                                                                                                                                                                                                    |              
| About - Media                  | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_media_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_media_zh.html)                                                                                                                                                                                                                                    |              
| About - Items in One Sentence About Java        | 6.7.3   | 2023-5-22 | [html](https://mara-mybox.sourceforge.io/mybox_about_java_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_java.html)                                                                                                                                                                                                                                    |
| About - Items in One Sentence About Javafx      | 6.7.3   | 2023-5-22 | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx.html)                                                                                                                                                                                                                                    |
| Examples - Data in Two-dimensional Storage Structure      | 6.9.2   | 2025-12-29 | [html](https://mara-mybox.sourceforge.io/mybox_examples_data2d_en.html)                                                                                                                                                                                                                          | [html](https://mara-mybox.sourceforge.io/mybox_examples_data2d_cn.html)                                                                                                                                                                                                                                    |
| Examples - Html Tree           | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Html_en.html)                                                                                                                                                                                                                          | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Html_zh.html)                                                                                                                                                                                                                                    |
| Examples - Text Tree           | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Text_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Text_zh.html)                                                                                                                                                                                                                                    |
| Examples - Favorite Address    | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Web_Favorite_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Web_Favorite_zh.html)                                                                                                                                                                                                                                    |
| Examples - SQL                 | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_SQL_en.html)                                                                                                                                                                                                                            | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_SQL_zh.html)                                                                                                                                                                                                                                    |
| Examples - JShell              | 6.8.5   | 2025-3-5   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JShell_en.html)                                                                                                                                                                                                                         | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JShell_zh.html)                                                                                                                                                                                                                                    |
| Examples - JEXL                | 6.8.5   | 2025-3-5   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JEXL_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JEXL_zh.html)                                                                                                                                                                                                                                    |
| Examples - JavaScript          | 6.8.5   | 2025-3-5   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JavaScript_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JavaScript_zh.html)                                                                                                                                                                                                                                    |              
| Examples - Math Function       | 6.8.5   | 2025-3-5   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Math_Function_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Math_Function_zh.html)                                                                                                                                                                                                                                    |              
| Examples - Row Expression      | 6.8.6   | 2025-3-30  | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Row_Expression_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Row_Expression_zh.html)                                                                                                                                                                                                                                    |              
| Examples - Data Column         | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Data_Column_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Data_Column_zh.html)                                                                                                                                                                                                                                    |
| Examples - Geography Code      | 6.8.3   | 2025-1-27  | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Geography_Code_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Geography_Code_zh.html)                                                                                                                                                                                                                                    |
| Examples - Image Scope         | 6.8.6   | 2025-3-30  | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Image_Scope_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Image_Scope_zh.html)                                                                                                                                                                                                                                    |
| Examples - Macro Commands   | 6.9.1   | 2025-10-2  | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Macro_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Macro_zh.html)                                                                                                                                                                                                                                    |
| Palette - Default Colors                        | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_default_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_default_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_default_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_default_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Common Web Colors                     | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Chinese Traditional Colors            | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all_en.html)                                                                                                                          | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese_rgba_zh.html) [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all_zh.html)                                                                                                                                                                                                                                    |
| Palette - Japanese Traditional Colors           | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all_en.html)                                                                                                                        | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all_zh.html)                                                                                                                                                                                                                                  |
| Palette - Colors from colorhexa.com             | 6.8.3   | 2025-1-27 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all_en.html)                                                                                                                      | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Art Hues Wheel(RYB) - 12 Colors       | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Art Hues Wheel(RYB) - 24 Colors       | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Art Hues Wheel(RYB) - 360 Colors      | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Optical Hues Wheel(RGB) - 12 Colors   | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Optical Hues Wheel(RGB) - 24 Colors   | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Optical Hues Wheel(RGB) - 360 Colors  | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Art Paints                            | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - MyBox Colors                          | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all_en.html)                                                                                                                              | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all_zh.html)                                                                                                                                                                                                                                   |
| Palette - Gray Scale                            | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all_zh.html)                                                                                                                                                                                                                                   |
| Stories of Images                               | 6.8.5   | 2025-3-5 | [html](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-zh.odt)                     |   
| Useful Links                   | 6.8.6   | 2025-3-30 | [html](https://mara-mybox.sourceforge.io/mybox_useful_link_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_useful_link_zh.html)                                                                                                                                                                                                                                    |


# Implementation        
MyBox is based on following open sources:        

| Name | Role | Link |
| --- | --- | --- |
| JDK                                 | Java                            | <http://jdk.java.net/>                                                                                                                                                                                                              |
|                                     |                                 | <https://www.oracle.com/technetwork/java/javase/downloads/index.html>                                                                                                                                                               |
|                                     |                                 | <https://docs.oracle.com/en/java/javase/25/docs/api/index.html>                                                                                                                                                                     |
| JavaFx                              | GUI                             | <https://gluonhq.com/products/javafx/>                                                                                                                                                                                              |
|                                     |                                 | <https://docs.oracle.com/javafx/2/>                                                                                                                                                                                                 |
|                                     |                                 | <https://gluonhq.com/products/scene-builder/>                                                                                                                                                                                       |
|                                     |                                 | <https://openjfx.io/javadoc/24/>                                                                                                                                                                                                    |
| Derby                               | Database                        | <http://db.apache.org/derby/>                                                                                                                                                                                                       |
| NetBeans                            | IDE                             | <https://netbeans.org/>                                                                                                                                                                                                             |
| jpackage                            | pack                            | <https://docs.oracle.com/en/java/javase/25/docs/specs/man/jpackage.html>                                                                                                                                                            |
| maven                               | build                           | <https://maven.apache.org/>                                                                                                                                                                                                         |
| jai-imageio                         | Image manufacture               | <https://github.com/jai-imageio/jai-imageio-core>                                                                                                                                                                                   |
| PDFBox                              | PDF manufacture                 | <https://pdfbox.apache.org/>                                                                                                                                                                                                        |
| PDF2DOM                             | PDF to html                     | <http://cssbox.sourceforge.net/pdf2dom/>                                                                                                                                                                                            |
| GifDecoder                          | Decode broken gif               | <https://github.com/DhyanB/Open-Imaging/>                                                                                                                                                                                           |
| EncodingDetect                      | Determine encoding of text file | <https://www.cnblogs.com/ChurchYim/p/8427373.html>                                                                                                                                                                                  |
| Lindbloom                           | Color theory                    | <http://brucelindbloom.com/index.html>                                                                                                                                                                                                 |
| Free Icons                          | Icon                            | <https://icons8.com/icons/set/home>                                                                                                                                                                                              |
| tess4j                              | OCR                             | <https://github.com/nguyenq/tess4j>                                                                                                                                                                                                 |
| tesseract                           | OCR                             | <https://github.com/tesseract-ocr/tesseract>                                                                                                                                                                                        |
| barcode4j                           | Create barcodes                 | [http://barcode4j.sourceforge.net](http://barcode4j.sourceforge.net/)                                                                                                                                                               |
| zxing                               | Create/Decode barcodes          | <https://github.com/zxing/zxing>                                                                                                                                                                                                    |
| flexmark-java                       | Convert Markdown                | <https://github.com/vsch/flexmark-java>                                                                                                                                                                                             |
| commons-compress                    | archive/compress                | <https://commons.apache.org/proper/commons-compress>                                                                                                                                                                                |
| XZ for Java                         | archive/compress                | <https://tukaani.org/xz/java.html>                                                                                                                                                                                                  |
| ffmpeg                              | convert/create medias           | [http://ffmpeg.org](http://ffmpeg.org/)                                                                                                                                                                                             |
| image4j                             | ico format                      | <https://github.com/imcdonagh/image4j>                                                                                                                                                                                              |
| AutoCommitCell                      | Submit updates                  | <https://stackoverflow.com/questions/24694616>[(Ogmios](https://stackoverflow.com/questions/24694616)[)](https://stackoverflow.com/questions/24694616)                                                                              |
| GaoDe                               | Map                             | <https://lbs.amap.com/api/javascript-api/summary>                                                                                                                                                                                   |
| GaoDe                               | Coordinate                      | <https://lbs.amap.com/api/webservice/guide/api/georegeo>                                                                                                                                                                            |
| WeiBo                               | Image materials                 | <https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665>                                                                                                                                                             |
| poi                                 | Microsoft Documents             | [https://poi.apache.org](https://poi.apache.org/)                                                                                                                                                                                   |
| LabeledBarChart                     | Javafx charts                   | [https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland)](https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789) |
| commons-csv                         | CSV                             | <https://commons.apache.org/proper/commons-csv/>                                                                                                                                                                                    |
| geonames                            | Location data                   | <https://www.geonames.org/countries/>                                                                                                                                                                                               |
| world-area                          | Location data                   | <https://github.com/wizardcode/world-area>                                                                                                                                                                                          |
| China National Bureau of Statistics | Data                            | <https://data.stats.gov.cn/>                                                                                                                                                                                 |
| JHU                                 | COVID-19 data                   | <https://github.com/CSSEGISandData/COVID-19>                                                                                                                                                                                        |
| Website                             | Color data                      | <https://tool.lanrentuku.com/color/china.html>                                                                                                                                                                                      |
| Book                                | Materials                       | ISBN:978-7-5330-2773-5                                                                                                                                                                                          |
| National Geomatics Center of China  | Map                             | <http://lbs.tianditu.gov.cn/api/js4.0/guide.html>                                                                                                                                                                                   |
| movebank                            | Location data                   | [https://www.datarepository.movebank.org](https://www.datarepository.movebank.org/)                                                                                                                                                 |
| CoordinateConverter                 | convert coordinates             | <https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage>                                                                                                                                                                         |
| JavaMail                            | email                           | <https://javaee.github.io/javamail/>                                                                                                                                                                                                |
| Commons IO                          | File IO                         | <https://commons.apache.org/proper/commons-io/>                                                                                                                                                                                     |
| colorhexa                           | Color data                      | <https://www.colorhexa.com/color-names>                                                                                                                                                                                             |
| WQY                                 | Font file                       | <http://wenq.org/wqy2/>                                                                                                                                                                                                             |
| ttc2ttf                             | Extract ttf                     | <https://github.com/fermi1981/TTC_TTF>                                                                                                                                                                                              |
| Book                                | Materials                       | ISBN:978-7-03-0307290-0                                                                                                                                                                                        |
| PaginatedPdfTable                   | PDF                             | <https://github.com/eduardohl/Paginated-PDFBox-Table-Sample>                                                                                                                                                                        |
| jsoup                               | html                            | <https://jsoup.org/>                                                                                                                                                                                                                |
| ZhiHu                               | Materials                       | <https://www.zhihu.com/question/41580677/answer/1300242801>                                                                                                                                                                         |
| commons-math                        | Calculation                     | <https://commons.apache.org/proper/commons-math/index.html>                                                                                                                                                                         |
| JEXL                                | Calculation                     | <https://commons.apache.org/proper/commons-jexl>                                                                                                                                                                                    |
| OpenOffice                          | Document                        | <http://www.openoffice.org/>                                                                                                                                                                                                        |
| nashorn                             | JavaScript                      | <https://openjdk.org/projects/nashorn/>                                                                                                                                                                                             |
| echarts-gl                          | WebGL                           | <https://github.com/ecomfe/echarts-gl>                                                                                                                                                                                              |
| RYB hues                            | art colors                      | <https://blog.csdn.net/weixin_44938037/article/details/90599711>                                                                                                                                                                    |
| jsch                                | sftp                            | <http://www.jcraft.com/jsch/>                                                                                                                                                                                                       |
| jackson                             | json                            | <https://github.com/FasterXML/jackson>              
| batik                               | SVG                             | <https://xmlgraphics.apache.org/batik/>              
| jankovicsandras                     | SVG                             | <https://github.com/jankovicsandras/imagetracerjava>    |
| miguelemosreverte                   | SVG                             | <https://github.com/miguelemosreverte/imagetracerjava>    |
| webp-imageio | WebP | <https://github.com/sejda-pdf/webp-imageio>    |
| Book | Materials | ISBN:7-5305-231 7-1    |
| DeepSeek | Knowledge | <https://www.deepseek.com/>     |
| LibreOffice                         | Document                        | <https://www.libreoffice.org/>                                                                                                                                                                                                        |
| doubao                         | Knowledge                        | <https://www.doubao.com/>                                                                                                                                                                                                        |
| Chinese characters | data | <https://github.com/iDvel/The-Table-of-General-Standard-Chinese-Characters>    |   
| Chinese Pinyin | data | <https://github.com/amorphobia/opencc-tonggui>    |   
| Chinese strokes | data | <https://github.com/hamidun123/chinese-stroke>    |   
| Chinese codes | data | <https://github.com/Hansha2011/TongYongGuiFanCharacterTable>    |   
| Chinese words | data | <https://github.com/amorphobia/opencc-tonggui>    |   
| Chinese statistic | data | <https://github.com/zispace/hanzi-freq>    |   
|  |  | <http://bcc.blcu.edu.cn>    |   
|  |  | <https://github.com/chinese-poetry/chinese-poetry>    |   
|  |  | <https://github.com/forfudan/chinese-characters-frequency>    |   
|  |  | <http://yong321.freeshell.org/misc/ChineseCharFrequency.html>    |   
|  |  | <https://github.com/mahavivo/scripta-sinica>    |   
|  |  | <https://forum.freemdict.com/t/topic/3748>    |   
|  |  | <https://github.com/studyzy/imewlconverter>    |   


# Features        
## Cross Platforms        

MyBox is implemented in pure Java and only based on open sources, and it can run on platforms which support Java 25.        
Versions before v5.3 are based on Java 8.        

## Internationalized       

1. All codes of MyBox are internationalized. Language can be switched in time.
2. Each language is defined in one resource file, like: "Messages_NAME.properties".
3. Support adding new languages online. Table is provided with English as comparison. New languages take effects at once.        
Example,  the new language is named as "aa", and its resource file is "Messages_aa.properties".        
4. Translated languages can be shared with others: Put the files under "mybox_languages" of data path and MyBox is aware of them immediately.        
5. Embed Chinese and English, whose files are under path `MyBox/src/main/resources/bundles/`.        

| Language | Resource file of interface |
| --- | --- | 
| Chinese | Messages_zh_CN.properties | 
| English | Messages_en.properties |   


## Personal       

1. All functions are executed in local host.        
2. All data are stored in local host.    
3. No register/login/DataCenter/Cloud.     
4. Access network only when user requests.    
5. Only read/write data which user need.       


## Friendly       

1. Provide tips of current function/interface.       
2. Remember user's inputs or selections, and provide histories list.
3. Interfaces can be adjusted and some panes can be hidden.
4. Behaviors of some controls can be chosen. 
5. Provide context menu and function menu to texts/image/html/table/tree.
6. Current texts/image/html/data can be popped.
7. Clipboards of texts/image/data can be managed.
8. Provide examples and useful information. 
9. Appearance of interface and controls can be chosen and modified.    
       

## Data Compatible    

1. Exported data are in common text formats, like txt/csv/xml/json/html.
2. Imported data are in common text format, like xml/csv.       
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
4. MyBox internal tables store the data required by all functions. User can access and modify the internal data online.            
5. User can define customized database tables.            
6. User can view and edit definitions and data of database tables in paginated tables, and execute SQL statements.                   
7. To implement data sorting, transposing, statistic, and grouping, MyBox will create temporary database tables, and clear them in time.            

## Assist Coding Self            

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


    
