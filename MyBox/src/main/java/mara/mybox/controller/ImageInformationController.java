package mara.mybox.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import mara.mybox.controller.base.BaseController;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageInformationPng;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageInformationController extends BaseController {

    private ImageInformation imageInfo;

    @FXML
    private TextArea infoArea;
    @FXML
    private CheckBox consoleCheck, rawCheck;
    @FXML
    private HBox iccBox;
    @FXML
    private ComboBox<String> indexSelector;

    public ImageInformationController() {
        baseTitle = message("ImageInformation");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            consoleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    AppVaribles.setUserConfigValue("ImageInformationConsoleKey", consoleCheck.isSelected());
                    if (consoleCheck.isSelected()) {
                        // https://stackoverflow.com/questions/36423200/textarea-javafx-color
                        infoArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: #66FF66;");
                    } else {
                        infoArea.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; ");
                    }
                }
            });
            consoleCheck.setSelected(AppVaribles.getUserConfigBoolean("ImageInformationConsoleKey", false));

            rawCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    AppVaribles.setUserConfigValue("ImageInformationRawKey", rawCheck.isSelected());
                    loadImageFileInformation(imageInfo);
                }
            });
            rawCheck.setSelected(AppVaribles.getUserConfigBoolean("ImageInformationRawKey", false));

            iccBox.setVisible(false);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadImageFileInformation(ImageInformation info) {
        try {
            if (info == null) {
                return;
            }
            imageInfo = info;
            indexSelector.getItems().clear();

            ImageFileInformation finfo = info.getImageFileInformation();
            File file = finfo.getFile();
            // https://stackoverflow.com/questions/3747860/style-a-jtextpane-to-have-console-like-formatting
            // https://stackoverflow.com/questions/16279781/getting-jtextarea-to-display-fixed-width-font-without-antialiasing
            // https://stackoverflow.com/questions/10774825/how-to-format-text-for-printing-in-java-using-printer-api/10775063?r=SearchResults#10775063
//            infoArea.setFont(Font.font("monospaced")); // "Courier New"
            infoArea.setText(sLine("FilesPath", file.getParent()));
            infoArea.appendText(sLine("FileName", file.getName()));
            infoArea.appendText(sLine("FileSize", FileTools.showFileSize(finfo.getFileSize())));
            infoArea.appendText(sLine("CreateTime", DateTools.datetimeToString(finfo.getCreateTime())));
            infoArea.appendText(sLine("ModifyTime", DateTools.datetimeToString(finfo.getModifyTime())));
            infoArea.appendText(sLine("Format", finfo.getImageFormat()));
            infoArea.appendText(sLine("NumberOfImagesInFile", finfo.getNumberOfImages() + ""));

            for (int i = 0; i < finfo.getImagesInformation().size(); i++) {
                ImageInformation iInfo = finfo.getImagesInformation().get(i);
                infoArea.appendText("\n----------------------- " + message("Image") + " " + (i + 1) + " -----------------------\n");
                if (iInfo.getIccProfile() != null) {
                    indexSelector.getItems().add((i + 1) + "");
                }
                loadImageInformation(iInfo);
            }

            if (indexSelector.getItems().isEmpty()) {
                iccBox.setVisible(false);
            } else {
                iccBox.setVisible(true);
                indexSelector.getSelectionModel().selectFirst();
            }

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            myStage.toFront();
                            infoArea.home();
                            infoArea.requestFocus();
                        }
                    });
                }
            }, 1000);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadImageInformation(ImageInformation imageInfo) {
        try {

            loadStandardInformation(imageInfo);
            switch (imageInfo.getImageFormat()) {
                case "png":
                    loadPngInformation(imageInfo);
                default:
                    loadNativeAttributes(imageInfo);

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadStandardInformation(ImageInformation imageInfo) {
        try {
            infoArea.appendText(sLine("xPixels", imageInfo.getWidth()));
            infoArea.appendText(sLine("yPixels", imageInfo.getHeight()));

//            commonKeys.addAll(Arrays.asList("xDpi", "yDpi", "ColorSpace", "ColorChannels",
//                    "BitDepth", "BitsPerSample", "Gamma", "BlackIsZero", "PaletteSize", "BackgroundIndex",
//                    "BackgroundColor", "CompressionType", "IsLossless", "NumProgressiveScans",
//                    "BitRate", "PlanarConfiguration", "SampleFormat", "SignificantBitsPerSample",
//                    "SampleMSB", "PixelAspectRatio", "ImageRotation", "HorizontalPixelSize",
//                    "VerticalPixelSize", "HorizontalPhysicalPixelSpacing", "VerticalPhysicalPixelSpacing",
//                    "HorizontalPosition", "VerticalPosition", "HorizontalPixelOffset", "VerticalPixelOffset",
//                    "HorizontalScreenSize", "VerticalScreenSize", "FormatVersion", "SubimageInterpretation",
//                    "ImageCreationTime", "ImageModificationTime", "Alpha", "TransparentIndex", "TransparentColor"));
            if (imageInfo.getXDpi() > 0) {
                infoArea.appendText(sLine("xDensity", imageInfo.getXDpi() + " dpi"));
                float xinch = imageInfo.getWidth() / imageInfo.getXDpi();
                infoArea.appendText(sLine("xSize", xinch + " " + message("inches")
                        + " = " + (xinch * 2.54) + " " + message("centimetres")));
            }
            if (imageInfo.getYDpi() > 0) {
                infoArea.appendText(sLine("yDensity", imageInfo.getYDpi() + " dpi"));
                float yinch = imageInfo.getHeight() / imageInfo.getYDpi();
                infoArea.appendText(sLine("ySize", yinch + " " + message("inches")
                        + " = " + (yinch * 2.54) + " " + message("centimetres")));
            }
            infoArea.appendText(sLine("ColorSpace", imageInfo.getColorSpace()));
            infoArea.appendText(sLine("ColorChannels", imageInfo.getColorChannels()));
            if (imageInfo.getBitDepth() > 0) {
                infoArea.appendText(sLine("BitDepth", imageInfo.getBitDepth()));
            }
            if (imageInfo.getBitsPerSample() != null) {
                infoArea.appendText(sLine("BitsPerSample", imageInfo.getBitsPerSample()));
            }
            if (imageInfo.getGamma() > 0) {
                infoArea.appendText(sLine("Gamma", imageInfo.getGamma()));
            }
            infoArea.appendText(sLine("BlackIsZero", message(imageInfo.isBlackIsZero() + "")));
            if (imageInfo.getStandardIntAttribute("PaletteSize") > 0) {
                infoArea.appendText(sLine("PaletteSize", imageInfo.getStandardIntAttribute("PaletteSize")));
            }
            if (imageInfo.getBackgroundIndex() > 0) {
                infoArea.appendText(sLine("BackgroundIndex", imageInfo.getBackgroundIndex()));
            }
            if (imageInfo.getBackgroundColor() != null) {
                infoArea.appendText(sLine("BackgroundColor", imageInfo.getBackgroundColor().toString()));
            }
            if (imageInfo.getCompressionType() != null) {
                infoArea.appendText(sLine("CompressionType", imageInfo.getCompressionType()));
                infoArea.appendText(sLine("LosslessCompression", message(imageInfo.isIsLossless() + "")));
            }
            if (imageInfo.getNumProgressiveScans() > 0) {
                infoArea.appendText(sLine("NumProgressiveScans", imageInfo.getNumProgressiveScans()));
            }
            if (imageInfo.getBitRate() > 0) {
                infoArea.appendText(sLine("BitRate", imageInfo.getBitRate()));
            }
            if (imageInfo.getPlanarConfiguration() != null) {
                infoArea.appendText(sLine("PlanarConfiguration", imageInfo.getPlanarConfiguration()));
            }
            if (imageInfo.getSampleFormat() != null) {
                infoArea.appendText(sLine("SampleFormat", imageInfo.getSampleFormat()));
            }
            if (imageInfo.getSignificantBitsPerSample() != null) {
                infoArea.appendText(sLine("SignificantBitsPerSample", imageInfo.getSignificantBitsPerSample()));
            }
            if (imageInfo.getSampleMSB() != null) {
                infoArea.appendText(sLine("SampleMSB", imageInfo.getSampleMSB()));
            }
            if (imageInfo.getPixelAspectRatio() > 0) {
                infoArea.appendText(sLine("PixelAspectRatio", imageInfo.getPixelAspectRatio()));
            }
            if (imageInfo.getImageRotation() != null) {
                infoArea.appendText(sLine("ImageOrientation", imageInfo.getImageRotation()));
            }
            if (imageInfo.getHorizontalPixelSize() > 0) {
                infoArea.appendText(sLine("HorizontalPixelSize", imageInfo.getHorizontalPixelSize()));
            }
            if (imageInfo.getVerticalPixelSize() > 0) {
                infoArea.appendText(sLine("VerticalPixelSize", imageInfo.getVerticalPixelSize()));
            }
            if (imageInfo.getHorizontalPhysicalPixelSpacing() > 0) {
                infoArea.appendText(sLine("HorizontalPhysicalPixelSpacing", imageInfo.getHorizontalPhysicalPixelSpacing()));
            }
            if (imageInfo.getVerticalPhysicalPixelSpacing() > 0) {
                infoArea.appendText(sLine("VerticalPhysicalPixelSpacing", imageInfo.getVerticalPhysicalPixelSpacing()));
            }
            if (imageInfo.getHorizontalPosition() > 0) {
                infoArea.appendText(sLine("HorizontalPosition", imageInfo.getHorizontalPosition()));
            }
            if (imageInfo.getVerticalPosition() > 0) {
                infoArea.appendText(sLine("VerticalPosition", imageInfo.getVerticalPosition()));
            }
            if (imageInfo.getHorizontalPixelOffset() > 0) {
                infoArea.appendText(sLine("HorizontalPixelOffset", imageInfo.getHorizontalPixelOffset()));
            }
            if (imageInfo.getVerticalPixelOffset() > 0) {
                infoArea.appendText(sLine("VerticalPixelOffset", imageInfo.getVerticalPixelOffset()));
            }
            if (imageInfo.getHorizontalScreenSize() > 0) {
                infoArea.appendText(sLine("HorizontalScreenSize", imageInfo.getHorizontalScreenSize()));
            }
            if (imageInfo.getVerticalScreenSize() > 0) {
                infoArea.appendText(sLine("VerticalScreenSize", imageInfo.getVerticalScreenSize()));
            }
            if (imageInfo.getFormatVersion() != null) {
                infoArea.appendText(sLine("FormatVersion", imageInfo.getFormatVersion()));
            }
            if (imageInfo.getSubimageInterpretation() != null) {
                infoArea.appendText(sLine("SubimageInterpretation", imageInfo.getSubimageInterpretation()));
            }
            if (imageInfo.getImageCreationTime() != null) {
                infoArea.appendText(sLine("ImageCreationTime", imageInfo.getImageCreationTime()));
            }
            if (imageInfo.getImageModificationTime() != null) {
                infoArea.appendText(sLine("ImageModificationTime", imageInfo.getImageModificationTime()));
            }
            infoArea.appendText(sLine("AlphaChannel", imageInfo.getAlpha()));
            if (imageInfo.getTransparentIndex() > 0) {
                infoArea.appendText(sLine("TransparentIndex", imageInfo.getTransparentIndex()));
            }
            if (imageInfo.getTransparentColor() != null) {
                infoArea.appendText(sLine("TransparentColor", imageInfo.getTransparentColor()));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadPngInformation(ImageInformation imageInfo) {
        try {
            ImageInformationPng pngInfo = (ImageInformationPng) imageInfo;
            if (pngInfo.getColorType() != null) {
                infoArea.appendText(sLine("ColorType", pngInfo.getColorType()));
            }

            if (pngInfo.getCompressionMethod() != null) {
                infoArea.appendText(sLine("CompressionMethod", pngInfo.getCompressionMethod()));
            }
            if (pngInfo.getFilterMethod() != null) {
                infoArea.appendText(sLine("FilterMethod", pngInfo.getFilterMethod()));
            }
            if (pngInfo.getInterlaceMethod() != null) {
                infoArea.appendText(sLine("InterlaceMethod", pngInfo.getInterlaceMethod()));
            }
            if (pngInfo.getUnitSpecifier() != null) {
                infoArea.appendText(sLine("UnitSpecifier", pngInfo.getUnitSpecifier()));
            }
            if (pngInfo.getPixelsPerUnitXAxis() > 0) {
                infoArea.appendText(sLine("PixelsPerUnitXAxis", pngInfo.getPixelsPerUnitXAxis()));
            }
            if (pngInfo.getPixelsPerUnitYAxis() > 0) {
                infoArea.appendText(sLine("PixelsPerUnitYAxis", pngInfo.getPixelsPerUnitYAxis()));
            }
            if (pngInfo.getPngPaletteSize() > 0) {
                infoArea.appendText(sLine("PngPaletteSize", pngInfo.getPngPaletteSize()));
            }
            if (pngInfo.getbKGD_Grayscale() >= 0) {
                infoArea.appendText(sLine("BKGD_Grayscale", pngInfo.getbKGD_Grayscale()));
            }
            if (pngInfo.getbKGD_RGB() != null) {
                infoArea.appendText(sLine("BKGD_RGB", pngInfo.getbKGD_RGB().toString()));
            }
            if (pngInfo.getbKGD_Palette() >= 0) {
                infoArea.appendText(sLine("BKGD_Palette", pngInfo.getbKGD_Palette()));
            }
            if (pngInfo.getWhite() != null) {
                infoArea.appendText(sLine("White", pngInfo.getWhite().getNormalizedX() + "," + pngInfo.getWhite().getNormalizedY()));
            }
            if (pngInfo.getRed() != null) {
                infoArea.appendText(sLine("Red", pngInfo.getRed().getNormalizedX() + "," + pngInfo.getRed().getNormalizedY()));
            }
            if (pngInfo.getGreen() != null) {
                infoArea.appendText(sLine("Green", pngInfo.getGreen().getNormalizedX() + "," + pngInfo.getGreen().getNormalizedY()));
            }
            if (pngInfo.getBlue() != null) {
                infoArea.appendText(sLine("Blue", pngInfo.getBlue().getNormalizedX() + "," + pngInfo.getBlue().getNormalizedY()));
            }
            if (pngInfo.getProfileName() != null) {
                infoArea.appendText(sLine("ProfileName", pngInfo.getProfileName()));
                infoArea.appendText(sLine("ProfileCompressionMethod", pngInfo.getProfileCompressionMethod()));
                infoArea.appendText(sLine("IccProfile", pngInfo.getIccProfile().length));
            }
            if (pngInfo.getsBIT_Grayscale() >= 0) {
                infoArea.appendText(sLine("sBIT_Grayscale", pngInfo.getsBIT_Grayscale()));
            }
            if (pngInfo.getsBIT_GrayAlpha_alpha() >= 0) {
                infoArea.appendText(sLine("sBIT_GrayAlpha", pngInfo.getsBIT_GrayAlpha_gray() + " " + pngInfo.getsBIT_GrayAlpha_alpha()));
            }
            if (pngInfo.getsBIT_RGB_red() >= 0) {
                infoArea.appendText(sLine("sBIT_RGB", pngInfo.getsBIT_RGB_red() + " " + pngInfo.getsBIT_RGB_green() + " " + pngInfo.getsBIT_RGB_blue()));
            }
            if (pngInfo.getsBIT_RGBAlpha_red() >= 0) {
                infoArea.appendText(sLine("sBIT_RGBAlpha",
                        pngInfo.getsBIT_RGBAlpha_red() + " " + pngInfo.getsBIT_RGBAlpha_green()
                        + " " + pngInfo.getsBIT_RGBAlpha_blue() + " " + pngInfo.getsBIT_RGBAlpha_alpha()));
            }
            if (pngInfo.getsBIT_Palette_red() >= 0) {
                infoArea.appendText(sLine("sBIT_Palette",
                        +pngInfo.getsBIT_Palette_red() + " " + pngInfo.getsBIT_Palette_green() + " " + pngInfo.getsBIT_Palette_blue()));
            }
            if (pngInfo.getSuggestedPaletteSize() > 0) {
                infoArea.appendText(sLine("SuggestedPaletteSize", pngInfo.getSuggestedPaletteSize()));
            }
            if (pngInfo.getRenderingIntent() != null) {
                infoArea.appendText(sLine("RenderingIntent", pngInfo.getRenderingIntent()));
            }
            if (pngInfo.gettRNS_Grayscale() >= 0) {
                infoArea.appendText(sLine("tRNS_Grayscale", pngInfo.gettRNS_Grayscale()));
            }
            if (pngInfo.gettRNS_RGB() != null) {
                infoArea.appendText(sLine("tRNS_RGB", pngInfo.gettRNS_RGB().toString()));
            }
            if (pngInfo.gettRNS_Palette_index() >= 0) {
                infoArea.appendText(sLine("tRNS_Palette",
                        +pngInfo.gettRNS_Palette_index() + " " + pngInfo.gettRNS_Palette_alpha()));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadNativeAttributes(ImageInformation imageInfo) {
        try {
            LinkedHashMap<String, Object> attributes = imageInfo.getNativeAttributes();
            if (attributes == null) {
                return;
            }
            for (String key : attributes.keySet()) {
                infoArea.appendText(sLine(key, attributes.get(key)));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private String sLine(String name, Object value) {
        if (rawCheck.isSelected()) {
            return String.format("%-" + 35 + "s : %s", name, value) + "\n";
        } else {
            return String.format("%-" + 35 + "s : %s", message(name), value) + "\n";
        }
    }

    @FXML
    public void viewAction() {
        try {
            int imageIndex = Integer.parseInt(indexSelector.getSelectionModel().getSelectedItem());
            ImageInformation iInfo = imageInfo.getImageFileInformation().getImagesInformation().get(imageIndex - 1);
            if (iInfo.getIccProfile() == null) {
                indexSelector.getItems().remove(indexSelector.getSelectionModel().getSelectedIndex());
                return;
            }

            IccProfileEditorController controller
                    = (IccProfileEditorController) openStage(CommonValues.IccProfileEditorFxml);
            String name = message("File") + " : " + imageInfo.getFileName() + "  "
                    + message("Image") + " " + imageIndex;
            controller.externalData(name, iInfo.getIccProfile());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
