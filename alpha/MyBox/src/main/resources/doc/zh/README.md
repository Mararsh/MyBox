# [ReadMe in English](https://github.com/Mararsh/MyBox/tree/master/en)  ![ReadMe](https://mara-mybox.sourceforge.io/iconGo.png)

# MyBox：简易工具集
这是利用JavaFx开发的图形化桌面应用，目标是提供简单易用的功能。免费开源。          

## 最新版本       
本文档的内容可能已过期，但是其中的链接均指向最新版本。     

以下网址可以下载到最新版本和已归档的所有版本：    

| 地址 | 下载 | 自述 | 
| --- | --- | --- | 
| github | [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  | [英文](https://mararsh.github.io/MyBox/readme-en.html)   [中文](https://mararsh.github.io/MyBox/readme-zh.html) |
| sourceforge | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)  | [英文](https://mara-mybox.sourceforge.io/readme-en.html)  [中文](https://mara-mybox.sourceforge.io/readme-zh.html) |
| 云盘 | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)   |  |


## 新内容
2023-3-13 版本6.7.1                
                                
* 新增：                  
     - html编辑模式：DOM。                                                             
     - 图片处理：对于选择的范围进行颜色混合。                                                             
     - 批量设置网页的http-equiv。                                                             
     - 可以定制按钮的颜色。                                                                                                                                                
* 改进：                                
     - 图片处理：替换颜色时可选是否替换色相/饱和度/明亮度。                                                             
     - 颜色的新属性：RYB角度、RGB反色、和RYB补色。更多的调色盘示例。                                                             
     - 可设置选择框的滚动尺寸。                                                             
     - 网页加载完毕之前就能使用链接的菜单。                                                             
     - 用jsoup解析html。                                                                                   
     - 可选择在悬停按钮时是否弹出菜单/窗口。                                                                                   
     - 自述文件中链接指向最新版本。                                                                                
     - 在打包脚本中设置JAVA_HOME。                                                                                
* 移除：                                
     - 不再支持更改界面风格。                                                             
     - 在一些界面上移除鸡肋的滑动条。                                                             
     - 程序包名不再包含版本号。                                                                                                                                              
* Java和JavaFx均升级到v19。                                                             
* 解决问题：                                     
     - 添加文件备份时可能自动清除所有文件备份。                                                             
     - 当标签图片不存在时加载地图会失败。                                                             
     - 将网址加为收藏时会失败。                                                             
     - 一些界面上访问目录的历史未生效。                                                             
     - 批量粘贴图片时，对于粘贴位置的处理不准确。                                                             
     - 编辑图片颜色时快捷键未生效。                                                                                                             
     - Javascript的编辑历史没有保存。                                                                                                             
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.7.1)             
             
   
## 下载与运行
每个版本编译好的包已发布在[Releases](https://github.com/Mararsh/MyBox/releases)目录下。      

### 源码
| 源码 | 开发指南 | 打包步骤 |           
| --- | --- | --- | 
| [MyBox-src.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-src.zip)   65M-  | [pdf](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.pdf) | [html](https://mara-mybox.sourceforge.io/pack_steps.html) |
           
### 自包含程序包
自包含的程序包无需java环境、无需安装、解包可用。     

| 平台 | 链接 | 大小 | 启动文件 |
| --- | --- | --- |  --- |
| win10 x64 | [MyBox-win10-x64.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win10-x64.zip)  | 260MB- | MyBox.exe |
| CentOS 7 x64 | [MyBox-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-CentOS7-x64.tar.gz)  | 280MB-  | MyBox  |
| mac x64| [MyBox-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.dmg)  | 280MB-  |  MyBox.app   |

双击或者用命令行执行包内的启动文件即可运行程序。可以把图片/文本/PDF文件的打开方式关联到MyBox，这样双击文件名就直接是用MyBox打开了。        
  

### Jar包
在已安装JRE或者JDK [Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html)或[open jdk](http://jdk.java.net/)均可）的环境下，可以下载jar包。       

| 平台 | 链接 | 大小 | 运行需要 |
| --- | --- | --- |  --- |
| win | [MyBox-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win-jar.zip)  | 190MB- | Java 19或更高版本 |
| linux | [MyBox-linux.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-linux.jar.gz)  | 200MB-  | Java 19或更高版本 |
| mac | [MyBox-mac.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.jar.gz)  |  200MB-  | Java 19或更高版本 |

执行以下命令来启动程序：       
<PRE><CODE>     java   -jar   MyBox.jar</CODE></PRE>       
程序可以跟一个文件名作为参数、以用MyBox直接打开此文件。例如以下命令是打开此图片：       
<PRE><CODE>     java   -jar   MyBox.jar   /tmp/a1.jpg</CODE></PRE>       


### 限制
  
* MyBox未经足够测试，可靠性低，可能出现毁坏数据的错误。       
* MyBox持续新增功能同时改进代码，稳定性差，可能发生非预期的功能失效。       
* 在某个输入法运行时，MyBox的窗口经常僵住。解决办法：禁用/卸载此输入法。       

## 版本迁移
1. 每个版本有自己的配置文件，新版本可以复制已安装版本的参数。       
2. 每个版本处理的所有数据都在它指向的“数据目录”下。多个版本可以指向同一数据目录。
3. MyBox向后兼容：新版本可以处理旧版本的数据目录。而不保证向前兼容：旧版本处理新版本的数据目录时可能出错。

## 配置
配置文件在"用户目录"下:       

| 平台 | MyBox配置文件的目录 |
| --- | --- |
| win | `C:\用户\用户名\mybox\MyBox_v版本号.ini`  |
| linux | `/home/用户名/mybox/MyBox_v版本号.ini` |
| mac | `/Users/用户名/mybox/MyBox_v版本号.ini` |       

可以临时改变配置文件：在命令行启动jar包时设置参数"config=\"配置文件名\""。       
利用“设置”功能也可以修改配置参数。       

# 资源地址       
| 内容 | 链接 |       
| --- | --- |
| 项目主页 | [https://github.com/Mararsh/MyBox](https://github.com/Mararsh/MyBox)   |
| 源代码和编译好的包 |  [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  |
| 在线提交软件需求和问题报告 | [https://github.com/Mararsh/MyBox/issues](https://github.com/Mararsh/MyBox/issues) |
| 数据 | [https://github.com/Mararsh/MyBox_data](https://github.com/Mararsh/MyBox_data) |
| 文档 | [https://github.com/Mararsh/MyBoxDoc](https://github.com/Mararsh/MyBoxDoc) |
| 镜像 | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/) |
| 云盘 | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F) |       

# 文档       
|      文档名       | 版本    | 修改时间   |                                                                                                                                            英文                                                                                                                                               |                                                                                                                                            中文                                                                                                                                               |
|-------------------|---------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 开发日志          | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_devLogs.html)                                                                                                                                                                                                                                    |
| 快捷键            | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts.html)                                                                                                                                                                                                                                  |
| 功能列表          | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_functions_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_functions.html)                                                                                                                                                                                                                                  |
| 打包步骤          | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mara-mybox.sourceforge.io/pack_steps.html)                                                                                                                                                                                                                                       |
| 开发指南          | 2.1     | 2020-8-27  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.odt)                                                                                                                                                                                  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.pdf)  [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.odt)                                                                                                                                                                                                                |
| 用户手册-综述     | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-Overview-en/MyBox-Overview-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-Overview-zh/MyBox-Overview-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.odt)                     |
| 用户手册-数据工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DataTools-en/MyBox-DataTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DataTools-zh/MyBox-DataTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.odt)                 |
| 用户手册-文档工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DocumentTools-en/MyBox-DocumentTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.odt) | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DocumentTools-zh/MyBox-DocumentTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.odt) |
| 用户手册-图像工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-ImageTools-en/MyBox-ImageTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-ImageTools-zh/MyBox-ImageTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.odt)             |
| 用户手册-文件工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-FileTools-en/MyBox-FileTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-FileTools-zh/MyBox-FileTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.odt)                 |
| 用户手册-网络工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-NetworkTools-en/MyBox-NetworkTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.odt)     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-NetworkTools-zh/MyBox-NetworkTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.odt)     |
| 用户手册-媒体工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-MediaTools-en/MyBox-MediaTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-MediaTools-zh/MyBox-MediaTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.odt)             |
| 用户手册-开发工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DevTools-en/MyBox-DevTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DevTools-zh/MyBox-DevTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.odt)                     |
| 示例-笔记         | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/mybox_examples_notes_en.html)                                                                                                                                                                                                                          | [html](https://mara-mybox.sourceforge.io/mybox_examples_notes.html)                                                                                                                                                                                                                                    |
| 示例-树形         | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_tree_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_tree.html)                                                                                                                                                                                                                                    |
| 示例-收藏的网址   | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_web_favorite_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_web_favorite.html)                                                                                                                                                                                                                                    |
| 示例-SQL          | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_sql_en.html)                                                                                                                                                                                                                            | [html](https://mara-mybox.sourceforge.io/mybox_examples_sql.html)                                                                                                                                                                                                                                    |
| 示例-JShell       | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_jshell_en.html)                                                                                                                                                                                                                         | [html](https://mara-mybox.sourceforge.io/mybox_examples_jshell.html)                                                                                                                                                                                                                                    |
| 示例-JEXL         | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_jexl_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_jexl.html)                                                                                                                                                                                                                                    |
| 示例-JavaScript   | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_javascript_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_javascript.html)                                                                                                                                                                                                                                    |
| 示例-数学函数     | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_math_funtion_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_math_funtion.html)                                                                                                                                                                                                                                    |
| 示例-行过滤       | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_examples_row_filter_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_row_filter.html)                                                                                                                                                                                                                                    |
| 关于-数据分组     | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping.html)                                                                                                                                                                                                                                    |
| 关于-颜色         | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_color_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_color.html)                                                                                                                                                                                                                                    |
| 关于-坐标系统     | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system_en.html)                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system.html)                                                                                                                                                                                                                                    |
| 关于-媒体         | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_media_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_media.html)                                                                                                                                                                                                                                    |
| 关于-数据分析     | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis.html)                                                                                                                                                                                                                                    |
| 关于-Java编程的一句话事项     | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/mybox_about_java_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_java.html)                                                                                                                                                                                                                                    |
| 关于-JavaFx编程的一句话事项   | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx.html)                                                                                                                                                                                                                                    |
| 调色盘-常用网页颜色           | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all.html)                                                                                                                                                                                                                                   |
| 调色盘-中国传统颜色           | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all_en.html)                                                                                                                          | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese.html) [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all.html)                                                                                                                                                                                                                                    |
| 调色盘-日本传统颜色           | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all_en.html)                                                                                                                        | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all.html)                                                                                                                                                                                                                                  |
| 调色盘-来自colorhexa.com颜色  | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all_en.html)                                                                                                                      | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all.html)                                                                                                                                                                                                                                   |
| 调色盘-美术色相环(RYB)12色    | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all.html)                                                                                                                                                                                                                                   |
| 调色盘-美术色相环(RYB)24色    | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all.html)                                                                                                                                                                                                                                   |
| 调色盘-美术色相环(RYB)360色   | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all.html)                                                                                                                                                                                                                                   |
| 调色盘-光学色相环(RGB)12色    | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all.html)                                                                                                                                                                                                                                   |
| 调色盘-光学色相环(RGB)24色    | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all.html)                                                                                                                                                                                                                                   |
| 调色盘-光学色相环(RGB)360色   | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all.html)                                                                                                                                                                                                                                   |
| 调色盘-美术颜料               | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all.html)                                                                                                                                                                                                                                   |
| 调色盘-MyBox的颜色            | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all_en.html)                                                                                                                              | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all.html)                                                                                                                                                                                                                                   |
| 调色盘-灰阶                   | 6.7.1   | 2023-3-13 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all.html)                                                                                                                                                                                                                                   |
| 图片的故事                    | 6.7.1   | 2023-3-13 | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-StoriesOfImages-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-StoriesOfImages-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-zh.odt)                     |   

# 实现基础       
MyBox基于以下开放资源：       

| 名字 | 角色 | 链接 |
| --- | --- | --- |
| JDK | Java语言 |  [http://jdk.java.net/](http://jdk.java.net/)   |
|   |   | [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)  |
|   |   | [https://docs.oracle.com/en/java/javase/19/docs/api/index.html](https://docs.oracle.com/en/java/javase/19/docs/api/index.html)  |
| JavaFx | 图形化界面 | [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)  |
|   |   |  [https://docs.oracle.com/javafx/2/](https://docs.oracle.com/javafx/2/)  |
|   |   |  [https://gluonhq.com/products/scene-builder/](https://gluonhq.com/products/scene-builder/) |
|   |   |  [https://openjfx.io/javadoc/19/](https://openjfx.io/javadoc/19/) |
| Derby | 数据库 | [http://db.apache.org/derby/](http://db.apache.org/derby/) |
| NetBeans | 集成开发环境 | [https://netbeans.org/](https://netbeans.org/) |
| jpackage | 自包含包 | [https://docs.oracle.com/en/java/javase/19/docs/specs/man/jpackage.html](https://docs.oracle.com/en/java/javase/19/docs/specs/man/jpackage.html) |
| maven | 代码构建 | [https://maven.apache.org/](https://maven.apache.org/) |
| jai-imageio | 图像处理 | [https://github.com/jai-imageio/jai-imageio-core](https://github.com/jai-imageio/jai-imageio-core) |
| PDFBox | PDF处理 | [https://pdfbox.apache.org/](https://pdfbox.apache.org/) |
| PDF2DOM | PDF转html | [http://cssbox.sourceforge.net/pdf2dom/](http://cssbox.sourceforge.net/pdf2dom/) |
| GifDecoder | 不规范Gif | [https://github.com/DhyanB/Open-Imaging/](https://github.com/DhyanB/Open-Imaging/) |
| EncodingDetect | 文本编码 | [https://www.cnblogs.com/ChurchYim/p/8427373.html](https://www.cnblogs.com/ChurchYim/p/8427373.html) |
| Free Icons | 图标 | [https://icons8.com/icons/set/home](https://icons8.com/icons/set/home) |
| Lindbloom | 色彩理论 | [http://brucelindbloom.com/index.html](http://brucelindbloom.com/index.html) |
| tess4j | OCR | [https://github.com/nguyenq/tess4j](https://github.com/nguyenq/tess4j) |
| tesseract | OCR | [https://github.com/tesseract-ocr/tesseract](https://github.com/tesseract-ocr/tesseract) |
| barcode4j | 生成条码 | [http://barcode4j.sourceforge.net](http://barcode4j.sourceforge.net) |
| zxing | 生成/解码条码 | [https://github.com/zxing/zxing](https://github.com/zxing/zxing) |
| flexmark-java | 转换Markdown | [https://github.com/vsch/flexmark-java](https://github.com/vsch/flexmark-java) |
| commons-compress | 归档/压缩 | [https://commons.apache.org/proper/commons-compress](https://commons.apache.org/proper/commons-compress) |
| XZ for Java | 归档/压缩 | [https://tukaani.org/xz/java.html](https://tukaani.org/xz/java.html) |
| ffmpeg| 媒体转换/生成 | [http://ffmpeg.org](http://ffmpeg.org) |
| image4j | ico格式 | [https://github.com/imcdonagh/image4j](https://github.com/imcdonagh/image4j) |
| AutoCommitCell | 提交修改 | [https://stackoverflow.com/questions/24694616 （Ogmios）](https://stackoverflow.com/questions/24694616) |
| 高德 | 地图 | [https://lbs.amap.com/api/javascript-api/summary](https://lbs.amap.com/api/javascript-api/summary) |
| 高德 | 坐标 | [https://lbs.amap.com/api/webservice/guide/api/georegeo](https://lbs.amap.com/api/webservice/guide/api/georegeo) |
| 微博 | 图片素材 | [https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665](https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665) |
| poi | 微软文档 | [https://poi.apache.org](https://poi.apache.org) |
| LabeledBarChart | JavaFx图 | [https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland)](https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland)) |
| commons-csv | CSV | [https://commons.apache.org/proper/commons-csv/](https://commons.apache.org/proper/commons-csv/) |
| geonames | 位置数据 | [https://www.geonames.org/countries/](https://www.geonames.org/countries/) |
| world-area | 位置数据 | [https://github.com/wizardcode/world-area](https://github.com/wizardcode/world-area) |
| 中国国家统计局 | 数据 | [http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/](http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/) |
| JHU | COVID-19数据 | [https://github.com/CSSEGISandData/COVID-19](https://github.com/CSSEGISandData/COVID-19) |
| 懒人图库 | 色彩数据 | [https://tool.lanrentuku.com/color/china.html](https://tool.lanrentuku.com/color/china.html) |
| 中国纹样全集 | 素材 | [https://book.douban.com/subject/3894923/](https://book.douban.com/subject/3894923/) |
| 中国国家基础地理信息中心 | 地图 | [http://lbs.tianditu.gov.cn/api/js4.0/guide.html](http://lbs.tianditu.gov.cn/api/js4.0/guide.html) |
| movebank | 位置数据 | [https://www.datarepository.movebank.org](https://www.datarepository.movebank.org) |
| CoordinateConverter | 坐标转换 | [https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage](https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage) |
| JavaMail | email | [https://javaee.github.io/javamail/](https://javaee.github.io/javamail/) |
| Commons IO | 文件读写 | [https://commons.apache.org/proper/commons-io/](https://commons.apache.org/proper/commons-io/) |
| colorhexa | 色彩数据 | [https://www.colorhexa.com/color-names](https://www.colorhexa.com/color-names) |
| 文泉驿 | 开源字体 | [http://wenq.org/wqy2/](http://wenq.org/wqy2/) |
| ttc2ttf | 提取ttf | [https://github.com/fermi1981/TTC_TTF](https://github.com/fermi1981/TTC_TTF) |
| 中国出土壁画全集 | 素材 | [https://book.douban.com/subject/10465940/](https://book.douban.com/subject/10465940/) |
| sfds | 书法 | [http://www.sfds.cn/725B/](http://www.sfds.cn/725B/) |
| PaginatedPdfTable | PDF | [https://github.com/eduardohl/Paginated-PDFBox-Table-Sample](https://github.com/eduardohl/Paginated-PDFBox-Table-Sample) |
| jsoup | DOM | [https://jsoup.org/](https://jsoup.org/) |       
| 知乎 | 素材 | [https://www.zhihu.com/question/41580677/answer/1300242801](https://www.zhihu.com/question/41580677/answer/1300242801) |
| commons-math | 计算 | [https://commons.apache.org/proper/commons-math/index.html](https://commons.apache.org/proper/commons-math/index.html) |
| JEXL | 计算 | [https://commons.apache.org/proper/commons-jexl](https://commons.apache.org/proper/commons-jexl) |
| OpenOffice | 文档 | [http://www.openoffice.org/](http://www.openoffice.org/) |
| nashorn | JavaScript | [https://openjdk.org/projects/nashorn/](https://openjdk.org/projects/nashorn/) |
| echarts-gl | WebGL | [https://github.com/ecomfe/echarts-gl](https://github.com/ecomfe/echarts-gl) |
| RYB色相 | 美术色彩 | [https://blog.csdn.net/weixin_44938037/article/details/90599711](https://blog.csdn.net/weixin_44938037/article/details/90599711) |
| jsch | sftp | [http://www.jcraft.com/jsch/](http://www.jcraft.com/jsch/) |


# 特点
## 跨平台       

MyBox用纯Java实现且只基于开放资源，MyBox可运行于支持Java 19的平台。       
MyBox v5.3以前的版本均基于Java 8。       

## 国际化        

1. 所有代码均国际化。可实时切换语言。
2. 每种语言对应一个资源文件："Messages_语言名.properties"。
3. 支持在线添加语言。提供表格，对照英语翻译。新语言可实时生效。       
   例如，新语言名字为“aa”，则它的资源文件是“Messages_aa.properties”。       
4. 新语言可共享给别人：把资源文件复制到数据目录的子目录"mybox_languages"下，则MyBox可即时感知到新语言。       
5. 内置中文和英文， 在目录`MyBox/src/main/resources/bundles/`中：       

| 语言 | 界面的资源文件 |      
| --- | --- | 
| 中文 | Messages_zh_CN.properties | 
| 英文 | Messages_en.properties |  

## 个人的            

1. 所有功能在本机执行。 
2. 所有数据在本机存储。
3. 无注册/登录/数据中心/云存储。
3. 只在用户要求时才访问网络。
4. 只读写用户需要的数据。       

## 友善的          

1. 提供当前功能/界面的提示。       
2. 记住用户的输入或选择，提供历史记录。
3. 界面可调整，一些面板可隐藏。
4. 一些控件的行为可选择。
5. 提供文本/图片/网页/数据的右键菜单和功能菜单。
6. 可弹出当前文本/图片/网页/数据。
7. 可管理的文本/图片/数据粘贴板。
8. 提供示例和有用的信息。  
9. 界面和控件的外观可选择和修改。      

## 数据兼容            

1. 导出的数据是通用的文本格式，如txt/csv/xml/json/html。
2. 导入的数据是通用的文本格式，如txt/csv。       
3. 至少有一种导出格式可以被导入。
4. 导入的数据是自包含的，即重建原数据无需辅助数据。       

## 内置数据库系统            

1. 数据库系统derby免费开源：
    - 首次启动MyBox时，需要消耗一些时间以创建内部数据库并写入初始数据。        
    - 所有数据库文件均在数据目录下，因此可以通过切换数据目录来处理不同的数据目标集。         
    - 通过访问同一数据目录，多个MyBox实例可以共享数据。         
    - 当数据目录下未发现数据库文件（如文件损毁或意外删除），MyBox会自动创建数据库。         
2. Derby缺省以内置模式运行：         
    - 外部进程无法访问数据库。         
    - 同时只能有一个MyBox实例访问数据库。         
3. Derby还可以网络运行：         
    - 首个访问数据库的MyBox实例成为服务器。         
    - 本机进程可通过端口1527来访问数据库、即为客户端。                
    - 非本机进程不能直接访问数据库。                
4. MyBox内部数据表存放各个功能所需的数据。 用户可以在线访问和修改内部数据。               
5. 用户可以自定义数据库表。         
6. 用户可以通过分页表格的来查看和编辑数据库表的定义和数据，也可以执行SQL语句。                
7. 为了实现数据的排序、转置、统计、和分组，MyBox会生成临时数据库表、并负责及时清除它们。                
    

## 辅助编码            

1. 实时监测内存和CPU占用量。
2. 在线调整和查询日志。       
3. 自动生成图标。
4. 在线编辑和应用语言。
5. 自动测试。
6. 各种文档。       


# 截图
      
![截屏-封面](https://mara-mybox.sourceforge.io/snap-cover.jpg)       

![截屏-界面](https://mara-mybox.sourceforge.io/snap-interface.jpg)       

![截屏-表格](https://mara-mybox.sourceforge.io/snap-table.jpg)       
       