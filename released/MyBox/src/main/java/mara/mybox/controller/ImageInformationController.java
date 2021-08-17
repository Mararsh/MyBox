package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import mara.mybox.data.StringTable;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ImageInformationPng;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;

import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
        baseTitle = Languages.message("ImageInformation");
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

            ImageFileInformation finfo = info.getImageFileInformation();
            StringBuilder s = new StringBuilder();
            s.append(makeFileInfoTable(finfo)).append("\n</br></br>\n");
            for (int i = 0; i < finfo.getImagesInformation().size(); ++i) {
                ImageInformation iInfo = finfo.getImagesInformation().get(i);
                if (iInfo.getIccProfile() != null) {
                    indexSelector.getItems().add((i + 1) + "");
                }
                s.append(makeImageInformationTable(i, iInfo)).append("</br>\n");
            }
            String htmlStyle = UserConfig.getString(baseName + "HtmlStyle", "Default");
            html = HtmlWriteTools.html(finfo.getFileName(), htmlStyle, s.toString());
            webView.getEngine().loadContent​(html);
            if (indexSelector.getItems().isEmpty()) {
                iccBox.setVisible(false);
            } else {
                iccBox.setVisible(true);
                indexSelector.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected String makeFileInfoTable(ImageFileInformation finfo) {
//        List<String> names = new ArrayList<>();
//        names.addAll(Arrays.asList(message("Name"), message("Value")
//        ));
        table = new StringTable(null, Languages.message("ImageFileInformation"));
        File file = finfo.getFile();
        table.add(Arrays.asList(Languages.message("FilesPath"), file.getParent()));
        table.add(Arrays.asList(Languages.message("FileName"), file.getName()));
        table.add(Arrays.asList(Languages.message("FileSize"), FileTools.showFileSize(finfo.getFileSize())));
        table.add(Arrays.asList(Languages.message("CreateTime"), DateTools.datetimeToString(finfo.getCreateTime())));
        table.add(Arrays.asList(Languages.message("ModifyTime"), DateTools.datetimeToString(finfo.getModifyTime())));
        table.add(Arrays.asList(Languages.message("Format"), finfo.getImageFormat()));
        table.add(Arrays.asList(Languages.message("NumberOfImagesInFile"), finfo.getNumberOfImages() + ""));
        return StringTable.tableDiv(table);
    }

    public String makeImageInformationTable(int index, ImageInformation imageInfo) {
        try {
//            List<String> names = new ArrayList<>();
//            names.addAll(Arrays.asList(message("Name"), message("Value")
//            ));
            table = new StringTable(null, Languages.message("Image") + " " + (index + 1));
            loadStandardInformation(table, imageInfo);
            switch (imageInfo.getImageFormat()) {
                case "png":
                    loadPngInformation(table, imageInfo);
                default:
                    loadNativeAttributes(table, imageInfo);
            }
            return StringTable.tableDiv(table);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public void loadStandardInformation(StringTable table, ImageInformation imageInfo) {
        try {
            table.add(Arrays.asList(Languages.message("ImageType"), ImageColorSpace.imageType(imageInfo.getImageType())));
            table.add(Arrays.asList(Languages.message("xPixels"), imageInfo.getWidth() + ""));
            table.add(Arrays.asList(Languages.message("yPixels"), imageInfo.getHeight() + ""));

            if (imageInfo.getXDpi() > 0) {
                table.add(Arrays.asList(Languages.message("xDensity"), imageInfo.getXDpi() + " dpi"));
                float xinch = imageInfo.getWidth() / imageInfo.getXDpi();
                table.add(Arrays.asList(Languages.message("xSize"), xinch + " " + Languages.message("inches")
                        + " = " + (xinch * 2.54) + " " + Languages.message("centimetres")));
            }
            if (imageInfo.getYDpi() > 0) {
                table.add(Arrays.asList(Languages.message("yDensity"), imageInfo.getYDpi() + " dpi"));
                float yinch = imageInfo.getHeight() / imageInfo.getYDpi();
                table.add(Arrays.asList(Languages.message("ySize"), yinch + " " + Languages.message("inches")
                        + " = " + (yinch * 2.54) + " " + Languages.message("centimetres")));
            }
            table.add(Arrays.asList(Languages.message("ColorSpace"), imageInfo.getColorSpace()));
            table.add(Arrays.asList(Languages.message("ColorChannels"), imageInfo.getColorChannels() + ""));
            if (imageInfo.getBitDepth() > 0) {
                table.add(Arrays.asList(Languages.message("BitDepth"), imageInfo.getBitDepth() + ""));
            }
            if (imageInfo.getBitsPerSample() != null) {
                table.add(Arrays.asList(Languages.message("BitsPerSample"), imageInfo.getBitsPerSample()));
            }
            if (imageInfo.getGamma() > 0) {
                table.add(Arrays.asList(Languages.message("Gamma"), imageInfo.getGamma() + ""));
            }
            table.add(Arrays.asList(Languages.message("BlackIsZero"), Languages.message(imageInfo.isBlackIsZero() + "")));
            if (imageInfo.getStandardIntAttribute("PaletteSize") > 0) {
                table.add(Arrays.asList(Languages.message("PaletteSize"), imageInfo.getStandardIntAttribute("PaletteSize") + ""));
            }
            if (imageInfo.getBackgroundIndex() > 0) {
                table.add(Arrays.asList(Languages.message("BackgroundIndex"), imageInfo.getBackgroundIndex() + ""));
            }
            if (imageInfo.getBackgroundColor() != null) {
                table.add(Arrays.asList(Languages.message("BackgroundColor"), imageInfo.getBackgroundColor().toString()));
            }
            if (imageInfo.getCompressionType() != null) {
                table.add(Arrays.asList(Languages.message("CompressionType"), imageInfo.getCompressionType()));
                table.add(Arrays.asList(Languages.message("LosslessCompression"), Languages.message(imageInfo.isIsLossless() + "")));
            }
            if (imageInfo.getNumProgressiveScans() > 0) {
                table.add(Arrays.asList(Languages.message("NumProgressiveScans"), imageInfo.getNumProgressiveScans() + ""));
            }
            if (imageInfo.getBitRate() > 0) {
                table.add(Arrays.asList(Languages.message("BitRate"), imageInfo.getBitRate() + ""));
            }
            if (imageInfo.getPlanarConfiguration() != null) {
                table.add(Arrays.asList(Languages.message("PlanarConfiguration"), imageInfo.getPlanarConfiguration()));
            }
            if (imageInfo.getSampleFormat() != null) {
                table.add(Arrays.asList(Languages.message("SampleFormat"), imageInfo.getSampleFormat()));
            }
            if (imageInfo.getSignificantBitsPerSample() != null) {
                table.add(Arrays.asList(Languages.message("SignificantBitsPerSample"), imageInfo.getSignificantBitsPerSample()));
            }
            if (imageInfo.getSampleMSB() != null) {
                table.add(Arrays.asList(Languages.message("SampleMSB"), imageInfo.getSampleMSB()));
            }
            if (imageInfo.getPixelAspectRatio() > 0) {
                table.add(Arrays.asList(Languages.message("PixelAspectRatio"), imageInfo.getPixelAspectRatio() + ""));
            }
            if (imageInfo.getImageRotation() != null) {
                table.add(Arrays.asList(Languages.message("ImageOrientation"), imageInfo.getImageRotation()));
            }
            if (imageInfo.getHorizontalPixelSize() > 0) {
                table.add(Arrays.asList(Languages.message("HorizontalPixelSize"), imageInfo.getHorizontalPixelSize() + ""));
            }
            if (imageInfo.getVerticalPixelSize() > 0) {
                table.add(Arrays.asList(Languages.message("VerticalPixelSize"), imageInfo.getVerticalPixelSize() + ""));
            }
            if (imageInfo.getHorizontalPhysicalPixelSpacing() > 0) {
                table.add(Arrays.asList(Languages.message("HorizontalPhysicalPixelSpacing"), imageInfo.getHorizontalPhysicalPixelSpacing() + ""));
            }
            if (imageInfo.getVerticalPhysicalPixelSpacing() > 0) {
                table.add(Arrays.asList(Languages.message("VerticalPhysicalPixelSpacing"), imageInfo.getVerticalPhysicalPixelSpacing() + ""));
            }
            if (imageInfo.getHorizontalPosition() > 0) {
                table.add(Arrays.asList(Languages.message("HorizontalPosition"), imageInfo.getHorizontalPosition() + ""));
            }
            if (imageInfo.getVerticalPosition() > 0) {
                table.add(Arrays.asList(Languages.message("VerticalPosition"), imageInfo.getVerticalPosition() + ""));
            }
            if (imageInfo.getHorizontalPixelOffset() > 0) {
                table.add(Arrays.asList(Languages.message("HorizontalPixelOffset"), imageInfo.getHorizontalPixelOffset() + ""));
            }
            if (imageInfo.getVerticalPixelOffset() > 0) {
                table.add(Arrays.asList(Languages.message("VerticalPixelOffset"), imageInfo.getVerticalPixelOffset() + ""));
            }
            if (imageInfo.getHorizontalScreenSize() > 0) {
                table.add(Arrays.asList(Languages.message("HorizontalScreenSize"), imageInfo.getHorizontalScreenSize() + ""));
            }
            if (imageInfo.getVerticalScreenSize() > 0) {
                table.add(Arrays.asList(Languages.message("VerticalScreenSize"), imageInfo.getVerticalScreenSize() + ""));
            }
            if (imageInfo.getFormatVersion() != null) {
                table.add(Arrays.asList(Languages.message("FormatVersion"), imageInfo.getFormatVersion()));
            }
            if (imageInfo.getSubimageInterpretation() != null) {
                table.add(Arrays.asList(Languages.message("SubimageInterpretation"), imageInfo.getSubimageInterpretation()));
            }
            if (imageInfo.getImageCreationTime() != null) {
                table.add(Arrays.asList(Languages.message("ImageCreationTime"), imageInfo.getImageCreationTime()));
            }
            if (imageInfo.getImageModificationTime() != null) {
                table.add(Arrays.asList(Languages.message("ImageModificationTime"), imageInfo.getImageModificationTime()));
            }
            table.add(Arrays.asList(Languages.message("AlphaChannel"), imageInfo.getAlpha()));
            if (imageInfo.getTransparentIndex() > 0) {
                table.add(Arrays.asList(Languages.message("TransparentIndex"), imageInfo.getTransparentIndex() + ""));
            }
            if (imageInfo.getTransparentColor() != null) {
                table.add(Arrays.asList(Languages.message("TransparentColor"), imageInfo.getTransparentColor()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadPngInformation(StringTable table, ImageInformation imageInfo) {
        try {
            ImageInformationPng pngInfo = (ImageInformationPng) imageInfo;
            if (pngInfo.getColorType() != null) {
                table.add(Arrays.asList(Languages.message("ColorType"), pngInfo.getColorType()));
            }

            if (pngInfo.getCompressionMethod() != null) {
                table.add(Arrays.asList(Languages.message("CompressionMethod"), pngInfo.getCompressionMethod()));
            }
            if (pngInfo.getFilterMethod() != null) {
                table.add(Arrays.asList(Languages.message("FilterMethod"), pngInfo.getFilterMethod()));
            }
            if (pngInfo.getInterlaceMethod() != null) {
                table.add(Arrays.asList(Languages.message("InterlaceMethod"), pngInfo.getInterlaceMethod()));
            }
            if (pngInfo.getUnitSpecifier() != null) {
                table.add(Arrays.asList(Languages.message("UnitSpecifier"), pngInfo.getUnitSpecifier()));
            }
            if (pngInfo.getPixelsPerUnitXAxis() > 0) {
                table.add(Arrays.asList(Languages.message("PixelsPerUnitXAxis"), pngInfo.getPixelsPerUnitXAxis() + ""));
            }
            if (pngInfo.getPixelsPerUnitYAxis() > 0) {
                table.add(Arrays.asList(Languages.message("PixelsPerUnitYAxis"), pngInfo.getPixelsPerUnitYAxis() + ""));
            }
            if (pngInfo.getPngPaletteSize() > 0) {
                table.add(Arrays.asList(Languages.message("PngPaletteSize"), pngInfo.getPngPaletteSize() + ""));
            }
            if (pngInfo.getbKGD_Grayscale() >= 0) {
                table.add(Arrays.asList(Languages.message("BKGD_Grayscale"), pngInfo.getbKGD_Grayscale() + ""));
            }
            if (pngInfo.getbKGD_RGB() != null) {
                table.add(Arrays.asList(Languages.message("BKGD_RGB"), pngInfo.getbKGD_RGB().toString()));
            }
            if (pngInfo.getbKGD_Palette() >= 0) {
                table.add(Arrays.asList(Languages.message("BKGD_Palette"), pngInfo.getbKGD_Palette() + ""));
            }
            if (pngInfo.getWhite() != null) {
                table.add(Arrays.asList(Languages.message("White"), pngInfo.getWhite().getNormalizedX() + "," + pngInfo.getWhite().getNormalizedY()));
            }
            if (pngInfo.getRed() != null) {
                table.add(Arrays.asList(Languages.message("Red"), pngInfo.getRed().getNormalizedX() + "," + pngInfo.getRed().getNormalizedY()));
            }
            if (pngInfo.getGreen() != null) {
                table.add(Arrays.asList(Languages.message("Green"), pngInfo.getGreen().getNormalizedX() + "," + pngInfo.getGreen().getNormalizedY()));
            }
            if (pngInfo.getBlue() != null) {
                table.add(Arrays.asList(Languages.message("Blue"), pngInfo.getBlue().getNormalizedX() + "," + pngInfo.getBlue().getNormalizedY()));
            }
            if (pngInfo.getProfileName() != null) {
                table.add(Arrays.asList(Languages.message("ProfileName"), pngInfo.getProfileName()));
                table.add(Arrays.asList(Languages.message("ProfileCompressionMethod"), pngInfo.getProfileCompressionMethod()));
                table.add(Arrays.asList(Languages.message("IccProfile"), pngInfo.getIccProfile().length + ""));
            }
            if (pngInfo.getsBIT_Grayscale() >= 0) {
                table.add(Arrays.asList(Languages.message("sBIT_Grayscale"), pngInfo.getsBIT_Grayscale() + ""));
            }
            if (pngInfo.getsBIT_GrayAlpha_alpha() >= 0) {
                table.add(Arrays.asList(Languages.message("sBIT_GrayAlpha"), pngInfo.getsBIT_GrayAlpha_gray() + " " + pngInfo.getsBIT_GrayAlpha_alpha()));
            }
            if (pngInfo.getsBIT_RGB_red() >= 0) {
                table.add(Arrays.asList(Languages.message("sBIT_RGB"), pngInfo.getsBIT_RGB_red() + " " + pngInfo.getsBIT_RGB_green() + " " + pngInfo.getsBIT_RGB_blue()));
            }
            if (pngInfo.getsBIT_RGBAlpha_red() >= 0) {
                table.add(Arrays.asList(Languages.message("sBIT_RGBAlpha"),
                        pngInfo.getsBIT_RGBAlpha_red() + " " + pngInfo.getsBIT_RGBAlpha_green()
                        + " " + pngInfo.getsBIT_RGBAlpha_blue() + " " + pngInfo.getsBIT_RGBAlpha_alpha()));
            }
            if (pngInfo.getsBIT_Palette_red() >= 0) {
                table.add(Arrays.asList(Languages.message("sBIT_Palette"),
                        +pngInfo.getsBIT_Palette_red() + " " + pngInfo.getsBIT_Palette_green() + " " + pngInfo.getsBIT_Palette_blue()));
            }
            if (pngInfo.getSuggestedPaletteSize() > 0) {
                table.add(Arrays.asList(Languages.message("SuggestedPaletteSize"), pngInfo.getSuggestedPaletteSize() + ""));
            }
            if (pngInfo.getRenderingIntent() != null) {
                table.add(Arrays.asList(Languages.message("RenderingIntent"), pngInfo.getRenderingIntent()));
            }
            if (pngInfo.gettRNS_Grayscale() >= 0) {
                table.add(Arrays.asList(Languages.message("tRNS_Grayscale"), pngInfo.gettRNS_Grayscale() + ""));
            }
            if (pngInfo.gettRNS_RGB() != null) {
                table.add(Arrays.asList(Languages.message("tRNS_RGB"), pngInfo.gettRNS_RGB().toString()));
            }
            if (pngInfo.gettRNS_Palette_index() >= 0) {
                table.add(Arrays.asList(Languages.message("tRNS_Palette"),
                        +pngInfo.gettRNS_Palette_index() + " " + pngInfo.gettRNS_Palette_alpha()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadNativeAttributes(StringTable table, ImageInformation imageInfo) {
        try {
            LinkedHashMap<String, Object> attributes = imageInfo.getNativeAttributes();
            if (attributes == null) {
                return;
            }
            for (String key : attributes.keySet()) {
                table.add(Arrays.asList(Languages.message(key), attributes.get(key) + ""));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            String name = Languages.message("File") + " : " + imageInfo.getFileName() + "  "
                    + Languages.message("Image") + " " + imageIndex;
            controller.externalData(name, iInfo.getIccProfile());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
