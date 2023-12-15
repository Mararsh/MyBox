package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorQueryController;
import mara.mybox.controller.WebBrowserController;
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
            File htmlFile = makeReadMe(Languages.embedFileLang());
            if (htmlFile == null) {
                return;
            }
            PopTools.browseURI(controller, htmlFile.toURI());
            SoundTools.miao5();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static File makeReadMe(String fileLang) {
        try {
            File htmlFile = new File(AppVariables.MyboxDataPath + "/doc/readme_" + fileLang + ".html");
            File mdFile = FxFileTools.getInternalFile("/doc/" + fileLang + "/README.md",
                    "doc", "README-" + fileLang + ".md", true);
            String html = MarkdownTools.md2html(null,
                    MarkdownTools.htmlOptions(), mdFile, HtmlStyles.DefaultStyle);
            if (html == null) {
                return null;
            }
            html = html.replaceAll("href=\"", "target=_blank href=\"");
            TextFileTools.writeFile(htmlFile, html);
            return htmlFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
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
            String lang = Languages.embedFileLang();
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
            String lang = Languages.embedFileLang();
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
            String lang = Languages.embedFileLang();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/mybox_about_row_expression_" + lang + ".html",
                    "doc", "mybox_about_row_expression_" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File aboutTreeInformation() {
        try {
            String lang = Languages.embedFileLang();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/mybox_about_tree_" + lang + ".html",
                    "doc", "mybox_about_tree_" + lang + ".html");
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void imageStories(BaseController controller) {
        FxTask task = new FxTask<Void>(controller) {
            private File htmFile;

            @Override
            protected boolean handle() {
                htmFile = imageStories(this, false, AppVariables.CurrentLangName);
                return htmFile != null && htmFile.exists();
            }

            @Override
            protected void whenSucceeded() {
                controller.browse(htmFile);
            }

        };
        controller.start(task);
    }

    public static File imageStories(FxTask task, boolean isRemote, String lang) {
        try {
            StringTable table = new StringTable(null, message(lang, "StoriesOfImages"));
            List<ImageItem> predefinedItems = ImageItem.predefined(lang);
            for (ImageItem item : predefinedItems) {
                String comments = item.getComments();
                File file = item.getFile();
                if (comments == null || comments.isBlank()
                        || file == null || !file.exists()) {
                    continue;
                }
                task.setInfo(file.getAbsolutePath());
                table.newNameValueRow(
                        "<Img src='" + (isRemote
                                ? "https://mara-mybox.sourceforge.io/images/" + file.getName()
                                : file.toURI().toString())
                        + "' width=" + item.getWidth() + ">",
                        comments);
            }

            String html = HtmlWriteTools.html(table.getTitle(), "utf-8",
                    HtmlStyles.styleValue("Table"), table.body());
            File file = new File(FileTmpTools.generatePath("html")
                    + "/MyBox-StoriesOfImages-" + lang + ".html");
            return TextFileTools.writeFile(file, html);

        } catch (Exception e) {
            task.setError(e.toString());
            return null;
        }
    }

    public static File usefulLinks(String lang) {
        try {
            StringTable table = new StringTable(null, message(lang, "Links"));
            table.newLinkRow(message(lang, "DecimalFormat"), decimalFormatLink());
            table.newLinkRow(message(lang, "DateFormat"), simpleDateFormatLink());
            table.newLinkRow(message(lang, "Charset"), charsetLink());
            table.newLinkRow("URI", uriLink());
            table.newLinkRow("URL", urlLink());
            table.newLinkRow("Full list of Math functions", javaMathLink());
            table.newLinkRow("Learning the Java Language", javaLink());
            table.newLinkRow("Java Development Kit (JDK) APIs", javaAPILink());
            table.newLinkRow(message(lang, "JavafxCssGuide"), javaFxCssLink());
            table.newLinkRow(message(lang, "DerbyReferenceManual"), derbyLink());
            table.newLinkRow(message(lang, "SqlIdentifier"), sqlLink());
            table.newLinkRow(message(lang, "HtmlTutorial") + " - " + message(lang, "Chinese"), htmlZhLink());
            table.newLinkRow(message(lang, "HtmlTutorial") + " - " + message(lang, "English"), htmlEnLink());
            table.newLinkRow(message(lang, "JavaScriptTutorial") + " - " + message(lang, "Chinese"), javaScriptZhLink());
            table.newLinkRow(message(lang, "JavaScriptTutorial") + " - " + message(lang, "English"), javaScriptEnLink());
            table.newLinkRow("JavaScript language specification", javaScriptSpecification());
            table.newLinkRow("Nashorn User's Guide", nashornLink());
            table.newLinkRow(message(lang, "CssTutorial") + " - " + message(lang, "Chinese"), cssZhLink());
            table.newLinkRow(message(lang, "CssTutorial") + " - " + message(lang, "English"), cssEnLink());
            table.newLinkRow(message(lang, "CssReference"), cssSpecificationLink());
            table.newLinkRow("RenderingHints", renderingHintsLink());
            table.newLinkRow(message(lang, "JsonTutorial") + " - " + message(lang, "Chinese"), jsonZhLink());
            table.newLinkRow(message(lang, "JsonTutorial") + " - " + message(lang, "English"), jsonEnLink());
            table.newLinkRow(message(lang, "JsonSpecification"), jsonSpecification());
            table.newLinkRow(message(lang, "XmlTutorial") + " - " + message(lang, "Chinese"), xmlZhLink());
            table.newLinkRow(message(lang, "XmlTutorial") + " - " + message(lang, "English"), xmlEnLink());
            table.newLinkRow(message(lang, "DomSpecification"), domSpecification());
            table.newLinkRow(message(lang, "SvgTutorial") + " - " + message(lang, "Chinese"), svgZhLink());
            table.newLinkRow(message(lang, "SvgTutorial") + " - " + message(lang, "English"), svgEnLink());
            table.newLinkRow(message(lang, "SvgSpecification"), svgSpecification());
            table.newLinkRow("SVGPath in JavaFX", javafxSVGPathLink());
            table.newLinkRow("Shape 2D in JavaFX", javafxShape2DLink());
            table.newLinkRow("Shape 2D in Java", javaShape2DLink());

            String html = HtmlWriteTools.html(message(lang, "Links"), HtmlStyles.DefaultStyle, table.div());

            File file = new File(FileTmpTools.generatePath("html")
                    + "/mybox_useful_link_" + lang + ".html");

            return TextFileTools.writeFile(file, html);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File makeInterfaceTips(String lang) {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<BODY>\n");

            s.append("<H1>").append(message(lang, "DocumentTools")).append("</H1>\n");
            s.append("    <H3>").append(message(lang, "Notes")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "NotesComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "PdfView")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "PdfViewTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "MarkdownEditer")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MarkdownEditerTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "HtmlEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "HtmlEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "HtmlSnap")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "HtmlSnapComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "JsonEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "JsonEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "XmlEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "XmlEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "TextEditer")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "TextEditerTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Charset")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "EncodeComments")).append("</PRE>\n");

            s.append("    <H3>").append("BOM").append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "BOMcomments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "FindReplace")).append(" - ").append(message(lang, "Texts")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "FindReplaceTextsTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "FindReplace")).append(" - ").append(message(lang, "Bytes")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "FindReplaceBytesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "FilterLines")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "FilterTypesComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "TextFindBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "TextFindBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "TextReplaceBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "TextReplaceBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "TextToHtml")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "PasteTextAsHtml")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "BytesFindBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "BytesFindBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "TextInMyBoxClipboard")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "TextClipboardUseComments")).append("</PRE></BR>\n");
            s.append("    <PRE>").append(message(lang, "TextInMyBoxClipboardTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "WordView")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "WordViewTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message(lang, "ImageTools")).append("</H1>\n");

            s.append("    <H3>").append(message(lang, "EditImage")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageEditTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Scope")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ScopeTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "SVGEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "SVGEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageAnalyse")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageAnalyseTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageQuantization")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageQuantizationComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Dithering")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DitherComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ColorMatching")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ColorMatchComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "PremultipliedAlpha")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "PremultipliedAlphaTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Thresholding")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageThresholdingComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageRepeatTile")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageRepeatTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageSample")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageSampleTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageSplit")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageSplitTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImagesBrowser")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImagesBrowserTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImagesEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImagesEditorTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImagesPlay")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImagesPlayTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageOCR")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageOCRComments")).append("</PRE></BR>\n");
            s.append("    <PRE>").append(message(lang, "OCRPreprocessComment")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImageAlphaExtract")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ImageAlphaExtractTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ImagesInSystemClipboard")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "RecordImagesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ManageColors")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ColorsManageTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "DrawChromaticityDiagram")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ChromaticityDiagramTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "IccProfileEditor")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "IccProfileTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message(lang, "NetworkTools")).append("</H1>\n");
            s.append("    <H3>").append(message(lang, "DownloadHtmls")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DownloadFirstLevelLinksComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ConvertUrl")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ConvertUrlTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "QueryDNSBatch")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "QueryDNSBatchTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "WeiboSnap")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "WeiboAddressComments")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message(lang, "DataTools")).append("</H1>\n");
            s.append("    <H3>").append(message(lang, "Column")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ColumnComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ManageData")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataManageTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "EditCSV")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataFileCSVTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "EditExcel")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataFileExcelTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "EditTextDataFile")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataFileTextTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "DatabaseTable")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataTableTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "SqlIdentifier")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "SqlIdentifierComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "XYChart")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataChartXYTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "PieChart")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "DataChartPieTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "BoxWhiskerChart")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "BoxWhiskerChartTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ComparisonBarsChart")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ComparisonBarsChartTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "SelfComparisonBarsChart")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "SelfComparisonBarsChartTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "XYZChart")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "WebglComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "SetStyles")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "SetStylesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "SimpleLinearRegression")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "SimpleLinearRegressionTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "SimpleLinearRegressionCombination")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "SimpleLinearRegressionCombinationTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "MultipleLinearRegression")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MultipleLinearRegressionTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "MultipleLinearRegressionCombination")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MultipleLinearRegressionCombinationTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Matrix")).append("</H3>\n");

            s.append("    <H4>").append(message(lang, "Plus")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "MatricesPlusComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "Minus")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "MatricesMinusComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "Multiply")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "MatricesMultiplyComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "HadamardProduct")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "HadamardProductComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "KroneckerProduct")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "KroneckerProductComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "VerticalMerge")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "VerticalMergeComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "HorizontalMerge")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "HorizontalMergeComments")).append("</PRE>\n");

            s.append("\n");

            s.append("    <H3>").append(message(lang, "JavaScript")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "JavaScriptTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "JShell")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "JShellTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "JEXL")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "JEXLTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "MathFunction")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MathFunctionTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "GeographyCode")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "GeographyCodeEditComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "MapOptions")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MapComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ConvertCoordinate")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ConvertCoordinateTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message(lang, "MediaTools")).append("</H1>\n");
            s.append("    <H3>").append(message(lang, "MediaPlayer")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MediaPlayerSupports")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "FFmpeg")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "FFmpegExeComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "FFmpegOptions")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "FFmpegOptionsTips")).append("</PRE></BR>\n");
            s.append("    <PRE>").append(message(lang, "FFmpegArgumentsTips")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "FFmpegScreenRecorder")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "FFmpegScreenRecorderComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "CRF")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "CRFComments")).append("</PRE>\n");

            s.append("    <H4>").append(message(lang, "X264")).append("</H4>\n");
            s.append("    <PRE>").append(message(lang, "X264PresetComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "GameElimniation")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "GameEliminationComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "GameMine")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "GameMineTips")).append("</PRE>\n");

            s.append("\n");

            s.append("<H1>").append(message(lang, "Others")).append("</H1>\n");
            s.append("    <H3>").append(message(lang, "Table")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "TableTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Play")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "PlayerComments")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ManageLanguages")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "MyBoxLanguagesTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "Shortcuts")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ShortcutsTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ChildWindow")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ChildWindowTips")).append("</PRE>\n");

            s.append("    <H3>").append(message(lang, "ClearExpiredData")).append("</H3>\n");
            s.append("    <PRE>").append(message(lang, "ClearExpiredDataComments")).append("</PRE>\n");

            s.append("\n");

            s.append("</BODY>\n");

            String html = HtmlWriteTools.html(message(lang, "InterfaceTips"), HtmlStyles.DefaultStyle, s.toString());

            File file = new File(FileTmpTools.generatePath("html")
                    + "/mybox_interface_tips_" + lang + ".html");

            return TextFileTools.writeFile(file, html);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String charsetLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/charset/Charset.html";
    }

    public static String uriLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html";
    }

    public static String urlLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URL.html";
    }

    public static String javaFxCssLink() {
        return "https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html";
    }

    public static String derbyLink() {
        return "https://db.apache.org/derby/docs/10.17/ref/index.html";
    }

    public static String sqlLink() {
        return "https://db.apache.org/derby/docs/10.17/ref/crefsqlj18919.html";
    }

    public static String javaLink() {
        return "https://docs.oracle.com/javase/tutorial/java/index.html";
    }

    public static String javaAPILink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/index.html";
    }

    public static String javaMathLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html";
    }

    public static String decimalFormatLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/DecimalFormat.html";
    }

    public static String simpleDateFormatLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/SimpleDateFormat.html";
    }

    public static String renderingHintsLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/RenderingHints.html";
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

    public static String javaShape2DLink() {
        return "https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Shape.html";
    }

    public static String javafxShape2DLink() {
        return "https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/shape/Shape.html";
    }

    public static String javafxSVGPathLink() {
        return "https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/shape/SVGPath.html";
    }

    public static String expEnLink() {
        return "https://baike.baidu.com/item/%E6%AD%A3%E5%88%99%E8%A1%A8%E8%BE%BE%E5%BC%8F/1700215";
    }

    public static String expZhLink() {
        return "https://en.wikipedia.org/wiki/Regular_expression";
    }

    public static String jexlLink() {
        return "https://commons.apache.org/proper/commons-jexl/index.html";
    }

    public static String jexlRefLink() {
        return "https://commons.apache.org/proper/commons-jexl/reference/index.html";
    }

    public static String strokeLink() {
        return "https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/shape/Shape.html";
    }

    public static List<MenuItem> javaHelps() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem("Learning the Java Language");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("Java Development Kit (JDK) APIs");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaAPILink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("Full list of Math functions");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaMathLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("JavaHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("JavaHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> jexlHelps() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem("JEXL Overview");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jexlLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("JEXL Reference");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jexlRefLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("Java Development Kit (JDK) APIs");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaAPILink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("JexlHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("JexlHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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

    public static List<MenuItem> javascriptHelps() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("JavaScriptTutorial") + " - " + message("English"));
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

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("JavaScriptHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("JavaScriptHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> rowExpressionHelps() {
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

    public static List<MenuItem> svgPathHelps() {
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

            menuItem = new MenuItem("SVGPath in JavaFX");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javafxSVGPathLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("Shape 2D in JavaFX");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javafxShape2DLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem("Shape 2D in Java");
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.javaShape2DLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("SvgPathHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("SvgPathHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);
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
                    WebBrowserController.openAddress("https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/paint/Color.html", true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("ColorSpace"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/color/ColorSpace.html", true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("ColorModels"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress("https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/image/ColorModel.html", true);
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
            values.put("M x,y ", message("SvgPathM"));
            values.put("m dx,dy ", message("SvgPathm"));
            values.put("L x,y ", message("SvgPathL"));
            values.put("l dx,dy ", message("SvgPathl"));
            values.put("H x ", message("SvgPathH"));
            values.put("h dx ", message("SvgPathh"));
            values.put("V y ", message("SvgPathV"));
            values.put("v dy ", message("SvgPathv"));
            values.put("Q x1,y1 x,y ", message("SvgPathQ"));
            values.put("q dx1,dy1 dx,dy ", message("SvgPathq"));
            values.put("T x,y ", message("SvgPathT"));
            values.put("t dx,dy ", message("SvgPatht"));
            values.put("C x1,y1 x2,y2 x,y ", message("SvgPathC"));
            values.put("c dx1,dy1 dx2,dy2 dx,dy ", message("SvgPathc"));
            values.put("S x2,y2 x,y ", message("SvgPathS"));
            values.put("s dx2,dy2 dx,dy ", message("SvgPaths"));
            values.put("A rx ry angle large-arc-flag sweep-flag x,y ", message("SvgPathA"));
            values.put("s rx ry angle large-arc-flag sweep-flag x,y ", message("SvgPatha"));
            values.put("Z ", message("SvgPathZ"));

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
            values.put("fill: rgba(0,50,100,0.5); ", message("FilledColor") + " - RGBA");
            values.put("fill: none; ", message("FilledColor") + " - " + message("None"));
            values.put("fill-opacity: 0.3; ", message("FillOpacity"));
            values.put("stroke: black; ", message("StrokeColor") + " - " + message("ColorCode"));
            values.put("stroke: rgb(0,128,0); ", message("StrokeColor") + " - RGB");
            values.put("stroke: hsla(60,80%,90%,0.6); ", message("StrokeColor") + " - HSLA");
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
