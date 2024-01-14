package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ImageInformationPng;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageInformationController extends HtmlTableController {

    protected ImageInformation imageInfo;

    @FXML
    protected HBox iccBox;
    @FXML
    protected ComboBox<String> indexSelector;

    public ImageInformationController() {
        baseTitle = message("ImageInformation");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            iccBox.setVisible(false);

        } catch (Exception e) {

        }
    }

    @Override
    public void displayHtml() {
        loadImageFileInformation(imageInfo);
    }

    public void loadImageFileInformation(ImageInformation info) {
        try {
            if (info == null) {
                return;
            }
            imageInfo = info;
            indexSelector.getItems().clear();

            StringBuilder s = new StringBuilder();
            ImageFileInformation finfo = info.getImageFileInformation();
            if (finfo != null) {
                s.append(makeFileInfoTable(finfo)).append("\n</br></br>\n");
                s.append(makeInfoTable(info)).append("\n</br></br>\n");
                for (int i = 0; i < finfo.getImagesInformation().size(); ++i) {
                    ImageInformation iInfo = finfo.getImagesInformation().get(i);
                    if (iInfo.getIccProfile() != null) {
                        indexSelector.getItems().add((i + 1) + "");
                    }
                    s.append(makeImageInformationTable(i, iInfo)).append("</br>\n");
                }
                html = HtmlWriteTools.html(finfo.getFile().getAbsolutePath(), HtmlStyles.styleValue("Default"), s.toString());
            } else {
                s.append(makeInfoTable(info)).append("\n</br></br>\n");
                html = HtmlWriteTools.html(null, HtmlStyles.styleValue("Default"), s.toString());
            }
            loadContents​(html);
            if (indexSelector.getItems().isEmpty()) {
                iccBox.setVisible(false);
            } else {
                iccBox.setVisible(true);
                indexSelector.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected String makeFileInfoTable(ImageFileInformation finfo) {
        table = new StringTable(null, message("ImageFileInformation"));
        File file = finfo.getFile();
        table.add(Arrays.asList(message("FilesPath"), file.getParent()));
        table.add(Arrays.asList(message("FileName"), file.getName()));
        table.add(Arrays.asList(message("FileSize"), FileTools.showFileSize(finfo.getFileSize())));
        table.add(Arrays.asList(message("CreateTime"), DateTools.datetimeToString(finfo.getCreateTime())));
        table.add(Arrays.asList(message("ModifyTime"), DateTools.datetimeToString(finfo.getModifyTime())));
        table.add(Arrays.asList(message("Format"), finfo.getImageFormat()));
        table.add(Arrays.asList(message("NumberOfImagesInFile"), finfo.getNumberOfImages() + ""));
        return StringTable.tableDiv(table);
    }

    protected String makeInfoTable(ImageInformation info) {
        table = new StringTable(null, message("CurrentImage"));
        table.add(Arrays.asList(message("CurrentFrame"), (info.getIndex() + 1) + ""));
        table.add(Arrays.asList(message("Pixels"), (int) info.getWidth() + "x" + (int) info.getHeight()));
        Image image = info.getThumbnail();
        if (image == null) {
            image = info.getImage();
        }
        if (image != null) {
            table.add(Arrays.asList(message("LoadedSize"), (int) image.getWidth() + "x" + (int) image.getHeight()));
        }
        if (info.isIsScaled()) {
            table.add(Arrays.asList(message("Scaled"), "scaleX: " + info.getXscale() + " scaleY:" + info.getYscale()));
        }
        if (info.isIsSampled()) {
            table.add(Arrays.asList(message("Sample"),
                    info.sampleInformation(null, image).replaceAll("\n", "<br>")));
        }
        return StringTable.tableDiv(table);
    }

    public String makeImageInformationTable(int index, ImageInformation imageInfo) {
        try {
            table = new StringTable(null, message("Image") + " " + (index + 1));
            loadStandardInformation(table, imageInfo);
            switch (imageInfo.getImageFormat()) {
                case "png":
                    loadPngInformation(table, imageInfo);
                default:
                    loadNativeAttributes(table, imageInfo);
            }
            return StringTable.tableDiv(table);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return "";
        }
    }

    public void loadStandardInformation(StringTable table, ImageInformation imageInfo) {
        try {
            table.add(Arrays.asList(message("ImageType"), ImageColorSpace.imageType(imageInfo.getImageType())));
            table.add(Arrays.asList(message("xPixels"), imageInfo.getWidth() + ""));
            table.add(Arrays.asList(message("yPixels"), imageInfo.getHeight() + ""));

            if (imageInfo.getXDpi() > 0) {
                table.add(Arrays.asList(message("xDensity"), imageInfo.getXDpi() + " dpi"));
                double xinch = imageInfo.getWidth() / imageInfo.getXDpi();
                table.add(Arrays.asList(message("xSize"), xinch + " " + message("inches")
                        + " = " + (xinch * 2.54) + " " + message("centimetres")));
            }
            if (imageInfo.getYDpi() > 0) {
                table.add(Arrays.asList(message("yDensity"), imageInfo.getYDpi() + " dpi"));
                double yinch = imageInfo.getHeight() / imageInfo.getYDpi();
                table.add(Arrays.asList(message("ySize"), yinch + " " + message("inches")
                        + " = " + (yinch * 2.54) + " " + message("centimetres")));
            }
            table.add(Arrays.asList(message("ColorSpace"), imageInfo.getColorSpace()));
            table.add(Arrays.asList(message("ColorChannels"), imageInfo.getColorChannels() + ""));
            if (imageInfo.getBitDepth() > 0) {
                table.add(Arrays.asList(message("BitDepth"), imageInfo.getBitDepth() + ""));
            }
            if (imageInfo.getBitsPerSample() != null) {
                table.add(Arrays.asList(message("BitsPerSample"), imageInfo.getBitsPerSample()));
            }
            if (imageInfo.getGamma() > 0) {
                table.add(Arrays.asList(message("Gamma"), imageInfo.getGamma() + ""));
            }
            table.add(Arrays.asList(message("BlackIsZero"), message(imageInfo.isBlackIsZero() + "")));
            if (imageInfo.getStandardIntAttribute("PaletteSize") > 0) {
                table.add(Arrays.asList(message("PaletteSize"), imageInfo.getStandardIntAttribute("PaletteSize") + ""));
            }
            if (imageInfo.getBackgroundIndex() > 0) {
                table.add(Arrays.asList(message("BackgroundIndex"), imageInfo.getBackgroundIndex() + ""));
            }
            if (imageInfo.getBackgroundColor() != null) {
                table.add(Arrays.asList(message("BackgroundColor"), imageInfo.getBackgroundColor().toString()));
            }
            if (imageInfo.getCompressionType() != null) {
                table.add(Arrays.asList(message("CompressionType"), imageInfo.getCompressionType()));
                table.add(Arrays.asList(message("LosslessCompression"), message(imageInfo.isIsLossless() + "")));
            }
            if (imageInfo.getNumProgressiveScans() > 0) {
                table.add(Arrays.asList(message("NumProgressiveScans"), imageInfo.getNumProgressiveScans() + ""));
            }
            if (imageInfo.getBitRate() > 0) {
                table.add(Arrays.asList(message("BitRate"), imageInfo.getBitRate() + ""));
            }
            if (imageInfo.getPlanarConfiguration() != null) {
                table.add(Arrays.asList(message("PlanarConfiguration"), imageInfo.getPlanarConfiguration()));
            }
            if (imageInfo.getSampleFormat() != null) {
                table.add(Arrays.asList(message("SampleFormat"), imageInfo.getSampleFormat()));
            }
            if (imageInfo.getSignificantBitsPerSample() != null) {
                table.add(Arrays.asList(message("SignificantBitsPerSample"), imageInfo.getSignificantBitsPerSample()));
            }
            if (imageInfo.getSampleMSB() != null) {
                table.add(Arrays.asList(message("SampleMSB"), imageInfo.getSampleMSB()));
            }
            if (imageInfo.getPixelAspectRatio() > 0) {
                table.add(Arrays.asList(message("PixelAspectRatio"), imageInfo.getPixelAspectRatio() + ""));
            }
            if (imageInfo.getImageRotation() != null) {
                table.add(Arrays.asList(message("ImageOrientation"), imageInfo.getImageRotation()));
            }
            if (imageInfo.getHorizontalPixelSize() > 0) {
                table.add(Arrays.asList(message("HorizontalPixelSize"), imageInfo.getHorizontalPixelSize() + ""));
            }
            if (imageInfo.getVerticalPixelSize() > 0) {
                table.add(Arrays.asList(message("VerticalPixelSize"), imageInfo.getVerticalPixelSize() + ""));
            }
            if (imageInfo.getHorizontalPhysicalPixelSpacing() > 0) {
                table.add(Arrays.asList(message("HorizontalPhysicalPixelSpacing"), imageInfo.getHorizontalPhysicalPixelSpacing() + ""));
            }
            if (imageInfo.getVerticalPhysicalPixelSpacing() > 0) {
                table.add(Arrays.asList(message("VerticalPhysicalPixelSpacing"), imageInfo.getVerticalPhysicalPixelSpacing() + ""));
            }
            if (imageInfo.getHorizontalPosition() > 0) {
                table.add(Arrays.asList(message("HorizontalPosition"), imageInfo.getHorizontalPosition() + ""));
            }
            if (imageInfo.getVerticalPosition() > 0) {
                table.add(Arrays.asList(message("VerticalPosition"), imageInfo.getVerticalPosition() + ""));
            }
            if (imageInfo.getHorizontalPixelOffset() > 0) {
                table.add(Arrays.asList(message("HorizontalPixelOffset"), imageInfo.getHorizontalPixelOffset() + ""));
            }
            if (imageInfo.getVerticalPixelOffset() > 0) {
                table.add(Arrays.asList(message("VerticalPixelOffset"), imageInfo.getVerticalPixelOffset() + ""));
            }
            if (imageInfo.getHorizontalScreenSize() > 0) {
                table.add(Arrays.asList(message("HorizontalScreenSize"), imageInfo.getHorizontalScreenSize() + ""));
            }
            if (imageInfo.getVerticalScreenSize() > 0) {
                table.add(Arrays.asList(message("VerticalScreenSize"), imageInfo.getVerticalScreenSize() + ""));
            }
            if (imageInfo.getFormatVersion() != null) {
                table.add(Arrays.asList(message("FormatVersion"), imageInfo.getFormatVersion()));
            }
            if (imageInfo.getSubimageInterpretation() != null) {
                table.add(Arrays.asList(message("SubimageInterpretation"), imageInfo.getSubimageInterpretation()));
            }
            if (imageInfo.getImageCreationTime() != null) {
                table.add(Arrays.asList(message("ImageCreationTime"), imageInfo.getImageCreationTime()));
            }
            if (imageInfo.getImageModificationTime() != null) {
                table.add(Arrays.asList(message("ImageModificationTime"), imageInfo.getImageModificationTime()));
            }
            table.add(Arrays.asList(message("AlphaChannel"), imageInfo.getAlpha()));
            if (imageInfo.getTransparentIndex() > 0) {
                table.add(Arrays.asList(message("TransparentIndex"), imageInfo.getTransparentIndex() + ""));
            }
            if (imageInfo.getTransparentColor() != null) {
                table.add(Arrays.asList(message("TransparentColor"), imageInfo.getTransparentColor()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadPngInformation(StringTable table, ImageInformation imageInfo) {
        try {
            ImageInformationPng pngInfo = (ImageInformationPng) imageInfo;
            if (pngInfo.getColorType() != null) {
                table.add(Arrays.asList(message("ColorType"), pngInfo.getColorType()));
            }

            if (pngInfo.getCompressionMethod() != null) {
                table.add(Arrays.asList(message("CompressionMethod"), pngInfo.getCompressionMethod()));
            }
            if (pngInfo.getFilterMethod() != null) {
                table.add(Arrays.asList(message("FilterMethod"), pngInfo.getFilterMethod()));
            }
            if (pngInfo.getInterlaceMethod() != null) {
                table.add(Arrays.asList(message("InterlaceMethod"), pngInfo.getInterlaceMethod()));
            }
            if (pngInfo.getUnitSpecifier() != null) {
                table.add(Arrays.asList(message("UnitSpecifier"), pngInfo.getUnitSpecifier()));
            }
            if (pngInfo.getPixelsPerUnitXAxis() > 0) {
                table.add(Arrays.asList(message("PixelsPerUnitXAxis"), pngInfo.getPixelsPerUnitXAxis() + ""));
            }
            if (pngInfo.getPixelsPerUnitYAxis() > 0) {
                table.add(Arrays.asList(message("PixelsPerUnitYAxis"), pngInfo.getPixelsPerUnitYAxis() + ""));
            }
            if (pngInfo.getPngPaletteSize() > 0) {
                table.add(Arrays.asList(message("PngPaletteSize"), pngInfo.getPngPaletteSize() + ""));
            }
            if (pngInfo.getbKGD_Grayscale() >= 0) {
                table.add(Arrays.asList(message("BKGD_Grayscale"), pngInfo.getbKGD_Grayscale() + ""));
            }
            if (pngInfo.getbKGD_RGB() != null) {
                table.add(Arrays.asList(message("BKGD_RGB"), pngInfo.getbKGD_RGB().toString()));
            }
            if (pngInfo.getbKGD_Palette() >= 0) {
                table.add(Arrays.asList(message("BKGD_Palette"), pngInfo.getbKGD_Palette() + ""));
            }
            if (pngInfo.getWhite() != null) {
                table.add(Arrays.asList(message("White"), pngInfo.getWhite().getNormalizedX() + "," + pngInfo.getWhite().getNormalizedY()));
            }
            if (pngInfo.getRed() != null) {
                table.add(Arrays.asList(message("Red"), pngInfo.getRed().getNormalizedX() + "," + pngInfo.getRed().getNormalizedY()));
            }
            if (pngInfo.getGreen() != null) {
                table.add(Arrays.asList(message("Green"), pngInfo.getGreen().getNormalizedX() + "," + pngInfo.getGreen().getNormalizedY()));
            }
            if (pngInfo.getBlue() != null) {
                table.add(Arrays.asList(message("Blue"), pngInfo.getBlue().getNormalizedX() + "," + pngInfo.getBlue().getNormalizedY()));
            }
            if (pngInfo.getProfileName() != null) {
                table.add(Arrays.asList(message("ProfileName"), pngInfo.getProfileName()));
                table.add(Arrays.asList(message("ProfileCompressionMethod"), pngInfo.getProfileCompressionMethod()));
                table.add(Arrays.asList(message("IccProfile"), pngInfo.getIccProfile().length + ""));
            }
            if (pngInfo.getsBIT_Grayscale() >= 0) {
                table.add(Arrays.asList(message("sBIT_Grayscale"), pngInfo.getsBIT_Grayscale() + ""));
            }
            if (pngInfo.getsBIT_GrayAlpha_alpha() >= 0) {
                table.add(Arrays.asList(message("sBIT_GrayAlpha"), pngInfo.getsBIT_GrayAlpha_gray() + " " + pngInfo.getsBIT_GrayAlpha_alpha()));
            }
            if (pngInfo.getsBIT_RGB_red() >= 0) {
                table.add(Arrays.asList(message("sBIT_RGB"), pngInfo.getsBIT_RGB_red() + " " + pngInfo.getsBIT_RGB_green() + " " + pngInfo.getsBIT_RGB_blue()));
            }
            if (pngInfo.getsBIT_RGBAlpha_red() >= 0) {
                table.add(Arrays.asList(message("sBIT_RGBAlpha"),
                        pngInfo.getsBIT_RGBAlpha_red() + " " + pngInfo.getsBIT_RGBAlpha_green()
                        + " " + pngInfo.getsBIT_RGBAlpha_blue() + " " + pngInfo.getsBIT_RGBAlpha_alpha()));
            }
            if (pngInfo.getsBIT_Palette_red() >= 0) {
                table.add(Arrays.asList(message("sBIT_Palette"),
                        +pngInfo.getsBIT_Palette_red() + " " + pngInfo.getsBIT_Palette_green() + " " + pngInfo.getsBIT_Palette_blue()));
            }
            if (pngInfo.getSuggestedPaletteSize() > 0) {
                table.add(Arrays.asList(message("SuggestedPaletteSize"), pngInfo.getSuggestedPaletteSize() + ""));
            }
            if (pngInfo.getRenderingIntent() != null) {
                table.add(Arrays.asList(message("RenderingIntent"), pngInfo.getRenderingIntent()));
            }
            if (pngInfo.gettRNS_Grayscale() >= 0) {
                table.add(Arrays.asList(message("tRNS_Grayscale"), pngInfo.gettRNS_Grayscale() + ""));
            }
            if (pngInfo.gettRNS_RGB() != null) {
                table.add(Arrays.asList(message("tRNS_RGB"), pngInfo.gettRNS_RGB().toString()));
            }
            if (pngInfo.gettRNS_Palette_index() >= 0) {
                table.add(Arrays.asList(message("tRNS_Palette"),
                        +pngInfo.gettRNS_Palette_index() + " " + pngInfo.gettRNS_Palette_alpha()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadNativeAttributes(StringTable table, ImageInformation imageInfo) {
        try {
            LinkedHashMap<String, Object> attributes = imageInfo.getNativeAttributes();
            if (attributes == null) {
                return;
            }
            for (String key : attributes.keySet()) {
                table.add(Arrays.asList(message(key), attributes.get(key) + ""));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                    = (IccProfileEditorController) openStage(Fxmls.IccProfileEditorFxml);
            String name = message("File") + " : " + imageInfo.getFile().getAbsolutePath() + "  "
                    + message("Image") + " " + imageIndex;
            controller.externalData(name, iInfo.getIccProfile());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static ImageInformationController open(ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            ImageInformationController controller = (ImageInformationController) WindowTools.openStage(Fxmls.ImageInformationFxml);
            if (controller != null) {
                controller.loadImageFileInformation(info);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
