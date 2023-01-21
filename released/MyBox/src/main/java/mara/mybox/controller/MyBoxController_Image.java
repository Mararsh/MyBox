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
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Image extends MyBoxController_Document {

    @FXML
    protected void showImageMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem imageViewer = new MenuItem(message("ImageViewer"));
        imageViewer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageViewerFxml);
        });

        MenuItem imagesBrowser = new MenuItem(message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesBrowserFxml);
        });

        MenuItem ImageAnalyse = new MenuItem(message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageAnalyseFxml);
        });

        MenuItem ImagesPlay = new MenuItem(message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesPlayFxml);
        });

        MenuItem ManageColors = new MenuItem(message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event1) -> {
            ColorsManageController.oneOpen();
        });

        MenuItem QueryColor = new MenuItem(message("ColorQuery"));
        QueryColor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ColorQueryFxml);
        });

        MenuItem ImagesInMyBoxClipboard = new MenuItem(message("ImagesInMyBoxClipboard"));
        ImagesInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            ImageInMyBoxClipboardController.oneOpen();
        });

        MenuItem ImagesInSystemClipboard = new MenuItem(message("ImagesInSystemClipboard"));
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
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(imageBox, event);

        view.setImage(new Image("img/ImageTools.png"));
        text.setText(message("ImageToolsImageTips"));
        locateImage(imageBox, true);

    }

    private Menu imageManufactureMenu() {
        MenuItem EditImage = new MenuItem(message("EditImage"));
        EditImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageManufactureFxml);
        });

        MenuItem ImagesEditor = new MenuItem(message("ImagesEditor"));
        ImagesEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem ImagesSplice = new MenuItem(message("ImagesSplice"));
        ImagesSplice.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImagesSpliceFxml);
        });

        MenuItem imageAlphaAdd = new MenuItem(message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAlphaAddBatchFxml);
        });

        MenuItem ImageSplit = new MenuItem(message("ImageSplit"));
        ImageSplit.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSplitFxml);
        });

        MenuItem ImageSample = new MenuItem(message("ImageSubsample"));
        ImageSample.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSampleFxml);
        });

        MenuItem ImageRepeat = new MenuItem(message("ImageRepeatTile"));
        ImageRepeat.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageRepeatFxml);
        });

        MenuItem imageAlphaExtract = new MenuItem(message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAlphaExtractBatchFxml);
        });

        MenuItem imageConverterBatch = new MenuItem(message("ImageConverterBatch"));
        imageConverterBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageConverterBatchFxml);
        });

        MenuItem imageOCR = new MenuItem(message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRFxml);
        });

        MenuItem imageOCRBatch = new MenuItem(message("ImageOCRBatch"));
        imageOCRBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRBatchFxml);
        });

        Menu manufactureMenu = new Menu(message("ImageManufacture"));

        manufactureMenu.getItems().addAll(
                EditImage, manufactureBatchMenu(), new SeparatorMenuItem(),
                ImagesEditor, ImagesSplice, ImageRepeat, imageAlphaAdd, new SeparatorMenuItem(),
                ImageSplit, ImageSample, imageAlphaExtract, new SeparatorMenuItem(),
                imageConverterBatch, imageOCR, imageOCRBatch);

        return manufactureMenu;

    }

    private Menu manufactureBatchMenu() {
        MenuItem imageSizeMenu = new MenuItem(message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchSizeFxml);
        });

        MenuItem imageCropMenu = new MenuItem(message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchCropFxml);
        });

        MenuItem imagePasteMenu = new MenuItem(message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchPasteFxml);
        });

        MenuItem imageColorMenu = new MenuItem(message("Color"));
        imageColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchColorFxml);
        });

        MenuItem imageEffectsMenu = new MenuItem(message("Effects"));
        imageEffectsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchEffectsFxml);
        });

        MenuItem imageEnhancementMenu = new MenuItem(message("Enhancement"));
        imageEnhancementMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchEnhancementFxml);
        });

        MenuItem imageReplaceColorMenu = new MenuItem(message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchReplaceColorFxml);
        });

        MenuItem imageTextMenu = new MenuItem(message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchTextFxml);
        });

        MenuItem imageArcMenu = new MenuItem(message("Arc"));
        imageArcMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchArcFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchShadowFxml);
        });

        MenuItem imageTransformMenu = new MenuItem(message("Transform"));
        imageTransformMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchTransformFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchMarginsFxml);
        });

        Menu manufactureBatchMenu = new Menu(message("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imagePasteMenu,
                imageColorMenu, imageEffectsMenu, imageEnhancementMenu, imageReplaceColorMenu,
                imageTextMenu, imageArcMenu, imageShadowMenu, imageTransformMenu, imageMarginsMenu);
        return manufactureBatchMenu;

    }

    private Menu colorSpaceMenu() {
        MenuItem IccEditor = new MenuItem(message("IccProfileEditor"));
        IccEditor.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.IccProfileEditorFxml);
        });

        MenuItem ChromaticityDiagram = new MenuItem(message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ChromaticityDiagramFxml);
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ChromaticAdaptationMatrixFxml);
        });

        MenuItem ColorConversion = new MenuItem(message("ColorConversion"));
        ColorConversion.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ColorConversionFxml);
        });

        MenuItem RGBColorSpaces = new MenuItem(message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGBColorSpacesFxml);
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGB2XYZConversionMatrixFxml);
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGB2RGBConversionMatrixFxml);
        });

        MenuItem Illuminants = new MenuItem(message("Illuminants"));
        Illuminants.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.IlluminantsFxml);
        });

        Menu csMenu = new Menu(message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    private Menu miscellaneousMenu() {

        MenuItem ImageBase64 = new MenuItem(message("ImageBase64"));
        ImageBase64.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageBase64Fxml);
        });

        MenuItem convolutionKernelManager = new MenuItem(message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ConvolutionKernelManagerFxml);
        });

        MenuItem pixelsCalculator = new MenuItem(message("PixelsCalculator"));
        pixelsCalculator.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.PixelsCalculatorFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                ImageBase64, convolutionKernelManager, pixelsCalculator
        );

        return miscellaneousMenu;

    }

}
