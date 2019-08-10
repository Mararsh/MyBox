# [中文ReadMe](https://github.com/Mararsh/MyBox)

# MyBox: Set of Easy Tools
This is GUI program based on JavaFx to provide simple and easy functions. It's free and open sources

## Download
Packages of each version have been uploaded at [Releases](https://github.com/Mararsh/MyBox/releases?) directory now. You can find them by clicking `releases` tab in main page of this project. 

Following are latest release:

| Platform | Link | Size | Requirements |
| - | - | -  | -  |
| win | [MyBox-5.3-exe.zip](https://github.com/Mararsh/MyBox/releases/download/v5.3/MyBox-5.3-exe.zip) | 240MB | None |
| win | [MyBox-5.3-jar-win.zip](https://github.com/Mararsh/MyBox/releases/download/v5.3/MyBox-5.3-jar-win.zip)  | 55MB | Java 12 or higher |
| linux | [MyBox-5.3-jar-linux.zip](https://github.com/Mararsh/MyBox/releases/download/v5.3/MyBox-5.3-jar-linux.zip)  | 59MB | Java 12 or higher |
| mac | [MyBox-5.3-jar-mac.zip](https://github.com/Mararsh/MyBox/releases/download/v5.3/MyBox-5.3-jar-mac.zip)  | 56MB | Java 12 or higher |
| win/linux/mac | [MyBox-5.3-jar-cross-platform.zip](https://github.com/Mararsh/MyBox/releases/download/v5.3/MyBox-5.3-jar-cross-platform.zip)  | 111MB | Java 12 or higher |

EXE package is avaliable for users who have not java env. It need not installation and users can run the EXE directly after unpack it. (Please unpack it under path with pure-English name.)

Since Java is installed by default in Linux env and Mac env, the installation images are not made for the 2 platfroms. User can download jar file if JRE or JDK 12 or higher(`Oracle jdk` or `open jdk`) is installed. 
Each platform has its jar, and there is cross-platform jar whose size is larger. 


## Launch
Double click "MyBox.exe" to launch MyBox on Windows. The default "Open Method" of image/text/PDF files can be associated to MyBox.exe and a file can be opened directly by MyBox by double clicking the file's name.

Run following command to launch this program with Jar package: (Please upgrade to Java 12 or higher)

    java   -jar   MyBox-5.3.jar
	
A file path can follow the command as argument to be opened directly by MyBox. Example, following command will open the image:

    java   -jar   MyBox-5.3.jar  /tmp/a1.jpg


## Limitation
MyBox.exe can not be lanuched under path including non-English characters.

The embedded Derby is in single-application mode, so only one instance of MyBox can read/write db. That is, multiple instances of MyBox can run at same time but only the first one can save and visit configuration data while others can do most things except for accessing user's data.


# Resource Addresses
Project Main Page: https://github.com/Mararsh/MyBox

Source Codes and Compiled Packages: https://github.com/Mararsh/MyBox/releases

Submit Software Requirements and Problem Reports: https://github.com/Mararsh/MyBox/issues

Cloud Storage: https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F

Online Helps: https://mararsh.github.io/MyBox/mybox_help_en.html


# Documents
User Guide - Overview https://github.com/Mararsh/MyBox/releases/download/v5.0/MyBox-UserGuide-5.0-Overview-en.pdf

User Guide - Image Tools  https://github.com/Mararsh/MyBox/releases/download/v5.0/MyBox-UserGuide-5.0-ImageTools-en.pdf

User Guide - PDF Tools  https://github.com/Mararsh/MyBox/releases/download/v5.0/MyBox-UserGuide-5.0-PdfTools-en.pdf

User Guide - Desktop Tools  https://github.com/Mararsh/MyBox/releases/download/v5.0/MyBox-UserGuide-5.0-DesktopTools-en.pdf

User Guide - Network Tools  https://github.com/Mararsh/MyBox/releases/download/v5.0/MyBox-UserGuide-5.0-NetworkTools-en.pdf

Development Guide  https://github.com/Mararsh/MyBox/releases/download/v5.3/MyBox-DevGuide-1.0-en.pdf

[Development Logs](#devLog)


# Current Version
Current version is 5.3. Its features are mentioned below in summary:
* [Cross platforms](#cross-platform)
* [Internationalized](#international)
* [PDF Tools](#pdfTools)
* [Image Tools](#imageTool)
    - [View Image](#viewImage)
    - [Browse Images](#browserImage)
    - [Manufacture Image](#imageManufacture)
    - [Convert Image](#imageConvert)
    - [Multi-frames Image](#multiFrames)
    - [Merge Images](#multipleImages)
    - [Part Image](#imagePart)
    - [Big Image](#bigImage)
    - [Others](#imageOthers)
* [Data Tools](#dataTool)
    - [Matrcies Calculation](#matrixTool)
    - [Color Spaces](#colorSpaces)
* [Desktop Tools](#desktopTool)
    - [Arrage Directories](#directoriesArrange)
    - [Edit Text](#editText)
    - [Edit Bytes](#editBytes)
    - [Others](#desktopOthers)
* [Network Tools](#netTool)
    - [Edit Html](#htmlEditor)
    - [Snap WeiBo](#weiboSnap)
* [Settings](#settings)
* [Window](#windows)

# Implementation
MyBox is developed with Netbeans8.2 and JavaFX Scene Builder 2.0: 

https://netbeans.org/

https://www.oracle.com/technetwork/java/javafxscenebuilder-1x-archive-2199384.html


It is based on following open sources softwares or libraries:

JavaFx  https://docs.oracle.com/javafx/2/
	
PDFBox  https://pdfbox.apache.org/
	
jai-imageio  https://github.com/jai-imageio/jai-imageio-core
	
javazoom  http://www.javazoom.net/index.shtml
	
log4j   https://logging.apache.org/log4j/2.x/
	
Derby   http://db.apache.org/derby/

GifDecoder   https://github.com/DhyanB/Open-Imaging/

EncodingDetect  https://www.cnblogs.com/ChurchYim/p/8427373.html

Free Icons  https://icons8.com/icons/set/home


# Current Version

## Cross-platform<a name="cross-platform" />
MyBox is implemented in pure Java and based on open source codes, so it can run any platform which supports Java 12.
Previous versions are based on Java 8.

## Internationalized<a name="international" />
All codes of MyBox are internationalized. User can switch language in time.
Currently MyBox supports Chinese and English. To support a new language is just to edit a new resource file.

## PDF Tools<a name="pdfTools" />
1. View PDF file in image mode. DPI can be set to adjust resolution. Page can be cropped as images.
   BookMarks(Table of Contents) and thumbnails can be shown.
2. Convert pages of PDF as images. Options like format, density, color space, compression, quality, etc.
3. Combine multiple images as PDF file. Options like compression, page size, margin size, header, author, etc. 
   Support Chinese and tool can locate system font files. User can input path of ttf font file.
4. Compress images in PDF file. JPEG quality or threshold of black-white can be set.
5. Merge multiple PDF files.
6. Split a PDF file into multiple PDF files, by pages number, by files number, or by start-end list.
7. Extract images in PDF file. Page range can be set.
8. Extract texts in PDF file. Splitting line can be customized.
9. Handle PDF files in batch way.
10. Modify PDF file's attributes like title, author, modify time, user password, owner password, user permissions, etc.
11. Maximum main memory of PDF handling can be set.

## Image Tools<a name="imageTool" />

### View Image<a name="viewImage" />
1. "Load Width". Read image file with "Original Size" or with defined width.
2. "Select Mode". When in this mode, Crop, Copy, and Save As are against the selected area. Or else these operations are against whole image.
3. Rotation can be saved.
4. Recover, Rename, Delete.
5. Select whether display Corodinate, X/Y Rulers, Data.
6. Statistic and visualization of image data, including average, variance, skewness, median, mode, minimum, maximum of occurance of each color channel, and their histograms.
7. Image attributes and image meta. ICC profile embedded in image can be decoded.
8. Navigation of images under same directory.


### Browse Images<a name="browserImage" />
1. Display multiple images in same screen. Rotation and zoomming can be separated or synchronized.
2. Rotation can be saved.
3. Grid Mode. Files number, columns number, and load width can be set.
4. Thumbnails List Mode.
5. Files List Mode.
6. Rename and Delete.


### Image Manufacture<a name="imageManufacture" />
1. Size. By dargging anchors, by setting scale, or by inputting pixel values with 4 types of keeping aspect ratio.
2. Crop. Cut inside or outside of rectangle, circle, ellipse, or polygon. Background color can be set.
3. Color. Increase, decrease, set, filter, or invert value of saturaion, brightness, hue, Red/Green/Blue/Yellow/Cyan/Magenta channel, RGB itself, or opacity. 
   Premultiplied Alpha is supported for setting opacity.
4. Effects. Clarity, contrast, posterize(reduce colors), thresholding, gray, black-white, Sepia, emboss, edges detect, blur, sharpen. 
   Algorithms and parameters can be set. Convolution can be defined and referred to make more effects.
5. Text. Options like font family, style, size, color, opacity, shadow, angle, whether outline, whether veritical. Locating text by clicking image.
6. Picture. Paste embedded/outside/clipboard picture on image. Blend modes can be selected.
7. Shape. Rectangle/circle/ellipse/polygon can be drawed on image. Options like stroke width and color,whether fill color, whether dotted.
8. Line. Clicking and dragging multiple times to draw one line on image. Options like stroke width and color, whether dotted.
9. Pen. Clicking and dragging one time to draw one line on image. Options like stroke width and color, whether dotted.
10. Mosaic. Fill mosaic or frosted glass inside/outside rectangle/circle/ellipse/polygon. Density can be set.
11. Round corner. Arc and background color can be set.
12. Shadow. Options like background color, shadow size, whether apply Premultiplied Alpha.
13. Transform. Shear, mirror, and rotate.
14. Margins. Blur margins with option of whether apply Premultiplied Alpha; Drag anchors to adjust margins; add margins by setting width; cut margins by setting width or color.
15. Scope. Types of All, Matting, Shapes(rectanlge/circle/ellipse/polygon), Color Matching, and Color Matching in Shapes. 
    Color Matching can be against saturaion, brightness, hue, RGB, or Red/Green/Blue channel with distance defined. 
	Scope can be applied for "Color" and "Effects". Scope can be determined by simply clicking image. 
	Parameters like points set of matting and colors list of color matching can be set easily. All scope can be set as Excluded.
16. "Undo" and "Redo" of previous operation. Original image can be recoverred at any time. 
    Updated histories can be saved automatically and set back. Number of updated histories can be set.
17. Select whether show reference image. Other pictures can be selected as the reference image.
18. Handle existed image, or create new image.
19. Copy(CTRL+c), paste(CTRL+v), pop, and reference.

### Image Conversion<a name="imageConvert" />
1. Formats of image file: png, jpg, bmp, tif, gif, wbmp, pnm, pcx, raw.
2. Color spaces: sRGB, Linear sRGB, ECI RGB, Adobe RGB, Apple RGB, Color Match RGB, ECI CMYK, Adobe CMYK(several), Gray, Binary
3. Color space based on external ICC profile.
4. Option to embed ICC profile.
5. Options to handle transparent channel, including keep, delete, premultiply and delete, premultiply and keep.
6. Options of compression types and quality.
7. For binary, algorithms can be choiced: OTSU, default or threshold. And option of dithering.
8. Conversion in batch.


### ultiple frames image file<a name="multiFrames" />
1. View/Extract images in multiple frames file.
2. Create/Edit multiple frames Tiff/Tif file.
3. View/Extract/Create/Edit animated Gif file. Interval, whether loop, and images' size can be set.

### Merge images<a name="multipleImages" />
1. Blend images. Options like intersected area and blending modes.
2. Combine images. Options like array ordering, background color, interval, margins, and size.
3. Combine images in PDF file.
4. Add Alpha channel.

### Part image<a name="imagePart" />
1. Split image. By number, by size, or by customizing. Results can be saved as image files, multiple frames Tiff file, or PDF file.
2. Subsample image. Options like sample region and sample ratio.
3. Extract Alpha channel.


### Big Image<a name="bigImage" />
1. Evaulate the required memory for whole image, and judge whether load all data in memory.
2. If enough memory is available to load whole image, read all data for next operations. Try best to operate in memory and avoid file I/O.
3. If memory may be out, subsample the image for next operations.
4. The sample ratio is determined by following rule: Make sure the sampled image is good enough while the sampled data occupy limited memory.
5. The sampled image is mainly for displaying, and not suitable for operations against whole image and images merging.
6. Some operations, like splitting and subsampling, can be handled by reading part of image data and writing-while-reading, so they are suitable for big images. Sampled image is displayed while original image is handled.

### Others<a name="imageOthers" />
1. Supported image formats include png, jpg, bmp, tif, gif, wbmp, pnm, pcx.	Adobe YCCK/CMYK jpg file can be decoded.
2. Manufacture images in batch.
3. Color palette.
4. Pixels calculator
5. Convolution Kernels Manager


## Data Tools<a name="dataTool" />

### Matrices Calculation<a name="matrixTool" />
1. Edit matrix data:
	-  Filter special characters in input/pasted data to fit for data in some format.
	-  Convert current matrix data into row vector, column vector, or matrix in defined columns number automatically.
	-  Generate identify matrix, random matrix, or random square matrix automatically.
2. Unary matrix calculation: Transpose, Row Echelon Form, Reduced Row Echelon Form, Determinant By Elimination, Determinant By Complement Minor, Inverse Matrix By Elimination, Inverse Matrix By Adjoint, Matrix Rank, Adjoint Matrix, Complement Minor, Normalize, Set Decimal Scale, Set As Integer, Multiply Number, Divide By Number, Power.
3. Binary matrices calculation: Plus, Minus, Hadamard Product, Kronecker Product, Horizontally Merge, Vertically Merge.
	
### Color Space<a name="colorSpaces" />
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
	
## Desktop Tools<a name="desktopTool" />

### Manage Directories<a name="directoriesArrange" />
1. Rename Files/Directories, with options of files' name and ordering. Renamed files can be recovered as original names in all or in part.
2. Sychronize directories, with options like whether copy sub-directories or new files, whether only copy modified files after specific date time, whether keep attributes of original files, or whther delete non-existed files/directories under original directory.
3. Arrange files and reorganize them under new directories by modifed time. This tool can be used to handle lots of files which need be archived according to time, like photoes, screenshots of games, or system logs.

### Edit Texts<a name="editText" />
1. File charset can be either detected automatically or set manually. Target file charset can be selected to implement encoding conversion. BOM setting is supported.
2. Detect line break automatically. Convert line break. Show lines number. 
3. Support LF(Unix/Linux), CR(Apple), and CRLF(Windows).
4. Find and replace. In current page, or in whole file. Counting.
5. Locate. Go to position of specified character or specified line.
6. Filter lines. By “Include One”, “Not Include All”, “Include All”, or “Not Include Any”. 
7. Cumulative filter. Filtered results can be saved. Select whether include lines number.
8. Hexadecimal codes according file's charset can be viewed, scrolled, and selected synchronously.
9. Paginate. Fit for viewing or editing very large file, such as logs in size of several GBs.
	-  Set page size.
	-  Pages navigation bar
	-  Load and display first page, and scan the file in background to count characters number and lines number. 
	   Part of functions are unavaliable while counting. Interface will be refreshed automatically after counting process is complete.
	-  Make sure correction of finding, replacing, and filtering of strings that are across pages.
10. General functions of editing, like copy/paste/cut/delete/selectAll/undo/redo/recover. And their shortcuts.

### Edit Bytes<a name="editBytes" />
1. Bytes are expressed as 2 hexadecimal characters. All blanks, line breaks, and invalid values are ignored.
2. Input boxes of general ASCII characters.
3. Break lines, which is only for display and has not actual effect. By bytes number or by some defined bytes.
4. Find and replace. In current page, or in whole file. Counting.
5. Locate. Go to position of specified character or specified line.
6. Filter lines. By “Include One”, “Not Include All”, “Include All”, or “Not Include Any”. 
7. Cumulative filter. Filtered results can be saved. Select whether include lines number.
8. Select charset to decode bytes which can be viewed, scrolled, and selected synchronously.
9. Paginate. Fit for viewing or editing very large file, such as binary file in size of several GBs.
	-  Set page size.
	-  Pages navigation bar
	-  Load and display first page, and scan the file in background to count bytes number and lines number. 
	   Part of functions are unavaliable while counting. Interface will be refreshed automatically after counting process is complete.
	-  Make sure correction of finding, replacing, and filtering of bytes group that are across pages. 
	   When break lines by bytes number, crossing pages need not concerned.
10. General functions of editing, like copy/paste/cut/delete/selectAll/undo/redo/recover. And their shortcuts.

### Others<a name="desktopOthers" />
1. Convert files' charset in batch.
2. Convert files' line break in batch.
3. Split file, by files number, by bytes number, or by start-end list.
4. Merge files.
5. Monitor images in system clipboard and have user save/view them. Lossless or compression type can be selected.
6. Alarm clocks, including options of time and music. Support rings of “Meow”, wav files, and mp3. Can run in background.

## Network Tools<a name="netTool" />

### Html Editor<a name="htmlEditor" />
1. Edit local web pages or online pages in rich text. (Not support FrameSet)
2. Edit Html codes directly. (Support FrameSet)
3. Web browser to view contents of Editors or load the online page. Support history browsing, font zooming 、and snapshoting of the whole page as an image or a PDF file.
4. Rich-text-editor, html-codes-edtor, and web browser are synchronized.

### Weibo Snaping Tool<a name="weiboSnap" />
1. Save Weibo pages of any months of any Weibo accounts automatically.
2. Set the months range.
3. Make sure whole page contents loaded. Can expand the comments and pictures in the pages.
4. Save the pages as local html files which can not be loaded normally due to dynamic loading of WeiBo contents. They can be used to extract texts in the pages.
5. Save the pages' snapshots as PDF files, with options like page size, margins, author, images format, etc.
6. Save all original size pictures in the pages.
7. Display progress information in time.
8. Stop the progress at any time. The interrupted month will be record and input as start month for next execution.
9. Set the retry times of failure.

## Settings<a name="settings" />
1. Whether restore last size of each scene. Whether open new stage to display scene. Whether pop recent visited files/directories.
2. Language, font size, interface style, color of controls' pictures, whether show comments.
3. Width and color of stroke and anchor. Whether anchors are solid.
4. Whether display coordinate and rulers.
5. Images histories number. Maximum width to display images.
6. Whether remove alpha channel when copy. Whether replace alpha as white when alpha is not supported.
7. Maximum main memory of PDF handling.
8. Whether close alarm clocks when exit program.
9. Temporary path of MyBox.
10. Clear personal settings. Open user's directory.

## Window<a name="windows" />
1. Open/Close monitor bar of Memory.
2. Open/Close monitor bar of CPU.
3. Display JVM attributes.
4. Refresh/Reset/Full-screen windows.
5. Close other windows.
6. Recent visited tools.


# Development Logs<a name="devLog" />
2019-8-8 v5.3  Migrated on Netbeans 11 + Java 12.</br>
Improve batch interface: add directories, extend directories, filter file names, handle duplicated file names. </br>
Improve image conversion: more color spaces, external ICC profile as color space, whether embed ICC, handle transparent channel.</br>
Improve image matedata decoding: fetch embedded ICC in image.</br>
Improve codes: reduce duplicated codes by anonymous classes and embedded fxml; adjust classes inheritance; build project for different platforms. </br>
First version of "Development Guide". </br>
Fix issues: Algorithm for "Image Manufacture-Color-Opacity-premultiply" is incorrect; Fail to save image when file extension is missed on Linux; </br>
Links do not work on Linux; Version of ICC profile is decoded/encodes incorrectly; Interface may be froozen when ICC holds too much data; </br>
Not-decoded data in ICC prevents generation of XML.</br>
</br>
2019-6-30 v5.2 Improve image decoding. Adobe YCCK/CMYK jpg file can be viewed. </br>
Attributes and meta data of all images in multi-frames image file are read and displayed.</br>
PDF file can be viewed with bookmarks(Table of contents) and thumbnails. </br>
PDF file's attributes like author, version, user password, user permissions, and owner password etc can be modified.</br>
Matrices Calculation. Edit matrix data: adapt formatted data; transform matrix data into row vector, column vector, or another matrix in defined column number;</br>
generate identify matrix, random matrix, random square matrix automatically with defined column/row number. </br>
Unary matrix calculation: Transpose, Row Echelon Form, Reduced Row Echelon Form, Determinant By Elimination, Determinant By Complement Minor, Inverse Matrix By Elimination, Inverse Matrix By Adjoint, Matrix Rank, Adjoint Matrix, Complement Minor, Normalize, Set Decimal Scale, Set As Integer, Multiply Number, Divide By Number, Power. </br>
Binary matrices calculation: Plus, Minus, Hadamard Product, Kronecker Product, Horizontally Merge, Vertically Merge. </br>
Tools of Color Space: draw Chromaticity Diagram, edit ICC profile, adapt primaries of RGB Color Space, transform matrices between Linear RGB and XYZ, transform matrices between Linear RGB and Linear RGB，chromatic adaptation, standard illuminants, chromatic adaptation matrices.</br>
Fix bugs: “414 Request-URI Too Large” is returned in WeiBo snap tool constantly; tooltip blinks at edge of screen; some links do not work.</br>
</br>
2019-5-1 v5.1 Interface: Controls are displayed in pictures which can be selected in 5 colors and whether display texts.</br>
Shorter tips to fit for 14 inches screen of laptop.</br>
Image tools: Extract/Add Alpha channel.</br>
Fix several problems, including error condition to filter transparent pixels in Image Manufacture.</br>
Happay Labor Day!</br>
</br>
2019-4-21 v5.0 Select scope or area by dragging anchors.</br>
Doodle: Paste picture, draw shape(rectangle/circle/ellipse/polygon) line or fill color in shape, draw lines by</br>
dragging mouse, in image. Stroke width and color, line dotted can be set.</br>
View image: Set load size. Select whether show coordinate and rulers. Rotation can be saved.</br>
Browse Images: Grid Mode/Thumnails List Mode/File List Mode. Set load size. Rotation can be saved.</br>
Image Manufacture: Dithering can be applied to all scope types except for matting. Opacity can be made by</br>
Premultiplied Aplha for formats not supporting alpha. Blur margins. Shadow implemented in low level.</br>
Adjust size or margins by dragging anchors. Crop inside/outside in rectangle/circle/ellipse/polygon. Veritcal</br>
texts.</br>
Interface: Only display useful controls. Enough and not distracting tips. Shortcuts, major buttons, and default</br>
buttons. Monitor memory/CPU in time. View JVM properties. Refresh/reset windows. Restore last size of</br>
interfaces. Pop recent visited files/directories. Recent visited tools.</br>
Codes refactoring: Implement selection logic by subclass instead of switch statement, to move judgement</br>
outside loop. Avoid float calculation in loop. Rationalize inheritance and reduce duplication. Central</br>
management of stages' opening/closing to avoid threads residual.</br>
</br>
2019-2-20 v4.9 Change image's contrast. Multiple algorithms are supported. Dithering can be selected when</br>
handle color quantization.</br>
Statistic data of image's color channels, including mean, variance, skewness, mode, midean, etc. Histograms.</br>
Recorder of images in system clipboard. Change font size any time.</br>
View images: copy/crop/save the selected area.</br>
</br>
2019-1-29 v4.8 View PDF file in image mode. Density can be set. Pages can be cropped and saved as images.</br>
Locate function in Text/Bytes Editer: Go to the position of specified character/byte/line.</br>
Cut file, by files number, by bytes number, or by start-end list.</br>
Merge multiple files' bytes as a new file.</br>
A file path can follow program as argument to be opened directly by MyBox. </br>
On windows, the default Open Method of image/text/PDF files can be associated with MyBox.exe, to open a file by MyBox by double clicking the file name.</br>
</br>
2019-1-15 v4.7 Edit Bytes. Input boxes of general ASCII characters. Break lines by bytes number or by some defined bytes. Find and replace in current page or in whole file, and count. Filter lines by "Include One", "Not Include All", "Include All", or "Not Include Any". Cumulative filter. Filtered results can be saved. Select whether include lines number. Select charset to decode bytes which can be viewed, scrolled, and selected synchronously. Paginate. Fit for viewing or editing very large file, such as binary file in size of several GBs. Page Size can be set. Make sure correction of finding, replacing, and filtering of bytes that are across pages.</br>
Convert line breaks of files in batching way.</br>
Merge "Rename Files" and "Rename Files under Directories".</br>
Image Blurring uses "Average Algorithm" which is good enough and quicker.</br>
</br>
</br>
2018-12-31 v4.6 Edit Text: Detect line break automatically. Convert line break. Support LF(Unix/Linux), CR(iOS), CRLF(Windows).</br>
Find and replace. In current page, or in whole file.</br>
Filter lines. By "Include one of strings" or "Not include all of strings". Cumulative filter. Filtered results can be saved.</br>
Paginate. Fit for viewing or editing very large file, such as logs in size of several GBs. Page Size can be set. Make sure correction of finding, replacing, and filtering of strings that are across pages.</br>
Load and display first page, and scan the file in background to count characters number and lines number. Part of functions are unavaliable while counting. Interface will be refreshed automatically after counting process is complete.</br>
In progress interface, buttons "MyBox" and "Cancel" are added to have user use other functions or cancel current process.</br>
</br>
</br>
2018-12-15 v4.5 Text Encoding. File charset can be either detected automatically or set manually. Target file charset can be selected to implement encoding conversion. BOM setting is supported. Hexadecimal codes can be viewed and selected synchronously. Line numbers are shown.</br>
Text Encoding conversion in batch way.</br>
Split image by size.</br>
Copy image or selected part of image in system clipboard(Ctrl-c).</br>
Crop and save part of image in interface of Image Viewer.</br>
</br>
</br>
2018-12-03 v4.4 View/Extract/Create/Edit multiple frames image file. Support multiple frames Tiff file.</br>
For all operations which use image as input, handle situation of multiple frames image file.</br>
For all operations which use image as input, handle situation of big image which includes too many pixels to be loaded and displayed under limitation of available memory. Evaulate possiblility OutOfMemory and judge whether subsample image automatically. After that, show meaningful information and prompt for next</br>
step. </br>
Support splitting big image by only reading required part of data and writing while reading. Splitted results can be saved as multiple image files, multiple frames Tiff file, or PDF file. </br>
Support subsampling big image with options of sample region and sample ratio.</br>
</br>
</br>
2018-11-22 v4.3 Support animated Gif. View: set interval, pause/continue, go special frame, next/previous frame. Extract: set from/to frames, target files' type. Create/Edit: add/delete images, adjust orders, set interval, whether loop, keep images' size or set images' size, save as, what you see is what you get.</br>
Easier and better Scope for Image Manufacture. Type:All, Matting, Rectangle, Circle, Color Matching, Color Matching in Rectangle, Color Matching in Circle. Color Matching can be against red/green/blue channel, saturation, brightness, hue, or whole RGB. Points set of matting and colors set of color matching can be</br>
added/deleted easily. All type of scope can be set as Excluded. </br>
Merge functions of "Color", "Filter", "Effect", and "Replace Color", to reduce interface elements and user inputs.</br>
Multiple Images Viewer: Number of files in each screen can be set; pictures are shown in balanced sizes.</br>
</br>
2018-11-13 v4.2 Scope for Image Manufacture: All, Matting, Rectangle, Circle, Color Matching, Hue Matching, Rectrangle/Circle plus Color/Hue Matching. "Matting" is like Magic Wand of PhotoShop or "Bucket Fill" of Paint on Windows.</br>
Scope can be applied for Color Increasing/Deceasing, Filtering, Effects, Replacing Color, and Convolution. The scope can be determined by clicking image.</br>
Convolution Kernels Manager: Values of Gaussian Distribution can be filled in automatically; Option about how to handle pixels in edges is supported. </br>
Directories Rename: Strings can be used to filter files to be handled.</br>
Optimize and reorganize codes of Image Manufacture.</br>
More shortcuts.</br>
</br>
2018-11-08 v4.1 Image Manufacture type of "Cover". Following can be set on an image: mosaic rectangle, mosaic circle, frosted rectangle, frosted circle, or picture. Area and size can be set for masaic or frosted cover. Internal pictures or user's pictures can be selected as cover, with options of size and opacity. </br>
Image Manufacture type of "Convolution". Convolution kernels can be picked to apply upon images. Batch way is supported.</br>
Convolution Kernels Manager. Kernels for image handling can be created/edited/deleted/copied. The matrix can be normalized automatically. The kernel can be tested. Example kernels are provided.</br>
New Image filters: Yellow/Cyan/Magenta channels.</br>
</br>
2018-11-04 v4.0 New channels of Image Color Adjustment: Yellow, Cyan, Magenta. Yellow channel can be used to generate warm-toned image.</br>
New filter of image: Sepia, which is used to make picture old.</br>
New image effect: Emboss. Options like direction,  radius, and whether changed as grayscale, can be set.</br>
Images blending. Options like defining intersected area and blending mode can be set.</br>
Online helps are enriched with important information.</br>
</br>
2018-10-26 v3.9 Embed Derby database to save program's data. Make sure data are migrated from configuration file to db correctly.</br>
Image manufacture: Record updating histories to return former status. Can set whether record histories or set the number of the updating histories.</br>
English version of User Guides.</br>
</br>
2018-10-15 v3.8 Optimize codes: Split the class ImageManufacture into classes of each function.</br>
Optimize interface: Make the tools more friendly. Set shortcuts.</br>
In Image Manufacture, more filters like Red/Green/Blue inverting, and "Outline" for text watermark.</br>
</br>
2018-10-09 v3.7 In Weibo Snap Tool, load images sequentially by using javascript event. Make sure the minimum interval to avoid being judged as invalid access by server. Meanwhile monitor the maximum interval to avoid broken iteration due to missed picture or untriggered event by fast loading of small picture. </br>
"Effects" of image manufacturing, including blurring, sharpening, edge detecting, posterizing, and thresholding.</br>
</br>
2018-10-04 v3.6 Optimize algorithm of Weibo Snap Tool to make sure all pictures loaded. Check codes to avoid meomry leak.</br>
Reduce brightness and saturation of background colors in interface styles.</br>
Add introduction about dpi-aware in document.</br>
</br>
2018-10-01 v3.5 Optimize algorithm of Weibo Snap Tool to make sure all pictures loaded.</br>
Provide multiple interface styles.</br>
</br>
2018-09-30 v3.4 Fix bugs: 1) In Weibo Snap Tool, adjust the judge conditions of loading pages to make sure all information in the pages can be saved. 2) When close/switch window and task is running, and user select "Cancel", current window should not be closed.</br>
New features: 1) Can set the maximum main memory for PDF operations. 2) Can clear peasonal settings.</br>
</br>
2018-09-30 v3.3 Solve the problem of Weibo website certificate finally. Verified on Windows, CentOS, and Mac.</br>
</br>
2018-09-29 v3.2 Weibo Snap Tools: 1) Import certificate automatically on Linux and Windows to have users need not login. But have no way on Max, so users of Apple computer will have to login Weibo to use Weibo Snap Tool. 2) Can expand all comments and all pictures in the pages before make snapshots. 3) Can save the original pictures of the pages.(Cooooooool)</br>
</br>
</br>
2018-09-26 v3.1 All image operations can be done in batch way. Fix and optimize algorithms of Color manufacture. Set default font size to fit for different resoltuions in different environments. Seperate User Guides for each type of tools. Prompt user to login Weibo to install its SSL certificate before start to use Weibo Snap Tools. I am looking for the way to remove this limitation because MyBox has not any interest of touching any provate information of users. </br>
</br>
2018-09-18 v3.0 Improve Snap Tool for Weibo: Only snap meaningful area in the page, by which half time is cost and half size of PDF files is saved. Can expand the comments of messages. I am so proud of this feature! Can set the maximum size of merged PDF.</br>
Fix bugs of Html Editor and enhance its functions.</br>
2018-09-17 v2.14 Improve Snap Tool for Weibo: Retry times of failure; Do not mergin month's PDF file when pages of the month is more than 10.</br>
2018-09-15 v2.13 Show Reference Image and Scope Image seperately. Make sure no thread runnig after program exits. Compress images in PDF in batching way. Snap Tool for Weibo, backup contents of any weibo account automatically. Duration can be set. Both PDF files and html files can be saved. </br>
Weibo pages are loaded dynamically, so locally backed pages can not be loaded and shown correctly. They are backed just for texts in the pages.</br>
This tool might fail to work when weibo would change the accessing channel of pages in future. Who know~ </br>
</br>
2018-09-11 v2.12 Combine images as PDF file, Compress images in PDF, combine PDF files, split PDF. Support Chinese written in PDF file, and system font file is locating automatically while user can input ttf file path. Prompt information is shown more smoothly and friendly. In web browser, font size can be zoom in and zoom out, and web page can be snapped into PDF file with settings of delay and PDF page size.</br>
</br>
2018-09-06 v2.11 Image combining which supports array options, background color, interval, edges, and size options. Web browser supports synchronized contents with web editor and snapshots of the whole web page in one image. Image maunfacture like shadow, arced corners, adding edges. Implement manufacture of big image and make sure performance is acceptable.</br>
</br>
2018-08-11 v2.10  Image Spliting which supports equipartition and custom. Scope of image manufacture is easier to use. No number limition is for multiple image files viewed in same screen now.</br>
</br>
2018-08-07 v2.9  Image croping. Scope, including area scope like rectangle and circle and color matching, is supported for image manufacture.</br>
</br>
2018-07-31 v2.8  Image edges' cutting. Watermark in image. Undo and redo for image manufacture. Html editor. Text edior.</br>
</br>
2018-07-30 v2.7  Image transform, including rotating, mirroring, and shearing.</br>
</br>
2018-07-26 v2.6  Improve color replacement: Support original colors' list and hue distance. Support opacity adjustment.</br>
</br>
2018-07-25 v2.5  Color palette. And Replace colors in image, by accurate matching of color, or by colors' distance. Color replacement can be used to change the background color of images or eliminate color noise of images.</br>
</br>
2018-07-24 v2.4  Improve functions of Image Manufacture and Multiple Images Viewer: Smoothly switching, reference image, and pixels adjustment.</br>
</br>
2018-07-18 v2.3  Alarm clocks, with options of time and ring. Support rings of "Meow", wav, and mp3. Can run in background. Thanks my GuaiGuai for her contribution of "Meow".</br>
</br>
2018-07-11 v2.2  Fix bug about threads' logic. Files rearragement that categories files under new directories according to their modify time or create time. This function can be used to handle photoes, games screenshots, or system logs which need archived based on time.</br>
</br>
2018-07-09 v2.1  Improve interface of image manufacturing and support images borwsering. Directory synchronization, with options like copying subdirectoies, new files, modified files after some time, original file's attributes, or deleting files and directories which are not in source path, etc.</br>
</br>
2018-07-06 v2.0  Extract texts from PDF files in batching way. Convert image files to other formats in batching way. Rename files under directories, with options about files' name and sorting. All of or part of renamed files can be recovered as originl names. </br>
</br>
2018-07-03 v1.9  Fix issues. Customize page separator line when extract texts from PDF. Improve image manufacture: Adjust saturation, lightness, and hue with parameters and provide filters like gray, invert, or binary. </br>
</br>
2018-07-01 v1.8  Extract texts from PDF files. Manufacture image: Adjust saturation, lightness, make it gray, or invert the color. </br>
</br>
2018-06-30 v1.7  Improve Pixels Calculator. Support to view multiple images in same screen.</br>
</br>
2018-06-27 v1.6  Convert image files to other formats, with options of color, size, compression, quality, etc. Pixels Calculator. Support more image formats: gif, wbmp, pnm, pcx.</br>
</br>
2018-06-24 v1.5  Extract images from PDF and save as original format. Support extracting and converting in batching way. Thanks helps from "https://shuge.org/" who asked the requirement of extracting images from PDF.</br>
</br>
2018-06-21 v1.4  Support reading/writing meta-data of images in format of png, jpg, bmp, tiff. Thanks helps from "https://shuge.org/" who asked the requirement of Meta-data of images.</br>
</br>
2018-06-15 v1.3  Fix the gray calculation in OTSU; Optimize shared codes; Support PDF password; More friendly interface .</br>
</br>
2018-06-14 v1.2  Add options of color conversion for binary image type. Save user's choices. And optimize reading of hellp document. Thanks helps from "https://shuge.org/" who asked the requirement of binary conversion of color with threshold.</br>
</br>
2018-06-13 v1.1  Add: image format TIFF and RAW, options of Compression Type and Quality, and Help information. Thanks helps from "https://shuge.org/" who asked the requirement of TIFF format</br>
</br>
2018-06-12 v1.0  Convert each page of PDF file to an image with options of format, density, color,  compression, and quality. And user can pause/continue the conversion.</br>
</br>

# Main Interface
![About](https://mararsh.github.io/MyBox/0-en.png)

![About](https://mararsh.github.io/MyBox/1-en.png)

![About](https://mararsh.github.io/MyBox/2-en.png)

![About](https://mararsh.github.io/MyBox/3-en.png)

![About](https://mararsh.github.io/MyBox/4-en.png)







