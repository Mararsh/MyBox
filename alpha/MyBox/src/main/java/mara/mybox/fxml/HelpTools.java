package mara.mybox.fxml;

import java.io.File;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.data.ImageItem;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-8
 * @License Apache License Version 2.0
 */
public class HelpTools {

    public static void about() {
        try {
            StringTable table = new StringTable(null, "MyBox");
            table.newNameValueRow("Author", "Mara");
            table.newNameValueRow("Version", AppValues.AppVersion);
            table.newNameValueRow("Date", AppValues.AppVersionDate);
            table.newNameValueRow("License", Languages.message("FreeOpenSource"));
            table.newLinkRow("", "https://www.apache.org/licenses/LICENSE-2.0");
            table.newLinkRow("MainPage", "https://github.com/Mararsh/MyBox");
            table.newLinkRow("Mirror", "https://sourceforge.net/projects/mara-mybox/files/");
            table.newLinkRow("LatestRelease", "https://github.com/Mararsh/MyBox/releases");
            table.newLinkRow("KnownIssues", "https://github.com/Mararsh/MyBox/issues");
            table.newNameValueRow("", Languages.message("WelcomePR"));
            table.newLinkRow("CloudStorage", "https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F");
            table.newLinkRow("MyBoxInternetDataPath", "https://github.com/Mararsh/MyBox_data");
            File htmFile = HtmlWriteTools.writeHtml(table.html());
            if (htmFile == null || !htmFile.exists()) {
                return;
            }
            SoundTools.miao5();
            WebBrowserController.oneOpen(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            StringTable table = new StringTable(null, Languages.message("AboutCoordinateSystem"));
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
            StringTable table = new StringTable(null, Languages.message("AboutMedia"));
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

    public static void imageStories(BaseController controller) {
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
                table.newNameValueRow(
                        "<Img src='" + file.toURI().toString() + "' width=" + item.getWidth() + ">",
                        comments);
            }
            File htmFile = HtmlWriteTools.writeHtml(table.html());
            if (htmFile == null || !htmFile.exists()) {
                return;
            }
            controller.browse(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
