## English Interface

[English Interface](https://mararsh.github.io/MyBox/english_interface.html)

## MyBox：简易工具集

这是利用JavaFx开发的桌面程序，目标是提供简单易用的功能。

当前是版本1.5，已实现功能：
1) 将PDF文件的每页转换为一张图片，包含图像密度、色彩、格式、压缩、质量、色彩转换等选项，并且可以暂停/继续转换过程。
2) 将PDF文件中的图片提取出来保存为原格式。
3) 查看图片的元数据和属性。目前支持图像格式：png, jpg, bmp, tif。
4) 支持批量转换和提取。

编译好的jar包：[MyBox-1.5.jar](https://mararsh.github.io/MyBox/MyBox-1.5.jar) 

在已安装JRE或者JDK的环境下执行以下命令来启动程序：
<PRE><CODE>     java   -jar   MyBox-1.5.jar</CODE></PRE>

## 开发日志

2018-06-24 版本1.5  提取PDF中的图片保存为原格式。支持批量转换和批量提取。

2018-06-21 版本1.4  读写图像的元数据,目前支持图像格式：png, jpg, bmp, tif。感谢 “https://shuge.org/” 的帮助：书格提出图像元数据读写的需求。

2018-06-15 版本1.3  修正OTSU算法的灰度计算；优化代码：提取共享部件；支持PDF密码；使界面操作更友好。

2018-06-14 版本1.2  针对黑白色添加色彩转换的选项；自动保存用户的选择；优化帮助文件的读取。感谢 “https://shuge.org/” 的帮助：书格提出二值化转换阈值的需求。

2018-06-13 版本1.1  添加：转换格式tiff和raw，压缩和质量选项，以及帮助信息。感谢 “https://shuge.org/” 的帮助：书格提出tiff转换的需求。

2018-06-12 版本1.0  实现功能：将PDF文件的每页转换为一张图片， 包含图像密度、类型、格式等选项，并且可以暂停/继续转换过程。


## 应用截图


![1](https://mararsh.github.io/MyBox/1.png)



![7](https://mararsh.github.io/MyBox/2.png)



![8](https://mararsh.github.io/MyBox/3.png)



![2](https://mararsh.github.io/MyBox/4.png)



![3](https://mararsh.github.io/MyBox/5.png)



![4](https://mararsh.github.io/MyBox/6.png)



![5](https://mararsh.github.io/MyBox/7.png)



![6](https://mararsh.github.io/MyBox/8.png)



![9](https://mararsh.github.io/MyBox/9.png)

