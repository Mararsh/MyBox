package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.objects.AlarmClock;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.scheduledTasks;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private ContextMenu pdfMenus, imageMenu, fileMenu, desktopMenu, languageMenu, networkMenu;

    @FXML
    private VBox imageBox, pdfBox, fileBox, desktopBox, languageBox, networkBox;

    @Override
    protected void initializeNext() {
        try {
            initPdfToolsMenu();
            initImageToolsMenu();
            initDesktopToolsMenu();
            initFileToolsMenu();
            initNetworkToolsMenu();
            initOtherMenu();

            List<AlarmClock> alarms = AlarmClock.readAlarmClocks();
            if (alarms != null) {
                for (AlarmClock alarm : alarms) {
                    if (alarm.isIsActive()) {
                        AlarmClock.scehduleAlarmClock(alarm);
                    }
                }
                if (scheduledTasks != null && scheduledTasks.size() > 0) {
                    bottomLabel.setText(MessageFormat.format(AppVaribles.getMessage("AlarmClocksRunning"), scheduledTasks.size()));
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initPdfToolsMenu() {
        MenuItem pdfExtractImages = new MenuItem(AppVaribles.getMessage("PdfExtractImages"));
        pdfExtractImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfExtractImagesFxml, AppVaribles.getMessage("PdfExtractImages"));
            }
        });
        MenuItem pdfExtractImagesBatch = new MenuItem(AppVaribles.getMessage("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfExtractImagesBatchFxml, AppVaribles.getMessage("PdfExtractImagesBatch"));
            }
        });
        MenuItem pdfExtractTexts = new MenuItem(AppVaribles.getMessage("PdfExtractTexts"));
        pdfExtractTexts.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfExtractTextsFxml, AppVaribles.getMessage("pdfExtractTexts"));
            }
        });
        MenuItem pdfExtractTextsBatch = new MenuItem(AppVaribles.getMessage("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfExtractTextsBatchFxml, AppVaribles.getMessage("PdfExtractTextsBatch"));
            }
        });
        MenuItem pdfConvertImages = new MenuItem(AppVaribles.getMessage("PdfConvertImages"));
        pdfConvertImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfConvertImagesFxml, AppVaribles.getMessage("PdfConvertImages"));
            }
        });
        MenuItem pdfConvertImagesBatch = new MenuItem(AppVaribles.getMessage("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfConvertImagesBatchFxml, AppVaribles.getMessage("PdfConvertImagesBatch"));
            }
        });
        MenuItem imagesCombinePdf = new MenuItem(AppVaribles.getMessage("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImagesCombinePdfFxml, AppVaribles.getMessage("ImagesCombinePdf"));
            }
        });
        MenuItem pdfCompressImages = new MenuItem(AppVaribles.getMessage("CompressPdfImages"));
        pdfCompressImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfCompressImagesFxml, AppVaribles.getMessage("CompressPdfImages"));
            }
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(AppVaribles.getMessage("CompressPdfImagesBatch"));
        pdfCompressImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfCompressImagesBatchFxml, AppVaribles.getMessage("CompressPdfImagesBatch"));
            }
        });

        MenuItem pdfMerge = new MenuItem(AppVaribles.getMessage("MergePdf"));
        pdfMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfMergeFxml, AppVaribles.getMessage("MergePdf"));
            }
        });
        MenuItem pdfSplit = new MenuItem(AppVaribles.getMessage("SplitPdf"));
        pdfSplit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.PdfSplitFxml, AppVaribles.getMessage("SplitPdf"));
            }
        });
        pdfMenus = new ContextMenu();
        pdfMenus.getItems().addAll(
                pdfConvertImages, pdfConvertImagesBatch, new SeparatorMenuItem(),
                imagesCombinePdf, new SeparatorMenuItem(),
                pdfCompressImages, pdfCompressImagesBatch, new SeparatorMenuItem(),
                pdfMerge, pdfSplit, new SeparatorMenuItem(),
                pdfExtractImages, pdfExtractImagesBatch, pdfExtractTexts, pdfExtractTextsBatch);

    }

    private void initImageToolsMenu() {
        MenuItem imageViewer = new MenuItem(AppVaribles.getMessage("ImageViewer"));
        imageViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
            }
        });
        MenuItem imagesViewer = new MenuItem(AppVaribles.getMessage("MultipleImagesViewer"));
        imagesViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImagesViewerFxml, AppVaribles.getMessage("MultipleImagesViewer"));
            }
        });
        MenuItem ImageManufacture = new MenuItem(AppVaribles.getMessage("ImageManufacture"));
        ImageManufacture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
            }
        });
        MenuItem ImageSplit = new MenuItem(AppVaribles.getMessage("ImageSplit"));
        ImageSplit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageSplitFxml, AppVaribles.getMessage("ImageSplit"));
            }
        });
        MenuItem ImageCombine = new MenuItem(AppVaribles.getMessage("ImageCombine"));
        ImageCombine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImagesCombineFxml, AppVaribles.getMessage("ImageCombine"));
            }
        });
        MenuItem imagesCombinePdf = new MenuItem(AppVaribles.getMessage("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImagesCombinePdfFxml, AppVaribles.getMessage("ImagesCombinePdf"));
            }
        });

        MenuItem imageConverter = new MenuItem(AppVaribles.getMessage("ImageConverter"));
        imageConverter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageConverterFxml, AppVaribles.getMessage("ImageConverter"));
            }
        });
        MenuItem imageConverterBatch = new MenuItem(AppVaribles.getMessage("ImageConverterBatch"));
        imageConverterBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageConverterBatchFxml, AppVaribles.getMessage("ImageConverterBatch"));
            }
        });
        MenuItem pixelsCalculator = new MenuItem(AppVaribles.getMessage("PixelsCalculator"));
        pixelsCalculator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.PixelsCalculatorFxml, AppVaribles.getMessage("PixelsCalculator"), false, false);
            }
        });
        MenuItem colorPalette = new MenuItem(AppVaribles.getMessage("ColorPalette"));
        colorPalette.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.ColorPaletteFxml, AppVaribles.getMessage("ColorPalette"), false, false);
            }
        });

        Menu manufactureSubMenu = initImageSubToolsMenu();
        Menu manufactureBatchMenu = initImageBatchToolsMenu();

        imageMenu = new ContextMenu();
        imageMenu.getItems().addAll(ImageManufacture, manufactureSubMenu, manufactureBatchMenu, new SeparatorMenuItem(),
                ImageSplit, ImageCombine, imagesCombinePdf, new SeparatorMenuItem(),
                imageViewer, imagesViewer, new SeparatorMenuItem(),
                imageConverter, imageConverterBatch, new SeparatorMenuItem(),
                colorPalette, pixelsCalculator);
    }

    private Menu initImageSubToolsMenu() {

        MenuItem imageSizeMenu = new MenuItem(AppVaribles.getMessage("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("size");
            }
        });
        MenuItem imageCropMenu = new MenuItem(AppVaribles.getMessage("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("crop");
            }
        });
        MenuItem imageColorMenu = new MenuItem(AppVaribles.getMessage("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("Color");
            }
        });
        MenuItem imageEffectsMenu = new MenuItem(AppVaribles.getMessage("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("effects");
            }
        });
        MenuItem imageFiltersMenu = new MenuItem(AppVaribles.getMessage("Filters"));
        imageFiltersMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("filters");
            }
        });
        MenuItem imageReplaceColorMenu = new MenuItem(AppVaribles.getMessage("ReplaceColor"));
        imageReplaceColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("replaceColor");
            }
        });
        MenuItem imageWatermarkMenu = new MenuItem(AppVaribles.getMessage("Watermark"));
        imageWatermarkMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("watermark");
            }
        });
        MenuItem imageTransformMenu = new MenuItem(AppVaribles.getMessage("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("transform");
            }
        });
        MenuItem imageCutMarginsMenu = new MenuItem(AppVaribles.getMessage("CutMargins"));
        imageCutMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("cutMargins");
            }
        });
        MenuItem imageAddMarginsMenu = new MenuItem(AppVaribles.getMessage("AddMargins"));
        imageAddMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("addMargins");
            }
        });
        MenuItem imageArcMenu = new MenuItem(AppVaribles.getMessage("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("arc");
            }
        });
        MenuItem imageShadowMenu = new MenuItem(AppVaribles.getMessage("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setInitTab("shadow");
            }
        });
        Menu manufactureSubMenu = new Menu(AppVaribles.getMessage("ImageManufactureSub"));
        manufactureSubMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imageColorMenu, imageEffectsMenu, imageFiltersMenu,
                imageReplaceColorMenu, imageWatermarkMenu, imageArcMenu, imageShadowMenu, imageTransformMenu,
                imageCutMarginsMenu, imageAddMarginsMenu);
        return manufactureSubMenu;

    }

    private Menu initImageBatchToolsMenu() {

        MenuItem imageSizeMenu = new MenuItem(AppVaribles.getMessage("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchSizeFxml, AppVaribles.getMessage("ImageManufactureBatchSize"));
            }
        });

        MenuItem imageCropMenu = new MenuItem(AppVaribles.getMessage("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchCropFxml, AppVaribles.getMessage("ImageManufactureBatchCrop"));
            }
        });

        MenuItem imageColorMenu = new MenuItem(AppVaribles.getMessage("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchColorFxml, AppVaribles.getMessage("ImageManufactureBatchColor"));
            }
        });

        MenuItem imageFiltersMenu = new MenuItem(AppVaribles.getMessage("Filters"));
        imageFiltersMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchFiltersFxml, AppVaribles.getMessage("ImageManufactureBatchFilters"));
            }
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVaribles.getMessage("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchEffectsFxml, AppVaribles.getMessage("ImageManufactureBatchEffects"));
            }
        });

        MenuItem imageReplaceColorMenu = new MenuItem(AppVaribles.getMessage("ReplaceColor"));
        imageReplaceColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchReplaceColorFxml, AppVaribles.getMessage("ImageManufactureBatchReplaceColor"));
            }
        });

        MenuItem imageWatermarkMenu = new MenuItem(AppVaribles.getMessage("Watermark"));
        imageWatermarkMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchWatermarkFxml, AppVaribles.getMessage("ImageManufactureBatchWatermark"));
            }
        });

        MenuItem imageArcMenu = new MenuItem(AppVaribles.getMessage("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchArcFxml, AppVaribles.getMessage("ImageManufactureBatchArc"));
            }
        });

        MenuItem imageShadowMenu = new MenuItem(AppVaribles.getMessage("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchShadowFxml, AppVaribles.getMessage("ImageManufactureBatchShadow"));
            }
        });

        MenuItem imageTransformMenu = new MenuItem(AppVaribles.getMessage("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchTransformFxml, AppVaribles.getMessage("ImageManufactureBatchTransform"));
            }
        });

        MenuItem imageCutMarginsMenu = new MenuItem(AppVaribles.getMessage("CutMargins"));
        imageCutMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchCutMarginsFxml, AppVaribles.getMessage("ImageManufactureBatchCutMargins"));
            }
        });

        MenuItem imageAddMarginsMenu = new MenuItem(AppVaribles.getMessage("AddMargins"));
        imageAddMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.ImageManufactureBatchAddMarginsFxml, AppVaribles.getMessage("ImageManufactureBatchAddMargins"));
            }
        });

        Menu manufactureBatchMenu = new Menu(AppVaribles.getMessage("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imageColorMenu, imageEffectsMenu, imageFiltersMenu,
                imageReplaceColorMenu, imageWatermarkMenu, imageArcMenu, imageShadowMenu, imageTransformMenu,
                imageCutMarginsMenu, imageAddMarginsMenu);
        return manufactureBatchMenu;

    }

    private void initDesktopToolsMenu() {
        MenuItem filesRename = new MenuItem(AppVaribles.getMessage("FilesRename"));
        filesRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.FilesRenameFxml, AppVaribles.getMessage("FilesRename"));
            }
        });
        MenuItem dirsRename = new MenuItem(AppVaribles.getMessage("DirectoriesRename"));
        dirsRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.DirectoriesRenameFxml, AppVaribles.getMessage("DirectoriesRename"));
            }
        });
        MenuItem dirSynchronize = new MenuItem(AppVaribles.getMessage("DirectorySynchronize"));
        dirSynchronize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.DirectorySynchronizeFxml, AppVaribles.getMessage("DirectorySynchronize"));
            }
        });
        MenuItem filesArrangement = new MenuItem(AppVaribles.getMessage("FilesArrangement"));
        filesArrangement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.FilesArrangementFxml, AppVaribles.getMessage("FilesArrangement"));
            }
        });
        MenuItem htmlEditor = new MenuItem(AppVaribles.getMessage("HtmlEditor"));
        htmlEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.HtmlEditorFxml, AppVaribles.getMessage("HtmlEditor"));
            }
        });
        MenuItem textEditor = new MenuItem(AppVaribles.getMessage("TextEditor"));
        textEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.TextEditorFxml, AppVaribles.getMessage("TextEditor"));
            }
        });

        MenuItem alarmClock = new MenuItem(AppVaribles.getMessage("AlarmClock"));
        alarmClock.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reloadStage(CommonValues.AlarmClockFxml, AppVaribles.getMessage("AlarmClock"));
            }
        });
        desktopMenu = new ContextMenu();
        desktopMenu.getItems().addAll(filesRename, dirsRename, new SeparatorMenuItem(),
                filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                htmlEditor, textEditor, new SeparatorMenuItem(), alarmClock);
    }

    private void initFileToolsMenu() {
        fileMenu = new ContextMenu();

    }

    private void initNetworkToolsMenu() {
        MenuItem htmlEditor = new MenuItem(AppVaribles.getMessage("HtmlEditor"));
        htmlEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HtmlEditorController controller
                        = (HtmlEditorController) reloadStage(CommonValues.HtmlEditorFxml, AppVaribles.getMessage("HtmlEditor"));
//                controller.switchBroswerTab();
            }
        });

        MenuItem weiboSnap = new MenuItem(AppVaribles.getMessage("WeiboSnap"));
        weiboSnap.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WeiboSnapController controller
                        = (WeiboSnapController) reloadStage(CommonValues.WeiboSnapFxml, AppVaribles.getMessage("WeiboSnap"));
            }
        });
        networkMenu = new ContextMenu();
        networkMenu.getItems().addAll(weiboSnap, htmlEditor);

    }

    private void initOtherMenu() {
        MenuItem setEnglish = new MenuItem("English");
        setEnglish.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppVaribles.setCurrentBundle("en");
                reloadStage(myFxml, AppVaribles.getMessage("AppTitle"));
            }
        });
        MenuItem setChinese = new MenuItem("中文");
        setChinese.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppVaribles.setCurrentBundle("zh");
                reloadStage(myFxml, AppVaribles.getMessage("AppTitle"));
            }
        });
        languageMenu = new ContextMenu();
        languageMenu.getItems().addAll(setChinese, setEnglish);

    }

    @FXML
    void showPdfMenu(MouseEvent event) {
        if (pdfMenus.isShowing()) {
            return;
        }
        Bounds bounds = pdfBox.localToScreen(pdfBox.getBoundsInLocal());
        pdfMenus.show(pdfBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        languageMenu.hide();
        networkMenu.hide();
    }

    @FXML
    void showImageMenu(MouseEvent event) {
        if (imageMenu.isShowing()) {
            return;
        }
        Bounds bounds = imageBox.localToScreen(imageBox.getBoundsInLocal());
        imageMenu.show(imageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        pdfMenus.hide();
        fileMenu.hide();
        desktopMenu.hide();
        languageMenu.hide();
        networkMenu.hide();

    }

    @FXML
    void showFileMenu(MouseEvent event) {
        if (fileMenu.isShowing()) {
            return;
        }
        Bounds bounds = fileBox.localToScreen(fileBox.getBoundsInLocal());
        fileMenu.show(fileBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        pdfMenus.hide();
        desktopMenu.hide();
        languageMenu.hide();
        networkMenu.hide();
    }

    @FXML
    void showNetworkMenu(MouseEvent event) {
        if (networkMenu.isShowing()) {
            return;
        }
        Bounds bounds = networkBox.localToScreen(networkBox.getBoundsInLocal());
        networkMenu.show(networkBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        pdfMenus.hide();
        languageMenu.hide();
        desktopMenu.hide();
    }

    @FXML
    void showDesktopMenu(MouseEvent event) {
        if (desktopMenu.isShowing()) {
            return;
        }
        Bounds bounds = desktopBox.localToScreen(desktopBox.getBoundsInLocal());
        desktopMenu.show(desktopBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        pdfMenus.hide();
        languageMenu.hide();
        networkMenu.hide();
    }

    @FXML
    void showLanguageMenu(MouseEvent event) {
        if (languageMenu.isShowing()) {
            return;
        }
        Bounds bounds = languageBox.localToScreen(languageBox.getBoundsInLocal());
        languageMenu.show(languageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        pdfMenus.hide();
        networkMenu.hide();
    }

    @FXML
    private void showAbout(MouseEvent event) {
        openStage(CommonValues.AboutFxml, true);
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        languageMenu.hide();
        pdfMenus.hide();
        networkMenu.hide();
    }

}
