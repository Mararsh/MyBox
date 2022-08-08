package mara.mybox.fxml;

import java.io.File;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-8
 * @License Apache License Version 2.0
 */
public class HelpTools {

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

    public static File aboutLinearRegressionHtml() {
        try {
            StringTable table = new StringTable(null, message("AboutLinearRegression"));
            table.newLinkRow(message("Guide"), "https://www.itl.nist.gov/div898/handbook/");
            table.newLinkRow("", "https://book.douban.com/subject/10956491/");
            table.newLinkRow(message("Video"), "https://www.bilibili.com/video/BV1Ua4y1e7YG");
            table.newLinkRow("", "https://www.bilibili.com/video/BV1i7411d7aP");
            table.newLinkRow(message("Example"), "https://www.xycoon.com/simple_linear_regression.htm");
            table.newLinkRow("", "https://www.scribbr.com/statistics/simple-linear-regression/");
            table.newLinkRow("", "http://www.datasetsanalysis.com/regressions/simple-linear-regression.html");
            table.newLinkRow(message("Dataset"), "http://archive.ics.uci.edu/ml/datasets/Iris");
            table.newLinkRow("", "https://github.com/tomsharp/SVR/tree/master/data");
            table.newLinkRow("", "https://github.com/krishnaik06/simple-Linear-Regression");
            table.newLinkRow("", "https://github.com/susanli2016/Machine-Learning-with-Python/tree/master/data");
            table.newLinkRow("Apache-Math", "https://commons.apache.org/proper/commons-math/");
            table.newLinkRow("", "https://commons.apache.org/proper/commons-math/apidocs/index.html");

            File htmFile = HtmlWriteTools.writeHtml(table.html());
            return htmFile;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
