# [中文ReadMe](https://github.com/Mararsh/MyBox)   ![ReadMe](https://mararsh.github.io/MyBox/iconGo.png)

# MyBox: Set of Easy Tools
This is desktop application based on JavaFx to provide simple and easy functions. It's free and open sources.


## What's New        
2022-4-3 v6.5.4         

* Information in tree. Manage nodes of tree. Edit nodes. Export/Import with tags. Output tree view. Examples. 
Extend as: Notes, Web favoraite addresses,  JShell codes, Javascript codes, SQL codes.                
* Improve. Execute multiple statements in JShell. Manage and execute queries for data tables. 
"When left click link or image" of web page has more options. Pop histories for input controls.                      
* Solved. Options of "Word" and "html" for Image OCR in batch do not work. Error popped when save new html in editor.
Nothing returned when query time in tree. Wrong may happen when delimited identify is defined in data table. "Auto save" not work in File Editor.       

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.4)             
                   

## Download and Execution
Packages of each version have been uploaded at [Releases](https://github.com/Mararsh/MyBox/releases?) directory now. 
You can find them by clicking `releases` tab in main page of this project.        


### Source Codes
[MyBox-6.5.4-src.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-src.zip)   40M(approximation)        

About structure, editing, and building of source codes, please refer to [Developement Guide](https://sourceforge.net/projects/mara-mybox/files/documents/MyBox-DevGuide-2.1-en.pdf) and
[Packing Steps](https://mararsh.github.io/MyBox/pack_steps_en.html)        


### Self-contain packages
Self-contain packages include all files and need not java env nor installation.      

| Platform | Link | Size(approximation)  | Launcher |        
| --- | --- | ---  | ---  |
| win10 x64 | [MyBox-6.5.4-win-x64.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-win10-x64.zip)  | 219MB | MyBox.exe |
| CentOS 7 x64 | [MyBox-6.5.4-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-CentOS7-x64.tar.gz)  | 252MB  | bin/MyBox  |
| mac | [MyBox-6.5.4-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-mac.dmg)  | 232MB  |  MyBox-6.5.4.app   |        

User can double click the launcher to start MyBox or run it by command line. The default "Open Method" of image/text/PDF files can be associated to MyBox and a file can be opened directly by MyBox by double clicking the file's name.        

### Jar
When JRE or JDK([Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or [open jdk](http://jdk.java.net/)) is installed, jar can run:        
In my Mac env(OS X 10.11.6/Darwin 15.6.0), openJDK 17 fails to start, so MyBox on mac is based on openJDK 16.         


| Platform | Link | Size(approximation)  | Requirements |        
| --- | --- | ---  | ---  |
| win | [MyBox-6.5.4-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-win-jar.zip)  | 156MB | Java 17 or higher |
| linux | [MyBox-6.5.4-linux-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-linux-jar.zip)  | 186MB  | Java 17 or higher |
| mac | [MyBox-6.5.4-mac-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.4/MyBox-6.5.4-mac-jar.zip)  |  159MB  | Java 16 |        


Run following command to launch this program with Jar package:        
<PRE><CODE>     java   -jar   MyBox-6.5.4.jar</CODE></PRE>        

A file path can follow the command as argument to be opened directly by MyBox. Example, following command will open the image:        
<PRE><CODE>     java   -jar   MyBox-6.5.4.jar   /tmp/a1.jpg</CODE></PRE>        

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
| win | `C:\users\UserName\mybox\MyBox_v6.5.4.ini`  |
| linux | `/home/UserName/mybox/MyBox_v6.5.4.ini` |
| mac | `/Users/UserName/mybox/MyBox_v6.5.4.ini` |        

Add parameter "config=\"FilePath\"" when run jar to change configuration file temporarily.        
Function "Settings" can be used to change configuration values.        

# Resource Addresses        
| Contents | Link |        
| --- | --- |
| Project Main Page | https://github.com/Mararsh/MyBox |
| Source Codes and Compiled Packages | https://github.com/Mararsh/MyBox/releases |
| Submit Software Requirements and Problem Reports | https://github.com/Mararsh/MyBox/issues |
| Data | https://github.com/Mararsh/MyBox_data |
| Documents | https://github.com/Mararsh/MyBox_documents |
| Mirror Site | https://sourceforge.net/projects/mara-mybox/files/ |
| Cloud Storage | https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F |        


# Documents        
| Name | Version | Time | Link |        
| --- | --- | --- |  --- |
| Development Logs | 6.5.4 | 2022-4-3 | [html](#devLog) |
| Shortcuts | 6.4.7 | 2021-8-17 | [html](https://mararsh.github.io/MyBox/mybox_shortcuts_en.html) |
| Packing Steps | 6.3.3 |  2020-9-27 | [html](https://mararsh.github.io/MyBox/pack_steps_en.html) |
| Development Guide | 2.1 | 2020-08-27 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-DevGuide-2.1-en.pdf) |
| User Guide - Overview | 5.0 | 2019-4-19 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-UserGuide-5.0-Overview-en.pdf) |
| User Guide - Image Tools | 5.0 | 2019-4-18 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-UserGuide-5.0-ImageTools-en.pdf) |
| User Guide - PDF Tools | 5.0 | 2019-4-20 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-UserGuide-5.0-PdfTools-en.pdf) |
| User Guide - Desktop Tools | 5.0 | 2019-4-16 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-UserGuide-5.0-DesktopTools-en.pdf) |
| User Guide - Network Tools | 5.0 | 2019-4-16 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-UserGuide-5.0-NetworkTools-en.pdf) |        


# Implementation        
MyBox is based on following open sources:        

| Name | Role | Link |
| --- | --- | --- |
| JDK | Java | http://jdk.java.net/   |
|   |   | https://www.oracle.com/technetwork/java/javase/downloads/index.html  |
|   |   | https://docs.oracle.com/en/java/javase/17/docs/api/index.html  |
|  JavaFx | GUI |  https://gluonhq.com/products/javafx/ |
|   |   |  https://docs.oracle.com/javafx/2/  |
|   |   |  https://gluonhq.com/products/scene-builder/  |
|   |   |  https://openjfx.io/javadoc/17/ |
| NetBeans | IDE| https://netbeans.org/ |
| jpackage | pack | https://docs.oracle.com/en/java/javase/16/docs/specs/man/jpackage.html |
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
| WeiBo | Materials | https://weibo.com/2328516855/LhFIHy26O#repost |
| ZhiHu | Materials | https://www.zhihu.com/question/41580677/answer/1300242801 |             

# Current Version        
Current version is 6.5.4. Its features are mentioned below in summary:          

*  [Cross Platforms](#Cross-platform)         
*  [Internationalized](#Internationalized)         
*  [Personal](#personal)
*  [Compatible Data](#dataCompatible)
*  [Document Tools](#documentTools)         
    -  [Information in Tree](#infoInTree)
    -  [Notes](#notes)         
    -  [PDF Tools](#pdfTools)         
    -  [Editor Base](#editTextBase)         
    -  [Markdown Tools](#markdownTools)         
    -  [Text Tools](#textTools)         
    -  [HtmlTools](#htmlTools)         
    -  [Microsoft Documents](#msDocuments)          
    -  [Edit Bytes](#editBytes)         
    -  [Text in MyBox Clipboard](#myboxTextClipboard)
    -  [Text in System Clipboard](#systemTextClipboard)
*  [Image Tools](#imageTools)         
    -  [View Image](#viewImage)         
    -  [Browse Images](#browserImage)         
    -  [Analyse Image](#analyseImage)       
    -  [Play Images](#playImages)  
    -  [Manufacture Image](#imageManufacture)      
    -  [Edit Images](#imagesList)
    -  [Merge Images](#multipleImages)         
    -  [Part Image](#imagePart)         
    -  [Part Image](#imagePart)         
    -  [Convert Image](#imageConvert)         
    -  [Recognize Texts in Image](#imageOCR)         
    -  [Color Management](#ColorManagement)         
    -  [Color Spaces](#colorSpaces)         
    -  [Images in MyBox Clipboard](#myboxImageClipboard)
    -  [Images in System Clipboard](#systemImageClipboard)
    -  [Others](#imageOthers)         
    -  [Big Image](#bigImage)         
*  [Data Tools](#dataTools)         
    -  [Manage Data](#manageData)
    -  [Edit Data](#editData)     
    -  [Data File](#dataFiles)         
    -  [Data Clipboard](#dataClipboard)   
    -  [Data in System Clipboard](#dataInSystemClipboard)
    -  [Data in MyBox Clipboard](#dataInMyBoxClipboard)      
    -  [Matrix](#matrix)         
    -  [Database Tables](#dataTables)
    -  [Database SQL](#dbSQL)
    -  [JShell(Java interactive coding tool)](#JShell)
    -  [Common Data Management](#dataManage)         
    -  [Map Data](#mapData)         
    -  [Geography Codes](#geographyCode)         
    -  [Location in Map](#locationInMap)         
    -  [Location Data](#locationData)         
    -  [Location Tools](#locationTools)         
    -  [Epidemic Reports](#epidemicReport)         
    -  [Others](#dataOthers)      
 *  [File Tools](#fileTools)         
    -  [Manage Files/Directories](#directoriesArrange)         
    -  [Archive/Compress/Decompress/Unarchive](#archiveCompress)         
    -  [Check Redundant Files](#filesRedundancy)         
    -  [Others](#fileOthers)         
*  [Media Tools](#MediaTools)         
    -  [Play Videos/Audios](#mediaPlayer)         
    -  [Manage Playlists](#mediaList)         
    -  [Wrap ffmpeg functions](#ffmpeg)         
    -  [Game-Elimination](#gameElimination)         
    -  [Game-Mine](#gameMine)         
    -  [Others](#mediaOthers)         
*  [Network Tools](#netTools)         
    -  [Web Browser](#webBrowser)         
    -  [Query Address](#queryAddress)         
    -  [Query DNS](#queryDNS)         
    -  [Encode/Decode URL](#encodeDecodeURL)         
    -  [Security Certificates](#securityCerificates)                  
    -  [Download Web Pages](#downloadFirstLevelLinks)         
    -  [Snap WeiBo](#weiboSnap)         
*  [Development Tools](#devTools)         
*  [Settings](#settings)         
*  [Window](#windows)         
*  [Helps](#helps)         
             

## Cross Platforms <a id="Cross-platform"></a>        

MyBox is implemented in pure Java and only based on open sources, and it can run on platforms which support Java 17.        
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


![Snap-Cover](https://mararsh.github.io/MyBox/snap-cover-en.jpg)        


## Personal<a id="personal" />
1. No register/login/DataCenter/Cloud.
1. No network if unnecessary.
2. Not read/write if unnecessary.       

## Data Compatible<a id="dataCompatible" />
1. Exported data are in common text formats, like txt/csv/xml/json/html.
2. Imported data are in common text format, like txt/csv。       
3. At least one exported format can be imported. 
4. Imported data are self-contain, that original data can be rebuilt without extra data.       


##  Document Tools <a id="documentTools"></a>             

### Information in Tree<a id="infoInTree" />        
1. Information is organized as a tree. 
2. Following operations can be done against any node in the tree: 
Add/Delete/Edit children, rename, move, copy, export, display tree view, unfold, and fold.
3. Edit node:
	-  Name should not include string " > "(Blank before and after ">").
	-  Each node can have multiple tags.
4. Export:       
	-  Formats: Text(for import), single html, html frameset, xml.
	-  Options to export time, or tags.       
	-  Charset can be set.
	-  Html style can be set.
5. Import. Example is provided. 
6. Query:
	-  Children or all descendants.
	-  By tags.
	-  By times.
	-  By strings in title/contents.       
 
![Snap-infoInTree](https://mararsh.github.io/MyBox/snap-infoInTree-en.jpg)


### Notes<a id="notes" />
1. Note is a piece of information in html format:
	-  Note can be edited in 4 modes: "html code", "Rich text", "Markdown", "Texts":
		-  Each edit mode can change the html separately. 
		-  The mode in current tab is "current edit mode".
		-  Click button "Synchronize"(F10) to apply updates in current edit mode to other modes.
		-  Click button "Save"(F2) to save updates in current edit mode and synchronize automatically.
		-  Note' html code should be contents of "body" and have not tags "html", "head" and "body".
	-  Style can be set for note. Style is only for displaying and not saved in note's codes.
2. Notebook is information tree of notes. 

![Snap-notes](https://mararsh.github.io/MyBox/snap-notes-en.jpg)        


### PDF Tools <a id="PDFTools"></a>
1. View PDF file:
	-  Bookmarks and thumbnails.
	-  Each page is converted as an image. DPI can be set to adjust resolution.  
	-  Extract text in page.
	-  Convert page as a html page.
	-  Recognize texts in PDF(OCR).
2. Convert PDF files in batch：
	-  Convert pages of PDF as image files. Options like format, density, color space, compression, quality, etc.
	-  Convert images in selected pages of PDF files, and save as new PDF files.
	-  Compress images in PDF files, and save as new PDF files. JPEG quality or threshold of black-white can be set.
	-  Convert PDF files as html files in batch. Options: Whether one html for each page or one html for each PDF; The way to handle fonts/images: embed, save separately, or ignore.
3. Extract data in batch:
	-  Extract images in PDF file.
	-  Extract texts in PDF file. Splitting line can be customized.
	-  Recognize texts(OCR) in images of PDF files.
4. Split a PDF file into multiple PDF files, by pages number, by files number, or by start-end list.
5. Merge multiple PDF files.
6. Combine multiple images as PDF file.
7. Options to write PDF like page size,  image attributes, font file, margin size, header, author, etc.
8. Modify PDF file's attributes like title, author, modify time, user password, owner password, user permissions, etc.        

![Snap-pdf](https://mararsh.github.io/MyBox/snap-pdf-en.jpg)        

### Editor Base<a id="editTextBase"></a>
1. General functions of editing, like copy/paste/cut/delete/selectAll/undo/redo/recover. And their shortcuts.
2. Find and replace:
	-  Options: Case-insensitive, Wrap around.
	-  Both Find String and Replace String can be multiple lines. Their line breaks are handled as file's definition.
	-  Support regular expression. Provide examples.
	-  Counting.
	-  Due to limitation of algorithm, to find regular expression in pages，assume maximum length of matched string is less than 1/16 of current JVM available memory.
3. Locate：
	-  Select character at specified position.
	-  Select line at specified position.
	-  Select lines of specified range.
	-  Select characters of specified range.
4. Filter lines:
	-  Conditions: Include/Not include One, Include/Not Include All, Include/Not Include Regular Expression, Match/Not Match Regular Expression.
	-  Cumulative filter.
	-  Filtered results can be saved. Select whether include lines number.
5. Paginate. Good at viewing or editing very large file, such as logs in size of several GBs.
	-  Set page size.
	-  Pages navigation bar
	-  Load and display first page, and scan the file in background to count characters number and lines number.
	   Part of functions are unavaliable while counting. Interface will be refreshed automatically after counting process is complete.
	-  Make sure correction of finding, replacing, and filtering of strings that are across pages.
6. Auto-save periodically.
7. Auto-backup when save.        

![Snap-textEditor](https://mararsh.github.io/MyBox/snap-textEditor-en.jpg)        

### Markdown Tools<a id="markdownTools" />         
1. Markdown Editor:
	-  Provide buttons to help inputting formats.     
	-  Display converted html and html codes synchronously：
2. Convert Markdown as html in batch.
3. Convert Markdown as texts in batch.
4. Convert Markdown as PDFs in batch.

![Snap-markdownEditor](https://mararsh.github.io/MyBox/snap-markdownEditor-en.jpg)        


### Text Tools<a id="textTools" />
1. Text Editor:
	-  File charset can be either detected automatically or set manually. Target file charset can be selected to implement encoding conversion. BOM setting is supported.
	-  Detect line break automatically. Convert line break. Show lines number.              
           Support LF(Unix/Linux), CR(Apple), and CRLF(Windows).   
	-  Hexadecimal codes according file's charset can be viewed, scrolled, and selected synchronously.              
2. Convert/Split text files in batch.
3. Merge text files.
4. Convert text as htmls/PDF in batch.
5. Replace strings in text files in batch.      

### Html Tools<a id="htmlTools" />       
1. Html Editor:    
	-  Html can be loaded by opening file, creating new file , or inputting address.
	-  Html can be edited in 4 modes which are "html code", "Rich text", "Markdown", "Texts":
		-  Each edit mode can change the html separately. 
		-  The mode in current tab is "current edit mode".
		-  Click button "Synchronize"(F10) to apply updates in current edit mode to other modes.
		-  Click button "Save"(F2) to save updates in current edit mode and do "Synchronize" automatically.
		-  Click button "Pop"(CTRL/ALT+p) to display contents in current edit mode in a new window.
		-  Click button "Menu"(F12) or right click edit area to pop buttons menu.
		-  Click button "MyBox Clipboard"(CTRL/ALT+m) to pop texts clipboard for pasting.
	-  Select frame in frameset to edit.    
2. Find strings in html.
3. Find elements in html.
4. Snap html page. Dpi can be set. Save as one image or images in PDF.
5. Extract table data in html in batch.
6. Convert html as Markdown/text/PDF in batch.
7. Convert charset/style in html files in batch.
8. Merge multiple pages as one html/Markdown/texts/PDF.
9. Generate frameset file for multiple files. 

![Snap-htmlEditor](https://mararsh.github.io/MyBox/snap-htmlEditor-en.jpg)        

### Microsoft Documents <a id="msDocuments"></a>
1. Handle Word file
	-  Formats:            
		-  File ".doc"(Word 97， OLE 2) is converted as html. Most of formats kept.
		-  File ".docx"(Word 2007, OOXML) is converted as text. Formats lost.
	-  View Word file.            
	-  Convert Word  files as html/PDF in batch.          
2. Handle PPT file:
	-  Formats:            
		-  File ".ppt"(PPT 97, OLE 2) .
		-  File ".pptx"(PPT 2007, OOXML).                       
             If no comment, both formats are suportted.                    
	-  View PPT file. Shown page by page:
		-  Page is converted as an image
		-  Slide text and Note text are displayed
	-  Convert PPT files as images/PDF in batch
	-  Extract objects in PPT file in batch:
		-  Selections: Slide text, Note text, Master text, Comments text,  image, sound,  OLE(Word/Excel)
		-  Not support to extract sounds in ".pptx".
	-  Split PPT files in batch
	-  Merge ".pptx" files
	-  Combine images as ".ppt" file.
	-  Play PPT file
3. Extract texts in Excel/Word/PowerPoint/Publisher/Visio files in batch.

![Snap-viewPPT](https://mararsh.github.io/MyBox/snap-viewPPT-en.jpg)        

### Edit Bytes <a id="editBytes"></a>
1. Bytes are expressed as 2 hexadecimal characters. All blanks, line breaks, and invalid values are ignored.
2. Input boxes of general ASCII characters.
3. Break lines, which is only for display and has not actual effect. By bytes number or by some defined bytes.
4. Select charset to decode bytes which can be viewed, scrolled, and selected synchronously.
5. Paginate. When break lines by bytes number, crossing pages need not concerned.


### Text in MyBox Clipboard<a id="myboxTextClipboard" />
1. Add/Delete/View texts in MyBox Clipboard.
2. Add text in System Clipboard.
3. Paste selected text in System Clipboard.
4. In context menu of all text input controls, MyBox Clipboard can be popped for selecting text to paste.

### Text in System Clipboard<a id="systemTextClipboard" />
Load/Refresh/Delete text in System Clipboard:             

1. After button is clicked, new texts in System Clipboard are monitored and saved in MyBox Clipboard.
2. Monitor interval can be set.
3. Monitored texts can be accumulated in this interface. Separator can be choiced.
4. This monitoring stops when one of following happens：
	-  User clicks button "Stop"
	-  MyBox exits.
	-  "Copy to MyBox Clipboard" is not checked and this interface is closed.
5. Option "Start monitoring when MyBox reboots".     

## Image Tools <a id="imageTools"></a>

### View Image <a id="viewImage"></a>

1. "Load Width". Read image file with "Original Size" or with defined width.
2. "Select Mode".
3. Rotation can be saved.
4. Recover, Rename, Delete.
5. Select whether display Corodinate, X/Y Rulers, Data.
6. Image attributes and image meta. ICC profile embedded in image can be decoded.
7. Navigation of images under same directory.
8. Context menu.
9. Option about whether handle selected area or whole image.       
10. Redering parameters when save or modify image.                    
    
![Snap-imageViewer](https://mararsh.github.io/MyBox/snap-imageViewer-en.jpg)        

### Browse Images <a id="browserImage"></a>

1. Display multiple images in same screen. Rotation and zoomming can be separated or synchronized.
2. Rotation can be saved.
3. Grid Mode. Files number, columns number, and load width can be set.
4. Thumbnails List Mode.
5. Files List Mode.
6. Rename and Delete.        

![Snap-imageBrowser](https://mararsh.github.io/MyBox/snap-imageBrowser-en.jpg)        


### Analyse Image <a id="analyseImage"></a>
1. Statistic and visualization of image data, including average, variance, skewness, median, mode, 
minimum, maximum of occurance of each color channel, and their histograms.
2. Channels of histograms can be selected.
3. Statistic against selected area.
4. Count dominant colors:
	- Calculate mostly different colors in image by K-Means Clustering.
	- Calculate mostly occurred colors in image by Popularity Quantization.
	- Results can be imported in Color Palette.
5. Image data can be saved as html file.        

![Snap-imageAnanlyse](https://mararsh.github.io/MyBox/snap-imageAnanlyse-en.jpg)        


### Play Images<a id="playImages" />     
1. Following types of files can be played:
	-  Dynamical gif file
	-  Multiple-frames tif file
	-  PDF file
	-  PPT file         
    Each page of PPT/PDF file is converted as an image to display.      
2. In this version, all required images are loaded in memory.              
    To avoid out of memory:
	-  Set frames range to display.
	-  Set width of images to load.
	-  Set dpi  for images in PDF.  
3. Images are displayed frame by frame:   
	-  Set intervals and speed times
	-  Pause/Continue
	-  Select a frame
 	-  Previous/Next frame
 	-  Options "Loop" and "Reverse"

### Image Manufacture <a id="imageManufacture"></a>
1. Copy
	-  Copy part inside current scope, part outside of current scope, or whole image.
	-  Whether cut margins, whether copy to system clipboard.
 	-  Set background color.
2. Crop
	-  Crop part inside current scope, or part outside of current scope.
	-  Whether cut margins, whether copy to system clipboard.
 	-  Set background color.
3. Clipboard
	-  Clip sources:
		- "Copy"(CTRL+c) against whole image or selected part of image
		- Cutted part of image
		- System clipboard
		- Image files in system
		- Example clips
	-  Manage clips list: Add, Delete, Clear, Set maximum number of list.
	-  Click button "Paste"(CTRL+v) anytime while editing image, to paste the first image in clipboard onto current edited image. Or double click item in the clipboard to paste it.
	-  Drag and move pasted clip on current edited image, to adjust clip's size and location.
	-  Options to paste: whether clip on top, whether keep aspect ratio, blending mode, opacity, rotation angle.
4. Scale: By dargging anchors, by setting scale, or by inputting pixel values with 4 types of keeping aspect ratio. Rendering parameters can be set.
5. Color. Increase, decrease, set, filter, or invert value of saturaion, brightness, hue, Red/Green/Blue/Yellow/Cyan/Magenta channel, RGB itself, or opacity.        
    Premultiplied Alpha is supported for setting opacity.        
6. Effect. Posterize(reduce colors), thresholding, gray, black-white, Sepia, emboss, edges detect. Algorithms and parameters can be set.
7. Enhancement. Contrast, smooth, sharpen, convolution. Algorithms and parameters can be set.        
    Algorithms and parameters can be set. Convolution can be defined and referred to make more effects.
8. Rich Text: Edit texts in web page mode. Drag the texts on image to adjust its location and size. Options: background color, opacity, margions width, arc size, rotation angle.
    Due to implementation of snapshots, results look blur. I have not found solution. (Not support in current version)
9. Text. Options like font family, style, size, color, blend modes, shadow, angle, whether outline, whether veritical. Locating text by clicking image.
10. Pen:
	-  Polyline: One line by multiple drawing. Options: stroke width, color, whether dotted, blend modes.
	-  Lines: One line by one drawing. Options: stroke width, color, whether dotted, blend modes.
	-  Eraser: One line by one drawing. Always transparent. Option: stroke width.
	-  Frosted Class: One dot by one drawing. Options: stroke width, intensity, shape(Rectangle or circle).
	-  Mosaic: One dot by one drwaing. Options: stroke width, intensity, shape(Rectangle or circle).
	-  Shape: Rectangle, Circle, Ellipse, Polygon. Options: stroke width, color, whether dotted, blend modes, whether fill-in, color of fill-in.
11. Transform. Shear, mirror, and rotate.
12. Round corner. Arc and background color can be set.
13. Shadow. Options: background color, shadow size, whether apply Premultiplied Alpha.
14. Margins. Blur margins with option of whether apply Premultiplied Alpha; Drag anchors to adjust margins; add margins by setting width; cut margins by setting width or color.
15. Editing histories:
	- Each modification will be recorded as image histories.
	- Manage histories:  Delete, Clear, Recover selected history as current editing image, Set maximum number of histories.
	- Undo(CTRL+z) and redo(CTRL+y) previous modification. Recover to original image(CTRL+r) at any time. Either select one history to recover.
16. "Scope":  Rulers to limit pixels to operate, including area rulers, color matching rulers, or rulers mixed by both types.
	- Define area: Rectangle, Circle, Ellipse, Polygon. Can be excluded.
	- Define colors list. Can pick colors directly from image by Color Palette.
	- Select object for color matching, including Red/Green/Blue channel, saturaion, brightness, hue, RGB, with distance defined. Can be excluded.
	- Matting: Match pixels around current pixel, and spread results with same matching rulers. Result is the collection of pixels matched by multiple points.
	- Outline: Extract outline of image which has transparent background, as the scope of operation.
	- Scope can be applied against Copy, Crop, Color, Effect, Convolution.
	- Scope can be defined against image history and reference image too. The part in scope can be copied into clipboard.
	- Scopes can be saved with names. User can manage them: Add, Delete, Clear, Edit, Use selected item in scopes list.
17. Pop current image. Option: Whether always on top.
18. Interface in style of "Visible As Need":
	- Show/Hide left pane(F4), show/hide right pane(F5)
	- Vertical accordion menus
	- Overlaying tabs to switch
	- Show/Hide controls as functions
19. Demo: One clicking to diaplay examples of kinds of image manufacture about "Color", "Effect", "Enhancement", and blend modes.
20. Image Manufacture in batch.        

![Snap-imageManufacture](https://mararsh.github.io/MyBox/snap-imageManufacture-en.jpg)        

### Edit Images<a id="imagesList" />
1. Add following：
	-  Animated gif file. All frames are added into list. 
	-  Multiple-frames tif file. All frames are added into list.   
	-  PDF file. All pages are converted as images and added into list.       
	-  PPT file. All pages are converted as images and added into list.        
	-  Image in system clipboard. 
	-  Any supported image files.     
2. Move images to set their orders.
3. Set durations of images, which work for playing list and animated gif file.
4. Play the list. Select some images by CTRL/SELECT to play, or select none to play whole list.
5. Save the list:：
	-  Select some images by CTRL/SELECT to save, or select none to save whole list.
	-  Save each item as a supported image file.
	-  Splice images.
	-  Merge items as a multipleg-frames tif file.
	-  Merge items as an animated gif  file.
	-  Merge items as a PDF file.
	-  Merge items as a PPT file.            
 	-  Merge items as a video file(need ffmpeg).          

![Snap-editImages](https://mararsh.github.io/MyBox/snap-editImages-en.jpg)         


### Merge images <a id="multipleImages"></a>
1. Splice images. Options like array ordering, background color, interval, margins, and size.
2. Add Alpha channel.

### Part image <a id="imagePart"></a>
1. Split image. By number, by size, or by customizing. Results can be saved as image files, multiple frames Tiff file, or PDF file.
2. Subsample image. Options like sample region and sample ratio.
3. When image file includes too many pixels and loaded as sampled image, splitting and subsampling handle the original image in file instead of the loaded image in memory.
4. Extract Alpha channel.

### Image Conversion <a id="imageConvert"></a>
1. Formats of image file: png, jpg, bmp, tif, gif, ico, wbmp, pnm, pcx, raw.
2. Color spaces: sRGB, Linear sRGB, ECI RGB, Adobe RGB, Apple RGB, Color Match RGB, ECI CMYK, Adobe CMYK(several), Gray, Binary
3. Color space based on external ICC profile.
4. Option to embed ICC profile.
5. Options to handle transparent channel, including keep, delete, premultiply and delete, premultiply and keep.
6. Options of compression types and quality.
7. For binary, algorithms can be choiced: OTSU, default or threshold. And option of dithering.
8. Conversion in batch.

### Recognize Texts in Image <a id="imageOCR"></a>
1. Preprocess image:
	-  Algorithms of image maunfacture
	-  Scale ratio
	-  Binary threshold
	-  Rotation angle
	-  whether deskew automatically
	-  Whether invert colors
2. Recognization Options:
	-  Languages list and their order
	-  whether generate data of "Regions" and level can be set
	-  whether generate data of "Words" and level can be set
3. When recognize single image:
	- Preprocessed image can be saved and loaded
	- Rectangle can be set to define the area to do OCR.
	- Display preprocessed image, original image, recognized texts and html.
	- Display data of Regions and Words in html which can be saved.
	- Demo: One clicking to show examples of image enhancement.
4. When recognize in batch, options:
	-  Whether generate html or PDF
	-  Whether merge recognized texts
5. OCR engine:
	-  For win, both embedded and installed tesseract can be selected.
	-  For linux and mac, only installed tesseract can be used.
6. OCR data files path:
	-  Can be set as any path which can be read. If tesseract is installed, suggest to set as its subdirectory "tessdata".
	-  MyBox includes "fast" data files of English and Chinese, and will copy them to this path if it has not them.
Notice:  When use embedded engine, it is better that name of file/path is pure English to avoid failure.        

![Snap-ocr](https://mararsh.github.io/MyBox/snap-ocr-en.jpg)        


### Color Management<a id="ColorManagement" />
1. Manage color palettes:
	-  Add/Delete/Rename/Copy.
	-  Examples: "Common web color", "Traditional Chinese colors", "Traditional Japanese Colors", and "Colors from colorhexa.com".
2. Manage colors in palettes: Add/delete/Copy/Name/Order/Import/Export.
3. Display colors:
	-  Data in table in simple/all columns. Or display colors in merged/separated columns.
	-  Color is shown in a small rectangle. Its name(if has), hexidecimal value, rgb values, opacity, cmyk values, and cie values are popped when mouse is moved upon it.
4. Add colors:
	-  Get colors from color-picker.
	-  Input colors list. Examples are provided. Valid color values are like:        
 	  	 	  	orange        
 	  	 	  	0xff668840        
 	  	 	  	0xff6688        
 	  	 	  	#ff6688        
 	  	 	  	#f68        
 	  	 	  	rgb(255,102,136)        
 	  	 	  	rgb(100%,50%,50%)        
 	  	 	  	rgba(255,102,136,0.25)        
 	  	 	  	rgba(255,50%,50%,0.25)        
 	  	 	  	hsl(240,100%,100%)        
 	  	 	  	hsla(120,0%,0%,0.25)        
	-  Click button "Pick Color" in intefaces of image viewer/manufactor.        
5. Change colors:
	-  Color name can be empty and duplicated. Same color can have different names in different palettes.
	-  Color order can be any float. Same color can have order numbers in different palettes.
	-  Click button to trim order numbers in step 1.
	-  Drag-drop colors to adjust their orders in palette.
6. Export colors: current page, all, or selected rows as html or csv file.
7. Import color file in CSV format:
	-  File encoding is UTF-8 or ASCII.
	-  The first line defines data headers which are delimited by English commas.
	-  Followed each line defines one data row. Data fields are delimited by English commas.
	-  Following fields are necessary:        
                 rgba  or rgb        
	-  Following fields are optional:        
                 name
8. Query color.

[Web Colors](http://mararsh.github.io/MyBox_data/colors/WebColors.html)        

![Snap-colors](https://mararsh.github.io/MyBox/snap-colors-en.jpg)        

### Color Space <a id="colorSpaces"></a>
1. Draw Chromaticity Diagram:
	-  Outlines of standard data, including CIE 1931 2 Degree Observer(D50), CIE 1964 10 Degree Observer(D50), CIE RGB Gamut, ECI RGB Gamut,
	   sRGB Gamut, Adobe RGB Gamut, Apple RGB Gamut, PAL RGB Gamut, NTSC RGB Gamut, ColorMath ProPhoto RGB Gamut, SMPTE-C RGB Gamut.
	-  Standard illuminants(White points), including A, C, D50, D55, D65, E.
	-  User can fill in tristimulus values or color coordinate or select color, and the tool will calculate values in kinds of color space and display the calculated color in the chromaticity diagram.
	-  User can input or import spectral data, and the tool will filter special characters and display the spectral values in the chromaticity diagram.
	-  User can select to show or hide the items of above data in the chromaticity diagram.
	-  User can select the background color of the chromaticity diagram as transparent, white, or black. Dot size or line size can be selected for outlines.        
	   Grid and wave values can be selected to show or hide too.
	-  Table and texts are shown for standard data, including CIE 1931 2 Degree Observer 1nm, CIE 1931 2 Degree Observer 5nm, CIE 1964 10 Degree Observer 1nm, CIE 1964 10 Degree Observer 5nm. Data texts can be exported.
2. Edit ICC profile:
	-  Predefined standard ICC profiles, including Java Embeded ICC like sRGB/XYZ/PYCC/GRAY/LINEAR_RGB, files from ECI like ECI_CMYK/ECI_RGB_v2, and files from Adobe like Adobe_RGB/Apple_RGB/CMYK.
	-  All fields in header can be edited. "Profile id" is calculated as MD5 digest automatically when ICC profile is saved.
	-  Tags table shows fields of tag, name, type, offset, size, description, decoded data, and raw data of bytes in hexadeciaml.
	-  Editable tag types include: Text, MultiLocalizedUnicode, Signature, DateTime, XYZ, Curve, ViewingConditions, Measurement, S15Fixed16Array.        
	   Tag type "LUT" is not editable in this version.
	-  Option "Normalize data of LUT in range of 0~1".
	-  Whole ICC profile is read as XML and can be exported. Data not decodes are shown as bytes in hexadeciaml.
	-  Loaded ICC data can be modified and saved as new ICC profile.
3. RGB Color Space:
	-  User select or input RGB color space(Primaries and white), select or input reference white, and the tool will calculate the adapted primaries values automatically and show the calculation procedure.
	-  Decimal scale can be set.
	-  Adaption algorithm can be selected from Bradford, XYZ Scaling, and Von Kries.
	-  Predefined standard RGB color spaces include CIE RGB, ECI RGB, sRGB, Adobe RGB, Apple RGB, PAL RGB, NTSC RGB, ColorMath ProPhoto RGB, SMPTE-C RGB.
	-  Predefined illuminants include A, B, C, D50, D55, D65, D75, E, F1~F12 of CIE 1931 and CIE 1964.
	-  Table and texts are shown for adapted primaries by different RGB color spaces, different illuminants, and different algorithms. Data texts can be exported.
4. Transform Matrices between Linear RGB and XYZ:
	-  User select or input RGB color space(Primaries and white), select or input reference white of XYZ color space, and the tool will calculate the transform matrix between the linear RGB and XYZ automatically and show the calculation procedure.
	-  Table and texts are shown for transform matrices by different RGB color spaces, different reference whites of XYZ, and different algorithms. Data texts can be exported.
5. Transform Matrices between Linear RGB and Linear RGB:
	-  User select or input source and target RGB color spaces(Primaries and white), and the tool will calculate the transform matrix between the 2 linear RGB color spaces automatically and show the calculation procedure..
	-  Table and texts are shown for transform matrices by different RGB color spaces and different algorithms. Data texts can be exported.
6. Illuminants:
	-  User input source color(relative/tristimulus/coordinate), select or input source white and target white, and the tool will calculate the adapted color automatically and show the calculation procedure..
	-  Table and texts are shown for standard illuminants list including color values, color temperature, and description. Data texts can be exported.
7. Chromatic Adaptation Matrices:
	-  User select or input source white and target white, and the tool will calculate the chromatic adaptation matrix automatically and show the calculation procedure..
	-  Table and texts are shown for chromatic adaptation matrices by different standard illuminants and different algorithms. Data texts can be exported.        

![Snap-colorDiagram](https://mararsh.github.io/MyBox/snap-colorDiagram-en.jpg)        

### Images in MyBox Clipboard<a id="myboxImageClipboard" />
1. Add/Delete/View images in MyBox Clipboard.
2. Examples are provided.
3. Image in System Clipboard can be added.
4. Selected image can be copyed in System Clipboard.    

### Images in System Clipboard<a id="systemImageClipboard" />
Load/Refresh/Delete image in System Clipboard:        

1. After button is clicked, new images in System Clipboard are monitored.
2. Monitor interval can be set.
3. Monitored images can be saved as files, or copyed in Mybox Clipboard.
4. Width of saved image can be set.          
5. The monitoring stops when one of following happens：
	-  User clicks button "Stop"
	-  MyBox exits.
	-  All of following are satisfied:
		- "Copy to MyBox Clipboard" is not checked
		- "Save as Files" is not checked or target path is invalid
		- This interface is closed

![Snap-systemClipboard](https://mararsh.github.io/MyBox/snap-systemClipboard-en.jpg)         

### Others <a id="imageOthers"></a>
1. Supported image formats include png, jpg, bmp, tif, gif, ico, wbmp, pnm, pcx.        
   Adobe YCCK/CMYK jpg file can be decoded.
2. Pixels calculator
3. Convolution Kernels Manager

### Big Image <a id="bigImage"></a>
1. Evaulate the required memory for whole image, and judge whether load all data in memory.
2. If enough memory is available to load whole image, read all data for next operations. Try best to operate in memory and avoid file I/O.
3. If memory may be out, subsample the image for next operations.
4. The sample ratio is determined by following rule: Make sure the sampled image is good enough while the sampled data occupy limited memory.
5. The sampled image is mainly for displaying, and not suitable for operations against whole image and images merging.
6. Some operations, like splitting and subsampling, can be handled by reading part of image data and writing-while-reading, so they are suitable for big images. Sampled image is displayed while original image is handled.

## Data Tools <a id="dataTools"></a>

### Manage Data<a id="manageData" />          
This tool manages following objects:            

1. Data Files
	- Record is created/updated when csv/excel/texts data file is opened by its editor.
	- Data are saved in data file.
	- Deleting record of data file will not delete data file itself.
2. Data Clipboards
	- Record is created when data is copied into MyBox Clipboad.
	- Data are saved in file under MyBox internal path.
	- Deleting record of data clipboards will delete its internal file.
3. Matrices
	- Records are maintained by Matrices Manager.
	- Data are saved in MyBox database.
	- Deleting record of matrix will delete data of this matrix.           
4. Data Tables
	- Records are maintained by Data Tables Manager.
	- Data are saved in MyBox database tables.
	- Deleting record of data table will delete data of this data table.           
    
![Snap-manageData](https://mararsh.github.io/MyBox/snap-dataManage-en.jpg)        


### Edit Data<a id="editData" />
1. Following objects can be edited in consistent way: data files(csv/excel/texts), data clipboard, matrices, and database tables.     
2. Data should be in same width. That is all rows have equal number of columns.   
3. Data are paginated. When pages number is larger than 1, changes should be saved before run some functions.
4. Data can be edited in 2 modes: 
      - "Table" is the master edit mode:
 		- Its modifications are applied to other panes automatically.
 		- It is the final data to save.
      - "Text" is the assist edit mode. 
 		- Click button "OK" to apply its modifications to "Table".
 		- Click button "Cancel" to discard its modifications and pick data from "Table".
 		- Click button "Delimiter" to pick data from "Table" and apply new delimiter while its modifications are discarded. 
5. Edit attributes/columns: 
      - Column names should not be null nor duplicated.
      - Data types are used to validate data values:
 		- Invalid value is rejected when edit data.
 		- Type is ignored when read data.
 		- Invalid number is counted as zero when calculate data.
 		- Data type affects sorting results.
      - Click button "OK" to apply it modifications to "Table".
      - Click button "Cancel" to discard its modifications and pick data from "Table".
6. When changed, * is displayed in tab header. And ** is displayed when modifications have not applied. 
7. Click button "Save" to write modifications to file and database:
      - Changes of rows in "Table", including modify/add/delete/sort, affect rows of current page in file. 
      - Changes in "Columns" tab, including modify/add/delete/sort, affect all rows in file. 
      - Changes of attributes and columns are saved in database. 
8. Click button "Recover" to discard all modifications and load data from file and database.
9. Handle data:    
	- Object: Selected rows or all data rows, and selected columns.           
	- Operation: Set values, Copy, Paste, Sort, Statistic, Percentage, Normalize, Transpose, Export.              
	- Target: New csv/excel/texts data file, matrix, System Clipboard, MyBox Clipboard, or insert/append/replace in defined location in the table.                  
10. Data Charts:        
	- "Category Column" defines data names.
	- "Value Column" defines data values which should be numbers:
	- Except for Pie Chart, multiple columns can be selected as number series in charts. 
 		- Different value series are shown in different colors or shapes. 
 		- When data can not be parsed as numbers, they are counted as zero. 
 		- Invalid data are ignored.
	- Bar/Area Chart represents data size with bars' heights.
	- Line Chart represents data trend with lines connecting points.
	- Pie Chart represents data percentages with a circle divided into segments.               
	   Value column should be non-negative.
	- Bubble Chart represents data size with circles of different radius：
 		- "Category Column" and "Value Column" define coordinates of data.
 		- "Size Column" defines data size.
 		- All columns should be numbers. Size columns should be non-negative.
	- Scatter Chart represents data distribution with symbols.
11. Convert data as database table:
	- Selected rows or all data rows, and selected columns.           
	- Generate auto-increment column as primary key, or select columns as primat keys.
	- Option about whether import data.
12. Text format and html are displayed synchronously:

![Snap-dataChart](https://mararsh.github.io/MyBox/snap-dataChart-en.jpg)         


### Data File<a id="dataFiles" />
1. Edit data file:
	- When file is loaded abnormally, change options and click Refresh button.
	- Options of CSV file and text data file include charset, whether has first line as field names, and delimiter of data.
	- To Excel file:
 		- Options include sheet number and whether has first line as field names.
		- Add/Delete/Rename sheets.
 		- Tool can only handle base data in Excel file. If file includes format, style, formula, or chart, suggest to save changes as new file to avoid data loss.
2. Convert/Split data files in batch:
	- Source files' formats can be csv, excel, and text. Options of source files can be set.
	- Target files' formats include csv, text, excel, xml, json, html, pdf. Options of target files can be set.
	- Split files as maximum lines.
3. Merge csv/excel/text data files.             

![Snap-dataEdit](https://mararsh.github.io/MyBox/snap-dataEdit-en.jpg)    


### Data in System Clipboard<a id="dataInMyBoxClipboard" />                          
1. Input texts or paste texts in System Clipboard.       
2. Select data rows and columns, and paste them into target sheet at selected location.             

![Snap-dataInSC](https://mararsh.github.io/MyBox/snap-dataInSC-en.jpg)         


### Data in MyBox Clipboard<a id="dataInSystemClipboard" />
1. Copy, edit, and save data in database.   
2. Select data rows and columns, and paste them into target sheet at selected location.             

![Snap-dataInMC](https://mararsh.github.io/MyBox/snap-dataInMC-en.jpg)         

### Matrix<a id="matrix"></a>
1. Edit matrix. 
2. matrix can be saved and reused.
3. Unary matrix calculation: Transpose, Row Echelon Form, Reduced Row Echelon Form, Determinant By Elimination, Determinant By Complement Minor, Inverse Matrix By Elimination, Inverse Matrix By Adjoint, Matrix Rank, Adjoint Matrix, Complement Minor, Normalize, Multiply Number, Divide By Number, Power.
4. Binary matrices calculation: Plus, Minus, Hadamard Product, Kronecker Product, Horizontally Merge, Vertically Merge.


### Database Tables<a id="dataTables" />
1. Table name and column names should satisfy "Limitations of SQL identifier":
      - Maximum length is 128.
      - "Ordinary identifier":
 		- Not surrounded by double quotation marks.
 		- Must begin with a letter.
 		- Contains only letters, underscore characters (_), and digits.
 		- Permits Unicode letters and digits.
 		- Can not be reserved words.
 		- It is converted as uppercase when saved in database.
 		- It is case-insensitive when referred in SQL statement.
 		   Example, AbC is same as abc and aBC.
      - "Delimited identifier":
 		- Surrounded by double quotation marks.
 		- Can contain any characters.
 		- It is saved as string inside the double quotations in database.
 		- It should be surrounded by double quotations when referred in SQL statement, except for following:  It only includes upper case letters and underscores.                     
 		   Example, "AbC" is different from AbC or "ABC" while "ABC" is same as ABC and abc.
2. After database table is created:
      - Definition of Primary keys can not be changed and deleted.
      - Definition of  other columns can be added and deleted but can not be changed.
3. When MyBox create name of table/column:
      - Invalid characters are converted as underscore characters.
      - If it does not start with a letter, character "a" is added in front of it.            

![Snap-dataInMC](https://mararsh.github.io/MyBox/snap-dataTables-en.jpg)         

### Database SQL<a id="dbSQL" />
1. Provide examples of SQL statements.
2. List names of all user tables automatically.
3. View table definitions of all user tables.
4. Display outputs of execution and results of query.     
5. SQL codes can be organized as information of tree.
6. Can load or save as external files.       

![Snap-dataInMC](https://mararsh.github.io/MyBox/snap-dbSQL-en.jpg)          


### JShell(Java interactive coding tool)<a id="JShell" />              
JShell is one of tools in JDK. This tool helps to run JShell in GUI:              
    
1. JShell provides capability to interactively evaluate "snippets", as Read-Eval-Print Loop (REPL). 
2. "Snippet" is a single expression, statement, or declaration of Java programming language code:
	- Semicolons should be in the end of statement while expression need not it.
	- Except for base classes, most of Java classes should be imported before call them.  
	- Variables and methods can be defined and called later.
3. Input several snippets and click button "Start" to run them: 
	- Snippets are evaluated one by one.
	- Results of snippets will affect later snippets, like "an execution environment".  
	- Attributes of all evaluated snippets will be shown in a table.
	- Click button "Delete" or "Clear" to drop some or all snippets from current environment.
	- Click button "Reset" to empty JShell and environment becomes blank.
4. JShell can be used for scientific computation and Java codes debug.
5. JShell codes can be organized as information of tree.
6. Can load or save as external files.   

![snap-JShell](https://mararsh.github.io/MyBox/snap-JShell-en.jpg)           
   

### Common Data Management<a id="dataManage" />
1. Define data.
2. Data constraints：
 	- Provisions:
 	  	- Null value of integer/long/short is the minimum value(MIN_VALUE)
 	  	- Null value of double is the maximum value(Double.MAX_VALUE)
	- Coordinate system:
 	  	- Valid values：
 	  	   	- CGCS2000(China Geodetic Coordinate System), real locations and approximate to WGS-84(GPS).
 	  	   	- GCJ-02(China encrypted coordinate), encrypted data with offsets of real locations.
 	  	   	- WGS-84(GPS), real locations.
 	  	   	- BD-09(Baidu encryted coordinate),  based on GCJ-02.
 	  	   	- Mapbar coordinate,  based on GCJ-02.
 	  	- When coordinate is unknown or invalid, the default value is CGCS2000.
	- Coordinate values：
 	  	- Decimal values of longitude and latitude, instead of Degrees Minutes Seconds(DMS), are used when data handled.
  	  	- MyBox provides "Location Tools" to convert coordinate values between decimal and DMS.
 	  	- Valid range of longitude is `-180~180`, and valid range of latitude is `-90~90`.
	- Time:
 	  	- Formats:
 	  	   	- Date and Time, like: 2014-06-11 13:51:33
 	  	   	- Date, like: 2014-06-11
 	  	   	- Year, like: 2014
 	  	   	- Month, like: 2014-06
 	  	   	- Time, like: 13:51:33
 	  	   	- Time with Milliseconds, like: 13:51:33.261
 	  	   	- Date and Time with Milliseconds, like: 2014-06-11 13:51:33.261
 	  	   	- Date and Time with zone, like: 2020-09-27 12:29:29 +0800
  	  	   	- Date and Time with Milliseconds and zone, like: 2020-09-27 12:29:29.713 +0800
  	  	   	- "T" can be written or omitted between date and time. "2014-06-11T13:51:33" equals to "2014-06-11 13:51:33".
 	  	- Era:     
 	  	 	  	 "0 AD" = "1 BC" = "0" = "-0" = "0000" = "-0000"  = "0001-01-01 00:00:00 BC" =  "公元前1" = "公元前0001-01-01 00:00:00"       
 	  	 	  	 "1 AD" =  "1"  = "0001" = "0001-01-01 00:00:00" = "0001-01-01 00:00:00 AD" =  "公元1" = "公元0001-01-01 00:00:00"       
 	  	 	  	 "202 BC" = "-203" = "-0203" = "-0203-01-01 00:00:00"  = "0202-01-01 00:00:00 BC" = "公元前202" =  "公元前0202-01-01 00:00:00"       
 	  	 	  	 "202 AD" = "202" = "0202" = "0202-01-01 00:00:00" = "0202-01-01 00:00:00 AD" = "公元202" = "公元0202-01-01 00:00:00"       
 	  	- Valid examples of Era:       
 	  	 	  	 2020-07-13 11:30:59       
  	  	 	  	 -2020-07-13 11:30:59       
	  	 	  	 -581-01-23              
 	  	 	  	 960       
 	  	 	  	 公元960       
 	  	 	  	 公元前770-12-11       
 	  	 	  	 公元前1046-03-10 10:10:10       
 	  	 	  	 202 BC       
 	  	 	  	 960-01-23 AD       
 	  	 	  	 1046-03-10 10:10:10 BC       
3. Add/Delete/Edit/Copy/Clear/Refresh data.       
4. Query data:
 	-  Define and manage query conditions.
 	-  Current query conditions is displayed on tab "information".
 	-  Data satisfying current query condition are displayed in tab "Data" in pages.
 	-  Data rows can be displayed in different colors as values of some column.
5. Import data in csv format:
 	- File encoding is UTF-8 or ASCII.
	- The first line defines data headers which are delimited by English commas.
	- Followed each line defines one data row. Data fields are delimited by English commas.
	- The order of fields is not cared.
	- Necessary fields must occupy their locations, but need not have valid values(related to data).
	- Select whether replace existed data. Predefined data or example data always replace existed values.
6. Export data:
 	- Define and manage export conditions.
 	- Export data fields can be selected.
	- Export file format can be selected: csv, xml, json, xlsx, html, pdf.
 	- Select maximum lines to split files.
 	- Can export current data page.
7. Delete/Clear data:
 	-  Define and manage delete conditions.
 	-  Predefined data can not be deleted.
 	-  Referred data(like foreign keys) can not be deleted.
8. Define, manage, and use "Conditions":
 	-  "Conditions" are used to execute querying, deleting, or exporting.
 	-  Set conditions in panes:       
 	  	- Data conditions are organized as trees.  Multiple nodes can be selected.
 	  	- Multiple data fields can be selected as sorting conditions, and their orders can be changed.
 	-  Edit condition: Title, where, order by, fetch.  They will be merged as final conditon.
 	-  Manage conditons: Add/delete/edit/copy.
 	-  Conditions ever executed are saved automatically.
	-  Recently visited conditions are listed in pop window of the buttons.


### Map Data<a id="mapData" />
1. Kinds of data can be presented in map, including Geography Codes, Location Data, and Coordinate Querying.
2. Data in map can be:
 	-  All data which satisfy current query condition. "Maximum number of data" can be set to avoid performance issues.
 	-  Data in current page.
3.  TianDiTu:
 	-  Accepts coordinates of CGCS2000 and display them at correct locations without offsets.
 	-  When display other coordinates, MyBox converts them to CGCS2000 to show correct locations.
 	-  Projection can be selected: EPSG:900913/3857(Web Mercator) or EPSG:4326(Geodetic).
 	-  Controls can selected: Zoom, Scale, Map Type, Symbols.
 	-  Map Types: Standard, Satellite, Mixed Satellite, Terrain, Mixed Terrain.
 	-  Languages in different regions.
 	-  Range of map levels is 1-18.
4. GaoDe Map:
 	-  Accepts coordinates of GCJ-02 and display them at correct locations without offsets.
 	-  When display other coordinates, MyBox converts them to GCJ-02 to show correct locations.
 	-  Projection is EPSG:900913/3857(Web Mercator).
 	-  Map layers:
 	  	- Can select multiples:  standard, satellite, roadnet, traffic.
 	  	- Roadnet layer and traffic layer are only supported for China.
 	  	- Satellite layer is supported for part of foreign addresses.
 	  	- Opacity can be set for each map layer.
 	-  Map language: Chinese, English, Chinese and English.
 	-  Range of map levels is 3-18
 	-  Can selected "Fit View" to adjust map level and center as best automatically while display all data.
5. Adjust map level by:
 	-  Scroll mouse wheel.
 	-  Click map controls.
 	-  Select "Map Size"
6. Marker image:
 	-  Selections: point(bubble), circle, or any image.
 	-  For Location Data, more selections: Data Set Image, Data Image. Point will be used if no valid value.
 	-  Size can be set(Same size for width and height)
7. Marker text：
 	-  Selections: Label, Coordinate, Address.
 	-  For Location Data, more selections: Start Time, End Time, Data Value, etc.
 	-  Multiples selections can be picked. Each selection will be showns in a line.
 	-  Size can be set.
 	-  Can select whether text is bold.
 	-  Color can be set. For Location Data, "Data Color" can be chosen.
8. Pop information:
  	-  Detailed information can be popped when mouse is upon marker.
  	-  Can select whether pop information.
9. Snapshot:
  	-  DPI can be set.
  	-  Current map and data in map can be saved and displayed in html.
10. Keys of map can be changed in "Settings".  The default keys are free and shared by all MyBox users.


### Geography Code<a id="geographyCode" />
1. Data definition：
	-  Basical attributes: id, level, longitude, latitude, chinese_name, english_name, 5 codes, 5 aliases,
	-  Subordinate: owner, continent, country, province, city, county, town, village, building. ("Ancestors")
	-  Auxiliary attributes: altitute, precision, coordinate system, area(square meters), population, comments, isPredefined.
2. Data constraints:
 	-  Not null values: id, level,  chinese_name or english_name.
 	-  Values of "level": global(only "Earth"), continent, country, province(state), city, county(district), town, village(neighborhood), building, point of interest.
 	-  Data is unnecessary to be subordinated level by level. Cross-over can happen.
           Example, a village is subordinated to Antarctica, and a city belongs to a country without province level.
	-  Match data:
 	  	- One of following can determine an address:
 	  	 	- Match “id"(assigned by MyBox automatically). This is accurate matching.
 	  	 	- Match "level" + ancestors + "chinese_name"/"english_name"/any one "alias". This is accurate matching.
 	  	 	- Match "level" + "chinese_name"/"english_name"/any one "alias". This is fuzzy matching. Duplicated names in same level can cause false matching.
 	  	- Matching of name or alias is case-insensitive.
 	  	- Sometimes 5 "code" are useful to match data.
3. Edit data：
 	-  "subordinate" of data is set by selecting node in locations tree.
 	-  "level" of data should be lower than its ancestors.
 	-  Data must have either chinese_name or english_name.
 	-  Select or display coordinate in map.
 	-  Set as "Predefined data" or "Inputted data" against selected rows.
4. Define "Condition":
 	-  All geograhy codes in MyBox are organized as a Locations Tree by their subordination relationship. Multiple nodes can be selected.
5. Import data:
 	-  Embedded predefined data in MyBox include continents, countries, Chinese provinces/cities/counties.
           Countries have values of "area" and "population".
 	-  CSV format:
 	  	-  Download address:
                        https://github.com/Mararsh/MyBox_data/tree/master/md/GeographyCode/en
 	  	-  Necessary fields:
                         Level,Longitude,Latitude
                         And "Chinese Name" or "English Name"
 	  	-  Optional fields:
                         Altitude,Precision,Coordinate System,Square Kilometers,Population,
                         Code 1,Code 2,Code 3,Code 4,Code 5,Alias 1,Alias 2,Alias 3,Alias 4,Alias 5,
                         Continent,Country,Province,City,County,Town,Village,Building,Comments
 	- Data from geoname.org:
 	  	- Download address:
                         http://download.geonames.org/export/zip/
 	  	-  Tab-delimited text in UTF8 encoding.
 	  	-  Data fields：
                           countryCode postalCode placeName
                           adminName1 adminCode1 adminName2 adminCode2 adminName3 adminCode3
                           latitude longitude accuracy
 	  	-  Coordinate system is WGS_84.
 	  	-  Same address is written only once even when it has multiple "postal code" or coordinates.
6. Settings:       
 	-  Customize colors of data rows. Provide "Default" and "Random" buttons.       

![Snap-geoCode](https://mararsh.github.io/MyBox/snap-geoCode-en.jpg)       


### Location in Map<a id="locationInMap" />
1. Query geography code by:
 	-  Click map.
 	-  Input address.       
 	  	-  TianDiTu supports chinese and foreign addresses in Chinese(like "伦敦") or in English(like "Paris")
 	  	-  GaoDe map only supports addresses in China.
 	-  Input longitude and latitude.
2. Query result can be saved in Geography Code table.       

![Snap-geoCode](https://mararsh.github.io/MyBox/snap-locationMap-en.jpg)       


### Location Data<a id="locationData" />
1. Data definition：
	-  Basical attributes: data set, label, longitude, latitude, start time, end time.
	-  Auxiliary attributes: altitute, precision, coordinate system, speed, direction, data value, data size, image, comments.
2. Data constraints:
 	-  Each location data belongs to a data set.
 	-  Data set defines common attributes of some location data, examples:
  	  	- Date format
 	  	- Whether omit "AD" for date AD
 	  	- Text color
 	  	- Image
            These attributes help to distinguish data points in map.
3. Define conditions:
  	- List of data sets. Multiple nodes can be selected.
 	- Time tree(Start time).   Multiple nodes can be selected.
4. Map data:
  	- At beginning, the first data is made as map center.
  	- Location Distribution: All data are displayed in malines between adjacent 2 points will be shown.       
                           Notice: Lines only work for China addresses in TianDiTu.       
 	  	- Control frames:
 	  	  	- Set interval.
 	  	  	- Select a frame(by start time).
 	  	  	- Pause/Continue playing.
 	  	  	- Previous/Next frame.
 	  	  	- Whether loop.
5. Snapshots:
  	- For "Location Distribution":
 	  	- html:Data and snapshot of current frame
 	  	- Snapshot of current frame. All supported image formats can be selected.
  	- For "Time Sequence", more choices:
 	  	- jpg:Snapshots of all frames
 	  	- png:Snapshots of all frames
 	  	- Animated gif:Snapshots of all frames(May out of memory)
6. Import data:
 	-  CSV format:
 	  	-  Necessary fields:       
                         Dataset,Longitude,Latitude       
 	  	-  Optional fields:       
                           Label,Address,Altitude,Precision,Speed,Direction,Coordinate System,       
                           Data Value,Data Size,Start Time,End Time,Image,Comments       
 	- Data from movebank.org:
 	  	- Download address:       
                         https://www.datarepository.movebank.org/       
 	  	-  Comma-delimited CSV file.
 	  	-  Necessary fields:       
                           timestamp,location-long,location-lat,study-name       
 	  	-  Coordinate system is WGS_84.
 	- Examples: Chinese Historical Capitals
 	- Examples: Autumn movement patterns of European Gadwalls
 	- Examples: Sperm whales Gulf of Mexico
 	- If data include a dataset which is not in database, the new dataset will be added in database automatically.       

![Snap-locationData](https://mararsh.github.io/MyBox/snap-locationData-en.jpg)       

### Location Tools <a id="locationTools" />
1. Convert coordinate value between decimal and DMS. Valid examples of DMS:       
 	  	 	  	48°51'12.28"       
 	  	 	  	-77° 3' 43.9308"       
 	  	 	  	48°51'12.28"N       
 	  	 	  	2°20'55.68"E       
 	  	 	  	S 34° 36' 13.4028"       
 	  	 	  	W 58° 22' 53.7348"       
 	  	 	  	118度48分54.152秒       
 	  	 	  	-32度04分10.461秒       
 	  	 	  	东经118度48分54.152秒       
 	  	 	  	北纬32度04分10.461秒       
 	  	 	  	西经118度48分54.152秒       
 	  	 	  	南纬32度04分10.461秒       
2. Convert coordinate values as other coordinate systems.       

![Snap-locationTools](https://mararsh.github.io/MyBox/snap-locationTools-en.jpg)       

### Epidemic Reports<a id="epidemicReport" />
1. Data definition：
	-  Basical attributes: dataSet, time, location, source.
	-  Basical values: confirmed, healed, dead.
	-  Subtraction statistic: increased confirmed, increased healed, increased dead.       
           Calculated by adjacent rows.
	-  Division statistics:
	  	- healed/confirmed permillage,  dead/confirmed permillage
	  	- confirmed/population permillage, healed/population permillage, dead/population permillage,
	  	- confirmed/area permillage,  healed/area permillage, dead/area permillage       
            When value of "area"/"population" of location is invalid(zero or negitive), corresponding statistics data are meaningless.       
            Predefined data "countries" have valid "area"/"population" and they have meaningful statistics values.       
	-  Accumulation statistics:
	  	- Values of some countries       
                   Calculated by values of country's provinces.       
	  	- Values of continents       
                   Calculated by values of continent's countries.       
	  	- Values of Earth       
                   Calculated by values of continents.       
2. Data constraints:
 	- Not null values: dataSet, time, location
 	- Values of "source": "Inputted data", "Predefined data", "Filled data", "Statistics data".
 	- "location" is foreign key of "Geography Code", which must have row defined in that table.
 	- In "confirm", "healed", "dead", at least one should be larger than zero.
	- One of following can determine a data row:       
 	  	- Match id, which is assigned by MyBox automatically. This is accurate matching.
  	  	- Match "dataSet" + "date" + "location". This is accurate matching.
	- This version assumes that only one valid data in each day for same dataSet plus same location.
3. Edit data：
 	-  When in single data, location is set by selecting node from locations tree.
 	-  In interface of "Epidemic Reports of Chinese Provinces" or  "Epidemic Reports of Countries", multiple rows can be inputted for same dataSet and time.
 	-  Modify values of "source" for selected data rows.
4. Import data:
 	-  Embedded predefined data in MyBox: COVID-19 historical data from Johns Hopkins University.(Till 2020-09-24)
 	-  CSV format:
 	  	-  Download address:       
                         https://github.com/Mararsh/MyBox_data/tree/master/md/COVID19/en       
 	  	-  Necessary fields:       
                           Data Set,Time,Confirmed,Healed,Dead       
                   And location data which are enough to define a geography code:       
                           Longitude,Latitude,Level,Continent,Country,Province,City,County,Town,Village,Building,Point of Interest       
 	  	-  Optional fields:       
                           Increased Confirmed,Increased Healed,Increased Dead       
	  	-  Coordinate system is CGCS2000.
 	-  COVID-19 historical data from Johns Hopkins University(Global) :
  	  	-  Download address:       
                           https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series       
 	  	-   Necessary fields:       
                           Province/State,Country/Region,Lat,Long       
                   And date list like "1/22/20,1/23/20..."       
	  	-  Coordinate system is WGS_84.       
	  	-  Australia, Canada and China are reported at the province/state level, and others are at country level.
	  	-  Items whose values are all zero will be skipped.
 	-  COVID-19 daily data from Johns Hopkins University(Global) :
  	  	-  Download address:       
                           https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_daily_reports       
 	  	-  Data fields are change as time flowing...       
                    Following is format of "01-22-2020.csv":       
                             Province/State,Country/Region,Last Update,Confirmed,Deaths,Recovered       
                    Following is format of "05-15-2020.csv":       
                             FIPS,Admin2,Province_State,Country_Region,Last_Update,Lat,Long_,Confirmed,Deaths,Recovered,Active,Combined_Key       
	  	-  Coordinate system is WGS_84.
	  	-  Items whose values are all zero will be skipped.
 	-  Option: Statistics against dataset.
 	-  Time of all data are changed as “23:59:00”.
 	-  If data include a geography code which is not in database, the new geography code will be added in database automatically.
5. Statistics data:
 	-  Option to accumulate date
 	-  Option to calculate subtraction statistic for different location levels.
6. Define Conditions:
 	-  “Data Sources Tree”:  Data sets and their different sources are organized a tree.  Multiple nodes can be selected.
 	-  “Locations Tree”:  All geograhy codes in MyBox are organized as a tree by their subordination relationship. Multiple nodes can be selected.
 	-  “Times Tree”:  All times involved in Epidemic Reports of MyBox are organized as a tree. Multiple nodes can be selected.
 	-   "Number of Top Data in Each Day":
 	  	- Unlimit. Charts will not be displayed. Data are queried as condition.
 	  	- Valid value:
 	  	  	- Data are queried as condition, and then be truncated as top data of each day, by which charts and data are displayed.
 	  	  	- "Time Descending" is always as the first ordering element automactically.
 	  	  	- At least one more column should be picked as ordering element.
 	  	  	- Beside "Time Descending", the first ordering element is called "major quering attribute".
        -  Number of Top Data in Each Day" and "Elements of ordering" work for Query and Export, and not for Clear.
 	-  Edit condition: title, where, order by, fetch, "Number of Top Data in Each Day"(0 or -1 means Unlimit), which are combined together as the final condition.
7. Display charts:
 	-  Only when query condition satisfies requirements, charts are displayed. Charts' data are always "Daily top data" and have "Major query attribute".
 	-  Beside "Major query attribute" , more attribues can be selected, to display multiple dimension data in same chart, or show multiple charts at same time.
 	-  Chart type: horizontal bars, vertical bars, horizontal lines, vertical lines, pie, map.
 	-  When there are  multiple times in data，charts are animated. Data charts of each time are displayed frame by frame in time ascending.
 	-  For animated charts, support Pause/Continue, Jump to frame of a time, Last frame, Next frame, setting interval.
 	-  Common settings, which take effect immediately：
 	  	- Legend location: not display, top, bottom, left, right.
 	  	- Value's label: name and value, value, name, not display, pop
 	  	- Whether display: category axis, horizontal grid lines, vertical grid lines
 	  	- Number axis: Cartesian Cordinate, Square Root Cordinate, Logarithmic E Cordinate, Logarithmic 10 Cordinate.
 	  	- Font size
 	  	- Parameters of map: level, layers, language
 	-  Snap chart.
 	  	- Snapshot of current frame. All supported image formats can be selected.
 	  	- jpg:Snapshots of all frames
 	  	- png:Snapshots of all frames
	  	- Animated gif:Snapshots of all frames(May out of memory)
8. Settings:
 	-  Snap dpi, maximum width of snapped animated images, time to loading chart's data.       
            These parameters are related to memory usage and computer's calculation capacity.       
 	-  Customize colors of data rows as column "source". Provide "Default" and "Random" buttons.
 	-  Customize colors of data values in charts. Provide "Default" and "Random" buttons.
 	-  Customize colors of location values in charts. Provide "Random" buttons.       

![Snap-epidemicReport](https://mararsh.github.io/MyBox/snap-epidemicReport-en.jpg)       

### Other<a id="dataOthers" />
1. Create Barcodes
 	-  Supported 1-d barcodes: 
 	  	- Types: Code39, Code128, Codabar, Interleaved2Of5, ITF_14, POSTNET, EAN13, EAN8, EAN_128, UPCA, UPCE, Royal_Mail_Customer_Barcode, USPS_Intelligent_Mail       
 	  	- Options about 1-d barcodes: Orientation, width/height, dpi, text location, font size, quiet-zone width, etc.
 	-  Supported 2-d barcodes: 
	  	- Types: QR_Code, PDF_417, DataMatrix
	  	- Options about 2-d barcodes: Width/height, margin, error correction level, compact mode, etc.
	  	- A picture can be shown in center of QR_Code. Its size can be adjusted automatically according to error correction level.
 	-  Examples of parameters and suggested values.
 	-  Validate generated barcode at once.
2. Decode Barcodes
 	-  Supported 1-d barcodes: Code39, Code128, Interleaved2Of5, ITF_14,  EAN13, EAN8, EAN_128, UPCA, UPCE
 	-  Supported 2-d barcodes: QR_Code, PDF_417, DataMatrix
 	-  Display contents of barcodes and its meta data including barcode type and error correction level if any.
3. Message Digest
 	-  Create digest for files or inputted texts.
 	-  Support MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512/224, SHA-512/256, SHA3-224, SHA3-256, SHA3-384, SHA3-512.
 	-  Ouput: Base64, Hexadecimal, Formatted hexadecimal.
4. Encode/Decode Base64
 	-  Encode file or texts as Base64.
 	-  Decode Base64 file or Base64 texts.
 	-  Set charset for texts.
 	-  Output as file or texts.
5. Extract ttf files from ttc file.

## File Tools <a id="fileTools"></a>

### Manage Files/Directories <a id="directoriesArrange"></a>
1. Find, Delete, Copy, Move, Rename.
2. Sychronize directories, with options like whether copy sub-directories or new files, whether only copy modified files after specific date time, whether keep attributes of original files, or whther delete non-existed files/directories under original directory.
3. Arrange files and reorganize them under new directories by modifed time. This tool can be used to handle lots of files which need be archived according to time, like photoes, screenshots of games, or system logs.
4. Delete all empty sub-directories under directory.
5. Delete "infinite-nested directory" which is created by bug of some softwares, like MyBox v6.0, and can not be deleted by normal way.
6. Delete files under system temporary path.


### Archive/Compress/Decompress/Unarchive<a id="archiveCompress" />
1. Archiving is the process to collect multiple files/directories as single file. Some archive formats like zip and 7z implement compression too. Unarchiving is the inverse process of archiving.
2. Compressing is the process to reduce size of single file. Generally it happens after archiving. Decompressing is the invert process of compressing.
3. Supported archive formats include zip, tar, 7z(Only support some algorithms), ar, cpio.
4. Supported unarchive formats include zip, tar, 7z(Only support some algorithms), ar, cpio, arj, dump.
5. Supported compress formats include gzip, bzip2, xz, lzma, Pack200, DEFLATE, snappy-framed, lz4-block, lz4-framed.
6. Supported decompress formats include gzip, bzip2, xz, lzma, Pack200, DEFLATE, snappy-framed, lz4-block, lz4-framed, DEFLATE64,  Z.
7. When unarchive/decompress, the formats can be defected automatically meanwhile user can choice the formats since some formats can not be defected.
8. When unarchive, files tree is displayed to help user select which to be extracted.

### Check Redundant Files<a id="filesRedundancy" />
1. Find duplicated files according to MD5.
2. Files tree is displayed to help user delete redundant files.
3. Deletion can be executed while checking is running.

### Others <a id="fileOthers"></a>
1. Split file, by files number, by bytes number, or by start-end list.
2. Merge files.
3. Compare files(bytes).
4. When operate in batch, select files by extension, file name, file size, or modified time. Regular expression is supported.

## Media Tools <a id="MediaTools"></a>

### Play Videos/Audios<a id="mediaPlayer" />                   
1. Create/load playlist
2. Options:  auto-play,  display milliseconds, loop number, random order
3. Set: volumn, speed(0~8 times)
4. Buttons: Play, Pause, Stop, Next, Previous, Media-info, Mute, Full-screen
5. When full screen, display controls in short duration by clicking screen, and quit full-screen by ESC
6. Support muxer format: aiff, mp3, mp4, wav, hls(m3u8), video codec: h.264/avc, audio codec: aac, mp3, pcm.       
    If video does not sound, this is due to unsupported audio codec.       
    Known issue: MyBox may quit when play some media stream.       
7. Sounds of GuaiGuai and BenBen
8. This tool need not ffmpeg.  But libavcodec and libavformat are required on Linux, and refer to:       
https://www.oracle.com/technetwork/java/javafx/downloads/supportedconfigurations-1506746.html       

### Manage Playlist <a id="mediaList" />
1. Create/Delete/Update playlists.
2. Add/Delete items in playlists.
3. Read medias information, including duration, audio encoding, and video encoding.       

![Snap-mediaPlayer](https://mararsh.github.io/MyBox/snap-mediaPlayer-en.jpg)       

### Wrap Functions of ffmpeg<a id="ffmpeg" />
Notice: This set of functions is based on ffmpeg, and user need download ffmpeg by yourself. (Suggest to use static version)        

1. When handle media:       
	-  All parameters can be selected/set, including format, codec, subtitle, frame rate, sample rate, change volumn, etc.
	-  Button "Default": When NVIDIA is available, chooce "h264_nvenc" as video encoder, to make use of hardware acceleartion.
	-  Most of players support: muxer "mp4", video codec "H.264", audio codec "AAC".
	-  Try different options of encoders, preset, and CRF, to get suitable settings of your computer, as following factors :
 	  	- The encoder should be fast enough to record without frames dropping.
 	  	- Consume limited system resources, and leave enough CPU and memory for other applications.
 	  	- Quality and size of the generated file are tolerable.
2. Record Screen:
	-  When have not NVIDIA and CPU is not so powerful:
 	  	- Choose "libx264rgb" as video encoder, to bypass conversion from RGB to yuv444p.
 	  	- Select quicker encoding preset.
 	  	- After recording, use conversion tool to change generated video from RGB to yuv444p with encoder "libx264".
	-  Select whether record video：
 	  	- Set size of threads queue.
 	  	- Record area: full screen, window by title, rectangle.
	-  Select wether record audio:
 	  	- Detect audio cards automatically, and pick the first one as the audio device.
 	  	- Set size of threads queue.
	-  Set delay:
 	  	- If "unlimited", record at once when user click button "Start".
 	  	- If valid value, recording will start when this time is past.
	-  Set duration:
 	  	- If "unlimited", record untill user click button "Stop".
 	  	- If valid value, recording will end when duration completes. User can click button "Stop" to finish recording at any time.
3. Convert videos/audios in batch:
	-  Source files are listed as files/directories.
	-  Source files are listed as streams and medias information.
4. Combine images and audios as video：
	-  Source files are listed as files/directories.
	-  Source files are listed as streams and medias information.
	-  Duration can be set for each image, or set for all images.
	-  Option: End video when audios finish.
	-  Images are adjusted automatically to fit for screen size meanwhile keep width-height ratio.
5. Read media information like format, audio stream, video stream, frames, packets,  pixel formats by ffprobe.
6. Read information of ffmpeg, like version, supported formats/codecs/filters, and query with customized parameters.       

![Snap-makeMedia](https://mararsh.github.io/MyBox/snap-makeMedia-en.jpg)       


### Game-Elimination<a id="gameElimination" />
1. Options about chesses images, number, size, effects like dropshadow or arc.
2. Chesses can be predefined images, user defined images, or colors.
3. Sound: praise from GuaiGuai, praise from BenBen, 3-conection from Ben and others from Guai, mute, or any mp3/wav file.
4. Counted chesses: Make scores only when eliminate selected types of chesses.
5. Customize rulers that how to give score when eliminate different type of connection.
6. Set strategy when deadlock happens: keep score and renew game, make chance of elimination, or pop alert to have user chooce.
7. Options: speed of automation, times of flush when eliminate, whether pop scores.
8. Button "Help Me": prompt valid step.
9. Button "Play Automatically": click to play by compute and click again to stop it.       

![Snap-game](https://mararsh.github.io/MyBox/snap-game-en.jpg)       

### Game-Mine<a id="gameMine" />
1. Set grid size and number of mines. Examlpes.
2. Help to see mines.
3. Recover game when trigger mine.       

![Snap-game](https://mararsh.github.io/MyBox/snap-mine-en.jpg)       


### Others<a id="mediaOthers" />
1. Alarm clocks, including options of time and music. Support rings of “Meow”, wav files, and mp3. Can run in background.       


## Network Tools <a id="netTools"></a>

### Download Web Pages<a id="downloadFirstLevelLinks" />
1. List first level links for given web address.
2. Download web pages of selected links:
	-  Select useful links. Nonsense links can create unwanted files and disturb final path index.
	-  Use function "Set subdirectory name" to make subdirectory's name reasonbale.
	-  Use functions "Set link name/title/address as file name" to make filenames meaningful.
	-  Use function "Add order number before filename" to help filenames ordered.       
       MyBox can order names like "xxx9", "xxx36", "xxx157" correctly.       
3. Options: Rewrite links in pages, Generate path index, Change pages' encoding, Merge as texts/html/Markdown/PDF.
4. Page style and PDF font files can be set.

### Weibo Snaping Tool <a id="weiboSnap"></a>                     
This tool fails to work now.                     

1. Save Weibo pages of any months of any Weibo accounts automatically. Pages which the account liked can be snapped and saved too.
2. Set the months range.
3. Make sure whole page contents loaded. Can expand the comments and pictures in the pages.
4. Save the pages as local html files which can not be loaded normally due to dynamic loading of WeiBo contents. They can be used to extract texts in the pages.
5. Save the pages' snapshots as PDF files, with options like dpi, format, page size, margins, author, etc.
6. Save all original size pictures in the pages.
7. Display progress information in time.
8. Stop the progress at any time. The interrupted month will be record and input as start month for next execution.
9. Set the retry times of failure.
10.  Initialize webview when run at first time. Click button "SSL" if miss this step.       

![Snap-weibo](https://mararsh.github.io/MyBox/snap-weibo-en.jpg)       


### Web Browser<a id="webBrowser" />
1. Display pages in multiple tabs
2. Manage web histories
3. Manage web favorites

![Snap-webBrowser](https://mararsh.github.io/MyBox/snap-webBrowser-en.jpg)       


### Query Address<a id="queryAddress" />
1. Query URL/host/IP
2. Select: local information,  query of ipaddress.com, query of ip.taobao.com
3. View/Save SSL certifcate.

### Query DNS in batch<a id="queryDNS" />
1. Input hosts/ips list.  Example is provided.
2. Open file "hosts".
3. Execute command to refresh DNS.


### Encode/Decode URL<a id="encodeDecodeURL" />
This tool helps to convert a string from/to the application/x-www-form-urlencoded MIME format.
The following rules are applied for encoding:

-  The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
-  The special characters ".", "-", "*", and "_" remain the same.
-  The space character "   " is converted into a plus sign "+".
-  All other characters are unsafe and are first converted into one or more bytes using some encoding scheme.
   Then each byte is represented by the 3-character string "%xy", where xy is the two-digit hexadecimal representation of the byte.       

Decoding does reverse conversion.       


### Manage Security Cerificates <a id="securityCerificates" />
1. Read certificates in any keystore/truststore, and export as html file.
2. Add/Read cerificates in any CA files.
3. Download and install cerificates of any websites.
4. Delete certifcates in keystore/truststore.
5. Backup keystore/truststore automatically when update.


## Development Tools<a id="devTools" />
1. Open/Close monitor bar of Memory.
2. Open/Close monitor bar of CPU.
3. MyBox Attributes
4. MyBox Logs：
	-  Types of MyBox logs: Error, Info, Debug, Console.
 	  	- All logs will be displayed on console.
 	  	- "Error" and "Info" are always written in database.
 	  	- "Debug" are written in database only in Dev Mode.
 	  	- "Console" are never written in database.
	-  Fields: ID, time, type, file, class, method, line, callers, comments.
	-  "Callers" is the calling chain. Each line is one node and records: file, class, method, line. Calling chains only include methods of MyBox itself.
	-  "Error" will cause interface of MyBox Logs Viewer is popped.
5. Run system commands.
6. Start JConsole(Java Monitoring and Management Console)
7. Manage languages.
8. Make icons.
9. Edit data in MyBox internal tables.
10. Automatical testing: Open interfaces.  
11. Send message to author

## Settings <a id="settings"></a>
1. Interface:
	-  Language, font size, icon size
	-  Control color, whether display control text, interface style
	-  Select hi-dpi icons(100x100) or common icons(40x40).       
           When screen resolution is not high than 120dpi, suggest to use common icons.  Hi-dpi icons may look blurred on low-resolution screen.
	-  Whether restore last size of each scene.
	-  Whether open new stage to display scene.
	-  Whether show/Hide splitted panes when mouse passing.
	-  Whether pop stage "Set Color" when mouse passing.
	-  Font size, color, duration of popped messages.
2. Base:
	-  Maximum memory usage of JVM
3. Data:
	-  Data path
	-  Derby driver mode
	-  Whether pop recent visited files/directories. And the number.
	-  Whether close alarm clocks when exit program.
4. PDF tools:
	-  Maximum main memory of PDF handling.
5. Image tools:
	- Width and color of stroke and anchor. Whether anchors are solid.
	- Color to replace Alpha when Alpha is not supported. (Suggest as White)
	- Wdith of thumbnail
	- Maximum width to display sampled images
6. Map:
	-  Data keys of map
7. Clear personal settings.
8. Open data directory.

## Window <a id="windows"></a>
1. Refresh/Reset/Full-screen/Top windows.
2. Close other windows.
3. Reboot MyBox.
4. Recent visited tools.
5. Snapshot of window/pane.           

## Helps <a id="helps"></a>
1. MyBox shortcuts:
	- When focus is in "Text Input" control, Delete/Home/End/PageUp/PageDown/Ctrl-c/v/z/y/x work for texts in the focus.  Or else shortcuts work for interface.</BR>
	- When focus is not in "Text Input" control,  Ctrl/Alt can be omitted. Example, if an image is currently foused, press "c" to copy and press "2" to set as pane size.
2. Functions list
3. ReadMe
4. Documents：Help user start download tasks. If MyBox documents are put into data path, MyBox will find them automatically.
5. About

# Development Logs <a id="devLog"></a>           
2022-4-3 v6.5.4         

* Information in tree. Manage nodes of tree. Edit nodes. Export/Import with tags. Output tree view. Examples. 
Extend as: Notes, Web favoraite addresses,  JShell codes, Javascript codes, SQL codes.                
* Improve. Execute multiple statements in JShell. Manage and execute queries for data tables. 
"When left click link or image" of web page has more options. Pop histories for input controls.                      
* Solved. Options of "Word" and "html" for Image OCR in batch do not work. Error popped when save new html in editor. 
Nothing returned when query time in tree. Wrong may happen when delimited identify is defined in data table. "Auto save" not work in File Editor.       

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.4)             
                   

2022-3-6 v6.5.3         

* Data. Convert data to database table. Manage and edit database tables. Execute database SQL statements.                        
* Calculation. GUI of JShell(Java interactive coding tool).            
* Dev. Edit data in MyBox internal tables. Start JConsole(Java Monitoring and Management Console).              
* Solved. "Extract images/links in html" fails. "File unarchive" makes wrong file-paths. 
"Synchronize Directory" will loop endlessly when target path is included in source path. Missed input field for "Power of matrix".        

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.3)             

2022-2-1 v6.5.2         

* Image. Option to show grid lines. Option to share scopes with all images. Filter color. Wirte multiple-lines texts in image with background and borders.
Rendering parameters when save or modify image.                  
* Data. Select data and parameters to generate Bar/Line/Pie/Scatter/Bubble/Area Chart.                                    
* Interface. Handle all data if select none.                     
* Codes. Pop menus are changed as child windows. Images are always handled as ARGB internally. Automatical testing: Open interfaces.
Reduce duplicated codes of editing data.                
* Solved. Handle non-alpha images(like jpg) wrongly when add texts in batch. Image is not updated after restored from backup. 
Several functions do wrongly when handle data in multiple pages. Menu fails to pop when name includes special characters.                 
Shortcuts may be triggered when input texts with pop menu being opened.                                    
 
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.2)             
   
Best wishes in Chinese Tiger Year!                      

2021-12-30 v6.5.1         

* Edit Table. Start editing when single click. Submit changes when lose focus. Validate types. CheckBoxs of rows.          
* Edit Data. Table/Texts are major/assist edit modes, and sheet mode is expired.  Consistent interface to manage types of data. 
Some logic are moved from controllers to objects.                           
* Image. Improve methods of reading image files. Loading thumbnails in background. Import example palettes in color picking window.                       
* Interface. Html/window style can be shared in all interfaces. Options of Find/Replace can be shared in all interfaces.  Snapshot of current window/node.                                 
* Solved. Can not input new lines in Notes.  "Crop" is always disabled in Image Manufacture. No extension filter is provided when save image as new file.
Long file names can cause histories menu or file's backing fail.  Panes of Texts Editor and Bytes Editor can not be resized.                                       
 
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.1)            

2021-10-13 v6.4.9         

* Improve. Text editor implements pagination as lines number and counts each line break as one character. 
Timestamp is appended in target filename for batch operations.        
* Add. Extract table data from html. Tabs can be closed in html/notes editor. Convert word/ppt/text as PDF.                
* Platform. Upgrade to java17 and javafx17. MyBox can start from path with non-ascii name. Media can be played normally on Linux.          
* Solved. Text/Bytes Editors read/write wrongly. Image scope is not displayed and fail to save. Inputting text may trigger shortcuts. Many functions fail to work.                           
      
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.9)            
   
2021-9-21 v6.4.8         

* Data file: Optimize operations of sheet. Data text can be edited. Support text data file.  Data clipboard can be saved and managed.                     
* Popped text/image/html can be synchronized with source. Query Color.  Images list supports PDF and PPT.        
* Solved problems. OCR of PDF fails to update. Stack overflow when pop context menu for matrix.  Fail to open file without suffix on Linux.                     
       
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.8)         

Happy Moon Festival!              

2021-8-17 v6.4.7         

* Improve interfaces. Consistent context menus of image/html/text. Better clipboards of image/text/data. Easier editor of Markdown/html/note/image. Integrated PDF viewer.      
* Optimize codes. Split big classes. Adjust shortcuts. Correct colors of icons.          
* Solved problems.  Very slow reading of jpg file. Transparency does not work for pen and eraser in Image Manufacture.            

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.7)         

2021-7-7 v6.4.6         

* Add. Text in System Clipboard. Text in MyBox Clipboard. Context menu of all text input controls. Context menu of html.       
* Improve and fix. Text Editor. Clean of closed windows.          
* Solved problems.  Can not open microsoft documents(word/excel/ppt). Page number is empty in data tables.            

[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.6)        
    
2021-6-24 v6.4.5       

* Add. View and convert Word files. View/convert/split/merge/extract PPT files. Edit images list. Play images list/PDF/PPT. Images in MyBox Clipboard. Paste image in batch.              
* Improve and fix. Image in System Clipboard. Text Editor. Bytes Editor.          
* Solved problems.  Error when merge html as PDF. Error When save splitted images. Chinese can not be inputted in Markdown Editor when update synchronously.      
 
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.5)        

2021-5-15 v6.4.4       

* Improve and fix Html Editor, Markdown Editor, and Web Browser. 
* Add: Manage Web Favorites, Query Web Histories, Extract Texts From Excel/Word/PowerPoint/Publisher/Visio, Functions List.       
* Upgrade to Java16 and javafx16.       
* Solved problems. Texts are stuck when input Chinese in Data Clipboard. Wrong results when convert CSV/Excel to PDF. Error interface for exporting data. Frameset is override when save html frame.       
  
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.4)        

2021-4-16 v6.4.3              
- Html Editor. Frame can be selected for frameset. Images list. Query elements by tag/id/name. Search string in page.       
- Merge colors/palettes management. Multiple palettes can be defined. Same color can have different names and orders in different palettes.
Improve interface of picking colors.       
- Notes. Search by title/contents/time. Whether query sub-notebooks. Copy notebook. Copy/move notes.       
- Merge data text/sheet clipboard.  Apply blend mode when mouse released in Image Manufacture-Pen.       
- Add. Query address by host/ip/url. Query DNS in batch. Run system command. Encode/Decode Base64.       
- Remove. Not install certificates for map and weibo. No bypass of SSL verification. No expired links in Epidemic reports. No invalid links in notes examples.       
- Solved problems.  New cert entry fails into keystore. Abnormal of some interfaces/functions when run at first time. Should do backup before some operations.       
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.3)       

2021-3-21 v6.4.2       
- Notes. Set tags of note and query notes by tags. NoteBook can be moved. Edit note in Rich Text. Set note's style.     
Export notes in single page, html frameset, and xml and charset and style can be set for exported files.                       
- Image Manufacture. "Text" and "Pen" support blend modes.                            
- Add Data Sheet Clipboard. Delimiter can be set for data in Data Text/Sheet clipboard.                        
- Solved problems.  Html Editor fails to save. Links in frameset should be handled. Download First Level Links can not handle links with special characters. File name should avoid blanks.                        
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.2)            

2021-3-8 v6.4.1
- Notes are information pieces in html format. Notebook is collection of notes and notebooks. Notebooks are organized as a tree. Examples are provided.                 
- Web page. Menu of handling links. Buttons to edit html codes.                    
- Data files. Merge csv/excel files.  Handle empty csv/excel file. Add/remove/rename excel sheets.            
- Improve. All editors support backing up automatically when save file. Image Manufacture supports square root of color distance and refresh clipboard automatically.
Remove unnecessary files in self-contain packages.                    
- Solved problems. Download First Level Links fails for some addresses. OCR in batch fails when file name includes non-English. Deleting history fails in Web Browser.                
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.1)                          
            
2021-2-11 v6.3.9                  
-  Improve: Frame can be selected when view/manufacture image. Always use iterator when read CSV/Excel file. Clear security attributes of PDF file.          
-  Solved problems.  Always go back to page 1 after CSV/Excel file is saved. Fail to unarchive file.  Non-english entries of zip file are messed.             
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.9)                     
Happy Spring Festival!  Best wishes in Year of the Ox!                

2021-1-27 v6.3.8                  
-  Add: Edit and convert csv file and excel file. Data clipboard. Edit and save matrix. Set html style in batch.               
-  Improve: Channels' weights can be set for image quantization. Shortcuts can omit Ctrl/Alt when focus is not in "Text Input" controls.  Stream reading when convert/export data.                       
-  Solved problems. Distance not work when replace images' color in batch. User password and owner password are messed for PDF.  
Pagination not work in text filter interface. Table labels need translation too. BC dates are parsed incorrectly in time tree.        
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.8)         

2020-12-03 v6.3.7                   
-  Add: Extract ttf from ttc. Embed an open source ttf file.                     
-  Improve: high dpi icons and common icons can be selected.  Set html charset in batch.  Consistent control to select PDF font file. 
Rendering parameters can be set when scale images. Set default video decoder on mac as VideoToolBox.             
-  Solved problems. Source file is destoryed when decompress gzip file and it becomes larger and larger.  Charset of html may be parsed incorrectly. 
Only one line is shown in right pane when edit bytes.                          
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.7)             

2020-11-29 v6.3.6               
-  Codes: Upgrade to java 15.0.1 and javafx 15.0.1(Except for mudole "javafx-web").  MyBox logs are managed by itself and does not depend on log4j2 now.             
-  File: Improve robustness of files' deleting, renaming, and moving.  New function to delete files under system temporary path.           
-  Color: Results of quantization can be imported to Color Palette.  Maximum loop can be set for Kmeans cluster quantization.  
User's CSV file can be imported in Color Palette.  "rgb" can be key to imprt colors.                  
-  Major issues solved: Errors pop when visit  histories include deleted files.  Wrong happens when set column number for combining images.                  
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.6)          
  
2020-11-18 v6.3.5        
-  Improve: Handle regular expression in pages for document find/replace; Opitimize algorithms of image reading; Deal big image more smoothly; 
Adjust interface of Html Editor.      
-  Add: Each version has itself's configuration file; Menu buttons; Document convertors; Encode/decode URL; Download first level links.      
-  Remove: Can not change derby mode when start the tool; Delete function "Manage downloads".         
-  Major issues solved: Document find/replace handle pages incorrectly; Source files should not be loaded for images list;      
Infinite empty directories can happen in batch functions; Wrong in interface of Image Manufacture.         
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.5)          
Thanks helps from [beijingjazzpanda](https://github.com/Mararsh/MyBox/issues/781).        

2020-10-11 v6.3.4        
-  Editors: Find/Replace by multiple lines, case-insensitive, from cursor, and with examples of regular expression; Pop doument; Close/Open right pane; Set whether update right pane synchronously.     
-  OCR: Base on tesseract commandd line, support win/linux/mac, and compatible with verison 3/4/5; Set psm and all parameters, and provide parameters list.     
-  Record screen: Support mac; Miaow when start and end; Set frame rate and bitrates by default.          
-  Game "Mine": Set size of grid and number of mines, and provide examlpes; Help to see all mines; Recover game when trigger mine.        
-  Major issues solved: Locate regular expression incorrectly for Find/Replace in Editor; Incorrect logic in version migration.            
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.4)                

2020-9-27 v6.3.3        
-  Image Manufacture: Show/Hide scope pane and image pane; Enlarge image size when pasted clip out of bound; Demo of blend modes; Parameter "invert" for convolution kernel.     
-  Data Tools: Helps to input CSV; Time with milliseconds or zone; Display points one by one in map; Set of snap images for Epidemic Report.     
-  Color: User can input colors list; Separated interfaces for setting color, picking color, and managing palette; Adjust colors order in palette.     
-  Others: Context menu for table and image; Auto-save periodically in editors; Delay for Screen Recorder; Screen Recorder on Linux; Dev Mode.        
-  Major issues solved:  "Clear personal settings" is to delete user configuration data instead of all data; Wrong statements for some tabes;  Shortcuts do not work on Linux; Location Data can not be created for new Data Set.        
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.3)          
 
This version is for China. Happy Birthday!                 
      
2020-8-27 v6.3.2  Improve "Location Data". Display data in map as "Location Distribution" or "Time Sequence". 3 examples of datasets. Data files on movebank.org can be imported.       
Improve how to display data in map. Support both TianDiTu and GaoDe map.                    
"Location Tool": Convert coordinate value between decimal and DMS. Convert coordinate values as other coordinate systems.                  
FFmpeg application: Record desktop. Support Windows only now.       
Import/Export color data in CSV format.         
Full automatic script of building and packing. Development Guide v2.1.                  
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.2)       
  
2020-6-11 v6.3.1  Migrate to java 14.0.1 + javaFx 14.0.1(Except for module "javafx-web") + Netbeans 11.3.        
Still support migration from lower versions instead of "Truncated Version".               
Improve table "Geography Codes" to get quicker queries.  Unit of "Area" is changed from "square kilometers" to "square meters".            
Keys of map can be set.           
Solved problems. Length of "Recently visited files" is not limited. Values in pie chart of "Epidemic Reports" should be percentage.            
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.1)        

2020-5-25 v6.3  Redesign "Geography Codes" and "Epidemic Reports".        
Provide selections of audio parameters in Audio/Vedio Convertor.           
Provide Common Chinese Colors and Common Japanese Colors.      
Solved problems.  Cursor should stay orginial location after saving in Text Editor. Option “Count dir size” may cause some batch operations fail.      
This version is for my mom. Wish all mothers loving and being loved.                     
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3)        

2020-3-3 v6.2.1  Improve "Epidemic Reports".  Node "Except for China": query and display data of countries other than China.            
Filled Data are shown in different color.  Change filled data as normal status by editing or click button "Sure".      
Support  structure of "Country-cities" whi level and is used by countries other than China.      
Solved problems. Editors of "Geography Codes" and "Epidemic Reports" work incorrectly.  May cost too much memroy when make snapshot.       
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.2.1)              
            
2020-2-28 v6.2  Improve "Epidemic Reports".  "Fill in data holes", to add missed data as previous days automatically.       
Dynamical charts. Option to set whether display values in charts. Frames duration can be set for dynamical charts/map.      
Export formats include xlsx(Excel2007) now. Auto-increased field is not exported and does not affect import.       
Solved problems. Negative coordinates are shown as empty. Statistic of Epidemic Reports are updated incorrectly. 
"Level" is missed in editor of Epidemic Report. Width setting is handled wrongly in Editor of animated gif.      
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.2)         
    
2020-2-21 v6.1.5  Fetch Data About "New Coronavirus Pneumonia". Extract latest in-time Chinese and global data in Baidu page. Query historical data of Chinese data since 2020-1-20 from TengXun api.     
Location attributes are added.  Data are trimmed in 3 formated: html、json、xml and can be imported in table "Epidemic Reports".       
Improve "Epidemic Reports".  Location data are in 4 levels: global, countries, provinces, cities/districts.  Statistic attributes of increased values and their charts. 
Examples data between 2020-1-20 and 2020-2-21.        
Improve "Grography Codes". Errors and missed are fixed.           
Solved problems: Filtering works incorrectly in editors.                   
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.1.5)         

2020-2-11 v6.1  Location in Map: Query and diaplay location data by clicking map, inputting address, or inputting coordinate. Map options like marker, text, layers, and language.      
General management base of data table: Add/Delete/Edit data; pagination; export/import; export as html.      
Manage geography codes: Query and display location in map; example of countries and Chinese provinces.      
Manage location data: Attribues of location and data set/label/value/size/time/image/comments. Support time of BC. Example of "China earlier cultures"(incomplete).      
Location data in map: Display data distribution in map. Data values can be used as map markers/texts/pops.      
Epidemic Reports: Attributes of location and epidemic data. Example of "New Coronavirus Pneumonia"(incomplete and may include wrong values). Data analysis based on dimensions of time and location. 
Time direction includes Global and China. Location direction includes countries and Chinese provinces. 
Charts: Number Bar Charts, Ratio Bar Charts, Pie Charts, and Numbers in Map based on location dimension; Number Line Charts, Ratio Line Charts, and Dynamic Numbers in Map based on time dimension.  
Data and charts can be saved as html.        
Ico file can be read/written/converted.       
Dynamic gif: More meta data are explained; display each frame in actual intervals; intervals can be set separately for each frame when edit.         
Manage colors: maintain colors library; add /remove colors in palette; display colors in simple/all columns; merged/separated columns.        
Weibo Snap tool: Pages which the account liked can be snapped and saved.       
File Tools: Delete all empty sub-directories under directory; delete "infinite-nested directory" which is created by bug of some softwares, like MyBox v6.0.        
Improve game Elimination: Chesses can be any pictures or colors; sound can be any mp3/wav file; better algorithm; button "help me" to prompt valid step; can play game automatically.          
Solved problems: "infinite-nested directory" may be generated; in "Merge images as video", durations less than 1 second are handled wrongly, and multiple-frames image is handled wrongly;
in "Edit html", snapshots are incomplete for high dpi screen; Markdown file can not be opened when run MyBox first time.      
This version is for China and her children, who are fighting against the evil disease. Looking forward to spring and flowers!      
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.1)         


2020-1-2 v6.0  Pop large image in Images Browser.    
Support translating MyBox online. Provide table with English as comparison. New language file can be shared with others. Put resource files under data path and MyBox will 
be aware of new languages.     
Game-Elimination.  Options about chesses images, number, size, effects. Sound choices.  Counted chesses: Make scores only when eliminate selected types of chesses.  
Customize rulers that how to give score when eliminate different type of connection.      
Manage download tasks. Resume break points.  Read header of address.     
Solved several bugs of Image Manufacture.        
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.0)     

2019-12-26 v5.95  Improve interfaces of batch operations with multiple tabs instead of filling one page with all of controls.      
Solved problems:  Avoid 414 error for Weibo Snap Tool; Controls' diable property is messed in interface of Image Manufacture; Format does not work for Image Manufacture in batch.        
Today honor Chairman Mao and his comrades who made Chinese people standing up.      
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.95)       

2019-12-21 v5.9  Web browser which supports multiple tabs. Its histories can be manages and SSL certifcates of websites can be installed online.      
Read certificates in any keystore/truststore and export as html file.  Add certificates from CA files or by download directly from websites.     
Play videos/audios with options like auto-play, displaying millionseconds, loop number, random order, volumn, speed,  mute, full-screen, etc. Sounds from GuaiGuai and BenBen. Manage playlists.     
Wrap functions of ffmpeg, like convert videos/audios in batch, merge images and audios as video,  read media/ffmpeg information.     
Message Digest has 12 algorithms now.     
Solved problems:  Updates should be submitted automatically when table cell loses focus; Concurrent exception popped when check files redanduncy;  Interface is frozen when add folder which holds lots of 
files; Fail to unarchive 7z in batch; Unknown files' size in zip.   
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.9)      


2019-11-18 v5.8  Upgrade to jdk13+javafx13+derby15.    
Derby database can be switched between network mode and embedded mode. Notice:  Starting/Stopping Derby network mode under some env is very slow.    
Create message digest for files or inputted texts. Support  MD5/SHA1/SHA256.    
FIles archive/compress/unarchive/decompress. Support formats include zip, tar, 7z, ar, cpio, gzip, bzip2, xz, lzma, Pack200, DEFLATE, snappy-framed, lz4-block, lz4-framed, etc.    
Check redundant files accoding to their MD5. Files tree is displayed to help user delete duplicated files. Deletion can be done as checking is running.    
Conversion between html and Markdown in batch.    
Solved problems: Some controls in interfaces do not work. Empty pages block WeiBo snap tool. 
Transparent pixels of background should be considerred too when blending. "Replace Color" of image does not work.     
Developer Guide v2.0.        
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.8)    

2019-10-26 v5.7  Edit Markdown. Conversion between html and Markdown.     
Improve algorithms of image quantization and apply to analysing image. K-Means Clustering is to calculate mostly different colors 
while Popularity Quantization is to calculate mostly occurred colors. Image data can be saved as html file.    
Operations to manage files/directories like Find, Delete, Copy, Move. Rename is made better.     
More selection mode  for batch operations like by extension, by name, by size, or by modified time. Regular expression is supported.    
Many interfaces are refined to balance layout of controls.   
WeiBo Snap Tools is fixed. Snapshots of HiDPI screen are in right resolution now.  Images are saved as temporary files to avoid out of memory.   
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.7)    

2019-10-01 v5.6 Configuration file is back to "User Home" and always there except for parameter in jar.    
For OCR, options of image preprocessing include nine enhancement algorithms, scale ratio, binary threashold, rotation angle, whether deskew, whether invert colors. 
Options of recognization include data files list and their order, whether generate data of regions/words and the levels can be set.
Options for OCR in batch include whether generate html or PDF, whether merge recognized texts.
"Fast" data files of English and Chinese are included in MyBox and OCR can be done out of box on Windows.    
13 types of 1-d barcodes and 3 types of 2-d barcodes can be created. Options supported. Picture can be added in QR_Code.    
9 types of 1-d barcodes and 3 types of 2-d barcodes can be decoded.     
One clicking to show examples of image manufacture.        
Color Palette: Colors can be named; Display more data like cmyk values and cie values.    
Happy Birthday, China!    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.6)    


2019-9-19 v5.5  Recognize texts in image and PDF(OCR) based on tess4j. Rectangle can be set for single image's OCR. Color space and density can be set when do OCR for PDF files in batch. Currently only Windows is supported and users need download data files by themselves.    
Make self-contain packages for each platform(Window/Linux/Mac).      
Improve codes: Build with maven without Java 8; make self-contain packages with latest jpackage tool.    
Fix bugs: WeiBo Snap tool failed to work in last version; and it never worked again on Mac after it ran first time; clicking links caused MyBox dead on Linux; normalization is unnecessary when calculate CIELuv and CIELab.     
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.5)    

2019-9-15 v5.4 Use "Execution Path" instead of "User Path" as default "Data Path". Record base parameters in configuration file.   
User can modify base parameters on line, including maximum JVM memory usage, whether close dpi-aware, data root path, which will cause MyBox reboot itself.   
Based on pdf2dom, view PDF pages in html mode and convert PDF files as html files.   
Refine interface of Image Manufacture as "Visible As Need": Left-right areas like curtain, vertical accordion menus, tabs to switch targets, more details of hiding/showing/adjusting in function areas.   
Image Clipboard: multiple sources to be pasted; drag pasted image to adjust its size and location; blend mode; rotation angle. Example clips are provided.   
Color Palette: size of thousands; provide 139 named colors; export as html; pick colors on current image, image history, or reference image.   
New scope type "Outline" for image manufacture: extract outline of image which has transparent background as scope of operation. Example outlines are provided.   
Scopes can be saved and managed.   
Uniform shortcuts whose help page is provided.    
Improve codes: use public APIs instead of interval classes; make sure singleton task enters exclusively and quits cleanly; write temporary file to avoid destorying original file in case of exception.   
Fix bugs: 3 tools fail to work in v5.3 due to modification; shadow and 3 blend modes miss special handling of transparent pixels.   
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.4)    

2019-8-8 v5.3  Migrated on Netbeans 11 + Java 12.    
Improve batch interface: add directories, extend directories, filter file names, handle duplicated file names.     
Improve image conversion: more color spaces, external ICC profile as color space, whether embed ICC, handle transparent channel.    
Improve image matedata decoding: fetch embedded ICC in image.    
Improve codes: reduce duplicated codes by anonymous classes and embedded fxml; adjust classes inheritance; build project for different platforms.     
First version of "Development Guide".     
Fix issues: Algorithm for "Image Manufacture-Color-Opacity-premultiply" is incorrect; Fail to save image when file extension is missed on Linux;     
Links do not work on Linux; Version of ICC profile is decoded/encodes incorrectly; Interface may be froozen when ICC holds too much data;     
Not-decoded data in ICC prevents generation of XML.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.3)    
    
2019-6-30 v5.2 Improve image decoding. Adobe YCCK/CMYK jpg file can be viewed.     
Attributes and meta data of all images in multi-frames image file are read and displayed.    
PDF file can be viewed with bookmarks(Table of contents) and thumbnails.     
PDF file's attributes like author, version, user password, user permissions, and owner password etc can be modified.    
Matrices Calculation. Edit matrix data: adapt formatted data; transform matrix data into row vector, column vector, or another matrix in defined column number;    
generate identify matrix, random matrix, random square matrix automatically with defined column/row number.     
Unary matrix calculation: Transpose, Row Echelon Form, Reduced Row Echelon Form, Determinant By Elimination, Determinant By Complement Minor, Inverse Matrix By Elimination, Inverse Matrix By Adjoint, Matrix Rank, Adjoint Matrix, Complement Minor, Normalize, Set Decimal Scale, Set As Integer, Multiply Number, Divide By Number, Power.     
Binary matrices calculation: Plus, Minus, Hadamard Product, Kronecker Product, Horizontally Merge, Vertically Merge.     
Tools of Color Space: draw Chromaticity Diagram, edit ICC profile, adapt primaries of RGB Color Space, transform matrices between Linear RGB and XYZ, transform matrices between Linear RGB and Linear RGB，chromatic adaptation, standard illuminants, chromatic adaptation matrices.    
Fix bugs: “414 Request-URI Too Large” is returned in WeiBo snap tool constantly; tooltip blinks at edge of screen; some links do not work.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.2)    
    
2019-5-1 v5.1 Interface: Controls are displayed in pictures which can be selected in 5 colors and whether display texts.    
Shorter tips to fit for 14 inches screen of laptop.    
Image tools: Extract/Add Alpha channel.    
Fix several problems, including error condition to filter transparent pixels in Image Manufacture.    
Happay Labor Day!    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.1)    
   
2019-4-21 v5.0 Select scope or area by dragging anchors.    
Doodle: Paste picture, draw shape(rectangle/circle/ellipse/polygon) line or fill color in shape, draw lines by    
dragging mouse, in image. Stroke width and color, line dotted can be set.    
View image: Set load size. Select whether show coordinate and rulers. Rotation can be saved.    
Browse Images: Grid Mode/Thumnails List Mode/File List Mode. Set load size. Rotation can be saved.    
Image Manufacture: Dithering can be applied to all scope types except for matting. Opacity can be made by    
Premultiplied Aplha for formats not supporting alpha. Blur margins. Shadow implemented in low level.    
Adjust size or margins by dragging anchors. Crop inside/outside in rectangle/circle/ellipse/polygon. Veritcal    
texts.    
Interface: Only display useful controls. Enough and not distracting tips. Shortcuts, major buttons, and default    
buttons. Monitor memory/CPU in time. View JVM properties. Refresh/reset windows. Restore last size of    
interfaces. Pop recent visited files/directories. Recent visited tools.    
Codes refactoring: Implement selection logic by subclass instead of switch statement, to move judgement    
outside loop. Avoid float calculation in loop. Rationalize inheritance and reduce duplication. Central    
management of stages' opening/closing to avoid threads residual.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.0)    
    
2019-2-20 v4.9 Change image's contrast. Multiple algorithms are supported. Dithering can be selected when    
handle color quantization.    
Statistic data of image's color channels, including mean, variance, skewness, mode, midean, etc. Histograms.    
Recorder of images in system clipboard. Change font size any time.    
View images: copy/crop/save the selected area.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.9)    
   
2019-1-29 v4.8 View PDF file in image mode. Density can be set. Pages can be cropped and saved as images.    
Locate function in Text/Bytes Editer: Go to the position of specified character/byte/line.    
Cut file, by files number, by bytes number, or by start-end list.    
Merge multiple files' bytes as a new file.    
A file path can follow program as argument to be opened directly by MyBox.     
On windows, the default Open Method of image/text/PDF files can be associated with MyBox.exe, to open a file by MyBox by double clicking the file name.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.8)    
    
2019-1-15 v4.7 Edit Bytes. Input boxes of general ASCII characters. Break lines by bytes number or by some defined bytes. Find and replace in current page or in whole file, and count. Filter lines by "Include One", "Not Include All", "Include All", or "Not Include Any". Cumulative filter. Filtered results can be saved. Select whether include lines number. Select charset to decode bytes which can be viewed, scrolled, and selected synchronously. Paginate. Fit for viewing or editing very large file, such as binary file in size of several GBs. Page Size can be set. Make sure correction of finding, replacing, and filtering of bytes that are across pages.    
Convert line breaks of files in batching way.    
Merge "Rename Files" and "Rename Files under Directories".    
Image Blurring uses "Average Algorithm" which is good enough and quicker.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.7)    
    
    
2018-12-31 v4.6 Edit Text: Detect line break automatically. Convert line break. Support LF(Unix/Linux), CR(iOS), CRLF(Windows).    
Find and replace. In current page, or in whole file.    
Filter lines. By "Include one of strings" or "Not include all of strings". Cumulative filter. Filtered results can be saved.    
Paginate. Fit for viewing or editing very large file, such as logs in size of several GBs. Page Size can be set. Make sure correction of finding, replacing, and filtering of strings that are across pages.    
Load and display first page, and scan the file in background to count characters number and lines number. Part of functions are unavaliable while counting. Interface will be refreshed automatically after counting process is complete.    
In progress interface, buttons "MyBox" and "Cancel" are added to have user use other functions or cancel current process.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.6)    
    
    
2018-12-15 v4.5 Text Encoding. File charset can be either detected automatically or set manually. Target file charset can be selected to implement encoding conversion. BOM setting is supported. Hexadecimal codes can be viewed and selected synchronously. Line numbers are shown.    
Text Encoding conversion in batch way.    
Split image by size.    
Copy image or selected part of image in system clipboard(Ctrl-c).    
Crop and save part of image in interface of Image Viewer.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.5)    
    
    
2018-12-03 v4.4 View/Extract/Create/Edit multiple frames image file. Support multiple frames Tiff file.    
For all operations which use image as input, handle situation of multiple frames image file.    
For all operations which use image as input, handle situation of big image which includes too many pixels to be loaded and displayed under limitation of available memory. Evaulate possiblility OutOfMemory and judge whether subsample image automatically. After that, show meaningful information and prompt for next    
step.     
Support splitting big image by only reading required part of data and writing while reading. Splitted results can be saved as multiple image files, multiple frames Tiff file, or PDF file.     
Support subsampling big image with options of sample region and sample ratio.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.4)    
    
    
2018-11-22 v4.3 Support animated Gif. View: set interval, pause/continue, go special frame, next/previous frame. Extract: set from/to frames, target files' type. Create/Edit: add/delete images, adjust orders, set interval, whether loop, keep images' size or set images' size, save as, what you see is what you get.    
Easier and better Scope for Image Manufacture. Type:All, Matting, Rectangle, Circle, Color Matching, Color Matching in Rectangle, Color Matching in Circle. Color Matching can be against red/green/blue channel, saturation, brightness, hue, or whole RGB. Points set of matting and colors set of color matching can be    
added/deleted easily. All type of scope can be set as Excluded.     
Merge functions of "Color", "Filter", "Effect", and "Replace Color", to reduce interface elements and user inputs.    
Multiple Images Viewer: Number of files in each screen can be set; pictures are shown in balanced sizes.    
[Closed requirements/bugs in this version](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.3)    
    
2018-11-13 v4.2 Scope for Image Manufacture: All, Matting, Rectangle, Circle, Color Matching, Hue Matching, Rectrangle/Circle plus Color/Hue Matching. "Matting" is like Magic Wand of PhotoShop or "Bucket Fill" of Paint on Windows.    
Scope can be applied for Color Increasing/Deceasing, Filtering, Effects, Replacing Color, and Convolution. The scope can be determined by clicking image.    
Convolution Kernels Manager: Values of Gaussian Distribution can be filled in automatically; Option about how to handle pixels in edges is supported.     
Directories Rename: Strings can be used to filter files to be handled.    
Optimize and reorganize codes of Image Manufacture.    
More shortcuts.    
    
2018-11-08 v4.1 Image Manufacture type of "Cover". Following can be set on an image: mosaic rectangle, mosaic circle, frosted rectangle, frosted circle, or picture. Area and size can be set for masaic or frosted cover. Internal pictures or user's pictures can be selected as cover, with options of size and opacity.     
Image Manufacture type of "Convolution". Convolution kernels can be picked to apply upon images. Batch way is supported.    
Convolution Kernels Manager. Kernels for image handling can be created/edited/deleted/copied. The matrix can be normalized automatically. The kernel can be tested. Example kernels are provided.    
New Image filters: Yellow/Cyan/Magenta channels.    
    
2018-11-04 v4.0 New channels of Image Color Adjustment: Yellow, Cyan, Magenta. Yellow channel can be used to generate warm-toned image.    
New filter of image: Sepia, which is used to make picture old.    
New image effect: Emboss. Options like direction,  radius, and whether changed as grayscale, can be set.    
Images blending. Options like defining intersected area and blending mode can be set.    
Online helps are enriched with important information.    
    
2018-10-26 v3.9 Embed Derby database to save program's data. Make sure data are migrated from configuration file to db correctly.    
Image manufacture: Record updating histories to return former status. Can set whether record histories or set the number of the updating histories.    
English version of User Guides.    
    
2018-10-15 v3.8 Optimize codes: Split the class ImageManufacture into classes of each function.    
Optimize interface: Make the tools more friendly. Set shortcuts.    
In Image Manufacture, more filters like Red/Green/Blue inverting, and "Outline" for text watermark.    
    
2018-10-09 v3.7 In Weibo Snap Tool, load images sequentially by using javascript event. Make sure the minimum interval to avoid being judged as invalid access by server. Meanwhile monitor the maximum interval to avoid broken iteration due to missed picture or untriggered event by fast loading of small picture.     
"Effects" of image manufacturing, including blurring, sharpening, edge detecting, posterizing, and thresholding.    
    
2018-10-04 v3.6 Optimize algorithm of Weibo Snap Tool to make sure all pictures loaded. Check codes to avoid meomry leak.    
Reduce brightness and saturation of background colors in interface styles.    
Add introduction about dpi-aware in document.    
    
2018-10-01 v3.5 Optimize algorithm of Weibo Snap Tool to make sure all pictures loaded.    
Provide multiple interface styles.    
    
2018-09-30 v3.4 Fix bugs: 1) In Weibo Snap Tool, adjust the judge conditions of loading pages to make sure all information in the pages can be saved. 2) When close/switch window and task is running, and user select "Cancel", current window should not be closed.    
New features: 1) Can set the maximum main memory for PDF operations. 2) Can clear peasonal settings.    
    
2018-09-30 v3.3 Solve the problem of Weibo website certificate finally. Verified on Windows, CentOS, and Mac.    
    
2018-09-29 v3.2 Weibo Snap Tools: 1) Import certificate automatically on Linux and Windows to have users need not login. But have no way on Max, so users of Apple computer will have to login Weibo to use Weibo Snap Tool. 2) Can expand all comments and all pictures in the pages before make snapshots. 3) Can save the original pictures of the pages.(Cooooooool)    
    
    
2018-09-26 v3.1 All image operations can be done in batch way. Fix and optimize algorithms of Color manufacture. Set default font size to fit for different resoltuions in different environments. Seperate User Guides for each type of tools. Prompt user to login Weibo to install its SSL certificate before start to use Weibo Snap Tools. I am looking for the way to remove this limitation because MyBox has not any interest of touching any provate information of users.     
    
2018-09-18 v3.0 Improve Snap Tool for Weibo: Only snap meaningful area in the page, by which half time is cost and half size of PDF files is saved. Can expand the comments of messages. I am so proud of this feature! Can set the maximum size of merged PDF.    
Fix bugs of Html Editor and enhance its functions.    
2018-09-17 v2.14 Improve Snap Tool for Weibo: Retry times of failure; Do not mergin month's PDF file when pages of the month is more than 10.    
2018-09-15 v2.13 Show Reference Image and Scope Image seperately. Make sure no thread runnig after program exits. Compress images in PDF in batching way. Snap Tool for Weibo, backup contents of any weibo account automatically. Duration can be set. Both PDF files and html files can be saved.     
Weibo pages are loaded dynamically, so locally backed pages can not be loaded and shown correctly. They are backed just for texts in the pages.    
This tool might fail to work when weibo would change the accessing channel of pages in future. Who know~     
    
2018-09-11 v2.12 Combine images as PDF file, Compress images in PDF, combine PDF files, split PDF. Support Chinese written in PDF file, and system font file is locating automatically while user can input ttf file path. Prompt information is shown more smoothly and friendly. In web browser, font size can be zoom in and zoom out, and web page can be snapped into PDF file with settings of delay and PDF page size.    
    
2018-09-06 v2.11 Image combining which supports array options, background color, interval, edges, and size options. Web browser supports synchronized contents with web editor and snapshots of the whole web page in one image. Image maunfacture like shadow, arced corners, adding edges. Implement manufacture of big image and make sure performance is acceptable.    
    
2018-08-11 v2.10  Image Spliting which supports equipartition and custom. Scope of image manufacture is easier to use. No number limition is for multiple image files viewed in same screen now.    
    
2018-08-07 v2.9  Image croping. Scope, including area scope like rectangle and circle and color matching, is supported for image manufacture.    
    
2018-07-31 v2.8  Image edges' cutting. Watermark in image. Undo and redo for image manufacture. Html editor. Text edior.    
    
2018-07-30 v2.7  Image transform, including rotating, mirroring, and shearing.    
    
2018-07-26 v2.6  Improve color replacement: Support original colors' list and hue distance. Support opacity adjustment.    
    
2018-07-25 v2.5  Color palette. And Replace colors in image, by accurate matching of color, or by colors' distance. Color replacement can be used to change the background color of images or eliminate color noise of images.    
    
2018-07-24 v2.4  Improve functions of Image Manufacture and Multiple Images Viewer: Smoothly switching, reference image, and pixels adjustment.    
    
2018-07-18 v2.3  Alarm clocks, with options of time and ring. Support rings of "Meow", wav, and mp3. Can run in background. Thanks my GuaiGuai for her contribution of "Meow".    
    
2018-07-11 v2.2  Fix bug about threads' logic. Files rearragement that categories files under new directories according to their modify time or create time. This function can be used to handle photoes, games screenshots, or system logs which need archived based on time.    
    
2018-07-09 v2.1  Improve interface of image manufacturing and support images borwsering. Directory synchronization, with options like copying subdirectoies, new files, modified files after some time, original file's attributes, or deleting files and directories which are not in source path, etc.    
    
2018-07-06 v2.0  Extract texts from PDF files in batching way. Convert image files to other formats in batching way. Rename files under directories, with options about files' name and sorting. All of or part of renamed files can be recovered as originl names.     
    
2018-07-03 v1.9  Fix issues. Customize page separator line when extract texts from PDF. Improve image manufacture: Adjust saturation, lightness, and hue with parameters and provide filters like gray, invert, or binary.     
    
2018-07-01 v1.8  Extract texts from PDF files. Manufacture image: Adjust saturation, lightness, make it gray, or invert the color.     
    
2018-06-30 v1.7  Improve Pixels Calculator. Support to view multiple images in same screen.    
    
2018-06-27 v1.6  Convert image files to other formats, with options of color, size, compression, quality, etc. Pixels Calculator. Support more image formats: gif, wbmp, pnm, pcx.    
    
2018-06-24 v1.5  Extract images from PDF and save as original format. Support extracting and converting in batching way. Thanks helps from "https://shuge.org/" who asked the requirement of extracting images from PDF.    
    
2018-06-21 v1.4  Support reading/writing meta-data of images in format of png, jpg, bmp, tiff. Thanks helps from "https://shuge.org/" who asked the requirement of Meta-data of images.    
    
2018-06-15 v1.3  Fix the gray calculation in OTSU; Optimize shared codes; Support PDF password; More friendly interface .    
    
2018-06-14 v1.2  Add options of color conversion for binary image type. Save user's choices. And optimize reading of hellp document. Thanks helps from "https://shuge.org/" who asked the requirement of binary conversion of color with threshold.    
    
2018-06-13 v1.1  Add: image format TIFF and RAW, options of Compression Type and Quality, and Help information. Thanks helps from "https://shuge.org/" who asked the requirement of TIFF format    
    
2018-06-12 v1.0  Convert each page of PDF file to an image with options of format, density, color,  compression, and quality. And user can pause/continue the conversion.    
    

[Closed requirements/bugs not in any version](https://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+no%3Amilestone)    