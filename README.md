# [ReadMe in English](https://github.com/Mararsh/MyBox/tree/master/en)  ![ReadMe](https://mararsh.github.io/MyBox/iconGo.png)   

# MyBox：简易工具集
这是利用JavaFx开发的图形化桌面应用，目标是提供简单易用的功能。免费开源。  
  
## 新内容      
2021-1-27 版本6.3.8                  
-  添加：编辑和转换csv和excel文件。数据粘贴板。编辑和保存矩阵。批量设置html的风格。          
-  改进：颜色量化可以设置通道权重。焦点不在文本输入控件时快捷键可以省略Ctrl/Alt。数据转换/导出采用流读取。                 
-  解决的主要问题：批量替换图片颜色的距离不生效。PDF的用户密码和所有者密码混乱。文本过滤界面分页错误。表字段也需要翻译。时间树上公元前的时间解析错误。                    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.8)                   

 ## 下载与运行
每个版本编译好的包已发布在[Releases](https://github.com/Mararsh/MyBox/releases)目录下（点击上面的`releases`页签）。    
 
### 源码   
[MyBox-6.3.8-src.zip](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-src.zip)   37M  

关于源码的结构、编辑、和构建，请参考[开发指南](https://mararsh.github.io/MyBox_documents/zh/MyBox-DevGuide-2.1-zh.pdf) 和 
[打包步骤](https://mararsh.github.io/MyBox/pack_steps.html)             
 
 
### 自包含程序包   
自包含的程序包无需java环境、无需安装、解包可用。（解包的目录名不要包含汉字）  
  
| 平台 | 链接 | 大小 | 启动文件 |    
| --- | --- | --- |  --- |                                                                               
| win x64 | [MyBox-6.3.8-win.zip](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-win-exe.zip)  | 292MB | MyBox.exe |       
| linux x64 | [MyBox-6.3.8-linux.tar.gz](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-linux.tar.gz)  | 299MB  | bin/MyBox  |     
| mac | [MyBox-6.3.8-mac.dmg](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-mac.dmg)  | 306MB  |  MyBox-6.3.8.app   |   

双击或者用命令行执行包内的启动文件即可运行程序。可以把图片/文本/PDF文件的打开方式关联到MyBox，这样双击文件名就直接是用MyBox打开了。
（目前无法双击打开路径包含汉字的文件） 
    
### Jar包   
在已安装JRE或者JDK 15或更高版本（`Oracle java`或`Open jdk`均可）的环境下，可以下载jar包。   
 
| 平台 | 链接 | 大小 | 运行需要 |    
| --- | --- | --- |  --- |   
| win | [MyBox-6.3.8-win-jar.zip](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-win-jar.zip)  | 148MB | Java 15.0.1或更高版本 |    
| linux | [MyBox-6.3.8-linux-jar.zip](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-linux-jar.zip)  | 155MB  | Java 15.0.1或更高版本 |    
| mac | [MyBox-6.3.8-mac-jar.zip](https://github.com/Mararsh/MyBox/releases/download/v6.3.8/MyBox-6.3.8-mac-jar.zip)  |  152MB  | Java 15.0.1或更高版本 |    
    
执行以下命令来启动程序：
<PRE><CODE>     java   -jar   MyBox-6.3.8.jar</CODE></PRE>
程序可以跟一个文件名作为参数、以用MyBox直接打开此文件。例如以下命令是打开此图片：
<PRE><CODE>     java   -jar   MyBox-6.3.8.jar   /tmp/a1.jpg</CODE></PRE>

### 其它下载地址     
从云盘下载：  https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F     
从sourceforge下载：https://sourceforge.net/projects/mara-mybox/files/     

### 限制   
自包含包：无法在包含非英文字符的路径下启动；而且无法双击打开包含汉字的文件。已向jpackage开发组报告这个问题：
[JDK-8232936](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8232936) 

## 版本迁移      
每个版本有自己的配置文件，新版本可以复制已安装版本的参数。     

## 配置<a id="Config" />
配置文件在"用户目录"下:                 
| 平台 | MyBox配置文件的目录 |        
| --- | --- |        
| win | `C:\用户\用户名\mybox\MyBox_v6.3.8.ini`  |         
| linux | `/home/用户名/mybox/MyBox_v6.3.8.ini` |        
| mac | `/Users/用户名/mybox/MyBox_v6.3.8.ini` |         

可以临时改变配置文件：在命令行启动jar包时设置参数"config=\"配置文件名\""。
利用“设置”功能也可以修改配置参数。

# 资源地址     
| 内容 | 链接 | 
| --- | --- |     
| 项目主页 | https://github.com/Mararsh/MyBox |    
| 源代码和编译好的包 | https://github.com/Mararsh/MyBox/releases |    
| 在线提交软件需求和问题报告 | https://github.com/Mararsh/MyBox/issues |    
| 数据 | https://github.com/Mararsh/MyBox_data |    
| 文档 | https://github.com/Mararsh/MyBox_documents |    
| 镜像 | https://sourceforge.net/projects/mara-mybox/files/ |        
| 云盘 | https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F |    

# 文档         
| 文档名 | 版本 | 修改时间  | 链接 |
| --- | --- | --- | --- |
| 开发日志 | 6.3.8 |  2021-1-27 | [html](#devLog) |
| 快捷键 | 6.3.8 |  2021-1-27 | [html](https://mararsh.github.io/MyBox/mybox_shortcuts.html) |
| 打包步骤 | 6.3.3 |  2020-9-27 | [html](https://mararsh.github.io/MyBox/pack_steps.html) |
| 开发指南 | 2.1 |  2020-08-27 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-DevGuide-2.1-zh.pdf) |
| 用户手册-综述 |  5.0 |  2019-4-19 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-Overview-zh.pdf) |
| 用户手册-图像工具 | 5.0 |  2019-4-18 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-ImageTools-zh.pdf) |
| 用户手册-PDF工具 | 5.0 |  2019-4-20 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-PdfTools-zh.pdf) |
| 用户手册-桌面工具 | 5.0 |  2019-4-16 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-DesktopTools-zh.pdf) |
| 用户手册-网络工具 | 5.0 |  2019-4-16 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-NetworkTools-zh.pdf) |


# 实现基础
MyBox基于以下开源资源：  

| 名字 | 角色 | 链接 |  
| --- | --- | --- | 
| JDK | Java语言 | http://jdk.java.net/   |
|   |   | https://www.oracle.com/technetwork/java/javase/downloads/index.html  |   
| JavaFx | 图形化界面 | https://gluonhq.com/products/javafx/  |     
|   |   |  https://docs.oracle.com/javafx/2/  |     
|   |   |  https://www.oracle.com/technetwork/java/javafxscenebuilder-1x-archive-2199384.html |     
| NetBeans | 集成开发环境 | https://netbeans.org/ |     
| jpackage | 自包含包 | https://docs.oracle.com/en/java/javase/15/docs/specs/man/jpackage.html |     
| maven | 代码构建 | https://maven.apache.org/ |     
| jai-imageio | 图像处理 | https://github.com/jai-imageio/jai-imageio-core |   
| PDFBox | PDF处理 | https://pdfbox.apache.org/ |   
| PDF2DOM | PDF转html | http://cssbox.sourceforge.net/pdf2dom/ |   
| javazoom | MP3解码 | http://www.javazoom.net/index.shtml |      
| Derby | 数据库 | http://db.apache.org/derby/ |   
| GifDecoder | 不规范Gif | https://github.com/DhyanB/Open-Imaging/ |   
| EncodingDetect | 文本编码 | https://www.cnblogs.com/ChurchYim/p/8427373.html |   
| Free Icons | 图标 | https://icons8.com/icons/set/home |  
| Lindbloom | 色彩理论 | http://brucelindbloom.com/index.html |  
| tess4j | OCR | https://github.com/nguyenq/tess4j |  
| tesseract | OCR | https://github.com/tesseract-ocr/tesseract |   
| barcode4j | 生成条码 | http://barcode4j.sourceforge.net |  
| zxing | 生成/解码条码 | https://github.com/zxing/zxing |   
| flexmark-java | 转换Markdown | https://github.com/vsch/flexmark-java |   
| commons-compress | 归档/压缩 | https://commons.apache.org/proper/commons-compress |   
| XZ for Java | 归档/压缩 | https://tukaani.org/xz/java.html |   
| jaffree | 封装ffmpeg | https://github.com/kokorin/Jaffree |   
| ffmpeg| 媒体转换/生成 | http://ffmpeg.org |   
| image4j | ico格式 | https://github.com/imcdonagh/image4j |   
| AutoCommitCell | 提交修改 | https://stackoverflow.com/questions/24694616 （Ogmios） |   
| 高德 | 地图 | https://lbs.amap.com/api/javascript-api/summary |      
| 高德 | 坐标 | https://lbs.amap.com/api/webservice/guide/api/georegeo |      
| 微博 | 图片素材 | https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665 |      
| 百度 | COVID-19数据 | https://voice.baidu.com/act/newpneumonia/newpneumonia/?from=osari_pc_3 |      
| 腾讯 | COVID-19数据 | https://api.inews.qq.com/newsqa/v1/query/pubished/daily/list?province=湖北&city=武汉 |      
| poi | Excel | https://poi.apache.org |      
| LabeledBarChart | JavaFx图 | https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland) |      
| commons-csv | CSV | https://commons.apache.org/proper/commons-csv/ |      
| geonames | 位置数据 | https://www.geonames.org/countries/ |      
| world-area | 位置数据 | https://github.com/wizardcode/world-area |      
| 中国国家统计局 | 数据 | http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/ |      
| JHU | COVID-19数据 | https://github.com/CSSEGISandData/COVID-19 |      
| 懒人图库 | 色彩数据 | https://tool.lanrentuku.com/color/china.html |      
| 中国纹样全集 | 素材 | https://book.douban.com/subject/3894923/ |      
| 中国国家基础地理信息中心 | 地图 | http://lbs.tianditu.gov.cn/api/js4.0/guide.html |      
| movebank | 位置数据 | https://www.datarepository.movebank.org |      
| CoordinateConverter | 坐标转换 | https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage |      
| JavaMail | email | https://javaee.github.io/javamail/ |      
| Commons IO | 文件读写 | https://commons.apache.org/proper/commons-io/ |      
| colorhexa | 色彩数据 | https://www.colorhexa.com/color-names |      
| 文泉驿 | 开源字体 | http://wenq.org/wqy2/ |      
| ttc2ttf | 提取ttf | https://github.com/fermi1981/TTC_TTF |      


# 当前版本
当前是版本6.3.8，已实现的特点概述如下:
* [跨平台](#cross-platform)
* [国际化](#international)
* [本机](#localhost)
* [文档工具](#documentTools)
    - [PDF工具](#pdfTools)
    - [文本编辑的基础](#editTextBase)
    - [编辑文本](#editText)
    - [编辑字节](#editBytes)
    - [编辑网页](#htmlEditor)
    - [网页工具](#htmlTools)
    - [编辑Markdown](#markdownEditor)
    - [转换文档](#convertDocuments)
* [图像工具](#imageTools)
    - [查看图像](#viewImage)
    - [浏览图像](#browserImage)  
    - [分析图像](#ImageData)  
    - [图像处理](#imageManufacture)   
    - [多帧图像文件](#multiFrames)
    - [多图合一](#multipleImages)
    - [图像局部化](#imagePart)
    - [图片转换](#imageConvert)
    - [识别图像中的文字](#imageOCR)
    - [颜色管理](#ColorManagement)
    - [调色盘](#ColorPalette)
    - [色彩空间](#colorSpaces)
    - [其它](#imageOthers)
    - [大图片的处理](#bigImage)
* [数据工具](#dataTools)
    - [数据文件](#dataFiles)
    - [数据粘贴板](#dataClipboard)
    - [矩阵](#matrix)
    - [数据管理](#dataManage)
    - [地图数据](#mapData)
    - [地理编码](#geographyCode)
    - [地图上的位置](#locationInMap)
    - [位置数据](#locationData)
    - [位置工具](#locationTools)
    - [疫情报告](#epidemicReport)
    - [生成条码](#createBarcodes)
    - [解码条码](#decodeBarcodes)
    - [消息摘要](#messageDegist)
* [文件工具](#fileTools)
    - [管理文件/目录](#directoriesArrange)
    - [归档/压缩/解压/解档](#archiveCompress)
    - [检查冗余文件](#filesRedundancy)
    - [其它](#fileOthers)  
* [媒体工具](#MediaTools)
    - [播放视频/音频](#mediaPlayer)
    - [管理播放列表](#mediaList)
    - [封装ffmpeg的功能](#ffmpeg)
    - [游戏-消消乐](#gameElimination)
    - [游戏-挖雷](#gameMine)
    - [其它](#mediaOthers)
* [网络工具](#netTools)
    - [下载第一级链接](#downloadFirstLevelLinks)
    - [微博截图工具](#weiboSnap)
    - [解码/编码URL](#encodeDecodeURL)
    - [浏览器](#webBrowser)
    - [管理安全证书](#securityCerificates)
* [设置](#settings)
* [窗口](#windows)
* [帮助](#helps)
* [对于高清晰屏幕的支持](#Hidpi)    
* [开发模式](#DevMode)    
* [MyBox日志](#MyBoxLogs)    
    
## 跨平台<a id="cross-platform" />   
MyBox用纯Java实现且只基于开源库，因此MyBox可运行于所有支持Java 15的平台。       
MyBox v5.3以前的版本均基于Java 8。           
 
## 国际化<a id="international" />
1. 所有代码均国际化。可实时切换语言。
2. 一种语言对应两个资源文件："Messages_语言名.properties"、"TableMessages_语言名.properties"。  
3. 内置中文和英文， 在目录`MyBox/src/main/resources/bundles/`中：       

| 语言 | 界面的资源文件 | 数据表的资源文件 |            
| --- | --- |  --- |        
| 中文 | Messages_zh_CN.properties | TableMessages_zh_CN.properties |                 
| 英文 | Messages_en.properties | TableMessages_en.properties |        

4. 支持在线添加语言。提供表格，对照英语翻译。新语言可实时生效。     
例如，新语言名字为“aa”，则它的资源文件是：Messages_aa.properties和TableMessages_aa.properties。     
5. 新语言可共享给别人：把资源文件复制到数据目录的子目录"mybox_languages"下，则MyBox可即时感知到新语言。

![截屏-封面](https://mararsh.github.io/MyBox/snap-cover.jpg)

## 本机<a id="localhost" />
1. 如无必要，不访问网络。
2. 如无必要，不读不写。


## 文档工具<a id="documentTools" />

### PDF工具<a id="pdfTools" />
1. 以图像模式查看PDF文件，每页被转换为一张图片：     
	-  标签和缩略图
	-  可设置dpi以调整清晰度
	-  可把页面剪切保存为图片
	-  识别页面中的文字（OCR）
2. 以网页模式查看PDF文件，可逐页查看和编辑页面和html。
3. 批量转换：
	-  将PDF文件的每页转换并保存为一张图片文件，包含图像密度、色彩、格式、压缩、质量、色彩转换等选项。
	-  转换PDF文件中被选择页的图片，并保存为新的PDF。
	-  压缩PDF文件中的图片，保存为新的PDF。可设置JPEG质量或者黑白色阈值
	-  将PDF转换为网页，可选：每页保存为一个html、还是整个PDF保存为一个html；字体文件/图像文件是嵌入、单独保存、还是忽略。
4. 批量提取：
	-  将PDF中的图片提取出来。
	-  将PDF文件中的文字提取出来，可以定制页的分割行。
	-  识别PDF文件中图片的文字（OCR）。
5. 分割PDF文件为多个PDF文件，可按页数或者文件数来均分，也可以设置起止列表。
6. 合并多个PDF文件。 
7. 将多个图片合成PDF文件
8. 写PDF的选项：页面尺寸、图片属性、字体文件、页边、页眉、作者等。
9. 修改PDF的属性，如：标题、作者、版本、修改时间、用户密码、所有者密码、用户权限等


![截屏-pdf](https://mararsh.github.io/MyBox/snap-pdf.jpg)       

### 文本编辑的基础<a id="editTextBase" />
1. 编辑功能（复制/粘贴/剪切/删除/全选/撤销/重做/恢复）及其快捷键。    
2. 查找与替换。
	-  选项：忽略大小写、回绕。
	-  查找字串和替换字串都支持支持多行。其中的换行符按照当前文件换行符的定义来处理。
	-  支持正则表达式，提供示例。
	-  计数
	-  由于算法的限制，对于多页文档查找规则表达式时，假设：匹配的字符串的最大长度小于当前JVM可用内存的1/16。
3. 定位。跳转到指定的字符位置或行号。
4. 行过滤：
	-  条件：包含/不包含任一、包含/不含所有、包含/不包含规则表达式、匹配/不匹配规则表达式。 
	-  可累加过滤。
  	-  可保存过滤结果。可选是否包含行号。  
5. 分页。可用于查看和编辑非常大的文件，如几十G的运行日志。
	-  设置页尺寸。
	-  页面导航。
	-  先加载显示首页，同时后端扫描文件以统计字符数和行数；统计期间部分功能不可用；统计完毕自动刷新界面。
	-  对于跨页字符串，确保查找、替换、过滤的正确性。
6. 定时自动保存。

![截屏-textEditor](https://mararsh.github.io/MyBox/snap-textEditor.jpg)         

### 编辑文本<a id="editText" />
1. 自动检测或手动设置文件编码；改变字符集实现转码；支持BOM设置。
2. 自动检测换行符；改变换行符。显示行号。     
   支持LF（Unix/Linux）、 CR（Apple）、 CRLF（Windows）。
3. 字符集对应的编码：字节的十六进制，同步显示、同步选择，可选同步滚动、同步更新。


### 编辑字节<a id="editBytes" />
1. 字节被表示为两个十六进制字符。所有空格、换行、非法值将被忽略。
2. 常用ASCII字符的输入选择框。
3. 换行。仅用于显示、无实际影响。显示行号。可按字节数换行、或按一组字节值来换行。
4. 选择字符集来解码：同步显示、同步滚动、同步选择。非字符显示为问号。
5. 分页。若按字节数换行，则行过滤时不考虑跨页。


### 编辑网页<a id="htmlEditor" />
1. 加载或编辑本地网页或在线网页，可以彼此同步内容：
	-  网页浏览器
	-  编辑网页富文本（不支持FrameSet）
	-  编辑网页代码。（支持FrameSet）
	-  编辑Markdown
2. 显示：
	-  转换的文本
	-  提取网页中标题
	-  提取网页中的链接

![截屏-textEditor](https://mararsh.github.io/MyBox/snap-htmlEditor.jpg)        

### 网页工具<a id="htmlTools" />      
1. 网页截图：可设置dpi，看选保存为一张图还是多图保存在PDF中。
2. 合并多个网页为一个网页/Markdown/文本/PDF文件。
3. 对多个文件生成框架文件。      

### 编辑Markdown<a id="markdownEditor" />
1. 提供输入格式的按钮。
2. 显示，可选是否同步更新：
	-  转换的网页
	-  转换的网页代码
	-  转换为文本
	-  提取网页中标题
	-  提取网页中的链接

### 转换文档<a id="convertDocuments" />
1. 批量转换文本：
	-  转换文件的字符集。
	-  转换文件的换行符。
	-  转换文本为网页。
	-  替换文本中的字符串。
2. 批量转换网页：
	-  转换网页为Markdown。
	-  转换网页为文本。
	-  转换网页为PDF。
	-  修改网页编码。
3. 批量转换Markdown：
	-  转换Markdown为网页。
	-  转换Markdown为文本。
	-  转换Markdown为PDF。

## 图像工具<a id="imageTools" />

### 查看图像<a id="viewImage" />
1. 设置加载宽度：原始尺寸或指定宽度。
2. 选择区域。
3. 旋转可保存。
4. 删除、重命名、恢复。
5. 可选显示：坐标、横标尺、纵标尺、数据。
6. 查看图像的元数据和属性，可解码图像中嵌入的ICC特性文件。
7. 同目录下图像文件导览，多种文件排序方式。  
8. 右键菜单。
9. "剪裁"、“复制”、“保存为”、和各个功能是针对内存中当前图片所选择的区域。

![截屏-imageViewer](https://mararsh.github.io/MyBox/snap-imageViewer.jpg)       

### 浏览图像<a id="browserImage" />
1. 同屏显示多图，分别或者同步旋转和缩放。
2. 旋转可选保存。
3. 格栅模式：可选文件数、列数、加载宽度
4. 文件列表模式
5. 缩略图列表模式
6. 重命名、删除    

![截屏-imageBrowser](https://mararsh.github.io/MyBox/snap-imageBrowser.jpg)       


### 分析图像<a id="ImageData" /> 
1. 统计显示图像的数据：各颜色成分的均值/方差/斜率/中值/众数/最大/最小，以及直方图。  
2. 直方图的颜色成分可多选。
3. 可针对选择的矩形区域做统计显示。  
4. 计算主色调：    
	-  利用K-Means聚类计算最不同的颜色。 
	-  利用统计量化计算出现最多的颜色。      
	-  可将计算结果导入调色板。       
5. 图像数据可以被保存为html文件。   

![截屏-imageAnanlyse](https://mararsh.github.io/MyBox/snap-imageAnanlyse.jpg)     

### 图像处理<a id="imageManufacture" />
1. 复制：   
	-  复制：当前范围以内的部分、当前范围之外的部分、或整个图片
	-  选择：是否切除边沿、是否同时复制到系统粘贴板
	-  设置背景色   
2. 剪裁：
	-  剪切：当前范围以内的部分、或当前范围之外的部分
	-  选择：是否切除边沿、是否把剪切下来的部分放入粘贴板
	-  设置背景色   
3. 粘贴板。
	-  数据来源：     
		-  对图像整体或选择的部分做"复制"（CTRL+c）  
		-  剪切下来的图片部分  
		-  系统粘贴板  
		-  系统中的图片文件  
		-  示例图片    
	-  管理粘贴板列表：增、删、清除、刷新，可设置最多保存数。
	-  编辑图像时按粘贴按钮（CTRL+v）以把粘贴板的第一张图贴到当前图片上，也可以双击粘贴板列表的项目以粘贴。
	-  在当前图片上拖拉被粘贴图片，调整大小和位置。
	-  粘贴选项：是否保持宽高比、混合模式、不透明度、旋转角度。
4. 伸缩：拖动锚点调整大小、按比例收缩、或设置像素。四种保持宽高比的选项。设置渲染参数。
5. 色彩：针对红/蓝/绿/黄/青/紫通道、饱和度、明暗、色相、不透明度，进行增加、减少、设值、过滤、取反色的操作。可选是否预乘透明。
6. 效果：海报（减色）、阈值化、灰色、黑白色、褐色、浮雕、边沿检测、马赛克、磨砂玻璃。可选算法和参数。
7. 增强：对比度、平滑、锐化、卷积。可选算法和参数。
8. 富文本：以网页形式编辑文本，在图片上拖放调整文本的大小和位置。可设置背景的颜色、不透明度、边沿宽度、圆角大小，可设置文字的旋转角度。   
    由于是利用截屏实现，结果比较模糊，还没有好的解决办法。（此版本暂时不支持）
9. 文字：设置字体、风格、大小、色彩、不透明度、阴影、角度，可选是否轮廓、是否垂直，点击图片定位文字。
10. 画笔：
	-  折线：多笔一线。可选画笔的宽度、颜色、是否虚线、不透明度。
	-  线条：一笔一线。可选画笔的宽度、颜色、是否虚线、不透明度。
	-  橡皮檫：一笔一线。总是透明色，可选画笔的宽度。
	-  磨砂玻璃：一点一画。可选画笔的宽度、模糊强度、形状（圆形还是方形）。
	-  马赛克：一点一画。可选画笔的宽度、模糊强度、形状（圆形还是方形）。
	-  形状：矩形、圆形、椭圆、多边形。可选画笔的宽度、颜色、是否虚线、不透明度、是否填充、填充色。
11. 变形：斜拉、镜像、旋转，可设置参数。
12. 圆角：把图像四角改为圆角，可设置背景色、圆角大小。
13. 阴影：可设置背景色、阴影大小、是否预乘透明。
14. 边沿：模糊边沿，可设置是否预乘透明；拖动锚点以调整边沿；按宽度加边；按宽度切边；按颜色切边。可选四边、颜色。
15. 修改历史：
	- 对于图片的每一次修改，工具可以自动保存为图片历史。
	- 管理历史：删除、清除、选择并恢复为当前图片，可设置最多保存的历史个数。
	- 对上一步的撤销（CTRL+z）和重做（CTRL+y）。可以随时恢复原图（CTRL+r）。也可以选择历史列表中任意图片来恢复。
16. "范围"：定义操作针对的像素内容，既可定义区域、定义颜色匹配规则，也可同时定义区域和颜色匹配。
	- 定义区域：可以是矩形、圆形、椭圆、多边形，区域可反选。
	- 定义要匹配颜色列表，可以利用调色盘在图片上直接取色。
	- 选择颜色匹配的对象，可以是红/蓝/绿通道、饱和度、明暗、色相，色距可定义。颜色匹配结果可反选。
	- 抠图：匹配像素周围的像素、并按同一匹配规则持续扩散出去。多个像素点的匹配合集就是结果。
	- 轮廓：把背景透明的图片的轮廓自动提取出来，作为操作的范围。
	- 范围可作用于：复制、剪切、颜色、效果、和卷积。
	- 保存和管理范围：增、删、改、清除，应用已保存的范围。    
17. 弹出图片：当前图片可以显示在弹出的新窗口中，可选择弹出窗口是否总是在最上面。 
18. "按需可见"的界面布局：
	- 显示/隐藏左面板（F4）、右面板（F5）
	- 显示/隐藏范围面板（F7）、图片面板（F8）
	- 上下风箱式页签
	- 叠加多页签切换
	- 按功能显示/隐藏控件
19. 演示：对于粘贴的混合模式、"颜色"、"效果"、"增强"，一键展示各种数据处理的示例。
20. 批量图像处理。

![截屏-imageManufacture](https://mararsh.github.io/MyBox/snap-imageManufacture.jpg)       


### 多帧图像文件<a id="multiFrames" />
1. 查看、提取多帧图像文件
2. 创建、编辑多帧tiff文件
3. 查看/提取/创建/编辑动画Gif文件。可设置间隔、是否循环、图片尺寸    

### 多图合一<a id="multipleImages" />
1. 图片的合并。支持排列选项、背景颜色、间隔、边沿、和尺寸选项。
2. 将多个图片合成PDF文件
3. 添加透明通道   

### 图像局部化<a id="imagePart" />
1. 图像的分割。支持按个数分割、按尺寸分割、和定制分割。可以保存为多个图像文件、多帧Tiff文件、或者PDF。
2. 图像的降采样。可以设置采样区域、采样比例。
3. 当图片文件包含太多像素而被采样加载时，分割和降采样的是文件中的原图片而非加载到内存的图片。
4. 提取透明通道。      

### 图片转换<a id="imageConvert" />
1. 可选图像文件的格式，包括：png,jpg,bmp,tif,gif, ico, wbmp,pnm,pcx, raw。
2. 可选颜色空间，包括：sRGB、Linear sRGB、ECI RGB、Adobe RGB、Apple RGB、Color Match RGB、ECI CMYK、Adobe CMYK(多种)、灰色、黑白色。
3. 可选外部ICC特性文件作为转换的依据。
4. 对于jpg/png格式可选是否嵌入ICC特性文件，对于Tif格式必选嵌入。
5. 可选对透明通道（如果有）的处理：保留、删除、预乘并保留、预乘并删除。
6. 可选压缩类型和质量。
7. 对于黑白色，可选二值化算法：OTSU、缺省、或输入预置，可选是否抖动处理。
8. 批量转换。   

### 识别图像中的文字<a id="imageOCR" />
1. 对图像预处理：
	-  多种图像算法
	-  伸缩比例
	-  黑白阈值
	-  旋转角度。
	-  是否自动矫正偏斜
	-  是否反色
2. 文字识别的选项：
	-  数据文件列表及其顺序
	-  是否生成"区域"数据，及其粒度
	-  是否生成"词"数据，及其粒度
3. 单图识别：
	- 可以保存并加载预处理后的图像。
	- 可以设置需要识别的矩形区域。
	- 同步显示：预处理后的图像、原图、和识别出的文字及其html。
	- 以html显示"区域"数据和"词"数据，并可保存为文件。
	- 演示：一键展示各个图像增强算法的示例。
4. 批量识别：
	-  可选是否同时生成html或PDF
	-  可选是否合并识别出文字
5. OCR引擎：
	-  对于win，可以选择内置的tesseract引擎、或用户安装的tesseract。
	-  对于linux和mac，只能使用用户安装的tesseract。
6. OCR数据文件目录：
	-  可以设为任何可读可写的目录。若已安装tesseract，建议设为它的子目录 "tessdata"。
	-  MyBox内置英文和中文的"最快的"数据文件，若此目录下没有这些文件，则MyBox将它们把复制到此目录下。      
注意：当使用内置引擎时，文件/目录名最好是纯英文，以免失败。      

![截屏-ocr](https://mararsh.github.io/MyBox/snap-ocr.jpg)       

### 颜色管理<a id="ColorManagement" />
1. 在颜色库中增/删/改任意色彩：给颜色命名、把颜色加入/移出调色板。
2. 简单/全部显示颜色属性，或合并/分列显示颜色属性。
3. 导出当前页、全部、或选择的颜色为html或csv文件。
4. 导入颜色文件，CSV格式：
	-  文件编码是UTF-8或ASCII
	-  第一行定义数据头，以英文逗号分隔。
	-  其余每行定义一条数据，数据域以英文逗号分隔。
	-  以下为必要数据域：     
                 rgba 或 rgb
	-  以下是可选数据域：     
                 name
5. 用户可输入颜色列表。提供示例。      
有效的颜色值示例：              
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
[常用网页颜色列表](http://mararsh.github.io/MyBox_data/colors/%E5%B8%B8%E7%94%A8%E7%BD%91%E9%A1%B5%E9%A2%9C%E8%89%B2.html)                   
6. 内置：常用网页色彩、传统中国色彩、传统日本色彩、来自colorhexa.com的颜色。   

### 调色盘<a id="ColorPalette" />
1. 色块显示颜色。弹出：颜色的名字（如果有）、十六进制值、rgb值、hsb值、不透明值、cmyk值、cie值。
2. 拖动色块以调整颜色的顺序。 

![截屏-colors](https://mararsh.github.io/MyBox/snap-colors.jpg)       

### 色彩空间<a id="colorSpaces" />
1. 绘制色度图
	-  标准数据的轮廓线：CIE 1931 2度观察者（D50）、CIE 1964 10度观察者（D50）、CIE RGB色域、ECI RGB色域、sRGB色域、
	   Adobe RGB色域、Apple RGB色域、PAL RGB色域、NTSC RGB色域、ColorMath ProPhoto RGB色域、SMPTE-C RGB色域。
	-  标准光源（白点）：A、C、D50、D55、D65、E。
	-  用户可填写刺激值或色坐标、或选择色彩，工具自动计算各种色彩空间对应的色彩数值、并把计算值显示在色度图上。
	-  用户可输入或用文件导入光谱数据，工具自动过滤掉特殊字符、并把光谱数据显示在色度图上。
	-  用户可以选择在色度图上显示/不显示以上数据。
	-  用户可选色度图的背景为透明/白色/黑色，可选轮廓线的点尺寸或线尺寸，可选是否显示格栅和波值。
	-  工具以表格和文本显示标准数据：CIE 1931 2度观察者1nm、CIE 1931 2度观察者5nm、CIE 1964 10度观察者1nm、CIE 1964 10度观察者5nm，
	   用户可导出数据的文本。
2. 编辑ICC色彩特性文件
	-  预置标准ICC文件：Java内嵌的ICC文件（包括sRGB、XYZ、PYCC、GRAY、LINEAR_RGB）、
	   ECI提供的ICC文件（包括ECI_CMYK、ECI_RGB_v2）和Adobe提供的ICC文件（包括Adobe RGB、Apple RGB、及多种CMYK ICC文件）。
	-  头部所有字段可编辑。在保存ICC文件时，工具自动计算"profile id"字段（MD5摘要）。
	-  标签表：标签、名字、类型、偏移、大小、描述、解码后的数据、数据的原始值（十六进制字节）
	-  可编辑的标签类型：Text、MultiLocalizedUnicode、Signature、DateTime、XYZ、Curve、ViewingConditions、Measurement、S15Fixed16Array。
	   当前版本不支持编辑LUT类型的标签。
	-  选项：把LUT表中的数据归一化到0~1。
	-  整个ICC数据被解析显示为XML，并可导出。未被解码的数据显示为十六进制字节。
	-  读入的ICC数据可以修改另存为新的ICC文件。
3. RGB色彩空间
	-  用户选择或输入RGB色彩空间（基色和白点）、选择或输入要适应的参考白点，工具自动计算色适应后的基色值，并展示计算过程。
	-  可设置小数位数。
	-  色适应算法可选：Bradford、XYZ Scaling、Von Kries。
	-  预置的标准RGB色彩空间包括：CIE RGB、ECI RGB、sRGB、Adobe RGB、Apple RGB、PAL RGB、NTSC RGB、ColorMath ProPhoto RGB、SMPTE-C RGB
    -  预置的标准光源包括CIE 1931和CIE 1964的：A、B、C、D50、D55、D65、D75、E、F1~F12。 
	-  工具以表格和文本显示：不同的标准RGB色彩空间、不同的标准光源、不同的算法所计算出的色适应后的基色值。用户可导出数据的文本。
4. 线性RGB到XYZ的转换矩阵
	-  用户选择或输入RGB色彩空间（基色和白点）、选择或输入XYZ空间的参考白点，工具自动计算线性RGB到XYZ的转换矩阵，并展示计算过程。
	-  以表格和文本显示：不同的标准RGB色彩空间、不同的XYZ空间参考白点、不同的算法所计算出的转换矩阵。用户可导出数据的文本。
5. 线性RGB到线性RGB的转换矩阵
	-  用户选择或输入源和目标的RGB色彩空间（基色和白点），工具自动计算源线性RGB到目标线性RGB的转换矩阵，并展示计算过程。
	-  工具以表格和文本显示：不同的标准RGB色彩空间之间以不同的算法所计算出的转换矩阵。用户可导出数据的文本。
6. 光源
	-  用户输入源颜色（相对值/色度坐标/刺激值）、选择或输入源白点和目标白点，工具自动计算色适应后的颜色值，并展示计算过程。
	-  工具以表格和文本显示标准光源的数据值、色温和说明。用户可导出数据的文本。
7. 色度适应矩阵
	-  用户选择或输入源白点和目标白点，工具自动计算色度适应矩阵，并展示计算过程。
	-  工具以表格和文本显示不同标准光源之间不同的算法的色度适应矩阵。用户可导出数据的文本。   

![截屏-colorDiagram](https://mararsh.github.io/MyBox/snap-colorDiagram.jpg)       

### 其它<a id="imageOthers" />
1. 支持图像格式：png,jpg,bmp,tif,gif,ico,wbmp,pnm,pcx。可读Adobe YCCK/CMYK的jpg图像。
2. 像素计算器
3. 卷积核管理器

 
### 大图片的处理<a id="bigImage" />
1. 评估加载整个图像所需内存,判断能否加载整个图像。
2. 若可用内存足够载入整个图像，则读取图像所有数据做下一步处理。尽可能内存操作而避免文件读写。
3. 若内存可能溢出，则采样读取图像数据做下一步处理。
4. 采样比的选择：即要保证采样图像足够清晰、又要避免采样数据占用过多内存。
5. 采样图像主要用于显示图像。已被采样的大图像，不适用于图像整体的操作和图像合并操作。
6. 一些操作，如分割图像、降采样图像，可以局部读取图像数据、边读边写，因此适用于大图像：显示的是采样图像、而处理的是原图像。  

## 数据工具<a id="dataTools" />

### 数据文件<a id="dataFiles" />
1. 编辑数据文件：
	- 对于CSV文件：
 		- 选项包括：文件的字符集、是否以第一行作为字段的名字、字段的分隔符。
	- 对于Excel文件：
 		- 选项包括：工作表号、是否以第一行作为字段的名字。
 		- 工具只能处理Excel文件中的基本数据。如果文件包含格式、风格、或图，建议把修改保存为新文件以免数据丢失。
	- 文件中的数据应当是等宽的，即所有行的列数相同。
	- 数据被加载到表单中：
 		- 在表格单元中编辑数据。
 		- 将鼠标移至行/列的头部，以弹出此行/列的功能菜单。
 		- 将鼠标移至按钮上，以弹出所有/选择的行/列的功能菜单。
 		- 功能包括：设置列宽、选择、设值、复制、粘贴、插入、删除、排序。
	- 数据分页：
 		- 当功能超出当前页时，运行功能之前必须保存当前页的修改。
 		- 对所有页的修改是直接写文件，所以无法恢复。
	- "数据定义"用以描述和约束数据：
 		- 列名不能为空也不能重复。
 		- "数据类型"和"是否为空"用来检验数据值的合法性。
 		- 无论文件有无列名头行，数据定义都将被保存在数据库中。
 		- 点击“清除”按钮以删除数据库中的数据定义。文件将被重新加载。
	- 同步显示数据的文本格式和网页格式：
 		- 可选择文本格式的分隔符。
 		- 可利用数据粘贴板来修改复制数据。
2. 批量转换数据文件：
	- 源文件格式：csv、excel。可以设置源文件的选项。
	- 目标文件格式：csv、excel、xml、json、html、pdf。可以设置目标文件的选项。      

### 数据粘贴板<a id="dataClipboard" />       
1. 粘贴或输入文本，工具逐行按指定的分隔符解析数据。
2. 同步显示数据的文本格式和网页格式。    


### 矩阵<a id="matrix" />
1. 编辑矩阵：
	- 定义矩阵属性：名字、行数、列数、精度、描述。
	- 在表格单元中修改行-列的数值。
	- 将鼠标移至行/列的头部，以弹出此行/列的功能菜单。
	- 将鼠标移至按钮上，以弹出所有/选择的行/列的功能菜单。
	- 功能包括：设置列宽、选择、设值、复制、粘贴、插入、删除、排序。
	- 同步显示数据的文本格式和网页格式。
	- 利用数据粘贴板进行修改复制数值。特殊字符将被忽略。
2. 矩阵的一元计算：转置、行阶梯形、简化行阶梯形、行列式值-用消元法求解、行列式值-用余子式求解、逆矩阵-用消元法求解、逆矩阵-用伴随矩阵求解、矩阵的秩、伴随矩阵、余子式、归一化、乘以数值、除以数值、幂。
3. 矩阵的二元计算：加、减、乘、克罗内克积、哈达马积、水平合并、垂直合并。
4. 保存矩阵。编辑或计算后的矩阵可以保存在数据库中，以方便引用。  


### 通用的数据管理<a id="dataManage" />      
1. 定义数据
2. 数据约束：
	- 约定：
 		- 整型数值（integer/long/short）的无效值是最小值（MIN_VALUE）
 		- 双精度数值的无效值是最大值（Double.MAX_VALUE）
	- 坐标系统：
		- 可取值：
			- CGCS2000（中国大地坐标），真实位置，近似于WGS_84(GPS)坐标。
			- GCJ-02（中国加密坐标），经过加密的数据，与真实位置有偏差。
			- WGS-84（GPS），真实位置
			- BD-09（百度加密坐标），基于GCJ-02
			- Mapbar（图吧坐标），基于GCJ-02
		- 当坐标系统未定义或非法时，缺省值为CGCS2000。
	- 坐标值： 
		- 数据处理时均用经纬度的小数而不是“度分秒”（DMS）。
		- MyBox提供“位置工具”以帮助转换坐标的小数和度分秒。                     
		- 经度有效值范围：`-180~180`，纬度有效值范围：`-90~90`。
	- 时间：
		- 格式：      
			- 日期时间，如：2014-06-11 13:51:33
			- 日期，如：2014-06-11
			- 年，如：2014
			- 月，如：2014-06
			- 时间，如：13:51:33
			- 带毫秒的时间，如：13:51:33.261
			- 带毫秒的日期时间，如：2014-06-11 13:51:33.261
			- 带时区的日期时间，如：2020-09-27 12:29:29 +0800
			- 带毫秒和时区的日期时间，如：2020-09-27 12:29:29.713 +0800
			- 日期和时间之间可以有或没有“T”。“2014-06-11T13:51:33”等同于“2014-06-11 13:51:33”。
		- 纪元：        
 	  	 	  	 "0 AD" = "1 BC" = "0" = "-0" = "0000" = "-0000"  = "0001-01-01 00:00:00 BC" =  "公元前1" = "公元前0001-01-01 00:00:00"            
 	  	 	  	 "1 AD" =  "1"  = "0001" = "0001-01-01 00:00:00" = "0001-01-01 00:00:00 AD" =  "公元1" = "公元0001-01-01 00:00:00"           
 	  	 	  	 "202 BC" = "-203" = "-0203" = "-0203-01-01 00:00:00"  = "0202-01-01 00:00:00 BC" = "公元前202" =  "公元前0202-01-01 00:00:00"           
 	  	 	  	 "202 AD" = "202" = "0202" = "0202-01-01 00:00:00" = "0202-01-01 00:00:00 AD" = "公元202" = "公元0202-01-01 00:00:00"            
		- 有效的时间示例：                  
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
3. 增/删/改/复制/清除/刷新数据。
4. 查询数据：
 	-  定义和管理查询条件。
 	-  当前查询条件被显示在"信息"页签上。
 	-  符合条件的数据分页显示在"数据"表中。        
 	-  数据行可以按数据值显示不同颜色。
5. 导入数据，csv格式：
 	- 文件编码是UTF-8或ASCII。   
	- 第一行定义数据头，以英文逗号分隔。
	- 其余每行定义一条数据，字段以英文逗号分隔。
	- 字段的顺序随意。
	- 必要字段都应占位，但不一定必须有值（与具体数据有关）。  
	- 可选是否替换已存在的数据。预定义数据或者示例数据总是替换。    
6. 导出数据：
 	- 定义和管理导出条件。
 	- 导出的数据字段可选。
	- 导出格式可选：csv、xml、json、xlsx、html。
 	- 可选择导出文件的分割行数。
 	- 可导出当前数据页。
7. 删除数据：
 	-  定义和管理删除条件。
 	-  预定义的数据无法被删除。
 	-  被引用的数据（如外键引用）无法被删除。
8. 定义、管理、和应用"条件"：
 	-  "条件"被用来执行：查询、删除、导出。
 	-  在面板中设置条件：
 	  	- 数据条件形成树结构，树结点可以多选。
 	  	- 排序的字段可以多选，顺序可调。
 	-  编辑条件：标题、where、order by、fetch，它们被拼接成最终条件。
 	-  管理条件：增/删/改/复制。
 	-  被执行过的条件被自动保存。
	-  最近执行过的条件被列出在按钮的弹出窗口中。


### 地图数据<a id="mapData" />
1. 在地图上可以展示多种数据：地理编码、位置数据、或坐标查询。
2. 显示在地图上的数据可以是：
 	-  满足当前查询的所有数据。可以设置“最多个数”以免性能问题。
 	-  当前数据页。 
3. 天地图：
 	-  接受CGCS2000坐标数据，并把它们显示为无偏差的正确位置。
 	-  对于其它坐标数据，MyBox把它们转换为CGCS2000以显示正确位置。
 	-  可选投影：EPSG:900913/3857（球面墨卡托）、EPSG:4326（经纬度直投）。
 	-  可选控件：缩放、缩放比例、地图类型、符号。
 	-  地图类型：地图、卫星、卫星混合、地形、地形混合。
 	-  地图自带语言
 	-  地图级别为1-18
4. 高德地图：
 	-  接受GCJ-02坐标，并把它们显示为无偏差的正确位置。
 	-  对于其它坐标数据，MyBox把它们转换为GCJ-02以显示正确位置。
 	-  投影为EPSG:900913/3857（球面墨卡托）。
 	-  图层：
 	  	- 可多选：标准图层、卫星图层、路网图层、交通图层。
 	  	- 外国坐标不支持路网图层和交通图层。
 	  	- 部分外国坐标支持卫星图层。
 	  	- 可分别设置每个图层的不透明度。 
 	-  地图的语言：中文、英文、中英文。
 	-  地图级别为3-18
 	-  可选“适应地图”，即将地图自动调整为可以显示所有数据的最佳大小和位置。
5. 调整地图级别：
 	-  滑动鼠标滚轮。
 	-  点击地图控件。
 	-  选择“地图尺寸”。
6. 标注图片：
 	-  可选：点（泡泡）、圆形、任意图片
 	-  对于位置数据，还可选：数据集图像、数据图像。若无有效值，则为点。
 	-  可设置标记图片的尺寸（长宽相同）。
7. 标注文字：
 	-  可选：标签、坐标、地址。
 	-  对于位置数据，还可选：开始时间、结束时间、数据值等。
 	-  可多选。每个选择显示为一行。
 	-  可设置标记文字的尺寸。
 	-  可选是否粗体。
 	-  可设置标记文字的颜色。对于位置数据，还可选：数据颜色。
8. 弹出信息：
  	-  鼠标放在标注上则可弹出更多信息。
  	-  可选是否弹出。
9. 截图：
  	-  可设置截图分辨率。
  	-  把当前地图和图中数据截取为html
10. 地图的数据密钥可以在“设置”里修改。 缺省的密钥是所有MyBox用户共享的免费密钥。
11. 显示地图时，信任地图服务商的所有主机地址。
     

### 地理编码<a id="geographyCode" />
1. 数据定义： 
	-  基本属性：标识、级别、经度、纬度、中国名、英文名、5个代码、5个别名。  
	-  从属属性：属主、洲、国家、省、市、县、镇、村、建筑。（构成"祖先"）        
	-  辅助属性：高度、精度、坐标系统、面积（平方米）、人口、注释、是否预定义。
2. 数据约束：
 	-  必须有值：标识、级别、中文名或英文名 
 	-  "级别"的可取值：全球（只能是"地球"）、洲、国家、省（州）、市、县（区）、镇（乡）、村（居委会）、建筑、兴趣点。
 	- 数据不必逐级从属，即可以跨级定义，例如：一个村庄直接属于南极洲；又如：城市直接属于国家，而没有省/州一级。
 	- 匹配数据：
 	  	- 以下方式之一可以确定一个地址：
 	  	 	- 匹配数据标识（由MyBox自动赋值）。这是精确匹配。
  	  	 	- 匹配"级别" + "祖先" + "中文名"或"英文名"或任一别名"。这是精确匹配。
 	  	 	- 匹配"级别" + "中文名"或"英文名"或任一"别名"。这是模糊匹配，可能有同级重名导致匹配错误的情况。       
 	  	- 匹配名字或者别名时，不区分大小写。            
 	  	- 有时候"代码"（code1/2/3/4/5）也可以辅助查找。      
3. 编辑数据：
 	-  数据的"从属关系"只能从位置树上选择。
 	-  数据的级别必须比祖先低。
 	-  数据必须有中文名或者英文名。
 	-  可在地图上选择/显示坐标。
 	-  对选择的数据项设置为："预定义的数据"、或"输入的数据"。
4. 定义条件：
  	- 地理代码按级别和从属关系形成一棵树，可多选
5. 导入数据：
 	-  内置的预定义数据：洲、国家、中国的省/市/县。     
           国家的"面积"和"人口"有有效值。       
 	-  CSV格式。            
 	  	- 下载地址：       
                         https://github.com/Mararsh/MyBox_data/tree/master/md/GeographyCode       
 	  	-  以下为必要字段：        
                         Level,Longitude,Latitude            
                         以及"Chinese Name"或 "English Name"            
 	  	-  以下是可选字段：     
                         Altitude,Precision,Coordinate System,Square Kilometers,Population,           
                         Code 1,Code 2,Code 3,Code 4,Code 5,Alias 1,Alias 2,Alias 3,Alias 4,Alias 5,            
                         Continent,Country,Province,City,County,Town,Village,Building,Comments                   
 	-  来自geoname.org的位置数据。          
 	  	- 下载地址：            
                         http://download.geonames.org/export/zip/         
 	  	-  以制表符分隔的文本，编码为UTF-8。
 	  	-  字段：     
                           countryCode postalCode placeName                      
                           adminName1 adminCode1 adminName2 adminCode2 adminName3 adminCode3              
                           latitude longitude accuracy             
 	  	-  坐标系统是WGS_84。    
 	  	-  同一地址只写一次，即使它有多个邮编或者坐标。   
6. 设置：
 	-  定制数据行颜色。提供"缺省"和"随机"按钮。

![截屏-geoCode](https://mararsh.github.io/MyBox/snap-geoCode.jpg)       


### 地图上的位置<a id="locationInMap" />
1. 查询地理代码：
 	-  点击地图
 	-  输入地址       
 	  	-  天地图支持中外地址的中英文（如“伦敦”、“Paris”）
 	  	-  高德地图只支持中国地址的中文。
 	-  输入坐标  
2. 可以保存查询出来的地理代码。

![截屏-geoCode](https://mararsh.github.io/MyBox/snap-locationMap.jpg)       

### 位置数据<a id="locationData" />
1. 数据定义： 
	-  基本属性：数据集、标签、经度、纬度、开始时间、结束时间。  
	-  辅助属性：地址、高度、精度、坐标系统、速度、方向、数据值、数据规模、图像、说明。
2. 数据集：
 	-  每个位置数据都属于一个数据集。 
 	-  数据集定义其所包含的位置数据的共同属性，包括：
 	  	- 时间格式
 	  	- 对于公元后的时间是否省略“公元”
 	  	- 文字的颜色
 	  	- 图像      
            这些属性有助于在地图上显著区分数据点。
3. 定义条件：
  	- 数据集列表，可多选
 	- 时间树（开始时间），可多选 
4. 地图数据：
  	- 开始显示地图数据时，以第一个数据为地图中心。  
  	- 位置分布：所有数据全部显示在地图上。
  	- 时间序列：
 	  	- 按“开始时间”升序逐帧显示数据
 	  	- 选项：    
 	  	  	- 若选择“累加”，则已显示帧的数据点不被抹除，即数据点逐帧叠加。
 	  	  	- 若选择“时间叠加”，则凡是时间区间与当前帧的时间区间有重叠的数据都算作当前帧的有效数据。       
                    	   例如，当前帧的开始时间是“公元前1044”、结束时间是“公元前221”，则所有起止区间与这个时间段有重合的数据都显示在当前帧中。
 	  	  	- 若选择“移到中心”，则每帧都会调整地图中心。    
 	  	  	- 若选择“链接”，则显示相邻两帧的连线。      
                           注：只有天地图的国内地址才正常显示连线。    
 	  	- 帧控制：
 	  	  	- 可以设置时间间隔    
 	  	  	- 可以选择帧（开始时间）    
 	  	  	- 暂停/播放   
 	  	  	- 前/后帧   
 	  	  	- 是否循环播放  
5. 截图：
  	- 对于“位置分布”，可选
 	  	- html：数据和当前帧的截图
 	  	- 当前帧的截图。格式自选。
  	- 对于“时间序列”，还可以选择：
 	  	- jpg：所有帧的截图
 	  	- png：所有帧的截图
 	  	- 动态gif：所有帧的截图（可能内存不够）
6. 导入数据：
 	- CSV格式。            
 	  	-  以下为必要字段：       
                           Dataset,Longitude,Latitude             
 	  	-  以下是可选字段：     
                           Label,Address,Altitude,Precision,Speed,Direction,Coordinate System,         
                           Data Value,Data Size,Start Time,End Time,Image,Comments           
 	-  来自movebank.org的位置数据。          
 	  	- 下载地址：            
                         https://www.datarepository.movebank.org/          
 	  	-  以逗号分隔CSV格式。
 	  	-  以下为必要字段：       
                           timestamp,location-long,location-lat,study-name         
 	  	-  坐标系统是WGS_84。    
 	- 示例：中国历代都城     
 	- 示例：欧洲赤膀鸭的秋季迁徙模式     
 	- 示例：墨西哥湾的抹香鲸        
 	- 若数据包含的数据集在数据库中还未定义，则自动添加到数据库中。      

![截屏-geoCode](https://mararsh.github.io/MyBox/snap-locationData.jpg)       


### 位置工具<a id="locationTools" />
1. 转换坐标的小数和度分秒。有效的“度分秒”（DMS）示例：             
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
2. 把坐标转换为其它坐标系。 

![截屏-geoCode](https://mararsh.github.io/MyBox/snap-locationTools.jpg)       


### 疫情报告<a id="epidemicReport" />
1. 数据定义： 
	-  基本属性：数据集、时间、位置、来源。  
	-  基础数值：确认、治愈、死亡。
	-  差值统计：新增确认、新增治愈、新增死亡。         
            由前后两天数据行的差值计算出。
	-  除值统计：      
	  	- 治愈/确认千分比、死亡/确认千分比。
	  	- 确认/平方公里千分比、治愈/平方公里千分比、死亡/平方公里千分比。
	  	- 确认/人口千分比、治愈/人口千分比、死亡/人口千分比。         
            当位置数据的"面积"/"人口"值非法（零或负数）时，相应的统计值没有意义（零）。         
            预定义的"国家"数据有合法的"面积"和"人口"值，因此可以得到有意义的统计值。          
	-  累加统计：
	  	- 一些国家的数值            
                   由此国家的省的数值累加计算出。          
	  	- 洲的数值                
                  由此洲的国家的数值累加计算出。             
	  	- 全球的数值              
                  由各洲的数值累加计算出。        
2. 数据约束：
 	-  必须有值：数据集、时间、位置
 	- "来源"的取值："输入的数据"、"预定义数据"、"填充的数据"、"统计数据"。
 	- "位置"是"地理编码"的外键，即所引用的位置必须在"地理编码"中有定义。
 	- "确认"、"治愈"、"死亡"这三个值至少有一个大于零。
	- 以下方式之一可以确定一个疫情报告：     
 	  	- 匹配数据标识（由MyBox自动赋值）。这是精确匹配。
  	  	- 匹配"数据集 + "日期" + "位置"。这是精确匹配。
	- 此版本假设，对于确定的数据集、确定的地址，一天只有一个有效数据。
3. 编辑数据：
 	-  输入单个数据时，"位置"只能从位置树上选择。
 	-  在"中国省会疫情报告"和"全球疫情报告"界面上，可以填写多个地址的同一数据集同一天的数据。
 	-  对选择的多个数据项修改"来源"的取值。
4. 导入数据：
 	-  MyBox内置预定义数据：来自约翰霍普金斯大学的COVID-19数据（直到2020-09-24）。
 	-  CSV格式：      
 	  	-  下载地址：         
                           https://github.com/Mararsh/MyBox_data/tree/master/md/COVID19              
 	  	-  以下为必要字段：           
                           数据集,时间,确认,治愈,死亡                     
                   以及足以定义一个地理编码的位置数据：                 
                          经度,纬度,级别,洲,国家,省,市,区县,乡镇,村庄,建筑物,兴趣点          
 	  	-  以下是可选字段：           
                         新增确诊,新增治愈,新增死亡             
	  	-  坐标系统是CGCS2000。           
 	-  来自约翰霍普金斯大学的COVID-19历史数据（全球）：     
  	  	-  下载地址：         
                           https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series               
 	  	-  以下为必要字段：           
                           Province/State,Country/Region,Lat,Long           
                   以及日期列表“1/22/20,1/23/20...”                           
	  	-  坐标系统是WGS_84。           
	  	-  澳大利亚、加拿大、和中国的数据是省/州级别的，其它的数据是国家级别。      
	  	-  全部是零的数据项将被跳过。           
 	-  来自约翰霍普金斯大学的COVID-19每日数据（全球）。     
  	  	-  下载地址：         
                           https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_daily_reports s               
 	  	-  字段随着时间在变化。。。          
                      以下是"01-22-2020.csv"的格式：          
                             Province/State,Country/Region,Last Update,Confirmed,Deaths,Recovered          
                     以下是"05-15-2020.csv"的格式：          
                             FIPS,Admin2,Province_State,Country_Region,Last_Update,Lat,Long_,Confirmed,Deaths,Recovered,Active,Combined_Key                                      
	  	-  坐标系统是WGS_84。             
	  	-  全部是零的数据项将被跳过。           
 	-  来自百度的COVID-19今日数据（全球）。     
            下载地址：https://voice.baidu.com/act/newpneumonia/newpneumonia/?from=osari_pc_3       
 	-  来自腾讯的COVID-19历史数据（中国）。     
            下载地址：https://api.inews.qq.com/newsqa/v1/query/pubished/daily/list?       
 	-  导入时可选是否统计数据集。      
 	-  导入数据的时间均被改为"23:59:00"。      
 	-  若数据包含的地理代码在数据库中还未定义，则自动添加到数据库中。      
5. 统计数据：
 	-  可选累加数据：
 	  	-  省/州的数据累加为国家的数据
 	  	-  国家的数据累加为洲的数据
 	  	-  洲的数据累加为地球的数据。
 	-  可选差值数据的位置级别。
6. 定义"条件"：
 	-  "数据源树"：数据集、及其不同数据源形成一棵"数据源树"，树结点可以多选。
 	-  "位置树"：MyBox中所有的地理编码按从属关系形成一棵"位置树"，树结点可以多选。
 	-  "时间树"：MyBox中所有的疫情报告涉及的时间按年/月/日形成一棵"时间树"，树结点可以多选。
 	-   "每日首部数据的个数"：
 	  	- 不限制。不显示图，只按条件查询数据。
 	  	- 有效值：
 	  	  	- 按条件查询数据后、截取每日最前面的数据，以此显示数据和图。
  	  	  	- "时间降序"自动被设置为最前面的排序条件。
  	  	  	- 还必须再选择至少一个排序字段
 	  	  	- 除了"时间降序"，最前面的排序字段被称为"查询的主属性"
        - "每日首部数据的个数"和排序条件只对查询和导出有效，对删除无效。     
 	-  编辑条件：标题、where、order by、fetch、"每日首部数据的个数"(-1或0表示不限制)，它们被拼接成最终查询条件。
7. 显示图：
 	-  当查询条件符合要求时，才会显示图：图数据总是"每日首部数据"，并有一个"查询的主属性"。
 	-  除了"查询的主属性"，可以选择更多数据属性，以在图中显示多维数据、或者同时显示多个数据的图。
 	-  可选的图类型：水平条图、垂直条图、水平线图、垂直线图、饼图、地图。
 	-  当数据的时间不唯一时，图是动态的：按时间升序逐帧显示每个时间的数据图。
 	-  对于动态图，可以暂停/继续、设置间隔、停在指定时间的帧、上一帧、下一帧。
 	-  常用选择，即时生效：
 	  	- 图例位置：不显示、顶、底、左、右
 	  	- 数值标签：名和值、值、名、不显示、弹出
 	  	- 显示/不显示：类别轴的标签、水平网格线、垂直网格线
 	  	- 数值轴：笛卡尔坐标、方根坐标、自然对数坐标、以10为底的对数坐标
 	  	- 文字大小
 	  	- 地图的参数：级别、图层、语言
 	-  截图
 	  	- 当前帧的截图。格式自选。
 	  	- jpg：所有帧的截图
 	  	- png：所有帧的截图
 	  	- 动态gif：所有帧的截图（可能内存不够）
8. 设置：
 	-  截图的分辨率、动态截图的最大宽度、加载图数据的时间。        
            这些参数与内存消耗和计算机计算能力有关。    
 	-  按"数据源"定制数据行颜色。提供"缺省"和"随机"按钮。
 	-  定制图中数据值的颜色。提供"缺省"和"随机"按钮。
 	-  定制图中位置值的颜色。提供"随机"按钮。      

![截屏-epidemicReport](https://mararsh.github.io/MyBox/snap-epidemicReport.jpg)       


	

### 生成条码<a id="createBarcodes" />
1. 支持的一维码类型： Code39, Code128, Codabar, Interleaved2Of5, ITF_14, POSTNET, EAN13, EAN8, EAN_128, UPCA, UPCE,
        Royal_Mail_Customer_Barcode, USPS_Intelligent_Mail
2. 支持的二维码类型：QR_Code, PDF_417, DataMatrix
3. 一维码选项：朝向、宽高、分辨率、文字位置、字体大小、空白区宽度等。不同类型的选项不同。
4. 二维码选项：宽高、边沿、纠错级别、压缩模式。不同类型的选项不同。
5. 二维码QR_Code可以在中心显示一个图片。根据纠错级别自动调整图片大小。
6. 示例参数和建议值。
7. 对生成的条码即时检验。

### 解码条码<a id="decodeBarcodes" />
1. 支持的一维码类型： Code39, Code128, Interleaved2Of5, ITF_14,  EAN13, EAN8, EAN_128, UPCA, UPCE
2. 支持的二维码类型：QR_Code, PDF_417, DataMatrix
3. 显示条码内容和元数据（条码类型、纠错级别等） 

### 消息摘要<a id="messageDigest" />
1. 生成文件或者输入文本的消息摘要   
2. 支持MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512/224, SHA-512/256, SHA3-224, SHA3-256, SHA3-384, SHA3-512  
   

## 文件工具<a id="fileTools" />

### 管理文件/目录<a id="directoriesArrange" />
1. 查找、删除、复制、移动、重命名。
2. 目录同步，包含复制子目录、新文件、特定时间以后已修改文件、原文件属性，以及删除源目录不存在文件和目录，等选项。
3. 整理文件，将文件按修改时间或者生成时间重新归类在新目录下。此功能可用于处理照片、游戏截图、和系统日志等需要按时间归档的批量文件。
4. 删除目录下所有的空目录。
5. 删除"无限嵌套目录"（由于软件错误而生成的无法正常删除的目录）。  
6. 删除系统临时目录下的文件。  
7. 从ttc文件中提取ttf文件。

### 归档/压缩/解压/解档<a id="archiveCompress" />  
1. 归档是把多个文件/目录聚集为单个文件的过程，有的归档格式支持同时实现压缩（如zip和7z）。解档是还原归档文件的过程。 
2. 压缩是把单个文件转变为一个更小的文件的过程。通常是先归档再压缩。解压是还原压缩文件的过程。  
3. 支持归档格式： zip, tar, 7z, ar, cpio。  
4. 支持解档格式： zip, tar, 7z, ar, cpio, arj, dump。  
5. 支持压缩格式：gzip, bzip2, xz, lzma, Pack200, DEFLATE, snappy-framed, lz4-block, lz4-framed。 
6. 支持解压格式：gzip, bzip2, xz, lzma, Pack200, DEFLATE, snappy-framed, lz4-block, lz4-framed, DEFLATE64,  Z。 
7. 解档/解压时，可自动检测格式，也可由用户指定格式（有的格式无法自动检测出来）。 
8. 解档时，提供树状结构方便用户选择提取内容。 

### 检查冗余文件<a id="filesRedundancy" />  
1. 根据MD5检查重复的文件。  
2. 提供树状结构方便用户删除冗余文件。   
3. 支持边检查边删除。  

### 其它<a id="fileOthers" />
1. 切割文件。切割方式可以是：按文件数、按字节数、或按起止列表。
2. 合并文件。
3. 比较文件（字节）。
4. 批量处理时，选择文件的方式：扩展名、文件名、文件大小、文件修改时间，支持正则表达式。

## 媒体工具<a id="MediaTools" />

### 播放视频/音频<a id="mediaPlayer" />
1. 创建/加载播放列表
2. 选项：自动播放、显示毫秒、循环次数、随机顺序
3. 设置：音量、速度（0~8倍）
4. 按键：播放/暂停/停止/上一个/下一个/媒体信息/静音/全屏
5. 全屏时：触屏短暂显示控件、ESC退出全屏
6. 支持的容器格式：aiff, mp3, mp4, wav, hls(m3u8)，支持的视频编码：h.264/avc，支持的音频编码：aac, mp3, pcm。   
    如果视频没有声音，这是因为播放器不支持它的音频编码器。    
    已知问题：播放一些流媒体时MyBox可能崩溃退出。   
7. 乖乖和笨笨的声音
8. 此工具无需ffmpeg。但是在Linux上需要安装libavcodec和libavformat， 参见：      
https://www.oracle.com/technetwork/java/javafx/downloads/supportedconfigurations-1506746.html

### 管理播放列表<a id="mediaList" />
1. 增删改播放列表
2. 增删改播放列表的内容。
3. 读取所支持媒体格式的信息：时长、音频编码、视频编码

![截屏-mediaPlayer](https://mararsh.github.io/MyBox/snap-mediaPlayer.jpg)       


### 封装ffmpeg的功能<a id="ffmpeg" />
注：这一组功能依赖于ffmpeg，需要用户自己下载ffmpeg（建议使用静态版本）。
1. 处理媒体时：
	-  可选择/设置所有的参数，包括文件格式（合成器）、视频编码、音频编码、字幕、视频帧率、宽高比、音频采样率、改变音量等。
	-  “缺省”按钮：当有NVIDIA时，选择"h264_nvenc"作为视频编码器，以利用硬件加速。
	- 多数播放器支持：合成器"mp4"，视频编码"H.264", 音频编码"AAC"。
	- 尝试不同的编码器、编码预调、CRF值，以获得适应于你的计算机的设置，考虑以下因素：
 	  	- 编码器应该足够快以保证不掉帧。
 	  	- 消耗有限的系统资源，以留给其它应用足够的CPU和内存。
 	  	- 生成的文件的质量和大小是可忍受的。
2. 录屏：
	-  当没有NVIDIA、并且CPU不那么强时：
 	  	- 选择"libx264rgb"作为视频编码器，以跳过从RGB到yuv444p的转换。
 	  	- 选择较快的编码预调。
 	  	- 录制之后，利用视频转换工具以编码器“libx264”将生成的RGB视频转换为yuv444p。
	-  可选是否录视频：
 	  	- 设置线程队列大小。
 	  	- 录制范围：全屏、窗口、矩形。
 	-  可选是否录音频：
 	  	- 自动检测声卡，并把第一个声卡作为设备。
 	  	- 设置线程队列大小。
 	-  设置延时：
 	  	- 若是”不限制“，则用户按按钮”开始“时立即开始录制。
 	  	- 若是有效值，则在这个时间结束时开始录制。
 	-  设置时长：
 	  	- 若是”不限制“，则用户按按钮”停止“时才停止。
 	  	- 若是有效值，则时间结束时自动停止。用户按按钮”停止“也可中止录制。
3. 批量转换音频/视频：
	-  源文件以文件/目录表显示
	-  源文件以流和媒体信息表显示
4. 把图片和音频合成为视频：
	-  源文件以文件/目录表显示
	-  源文件以流和媒体信息表显示
	-  可以单独设置每个图片的时长，也可对全部图片设置时长
	-  可选择是否"音频流结束时结束视频"。
	-  图片被自动适应为屏幕大小且保持宽高比。
5. 利用ffprobe读取媒体的信息：格式、音频流、视频流、帧、包、支持的像素格式。
6. 读取ffmpeg的信息：版本、格式、支持的编码解码器、支持的滤镜，以及自定义查询参数。    


![截屏-makeMedia](https://mararsh.github.io/MyBox/snap-makeMedia.jpg)       


### 游戏-消消乐<a id="gameElimination" />
1. 可选棋子的图片、个数、尺寸、显示效果（是否阴影/圆角）。
2. 棋子可以是预定义图片、用户指定的任意图片、或用户选择的颜色。  
3. 可选音效：来自乖乖的赞许、来自笨笨的赞许、3连由笨笨赞许其它由乖乖赞许、静音、或任意mp3/wav文件。   
4. 可选计分的棋子：只有消除选择的棋子的连线，才能得分。   
5. 可设置得分规则：定义不同的连接个数对应的分数值。   
6. 可设置僵局（没有有效的交换步骤）时的处理策略：保留得分并重置游戏、制造机会、或弹出提示让用户选择。
7. 可设置：自动玩游戏的速度、消除时的闪烁次数、是否弹出得分。
8. "帮我"按钮：为用户提示有效的步骤。
9. "自动玩"按钮：点击即自动玩游戏、再次点击则停止自动玩。

![截屏-game](https://mararsh.github.io/MyBox/snap-game.jpg)       


### 游戏-挖雷<a id="gameMine" />
1. 可以设置格子大小和地雷的个数，提供示例。
2. 可以偷看地雷。  
3. 触雷后可以恢复棋局。

![截屏-game](https://mararsh.github.io/MyBox/snap-mine.jpg)   


### 其它<a id="mediaOthers" />
1. 记录系统粘贴板中的图像：保存或查看粘贴板中的图像，可选无损图像或压缩类型。
2. 闹钟，包括时间选项和音乐选项，支持铃音"喵"、wav铃音、和MP3铃音，可以在后端运行。

![截屏-snap](https://mararsh.github.io/MyBox/snap-snap.jpg)       


## 网络工具<a id="netTools" />

### 下载第一级链接<a id="downloadFirstLevelLinks" />
1. 列出给定网址中的第一级链接。   
2. 下载用户选择的链接:
	-  选择有用的链接。无意义的链接可能生成无用文件并且干扰最后的目录索引。
	-  使用功能“设置子目录名”以使子目录名字合理。
	-  使用功能“以链接名/标题/地址为文件名”以使文件名有意义。
	-  使用功能“在文件名前添加序号”以使文件名能正确排序。         
       MyBox可以正确排序形如"xxx9", "xxx36", "xxx157"的文件名。
3. 选项：重写网页中的链接、生成目录索引、修改网页编码、合并为文本/网页/Markdown/PDF。
4. 可以设置网页样式和PDF的字体。   

### 微博截图工具<a id="weiboSnap" />
1. 自动保存任意微博账户的任意月份的微博内容、或者其点赞的内容。
2. 设置起止月份。
3. 确保页面完全加载，可以展开页面包含的评论、可以展开页面包含的所有图片。
4. 将页面保存为本地html文件。由于微博是动态加载内容，本地网页无法正常打开，仅供获取其中的文本内容。
5. 将页面截图保存为PDF。可以设置截图的格式、像素密度，和PDF的页尺寸、边距、作者等。
6. 将页面包含的所有图片的原图全部单独保存下来。
7. 实时显示处理进度。
8. 可以随时中断处理。程序自动保存上次中断的月份并填入作本次的开始月份。
9. 可以设置错误时重试次数。若超时错误则自动加倍最大延迟时间。    

![截屏-weibo](https://mararsh.github.io/MyBox/snap-weibo.jpg)   

### 解码/编码URL<a id="encodeDecodeURL" />
此工具帮助将字符串与application/x-www-form-urlencoded MIME之间转换。      
在编码时应用以下规则：         
-  "a"到"z"之间的字母、 "A"到 "Z"之间的字母、以及 "0"到 "9"之间的数字保留原样。
-  特殊字符".", "-", "*", 和 "_"保留原样。
-  空字符" "转换为加号"+"
-  其它所有字符被认为是不安全的，首先采用指定的字符集转换为1个或多个字节。          
   然后每个字节被表达为形如 "%xy"的3个字符的字符串，其中xy是字节的十六进制两位数字表示。                 

解码反过来。       


### 浏览器<a id="webBrowser" />
1. 多页签显示网页
2. 管理浏览历史
3. 在线安装网站SSL证书。
4. 可选忽略指定网站或全部网站的SSL证书的验证（可用于证书有问题的网页，但是可能导致安全风险）。

### 管理安全证书<a id="securityCerificates" />
1. 读取任意密钥库文件中的证书内容，可导出为html文件
2. 添加/读取任意证书文件的内容
3. 下载并安装任意网址的证书。
4. 删除密钥库中的证书。
5. 修改密钥库时自动备份

## 设置<a id="settings" />
1. 界面：
	-  语言、字体大小、图标大小
	-  可选择高清晰图标（100px）还是普通图标（40px）。         
           显示器分辨率不高于120dpi时，建议选择普通图标。在非高清晰的显示器上显示高清晰图标反而会模糊。          
	-  控件颜色、是否显示控件文字、界面风格
	-  是否恢复界面上次尺寸
	-  是否在新窗口中打开界面
	-  显示/隐藏面板的方式：鼠标经过、点击鼠标
2. 基础：
	-  JVM最大可用内存
	-  是否关闭分辨率感知
3. 数据：
	-  数据目录
	-  Derby运行模式：嵌入模式、网络模式（只允许本地）
	-  是否弹出最近访问的文件/目录、以及弹出个数
	-  退出程序时是否关闭闹钟
4. PDF工具：
	-  PDF可用最大主内存
5. 图像工具：
	-  画笔/锚点的宽度和颜色、锚点是否实心
	-  不支持Alpha时要替换的颜色（建议为白色）
	-  缩略图宽度
	-  采样图像最大显示宽度
	-  是否往系统粘贴板中复制图片
6. 地图：
	-  设置数据密钥
7. 开发：
	-  开启/关闭“开发模式”
	-  源码目录
8. 清除个人设置。
9. 打开数据目录。

## 窗口<a id="windows" />
1. 开/关内存监视条
2. 开/关CPU监视条
3. 刷新/重置/全屏窗口
4. 关闭其它窗口
5. 最近访问的工具

## 帮助<a id="helps" />
1. MyBox快捷键：
	-  若焦点在“文本输入”控件中，则Delete/Home/End/PageUp/PageDown/Ctrl-c/v/z/y/x作用于此控件中的文本。否则，快捷键作用于界面。
	-  若焦点不在“文本输入”控件中，Ctrl/Alt键可省略。例如，焦点在图片上时，按"c"以复制，按"2"以设置为面板尺寸。
2. MyBox的属性
3. 文档：帮助用户启动下载任务。若数据目录下已有MyBox文档，则MyBox会自动发现它们。


## 对于高清晰屏幕的支持<a id="Hidpi" />     
1. Java 9以后已支持HiDPI，控件和字体都会适应当前清晰度配置。MyBox支持在线关闭/打开DPI敏感，修改时MyBox会自动重启。       
开发者需要注意的是：JavaFx虚拟屏幕的dpi不同于物理屏幕的dpi，对于窗口元素尺寸的计算还要考虑伸缩比。     
2. 用户可以选择高清晰图标还是普通图标。     

## 开发模式<a id="DevMode" />  
1. 在“设置”中开启/关闭开发模式。
2. 以下功能只有处于“开发模式”才可见：
	-  菜单项：“帮助”-“制作图标”
	-  颜色管理的菜单项：“导入MyBox颜色”
3. 类型为“调试”的日志只在开发模式下才写入数据库。

## MyBox日志<a id="MyBoxLogs" />  
1. MyBox日志类型包括：错误、信息、调试、终端：
	-  所有日志都会显示在终端。
	- “错误”和“信息”总是写入数据库。
	- ”调试“只在开发模式下才写入数据库。
	- ”终端“从不写入数据库。
2. 字段：标识、时间、类型、文件名、类名、方法名、行号、调用者、注释。
3. “调用者”是调用链，每行是一个节点：文件名、类名、方法名、行号。调用链只记录MyBox自身的方法。
4. “错误”会导致查看日志的界面被弹出。

# 开发日志<a id="devLog" />          
2021-1-27 版本6.3.8                  
-  添加：编辑和转换csv和excel文件。数据粘贴板。编辑和保存矩阵。批量设置html的风格。          
-  改进：颜色量化可以设置通道权重。焦点不在文本输入控件时快捷键可以省略Ctrl/Alt。数据转换/导出采用流读取。                 
-  解决的主要问题：批量替换图片颜色的距离不生效。PDF的用户密码和所有者密码混乱。文本过滤界面分页错误。表字段也需要翻译。时间树上公元前的时间解析错误。                    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.8)               

2020-12-03 版本6.3.7                  
-  添加：从ttc文件中提取ttf文件。内置一个开源免费的ttf文件。          
-  改进：可以选择高清晰图标和普通图标。可以批量设置html的编码。统一PDF字体选择控件。图像伸缩处理时可以设置渲染参数。设置mac的缺省视频解码器为VideoToolBox。                 
-  解决的主要问题：解压缩gz文件会毁坏源文件并且不断增大文件。html编码可能解析错误。字节编辑的右面板只显示一行。                     
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.7)                    

2020-11-29 版本6.3.6         
-  代码：升级到java 15.0.1和javafx15.0.1（除javafx-web模块）。MyBox日志自行实现，不再依赖log4j2。        
-  文件：增加文件删除/重命名/移动的健壮性。可删除系统临时目录下的文件。       
-  颜色：量化后的结果可导入调色盘。kmeans cluster量化可设置最大循环数。可导入用户的颜色csv文件。颜色导入的关键字可以是rgb。        
-  解决的主要问题：文件删除会造成最近访问文件列表弹出错误。合并图片设置列数会出错。           
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.6)        

2020-11-18 版本6.3.5        
-  改进：文档的查找/替换有效处理正则表达式和分页；完善读取图片的算法；更顺畅地处理大图片；调整网页编辑器界面。    
-  增加：每个版本都有自己的配置文件；菜单按钮；若干文档转换功能；编码/解码URI字串；下载第一级链接。     
-  移除：启动时不再允许改变derby模式；删除“管理下载”功能。   
-  解决的主要问题：文档的查找/替换对于分页处理不准确；图片列表不应加载原文件；批处理可能产生无限嵌套目录；图片处理界面有错误的处理。                 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.5)        
感谢[beijingjazzpanda](https://github.com/Mararsh/MyBox/issues/781)的帮助。      

2020-10-11 版本6.3.4       
-  编辑器：查找/替换支持多行、忽略大小写、从光标处、和正则表达式的示例；弹出文档；关闭/打开右面板；设置是否同步更新右面板。
-  OCR：基于tesseract命令行，支持win/linux/mac，兼容版本3/4/5；支持设置psm和所有参数，提供参数列表。
-  录屏：支持mac；开始和结束时都喵；缺省设置帧率和码率。
-  游戏“挖雷”：可以设置格子大小和地雷的个数，提供示例；可以偷看地雷；触雷后可以恢复棋局。
-  解决的主要问题：文档的查找/替换对于正则表达式的定位不准确；版本升级的逻辑不正确。        
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.4)               

2020-9-27 版本6.3.3        
-  图像处理：显示/隐藏范围面板和图片面板；当粘贴超出边界时增大图片；混合模式的示例；卷积核参数“反色”。
-  数据工具：方便输入CSV；时间支持毫秒和时区；地图逐点显示；疫情报告保存为一组截图。
-  颜色：输入颜色列表；设置颜色、图片取色、调色板管理分为不同的界面；可改变调色板上颜色的顺序。
-  其它：表格和图片的右键菜单；编辑器定时自动保存；录屏可延时；录屏支持Linux；开发模式。
-  解决的主要问题：“清除个人设置”是删除用户配置数据而不是删除所有数据；一些数据表处理错误；快捷键在Linux上无效；新位置数据无法添加到新数据集。     
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.3)       

这一版献给中国，祝她生日快乐！    

2020-8-27 版本6.3.2  完善“位置数据”。按“位置分布”和“时间序列”在地图上显示数据。3个数据集的示例。可导入movebank.org的数据文件。              
完善数据在地图上的显示。支持天地图和高德地图的切换。       
“位置工具”：转换坐标的小数值和度分秒；把坐标值转换为其它坐标系统。       
FFmpeg应用：录屏。目前只支持windows。        
以CSV格式导入/导出颜色数据。         
全自动的打包脚本。《开发指南》v2.1。                  
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.2)           

2020-6-11 版本6.3.1  升级至java 14.0.1 + javaFx 14.0.1(除“javafx-web”模块) + NetBeans 11.3。        
仍支持旧版本的迁移而不是截断版本。        
完善“地理编码”表定义以提高查询速度。“面积”单位由平方公里改为平方米。                
可以设置地图的数据密钥。       
解决问题：“最近访问的文件”列表没有限制长度。“疫情报告”的饼图数据值应当是百分值。            
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3.1)      

2020-5-25 版本6.3  重新设计和实现"地理编码"和"疫情报告"。        
音/视频转换时提供音频参数的选择。       
提供常用中国颜色和常用日本颜色。      
解决问题：文本编辑保存后光标应留在原地。选项"为目录下文件计数"可能使批处理失败。            
这一版献给我妈。祝所有母亲都爱与被爱。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.3)               

2020-3-3 版本6.2.1  完善疫情报告。节点"除了中国"：查询和显示中国以外的数据。      
填充的数据用不同颜色显示。编辑填充的数据或点按钮"确认"以把填充数据改为正常状态。        
支持不包含"省"级的"国家-城市"的结构（除中国以外的其它国家是这种行政层级）。      
解决问题："地理代码"和"疫情报告"的编辑界面没有正确处理。截图时可能内存溢出。           
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.2.1)              

2020-2-28 版本6.2  完善疫情报告。"填充数据空洞"：按前面的数据自动添加缺失的数据。      
动态条图和多位置时间序列线图。可选是否在图示上显示数据值。可以设置动态图的每帧时长。      
数据导出格式添加xlsx(Excel2007)。自增字段不再被导出，且不影响导入。        
解决问题：负值坐标显示被空白；疫情报告的统计值没有被正确更新；疫情报告的编辑界面缺少字段"级别"；改变动态gif的宽度时没有正确处理。          
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.2)         

2020-2-21 版本6.1.5  读取"新型冠状病毒肺炎"的数据。解析百度数据页面，获得当前最新实时数据；调用腾讯查询接口，获得从2020-1-20起的中国省/市/区的历史数据。
加入地理属性。数据写为html、json、xml，并可写入"疫情报告"表中。       
完善疫情报告。位置分四级：全球、国家、省、市/区。统计属性"新增值"及其图示。示例包含2020-1-20到2020-2-21中国省和市区的数据以及部分国家的数据。     
完善地理代码。修正错误和残缺。      
解决问题：编辑器的过滤功能出现错误。          
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.1.5)         

2020-2-11 版本6.1  地图上的位置：通过点击地图、输入地址、或输入坐标来查询和显示位置数据。可设置地图的标记、文字、图层、语言等。       
通用的数据表管理基础：增/删/改数据；分页显示；导入/导出；全部或选择导出为html。    
管理地理编码：在地图上查询和显示定位。示例：所有国家和中国省/直辖市/特别行政区。     
管理位置数据：属性包含位置信息、数据集/标签/值/规模/时间/图像/说明等。数据时间支持"公元前"。示例：中国早期文化（未完成）。      
地图上的位置数据：在地图上显示数据集的分布。可按照数据值设置地图的标记、文字、弹出信息。     
疫情报告：属性包含位置信息、疫情数据。示例：新型冠状病毒肺炎（不完整且可能有错）。数据分析：维度包括时间和位置；时间分量为全球和中国；位置分量为国家和中国省。
图示：基于位置分量的数值条图/比率条图/饼图/数值地图、基于时间分量的数值线图/比率线图/动态数值地图。 数据和图示可导出为html文件。       
支持ico图像文件的读/写/转换。     
动态gif：解释更多的元数据；按实际间隔显示每帧；编辑时可为每帧单独设置间隔。    
管理颜色：增/删/改颜色库；把颜色加入/移出调色板；导出全部或选择的颜色；简单/全部显示颜色属性；合并/分列显示颜色属性。     
微博截图工具：可以保存博主点赞的页面及其引用的图片。     
文件工具：删除目录下所有的空目录；删除"无限嵌套目录"（由于软件错误而生成的无法正常删除的目录）。     
改善游戏消消乐：棋子可为任意图片文件或任意颜色；声效可以是任意mp3/wav文件；优化消除算法；"帮我"按钮为用户提示有效的步骤；可自动玩游戏。        
解决问题：目录操作可能生成"无限嵌套目录"；"将图片和音频合成为视频"：错误处理小于1秒的时长、错误处理多帧图片；"编辑html"在高清屏下截图不完整；
首次运行时无法打开Markdown文件。        
这一版献给中国和她的孩子们，他们正在与病魔战斗。期盼春暖花开！     
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.1)      

2020-1-2 版本6.0  图像浏览界面可以方便地弹出大图。    
支持在线翻译MyBox：在表格中对照英语翻译。资源文件可分享给别人：把新资源文件复制到数据目录下，则MyBox即时感知到新语言。   
游戏消消乐： 可选棋子的图片、个数、尺寸、是否阴影/圆角；音效：来自乖乖或笨笨的赞许；可选计分棋子；可设置不同连接个数对应的分数值。   
下载管理：添加、删除、启动、取消下载任务；断点续传；读取下载地址的头数据。   
解决问题：修正多个关于图像处理的问题。        
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.0)      

2019-12-26 版本5.95  改进批处理界面：使用多页签而不是把控件挤在一个页面上。      
解决问题：避免微博截图工具414错误；图像处理的界面控件显示逻辑混乱；图像批处理的保存格式不生效。        
今天缅怀毛主席和他的战友， 他们使中国人民站起来了。   
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.95)         

2019-12-21 版本5.9 支持多页签的浏览器。 可管理浏览历史、在线安装网站SSL证书。   
读取任意密钥库文件中的证书内容，可导出为html文件。在密钥库中添加证书文件的内容、或下载安装网址的证书。   
视频/音频播放器，可设置自动播放、显示毫秒、循环次数、随机顺序、音量、速度、静音、全屏等。乖乖和笨笨的声音。管理播放列表。   
封装ffmpeg的功能：批量转换音频/视频、把图片和音频合成为视频、读取媒体的信息、读取ffmpeg的信息。   
消息摘要扩展为12种算法。   
解决问题：表单元失焦时应自动提交修改；检查文件冗余时抛出并发异常；添加包含大量文件的目录会使界面僵住；批处理解包7z格式失败；zip包中文件大小未知。     
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.9)    


2019-11-18 版本5.8  升级至jdk13+javafx13+derby15。  
Derby数据库可以在网络模式和嵌入模式之间切换。提醒：在一些机器上启动和关闭Derby网络模式都非常慢。   
可对文件或输入文本生成消息摘要。支持：MD5/SHA1/SHA256。   
文件的归档/压缩/解压/解档。支持：zip, tar, 7z, ar, cpio, gzip, bzip2, xz, lzma, Pack200, DEFLATE, snappy-framed, lz4-block, lz4-framed等。  
检查文件冗余：根据MD5查找重复的文件，提供树状结构方便用户删除冗余文件，可边查边删。  
html与Markdown之间批量互换。   
解决问题：多个界面上有失效控件；无内容页面会阻塞微博截图工具；图像混合时还要考虑背景透明的情形；图像"换色"功能失效。   
《开发者指南》v2.0。  
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.8)    


2019-10-26 版本5.7  编辑Markdown。同步转换html和Markdown。   
增加图像量化的算法并用于分析图象：K-Means适用于计算最不同的颜色，统计量化适用于计算出现最多的颜色。图象数据可以保存为html文件。   
文件/目录操作：查找、删除、复制、移动，并改进重命名功能。   
改进批量处理时选择文件的方式：扩展名、文件名、文件大小、文件修改时间，支持正则表达式。   
改进多个工具的界面以平衡控件的布局。   
改进微博截图工具：提高在高清屏幕上截图的分辨率；截图保存为文件以免内存溢出。   
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.7)    


2019-10-01 版本5.6 配置文件仍定位于用户目录。   
对于OCR，图像预处理选项：九种增强算法、伸缩比例、黑白阈值、旋转角度、是否自动矫正偏斜、是否反色。
识别选项：数据文件列表及其顺序、是否生成"区域"/"词"数据及其粒度。
批量识别选项：是否生成html或PDF、是否合并识别出文字。
内置英文和中文的"最快的"数据文件，在windows上解包即用。    
生成13种一维码和3种二维码。一维码选项：朝向、宽高、分辨率、文字位置、字体大小、空白区宽度等。
二维码选项：宽高、边沿、纠错级别、压缩模式。不同类型的选项不同。
二维码QR_Code可以在中心显示一个图片。根据纠错级别自动调整图片大小。   
解码9种一维码和3种二维码，显示条码内容和元数据（条码类型、纠错级别等）。   
调色盘：可命名颜色；增加显示cmyk值和cie值。        
生日快乐，中国！  
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.6)    


2019-9-19 版本5.5 基于tess4j支持识别图像和PDF中的文字。单图识别可选择矩形区域。PDF批量识别可设置转换图像的色彩空间和像素密度。目前只限Windows，并且用户需要下载数据文件。   
生成windows/linux/mac的自包含程序包。    
优化代码：只用maven打包而脱离对java 8的依赖；利用最新jpackage制作自包含包。    
修正问题：上一版本中微博截图工具挂了；在mac上微博截图工具首次运行后再也无法使用；linux上点击链接则程序僵死；计算CIELuv和CIELab时不应该归一化。   
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.5)    

2019-9-15 版本5.4  "数据目录"改为"运行目录"而不是以前的"用户目录"。用配置文件来保存基础参数。   
在线修改运行参数：最大可用内存、是否关闭分辨率感知、数据目录。修改这几个参数将会使MyBox自动重启。   
基于pdf2dom，以网页模式查看PDF页面。批量把PDF转换为网页。    
重构图像处理的界面：左右幕布式区域、上下风箱式菜单、多页签切换目标、子功能区更细化的显示/隐藏/调整。"按需可见"。   
粘贴板：保存多个来源的图片以供粘贴，在图片上拖拉来调整位置和大小，可选混合模式，可旋转被粘贴图片。提供示例图片。  
调色盘：可保存上千种色彩，可自动填写139种常用色彩，可导出为html页面，可在当前图片、图片历史、或参照图上点击取色。  
新的范围类型"轮廓"：把背景透明的图片的轮廓自动提取出来，作为操作的范围。提供示例图片。   
保存和管理图像处理的"范围"：增、删、改、清除，应用已保存的范围。   
统一快捷键，并提供帮助页面。  
优化代码：用公开的API替换掉内部类引用。确保单例程任务互斥进入和干净退出。写文件时先写到临时文件中以免意外导致源文件损害。        
修正问题。上一版本中以下工具失效："修改PDF属性"、"压缩PDF"、"分割PDF"。阴影处理和3种混合模式中遗漏对于透明像素的处理。      
 [此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.4)    
   
2019-8-8 版本5.3 迁移至： netbeans 11 + Java 12。    
优化批量处理界面：可添加目录、展开目录、过滤文件名、选择如何处理重名文件。    
优化图像转换：可选择更多的颜色空间并支持引用外部ICC特性文件、可选图像嵌入ICC特性文件、可选对透明通道的处理。    
优化图像元数据的解码：可读取图像中嵌入的ICC特性文件。    
优化代码：利用匿名类和嵌入fxml尽可能减少重复代码；整理类继承的关系；使项目配置文件支持多平台构建。    
初版《开发指南》。    
修正问题："图像处理-颜色-透明度"的预乘透明算法用错了；在linux上另存图像时未自动添加扩展名而导致保存失败；    
linux上无法打开链接；ICC特性文件版本解码/编码错误、数据太多时界面会僵住、未解码的数据会导致xml无法生成。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.3)    
    
2019-6-30 版本5.2 图像解码：可读Adobe YCCK/CMYK的jpg图像；读取和显示多帧图像文件中所有图像的属性和元数据。    
PDF工具：标签（目录）和缩略图；可修改PDF文件的属性，如作者、版本、用户密码、用户权限、所有者密码等。    
编辑矩阵数据：适应带格式的输入数据；自动把当前矩阵数据转变为行向量、列向量、或指定列数的矩阵；    
自动生成单位矩阵、随机方阵、或随机矩阵。    
矩阵一元计算，包括转置、行阶梯形、简化行阶梯形、行列式值-用消元法求解、行列式值-用余子式求解、逆矩阵-用消元法求解、    
逆矩阵-用伴随矩阵求解、矩阵的秩、伴随矩阵、余子式、归一化、设置小数位数、设为整型、乘以数值、除以数值、幂。    
矩阵二元计算，包括加、减、乘、克罗内克积、哈达马积、水平合并、垂直合并。    
色彩空间的工具：绘制色度图；编辑ICC色彩特性文件；RGB色彩空间基色的色适应；线性RGB与XYZ之间的转换矩阵；    
线性RGB到线性RGB的转换矩阵；颜色值的色适应；标准光源；色度适应矩阵。    
修正问题：微博截图工具经常"414 Request-URI Too Large"；按钮的提示在屏幕边沿闪烁；一些链接不可用。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.2)    
   
2019-5-1 版本5.1 界面：控件显示为图片，5种颜色可选，可选是否显示控件文字。    
简化小提示，以适应14英寸的笔记本屏幕。    
图像工具：提取/添加透明通道。    
修正若干问题，包括：图像处理中过滤透明像素的错误条件。    
劳动节快乐！    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.1)    
    
2019-4-21 版本5.0 以拖拉锚点的方式选择图像操作的区域。    
"涂鸦"：在图像上粘贴图片、添加形状（矩形/圆形/椭圆/多边形）的线条或填充形状、绘制多笔一线或一笔一线。    
画笔的宽度、颜色、实虚可选。    
查看图像：设置加载宽度；选择显示坐标和标尺；旋转可保存。    
浏览图像：缩略图格栅模式、缩略图列表模式、文件列表模式；可设置加载宽度；旋转可保存。    
图像处理：抖动处理扩展到除抠图以外的所有范围；利用预乘透明技术使不支持Alpha通道的格式也可展示透明效果；    
模糊边沿；低层实现阴影效果；拖动锚点以修改大小或边沿；多种形状的剪裁；文字可垂直。    
界面：只显示有用控件；足够但不叨扰的提示信息；快捷键/主键/缺省键；实时监视内存/CPU状态；    
查看JVM属性；刷新/重置窗口；保存和恢复界面尺寸；弹出最近访问的文件/目录；记录最近使用的工具。    
代码重构：以子类而不是分支语句来实现选择逻辑、把判断移至循环外；循环中避免浮点计算；理顺继承关系、减少重复代码；    
统一管理窗口的打开和关闭、避免线程残留。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av5.0)    
    
2019-2-20 版本4.9 图像对比度处理，可选算法。颜色量化时可选是否抖动处理。    
图像的统计数据分析，包含各颜色通道的均值/方差/斜率/中值/众数以及直方图。    
系统粘贴板内图像的记录工具。    
随时修改界面字体。    
查看图像：可选择区域来剪裁、复制、保存。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.9)    

    
2019-1-29 版本4.8 以图像模式查看PDF文件，可以设置dpi以调整清晰度，可以把页面剪切保存为图片。    
文本/字节编辑器的"定位"功能：跳转到指定的字符/字节位置、或跳转到指定的行号。    
切割文件：按文件数、按字节数、或按起止列表把文件切割为多个文件。    
合并文件：把多个文件按字节合并为一个新文件。    
程序可以跟一个文件名作为参数，以用MyBox直接打开此文件。    
在Windows上可以把图片/文本/PDF文件的打开方式缺省关联到MyBox.exe，可以在以双击文件时直接用MyBox打开。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.8)    
    
2019-1-15 版本4.7  编辑字节：常用ASCII字符的输入选择框；按字节数、或按一组字节值来换行；查找与替换，本页或整个文件，计数功能；    
行过滤，"包含任一"、"不含所有"、"包含所有"、"不含任一"，累加过滤，保存过滤结果，是否包含行号；    
选择字符集来解码，同步显示、同步滚动、同步选择；    
分页，可用于查看和编辑非常大的文件，如几十G的二进制文件，设置页尺寸，对于跨页字节组，确保查找、替换、过滤的正确性。    
批量改变文件的换行符。    
合并"文件重命名"和"目录文件重命名"。    
图像模糊改为"平均模糊"算法，它足够好且更快。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.7)    
    
2018-12-31 版本4.6  编辑文本：自动检测换行符；转换换行符；支持LF（Unix/Linux）、CR（iOS）、CRLF（Windows）。    
查找与替换，可只本页查找、或整个文件查找。    
行过滤，匹配类型："包含字串之一"、"不包含所有字串"，可累加过滤，可保存过滤结果。    
分页：可用于查看和编辑非常大的文件，如几十G的运行日志；可设置页尺寸；对于跨页字符串确保查找、替换、过滤的正确性。    
先加载显示首页，同时后端扫描文件以统计字符数和行数；统计期间部分功能不可用；统计完毕自动刷新界面。    
进度等待界面添加按钮"MyBox"和"取消"，以使用户可使用其它功能、或取消当前进程。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.6)    
    
2018-12-15 版本4.5 文字编码：自动检测或手动设置文件编码；设置目标文件编码以实现转码；支持BOM设置；    
十六进制同步显示、同步选择；显示行号。批量文字转码。    
图像分割支持按尺寸的方式。    
可将图像或图像的选中部分复制到系统粘贴板（Ctrl-c）。    
在查看图像的界面可裁剪保存。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.5)    
    
2018-12-03 版本4.4 多帧图像文件的查看、提取、创建、编辑。支持多帧Tiff文件。    
对于所有以图像文件为输入的操作，处理多帧图像文件的情形。    
对于所有以图像文件为输入的操作，处理极大图像（加载所需内存超出可用内存）的情形。自动评估、判断、给出提示信息和下一步处理的选择。    
对于极大图像，支持局部读取、边读边写的图像分割，可保存为多个图像文件、多帧Tiff、或者PDF。    
对于极大图像，支持降采样。可设置采样区域和采样比率。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.4)    
    
2018-11-22 版本4.3 支持动画Gif。查看动画Gif：设置间隔、暂停/继续、显示指定帧并导览上下帧。    
提取动画Gif：可选择起止帧、文件类型。    
创建/编辑动画Gif：增删图片、调整顺序、设置间隔、是否循环、选择保持图片尺寸、或统一设置图片尺寸、另存，所见即所得。    
更简洁更强力的图像处理"范围"：全部、矩形、圆形、抠图、颜色匹配、矩形中颜色匹配、圆形中颜色匹配；    
颜色匹配可针对：红/蓝/绿通道、饱和度、明暗、色相；可方便地增减抠图的点集和颜色列表；均可反选。    
归并图像处理的"颜色"、"滤镜"、"效果"、"换色"，以减少界面选择和用户输入。    
多图查看界面：可调整每屏文件数；更均匀地显示图片。    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av4.3)    
    
2018-11-13 版本4.2 图像处理的"范围"：全部、抠图、矩形、圆形、色彩匹配、色相匹配、矩形/圆形累加色彩/色相匹配。    
"抠图"如PhotoShop的魔术棒或者windows画图的油漆桶。    
"范围"可应用于：色彩增减、滤镜、效果、卷积、换色。可简单通过左右键点击来确定范围。    
卷积管理器：可自动填写高斯分布值；添加处理边缘像素的选项。    
目录重命名：可设置关键字列表来过滤要处理的文件。    
调整和优化图像处理的代码。    
更多的快捷键。    
    
2018-11-08 版本4.1 图像的"覆盖"处理。可在图像上覆盖：矩形马赛克、圆形马赛克、矩形磨砂玻璃、圆形磨砂玻璃、或者图片。    
可设置马赛克或磨砂玻璃的范围和粒度；可选内部图片或用户自己的图片；可设置图片的大小和透明度。    
图像的"卷积"处理。可选择卷积核来加工图像。可批量处理。    
卷积核管理器。自定义（增/删/改/复制）图像处理的卷积核，可自动归一化，可测试。提供示例数据。    
图像滤镜：新增黄/青/紫通道。    
    
2018-11-04 版本4.0  图像色彩调整：新增黄/青/紫通道。尤其黄色通道方便生成"暖色"调图片。    
图像滤镜：新增"褐色"。可以生成怀旧风格的图片。    
图像效果：新增"浮雕"，可以设置方向、半径、是否转换为灰色。    
图像的混合：可设置图像交叉位置、可选择多种常用混合模式。    
在线帮助：新增一些关键信息。    
    
2018-10-26 版本3.9  内嵌Derby数据库以保存程序数据；确保数据正确从配置文件迁移到数据库。    
图像处理：保存修改历史，以便退回到前面的修改；用户可以设置历史个数。    
用户手册的英文版。    
    
2018-10-15 版本3.8 优化代码：拆分图像处理的大类为各功能的子类。    
优化界面控件，使工具更易使用。设置快捷键。    
图像处理添加三个滤镜：红/蓝/绿的单通道反色。水印文字可以设置为"轮廓"。    
    
2018-10-09 版本3.7 微博截图工具：利用Javascript事件来依次加载图片，确保最小间隔以免被服务器判定为不善访问，    
同时监视最大加载间隔以免因图片挂了或者加载太快未触发事件而造成迭代中断。    
图像处理"效果"：模糊、锐化、边沿检测、海报（减色）、阈值化。    
    
2018-10-04 版本3.6 微博截图工具：继续调优程序逻辑以确保界面图片全部加载；整理代码以避免内存泄露。    
降低界面皮肤背景的明亮度和饱和度。    
在文档中添加关于界面分辨率的介绍。    
    
2018-10-01 版本3.5 微博截图工具：调优程序逻辑，以确保界面图片全部加载。    
提供多种界面皮肤。    
    
2018-09-30 版本3.4 修正问题：1）微博截图工具，调整页面加载完成的判断条件，以保证页面信息被完整保存。    
2）关闭/切换窗口时若任务正在执行，用户选择"取消"时应留在当前窗口。    
新增功能：1）可以设置PDF处理的最大主内存和临时文件的目录；2）可以清除个人设置。    
    
2018-09-30 版本3.3 最终解决微博网站认证的问题。已在Windows、CentOS、Mac上验证。    
    
2018-09-29 版本3.2 微博截图功能：1）在Linux和Windows上自动导入微博证书而用户无需登录可直接使用工具。    
但在Mac上没有找到导入证书的途径，因此苹果用户只好登录以后才能使用。    
2）可以展开页面上所有评论和所有图片然后截图。    
3）可以将页面中所有图片的原图保存下来。（感觉好酷）    
    
2018-09-26 版本3.1 所有图像操作都可以批量处理了。修正颜色处理算法。    
设置缺省字体大小以适应屏幕分辨率的变化。用户手册拆分成各个工具的分册了。    
提示用户：在使用微博截图功能之前需要在MyBox浏览器里成功登录一次以安装微博证书、    
（正在寻求突破这一限制的办法。Mybox没有兴趣接触用户个人信息）。    
    
2018-09-18 版本3.0 微博截图工具：可以只截取有效内容（速度提高一倍并且文件大小减小一半）、    
可以展开评论（好得意这个功能！）、可以设置合并PDF的最大尺寸。    
修正html编辑器的错误并增强功能。    
    
2018-09-17 版本2.14 微博截图工具：设置失败时重试次数、以应对网络状况很糟的情况；    
当某个月的微博页数很多时，不合并当月的PDF文件，以避免无法生成非常大的PDF文件的情况（有位博主一个月发了36页微博~）。。    
    
2018-09-15 版本2.13 分开参照图和范围图。确保程序退出时不残留线程。批量PDF压缩图片。    
微博截图工具：自动保存任意微博账户的所有微博内容，可以设置起止月份，可以截图为PDF、也可以保存html文件    
（由于微博是动态加载内容，本地网页无法正常打开，仅供获取其中的文本内容）。    
如果微博修改网页访问方式，此工具将可能失效。    
    
2018-09-11 版本2.12 合并多个图片为PDF文件、压缩PDF文件的图片、合并PDF、分割PDF。     
支持PDF写中文，程序自动定位系统中的字体文件，用户也可以输入ttf字体文件路径。     
提示信息的显示更平滑友好。网页浏览器：字体缩放，设置截图延迟、截图可保存为PDF。    
    
2018-09-06 版本2.11 图片的合并，支持排列选项、背景颜色、间隔、边沿、和尺寸选项。    
网页浏览器，同步网页编辑器，把网页完整内容保存为一张图片。图片处理：阴影、圆角、加边。    
确保大图片处理的正确性和性能。    
    
2018-08-11 版本2.10 图像的分割，支持均等分割个和定制分割。使图像处理的"范围"更易用。    
同屏查看多图不限制文件个数了。    
    
2018-08-07 版本2.9 图像的裁剪。图像处理的"范围"：依据区域（矩形或圆形）和颜色匹配，可用于局部处理图像。    
    
2018-07-31 版本2.8 图像的切边、水印、撤销、重做。Html编辑器、文本编辑器。    
    
2018-07-30 版本2.7 图像的变形：旋转、斜拉、镜像。    
    
2018-07-26 版本2.6 增强图像的换色：可以选择多个原色，可以按色彩距离或者色相距离来匹配。支持透明度处理。    
    
2018-07-25 版本2.5 调色盘。图像的换色：可以精确匹配颜色、或者设置色距，此功能可以替换图像背景色、或者清除色彩噪声。    
    
2018-07-24 版本2.4 完善图像处理和多图查看：平滑切换、对照图、像素调整。    
    
2018-07-18 版本2.3 闹钟，包括时间选项和音乐选项，支持wav铃音和MP3铃音，可以在后端运行。感谢我家乖乖贡献了"喵"。    
    
2018-07-11 版本2.2 修正线程处理逻辑的漏洞。整理文件，将文件按修改时间或者生成时间重新归类在新目录下。    
此功能可用于处理照片、游戏截图、和系统日志等需要按时间归档的批量文件。    
    
2018-07-09 版本2.1 完善图片处理的界面，支持导览。    
目录同步，包含复制子目录、新文件、特定时间以后已修改文件、原文件属性，以及删除源目录不存在文件和目录，等选项。    
    
2018-07-06 版本2.0 批量提取PDF文字、批量转换图片。    
目录文件重命名，包含文件名和排序的选项，被重命名的文件可以全部恢复或者指定恢复原来的名字。    
    
2018-07-03 版本1.9 修正问题。提取PDF文字时可以定制页分割行。    
完善图像处理：参数化调整饱和度、明暗、色相；滤镜：灰色、反色、黑白色。    
    
2018-07-01 版本1.8 将PDF文件中的文字提取出来。处理图片：调整饱和度、明暗，或者转换为灰色、反色。    
    
2018-06-30 版本1.7 完善像素计算器。支持同屏查看最多十张图，可以分别或者同步旋转和缩放。    
    
2018-06-27 版本1.6 将图片转换为其它格式，支持色彩、长宽、压缩、质量等选项。    
提供像素计算器。新增图像格式：gif, wbmp, pnm, pcx。    
    
2018-06-24 版本1.5 提取PDF中的图片保存为原格式。    
支持批量转换和批量提取。感谢 "https://shuge.org/" 的帮助：书格提出提取PDF中图片的需求。    
    
2018-06-21 版本1.4 读写图像的元数据,目前支持图像格式：png, jpg, bmp, tif。    
感谢 "https://shuge.org/" 的帮助：书格提出图像元数据读写的需求。    
    
2018-06-15 版本1.3 修正OTSU算法的灰度计算；优化代码：提取共享部件；支持PDF密码；使界面操作更友好。    
    
2018-06-14 版本1.2 针对黑白色添加色彩转换的选项；自动保存用户的选择；优化帮助文件的读取。    
感谢 "https://shuge.org/" 的帮助：书格提出二值化转换阈值的需求。    
    
2018-06-13 版本1.1 添加：转换格式tiff和raw，压缩和质量选项，以及帮助信息。    
感谢 "https://shuge.org/" 的帮助：书格提出tiff转换的需求。    
    
2018-06-12 版本1.0 实现功能：将PDF文件的每页转换为一张图片，包含图像密度、类型、格式等选项，并且可以暂停/继续转换过程。    

[未定义版本的已关闭的需求/问题列表](https://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+no%3Amilestone)    






