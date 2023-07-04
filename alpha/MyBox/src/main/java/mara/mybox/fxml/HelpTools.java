package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorQueryController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.data.FunctionsList;
import mara.mybox.data.ImageItem;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTmpTools;
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

    public static File aboutColor() {
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
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File aboutDataAnalysis() {
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
            table.newLinkRow("", "https://data.stats.gov.cn/index.htm");
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File aboutData2D() {
        try {
            String lang = Languages.getLangName();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/mybox_about_data2d_" + lang + ".html",
                    "doc", "mybox_about_data2d_" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File aboutGroupingRows() {
        try {
            String lang = Languages.getLangName();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/mybox_about_grouping_" + lang + ".html",
                    "doc", "mybox_about_grouping_" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File aboutRowExpression() {
        try {
            String lang = Languages.getLangName();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/mybox_about_row_expression_" + lang + ".html",
                    "doc", "mybox_about_row_expression_" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
                            HtmlStyles.styleValue("Table"), table.body());
                    htmFile = HtmlWriteTools.writeHtml(html);
                } catch (Exception e) {
                    MyBoxLog.error(e);
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

    public static File usefulLinks() {
        try {
            StringTable table = new StringTable(null, message("Links"));
            table.newLinkRow(message("DecimalFormat"), decimalFormatLink());
            table.newLinkRow(message("DateFormat"), simpleDateFormatLink());
            table.newLinkRow(message("HtmlTutorial") + " - " + message("Chinese"), htmlZhLink());
            table.newLinkRow(message("HtmlTutorial") + " - " + message("English"), htmlEnLink());
            table.newLinkRow(message("JavaScriptTutorial") + " - " + message("Chinese"), javaScriptZhLink());
            table.newLinkRow(message("JavaScriptTutorial") + " - " + message("English"), javaScriptEnLink());
            table.newLinkRow("JavaScript language specification", javaScriptSpecification());
            table.newLinkRow("Nashorn User's Guide", nashornLink());
            table.newLinkRow(message("CssTutorial") + " - " + message("Chinese"), cssZhLink());
            table.newLinkRow(message("CssTutorial") + " - " + message("English"), cssEnLink());
            table.newLinkRow(message("CssReference"), cssSpecificationLink());
            table.newLinkRow(message("JavafxCssGuide"), javaFxCssLink());
            table.newLinkRow("Full list of Math functions", javaMathLink());
            table.newLinkRow("Learning the Java Language", javaLink());
            table.newLinkRow("Java Development Kit (JDK) APIs", javaAPILink());
            table.newLinkRow(message("DerbyReferenceManual"), derbyLink());
            table.newLinkRow(message("SqlIdentifier"), sqlLink());
            table.newLinkRow("RenderingHints", renderingHintsLink());
            table.newLinkRow(message("JsonTutorial") + " - " + message("Chinese"), jsonZhLink());
            table.newLinkRow(message("JsonTutorial") + " - " + message("English"), jsonEnLink());
            table.newLinkRow(message("JsonSpecification"), jsonSpecification());
            table.newLinkRow(message("XmlTutorial") + " - " + message("Chinese"), xmlZhLink());
            table.newLinkRow(message("XmlTutorial") + " - " + message("English"), xmlEnLink());
            table.newLinkRow(message("DomSpecification"), domSpecification());
            table.newLinkRow(message("Charset"), "https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/nio/charset/Charset.html");
            table.newLinkRow("URI", "https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/net/URI.html");
            table.newLinkRow("URL", "https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/net/URL.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            return htmFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File makeFunctionsList(MenuBar menuBar) {
        try {
            FunctionsList list = new FunctionsList(menuBar, false);
            StringTable table = list.make();
            if (table != null) {
                File htmFile = HtmlWriteTools.writeHtml(table.html());
                return htmFile;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File interfaceTips() {
        try {
            String lang = Languages.getLangName();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/mybox_interface_tips_" + lang + ".html",
                    "doc", "mybox_interface_tips_" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File makeInterfaceTips() {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");

            s.append("<H1>").append(message("DocumentTools")).append("</H1>\n");
            s.append("    <H3>").append(message("Notes")).append("</H3>\n");
            s.append("    <PRE>").append(message("NotesComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("PdfView")).append("</H3>\n");
            s.append("    <PRE>").append(message("PdfViewTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("MarkdownEditer")).append("</H3>\n");
            s.append("    <PRE>").append(message("MarkdownEditerTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("HtmlEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message("HtmlEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("HtmlSnap")).append("</H3>\n");
            s.append("    <PRE>").append(message("HtmlSnapComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("JsonEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message("JsonEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("XmlEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message("XmlEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("TextEditer")).append("</H3>\n");
            s.append("    <PRE>").append(message("TextEditerTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("Charset")).append("</H3>\n");
            s.append("    <PRE>").append(message("EncodeComments")).append("</PRE>\n");

            s.append("    <H3>").append("BOM").append("</H3>\n");
            s.append("    <PRE>").append(message("BOMcomments")).append("</PRE>\n");

            s.append("    <H3>").append(message("FindReplace")).append(" - ").append(message("Texts")).append("</H3>\n");
            s.append("    <PRE>").append(message("FindReplaceTextsTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("FindReplace")).append(" - ").append(message("Bytes")).append("</H3>\n");
            s.append("    <PRE>").append(message("FindReplaceBytesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("FilterLines")).append("</H3>\n");
            s.append("    <PRE>").append(message("FilterTypesComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("TextFindBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message("TextFindBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("TextReplaceBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message("TextReplaceBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("TextToHtml")).append("</H3>\n");
            s.append("    <PRE>").append(message("PasteTextAsHtml")).append("</PRE>\n");

            s.append("    <H3>").append(message("BytesFindBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message("BytesFindBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("TextInMyBoxClipboard")).append("</H3>\n");
            s.append("    <PRE>").append(message("TextClipboardUseComments")).append("</PRE></BR>\n");
            s.append("    <PRE>").append(message("TextInMyBoxClipboardTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("WordView")).append("</H3>\n");
            s.append("    <PRE>").append(message("WordViewTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message("ImageTools")).append("</H1>\n");
            s.append("    <H3>").append(message("ImageViewer")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageViewerTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("EditImage")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageManufactureTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("SVGEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message("SVGEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageAnalyse")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageAnalyseTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageQuantization")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageQuantizationComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("Dithering")).append("</H3>\n");
            s.append("    <PRE>").append(message("DitherComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("ColorMatching")).append("</H3>\n");
            s.append("    <PRE>").append(message("ColorMatchComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("PremultipliedAlpha")).append("</H3>\n");
            s.append("    <PRE>").append(message("PremultipliedAlphaTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("Thresholding")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageThresholdingComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageRepeatTile")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageRepeatTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageSample")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageSampleTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageSplit")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageSplitTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImagesBrowser")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImagesBrowserTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImagesEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImagesEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImagesPlay")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImagesPlayTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageOCR")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageOCRComments")).append("</PRE></BR>\n");
            s.append("    <PRE>").append(message("OCRPreprocessComment")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImageAlphaExtract")).append("</H3>\n");
            s.append("    <PRE>").append(message("ImageAlphaExtractTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ImagesInSystemClipboard")).append("</H3>\n");
            s.append("    <PRE>").append(message("RecordImagesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ManageColors")).append("</H3>\n");
            s.append("    <PRE>").append(message("ColorsManageTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("DrawChromaticityDiagram")).append("</H3>\n");
            s.append("    <PRE>").append(message("ChromaticityDiagramTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("IccProfileEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message("IccProfileTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message("NetworkTools")).append("</H1>\n");
            s.append("    <H3>").append(message("DownloadHtmls")).append("</H3>\n");
            s.append("    <PRE>").append(message("DownloadFirstLevelLinksComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("ConvertUrl")).append("</H3>\n");
            s.append("    <PRE>").append(message("ConvertUrlTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("QueryDNSBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message("QueryDNSBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("WeiboSnap")).append("</H3>\n");
            s.append("    <PRE>").append(message("WeiboAddressComments")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message("DataTools")).append("</H1>\n");
            s.append("    <H3>").append(message("Column")).append("</H3>\n");
            s.append("    <PRE>").append(message("ColumnComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("ManageData")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataManageTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("EditCSV")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataFileCSVTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("EditExcel")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataFileExcelTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("EditTextDataFile")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataFileTextTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("DatabaseTable")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataTableTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("SqlIdentifier")).append("</H3>\n");
            s.append("    <PRE>").append(message("SqlIdentifierComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("XYChart")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataChartXYTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("PieChart")).append("</H3>\n");
            s.append("    <PRE>").append(message("DataChartPieTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("BoxWhiskerChart")).append("</H3>\n");
            s.append("    <PRE>").append(message("BoxWhiskerChartTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ComparisonBarsChart")).append("</H3>\n");
            s.append("    <PRE>").append(message("ComparisonBarsChartTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("SelfComparisonBarsChart")).append("</H3>\n");
            s.append("    <PRE>").append(message("SelfComparisonBarsChartTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("XYZChart")).append("</H3>\n");
            s.append("    <PRE>").append(message("WebglComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("SetStyles")).append("</H3>\n");
            s.append("    <PRE>").append(message("SetStylesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("SimpleLinearRegression")).append("</H3>\n");
            s.append("    <PRE>").append(message("SimpleLinearRegressionTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("SimpleLinearRegressionCombination")).append("</H3>\n");
            s.append("    <PRE>").append(message("SimpleLinearRegressionCombinationTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("MultipleLinearRegression")).append("</H3>\n");
            s.append("    <PRE>").append(message("MultipleLinearRegressionTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("MultipleLinearRegressionCombination")).append("</H3>\n");
            s.append("    <PRE>").append(message("MultipleLinearRegressionCombinationTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("Matrix")).append("</H3>\n");

            s.append("    <H4>").append(message("Plus")).append("</H4>\n");
            s.append("    <PRE>").append(message("MatricesPlusComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("Minus")).append("</H4>\n");
            s.append("    <PRE>").append(message("MatricesMinusComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("Multiply")).append("</H4>\n");
            s.append("    <PRE>").append(message("MatricesMultiplyComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("HadamardProduct")).append("</H4>\n");
            s.append("    <PRE>").append(message("HadamardProductComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("KroneckerProduct")).append("</H4>\n");
            s.append("    <PRE>").append(message("KroneckerProductComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("VerticalMerge")).append("</H4>\n");
            s.append("    <PRE>").append(message("VerticalMergeComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("HorizontalMerge")).append("</H4>\n");
            s.append("    <PRE>").append(message("HorizontalMergeComments")).append("</PRE>\n");

            s.append("\n");

            s.append("    <H3>").append(message("JavaScript")).append("</H3>\n");
            s.append("    <PRE>").append(message("JavaScriptTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("JShell")).append("</H3>\n");
            s.append("    <PRE>").append(message("JShellTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("JEXL")).append("</H3>\n");
            s.append("    <PRE>").append(message("JEXLTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("MathFunction")).append("</H3>\n");
            s.append("    <PRE>").append(message("MathFunctionTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("GeographyCode")).append("</H3>\n");
            s.append("    <PRE>").append(message("GeographyCodeEditComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("MapOptions")).append("</H3>\n");
            s.append("    <PRE>").append(message("MapComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("ConvertCoordinate")).append("</H3>\n");
            s.append("    <PRE>").append(message("ConvertCoordinateTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message("MediaTools")).append("</H1>\n");
            s.append("    <H3>").append(message("MediaPlayer")).append("</H3>\n");
            s.append("    <PRE>").append(message("MediaPlayerSupports")).append("</PRE>\n");

            s.append("    <H3>").append(message("FFmpeg")).append("</H3>\n");
            s.append("    <PRE>").append(message("FFmpegExeComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("FFmpegOptions")).append("</H4>\n");
            s.append("    <PRE>").append(message("FFmpegOptionsTips")).append("</PRE></BR>\n");
            s.append("    <PRE>").append(message("FFmpegArgumentsTips")).append("</PRE>\n");

            s.append("    <H4>").append(message("FFmpegScreenRecorder")).append("</H4>\n");
            s.append("    <PRE>").append(message("FFmpegScreenRecorderComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("CRF")).append("</H4>\n");
            s.append("    <PRE>").append(message("CRFComments")).append("</PRE>\n");

            s.append("    <H4>").append(message("X264")).append("</H4>\n");
            s.append("    <PRE>").append(message("X264PresetComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("GameElimniation")).append("</H3>\n");
            s.append("    <PRE>").append(message("GameEliminationComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("GameMine")).append("</H3>\n");
            s.append("    <PRE>").append(message("GameMineTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message("Others")).append("</H1>\n");
            s.append("    <H3>").append(message("Table")).append("</H3>\n");
            s.append("    <PRE>").append(message("TableTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("Play")).append("</H3>\n");
            s.append("    <PRE>").append(message("PlayerComments")).append("</PRE>\n");

            s.append("    <H3>").append(message("ManageLanguages")).append("</H3>\n");
            s.append("    <PRE>").append(message("MyBoxLanguagesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("Shortcuts")).append("</H3>\n");
            s.append("    <PRE>").append(message("ShortcutsTips")).append("</PRE>\n");

            s.append("    <H3>").append(message("ClearExpiredData")).append("</H3>\n");
            s.append("    <PRE>").append(message("ClearExpiredDataComments")).append("</PRE>\n");

            s.append("\n");

            s.append("</BODY>\n");

            String html = HtmlWriteTools.html(message("InterfaceTips"), HtmlStyles.DefaultStyle, s.toString());

            File file = new File(FileTmpTools.generatePath("html")
                    + "/mybox_interface_tips_" + Languages.getLangName() + ".html");

            return TextFileTools.writeFile(file, html);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
        return "https://docs.oracle.com/en/java/javase/20/docs/api/index.html";
    }

    public static String javaMathLink() {
        return "https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/lang/Math.html";
    }

    public static String decimalFormatLink() {
        return "https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/text/DecimalFormat.html";
    }

    public static String simpleDateFormatLink() {
        return "https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/text/SimpleDateFormat.html";
    }

    public static String renderingHintsLink() {
        return "https://docs.oracle.com/en/java/javase/20/docs/api/java.desktop/java/awt/RenderingHints.html";
    }

    public static String cssSpecificationLink() {
        return "https://www.w3.org/TR/CSS/#css";
    }

    public static String cssEnLink() {
        return "https://developer.mozilla.org/en-US/docs/web/css/reference";
    }

    public static String cssZhLink() {
        return "https://developer.mozilla.org/zh-CN/docs/web/css/reference";
    }

    public static String nashornLink() {
        return "https://docs.oracle.com/javase/10/nashorn/toc.htm";
    }

    public static String htmlZhLink() {
        return "https://developer.mozilla.org/zh-CN/docs/Web/JavaScript";
    }

    public static String htmlEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Learn/HTML";
    }

    public static String javaScriptZhLink() {
        return "https://developer.mozilla.org/zh-CN/docs/Web/JavaScript";
    }

    public static String javaScriptEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Web/JavaScript";
    }

    public static String javaScriptSpecification() {
        return "https://www.ecma-international.org/publications-and-standards/standards/ecma-262/";
    }

    public static String jsonEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Objects/JSON";
    }

    public static String jsonZhLink() {
        return "http://www.vue5.com/json/json_quick_guide.html";
    }

    public static String jsonSpecification() {
        return "https://www.ecma-international.org/publications-and-standards/standards/ecma-404/";
    }

    public static String xmlEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Web/XML/XML_introduction";
    }

    public static String xmlZhLink() {
        return "http://www.vue5.com/xml/dom.html";
    }

    public static String domSpecification() {
        return "https://www.w3.org/TR/DOM-Level-3-Core/";
    }

    public static String svgEnLink() {
        return "https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial";
    }

    public static String svgZhLink() {
        return "http://www.vue5.com/svg/svg_tutorial.html";
    }

    public static String svgSpecification() {
        return "https://www.w3.org/Graphics/SVG/";
    }

    public static List<MenuItem> htmlHelps(boolean popMenu) {
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

            menuItem = new MenuItem("JavaScript language specification");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaScriptSpecification(), true);
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
                    WebBrowserController.openAddress(HelpTools.cssSpecificationLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("ColorQuery"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ColorQueryController.open();
                }
            });
            items.add(menuItem);

            if (popMenu) {
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
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> rowExpressionHelps(boolean popMenu) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("AboutRowExpression"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openFile(HelpTools.aboutRowExpression());
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

            menuItem = new MenuItem("JavaScript language specification");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaScriptSpecification(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("Nashorn User's Guide");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.nashornLink(), true);
                }
            });
            items.add(menuItem);

            if (popMenu) {
                items.add(new SeparatorMenuItem());

                CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
                hoverMenu.setSelected(UserConfig.getBoolean("RowExpressionsHelpsPopWhenMouseHovering", false));
                hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean("RowExpressionsHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                    }
                });
                items.add(hoverMenu);
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> xmlHelps(boolean popMenu) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("XmlTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.xmlEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("XmlTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.xmlZhLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("DomSpecification"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.domSpecification(), true);
                }
            });
            items.add(menuItem);

            if (popMenu) {
                items.add(new SeparatorMenuItem());

                CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
                hoverMenu.setSelected(UserConfig.getBoolean("XmlHelpsPopWhenMouseHovering", false));
                hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean("XmlHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                    }
                });
                items.add(hoverMenu);
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> svgHelps(boolean popMenu) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("SvgTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.svgEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("SvgTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.svgZhLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("SvgSpecification"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.svgSpecification(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            items.addAll(HelpTools.xmlHelps(false));

            items.add(new SeparatorMenuItem());

            items.addAll(HelpTools.htmlHelps(false));

            if (popMenu) {
                items.add(new SeparatorMenuItem());

                CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
                hoverMenu.setSelected(UserConfig.getBoolean("SvgHelpsPopWhenMouseHovering", false));
                hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean("SvgHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                    }
                });
                items.add(hoverMenu);
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> colorHelps(boolean popMenu) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("ColorCode"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://openjfx.io/javadoc/20/javafx.graphics/javafx/scene/paint/Color.html", true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("ColorSpace"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://docs.oracle.com/en/java/javase/20/docs/api/java.desktop/java/awt/color/ColorSpace.html", true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("ColorModels"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://docs.oracle.com/en/java/javase/20/docs/api/java.desktop/java/awt/image/ColorModel.html", true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem("sRGB");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://www.w3.org/Graphics/Color/sRGB.html", true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("RYBComplementaryColor"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://blog.csdn.net/weixin_44938037/article/details/90599711", true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("AboutColor"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openFile(aboutColor());
                }
            });
            items.add(menuItem);

            if (popMenu) {
                items.add(new SeparatorMenuItem());

                CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
                hoverMenu.setSelected(UserConfig.getBoolean("ColorHelpsPopWhenMouseHovering", false));
                hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean("ColorHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                    }
                });
                items.add(hoverMenu);
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static LinkedHashMap<String, String> svgPathExamples() {
        try {
            LinkedHashMap<String, String> values = new LinkedHashMap<>();
            values.put("M x,y; ", message("SvgPathM"));
            values.put("m dx,dy; ", message("SvgPathm"));
            values.put("L x,y; ", message("SvgPathL"));
            values.put("l dx,dy; ", message("SvgPathl"));
            values.put("H x; ", message("SvgPathH"));
            values.put("h dx; ", message("SvgPathh"));
            values.put("V y; ", message("SvgPathV"));
            values.put("v dy; ", message("SvgPathv"));
            values.put("Q x1,y1 x,y; ", message("SvgPathQ"));
            values.put("q dx1,dy1 dx,dy; ", message("SvgPathq"));
            values.put("T x,y; ", message("SvgPathT"));
            values.put("t dx,dy; ", message("SvgPatht"));
            values.put("C x1,y1 x2,y2 x,y; ", message("SvgPathC"));
            values.put("c dx1,dy1 dx2,dy2 dx,dy; ", message("SvgPathc"));
            values.put("S x2,y2 x,y; ", message("SvgPathS"));
            values.put("s dx2,dy2 dx,dy; ", message("SvgPaths"));
            values.put("A rx ry angle large-arc-flag sweep-flag x,y; ", message("SvgPathA"));
            values.put("s rx ry angle large-arc-flag sweep-flag x,y; ", message("SvgPatha"));
            values.put("Z; ", message("SvgPathZ"));

            return values;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static LinkedHashMap<String, String> svgStyleExamples() {
        try {
            LinkedHashMap<String, String> values = new LinkedHashMap<>();
            values.put("fill: #CCCCCC; ", message("FilledColor") + " - " + message("ColorCode"));
            values.put("fill: skyblue; ", message("FilledColor") + " - " + message("Name"));
            values.put("fill: hsb(0,50,100); ", message("FilledColor") + " - HSB");
            values.put("fill: none; ", message("FilledColor") + " - " + message("None"));
            values.put("fill-opacity: 0.3; ", message("FillOpacity"));
            values.put("stroke: black; ", message("StrokeColor") + " - " + message("ColorCode"));
            values.put("stroke: rgb(0,128,0); ", message("StrokeColor") + " - RGB");
            values.put("stroke-opacity: 0.3; ", message("StrokeOpacity"));
            values.put("stroke-width: 2; ", message("StrokeWidth"));
            values.put("stroke-linecap: butt; ", message("StrokeLinecap") + " - " + message("Butt"));
            values.put("stroke-linecap: round; ", message("StrokeLinecap") + " - " + message("Round"));
            values.put("stroke-linecap: square; ", message("StrokeLinecap") + " - " + message("SquareShape"));
            values.put("stroke-dasharray: 2,5; ", message("StrokeDasharray"));
            values.put("stroke-dasharray: 20,10,5,5,5,10; ", message("StrokeDasharray"));
            values.put("font-size: 15px; ", message("FontSize"));
            values.put("font-family: sans-serif; ", message("FontFamily"));
            values.put("color: #6900ff; ", message("Color"));
            values.put("background: #bae498; ", message("BackgroundColor"));

            return values;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
