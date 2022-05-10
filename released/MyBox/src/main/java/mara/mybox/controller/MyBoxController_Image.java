package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Image extends MyBoxController_Document {

    @FXML
    protected void showImageMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem imageViewer = new MenuItem(Languages.message("ImageViewer"));
        imageViewer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageViewerFxml);
        });

        MenuItem imagesBrowser = new MenuItem(Languages.message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesBrowserFxml);
        });

        MenuItem ImageAnalyse = new MenuItem(Languages.message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageAnalyseFxml);
        });

        MenuItem ImagesPlay = new MenuItem(Languages.message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesPlayFxml);
        });

        MenuItem ManageColors = new MenuItem(Languages.message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event1) -> {
            ColorsManageController.oneOpen();
        });

        MenuItem QueryColor = new MenuItem(Languages.message("ColorQuery"));
        QueryColor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ColorQueryFxml);
        });

        MenuItem ImagesInMyBoxClipboard = new MenuItem(Languages.message("ImagesInMyBoxClipboard"));
        ImagesInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            ImageInMyBoxClipboardController.oneOpen();
        });

        MenuItem ImagesInSystemClipboard = new MenuItem(Languages.message("ImagesInSystemClipboard"));
        ImagesInSystemClipboard.setOnAction((ActionEvent event1) -> {
            ImageInSystemClipboardController.oneOpen();
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                imageViewer, imagesBrowser, ImageAnalyse, ImagesPlay, new SeparatorMenuItem(),
                imageManufactureMenu(), new SeparatorMenuItem(),
                ManageColors, QueryColor, colorSpaceMenu(), new SeparatorMenuItem(),
                ImagesInMyBoxClipboard, ImagesInSystemClipboard, miscellaneousMenu());

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(imageBox, event);

        view.setImage(new Image("img/ImageTools.png"));
        text.setText(Languages.message("ImageToolsImageTips"));
        locateImage(imageBox, true);

    }

    private Menu imageManufactureMenu() {
        MenuItem EditImage = new MenuItem(Languages.message("EditImage"));
        EditImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageManufactureFxml);
        });

        MenuItem ImagesEditor = new MenuItem(Languages.message("ImagesEditor"));
        ImagesEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem ImagesSplice = new MenuItem(Languages.message("ImagesSplice"));
        ImagesSplice.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImagesSpliceFxml);
        });

        MenuItem imageAlphaAdd = new MenuItem(Languages.message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAlphaAddBatchFxml);
        });

        MenuItem ImageSplit = new MenuItem(Languages.message("ImageSplit"));
        ImageSplit.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSplitFxml);
        });

        MenuItem ImageSample = new MenuItem(Languages.message("ImageSubsample"));
        ImageSample.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSampleFxml);
        });

        MenuItem imageAlphaExtract = new MenuItem(Languages.message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAlphaExtractBatchFxml);
        });

        MenuItem imageConverterBatch = new MenuItem(Languages.message("ImageConverterBatch"));
        imageConverterBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageConverterBatchFxml);
        });

        MenuItem imageOCR = new MenuItem(Languages.message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRFxml);
        });

        MenuItem imageOCRBatch = new MenuItem(Languages.message("ImageOCRBatch"));
        imageOCRBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRBatchFxml);
        });

        Menu manufactureMenu = new Menu(Languages.message("ImageManufacture"));

        manufactureMenu.getItems().addAll(
                EditImage, manufactureBatchMenu(), new SeparatorMenuItem(),
                ImagesEditor, ImagesSplice, imageAlphaAdd, new SeparatorMenuItem(),
                ImageSplit, ImageSample, imageAlphaExtract, new SeparatorMenuItem(),
                imageConverterBatch, imageOCR, imageOCRBatch);

        return manufactureMenu;

    }

    private Menu manufactureBatchMenu() {
        MenuItem imageSizeMenu = new MenuItem(Languages.message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchSizeFxml);
        });

        MenuItem imageCropMenu = new MenuItem(Languages.message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchCropFxml);
        });

        MenuItem imagePasteMenu = new MenuItem(Languages.message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchPasteFxml);
        });

        MenuItem imageColorMenu = new MenuItem(Languages.message("Color"));
        imageColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchColorFxml);
        });

        MenuItem imageEffectsMenu = new MenuItem(Languages.message("Effects"));
        imageEffectsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchEffectsFxml);
        });

        MenuItem imageEnhancementMenu = new MenuItem(Languages.message("Enhancement"));
        imageEnhancementMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchEnhancementFxml);
        });

        MenuItem imageReplaceColorMenu = new MenuItem(Languages.message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchReplaceColorFxml);
        });

        MenuItem imageTextMenu = new MenuItem(Languages.message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchTextFxml);
        });

        MenuItem imageArcMenu = new MenuItem(Languages.message("Arc"));
        imageArcMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchArcFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(Languages.message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchShadowFxml);
        });

        MenuItem imageTransformMenu = new MenuItem(Languages.message("Transform"));
        imageTransformMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchTransformFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(Languages.message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchMarginsFxml);
        });

        Menu manufactureBatchMenu = new Menu(Languages.message("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imagePasteMenu,
                imageColorMenu, imageEffectsMenu, imageEnhancementMenu, imageReplaceColorMenu,
                imageTextMenu, imageArcMenu, imageShadowMenu, imageTransformMenu, imageMarginsMenu);
        return manufactureBatchMenu;

    }

    private Menu colorSpaceMenu() {
        MenuItem IccEditor = new MenuItem(Languages.message("IccProfileEditor"));
        IccEditor.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.IccProfileEditorFxml);
        });

        MenuItem ChromaticityDiagram = new MenuItem(Languages.message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ChromaticityDiagramFxml);
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(Languages.message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ChromaticAdaptationMatrixFxml);
        });

        MenuItem ColorConversion = new MenuItem(Languages.message("ColorConversion"));
        ColorConversion.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ColorConversionFxml);
        });

        MenuItem RGBColorSpaces = new MenuItem(Languages.message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGBColorSpacesFxml);
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(Languages.message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGB2XYZConversionMatrixFxml);
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(Languages.message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGB2RGBConversionMatrixFxml);
        });

        MenuItem Illuminants = new MenuItem(Languages.message("Illuminants"));
        Illuminants.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.IlluminantsFxml);
        });

        Menu csMenu = new Menu(Languages.message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    private Menu miscellaneousMenu() {

        MenuItem ImageBase64 = new MenuItem(Languages.message("ImageBase64"));
        ImageBase64.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageBase64Fxml);
        });

        MenuItem convolutionKernelManager = new MenuItem(Languages.message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ConvolutionKernelManagerFxml);
        });

        MenuItem pixelsCalculator = new MenuItem(Languages.message("PixelsCalculator"));
        pixelsCalculator.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.PixelsCalculatorFxml);
        });

        Menu miscellaneousMenu = new Menu(Languages.message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                ImageBase64, convolutionKernelManager, pixelsCalculator
        );

        return miscellaneousMenu;

    }

}
