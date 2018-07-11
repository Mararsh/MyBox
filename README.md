## [English Interface](https://mararsh.github.io/MyBox/english_interface.html)

## MyBox：简易工具集

![About](https://mararsh.github.io/MyBox/7.png)

这是利用JavaFx开发的图形化界面程序，目标是提供简单易用的功能。免费开源。

每个版本编译好的包已发布在Release目录下（点击上面的releases页签）

可以下载exe包，无需java环境、无需安装、解包可用： [MyBox-2.2-exe.zip](https://github.com/Mararsh/MyBox/releases/download/v2.2/MyBox-2.2-exe.zip) 

在已安装JRE或者JDK的环境下，可以下载jar包 [MyBox-2.2-jar.zip](https://github.com/Mararsh/MyBox/releases/download/v2.2/MyBox-2.2-jar.zip) ，执行以下命令来启动程序：
<PRE><CODE>     java   -jar   MyBox-版本号.jar</CODE></PRE>

当前是版本2.2，已实现的特点：
1. 随时切换中英文界面 
2. 将PDF文件的每页转换为一张图片，包含图像密度、色彩、格式、压缩、质量、色彩转换等选项
3. 将PDF文件中的图片提取出来保存为原格式。
4. 将PDF文件中的文字提取出来，可以定制页的分割行。
5. 将图片转换为其它格式，包含色彩、长宽、压缩、质量等选项。
6. 以上功能支持批量处理， 可以暂停/继续处理过程。
7. 支持图像格式：png, jpg, bmp, tif, gif, wbmp, pnm, pcx。
8. 处理图片：参数化调整饱和度、明暗、色相；提供滤镜：灰色、反色、黑白色。支持导览。
9. 查看图片的元数据和属性。
10. 同屏查看最多十张图，可以分别或者同步旋转和缩放。
11. 像素计算器。
12. 目录文件重命名，包含文件名和排序的选项。被重命名的文件可以全部恢复或者指定恢复原来的名字。
13. 目录同步，包含复制子目录、新文件、特定时间以后已修改文件、原文件属性，以及删除源目录不存在文件和目录，等选项
14. 整理文件，将文件按修改时间或者生成时间重新归类在新目录下。此功能可用于处理照片、游戏截图、和系统日志等需要按时间归档的批量文件。


## 开发日志

2018-07-11 版本2.2  修正线程处理逻辑的漏洞。整理文件，将文件按修改时间或者生成时间重新归类在新目录下。此功能可用于处理照片、游戏截图、和系统日志等需要按时间归档的批量文件。

2018-07-09 版本2.1  完善图片处理的界面，支持导览。目录同步，包含复制子目录、新文件、特定时间以后已修改文件、原文件属性，以及删除源目录不存在文件和目录，等选项

2018-07-06 版本2.0  批量提取PDF文字、批量转换图片。目录文件重命名，包含文件名和排序的选项，被重命名的文件可以全部恢复或者指定恢复原来的名字。

2018-07-03 版本1.9  修正问题。提取PDF文字时可以定制页分割行。完善图像处理：参数化调整饱和度、明暗、色相；滤镜：灰色、反色、黑白色。

2018-07-01 版本1.8  将PDF文件中的文字提取出来。处理图片：调整饱和度、明暗，或者转换为灰色、反色。

2018-06-30 版本1.7  完善像素计算器。支持同屏查看最多十张图，可以分别或者同步旋转和缩放。

2018-06-27 版本1.6  将图片转换为其它格式，支持色彩、长宽、压缩、质量等选项。提供像素计算器。新增图像格式：gif, wbmp, pnm, pcx。

2018-06-24 版本1.5  提取PDF中的图片保存为原格式。支持批量转换和批量提取。感谢 “https://shuge.org/” 的帮助：书格提出提取PDF中图片的需求。

2018-06-21 版本1.4  读写图像的元数据,目前支持图像格式：png, jpg, bmp, tif。感谢 “https://shuge.org/” 的帮助：书格提出图像元数据读写的需求。

2018-06-15 版本1.3  修正OTSU算法的灰度计算；优化代码：提取共享部件；支持PDF密码；使界面操作更友好。

2018-06-14 版本1.2  针对黑白色添加色彩转换的选项；自动保存用户的选择；优化帮助文件的读取。感谢 “https://shuge.org/” 的帮助：书格提出二值化转换阈值的需求。

2018-06-13 版本1.1  添加：转换格式tiff和raw，压缩和质量选项，以及帮助信息。感谢 “https://shuge.org/” 的帮助：书格提出tiff转换的需求。

2018-06-12 版本1.0  实现功能：将PDF文件的每页转换为一张图片， 包含图像密度、类型、格式等选项，并且可以暂停/继续转换过程。


## 应用截图

![1](https://mararsh.github.io/MyBox/1.png)



![22](https://mararsh.github.io/MyBox/22.jpg)



![23](https://mararsh.github.io/MyBox/23.jpg)



![21](https://mararsh.github.io/MyBox/21.jpg)



![16](https://mararsh.github.io/MyBox/16.jpg)



![17](https://mararsh.github.io/MyBox/17.jpg)



![19](https://mararsh.github.io/MyBox/19.jpg)



![20](https://mararsh.github.io/MyBox/20.jpg)



![15](https://mararsh.github.io/MyBox/15.png)



![18](https://mararsh.github.io/MyBox/18.jpg)



![13](https://mararsh.github.io/MyBox/13.jpg)



![14](https://mararsh.github.io/MyBox/14.jpg)



![11](https://mararsh.github.io/MyBox/11.png)



![12](https://mararsh.github.io/MyBox/12.png)



![7](https://mararsh.github.io/MyBox/10.png)



![7](https://mararsh.github.io/MyBox/2.png)



![8](https://mararsh.github.io/MyBox/3.png)



![2](https://mararsh.github.io/MyBox/4.png)



![3](https://mararsh.github.io/MyBox/5.png)



![4](https://mararsh.github.io/MyBox/6.png)



![6](https://mararsh.github.io/MyBox/8.png)



![9](https://mararsh.github.io/MyBox/9.png)

