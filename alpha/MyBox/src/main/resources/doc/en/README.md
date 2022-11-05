# [中文ReadMe](https://github.com/Mararsh/MyBox)   ![ReadMe](https://mararsh.github.io/MyBox/iconGo.png)

# MyBox: Set of Easy Tools
This is desktop application based on JavaFx to provide simple and easy functions. It's free and open sources.

## What's New          
2022-11-6 v6.6.1          
* Add:                  
     - Formats of data columns. Example, following formats can be selected for numbers: group in thousands, group in ten thousands, scientific notation, and no format.                       
     - New types of data columns: Era, Color, Longitude, Latitude, Enumeration.          
     - When edit data, specific controls are shown according to types and formats of columns:          
        - To boolean, checkbox is provided.          
        - To enumeration, list view is provided with selections.          
        - To color, palette is provided.          
        - To longitude and latitude, map can be popped to locate coordinate.                        
     - When data contain columns of longitude and latitude, "Location Distibution" can be generated.                         
     - Charts of groups. With grouped data, dynamic charts of XY/Pie/ComparisonBars/SelfComparisonBars/BoxWhisker can be generated in sequence of groups.                           
* Enhance:                      
     - Group data as following 4 ways:          
        - Equal values. Select sevaral columns and put data into same group when they have same values of these columns.                         
        - Value range. Select one column and divide data by value ranges of this column. Following 3 selections can be for the split:           
            - Size of split(interval of values)          
            - Number of split          
            - List of "StartValue-EndValue"          
        - Conditions. Define some row filters and data rows are divided in groups by these row filters.          
        - Row number. Divide data rows by their row numbers. Following 3 selections can be for the split:                    
            - Size of split(interval of row numbers)          
            - Number of split          
            - List of "StartNumber-EndNumber"          
     - Assist mode of data editing is changed as CSV, to support values which contain delimeter or line break.                                                
     - When read data in System Clipboard, both CSV parser and Text parser can be used.                                                
     - Row filters can be saved in tree.                                                
     - All types of charts can set sort conditions and maximum number of picked data.                                                
     - Parse data format with more commonly algorithm.                         
     - Parse numbers with NumberFormat.                                               
     - Player:           
        - Implemented with ScheduledExecutorService. Support both "deplay" mode and "interval" mode.                                                
        - Snapshots                                               
* Remove functions "Location Data" and "Edpidemic Reports".          
  Their existed data can be found under menu "Data - Database - Database Table" and can be changed and used as common data.                               
* Solved:           
     - Row expressions have no way to describe "value is null".                        
     - When value of category is null, charts may pop errors.                        
     - Comparison bars chart picks wrong values for one column.                        
     - Sort for current page may cause error.                        
     - When save new data which have applied styles, errors will pop.                       
     - When copy nodes in tree, errors may pop.                              
                

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.6.1)                    

## Download and Execution
Packages of each version have been uploaded at [Releases](https://github.com/Mararsh/MyBox/releases?) directory now. 
You can find them by clicking `releases` tab in main page of this project.        


### Source Codes
[MyBox-6.6.1-src.zip](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-src.zip)   52MB-        

About structure, editing, and building of source codes, please refer to [Developement Guide](https://sourceforge.net/projects/mara-mybox/files/documents/MyBox-DevGuide-2.1-en.pdf) and
[Packing Steps](https://mararsh.github.io/MyBox/pack_steps_en.html)        


### Self-contain packages
Self-contain packages include all files and need not java env nor installation.      

| Platform | Link | Size  | Launcher |        
| --- | --- | ---  | ---  |
| win10 x64 | [MyBox-6.6.1-win-x64.zip](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-win10-x64.zip)  | 240MB- | MyBox.exe |
| CentOS 7 x64 | [MyBox-6.6.1-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-CentOS7-x64.tar.gz)  | 260MB-  | bin/MyBox  |
| mac | [MyBox-6.6.1-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-mac.dmg)  | 260MB-  |  MyBox-6.6.1.app   |        

User can double click the launcher to start MyBox or run it by command line. The default "Open Method" of image/text/PDF files can be associated to MyBox and a file can be opened directly by MyBox by double clicking the file's name.        

### Jar
When JRE or JDK([Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or [open jdk](http://jdk.java.net/)) is installed, jar can run:        
   
| Platform | Link | Size  | Requirements |        
| --- | --- | ---  | ---  |
| win | [MyBox-6.6.1-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-win-jar.zip)  | 170MB- | Java 18 or higher |
| linux | [MyBox-6.6.1-linux-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-linux-jar.zip)  | 190MB-  | Java 18 or higher |
| mac | [MyBox-6.6.1-mac-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.6.1/MyBox-6.6.1-mac-jar.zip)  |  180MB-  | Java 18 or higher |        


Run following command to launch this program with Jar package:        
<PRE><CODE>     java   -jar   MyBox-6.6.1.jar</CODE></PRE>        

A file path can follow the command as argument to be opened directly by MyBox. Example, following command will open the image:        
<PRE><CODE>     java   -jar   MyBox-6.6.1.jar   /tmp/a1.jpg</CODE></PRE>        

### Other addresses to download
Download from cloud storage: [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)               
Download from sourceforge: [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)             

## Limitation        
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
| win | `C:\users\UserName\mybox\MyBox_v6.6.1.ini`  |
| linux | `/home/UserName/mybox/MyBox_v6.6.1.ini` |
| mac | `/Users/UserName/mybox/MyBox_v6.6.1.ini` |        

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
|              Name              | Version |   Time    |                                                                                                                                            English                                                                                                                                            |                                                                                                                                            Chinese                                                                                                                                            |
|--------------------------------|---------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Development Logs               | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBox/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mararsh.github.io/MyBox/mybox_devLogs.html)                                                                                                                                                                                                                                    |
| Shortcuts                      | 6.5.6   | 2022-6-11 | [html](https://mararsh.github.io/MyBox/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mararsh.github.io/MyBox/mybox_shortcuts.html)                                                                                                                                                                                                                                  |
| Packing Steps                  | 6.3.3   | 2020-9-27 | [html](https://mararsh.github.io/MyBox/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mararsh.github.io/MyBox/pack_steps.html)                                                                                                                                                                                                                                       |
| Development Guide              | 2.1     | 2020-8-27 | [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-DevGuide-2.1-en.pdf)                                                                                                                                                                                                                 | [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-DevGuide-2.1-zh.pdf)                                                                                                                                                                                                                 |
| User Guide - Overview          | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-Overview-en/MyBox-6.6.1-Overview-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-Overview-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-Overview-en.odt)                     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-Overview-zh/MyBox-6.6.1-Overview-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-Overview-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-Overview-zh.odt)                     |
| User Guide - Document Tools    | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DocumentTools-en/MyBox-6.6.1-DocumentTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DocumentTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DocumentTools-en.odt) | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DocumentTools-zh/MyBox-6.6.1-DocumentTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DocumentTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DocumentTools-zh.odt) |
| User Guide - Image Tools       | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-ImageTools-en/MyBox-6.6.1-ImageTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-ImageTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-ImageTools-en.odt)             | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-ImageTools-zh/MyBox-6.6.1-ImageTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-ImageTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-ImageTools-zh.odt)             |
| User Guide - File Tools        | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-FileTools-en/MyBox-6.6.1-FileTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-FileTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-FileTools-en.odt)                 | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-FileTools-zh/MyBox-6.6.1-FileTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-FileTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-FileTools-zh.odt)                 |
| User Guide - Network Tools     | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-NetworkTools-en/MyBox-6.6.1-NetworkTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-NetworkTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-NetworkTools-en.odt)     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-NetworkTools-zh/MyBox-6.6.1-NetworkTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-NetworkTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-NetworkTools-zh.odt)     |
| User Guide - Data Tools        | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DataTools-en/MyBox-6.6.1-DataTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DataTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DataTools-en.odt)                 | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DataTools-zh/MyBox-6.6.1-DataTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DataTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DataTools-zh.odt)                 |
| User Guide - Media Tools       | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-MediaTools-en/MyBox-6.6.1-MediaTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-MediaTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-MediaTools-en.odt)             | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-MediaTools-zh/MyBox-6.6.1-MediaTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-MediaTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-MediaTools-zh.odt)             |
| User Guide - Development Tools | 6.6.1   | 2022-9-28 | [html](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DevTools-en/MyBox-6.6.1-DevTools-en.html) [PDF](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DevTools-en.pdf) [odt](https://mararsh.github.io/MyBoxDoc/en/MyBox-6.6.1-DevTools-en.odt)                     | [html](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DevTools-zh/MyBox-6.6.1-DevTools-zh.html) [PDF](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DevTools-zh.pdf) [odt](https://mararsh.github.io/MyBoxDoc/zh/MyBox-6.6.1-DevTools-zh.odt)                     |


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
| NetBeans | IDE| https://netbeans.org/ |
| jpackage | pack | https://docs.oracle.com/en/java/javase/18/docs/specs/man/jpackage.html |
| maven | build | https://maven.apache.org/ |
| jai-imageio | Image manufacture | https://github.com/jai-imageio/jai-imageio-core |
| PDFBox | PDF manufacture | https://pdfbox.apache.org/ |
| PDF2DOM | PDF to html | http://cssbox.sourceforge.net/pdf2dom/ |
| javazoom | MP3 manufacture | https://sourceforge.net/projects/javalayer/ |
| Derby | Database | http://db.apache.org/derby/ |
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
| jaffree | wrap ffmpeg | https://github.com/kokorin/Jaffree |
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
| WeiBo | Materials | https://weibo.com/2328516855/LhFIHy26O |
| ZhiHu | Materials | https://www.zhihu.com/question/41580677/answer/1300242801 |             
| commons-math | Calculation | https://commons.apache.org/proper/commons-math/index.html |
| JEXL | Calculation | https://commons.apache.org/proper/commons-jexl |
| OpenOffice | Document | [http://www.openoffice.org/](http://www.openoffice.org/) |
| nashorn | JavaScript | [https://openjdk.org/projects/nashorn/](https://openjdk.org/projects/nashorn/) |
| echarts-gl | WebGL | [https://github.com/ecomfe/echarts-gl](https://github.com/ecomfe/echarts-gl) |

# Features        
## Cross Platforms <a id="Cross-platform"></a>        

MyBox is implemented in pure Java and only based on open sources, and it can run on platforms which support Java 18.        
Versions before v5.3 are based on Java 8.        

## Internationalized <a id="Internationalized"></a>        
1. All codes of MyBox are internationalized. Language can be switched in time.
2. Each langauge is defined in two resource files, like: "Messages_NAME.properties" and "TableMessages_NAME.properties"。
3. Support adding new languages online. Table is provided with English as comparison. New langauges take effects at once.        
Example,  the new language is named as “aa”, and its resource files are Messages_aa.properties and TableMessages_aa.properties.        
4. Translated languages can be shared with others: Put the files under "mybox_languages" of data path and MyBox is aware of them immediately.        
5. Embed Chinese and English, whose files are under path `MyBox/src/main/resources/bundles/`.        

| Language | Resource file of interface | Resource file of data tables |
| --- | --- |  --- |
| Chinese | Messages_zh_CN.properties | TableMessages_zh_CN.properties |
| English | Messages_en.properties | TableMessages_en.properties |        


## Personal<a id="personal" />
1. No register/login/DataCenter/Cloud.
1. No network if unnecessary.
2. Not read/write if unnecessary.       

## Data Compatible<a id="dataCompatible" />
1. Exported data are in common text formats, like txt/csv/xml/json/html.
2. Imported data are in common text format, like txt/csv。       
3. At least one exported format can be imported. 
4. Imported data are self-contain, that original data can be rebuilt without extra data.       

![Snap-cover](https://mararsh.github.io/MyBox/snap-cover-en.jpg)       

![Snap-interface](https://mararsh.github.io/MyBox/snap-interface-en.jpg)  

    