# [ReadMe in English](https://github.com/Mararsh/MyBox/tree/master/en)  ![ReadMe](https://mara-mybox.sourceforge.io/iconGo.png)

# MyBox：简易工具集
这是图形化计算机应用，目标是提供简单易用的功能。免费开源。           

## 新内容
2024-1-27 版本6.8.3                
                             
* 新增：                                     
     - 读写webp图片。                                     
     - 数据列类型：枚举短整型，显示为字符串，保存为短整型。                                     
* 改进：                                     
     - 把“地理编码”改成数据树。                                                                                                            
     - 数据处理：                                                                                                          
         - 移除数据列的“对非法值的处理”。                                                                                                               
                                    
* 解决问题：                                     
     - 对数据库表的所有数据执行“赋值”可能会写错数据。                                 
     - 文件另存时选择的目录失效。                                 
     - 从低版本升级到6.8.1或6.8.2后，[“数据处理”不能正确加载已存在的数据](https://github.com/Mararsh/MyBox/issues/1979) 。                                 
                                                                                                           
                                                                                                                      
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.8.3)                         
          
## 注意       
MyBox未经足够测试，可靠性低、稳定性差。使用MyBox，可能出现以下情况：      
 
* 毁坏文件/数据。       
* 输出错误的结果。       
* 功能失效。       
* 其它非预期的行为。         

           
# 下载与运行

## 源码
| 下载 | 大小 | 开发指南 | 打包步骤 |           
| --- | --- | --- | --- | 
| [MyBox-src.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-src.zip)   |  80M-  | [pdf](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.pdf) | [html](https://mara-mybox.sourceforge.io/pack_steps_zh.html) |
           
## 自包含程序包
自包含的程序包无需java环境、无需安装、解包可用。     

| 平台 | 链接 | 大小 | 启动文件 |
| --- | --- | --- |  --- |
| win10 x64 | [MyBox-win10-x64.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win10-x64.zip)  | 280MB- | MyBox.exe |
| CentOS 7 x64 | [MyBox-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-CentOS7-x64.tar.gz)  | 320MB-  | MyBox  |
| mac x64| [MyBox-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.dmg)  | 300MB-  |  MyBox.app   |

双击或者用命令行执行包内的启动文件即可运行程序。可以把图片/文本/PDF文件的打开方式关联到MyBox，这样双击文件名就直接是用MyBox打开了。        
  

## Jar包
在已安装JRE或者JDK [Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html)或[open jdk](http://jdk.java.net/)均可）的环境下，可以下载jar包。       

注意：由于编译错误，Linux平台仍为Java21 + JavaFX 21。                

| 平台 | 链接 | 大小 | 运行需要 |
| --- | --- | --- |  --- |
| win | [MyBox-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-win-jar.zip)  | 240MB- | Java 23或更高版本 |
| linux | [MyBox-linux.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-linux.jar.gz)  | 240MB-  | Java 21 |
| mac | [MyBox-mac.jar.gz](https://sourceforge.net/projects/mara-mybox/files/latests/MyBox-mac.jar.gz)  |  240MB-  | Java 23或更高版本 |

执行以下命令来启动程序：       
`    java   -jar   MyBox.jar `     

程序可以跟一个文件名作为参数、以用MyBox直接打开此文件。例如以下命令是打开此图片：       
`     java   -jar   MyBox.jar   /tmp/a1.jpg`      

     
## 版本       
本文档的内容可能已过期，但是其中的链接均指向最新版本。     

以下网址可以下载到最新版本和已归档的版本：    

| 地址 | 下载 | 自述 | 
| --- | --- | --- | 
| github | [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  | [英文](https://mararsh.github.io/MyBox/readme_en.html)   [中文](https://mararsh.github.io/MyBox/readme_zh.html) |
| sourceforge | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)  | [英文](https://mara-mybox.sourceforge.io/readme_en.html)  [中文](https://mara-mybox.sourceforge.io/readme_zh.html) |
| 云盘 | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)   |  |     

## 配置
配置文件在"用户目录"下:       

| 平台 | MyBox配置文件的目录 |
| --- | --- |
| win | `C:\用户\用户名\mybox\MyBox_v版本号.ini`  |
| linux | `/home/用户名/mybox/MyBox_v版本号.ini` |
| mac | `/Users/用户名/mybox/MyBox_v版本号.ini` |       

可以临时改变配置文件：在命令行启动jar包时设置参数"config=\"配置文件名\""。       
利用“设置”功能也可以修改配置参数。       

## 迁移
1. 每个版本有自己的配置文件，新版本可以复制已安装版本的参数。       
2. 每个版本处理的所有数据都在它指向的“数据目录”下。多个版本可以指向同一数据目录。
3. MyBox向后兼容：新版本可以处理旧版本的数据目录。而不保证向前兼容：旧版本处理新版本的数据目录时可能出错。    

## 备份与恢复        
在MyBox中：       
1. 将目录复制到别处，即为备份。       
2. 将备份的目录覆盖当前目录，即为恢复。        
3. 不必备份数据目录的每个子目录。    
   以下列出数据目录中预定义的子目录：      

|       子目录       |     作用     | 内部引用 | 自动读写 | 需要备份 | 说明 |
|-----------------|------------|------|------|--------|------|
| AppTemp         | 临时文件       | 是    | 是    |        | 自动清除    |
| buttons         | 用户定制的按钮    | 是    | 是    |        |      |
| data            | 内部数据       | 是    | 是    |        |      |
| dataClipboard   | 数据粘贴板      | 是    | 是    | 是      |      |
| doc             | 内部文档       | 是    | 是    |        |      |
| downloads       | 下载的文件      |      | 是    |        |      |
| fileBackups     | 备份的文件      | 是    | 是    | 是      |      |
| generated       | 生成的文件      |      | 是    |  用户决定   |     |
| ICC             | 色彩特性文件     | 是    | 是    |        |      |
| icons           | 图标         | 是    | 是    |        |      |
| image           | 图片         | 是    | 是    |        |      |
| imageClipboard  | 图片粘贴板      | 是    | 是    | 是      |      |
| imageHistories  | 图片编辑历史     | 是    | 是    | 是      |      |
| imageScopes     | 图片的范围      | 是    | 是    | 是      |      |
| js              | javascript | 是    | 是    |        |      |
| logs            | 数据库日志      |      | 是    |        |      |
| map             | 地图相关的文件    | 是    | 是    |        |      |
| mybox_derby     | 数据库        | 是    | 是    | 是      |      |
| mybox_languages | 用户定制的语言    | 是    | 是    |  是      |      |
| security        | 安全证书       | 是    | 是    |        |      |
| sound           | 声音文件       | 是    | 是    |        |      |            

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
| 开发日志          | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_devLogs_zh.html)                                                                                                                                                                                                                                    |
| 快捷键与图标      | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_shortcuts_zh.html)                                                                                                                                                                                                                                  |
| 功能列表          | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_functions_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_functions_zh.html)                                                                                                                                                                                                                                  |
| 打包步骤          | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mara-mybox.sourceforge.io/pack_steps.html)                                                                                                                                                                                                                                       |
| 开发指南          | 2.1     | 2020-8-27  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-en.odt)                                                                                                                                                                                  | [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.pdf)  [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevGuide-zh.odt)                                                                                                                                                                                                                |
| 用户手册-综述     | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-Overview-en/MyBox-Overview-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-Overview-zh/MyBox-Overview-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-Overview-zh.odt)                     |
| 用户手册-数据工具 | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DataTools-en/MyBox-DataTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DataTools-zh/MyBox-DataTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.odt)                 |
| 用户手册-文档工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DocumentTools-en/MyBox-DocumentTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-en.odt) | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DocumentTools-zh/MyBox-DocumentTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DocumentTools-zh.odt) |
| 用户手册-图像工具 | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-ImageTools-en/MyBox-ImageTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-ImageTools-zh/MyBox-ImageTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-ImageTools-zh.odt)             |
| 用户手册-文件工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-FileTools-en/MyBox-FileTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-en.odt)                 | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-FileTools-zh/MyBox-FileTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-FileTools-zh.odt)                 |
| 用户手册-网络工具 | 6.7.2   | 2023-4-16  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-NetworkTools-en/MyBox-NetworkTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-en.odt)     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-NetworkTools-zh/MyBox-NetworkTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-NetworkTools-zh.odt)     |
| 用户手册-媒体工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-MediaTools-en/MyBox-MediaTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-en.odt)             | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-MediaTools-zh/MyBox-MediaTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-MediaTools-zh.odt)             |
| 用户手册-开发工具 | 6.7.1   | 2023-3-13  | [html](https://mara-mybox.sourceforge.io/guide/en/MyBox-DevTools-en/MyBox-DevTools-en.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/zh/MyBox-DevTools-zh/MyBox-DevTools-zh.html) [PDF](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.pdf) [odt](https://mara-mybox.sourceforge.io/guide/MyBox-DevTools-zh.odt)                     |
| 软件测试-测试环境          | 6.8.2   | 2024-12-24  | [html](https://mara-mybox.sourceforge.io/mybox_TestEnvironment_en.html)                                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_TestEnvironment_zh.html)                                                                                                                                                                                                                                    |
| 软件测试-基本功能验证列表  | 6.8.2   | 2024-12-24  | [html](https://mara-mybox.sourceforge.io/mybox_BaseVerificationList_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_BaseVerificationList_zh.html)                                                                                                                                                                                                                                  |
| 软件测试-兼容性测试        | 6.8.2   | 2024-12-24  | [html](https://mara-mybox.sourceforge.io/mybox_CompatibilityTesting_en.html)                                                                                                                                                                                                                               | [html](https://mara-mybox.sourceforge.io/mybox_CompatibilityTesting_zh.html)                                                                                                                                                                                                                                  |
| 软件测试-详细测试          | 6.8.2   | 2024-12-24  | [html](https://mara-mybox.sourceforge.io/mybox_DetailedTesting_en.html)                                                                                                                                                                                                                                    | [html](https://mara-mybox.sourceforge.io/mybox_DetailedTesting_zh.html)                                                                                                                                                                                                                                       |
| 界面中的提示信息  | 6.8.2   | 2024-12-24   | [html](https://mara-mybox.sourceforge.io/mybox_interface_tips_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_interface_tips_zh.html)                                                                                                                                                                                                                                    |
| 关于-树形信息     | 6.8.2   | 2024-12-24   | [html](https://mara-mybox.sourceforge.io/mybox_about_tree_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_tree_zh.html)                                                                                                                                                                                                                                    |
| 关于-二维存储结构的数据     | 6.8.2   | 2024-12-24    | [html](https://mara-mybox.sourceforge.io/mybox_about_data2d_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_data2d_zh.html)                                                                                                                                                                                                                                    |
| 关于-行表达式和行过滤器     | 6.8.2   | 2024-12-24   | [html](https://mara-mybox.sourceforge.io/mybox_about_row_expression_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_row_expression_zh.html)                                                                                                                                                                                                                                    |
| 关于-数据分组     | 6.7.1   | 2023-3-13   | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_grouping_zh.html)                                                                                                                                                                                                                                    |
| 关于-数据分析     | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_data_analysis_zh.html)                                                                                                                                                                                                                                    |
| 关于-坐标系统     | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system_en.html)                                                                                                                                                                                                                 | [html](https://mara-mybox.sourceforge.io/mybox_about_coordinate_system_zh.html)                                                                                                                                                                                                                                    |
| 关于-颜色         | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_color_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_color_zh.html)                                                                                                                                                                                                                                    |
| 关于-图像的范围   | 6.8     | 2024-2-9   | [html](https://mara-mybox.sourceforge.io/mybox_about_image_scope_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_image_scope_zh.html)                                                                                                                                                                                                                                    |
| 关于-媒体         | 6.6.1   | 2022-11-16 | [html](https://mara-mybox.sourceforge.io/mybox_about_media_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_about_media_zh.html)                                                                                                                                                                                                                                    |
| 关于-Java编程的一句话事项     | 6.7.3   | 2023-5-22 | [html](https://mara-mybox.sourceforge.io/mybox_about_java_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_java_zh.html)                                                                                                                                                                                                                                    |
| 关于-JavaFx编程的一句话事项   | 6.7.3   | 2023-5-22 | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_about_javafx_zh.html)                                                                                                                                                                                                                                    |
| 示例-网页树       | 6.8.2   | 2024-12-24  | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Html_en.html)                                                                                                                                                                                                                          | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Html_zh.html)                                                                                                                                                                                                                                    |
| 示例-文本树       | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Text_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Text_zh.html)                                                                                                                                                                                                                                    |
| 示例-收藏的网址   | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Web_Favorite_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Web_Favorite_zh.html)                                                                                                                                                                                                                                    |
| 示例-SQL          | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_SQL_en.html)                                                                                                                                                                                                                            | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_SQL_zh.html)                                                                                                                                                                                                                                    |
| 示例-JShell       | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JShell_en.html)                                                                                                                                                                                                                         | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JShell_zh.html)                                                                                                                                                                                                                                    |
| 示例-JEXL         | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JEXL_en.html)                                                                                                                                                                                                                           | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JEXL_zh.html)                                                                                                                                                                                                                                    |
| 示例-JavaScript   | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JavaScript_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_JavaScript_zh.html)                                                                                                                                                                                                                                    |
| 示例-数学函数     | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Math_Function_en.html)                                                                                                                                                                                                                   | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Math_Function_zh.html)                                                                                                                                                                                                                                    |
| 示例-行表达式     | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Row_Expression_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Row_Expression_zh.html)                                                                                                                                                                                                                                    |
| 示例-列定义       | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Data_Column_en.html)                                                                                                                                                                                                                     | [html](https://mara-mybox.sourceforge.io/mybox_examples_Node_Data_Column_zh.html)                                                                                                                                                                                                                                    |
| 调色盘-缺省颜色               | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_default_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_default_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_default_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_default_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-常用网页颜色           | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_web_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_web_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-中国传统颜色           | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all_en.html)                                                                                                                          | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_chinese_rgba_zh.html) [all](https://mara-mybox.sourceforge.io/mybox_palette_chinese_all_zh.html)                                                                                                                                                                                                                                    |
| 调色盘-日本传统颜色           | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all_en.html)                                                                                                                        | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_japanese_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_japanese_all_zh.html)                                                                                                                                                                                                                                  |
| 调色盘-来自colorhexa.com颜色  | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all_en.html)                                                                                                                      | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_colorhexa_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-美术色相环(RYB)12色    | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb12_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-美术色相环(RYB)24色    | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb24_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-美术色相环(RYB)360色   | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_ryb360_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-光学色相环(RGB)12色    | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb12_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-光学色相环(RGB)24色    | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb24_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-光学色相环(RGB)360色   | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_rgb360_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-美术颜料               | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_art_paints_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-MyBox的颜色            | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all_en.html)                                                                                                                              | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_mybox_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_mybox_all_zh.html)                                                                                                                                                                                                                                   |
| 调色盘-灰阶                   | 6.7.8   | 2024-1-15 | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray_rgba_en.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all_en.html)                                                                                                                                  | [rgba](https://mara-mybox.sourceforge.io/mybox_palette_gray_rgba_zh.html)  [all](https://mara-mybox.sourceforge.io/mybox_palette_gray_all_zh.html)                                                                                                                                                                                                                                   |
| 图片的故事                    | 6.8.2   | 2024-12-24 | [html](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-en.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-en.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-en.odt)                     | [html](https://mara-mybox.sourceforge.io/guide/MyBox-StoriesOfImages-zh.html) [PDF](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-zh.pdf) [odt](https://sourceforge.net/projects/mara-mybox/files/documents/latests/MyBox-StoriesOfImages-zh.odt)                     |   
| 有用的链接         | 6.7.8   | 2024-1-15 | [html](https://mara-mybox.sourceforge.io/mybox_useful_link_en.html)                                                                                                                                                                                                                             | [html](https://mara-mybox.sourceforge.io/mybox_useful_link_zh.html)                                                                                                                                                                                                                                    |

# 实现基础       
MyBox基于以下开放资源：       

| 名字 | 角色 | 链接 |
| --- | --- | --- |
| JDK | Java语言 | <http://jdk.java.net/>    |
| | | <https://www.oracle.com/technetwork/java/javase/downloads/index.html>   |
| | | <https://docs.oracle.com/en/java/javase/23/docs/api/index.html>   |
| JavaFx | 图形化界面 | <https://gluonhq.com/products/javafx/>    |
| | | <https://docs.oracle.com/javafx/2/>    |
| | | <https://gluonhq.com/products/scene-builder/>   |
| | | <https://openjfx.io/javadoc/23/>    |
| Derby | 数据库 | <http://db.apache.org/derby/>    |
| NetBeans | 集成开发环境 | <https://netbeans.org/>    |
| jpackage | 自包含包 | <https://docs.oracle.com/en/java/javase/23/docs/specs/man/jpackage.html>   |
| maven | 代码构建 | <https://maven.apache.org/>    |
| jai-imageio | 图像处理 | <https://github.com/jai-imageio/jai-imageio-core>   |
| PDFBox | PDF处理 | <https://pdfbox.apache.org/>    |
| PDF2DOM | PDF转html | <http://cssbox.sourceforge.net/pdf2dom/>    |
| GifDecoder | 不规范Gif | <https://github.com/DhyanB/Open-Imaging/>    |
| EncodingDetect | 文本编码 | <https://www.cnblogs.com/ChurchYim/p/8427373.html>   |
| Free Icons | 图标 | <https://icons8.com/icons/set/home>    |
| Lindbloom | 色彩理论 | <http://brucelindbloom.com/index.html>    |
| tess4j | OCR | <https://github.com/nguyenq/tess4j>    |
| tesseract | OCR | <https://github.com/tesseract-ocr/tesseract>    |
| barcode4j | 生成条码 | <http://barcode4j.sourceforge.net>    |
| zxing | 生成/解码条码 | <https://github.com/zxing/zxing>    |
| flexmark-java | 转换Markdown | <https://github.com/vsch/flexmark-java>    |
| commons-compress | 归档/压缩 | <https://commons.apache.org/proper/commons-compress>   |
| XZ for Java | 归档/压缩 | <https://tukaani.org/xz/java.html>    |
| ffmpeg | 媒体转换/生成 | <http://ffmpeg.org>    |
| image4j | ico格式 | <https://github.com/imcdonagh/image4j>    |
| AutoCommitCell | 提交修改 | [https://stackoverflow.com/questions/24694616 （Ogmios）](https://stackoverflow.com/questions/24694616)   |
| 高德 | 地图 | <https://lbs.amap.com/api/javascript-api/summary>   |
| 高德 | 坐标 | <https://lbs.amap.com/api/webservice/guide/api/georegeo>   |
| 微博 | 图片素材 | <https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665>   |
| poi | 微软文档 | <https://poi.apache.org>    |
| LabeledBarChart | JavaFx图 | [https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland)](https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 "Roland") |
| commons-csv | CSV | <https://commons.apache.org/proper/commons-csv/>   |
| geonames | 位置数据 | <https://www.geonames.org/countries/>    |
| world-area | 位置数据 | <https://github.com/wizardcode/world-area>    |
| 中国国家统计局 | 数据 | <http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/>   |
| JHU | COVID-19数据 | <https://github.com/CSSEGISandData/COVID-19>    |
| 懒人图库 | 色彩数据 | <https://tool.lanrentuku.com/color/china.html>   |
| 中国纹样全集 | 素材 | <https://book.douban.com/subject/3894923/>    |
| 中国国家基础地理信息中心 | 地图 | <http://lbs.tianditu.gov.cn/api/js4.0/guide.html>   |
| movebank | 位置数据 | <https://www.datarepository.movebank.org>    |
| CoordinateConverter | 坐标转换 | <https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage>   |
| JavaMail | email | <https://javaee.github.io/javamail/>    |
| Commons IO | 文件读写 | <https://commons.apache.org/proper/commons-io/>   |
| colorhexa | 色彩数据 | <https://www.colorhexa.com/color-names>    |
| 文泉驿 | 开源字体 | <http://wenq.org/wqy2/>    |
| ttc2ttf | 提取ttf | <https://github.com/fermi1981/TTC_TTF>    |
| 中国出土壁画全集 | 素材 | <https://book.douban.com/subject/10465940/>    |
| 字体 | 书法 | <https://sfzd.hwcha.com>    |
| PaginatedPdfTable | PDF | <https://github.com/eduardohl/Paginated-PDFBox-Table-Sample>   |
| jsoup | html | <https://jsoup.org/>    |
| 知乎 | 素材 | <https://www.zhihu.com/question/41580677/answer/1300242801>   |
| commons-math | 计算 | <https://commons.apache.org/proper/commons-math/index.html>   |
| JEXL | 计算 | <https://commons.apache.org/proper/commons-jexl>   |
| OpenOffice | 文档 | <http://www.openoffice.org/>    |
| nashorn | JavaScript | <https://openjdk.org/projects/nashorn/>    |
| echarts-gl | WebGL | <https://github.com/ecomfe/echarts-gl>    |
| RYB色相 | 美术色彩 | <https://blog.csdn.net/weixin_44938037/article/details/90599711>   |
| jsch | sftp | <http://www.jcraft.com/jsch/>    |
| jackson | json | <https://github.com/FasterXML/jackson>    |
| batik | SVG | <https://xmlgraphics.apache.org/batik/>    |
| jankovicsandras | SVG | <https://github.com/jankovicsandras/imagetracerjava>    |
| miguelemosreverte | SVG | <https://github.com/miguelemosreverte/imagetracerjava>    |


# 特点
## 跨平台       

MyBox用纯Java实现且只基于开放资源，MyBox可运行于支持Java 23的平台。             
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
5. 提供文本/图片/网页/表格/树形的右键菜单和功能菜单。
6. 可弹出当前文本/图片/网页/数据。
7. 可管理的文本/图片/数据粘贴板。
8. 提供示例和有用的信息。  
9. 界面和控件的外观可选择和修改。      

## 数据兼容            

1. 导出的数据是通用的文本格式，如txt/csv/xml/json/html。
2. 导入的数据是通用的文本格式，如xml/csv。       
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
    

## 辅助自身编码            

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
       