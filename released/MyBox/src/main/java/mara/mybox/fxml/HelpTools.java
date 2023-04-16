package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.data.ImageItem;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-8-8
 * @License Apache License Version 2.0
 */
public class HelpTools {

    public static void readMe(BaseController controller) {
        try {
            String lang = Languages.getLangName();
            File htmlFile = new File(AppVariables.MyboxDataPath + "/doc/readme-" + lang + ".html");
            File mdFile = FxFileTools.getInternalFile("/doc/" + lang + "/README.md",
                    "doc", "README-" + lang + ".md", true);
            String html = MarkdownTools.md2html(mdFile);
            if (html == null) {
                return;
            }
            html = html.replaceAll("href=\"", "target=_blank href=\"");
            TextFileTools.writeFile(htmlFile, html);
            PopTools.browseURI(controller, htmlFile.toURI());
            SoundTools.miao5();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static File aboutColorHtml() {
        try {
            StringTable table = new StringTable(null, message("ResourcesAboutColor"));
            table.newLinkRow("ICCWebsite", "http://www.color.org");
            table.newLinkRow("ICCProfileTags", "https://sno.phy.queensu.ca/~phil/exiftool/TagNames/ICC_Profile.html");
            table.newLinkRow("IccProfilesECI", "http://www.eci.org/en/downloads");
            table.newLinkRow("IccProfilesAdobe", "https://supportdownloads.adobe.com/detail.jsp?ftpID=3680");
            table.newLinkRow("ColorSpace", "http://brucelindbloom.com/index.html?WorkingSpaceInfo.html#Specifications");
            table.newLinkRow("StandardsRGB", "https://www.w3.org/Graphics/Color/sRGB.html");
            table.newLinkRow("RGBXYZMatrices", "http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html");
            table.newLinkRow("ColorCalculator", "http://www.easyrgb.com/en/math.php");
            table.newLinkRow("", "http://brucelindbloom.com/index.html?ColorCalculator.html");
            table.newLinkRow("", "http://davengrace.com/cgi-bin/cspace.pl");
            table.newLinkRow("ColorData", "https://www.rit.edu/science/pocs/useful-data");
            table.newLinkRow("", "http://www.thefullwiki.org/Standard_illuminant");
            table.newLinkRow("ColorTopics", "https://www.codeproject.com/Articles/1202772/Color-Topics-for-Programmers");
            table.newLinkRow("", "https://www.w3.org/TR/css-color-4/#lab-to-rgb");
            table.newLinkRow("ChromaticAdaptation", "http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html");
            table.newLinkRow("ChromaticityDiagram", "http://demonstrations.wolfram.com/CIEChromaticityDiagram/");
            table.newLinkRow("ArtHuesWheel", "https://blog.csdn.net/weixin_44938037/article/details/90599711");
            table.newLinkRow("", "https://stackoverflow.com/questions/4945457/conversion-between-rgb-and-ryb-color-spaces");
            table.newLinkRow("", "https://math.stackexchange.com/questions/305395/ryb-and-rgb-color-space-conversion");
            table.newLinkRow("", "http://bahamas10.github.io/ryb/about.html");
            table.newLinkRow("", "https://redyellowblue.org/ryb-color-model/");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            return htmFile;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File AboutDataAnalysisHtml() {
        try {
            StringTable table = new StringTable(null, message("AboutDataAnalysis"));
            table.newLinkRow(message("Dataset"), "http://archive.ics.uci.edu/ml/datasets.php");
            table.newLinkRow("", "https://www4.stat.ncsu.edu/~boos/var.select/");
            table.newLinkRow("", "http://lib.stat.cmu.edu/datasets/");
            table.newLinkRow("", "http://yann.lecun.com/exdb/mnist/");
            table.newLinkRow("", "https://docs.microsoft.com/en-us/azure/open-datasets/");
            table.newLinkRow("", "https://github.com/tomsharp/SVR/tree/master/data");
            table.newLinkRow("", "https://github.com/krishnaik06/simple-Linear-Regression");
            table.newLinkRow("", "https://github.com/susanli2016/Machine-Learning-with-Python/tree/master/data");
            table.newLinkRow("", "https://www.datarepository.movebank.org");
            table.newLinkRow("", "https://github.com/CSSEGISandData/COVID-19");
            table.newLinkRow("Apache-Math", "https://commons.apache.org/proper/commons-math/");
            table.newLinkRow("", "https://commons.apache.org/proper/commons-math/apidocs/index.html");
            table.newLinkRow(message("Study"), "https://github.com/InfolabAI/DeepLearning");
            table.newLinkRow("", "https://www.deeplearningbook.org/");
            table.newLinkRow("", "https://github.com/zsdonghao/deep-learning-book");
            table.newLinkRow("", "https://github.com/hadrienj/deepLearningBook-Notes");
            table.newLinkRow("", "https://github.com/janishar/mit-deep-learning-book-pdf");
            table.newLinkRow("", "https://clauswilke.com/dataviz/");
            table.newLinkRow("", "https://www.kancloud.cn/apachecn/dataviz-zh/1944809");
            table.newLinkRow("", "https://www.bilibili.com/video/BV1Ua4y1e7YG");
            table.newLinkRow("", "https://www.bilibili.com/video/BV1i7411d7aP");
            table.newLinkRow("", "https://github.com/fengdu78/Coursera-ML-AndrewNg-Notes");
            table.newLinkRow(message("Tools"), "https://scikit-learn.org/stable/");
            table.newLinkRow("", "https://www.mathworks.com/help/index.html");
            table.newLinkRow(message("Example"), "https://www.xycoon.com/simple_linear_regression.htm");
            table.newLinkRow("", "https://www.scribbr.com/statistics/simple-linear-regression/");
            table.newLinkRow("", "http://www.datasetsanalysis.com/regressions/simple-linear-regression.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            return htmFile;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File aboutCoordinateSystem() {
        try {
            StringTable table = new StringTable(null, message("AboutCoordinateSystem"));
            table.newLinkRow("ChinaCommonGeospatialInformationServices", "https://www.tianditu.gov.cn/");
            table.newLinkRow("", "https://www.tianditu.gov.cn/world_coronavirusmap/");
            table.newLinkRow("ChineseCoordinateSystems", "https://politics.stackexchange.com/questions/40991/why-must-chinese-maps-be-obfuscated");
            table.newLinkRow("", "https://zhuanlan.zhihu.com/p/62243160");
            table.newLinkRow("", "https://blog.csdn.net/qq_36377037/article/details/86479796");
            table.newLinkRow("", "https://www.zhihu.com/question/31204062?sort=created");
            table.newLinkRow("", "https://blog.csdn.net/ssxueyi/article/details/102622156");
            table.newLinkRow("EPSGCodes", "http://epsg.io/4490");
            table.newLinkRow("", "http://epsg.io/4479");
            table.newLinkRow("", "http://epsg.io/4326");
            table.newLinkRow("", "http://epsg.io/3857");
            table.newLinkRow("TrackingData", "https://www.microsoft.com/en-us/download/details.aspx?id=52367");
            table.newLinkRow("", "https://www.datarepository.movebank.org/discover");
            table.newLinkRow("", "https://sumo.dlr.de/docs/Data/Scenarios/TAPASCologne.html");
            table.newLinkRow("", "https://blog.csdn.net/souvenir001/article/details/52180335");
            table.newLinkRow("", "https://www.cnblogs.com/genghenggao/p/9625511.html");
            table.newLinkRow("TianDiTuAPI", "http://lbs.tianditu.gov.cn/api/js4.0/guide.html");
            table.newLinkRow("TianDiTuKey", "https://console.tianditu.gov.cn/api/key");
            table.newLinkRow("GaoDeAPI", "https://lbs.amap.com/api/javascript-api/summary");
            table.newLinkRow("GaoDeKey", "https://console.amap.com/dev/index");
            File htmFile = HtmlWriteTools.writeHtml(table.html());
            return htmFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File aboutMedia() {
        try {
            StringTable table = new StringTable(null, message("AboutMedia"));
            table.newLinkRow("FFmpegDocuments", "http://ffmpeg.org/documentation.html");
            table.newLinkRow("FFmpeg wiki", "https://trac.ffmpeg.org");
            table.newLinkRow("H264VideoEncodingGuide", "http://trac.ffmpeg.org/wiki/Encode/H.264");
            table.newLinkRow("AACEncodingGuide", "https://trac.ffmpeg.org/wiki/Encode/AAC");
            table.newLinkRow("UnderstandingRateControlModes", "https://slhck.info/video/2017/03/01/rate-control.html");
            table.newLinkRow("CRFGuide", "https://slhck.info/video/2017/02/24/crf-guide.html");
            table.newLinkRow("CapturingDesktopScreenRecording", "http://trac.ffmpeg.org/wiki/Capture/Desktop");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            return htmFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File aboutGroupingRows() {
        try {
            String lang = Languages.getLangName();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/MyBox-about-grouping-" + lang + ".html",
                    "doc", "MyBox-about-grouping-" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File aboutRowExpression() {
        try {
            String lang = Languages.getLangName();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/MyBox-about-row-expression-" + lang + ".html",
                    "doc", "MyBox-about-row-expression-" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void imageStories(BaseController controller) {
        SingletonTask task = new SingletonTask<Void>(controller) {
            private File htmFile;

            @Override
            protected boolean handle() {
                try {
                    StringTable table = new StringTable(null, message("StoriesOfImages"));
                    List<ImageItem> predefinedItems = ImageItem.predefined();
                    for (ImageItem item : predefinedItems) {
                        String comments = item.getComments();
                        File file = item.getFile();
                        if (comments == null || comments.isBlank()
                                || file == null || !file.exists()) {
                            continue;
                        }
                        setInfo(file.getAbsolutePath());
                        table.newNameValueRow(
                                "<Img src='" + file.toURI().toString() + "' width=" + item.getWidth() + ">",
                                comments);
                    }
                    String html = HtmlWriteTools.html(table.getTitle(), "utf-8",
                            HtmlStyles.styleValue("TableStyle"), table.body());
                    htmFile = HtmlWriteTools.writeHtml(html);
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
                return htmFile != null && htmFile.exists();
            }

            @Override
            protected void whenSucceeded() {
                controller.browse(htmFile);
            }

        };
        controller.start(task);
    }

    public static String javaFxCssLink() {
        return "https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html";
    }

    public static String derbyLink() {
        return "https://db.apache.org/derby/docs/10.15/ref/index.html";
    }

    public static String sqlLink() {
        return "https://db.apache.org/derby/docs/10.15/ref/crefsqlj18919.html";
    }

    public static String javaLink() {
        return "https://docs.oracle.com/javase/tutorial/java/index.html";
    }

    public static String javaAPILink() {
        return "https://docs.oracle.com/en/java/javase/18/docs/api/index.html";
    }

    public static String javaMathLink() {
        return "https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Math.html";
    }

    public static String decimalFormatLink() {
        return "https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/text/DecimalFormat.html";
    }

    public static String simpleDateFormatLink() {
        return "https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/text/SimpleDateFormat.html";
    }

    public static String renderingHintsLink() {
        return "https://docs.oracle.com/en/java/javase/18/docs/api/java.desktop/java/awt/RenderingHints.html";
    }

    public static String cssLink() {
        return "https://www.w3.org/TR/CSS/#css";
    }

    public static String cssEnLink() {
        return "https://developer.mozilla.org/en-US/docs/web/css/reference";
    }

    public static String cssZhLink() {
        return "https://www.w3school.com.cn/cssref/index.asp";
    }

    public static String nashornLink() {
        return "https://docs.oracle.com/javase/10/nashorn/toc.htm";
    }

    public static String htmlZhLink() {
        return "https://www.w3school.com.cn/html/index.asp";
    }

    public static String htmlEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Learn/HTML";
    }

    public static String javaScriptZhLink() {
        return "https://www.w3school.com.cn/js/index.asp";
    }

    public static String javaScriptEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Web/JavaScript";
    }

    public static List<MenuItem> htmlHelps(BaseController controller) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("HtmlTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.htmlEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("HtmlTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.htmlZhLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("JavaScriptTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaScriptEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("JavaScriptTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaScriptZhLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("CssTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.cssEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("CssTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.cssZhLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("CssReference"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.cssLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("HtmlHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("HtmlHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
