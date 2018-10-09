## [User Guide in English](https://mararsh.github.io/MyBox/english_interface.html)

## MyBox：简易工具集

这是利用JavaFx开发的图形化界面程序，目标是提供简单易用的功能。免费开源。

每个版本编译好的包已发布在Release目录下（点击上面的releases页签）。

可以下载exe包，在Windows上无需java环境、无需安装、解包可用：

[MyBox-3.7-exe.zip](https://github.com/Mararsh/MyBox/releases/download/v3.7/MyBox-3.7-exe.zip) 。

在Linux和Mac上缺省有Java环境，因此只提供jar包而未制作平台安装包。


在已安装JRE或者JDK的环境下，可以下载jar包 [MyBox-3.7-jar.zip](https://github.com/Mararsh/MyBox/releases/download/v3.7/MyBox-3.7-jar.zip) ，执行以下命令来启动程序：
<PRE><CODE>     java   -jar   MyBox-3.7.jar</CODE></PRE>


## 资源地址
[项目主页：https://github.com/Mararsh/MyBox](https://github.com/Mararsh/MyBox)

[源代码和编译好的包：https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)

[在线提交软件需求和问题报告：https://github.com/Mararsh/MyBox/issues](https://github.com/Mararsh/MyBox/issues)

[云盘地址：https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)


## 用户手册
[综述 https://github.com/Mararsh/MyBox/releases/download/v3.7/MyBox-UserGuide-3.7-Overview.pdf](https://github.com/Mararsh/MyBox/releases/download/v3.7/MyBox-UserGuide-3.7-Overview.pdf)

[图像工具 https://github.com/Mararsh/MyBox/releases/download/v3.7/MyBox-UserGuide-3.7-ImageTools.pdf](https://github.com/Mararsh/MyBox/releases/download/v3.7/MyBox-UserGuide-3.7-ImageTools.pdf)

[PDF工具 https://github.com/Mararsh/MyBox/releases/download/v3.1/MyBox-UserGuide-3.1-PdfTools.pdf](https://github.com/Mararsh/MyBox/releases/download/v3.1/MyBox-UserGuide-3.1-PdfTools.pdf)

[桌面工具 https://github.com/Mararsh/MyBox/releases/download/v3.1/MyBox-UserGuide-3.1-DesktopTools.pdf](https://github.com/Mararsh/MyBox/releases/download/v3.1/MyBox-UserGuide-3.1-DesktopTools.pdf)

[网络工具 https://github.com/Mararsh/MyBox/releases/download/v3.6/MyBox-UserGuide-3.6-NetworkTools.pdf](https://github.com/Mararsh/MyBox/releases/download/v3.6/MyBox-UserGuide-3.6-NetworkTools.pdf)


## 当前版本
当前是版本3.7，已实现的特点：
```
1. PDF工具：
	A. 将PDF文件的每页转换为一张图片，包含图像密度、色彩、格式、压缩、质量、色彩转换等选项。
	B. 将多个图片合成PDF文件，可以设置压缩选项、页面尺寸、页边、页眉、作者等。
	   支持中文，程序自动定位系统中的字体文件，用户也可以输入ttf字体文件路径。
	C. 压缩PDF文件的图片，设置JPGE质量或者黑白色阈值。
	D. 合并多个PDF文件。
	E. 分割PDF文件为多个PDF文件，可按页数或者文件数来均分，也可以设置起止列表。
	F. 将PDF中的图片提取出来。可以指定页码范围。
	G. 将PDF文件中的文字提取出来，可以定制页的分割行。
	H. PDF的批量处理。
	I. 可设置PDF处理的主内存使用量。
2. 图像工具：
	A. 增减：像素大小、饱和度、明暗、色相、红/蓝/绿通道。
	B. 滤镜：灰色、反色、黑白色、红/蓝/绿通道。
	C. 效果：模糊、锐化、边沿检测、海报（减色）、阈值化。
	D. 剪裁、换色、水印、圆角、阴影、斜拉、水平/垂直镜像、旋转、切边、加边。
	E. 定义“范围”：区域（矩形或圆形）和颜色匹配。用于局部处理图像。
	F. 图像处理上一步的“撤销”和“重做”。也可以随时恢复原图。
	G. 确保大图片处理的正确性和性能。
	H. 同目录下图像的导览
	I. 选择是否显示对照图。可以选择其它图片为对照图。
	J. 查看图片的元数据和属性。
	K. 图片的分割。支持均等分割和定制分割。
	L. 图片的合并。支持排列选项、背景颜色、间隔、边沿、和尺寸选项。
	M. 同屏查看多图，可以分别或者同步旋转和缩放。支持导览。
	N. 将图片转换为其它格式，包含色彩、长宽、压缩、质量等选项。
	O. 图像的批量处理。
	P. 调色盘
	Q. 像素计算器
3. 文件和目录工具：
	A. 目录/文件重命名，包含文件名和排序的选项。被重命名的文件可以全部恢复或者指定恢复原来的名字。
	B. 目录同步，包含复制子目录、新文件、特定时间以后已修改文件、原文件属性，以及删除源目录不存在文件和目录，等选项。
	C. 整理文件，将文件按修改时间或者生成时间重新归类在新目录下。
	   此功能可用于处理照片、游戏截图、和系统日志等需要按时间归档的批量文件。
	D. 文本编辑器。
4. 网络工具：
	A. 网页编辑器
		a. 富文本方式编辑本地网页或在线网页。
		b. 代码编辑器可以同步编辑html。
		c. 网页浏览器可以同步编辑器内容、也可以加载在线网页。支持前后导览、缩放字体、截图页面为整图或者PDF文件
	B. 微博截图工具
		a. 自动保存任意微博账户的任意月份的微博内容
		b. 设置起止月份。
		c. 确保页面完全加载，可以展开页面包含的评论、可以展开页面包含的所有图片。
		d. 将页面保存为本地html文件。由于微博是动态加载内容，本地网页无法正常打开，仅供获取其中的文本内容。
		e. 将页面截图保存为PDF。可以设置页尺寸、边距、作者、以及图片格式。
		f. 将页面包含的所有图片的原图全部单独保存下来。
		g. 实时显示处理进度。
		h. 可以随时中断处理。程序自动保存上次中断的月份并填入作本次的开始月份。
		i. 可以设置错误时重试次数。
5. 支持图像格式：png,jpg,bmp,tif,gif,wbmp,pnm,pcx。
6. 闹钟，包括时间选项和音乐选项，支持铃音“喵”、wav铃音、和MP3铃音，可以在后端运行。
7. 设置：切换中英文、是否显示注释、PDF处理的最大主内存、如何处理透明通道、退出程序时是否关闭闹钟、清除个人设置。
8. 多种界面皮肤。
```

## 开发日志
```
2018-10-09 版本3.7 微博截图工具：利用Javascript事件来依次加载图片，确保最小间隔以免被服务器判定为不善访问，
同时监视最大加载间隔以免因图片挂了或者加载太快未触发事件而造成迭代中断。
图像处理“效果”：模糊、锐化、边沿检测、海报（减色）、阈值化。

2018-10-04 版本3.6 微博截图工具：继续调优程序逻辑以确保界面图片全部加载；整理代码以避免内存泄露。
降低界面皮肤背景的明亮度和饱和度。
在文档中添加关于界面分辨率的介绍。

2018-10-01 版本3.5 微博截图工具：调优程序逻辑，以确保界面图片全部加载。
提供多种界面皮肤。

2018-09-30 版本3.4 修正问题：1）微博截图工具，调整页面加载完成的判断条件，以保证页面信息被完整保存。
2）关闭/切换窗口时若任务正在执行，用户选择“取消”时应留在当前窗口。
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

2018-08-11 版本2.10 图像的分割，支持均等分割个和定制分割。使图像处理的“范围”更易用。
同屏查看多图不限制文件个数了。

2018-08-07 版本2.9 图像的裁剪。图像处理的“范围”：依据区域（矩形或圆形）和颜色匹配，可用于局部处理图像。

2018-07-31 版本2.8 图像的切边、水印、撤销、重做。Html编辑器、文本编辑器。

2018-07-30 版本2.7 图像的变形：旋转、斜拉、镜像。

2018-07-26 版本2.6 增强图像的换色：可以选择多个原色，可以按色彩距离或者色相距离来匹配。支持透明度处理。

2018-07-25 版本2.5 调色盘。图像的换色：可以精确匹配颜色、或者设置色距，此功能可以替换图像背景色、或者清除色彩噪声。

2018-07-24 版本2.4 完善图像处理和多图查看：平滑切换、对照图、像素调整。

2018-07-18 版本2.3 闹钟，包括时间选项和音乐选项，支持wav铃音和MP3铃音，可以在后端运行。感谢我家乖乖贡献了“喵”。

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
支持批量转换和批量提取。感谢 “https://shuge.org/” 的帮助：书格提出提取PDF中图片的需求。

2018-06-21 版本1.4 读写图像的元数据,目前支持图像格式：png, jpg, bmp, tif。
感谢 “https://shuge.org/” 的帮助：书格提出图像元数据读写的需求。

2018-06-15 版本1.3 修正OTSU算法的灰度计算；优化代码：提取共享部件；支持PDF密码；使界面操作更友好。

2018-06-14 版本1.2 针对黑白色添加色彩转换的选项；自动保存用户的选择；优化帮助文件的读取。
感谢 “https://shuge.org/” 的帮助：书格提出二值化转换阈值的需求。

2018-06-13 版本1.1 添加：转换格式tiff和raw，压缩和质量选项，以及帮助信息。
感谢 “https://shuge.org/” 的帮助：书格提出tiff转换的需求。

2018-06-12 版本1.0 实现功能：将PDF文件的每页转换为一张图片，包含图像密度、类型、格式等选项，并且可以暂停/继续转换过程。
```

## 实现基础
MyBox使用NetBeans开发：

[https://netbeans.org/](https://netbeans.org/)


基于以下开源软件/开源库：

[JavaFx  https://docs.oracle.com/javafx/2/](https://docs.oracle.com/javafx/2/)
	
[PDFBox  https://pdfbox.apache.org/](https://pdfbox.apache.org/)
	
[jai-imageio  https://github.com/jai-imageio/jai-imageio-core](https://github.com/jai-imageio/jai-imageio-core)
	
[javazoom  http://www.javazoom.net/index.shtml](http://www.javazoom.net/index.shtml)
	
[log4j   https://logging.apache.org/log4j/2.x/](https://logging.apache.org/log4j/2.x/)
	

## 主界面
![About](https://mararsh.github.io/MyBox/0.png)


