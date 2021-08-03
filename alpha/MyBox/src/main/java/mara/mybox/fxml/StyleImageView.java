package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleImageView {

    public static StyleData imageViewStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("rightTips")) {
            return new StyleData(id, "", "", "iconTipsRight.png");
        }
        if (id.startsWith("sample")) {
            return new StyleData(id, "", "", "iconSampled.png");
        }
        if (id.startsWith("leftPane")) {
            return new StyleData(id, "", "", "iconDoubleLeft.png");
        }
        if (id.startsWith("rightPane")) {
            return new StyleData(id, "", "", "iconDoubleRight.png");
        }
        if (id.startsWith("scopePane")) {
            return new StyleData(id, "", "", "iconDoubleLeft.png");
        }
        if (id.startsWith("imagePane")) {
            return new StyleData(id, "", "", "iconDoubleRight.png");
        }
        if (id.startsWith("links")) {
            return new StyleData(id, "", Languages.message("Links"), "iconLink.png");
        }
        if (id.toLowerCase().endsWith("tipsview")) {
            switch (id) {
                case "refTipsView":
                    return new StyleData(id, "", Languages.message("ImageRefTips"), "", "iconTips.png");
                case "distanceTipsView":
                    return new StyleData(id, "", Languages.message("ColorMatchComments"), "", "iconTips.png");
                case "BWThresholdTipsView":
                    return new StyleData(id, "", Languages.message("BWThresholdComments"), "", "iconTips.png");
                case "pdfMemTipsView":
                    return new StyleData(id, "", Languages.message("PdfMemComments"), "", "iconTips.png");
                case "pdfPageSizeTipsView":
                    return new StyleData(id, "", Languages.message("PdfPageSizeComments"), "", "iconTips.png");
                case "preAlphaTipsView":
                    return new StyleData(id, "", Languages.message("PremultipliedAlphaTips"), "", "iconTips.png");
                case "thresholdingTipsView":
                    return new StyleData(id, "", Languages.message("ThresholdingComments"), "", "iconTips.png");
                case "quantizationTipsView":
                    return new StyleData(id, "", Languages.message("QuantizationComments"), "", "iconTips.png");
                default:
                    return new StyleData(id, "", "", "iconTips.png");
            }
        }
        return null;
    }

}
