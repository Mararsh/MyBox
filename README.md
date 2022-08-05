# [ReadMe in English](https://github.com/Mararsh/MyBox/tree/master/en)  ![ReadMe](https://mararsh.github.io/MyBox/iconGo.png)

# MyBox：简易工具集
这是利用JavaFx开发的图形化桌面应用，目标是提供简单易用的功能。免费开源。          

## 新内容
2022-8-4 版本6.5.8          

* 数据：行过滤/行表达式可用统计值名作为占位符；不再有列过滤。                                      
* 解决：地图失效；“粘贴MyBox粘贴板中的数据”弹出错误；“播放图列表”在范围内循环时帧序号出错。        
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.8)             
   

## 下载与运行
每个版本编译好的包已发布在[Releases](https://github.com/Mararsh/MyBox/releases)目录下（点击此项目主页的`releases`页签）。       

### 源码
[MyBox-6.5.8-src.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-src.zip)   41M（大约值）       

关于源码的结构、编辑、和构建，请参考[开发指南](https://sourceforge.net/projects/mara-mybox/files/documents/MyBox-DevGuide-2.1-zh.pdf) 和
[打包步骤](https://mararsh.github.io/MyBox/pack_steps.html)       


### 自包含程序包
自包含的程序包无需java环境、无需安装、解包可用。     

| 平台 | 链接 | 大小（大约值） | 启动文件 |
| --- | --- | --- |  --- |
| win10 x64 | [MyBox-6.5.8-win10-x64.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-win10-x64.zip)  | 210MB | MyBox.exe |
| CentOS 7 x64 | [MyBox-6.5.8-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-CentOS7-x64.tar.gz)  | 242MB  | bin/MyBox  |
| mac x64| [MyBox-6.5.8-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-mac.dmg)  | 220MB  |  MyBox-6.5.8.app   |

双击或者用命令行执行包内的启动文件即可运行程序。可以把图片/文本/PDF文件的打开方式关联到MyBox，这样双击文件名就直接是用MyBox打开了。        
  

### Jar包
在已安装JRE或者JDK [Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html)或[open jdk](http://jdk.java.net/)均可）的环境下，可以下载jar包。       

| 平台 | 链接 | 大小（大约值） | 运行需要 |
| --- | --- | --- |  --- |
| win | [MyBox-6.5.8-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-win-jar.zip)  | 144MB | Java 18或更高版本 |
| linux | [MyBox-6.5.8-linux-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-linux-jar.zip)  | 175MB  | Java 18或更高版本 |
| mac | [MyBox-6.5.8-mac-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.8/MyBox-6.5.8-mac-jar.zip)  |  147MB  | Java 18或更高版本 |

执行以下命令来启动程序：       
<PRE><CODE>     java   -jar   MyBox-6.5.8.jar</CODE></PRE>       
程序可以跟一个文件名作为参数、以用MyBox直接打开此文件。例如以下命令是打开此图片：       
<PRE><CODE>     java   -jar   MyBox-6.5.8.jar   /tmp/a1.jpg</CODE></PRE>       

### 其它下载地址       
从云盘下载：  [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)       
从sourceforge下载：[https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)      

### 限制
  
* 在某个输入法运行时，MyBox的窗口经常僵住。解决办法：禁用/卸载此输入法。       

## 版本迁移
1. 每个版本有自己的配置文件，新版本可以复制已安装版本的参数。       
2. 每个版本处理的所有数据都在它指向的“数据目录”下。多个版本可以指向同一数据目录。
3. MyBox向后兼容：新版本可以处理旧版本的数据目录。而不保证向前兼容：旧版本处理新版本的数据目录时可能出错。

## 配置<a id="Config" />
配置文件在"用户目录"下:       

| 平台 | MyBox配置文件的目录 |
| --- | --- |
| win | `C:\用户\用户名\mybox\MyBox_v6.5.8.ini`  |
| linux | `/home/用户名/mybox/MyBox_v6.5.8.ini` |
| mac | `/Users/用户名/mybox/MyBox_v6.5.8.ini` |       

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
| 开发日志 | 6.5.8 | 2022-8-4 | [html](#devLog) |
| 快捷键 | 6.5.6 |  2022-6-11 | [html](https://mararsh.github.io/MyBox/mybox_shortcuts.html) |
| 打包步骤 | 6.3.3 |  2020-9-27 | [html](https://mararsh.github.io/MyBox/pack_steps.html) |
| 开发指南 | 2.1 |  2020-08-27 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-DevGuide-2.1-zh.pdf) |
| 用户手册-综述 |  5.0 |  2019-4-19 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-Overview-zh.pdf) |
| 用户手册-图像工具 | 5.0 |  2019-4-18 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-ImageTools-zh.pdf) |
| 用户手册-PDF工具 | 5.0 |  2019-4-20 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-PdfTools-zh.pdf) |
| 用户手册-桌面工具 | 5.0 |  2019-4-16 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-DesktopTools-zh.pdf) |
| 用户手册-网络工具 | 5.0 |  2019-4-16 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-UserGuide-5.0-NetworkTools-zh.pdf) |       


# 实现基础       
MyBox基于以下开放资源：       

| 名字 | 角色 | 链接 |
| --- | --- | --- |
| JDK | Java语言 | http://jdk.java.net/   |
|   |   | https://www.oracle.com/technetwork/java/javase/downloads/index.html  |
|   |   | https://docs.oracle.com/en/java/javase/18/docs/api/index.html  |
| JavaFx | 图形化界面 | https://gluonhq.com/products/javafx/  |
|   |   |  https://docs.oracle.com/javafx/2/  |
|   |   |  https://gluonhq.com/products/scene-builder/ |
|   |   |  https://openjfx.io/javadoc/18/ |
| NetBeans | 集成开发环境 | https://netbeans.org/ |
| jpackage | 自包含包 | https://docs.oracle.com/en/java/javase/18/docs/specs/man/jpackage.html |
| maven | 代码构建 | https://maven.apache.org/ |
| jai-imageio | 图像处理 | https://github.com/jai-imageio/jai-imageio-core |
| PDFBox | PDF处理 | https://pdfbox.apache.org/ |
| PDF2DOM | PDF转html | http://cssbox.sourceforge.net/pdf2dom/ |
| javazoom | MP3解码 | https://sourceforge.net/projects/javalayer/ |
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
| poi | 微软文档 | https://poi.apache.org |
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
| 中国出土壁画全集 | 素材 | https://book.douban.com/subject/10465940/ |
| sfds | 书法 | http://www.sfds.cn/725B/ |
| PaginatedPdfTable | PDF | https://github.com/eduardohl/Paginated-PDFBox-Table-Sample |
| jsoup | DOM | https://jsoup.org/ |       
| 微博 | 素材 | https://weibo.com/2328516855/LhFIHy26O |
| 知乎 | 素材 | https://www.zhihu.com/question/41580677/answer/1300242801 |
| commons-math | 计算 | https://commons.apache.org/proper/commons-math/index.html |
| JEXL | 计算 | https://commons.apache.org/proper/commons-jexl |


# 当前版本       
当前是版本6.5.8，已实现的特点概述如下:      
  
* [跨平台](#cross-platform)
* [国际化](#international)
* [个人的](#personal)
* [数据兼容](#dataCompatible)
* [文档工具](#documentTools)
    - [树形信息](#infoInTree)
    - [笔记](#notes)
    - [PDF工具](#pdfTools)
    - [文本编辑基础](#editTextBase)
    - [Markdown工具](#markdownTools)
    - [文本工具](#textTools)
    - [网页工具](#htmlTools)
    - [微软的文档格式](#msDocuments)
    - [编辑字节](#editBytes)
    - [MyBox粘贴板中的文本](#myboxTextClipboard)
    - [系统粘贴板中的文本](#systemTextClipboard)
 * [图像工具](#imageTools)
    - [查看图像](#viewImage)
    - [浏览图像](#browserImage)
    - [分析图像](#ImageData)
    - [播放图像列表](#playImages)
    - [图像处理](#imageManufacture)
    - [编辑图像列表](#imagesList)
    - [多图合一](#multipleImages)
    - [图像局部化](#imagePart)
    - [图片转换](#imageConvert)
    - [识别图像中的文字](#imageOCR)
    - [颜色管理](#ColorManagement)
    - [色彩空间](#colorSpaces)
    - [MyBox粘贴板中的图像](#myboxImageClipboard)
    - [系统粘贴板中的图像](#systemImageClipboard)
    - [其它](#imageOthers)
    - [大图片的处理](#bigImage)
* [数据工具](#dataTools)
    - [编辑数据](#editData)
    - [行过滤](#rowFilter)
    - [加工数据](#manufactureData)
    - [整理数据](#trimData)
    - [计算数据](#calculateData)
    - [数据图](#dataCharts)
    - [管理数据](#manageData)
    - [拼接数据](#spliceData)
    - [数据文件](#dataFiles)
    - [系统粘贴板中数据](#dataInSystemClipboard)
    - [MyBox粘贴板中的数据](#dataInMyBoxClipboard)
    - [矩阵](#matrix)
    - [数据库表](#dataTables)
    - [数据库SQL](#dbSQL)
    - [JShell（Java交互编程工具）](#JShell)
    - [JEXL（Java表达式语言）](#JEXL)
    - [Javascript](#Javascript)
    - [通用的数据处理](#dataManufacture)
    - [地图数据](#mapData)
    - [地理编码](#geographyCode)
    - [地图上的位置](#locationInMap)
    - [位置数据](#locationData)
    - [位置工具](#locationTools)
    - [疫情报告](#epidemicReport)
    - [其它](#dataOthers)
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
    - [网页浏览器](#webBrowser)
    - [查询网址](#queryAddress)
    - [批量查询DNS](#queryDNS)
    - [解码/编码URL](#encodeDecodeURL)
    - [管理安全证书](#securityCerificates)
    - [下载网页](#downloadFirstLevelLinks)
    - [微博截图工具](#weiboSnap)
* [开发工具](#devTools)
* [设置](#settings)
* [窗口](#windows)
* [帮助](#helps)

## 跨平台<a id="cross-platform" />       
MyBox用纯Java实现且只基于开放资源，MyBox可运行于支持Java 18的平台。       
MyBox v5.3以前的版本均基于Java 8。       

## 国际化<a id="international" />       
1. 所有代码均国际化。可实时切换语言。
2. 一种语言对应两个资源文件："Messages_语言名.properties"、"TableMessages_语言名.properties"。
3. 支持在线添加语言。提供表格，对照英语翻译。新语言可实时生效。       
例如，新语言名字为“aa”，则它的资源文件是：Messages_aa.properties和TableMessages_aa.properties。       
4. 新语言可共享给别人：把资源文件复制到数据目录的子目录"mybox_languages"下，则MyBox可即时感知到新语言。       
5. 内置中文和英文， 在目录`MyBox/src/main/resources/bundles/`中：       

| 语言 | 界面的资源文件 | 数据表的资源文件 |       
| --- | --- |  --- |
| 中文 | Messages_zh_CN.properties | TableMessages_zh_CN.properties |
| 英文 | Messages_en.properties | TableMessages_en.properties |       

![截屏-封面](https://mararsh.github.io/MyBox/snap-cover.jpg)       

## 个人的<a id="personal" />
1. 无注册/登录/数据中心/云存储。
2. 如无必要，不访问网络。       
3. 如无必要，不读不写。       

## 数据兼容<a id="dataCompatible" />
1. 导出的数据是通用的文本格式，如txt/csv/xml/json/html。
2. 导入的数据是通用的文本格式，如txt/csv。       
3. 至少有一种导出格式可以被导入。
4. 导入的数据是自包含的，即重建原数据无需辅助数据。       


## 文档工具<a id="documentTools" />        

### 树形信息<a id="infoInTree" />        
1. 信息被组织为一棵树。
2. 对任意节点可执行：增删改子节点、重命名、移动、复制、导出、显示树图、展开、折叠。
3. 编辑节点：
	-  节点名不应包含字符串“ > ”（“>”前后是空格）。
	-  一个节点可以设置多个标签。
	-  可选显示节点的序列号。
4. 导出：       
	-  格式：文本（用于导入）、单个网页、网页框架、xml。       
	-  可选择是否导出时间、标签。       
	-  可选择文件字符集。       
	-  可设置网页风格。       
5. 导入。提供示例。  
6. 查询：
	-  节点的子节点或所有后代。
	-  按标签。可选多个标签。
	-  按更改时间。 可选多个时间。
	-  按标题或内容包含的字符串。 可输入多个关键字。       
 
![截屏-infoInTree](https://mararsh.github.io/MyBox/snap-infoInTree.jpg)


### 笔记<a id="notes" />       
1. 笔记是html格式的信息片段：
	-  笔记可以用四种模式来编辑，即“网页代码”、“富文本”、“Markdown”、“文本”：
		-  每个编辑模式都可以单独修改网页。
		-  当前页签下的编辑模式为“当前编辑模式”。
		-  点击按钮“同步”(F10)，把当前编辑模式中的修改应用到其它模式。
		-  点击按钮“保存”(F2)，把当前编辑模式的修改保存下来，并将修改自动同步到其它模式。
		-  笔记的html代码应是"body"的内容，不要包含标签“html”、"head"、和"body"。
	-  可对笔记设置风格，仅用于显示，不会保存在笔记的代码中。
2. 笔记本是由笔记构成的树状信息。

![截屏-notes](https://mararsh.github.io/MyBox/snap-notes.jpg)

### PDF工具<a id="pdfTools" />
1. 查看PDF文件：       
	-  标签和缩略图
	-  每页被转换为一张图片。可设置dpi以调整清晰度
	-  提取页面上的文字
	-  转换页面为一个网页
	-  识别页面中的文字（OCR）
2. 批量转换：
	-  将PDF文件的每页转换并保存为一张图片文件，包含图像密度、色彩、格式、压缩、质量、色彩转换等选项。
	-  转换PDF文件中被选择页的图片，并保存为新的PDF。
	-  压缩PDF文件中的图片，保存为新的PDF。可设置JPEG质量或者黑白色阈值
	-  将PDF转换为网页，可选：每页保存为一个html、还是整个PDF保存为一个html；字体文件/图像文件是嵌入、单独保存、还是忽略。
3. 批量提取：
	-  将PDF中的图片提取出来。
	-  将PDF文件中的文字提取出来，可以定制页的分割行。
	-  识别PDF文件中图片的文字（OCR）。
4. 分割PDF文件为多个PDF文件，可按页数或者文件数来均分，也可以设置起止列表。
5. 合并多个PDF文件。
6. 将多个图片合成PDF文件
7. 写PDF的选项：页面尺寸、图片属性、字体文件、页边、页眉、作者等。
8. 修改PDF的属性，如：标题、作者、版本、修改时间、用户密码、所有者密码、用户权限等       

![截屏-pdf](https://mararsh.github.io/MyBox/snap-pdf.jpg)       

### 文本编辑的基础<a id="editTextBase" />
1. 编辑功能（复制/粘贴/剪切/删除/全选/撤销/重做/恢复）及其快捷键。
2. 查找与替换：
	-  选项：忽略大小写、回绕。
	-  查找字串和替换字串都支持支持多行。其中的换行符按照当前文件换行符的定义来处理。
	-  支持正则表达式，提供示例。
	-  计数
	-  由于算法的限制，对于多页文档查找规则表达式时，假设：匹配的字符串的最大长度小于当前JVM可用内存的1/16。
3. 定位：
	-  选择指定行号的行。
	-  选择指定位置的字符。
	-  选择指定范围的行。
	-  选择指定范围的字符。
4. 行过滤：
	-  条件：包含/不包含任一、包含/不含所有、包含/不包含规则表达式、匹配/不匹配规则表达式。
	-  可累加过滤。
  	-  可保存过滤结果。
  	-  可选是否包含行号。
5. 分页。可用于查看和编辑非常大的文件，如几十G的运行日志。
	-  设置页尺寸。
	-  页面导航。
	-  先加载显示首页，同时后端扫描文件以统计字符数和行数；统计期间部分功能不可用；统计完毕自动刷新界面。
	-  对于跨页字符串，确保查找、替换、过滤的正确性。
6. 定时自动保存。只在有修改时保存。
7. 保存时自动备份。可恢复到备份。       


### Markdown工具<a id="markdownTools" />
1. Markdown编辑器：
	-  提供输入格式的按钮。       
	-  同步显示转换的网页和网页代码 
2. 批量转换Markdown为网页。
3. 批量转换Markdown为文本。
4. 批量转换Markdown为PDF。     

![截屏-markdownEditor](https://mararsh.github.io/MyBox/snap-markdownEditor.jpg)       


### 文本工具<a id="textTools" />
1. 文本编辑器：
	-  自动检测或手动设置文件编码；改变字符集实现转码；支持BOM设置。
	-  自动检测换行符；改变换行符。显示行号。         
           支持LF（Unix/Linux）、 CR（Apple）、 CRLF（Windows）。       
	-  字符集对应的编码：字节的十六进制，同步显示、同步选择，可选同步滚动、同步更新。              
2. 批量转换/分割文本文件。
3. 合并文本文件。
4. 批量转换文本文件为网页/PDF。
5. 批量替换文本文件中的字符串。

### 网页工具<a id="htmlTools" />       
1. 网页编辑器
	-  加载网页的方式：打开文件、创建文件、或输入网址。
	-  网页可以用四种模式来编辑，即“网页代码”、“富文本”、“Markdown”、“文本”：
		-  每个编辑模式都可以单独修改网页。
		-  当前页签下的编辑模式为“当前编辑模式”。
		-  点击按钮“同步”(F10)，把当前编辑模式中的修改应用到其它模式。
		-  点击按钮“保存”(F2)，把当前编辑模式的修改保存下来，并将修改自动同步到其它模式。
		-  点击按钮“弹出”(CTRL/ALT+p)以把当前编辑模式的内容显示在一个新窗口中。
		-  点击按钮“菜单”(F12)或右键点击编辑区域，以弹出按钮菜单。
		-  点击按钮“MyBox粘贴板”(CTRL/ALT+m)以弹出文本粘贴板来方便粘贴。
	-  对于frameset可以选择frame来编辑。 
2. 查找网页中的字串。  
3. 查找网页中的元素。   
4. 网页截图：可设置dpi，看选保存为一张图还是多图保存在PDF中。
5. 批量提取网页中的表格数据：
6. 批量转换网页为Markdown/文本/PDF文件：
7. 批量设置网页的编码/样式。
8. 合并多个网页为一个网页/Markdown/文本/PDF文件。
9. 对多个文件生成框架文件。   

![截屏-htmlEditor](https://mararsh.github.io/MyBox/snap-htmlEditor.jpg)       

### 微软的文档格式<a id="msDocuments" />
1. 处理Word文档
	-  格式：            
		-  文件".doc"(Word 97，即OLE 2格式)被转换为html，保留大部分格式。
		-  文件".docx"(Word 2007, 即OOXML格式)被转换为文本，格式丢失。
	-  查看Word文件。            
	-  批量转换Word文件为html/PDF。           
2. 处理PPT文档
	-  格式：            
		-  文件".ppt"(PPT 97，即OLE 2格式)。
		-  文件".pptx"(PPT 2007, 即OOXML格式）。                    
	   不说明则同时支持这两种格式。                      
	-  查看PPT文件-图像模式。逐页显示：
		-  页面被转换为图片
		-  幻灯片文本和备注文本
	-  批量转换PPT文件为图片/PDF。
	-  批量提取PPT文件中的对象：
		-  可选择提取：幻灯片文本、备注文本、模板文本、注释文本、图片、声音、OLE(Word/Excel)
		-  不支持从".pptx"中提取声音。
	-  批量分割PPT文件。
	-  合并".pptx"文件。
	-  将多个图片合成为".ppt"文件。
	-  播放PPT文件。
3. 从Excel/Word/PowerPoint/Publisher/Visio中提取文本。

![截屏-viewPPT](https://mararsh.github.io/MyBox/snap-viewPPT.jpg)       


### 编辑字节<a id="editBytes" />
1. 字节被表示为两个十六进制字符。所有空格、换行、非法值将被忽略。
2. 常用ASCII字符的输入选择框。
3. 换行。仅用于显示、无实际影响。显示行号。可按字节数换行、或按一组字节值来换行。
4. 选择字符集来解码：同步显示、同步滚动、同步选择。非字符显示为问号。
5. 分页。若按字节数换行，则行过滤时不考虑跨页。       

### MyBox粘贴板中的文本<a id="myboxTextClipboard" />
1. 增删查MyBox粘贴板中的文本。
2. 手动添加系统粘贴板中的文本。
3. 把选择的文本复制到系统粘贴板。
4. 在所有文本输入控件的右键菜单中，可以弹出MyBox粘贴板以供选用粘贴。

### 系统粘贴板中的文本<a id="systemTextClipboard" />
加载/刷新/删除系统粘贴板中的文本：         
 
1. 点击按钮后，开始监视系统粘贴板的新文本，将它们保存到MyBox粘贴板中。
2. 可以设置监视间隔。
3. 在监视界面上可以累加监视到文本。分隔符可选。
4. 在以下条件之一发生时，此监视停止：
	-  用户点击按钮“停止”
	-  MyBox退出
	-  未选择“复制到MyBox粘贴板”，并且此界面被关闭。
5. 可选在MyBox启动时开始监视。

## 图像工具<a id="imageTools" />

### 查看图像<a id="viewImage" />
1. 设置加载宽度：原始尺寸或指定宽度。
2. 选择区域。
3. 旋转可保存。
4. 删除、重命名、恢复。
5. 可选显示：坐标、标尺、网格、数据。
6. 查看图像的元数据和属性，可解码图像中嵌入的ICC特性文件。
7. 同目录下图像文件导览，多种文件排序方式。
8. 右键菜单。
9. 可选是处理选择的区域还是处理图片整体。       
10. 保存或修改图片时可选渲染参数。       

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

### 播放图像列表<a id="playImages" />
1. 可以播放如下文件：
	-  动态gif文件
	-  多帧tif文件
	-  PDF文件
	-  PPT文件      
    PPT/PDF文件的每页被转换为一帧图像来显示。
2. 本版本中，所有图片会被加载到内存中。为了避免内存不够，可以：
	-  设置要显示的帧范围
	-  设置图片加载的宽度
	-  对PDF图片设置dpi  
3. 图像被逐帧显示：
	-  设置时间间隔和速度加倍
	-  暂停/继续
	-  选择帧
 	-  前/后帧
 	-  可选循环、反序

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
	-  粘贴选项：剪辑是否在上、是否保持宽高比、混合模式、不透明度、旋转角度。
4. 伸缩：拖动锚点调整大小、按比例收缩、或设置像素。四种保持宽高比的选项。设置渲染参数。
5. 色彩：针对红/蓝/绿/黄/青/紫通道、饱和度、明暗、色相、不透明度，进行增加、减少、设值、过滤、取反色的操作。可选是否预乘透明。
6. 效果：海报（减色）、阈值化、灰色、黑白色、褐色、浮雕、边沿检测、马赛克、磨砂玻璃。可选算法和参数。
7. 增强：对比度、平滑、锐化、卷积。可选算法和参数。
8. 富文本：以网页形式编辑文本，在图片上拖放调整文本的大小和位置。可设置背景的颜色、不透明度、边沿宽度、圆角大小，可设置文字的旋转角度。       
    由于是利用截屏实现，结果比较模糊，还没有好的解决办法。（此版本暂时不支持）       
9. 文字：设置字体、风格、大小、色彩、混合模式、阴影、角度，可选是否轮廓、是否垂直，点击图片定位文字。
10. 画笔：
	-  折线：多笔一线。可选画笔的宽度、颜色、是否虚线、混合模式。
	-  线条：一笔一线。可选画笔的宽度、颜色、是否虚线、混合模式。
	-  橡皮檫：一笔一线。总是透明色，可选画笔的宽度。
	-  磨砂玻璃：一点一画。可选画笔的宽度、模糊强度、形状（圆形还是方形）。
	-  马赛克：一点一画。可选画笔的宽度、模糊强度、形状（圆形还是方形）。
	-  形状：矩形、圆形、椭圆、多边形。可选画笔的宽度、颜色、是否虚线、混合模式、是否填充、填充色。
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
	- 上下风箱式页签
	- 叠加多页签切换
	- 按功能显示/隐藏控件
19. 演示：对于粘贴的混合模式、"颜色"、"效果"、"增强"，一键展示各种数据处理的示例。
20. 批量图像处理。       

![截屏-imageManufacture](https://mararsh.github.io/MyBox/snap-imageManufacture.jpg)       


### 编辑图像列表<a id="imagesList" />
1. 可以添加：
	-  动态gif文件，每一帧都被加到列表中
	-  多帧tif文件，每一帧都被加到列表中
	-  PDF文件，每一页被转换为图片并被加到列表中
	-  PPT文件，每一页被转换为图片并被加到列表中
	-  系统粘贴板中的图像
	-  所有支持的图像格式文件      
2. 移动图像以设置顺序。
3. 设置图像的时长，用于播放列表和保存动态gif文件。
4. 播放列表：用CTRL/SHIFT选择一些图像来播放，或不选任何图像以播放整个列表。
5. 保存列表：
	-  用CTRL/SHIFT选择一些图像来保存，或不选任何图像以保存整个列表。
	-  将每一项保存为一个支持的图像格式文件。
	-  拼接图像
	-  合并为一个多帧tif文件
	-  合并为一个动态gif文件
 	-  合并为一个PDF文件
 	-  合并为一个PPT文件
 	-  合并为一个视频文件（需要ffmpeg）

![截屏-editImages](https://mararsh.github.io/MyBox/snap-editImages.jpg)         


### 多图合一<a id="multipleImages" />
1. 拼接图片。支持排列选项、背景颜色、间隔、边沿、和尺寸选项。
2. 添加透明通道

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
	-  旋转角度
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
1. 管理调色盘：
	-  增、删、改名、复制。
	-  提供调色盘的示例：常用网页色彩、传统中国色彩、传统日本色彩、来自colorhexa.com的颜色。
2. 管理调色盘中的颜色：增、删、复制、命名、排序、导出、导入。
3. 显示颜色：
	-  在表格中，简单/全部显示颜色属性，或合并/分列显示颜色属性。
	-  用色块显示颜色。弹出：颜色的名字（如果有）、十六进制值、rgb值、hsb值、不透明值、cmyk值、cie值。
4. 添加颜色：
	-  在取色器中挑选颜色。
	-  输入颜色列表。提供示例。有效的颜色值格式：       
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
	-  在查看/处理图片的界面上点取色按钮。       
5. 修改颜色：
	-  颜色名可空、可重复。同一颜色在不同调色盘中可有不同的名字。
	-  颜色序号是任意浮点数。同一颜色在不同调色盘中可有不同的序号。
	-  点击按钮以自动整理序号为步长为1的值。
	-  拖动色块以调整颜色的顺序。
6. 导出颜色：导出当前页、全部、或选择的颜色为html或csv文件。
7. 导入颜色，CSV格式：
	-  文件编码是UTF-8或ASCII
	-  第一行定义数据头，以英文逗号分隔。
	-  其余每行定义一条数据，数据域以英文逗号分隔。
	-  以下为必要数据域：rgba 或 rgb
	-  以下是可选数据域：name       
8. 查询颜色。

[常用网页颜色列表](http://mararsh.github.io/MyBox_data/colors/%E5%B8%B8%E7%94%A8%E7%BD%91%E9%A1%B5%E9%A2%9C%E8%89%B2.html)         

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


### MyBox粘贴板中的图像<a id="myboxImageClipboard" />
1. 增删查MyBox粘贴板中的图像。
2. 提供示例。
3. 可以添加系统粘贴板中的图像。
4. 可以把选择的图像复制到系统粘贴板。


### 系统粘贴板中的图像<a id="systemImageClipboard" />
加载/刷新/删除系统粘贴板中的图像：        

1. 点击按钮后，开始监视系统粘贴板的新图像。
2. 可以设置监视间隔。
3. 把监视到的新图像保存为文件、或者复制到MyBox粘贴板。
4. 可以设置图像保存的宽度          
5. 在以下条件之一发生时，此监视停止：
	-  用户点击按钮“停止”
	-  MyBox退出
	-  以下条件都满足：
		-  未选择“复制到MyBox粘贴板”
		-  未选择“保存为文件”，或者目标文件路径非法
		-  此界面被关闭

![截屏-systemClipboard](https://mararsh.github.io/MyBox/snap-systemClipboard.jpg)           


### 其它<a id="imageOthers" />
1. 支持图像格式：png,jpg,bmp,tif,gif,ico,wbmp,pnm,pcx。可读Adobe YCCK/CMYK的jpg图像。
2. 像素计算器
3. 卷积核管理器
4. 把图片转换为base64码           

### 大图片的处理<a id="bigImage" />
1. 评估加载整个图像所需内存,判断能否加载整个图像。
2. 若可用内存足够载入整个图像，则读取图像所有数据做下一步处理。尽可能内存操作而避免文件读写。
3. 若内存可能溢出，则采样读取图像数据做下一步处理。
4. 采样比的选择：即要保证采样图像足够清晰、又要避免采样数据占用过多内存。
5. 采样图像主要用于显示图像。已被采样的大图像，不适用于图像整体的操作和图像合并操作。
6. 一些操作，如分割图像、降采样图像，可以局部读取图像数据、边读边写，因此适用于大图像：显示的是采样图像、而处理的是原图像。

## 数据工具<a id="dataTools" />

### 编辑数据<a id="editData" />
1. 以下对象可以以一致的方式来编辑：数据文件（csv/excel/文本）、数据粘贴板、矩阵、数据库表。
2. 数据应当是等宽的，即所有行的列数相同。  
3. 数据被分页。当页数大于1时，运行一些功能之前必须保存当前页的修改。
4. 数据可以在两种模式下编辑：
	- “表格”是主编辑模式：
 		- 它的修改被自动应用其它面板。
 		- 它是用于保存的最终数据。
	- “文本”是辅编辑模式：
 		- 点击按钮“确定”以把它的修改应用于“表格”。
 		- 点击按钮“取消”以丢弃它的修改并从“表格”读取数据。
 		- 点击按钮“分隔符”以从“表格”读取数据并应用新的分隔符，它的修改被丢弃。
5. 编辑属性/列：
	- 列名不能为空也不能重复。
	- 列类型用来检验数据值:
 		- 在编辑数据时，非法值将被拒绝。
 		- 在读取数据时，类型被忽略。
 		- 在计算数值时，非法数值被计为零。
 		- 数据类型影响排序的结果。
	- 点击按钮“确定”以把修改应用于“表格”。
	- 点击按钮“取消”以丢弃修改并从“表格”读取数据。
6. 有修改时，页签头显示* 。若修改未应用，则显示 **。
7. 点击按钮“保存”以把修改写入文件和数据库：
	- “表格”中行的变化，包括修改/添加/删除/排序，影响文件中当前页的行。
	- “列”页签中的变化，包括修改/添加/删除/排序，影响文件中所有行。
	- “属性/列的修改被保存到数据库中。
8. 点击按钮“恢复”以丢弃所有修改并从文件和数据库中加载数据。
9. 同步显示数据的文本格式和网页格式。
10. 加载系统粘贴板的内容   
	- 读取并解析系统粘贴板的内容。
	- 可选数据分隔符：特殊字符、或输入的规则表达式。
	- 可选把第一行定义为列名。
11. 示例：
	- 中国的统计数据
	- 回归相关的数据

![截屏-dataEdit](https://mararsh.github.io/MyBox/snap-dataEdit.jpg)       

### 行过滤<a id="rowFilter" />               
在加工/整理/计算数据、生成数据图时，可以输入JavaScript表达式作为过滤数据的条件：     
           
1. 若脚本为空，则不过滤。       
2. 编辑脚本：             
	- 脚本可以包含任何合法的JavaScript元素。            
	- 脚本应当最终返回布尔值("true"或"false")。            
	- 脚本可以包含以下占位符：               
                 #{表行号}              
                 #{数据行号}            
                 #{<列名>}               
                 #{<列名>-<统计名>}               
	- 点击"编辑"按钮以保存脚本。            
3. 在MyBox计算表达式时:             
	- 占位符被数据行的实际值替换。            
	- '#{xxx} '被处理为字符串而#{xxx}被处理为数字。            
	- 处理所有页时，脚本若包含"#{表行号}"则会失败。            
4. 可以设置取用数据的最多行数:             
5. 示例:                             
                         
| 表达式 | 含义 |    
| --- | --- |    
| #{数据行号} % 2 == 0  |  数据行号为偶数 |    
| #{表行号}  % 2 == 1 | 当前页的奇数行 |    
| Math.abs(#{字段1}) >= 0 | 列"字段1"的值是数值 |    
| #{字段1}) > 0 | 列"字段1"的值大于零 |    
| #{字段1} - #{字段2}  < 100 | 列"字段1"与"字段2"的值差小于100  |    
| '#{字段1}'.length > 0 | 列"字段1"的值不为空值 |    
| '#{字段1}'.search(/Hello/ig) >= 0 | 列"字段1"的值包含字符串"Hello"（忽略大小写） |    
|  '#{字段1}'.startsWith('Hello')  | 列"字段1"的值以"Hello"开头 |    
| var array = [ 'A', 'B', 'C']; array.includes('#{字段1}') | 列"字段1"的值为'A'或 'B'或 'C' |    
| #{v1} < #{v1-均值}  | 列"字段1"的值小于列"字段1"的平均值 |                    


![截屏-dataRowFilter](https://mararsh.github.io/MyBox/snap-dataRowFilter.jpg)         


### 加工数据<a id="manufactureData" />       

#### 赋值   
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 对所选数据赋值为：
	- 常数：0、1、空格、或输入的值
	- 随机数、随机数（非负）
3. 当所选数据为方阵（行列数相同），可赋值为：高斯分布、单位矩阵、上三角矩阵、下三角矩阵
4. 对所选数据赋值为行表达式：             
	- 若脚本为空，则返回空字符串。             
	- 编辑脚本：             
 		- 脚本可以包含任何合法的JavaScript元素。             
 		- 脚本应当最终返回一个值。             
 		- 脚本可以包含以下占位符：             
                          #{表行号}             
                          #{数据行号}             
                          #{<列名>}                    
                          #{<列名>-<统计名>}                                           
	- 在MyBox计算表达式时:             
 		- 占位符被数据行的实际值替换。             
 		-  '#{xxx} '被处理为字符串而#{xxx}被处理为数字。             
 		- 处理所有页时，脚本若包含"#{表行号}"则会失败。             
	- 示例:             
    
| 表达式 | 含义 |    
| --- | --- |    
| #{数据行号} % 2 == 0 | 数据行号为偶数 |    
| #{表行号}  % 2 == 1 | 当前页的奇数行 |    
| Math.abs(#{字段1}) + Math.PI * Math.sqrt(#{字段2}) | 数学计算 |    
| '#{字段1}'.replace(/hello/ig, 'Hello') | 把列"字段1"的值中所有"hello"(忽略大小写)替换"Hello" |    
|  '#{字段1}'.toLowerCase() | 列"字段1"的值的小写 |    
| '#{字段1}'.split(',') | 把列"字段1"的值按逗号分隔 |           
| #{v1} - #{v1-均值}  | 列"字段1"的值与列"字段1"的平均值之间的差值 |            

5. 若处理数据文件的所有页，则在赋值之前对数据文件自动备份。          

![截屏-dataSetValues](https://mararsh.github.io/MyBox/snap-dataSetValues.jpg)           

#### 设置风格 / 标识异常值   
1. 添加/编辑/删除风格。                     
2. 定义条件以约束风格应用于哪些数据单元：
	- 数据行的范围                     
	- 列名                     
	- 行过滤                                     
	  注意：在添加或删除一些数据行以后，数据的行号可能会改变。                     
          例如，在第6行之前插入两行，则原来的第12行变成了第14行，而现在的第12行是原来的第10行。                     
          所以“行号”不是定位特定数据行的正确方式。                     
          引用特定的行的一个方法是列值构成的表达式。                     
3. 定义风格的值：
	- 字体颜色、字体大小、背景颜色、是否粗体
	- 可以输入JavaFx CSS格式的更多值。
4. 定义风格的标题和序号。
5. 选择风格是否标识异常值。
6. 所有风格按序号被逐条应用于数据。           

![截屏-dataStyles](https://mararsh.github.io/MyBox/snap-dataStyles.jpg)           

#### 复制/过滤/查询   
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
3. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。     

#### 删除  
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 设置行过滤条件。
2. 选项：是否在错误时继续处理。
3. 若处理数据文件的所有页，则在删除之前对数据文件自动备份。

#### 导出   
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 目标文件格式：csv、文本、excel、xml、json、html、pdf。可以设置目标文件的选项。
3. 可按最大行数分割导出后的文件。

![截屏-dataExport](https://mararsh.github.io/MyBox/snap-dataExport.jpg)           

#### 转换为数据库表   
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 生成自增主键、或选择主键列。
3. 可选是否导入数据。   

![截屏-dataConvertDB](https://mararsh.github.io/MyBox/snap-dataConvertDB.jpg)           

### 整理数据<a id="trimData" />       
#### 排序     
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 选择要排序的列、是否降序。
	- 列的数据类型影响排序的结果。
	- 可设置最多取结果的行数。
3. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
4. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。  


#### 转置     
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：可选“把第一列当作列名”。
3. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
4. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。  

#### 归一化     
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 可选按照：列、行、所有。
	- 算法可选：最大最小值（可设置区间）、和（L1）、ZScore（L2）。
	- 可选对非数字：略过、计为零。
3. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
4. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。  
        
![截屏-dataNormalize](https://mararsh.github.io/MyBox/snap-dataNormalize.jpg)        

### 计算数据<a id="calculateData" />        

#### 行表达式       
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 输入数值的名字             
	- 输入行表达式。
3. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
4. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。          


#### 描述性统计       
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 选择要统计的值：             
          计数、合计、均值、几何平均数、平方和、总体方差、样本方差、总体标准差、样本标准差、斜度、            
           最小值（Q0）、下四分位数（Q1）、中位数（Q2）、上四分位数（Q3）、最大值（Q4）、               
           上极端异常值线（E4）、上温和异常值线（E3）、下温和异常值线（E2）、下极端异常值线（E1）、众数         
	- 选择按照：列、行（可选类比列）、所有。        
	- 设置小数位数。
	- 可选对非数字：略过、计为零。
3. 目标：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表，或者在表内指定位置插入、附加、或替换。     
           
         
![截屏-dataStatistic](https://mararsh.github.io/MyBox/snap-dataStatistic.jpg)         

#### 简单线性回归             
此工具基于Apache Commons Math。                  
回归过程不存储数据，所以在处理很多数据时计算本身没有内存限制。           

1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 选择一列作为自变量。
	- 选择另一列作为因变量。
 	- 选择是否包含截距
	- 设置小数位数
3. 模型：
	- 在表格中显示回归每一步的数据值状态，包括：观察数、斜率、截距、判定系数（R方）、R值、            
           均方差（MSE）、方差和（SSE）、总体平方和（SSTO）、回归平方和（SSR）等
	- 显示拟合的线性模型。
	- 显示回归最后一步的数据值状态。
	- 输入的自变量，可以生成预测值。
4. 拟合图：
	- 当处理所有数据行（所有页）时，选择是否在图上显示所有值。           
           若选择“在图上显示所有值”，则当图中加载很多数据时可能发生内存不够。            
           否则，图上只显示当前页的数据，同时所有页都参与回归，所以没有内存限制。         
	- 可设置布局、横轴、竖轴的参数。
	- 可选择图上是否显示：拟合点、拟合线、模型描述。
	- 可选择数据的标签形式。
	- 可设置拟合点/线的随机颜色。
	- 可弹出拟合图。
	- 可生成包含拟合图及其数据的html。
	- 在表格中显示拟合图的数据。
5. 残差图：
	- 可选择X轴为：预期值、自变量、实际值。
	- 可选择是否标准化残差。                  
  	   当标准化残差时，显示Sigma2（95%）的上线和下线。
	- 可设置散点的随机颜色。
	- 在表格中显示残差图的数据。

![截屏-dataSimpleRegression](https://mararsh.github.io/MyBox/snap-dataSimpleRegression.jpg)        


#### 频数分布             
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 选择一列计算频数。
	- 选择是否忽略大小写
	- 设置小数位数
3. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
4. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。     

#### 数值百分比               
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 选择按照：列、行、所有
	- 选择把负数计为：零、绝对值
	- 设置小数位数
	- 可选对非数字：略过、计为零。
3. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。
4. 当选择当前页或选中行时，目标还可以为：在表内指定位置插入、附加、或替换。     
      

### 数据图<a id="dataCharts" />       
#### 坐标图             
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 当数据无法被解析为数字时，计为零。
	- 忽略非法数据。
	- 当处理所有数据行（所有页）时，坐标图需要考虑内存限制。
3. 数据轴：
	- 选择一列为“类别轴”，以定义数据名字。
	- 在“数值轴”方向上可以选择多列为“数值”，不同的数值序列显示为不同颜色或者形状。
	- 缺省情况下，“类别轴”是横向轴、“数值轴”是竖向轴。              
          可以在面板参数中设为翻转：“类别轴”是竖向轴、“数值轴”是横向轴。
4. 参数：
	- 面板：标题、字体、图例位置、小数位数、是否显示零度线/网格线、线宽等。
	- 类别轴：标签、字体、位置、刻度、当作字串处理还是数字处理、坐标等。
	- 数值轴：标签、字体、位置、刻度、坐标等。
5. 条图：
	- 以条块的高度表示数据的大小。
	- 类别列总是被当作字串处理。
6. 线图：
	- 以连接点的线条表示数据的趋势。
	- 类别列可以是数字或字串。
7. 气泡图：
	- 以不同半径的多个圆表示数据的大小。
	- “类别”列和“数值”列定义数据的坐标。
	- 选择若干“大小”列，定义数据的大小。
	- 所有列都必须是数字，大小列必须是非负数。
8. 散点图：
	- 以符号表示数据的分布。
	- 类别列可以是数字或字串。
9. 面积图：
	- 以面积表示数据的大小。
	- 类别列总是被当作字串处理。
10. 在数据表中显示坐标图的数据。              

![截屏-dataChartXY](https://mararsh.github.io/MyBox/snap-dataChartXY.jpg)         

#### 饼图             
1. 选择数据：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
2. 计算：
	- 选择一列为“类别”。
	- 选择一列为“数值”。
	- 以分割的圆表示数据的比例。
	- 数值列必须都是非负数。
	- 当处理所有数据行（所有页）时，饼图需要考虑内存限制。   
3. 在数据表中显示饼图的数据。           

![截屏-dataChartPie](https://mararsh.github.io/MyBox/snap-dataChartPie.jpg)        
      
#### 箱线图             
1. 箱线图用来展示数据的分布：         
	- 数据按照列/行/全部来升序排序             
	- 以下项可以显示出数据的聚集性和离散性：             
                  最小值          Q0 =  位于数据列的 0%（开头）             
                  下四分位数   Q1 =  位于数据列的 25%             
                  中位数          Q2 =  位于数据列的 50%（中部）             
                  上四分位数   Q3 =  位于数据列的 75%             
                  最大值          Q4 =  位于数据列的 100%（结尾）             
	- 以下项可以用于标识数据的异常值：             
                  下极端异常值线    E1 =  Q1 - 3 * ( Q3 - Q1 )             
                  下温和异常值线    E2 =  Q1 - 1.5 * ( Q3 - Q1 )             
                  上温和异常值线    E3 =  Q3 + 1.5 * ( Q3 - Q1 )             
                  上温和异常值线    E4 =  Q3 + 3 * ( Q3 - Q1 )             
	- 以下项可以用于离散性的参考：             
                  均值  = 数据的平均数           
2. 计算：
	- 基于线图。
	- 选择按照：列、行（选择类别列）、所有.
3. 设置或选择：             
	- 箱子的宽度。
	- 是否显示异常值线或均值。
	- 是否显示各类数值的连线、是否虚线。
	- 随机颜色。
4. 在数据表中显示箱线图的数据。           

![截屏-dataChartBox](https://mararsh.github.io/MyBox/snap-dataChartBox.jpg)        

#### 自比较条图               
1. 自比较条图用来对比数据与参考值。以下规则用来计算所选数据值的颜色条：       
	- 若值等于零，不显示条
	- 当按绝对值比较时：                          
                最大值 = 列/行/所有的最大绝对值            
                百分比 = 值的绝对值 /  最大值            
                宽度 = 最大宽度 * 百分比            
                颜色 = 若值大于零， 为列的颜色；若值小于零，为列的颜色的反色            
	- 当按最大最小值区间比较时：            
                最大值 = 列/行/所有的最大值            
                最小值 = 列/行/所有的最小值            
                百分比 = （值 - 最小值）/ （最大值 - 最小值）            
                宽度 = 最大宽度 * 百分比            
                条的颜色 = 列的颜色                     
2. 数据：选择表中行、或所有数据行（所有页），同时选择列。            
3. 计算：
	- 选择类别列（非必要）
	- 选择比较：列、行、所有
	- 选择按照：绝对值、最大最小区间
	- 当处理所有数据行（所有页）时，自比较条图需要考虑内存限制。 
4. 设置或选择：
	- 最大宽度。
	- 是否显示行号、值、百分比、类别、计算出来的值。
5. 编辑图中数据
6. 编辑图的html      		-     

![截屏-dataChartSelfComparison](https://mararsh.github.io/MyBox/snap-dataChartSelfComparison.jpg)        

#### 相比较条图            
1. 相比较条图用来对比两类数据。以下规则用来计算颜色条：             
	- 若值等于零，不显示条      
	- 当按绝对值比较时：            
                  最大值 = 两个值列的最大绝对值            
                  百分比 = 值的绝对值 /  最大值            
                  宽度 = 最大宽度 * 百分比            
                  颜色 = 若值大于零， 为列的颜色；若值小于零，为列的颜色的反色            
	- 当按最大最小区间比较时：            
                  最大值 = 两个值列的最大值            
                  最小值 = 两个值列的最小值            
                  百分比 = （值 - 最小值）/ （最大值 - 最小值）            
                  宽度 = 最大宽度 * 百分比            
                  条的颜色 = 列的颜色            
2. 数据：选择表中行、或所有数据行（所有页）。
3. 计算：
	- 选择类别列（非必要）
	- 选择两个数值列
	- 选择按照：绝对值、最大最小区间
	- 当处理所有数据行（所有页）时，自比较条图需要考虑内存限制。 
4. 设置或选择：
	- 最大宽度。
	- 是否显示行号、值、百分比、类别、计算出来的值。
5. 编辑图中数据
6. 编辑图的html      		-     

![截屏-dataChartComparison](https://mararsh.github.io/MyBox/snap-dataChartComparison.jpg)        


### 管理数据<a id="manageData" />       
此工具管理以下对象：        

1. 数据文件
	- 当csv/excel/文本数据文件被相应编辑器打开时，增加/修改此文件的记录。
	- 数据保存在数据文件中。
	- 删除数据文件的记录不会导致数据文件本身被删除。
2. 数据粘贴板
	- 当数据复制到MyBox粘贴板中时，增加此数据的记录。
	- 数据保存在MyBox内部目录下的文件中。
	- 删除数据粘贴板的记录将会同时删除它的内部文件。
3. 矩阵
	- 在矩阵管理器中增删改矩阵。
	- 数据保存在MyBox数据库中。
	- 删除矩阵的记录将会同时删除矩阵包含的数据。           
4. 数据库表
	- 在数据库表管理器中增删改数据库表。
	- 数据保存在MyBox数据库的表中。
	- 删除数据库表的记录将会同时删除数据库表包含的数据。           

![截屏-manageData](https://mararsh.github.io/MyBox/snap-dataManage.jpg)         

### 拼接数据<a id="spliceData" />
1. 选择或打开两个数据。
2. 分别选择两个数据的行列：
	- 行可以是：当前页、选中的行、或所有页。
	- 选择列，若不选则取所有列。
	- 设置行过滤条件。
3. 拼接选项：
	- 方向：横向、纵向。
	- 行/列数按照：数据A、数据B、较长的、较短的。
4. 目标可为：新的csv/excel/文本文件、矩阵、系统粘贴板、MyBox粘贴板、数据库表。            
        
![截屏-dataSplice](https://mararsh.github.io/MyBox/snap-dataSplice.jpg)              

### 数据文件<a id="dataFiles" />
1. 编辑数据文件：
	- 当文件读取异常时，改变选项然后点击按钮“刷新”。
	- 对于CSV文件和文本文件，选项包括：文件的字符集、是否以第一行作为字段的名字、字段的分隔符。
	- 对于Excel文件：
 		- 选项包括：工作表号、是否以第一行作为字段的名字。
 		- 添加/删除/重命名excel工作表。
 		- 工具只能处理Excel文件中的基本数据。如果文件包含格式、公式、风格、或图，建议把修改保存为新文件以免数据丢失。
2. 批量转换/分割数据文件：
	- 源文件格式：csv、excel、文本。可以设置源文件的选项。
	- 目标文件格式：csv、文本、excel、xml、json、html、pdf。可以设置目标文件的选项。
	- 可按最大行数分割转换后的文件。
3. 合并csv/excel/文本数据文件。        

![截屏-dataFile](https://mararsh.github.io/MyBox/snap-dataFile.jpg)           

### 系统粘贴板中数据<a id="dataInMyBoxClipboard" />
1. 读取并解析系统粘贴板的内容。
2. 可选数据分隔符：特殊字符、或输入的规则表达式。
3. 可选把第一行定义为列名。
4. 选择要粘贴的位置：行、列。
5. 选择操作：替换、插入、附加。

![截屏-dataInSC](https://mararsh.github.io/MyBox/snap-dataInSC.jpg)           

### MyBox粘贴板中的数据<a id="dataInSystemClipboard" />
1. 选择要复制的行和列。
2. 选择要粘贴的位置：行、列。
3. 选择操作：替换、插入、附加。

![截屏-dataInMC](https://mararsh.github.io/MyBox/snap-dataInMC.jpg)         


### 矩阵<a id="matrix" />
1. 编辑矩阵。
2. 矩阵可保存和取用。
3. 矩阵的一元计算：转置、行阶梯形、简化行阶梯形、行列式值-用消元法求解、行列式值-用余子式求解、逆矩阵-用消元法求解、逆矩阵-用伴随矩阵求解、矩阵的秩、
伴随矩阵、余子式、归一化、乘以数值、除以数值、幂。        
4. 矩阵的二元计算：加、减、乘、克罗内克积、哈达马积、水平合并、垂直合并。

![截屏-dataMatrix](https://mararsh.github.io/MyBox/snap-dataMatrix.jpg)          

### 数据库表<a id="dataTables" />
1. 表名和列名应当满足“SQL标识符的限制”：
	- 最大长度为128。
	- “普通标识符”          
 		- 指未用双引号包围的标识符：            
 		- 只能由字母、数字、和下划线(_)构成。
 		- 只能由字母开头。
 		- 字母和数字可以是Unicode（可以是中文）。
 		- 不能是SQL保留字。
 		- 当保存在数据库中，它被转换为大写的。
 		- 当在SQL语句中引用它时，忽略大小写。              
                   例如，AbC等同于abc和aBC。
	- “定界标识符”
 		- 指用双引号包围的标识符。
 		- 可以包含任何字符。
 		- 当保存在数据库中时，它只取双引号包围的字串。
 		- 当在SQL语句中引用它时，必须用双引号包围它，除了以下情形：它只包含大写字母和下划线。        
                   例如, "AbC"不同于AbC或"ABC"，而"ABC"等同于ABC和aBc.
2. 当MyBox创建表名或列名：
	- 非法字符将被转换为下划线。
	-  若标识符开头不为字母，则字符"a"将被加到前面。
3. 数据库表被创建以后：
	- 不能修改和删除主键的定义。
	- 可以添加和删除列定义，但是不能修改列定义。      

![截屏-dataTables](https://mararsh.github.io/MyBox/snap-dataTables.jpg)         


### 数据库SQL<a id="dbSQL" />
1. 提供常用SQL语句的示例。
2. 自动生成所有用户数据表名的列表。
3. 查看所有用户数据表的定义。
4. 显示执行输出和查询结果。   
5. SQL代码可以被组织为树状信息。
6. 可以打开或保存为外部文件。     

![截屏-dbSQL](https://mararsh.github.io/MyBox/snap-dbSQL.jpg)         


### JShell（Java交互编程工具）<a id="JShell" />             
JShell是JDK包含的工具之一：            

1. JShell提供交互执行"snippets"的能力，即"读取-执行-输出"循环 (REPL)。 
2. "Snippet"是Java编程语言的单个表达式、语句、或声明的代码：
	- 语句结尾必须有分号，而表达式不需要。
	- 可以定义变量和方法、然后调用它们。  
3. 外部Java类应当可访问：
	- JShell取系统环境中CLASSPATH。  
	- 其它Jar文件或路径可以附加在CLASSPATH后面。  
	- 除了基本类，在调用多数Java类之前需要把它们导入进来。  
4. JShell可以用于科学计算、和调试Java代码。           

此工具帮助图形化运行JShell:             

1. 输入若干snippet，然后点击 "开始"按钮以执行它们： 
2. Snippets被逐个计算。  
3. Snippets的结果会影响后续的snippets，即如"一个运行环境"。  
4. 所有已执行过的snippets的属性被显示在一张表中。
5. 点击按钮"删除"或"清除"以把一些或全部snippets从当前环境中移除出去。
6. 点击按钮"重置"以清零JShell，环境将变为空白。
7. 按下"CTRL+1"可以弹出代码的完成建议列表。
8. 若已添加了MyBox类路径，则可以引用MyBox的所有方法。
9. JShell代码被组织为树形，并提供示例。

![截屏-JShell](https://mararsh.github.io/MyBox/snap-JShell.jpg)         

### JEXL（Java表达式语言）<a id="JEXL" />             
JEXL（Java表达式语言）是一个库文件，以变量和脚本来动态生成值：        

1. JEXL与Java的语法有所不同，它更像是javascript。 
2. 在运行表达式/脚本之前，其中所有变量应当在JexlContext中有值。 
3. 创建Java类的实例为本地变量以引用它们。当用“new”时注意写全包名。
4. JEXL可用于科学计算和数据处理。           

此工具帮助图形化运行JEXL：              

1. 输入JEXL表达式或脚本。                
    注意：用单引号而不是双引号来围住字符串。
2. 按以下格式输入Java代码，以设置JexlContext：                  
                jexlContext.set("name", value);                  
    例如，设置以下语句以在表达式/脚本中使用Math.PI：                  
                jexlContext.set("Math", Math.class);                  
3. 输入JEXL脚本的参数（如果有）。以逗号分隔。                  
4. 点击按钮“开始”以计算表达式/脚本。                  
5. MyBox在JShell环境下自动执行以下步骤：                  
	- 把MyBox的库路径加到CLASSPATH。                  
	- 导入必要的JEXL包。                  
	- 运行JexlContext代码。                  
	- 带上参数（如果有）计算表达式/脚本。                  
6. 若所有变量和参数都有合法的值，则结果显示在右边面板中。                  
7. JEXL代码被组织为树形，并提供示例。

![截屏-JEXL](https://mararsh.github.io/MyBox/snap-JEXL.jpg)                           


### Javascript<a id="Javascript" />             
此工具帮助编辑、运行、保存Javascript代码。                  
Javascript代码被组织为树形，并提供示例。        

![截屏-Javascript](https://mararsh.github.io/MyBox/snap-Javascript.jpg)                           
         
### 通用的数据处理<a id="dataManufacture" />
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
	- 导出格式可选：csv、xml、json、xlsx、html、pdf。
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
                           https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_daily_reports                
 	  	-  字段随着时间在变化。。。        
                      以下是"01-22-2020.csv"的格式：        
                             Province/State,Country/Region,Last Update,Confirmed,Deaths,Recovered        
                     以下是"05-15-2020.csv"的格式：        
                             FIPS,Admin2,Province_State,Country_Region,Last_Update,Lat,Long_,Confirmed,Deaths,Recovered,Active,Combined_Key        
	  	-  坐标系统是WGS_84。        
	  	-  全部是零的数据项将被跳过。
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

### 其它<a id="dataOthers" />
1. 编码条码
 	-  支持的一维码
 	  	- 类型： Code39, Code128, Codabar, Interleaved2Of5, ITF_14, POSTNET, EAN13, EAN8, EAN_128, UPCA, UPCE, Royal_Mail_Customer_Barcode, USPS_Intelligent_Mail        
 	  	- 一维码选项：朝向、宽高、分辨率、文字位置、字体大小、空白区宽度等。不同类型的选项不同。
 	-  支持的二维码
 	  	- 类型：QR_Code, PDF_417, DataMatrix
 	  	- 二维码选项：宽高、边沿、纠错级别、压缩模式。不同类型的选项不同。
 	  	- 二维码QR_Code可以在中心显示一个图片。根据纠错级别自动调整图片大小。
 	-  示例参数和建议值。
 	-  对生成的条码即时检验。
2. 解码条码
 	-  支持的一维码类型： Code39, Code128, Interleaved2Of5, ITF_14,  EAN13, EAN8, EAN_128, UPCA, UPCE
 	-  支持的二维码类型：QR_Code, PDF_417, DataMatrix
 	-  显示条码内容和元数据（条码类型、纠错级别等）
3. 消息摘要
 	-  生成文件或者输入文本的消息摘要
 	-  支持MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512/224, SHA-512/256, SHA3-224, SHA3-256, SHA3-384, SHA3-512
 	-  输出：Base64、十六进制、格式化的十六进制。
4. 编码/解码Base64
 	- 把文件或者文本编码为Base64。
 	- 解码Base64文件或者Base64文本。
 	- 对于文本可以设置字符集。
 	- 输出为文件或者文本。
5. 从ttc文件中提取ttf文件

## 文件工具<a id="fileTools" />

### 管理文件/目录<a id="directoriesArrange" />
1. 查找、删除、复制、移动、重命名。
2. 目录同步，包含复制子目录、新文件、特定时间以后已修改文件、原文件属性，以及删除源目录不存在文件和目录，等选项。
3. 整理文件，将文件按修改时间或者生成时间重新归类在新目录下。此功能可用于处理照片、游戏截图、和系统日志等需要按时间归档的批量文件。
4. 删除目录下所有的空目录。
5. 删除"无限嵌套目录"（由于软件错误而生成的无法正常删除的目录）。
6. 删除系统临时目录下的文件。

### 归档/压缩/解压/解档<a id="archiveCompress" />
1. 归档是把多个文件/目录聚集为单个文件的过程，有的归档格式支持同时实现压缩（如zip和7z）。解档是还原归档文件的过程。
2. 压缩是把单个文件转变为一个更小的文件的过程。通常是先归档再压缩。解压是还原压缩文件的过程。
3. 支持归档格式： zip, tar, 7z（只支持部分算法）, ar, cpio。
4. 支持解档格式： zip, tar, 7z（只支持部分算法）, ar, cpio, arj, dump。
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
1. 闹钟，包括时间选项和音乐选项，支持铃音"喵"、wav铃音、和MP3铃音，可以在后端运行。        

## 网络工具<a id="netTools" />

### 下载网页<a id="downloadFirstLevelLinks" />
1. 列出给定网址中的第一级链接。
2. 下载用户选择链接的网页:
	-  选择有用的链接。无意义的链接可能生成无用文件并且干扰最后的目录索引。
	-  使用功能“设置子目录名”以使子目录名字合理。
	-  使用功能“以链接名/标题/地址为文件名”以使文件名有意义。
	-  使用功能“在文件名前添加序号”以使文件名能正确排序。        
       MyBox可以正确排序形如"xxx9", "xxx36", "xxx157"的文件名。        
3. 选项：重写网页中的链接、生成目录索引、修改网页编码、合并为文本/网页/Markdown/PDF。
4. 可以设置网页样式和PDF的字体。

### 微博截图工具<a id="weiboSnap" />            
此工具目前失效。           
                 
1. 自动保存任意微博账户的任意月份的微博内容、或者其点赞的内容。
2. 设置起止月份。
3. 确保页面完全加载，可以展开页面包含的评论、可以展开页面包含的所有图片。
4. 将页面保存为本地html文件。由于微博是动态加载内容，本地网页无法正常打开，仅供获取其中的文本内容。
5. 将页面截图保存为PDF。可以设置截图的格式、像素密度，和PDF的页尺寸、边距、作者等。
6. 将页面包含的所有图片的原图全部单独保存下来。
7. 实时显示处理进度。
8. 可以随时中断处理。程序自动保存上次中断的月份并填入作本次的开始月份。
9. 可以设置错误时重试次数。若超时错误则自动加倍最大延迟时间。
10.  首次运行时需要初始化webview。如果错过此步，可以点击按钮“SSL”。        

![截屏-weibo](https://mararsh.github.io/MyBox/snap-weibo.jpg)         

### 网页浏览器<a id="webBrowser" />
1. 多页签显示网页
2. 管理浏览历史
3. 管理收藏的网址

![截屏-webBrowser](https://mararsh.github.io/MyBox/snap-webBrowser.jpg)         

### 查询网址<a id="queryAddress" />
1. 查询URL/主机/IP
2. 可选：本地信息、查询ipaddress.com、查询ip.taobao.com
3. 查看/保存SSL证书。

### 批量查询DNS<a id="queryDNS" />
1. 输入主机/ip列表。提供示例。
2. 打开hosts文件。
3. 执行刷新DNS的命令。


### 解码/编码URL<a id="encodeDecodeURL" />
此工具帮助将字符串与application/x-www-form-urlencoded MIME之间转换。        
在编码时应用以下规则：        

-  "a"到"z"之间的字母、 "A"到 "Z"之间的字母、以及 "0"到 "9"之间的数字保留原样。
-  特殊字符".", "-", "*", 和 "_"保留原样。
-  空字符" "转换为加号"+"
-  其它所有字符被认为是不安全的，首先采用指定的字符集转换为1个或多个字节。        
   然后每个字节被表达为形如 "%xy"的3个字符的字符串，其中xy是字节的十六进制两位数字表示。        

解码反过来。    


### 管理安全证书<a id="securityCerificates" />
1. 读取任意密钥库文件中的证书内容，可导出为html文件
2. 添加/读取任意证书文件的内容
3. 下载并安装任意网址的证书。
4. 删除密钥库中的证书。
5. 自动备份。


## 开发工具<a id="devTools" />
1. 开/关内存监视条
2. 开/关CPU监视条
3. MyBox的属性
4. MyBox日志：
	-  类型包括：错误、信息、调试、终端：
 	  	- 所有日志都会显示在终端。
 	  	- “错误”和“信息”总是写入数据库。
 	  	- ”调试“只在开发模式下才写入数据库。
 	  	- ”终端“从不写入数据库。
	-  字段：标识、时间、类型、文件名、类名、方法名、行号、调用者、注释。
	-  “调用者”是调用链，每行是一个节点：文件名、类名、方法名、行号。调用链只记录MyBox自身的方法。
	-  “错误”会导致查看日志的界面被弹出。
5. 运行系统命令
6. 启动JConsole（Java监视和管理控制台）
7. 管理语言
8. 制作图标
9. 编辑MyBox内部表的数据
10. 自动测试-打开界面
11. 给作者发消息

## 设置<a id="settings" />
1. 界面：
	-  语言、字体大小、图标大小
	-  选择高清晰图标（100x100）还是普通图标（40x40）。
           显示器分辨率不高于120dpi时，建议选择普通图标。在非高清晰的显示器上显示高清晰图标反而会模糊。
	-  控件颜色、是否显示控件文字、界面风格
	-  是否恢复界面上次尺寸
	-  是否在新窗口中打开界面
	-  是否在鼠标经过按钮时弹出“设置颜色”
	-  是否在鼠标经过按钮时显示/隐藏面板
	-  界面消息的颜色、时长和字体大小。
2. 基础：
	-  JVM最大可用内存
	-  网络超时
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
6. 地图：
	-  设置数据密钥
7. 清除个人设置。
8. 打开数据目录。

## 窗口<a id="windows" />
1. 刷新/重置/全屏/置顶窗口
2. 关闭其它窗口
3. 重启MyBox
4. 最近访问的界面
5. 窗口/面板截屏

## 帮助<a id="helps" />
1. MyBox快捷键：
	-  若焦点在“文本输入”控件中，则Delete/Home/End/PageUp/PageDown/Ctrl-c/v/z/y/x作用于此控件中的文本。否则，快捷键作用于界面。
	-  若焦点不在“文本输入”控件中，Ctrl/Alt键可省略。例如，焦点在图片上时，按"c"以复制，按"2"以设置为面板尺寸。
2. 功能列表
3. ReadMe
4. 文档：帮助用户启动下载任务。若数据目录下已有MyBox文档，则MyBox会自动发现它们。
5. About


# 开发日志<a id="devLog" />             
2022-8-4 版本6.5.8          

* 数据：行过滤/行表达式可用统计值名作为占位符；不再有列过滤。                                      
* 解决：地图失效；“粘贴MyBox粘贴板中的数据”弹出错误；“播放图列表”在范围内循环时帧序号出错。        
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.8)             

2022-7-28 版本6.5.7          

* 数据：行过滤可设置最多源数据行数；“设置风格”可列过滤；“赋值”可为列平均/中位数/众数；新计算“行表达式”；统计/百分比/归一化时对于非数字可选忽略或取值为零；
   排序时可设置最多结果行数。              
* 性能：统计/排序/转置不再有内存限制；在非Fx线程中计算表达式时避免频繁切换到Fx线程。                
* 界面：一些按钮可设置是否在鼠标经过时弹出菜单；改进一些界面以免控件拥挤；。                         
* 解决：“文件重命名”在未匹配情况下会删除源文件（！！抱歉）；转换/计算数据库表数据时有错；读取系统粘贴板会残留临时文件，持续监听可能造成磁盘空间被占尽（！！抱歉）；
图片OCR失效；添加/提取透明通道失效。        
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.7)             
  
2022-6-11 版本6.5.6          

* 数据：所有数据处理均可设置行过滤条件(JavaScript代码的布尔表达式) ；横向/纵向拼接两个数据；“赋值”可以设置为行表达式；按行过滤条件删除数据；“设置风格”可增删改规则。              
* 计算：管理和编辑JavaScript代码；管理和编辑JEXL(Java Expression Language)代码；JShell可设置类路径、可弹出代码完成建议(CTRL+1)、完善示例。                
* 数据图：参数可在弹出窗口中设置(F12)。                         
* 解决：一些fxml文件包含本地文件路径；点击表头应按数据类型排序；在文本菜单中点击“替换”按钮弹出错误；对所有页或数据库表无法计算描述性统计。        
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.6)             
   
2022-5-10 版本6.5.5          

* 数据编辑：设置单元风格；可把第一行设为列名；转置可对所有页实施，可选把第一列设为列名；数据库执行结果不限行数；示例数据。                
* 数据计算：描述性统计可计算更多的值；统计/百分比/归一化可选按照列/行/全部；计算频度；简单线性回归。                
* 数据图：类别轴可为字串或数字；显示图的数据；比较条图；自比较条图；箱线图。               
* 树形信息：可显示序列号；可选复制子节点或所有后代；左键点击节点的选项。                     
* 其它：生成/粘贴图片的base64代码；在调色盘上输入/选择新颜色。                     
* 解决：数据文件的自动备份失效；数据统计所有页时所选列未生效；另存文本文件时没有按要求删除BOM；无法清除所有颜色。          
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.5)             
   
2022-4-3 版本6.5.4         

* 树形信息：管理树型；编辑节点；带标签的导入、导出；输出树图；提供示例；延展为笔记、收藏的网址、JShell代码、Javascript代码、SQL代码。                
* 完善：JShell可以执行多行语句；管理和执行数据表的查询语句；网页“左键点击链接或图片时”有更多选项；输入框的历史值用弹出窗口实现。                     
* 解决：批量图片OCR时信息粒度没有生效；编辑网页时保存新网页会弹出错误；以时间查询树时无返回； 
数据表定义“定界标识符”后可能出错；文件编辑器“自动保存”失效。                    

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.4)             
   
2022-3-6 版本6.5.3         

* 数据：数据可转换为数据库表；管理和编辑数据库表；执行数据库SQL语句。                      
* 计算：JShell（Java代码交互执行工具）的图形界面。         
* 开发：编辑MyBox内部表的数据；启动JConsole（Java监视和管理控制台）。                
* 解决："提取网页的图片和链接"失效；"文件解档"构建错误的文件路径；当目标目录是源目录的子目录时"目录同步"会无限循环；
"矩阵的幂"缺少输入框。         

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.3)             

2022-2-1 版本6.5.2         

* 图像：可显示网格；范围可被所有图片共享；滤色；可在图片上添加多行文字并设置背景边框；保存或修改图片时可选渲染参数。       
* 数据：选择数据和参数以生成条图/线图/饼图/散点图/气泡/面积图。                      
* 界面：选择数据时，若不选则处理所有。         
* 代码：弹出菜单改为子窗口；图像内部处理均用ARGB；自动测试-打开界面；减少数据处理的冗余代码。                
* 解决：对非透明图片（如jpg）批量添加文本时处理错误；恢复备份时图像未更新；处理多页数据的多个功能有错；包含特殊字符时菜单无法弹出；
弹出菜单打开时输入文本可能触发快捷键。               

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.2)           

虎年吉祥！         

2021-12-26 版本6.5.1         

* 编辑表格：单击开始编辑，失焦提交修改；类型检验；行的复选框。       
* 编辑数据：表格/文本为主/辅编辑模式，废弃表单模式；统一数据管理界面；处理逻辑从界面移至对象。       
* 图像：改进图像文件的读取方法；在后端加载缩略图；颜色选择窗口可以直接加载示例调色盘。                         
* 界面：网页和弹出窗口的样式可所有界面共享；查找/替换的选项可所有界面共享；截图当前窗口或节点的内容。                  
* 解决：笔记无法输入换行；处理图像的“剪切”按钮失效；另存图片时未提供扩展名选项；长文件名导致弹出菜单失效或者备份失败；文本/字节编辑器的面板无法调整宽度。               

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.1)            

2021-10-13 版本6.4.9         

* 改进：文本编辑器以行数分页、每个换行符算作一个字符；批处理的目标文件名均自动附加时标。       
* 新增：从网页中提取表格数据；网页/笔记编辑器可以关闭页签；转换word/ppt/文本为PDF。          
* 平台：升级到java17和javafx17；程序可以从非英文目录下启动了；Linux上可正常播放媒体了。          
* 解决：文本/字节编辑器读写错误；图像的范围不显示且无法保存；输入文本时可能触发快捷键；多个功能失效。               

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.9)           

2021-9-21 版本6.4.8         

* 数据：完善表单操作；可编辑数据文本；支持文本数据文件；数据粘贴板可保存和管理。       
* 改进：弹出的文本/图像/网页可与原内容同步；可查询颜色；图像列表支持PDF和PPT。          
* 解决的主要问题：PDF的OCR无法刷新；矩阵右键菜单时会内存溢出；Linux上无法选择无后缀的文件。       

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.8)          

中秋节快乐！       

2021-8-17 版本6.4.7         

* 改进界面：统一图像/网页/文本的弹出窗口和右键菜单、完善图像/文本/数据粘贴板、优化Markdown/网页/笔记/图像编辑器、查看PDF页面时可提取文字和转换为网页。       
* 改进代码：拆解大类、调整快捷键、校正图标颜色。          
* 解决的主要问题：jpg图片读取得很慢；图像处理的画笔和橡皮檫的透明色没生效。        

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.7)         

2021-7-7 版本6.4.6         

* 新增：系统粘贴板中的文本、MyBox粘贴板中的文本、所有文本输入控件的右键菜单、网页的右键菜单。       
* 改进和修正：文本编辑器、清除窗口残留。          
* 解决的主要问题：无法打开微软文档（word/excel/ppt）；数据表的页码为空。       

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.6)         

2021-6-24 版本6.4.5         

* 新增：查看和转换Word文件、查看/转换/分割/合并/提取PPT文件、编辑图片列表、播放图片列表/PDF/PPT、MyBox粘贴板中的图像、批量粘贴图片。       
* 改进和修正：系统粘贴板中的图像、文本编辑器、字节编辑器。          
* 解决的主要问题：合并html为pdf出错；分割图片保存时出错；Markdown编辑器同步更新时无法输入汉字。       

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.5)         

2021-5-15 版本6.4.4       

* 改进和修正网页编辑器、Markdown编辑器、网页浏览器。     
* 新增：管理收藏的网址、查询网址历史、从Excel/Word/PowerPoint/Publisher/Visio中提取文本、功能列表。       
* 升级到Java16和javafx16。       
* 解决的主要问题：数据粘贴板无法输入汉字；CSV/Excel转换为PDF的结果是错误的；导出数据的界面异常；保存网页frame会覆盖frameset。       

[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.4)         

2021-4-16 版本6.4.3        
- 网页编辑器：对于frameset可以选择frame来编辑；图片列表；按标签/编号/名字查找元素；查找网页中的字串。        
- 合并颜色管理和调色盘管理：可定义多个调色盘；同一颜色在不同调色盘中可有不同的名字和顺序编号；优化取色界面。        
- 笔记：按标题/内容/时间查找；可选是否查找子笔记本；复制笔记本；复制/移动笔记。        
- 改进：合并数据文本/表格粘贴板；图像处理的画笔在起笔时应用混合模式。        
- 新增：按URL/主机/IP查询网址；批量查询DNS；运行系统命令；编码/解码Base64。        
- 移除：地图和微博无需安装证书；网页浏览器不再支持忽略SSL认证；疫情报告不再访问过期链接；笔记的示例不包含非法链接。        
- 解决的主要问题：新SSL证书无法加入密钥库；一些界面/功能首次运行时异常；一些操作没有备份文件。        
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.3)        

2021-3-21 版本6.4.2        
- 笔记。设置笔记的标签、按标签查询笔记。笔记本可移动。富文本编辑笔记。设置显示风格。导出格式增加：单个网页、网页框架、xml，可以设置导出文件的字符集和样式。                 
- 图像处理：“文字”和“画笔”可以设置混合模式。            
- 新增数据表格粘贴板。对于数据文本/表格粘贴板，可以修改数据文本的分隔符。                     
- 解决的主要问题：网页编辑器无法保存。处理frameset的链接。下载第一级链接无法处理包含特殊字符的链接。文件名避免包含空格。            
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.2)                 

2021-3-8 版本6.4.1        
- 笔记是html格式的信息片段，笔记本是一组笔记和笔记本的集合，笔记本形成一棵树。提供示例。                 
- 网页：处理链接的菜单；编辑网页的按钮。            
- 数据文件：合并csv/excel文件；处理空的csv/excel文件；添加/删除/重命名excel表单。            
- 改进：所有编辑器都支持自动备份；图像处理支持颜色距离的方根、自动刷新剪贴板；清除自包含包中不必要的文件。            
- 解决的主要问题：下载第一级链接对一些网址出错；源文件名包含非英文时批量OCR失败；网页浏览器删除历史记录时出错。            
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.4.1)                             

2021-2-11 版本6.3.9                  
-  改进：查看/编辑多帧图像时可选择帧。读取CSV/Excel时总是使用iterator。修改PDF属性时可选清除安全属性。
-  解决的主要问题：编辑保存CSV/Excel后总是回到第一页。解档文件总是失败。解档zip时非英文解析为乱码。          
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+isue+i+is%3Aclosed+milestone%3Av6.3.9)                   
春节快乐！牛年大吉！         

2021-1-27 版本6.3.8                  
-  添加：编辑和转换csv和excel文件。数据粘贴板。编辑和保存矩阵。批量设置html的风格。          
-  改进：颜色量化可以设置通道权重。焦点不在文本输入控件时快捷键可以省略Ctrl/Alt。数据转换/导出采用流读取。                 
-  解决的主要问题：批量替换图片颜色的距离不生效。PDF的用户密码和所有者密码混乱。文本过滤界面分页错误。表字段也需要翻译。时间树上公元前的时间解析错误。                    
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issuesox/losed+milestone%3Av6.3.8)               

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