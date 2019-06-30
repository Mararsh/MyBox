package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageInformationPng;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageInformationController extends BaseController {

    @FXML
    private TextArea infoArea;

    public ImageInformationController() {
        baseTitle = AppVaribles.getMessage("ImageInformation");

    }

    public void loadImageFileInformation(ImageInformation info) {
        try {

            ImageFileInformation finfo = info.getImageFileInformation();
            File file = finfo.getFile();
            infoArea.setText(getMessage("FilesPath") + ": " + file.getParent() + "\n");
            infoArea.appendText(getMessage("FileName") + ": " + file.getName() + "\n");
            infoArea.appendText(getMessage("FileSize") + ": " + FileTools.showFileSize(finfo.getFileSize()) + "\n");
            infoArea.appendText(getMessage("CreateTime") + ": " + DateTools.datetimeToString(finfo.getCreateTime()) + "\n");
            infoArea.appendText(getMessage("ModifyTime") + ": " + DateTools.datetimeToString(finfo.getModifyTime()) + "\n");
            infoArea.appendText(getMessage("Format") + ": " + finfo.getImageFormat() + "\n");
            infoArea.appendText(getMessage("NumberOfImagesInFile") + ": " + finfo.getNumberOfImages() + "\n");
            int count = 0;
            for (ImageInformation imageInfo : finfo.getImagesInformation()) {
                infoArea.appendText("\n----------------------- " + getMessage("Image") + " " + (++count) + " -----------------------\n");
                loadImageInformation(imageInfo);
            }
            myStage.setAlwaysOnTop(true);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
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
            loadImageCommonInformation(imageInfo);
            switch (imageInfo.getImageFormat()) {
                case "png":
                    loadPngInformation(imageInfo);
                default:
                    loadAttributes(imageInfo);

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadImageCommonInformation(ImageInformation imageInfo) {
        try {
            infoArea.appendText(getMessage("xPixels") + ": " + imageInfo.getWidth() + "\n");
            infoArea.appendText(getMessage("yPixels") + ": " + imageInfo.getHeight() + "\n");

            if (imageInfo.getXDpi() > 0) {
                infoArea.appendText(getMessage("xDensity") + ": " + imageInfo.getXDpi() + " dpi" + "\n");
                float xinch = imageInfo.getWidth() / imageInfo.getXDpi();
                infoArea.appendText(getMessage("xSize") + ": " + xinch + " " + AppVaribles.getMessage("inches")
                        + " = " + (xinch * 2.54) + " " + AppVaribles.getMessage("centimetres") + "\n");
            }
            if (imageInfo.getYDpi() > 0) {
                infoArea.appendText(getMessage("yDensity") + ": " + imageInfo.getYDpi() + " dpi" + "\n");
                float yinch = imageInfo.getHeight() / imageInfo.getYDpi();
                infoArea.appendText(getMessage("ySize") + ": " + yinch + " " + AppVaribles.getMessage("inches")
                        + " = " + (yinch * 2.54) + " " + AppVaribles.getMessage("centimetres") + "\n");
            }
            infoArea.appendText(getMessage("ColorSpace") + ": " + imageInfo.getColorSpace() + "\n");
            infoArea.appendText(getMessage("ColorChannels") + ": " + imageInfo.getColorChannels() + "\n");
            if (imageInfo.getBitDepth() > 0) {
                infoArea.appendText(getMessage("BitDepth") + ": " + imageInfo.getBitDepth() + "\n");
            }
            if (imageInfo.getBitsPerSample() != null) {
                infoArea.appendText(getMessage("BitsPerSample") + ": " + imageInfo.getBitsPerSample() + "\n");
            }
            if (imageInfo.getGamma() > 0) {
                infoArea.appendText(getMessage("Gamma") + ": " + imageInfo.getGamma() + "\n");
            }
            infoArea.appendText(getMessage("BlackIsZero") + ": " + AppVaribles.getMessage(imageInfo.isBlackIsZero() + "") + "\n");
            if (imageInfo.getIntAttribute("PaletteSize") > 0) {
                infoArea.appendText(getMessage("PaletteSize") + ": " + imageInfo.getIntAttribute("PaletteSize") + "\n");
            }
            if (imageInfo.getBackgroundIndex() > 0) {
                infoArea.appendText(getMessage("BackgroundIndex") + ": " + imageInfo.getBackgroundIndex() + "\n");
            }
            if (imageInfo.getBackgroundColor() != null) {
                infoArea.appendText(getMessage("BackgroundColor") + ": " + imageInfo.getBackgroundColor().toString() + "\n");
            }
            if (imageInfo.getCompressionType() != null) {
                infoArea.appendText(getMessage("CompressionType") + ": " + imageInfo.getCompressionType() + "\n");
                infoArea.appendText(getMessage("LosslessCompression") + ": " + AppVaribles.getMessage(imageInfo.isIsLossless() + "") + "\n");
            }
            if (imageInfo.getNumProgressiveScans() > 0) {
                infoArea.appendText(getMessage("NumProgressiveScans") + ": " + imageInfo.getNumProgressiveScans() + "\n");
            }
            if (imageInfo.getBitRate() > 0) {
                infoArea.appendText(getMessage("BitRate") + ": " + imageInfo.getBitRate() + "\n");
            }
            if (imageInfo.getPlanarConfiguration() != null) {
                infoArea.appendText(getMessage("PlanarConfiguration") + ": " + imageInfo.getPlanarConfiguration() + "\n");
            }
            if (imageInfo.getSampleFormat() != null) {
                infoArea.appendText(getMessage("SampleFormat") + ": " + imageInfo.getSampleFormat() + "\n");
            }
            if (imageInfo.getSignificantBitsPerSample() != null) {
                infoArea.appendText(getMessage("SignificantBitsPerSample") + ": " + imageInfo.getSignificantBitsPerSample() + "\n");
            }
            if (imageInfo.getSampleMSB() != null) {
                infoArea.appendText(getMessage("SampleMSB") + ": " + imageInfo.getSampleMSB() + "\n");
            }
            if (imageInfo.getPixelAspectRatio() > 0) {
                infoArea.appendText(getMessage("PixelAspectRatio") + ": " + imageInfo.getPixelAspectRatio() + "\n");
            }
            if (imageInfo.getImageRotation() != null) {
                infoArea.appendText(getMessage("ImageOrientation") + ": " + imageInfo.getImageRotation() + "\n");
            }
            if (imageInfo.getHorizontalPixelSize() > 0) {
                infoArea.appendText(getMessage("HorizontalPixelSize") + ": " + imageInfo.getHorizontalPixelSize() + "\n");
            }
            if (imageInfo.getVerticalPixelSize() > 0) {
                infoArea.appendText(getMessage("VerticalPixelSize") + ": " + imageInfo.getVerticalPixelSize() + "\n");
            }
            if (imageInfo.getHorizontalPhysicalPixelSpacing() > 0) {
                infoArea.appendText(getMessage("HorizontalPhysicalPixelSpacing") + ": " + imageInfo.getHorizontalPhysicalPixelSpacing() + "\n");
            }
            if (imageInfo.getVerticalPhysicalPixelSpacing() > 0) {
                infoArea.appendText(getMessage("VerticalPhysicalPixelSpacing") + ": " + imageInfo.getVerticalPhysicalPixelSpacing() + "\n");
            }
            if (imageInfo.getHorizontalPosition() > 0) {
                infoArea.appendText(getMessage("HorizontalPosition") + ": " + imageInfo.getHorizontalPosition() + "\n");
            }
            if (imageInfo.getVerticalPosition() > 0) {
                infoArea.appendText(getMessage("VerticalPosition") + ": " + imageInfo.getVerticalPosition() + "\n");
            }
            if (imageInfo.getHorizontalPixelOffset() > 0) {
                infoArea.appendText(getMessage("HorizontalPixelOffset") + ": " + imageInfo.getHorizontalPixelOffset() + "\n");
            }
            if (imageInfo.getVerticalPixelOffset() > 0) {
                infoArea.appendText(getMessage("VerticalPixelOffset") + ": " + imageInfo.getVerticalPixelOffset() + "\n");
            }
            if (imageInfo.getHorizontalScreenSize() > 0) {
                infoArea.appendText(getMessage("HorizontalScreenSize") + ": " + imageInfo.getHorizontalScreenSize() + "\n");
            }
            if (imageInfo.getVerticalScreenSize() > 0) {
                infoArea.appendText(getMessage("VerticalScreenSize") + ": " + imageInfo.getVerticalScreenSize() + "\n");
            }
            if (imageInfo.getFormatVersion() != null) {
                infoArea.appendText(getMessage("FormatVersion") + ": " + imageInfo.getFormatVersion() + "\n");
            }
            if (imageInfo.getSubimageInterpretation() != null) {
                infoArea.appendText(getMessage("SubimageInterpretation") + ": " + imageInfo.getSubimageInterpretation() + "\n");
            }
            if (imageInfo.getImageCreationTime() != null) {
                infoArea.appendText(getMessage("ImageCreationTime") + ": " + imageInfo.getImageCreationTime() + "\n");
            }
            if (imageInfo.getImageModificationTime() != null) {
                infoArea.appendText(getMessage("ImageModificationTime") + ": " + imageInfo.getImageModificationTime() + "\n");
            }
            infoArea.appendText(getMessage("AlphaChannel") + ": " + imageInfo.getAlpha() + "\n");
            if (imageInfo.getTransparentIndex() > 0) {
                infoArea.appendText(getMessage("TransparentIndex") + ": " + imageInfo.getTransparentIndex() + "\n");
            }
            if (imageInfo.getTransparentColor() != null) {
                infoArea.appendText(getMessage("TransparentColor") + ": " + imageInfo.getTransparentColor() + "\n");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadPngInformation(ImageInformation imageInfo) {
        try {
            ImageInformationPng pngInfo = (ImageInformationPng) imageInfo;
            if (pngInfo.getColorType() != null) {
                infoArea.appendText(getMessage("ColorType") + ": " + pngInfo.getColorType() + "\n");
            }
            if (pngInfo.getCompressionMethod() != null) {
                infoArea.appendText(getMessage("CompressionMethod") + ": " + pngInfo.getCompressionMethod() + "\n");
            }
            if (pngInfo.getFilterMethod() != null) {
                infoArea.appendText(getMessage("FilterMethod") + ": " + pngInfo.getFilterMethod() + "\n");
            }
            if (pngInfo.getInterlaceMethod() != null) {
                infoArea.appendText(getMessage("InterlaceMethod") + ": " + pngInfo.getInterlaceMethod() + "\n");
            }
            if (pngInfo.getUnitSpecifier() != null) {
                infoArea.appendText(getMessage("UnitSpecifier") + ": " + pngInfo.getUnitSpecifier() + "\n");
            }
            if (pngInfo.getPixelsPerUnitXAxis() > 0) {
                infoArea.appendText(getMessage("PixelsPerUnitXAxis") + ": " + pngInfo.getPixelsPerUnitXAxis() + "\n");
            }
            if (pngInfo.getPixelsPerUnitYAxis() > 0) {
                infoArea.appendText(getMessage("PixelsPerUnitYAxis") + ": " + pngInfo.getPixelsPerUnitYAxis() + "\n");
            }
            if (pngInfo.getPngPaletteSize() > 0) {
                infoArea.appendText(getMessage("PngPaletteSize") + ": " + pngInfo.getPngPaletteSize() + "\n");
            }
            if (pngInfo.getbKGD_Grayscale() >= 0) {
                infoArea.appendText(getMessage("BKGD_Grayscale") + ": " + pngInfo.getbKGD_Grayscale() + "\n");
            }
            if (pngInfo.getbKGD_RGB() != null) {
                infoArea.appendText(getMessage("BKGD_RGB") + ": " + pngInfo.getbKGD_RGB().toString() + "\n");
            }
            if (pngInfo.getbKGD_Palette() >= 0) {
                infoArea.appendText(getMessage("BKGD_Palette") + ": " + pngInfo.getbKGD_Palette() + "\n");
            }
            if (pngInfo.getWhite() != null) {
                infoArea.appendText(getMessage("White") + ": "
                        + pngInfo.getWhite().getNormalizedX() + "," + pngInfo.getWhite().getNormalizedY() + "\n");
            }
            if (pngInfo.getRed() != null) {
                infoArea.appendText(getMessage("Red") + ": "
                        + pngInfo.getRed().getNormalizedX() + "," + pngInfo.getRed().getNormalizedY() + "\n");
            }
            if (pngInfo.getGreen() != null) {
                infoArea.appendText(getMessage("Green") + ": "
                        + pngInfo.getGreen().getNormalizedX() + "," + pngInfo.getGreen().getNormalizedY() + "\n");
            }
            if (pngInfo.getBlue() != null) {
                infoArea.appendText(getMessage("Blue") + ": "
                        + pngInfo.getBlue().getNormalizedX() + "," + pngInfo.getBlue().getNormalizedY() + "\n");
            }
            if (pngInfo.getProfileName() != null) {
                infoArea.appendText(getMessage("IccProfile") + ": " + pngInfo.getProfileName() + "\n");
            }
            if (pngInfo.getProfileCompressionMethod() != null) {
                infoArea.appendText(getMessage("ProfileCompressionMethod") + ": " + pngInfo.getProfileCompressionMethod() + "\n");
            }
            if (pngInfo.getsBIT_Grayscale() >= 0) {
                infoArea.appendText(getMessage("sBIT_Grayscale") + ": " + pngInfo.getsBIT_Grayscale() + "\n");
            }
            if (pngInfo.getsBIT_GrayAlpha_alpha() >= 0) {
                infoArea.appendText(getMessage("sBIT_GrayAlpha") + ": "
                        + pngInfo.getsBIT_GrayAlpha_gray() + " " + pngInfo.getsBIT_GrayAlpha_alpha() + "\n");
            }
            if (pngInfo.getsBIT_RGB_red() >= 0) {
                infoArea.appendText(getMessage("sBIT_RGB") + ": "
                        + pngInfo.getsBIT_RGB_red() + " " + pngInfo.getsBIT_RGB_green() + " " + pngInfo.getsBIT_RGB_blue() + "\n");
            }
            if (pngInfo.getsBIT_RGBAlpha_red() >= 0) {
                infoArea.appendText(getMessage("sBIT_RGBAlpha") + ": "
                        + pngInfo.getsBIT_RGBAlpha_red() + " " + pngInfo.getsBIT_RGBAlpha_green()
                        + " " + pngInfo.getsBIT_RGBAlpha_blue() + " " + pngInfo.getsBIT_RGBAlpha_alpha() + "\n");
            }
            if (pngInfo.getsBIT_Palette_red() >= 0) {
                infoArea.appendText(getMessage("sBIT_Palette") + ": "
                        + pngInfo.getsBIT_Palette_red() + " " + pngInfo.getsBIT_Palette_green() + " " + pngInfo.getsBIT_Palette_blue() + "\n");
            }
            if (pngInfo.getSuggestedPaletteSize() > 0) {
                infoArea.appendText(getMessage("SuggestedPaletteSize") + ": " + pngInfo.getSuggestedPaletteSize() + "\n");
            }
            if (pngInfo.getRenderingIntent() != null) {
                infoArea.appendText(getMessage("RenderingIntent") + ": " + pngInfo.getRenderingIntent() + "\n");
            }
            if (pngInfo.gettRNS_Grayscale() >= 0) {
                infoArea.appendText(getMessage("tRNS_Grayscale") + ": " + pngInfo.gettRNS_Grayscale() + "\n");
            }
            if (pngInfo.gettRNS_RGB() != null) {
                infoArea.appendText(getMessage("tRNS_RGB") + ": " + pngInfo.gettRNS_RGB().toString() + "\n");
            }
            if (pngInfo.gettRNS_Palette_index() >= 0) {
                infoArea.appendText(getMessage("tRNS_Palette") + ": "
                        + pngInfo.gettRNS_Palette_index() + " " + pngInfo.gettRNS_Palette_alpha() + "\n");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadAttributes(ImageInformation imageInfo) {
        try {
            LinkedHashMap<String, Object> attributes = imageInfo.getAttributes();
            for (String key : attributes.keySet()) {
                infoArea.appendText(getMessage(key) + ": " + attributes.get(key) + "\n");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
