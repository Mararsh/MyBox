package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import mara.mybox.MyBox;
import mara.mybox.data.AlarmClock;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.scheduledTasks;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private Popup imagePop;
    private ImageView view;
    private Text text;

    @FXML
    private VBox menuBox, imageBox, pdfBox, fileBox, recentBox, networkBox, dataBox,
            settingsBox, aboutBox, mediaBox;
    @FXML
    private CheckBox imageCheck;

    public MyBoxController() {
        baseTitle = AppVariables.message("AppTitle");

    }

    @Override
    public void initializeNext() {
        try {
            makeImagePopup();
            initAlocks();
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            thisPane.getScene().getWindow().focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    if (!newV) {
                        hideMenu(null);
                    }
                }
            });
//            String s = "";
//            if (AppVariables.appArgs != null) {
//                for (String arg : AppVariables.appArgs) {
//                    s += arg + "  " + new String(arg.getBytes(), "utf-8");
//                }
//            }
//            popText(s, 10000, "red");

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void makeImagePopup() {
        try {
            imagePop = new Popup();
            imagePop.setWidth(510);
            imagePop.setHeight(560);

            VBox vbox = new VBox();
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setMaxWidth(Double.MAX_VALUE);
            vbox.setMaxHeight(Double.MAX_VALUE);
            vbox.setStyle("-fx-background-color: white;");
            imagePop.getContent().add(vbox);

            view = new ImageView();
            view.setFitWidth(500);
            view.setFitHeight(500);
            vbox.getChildren().add(view);

            text = new Text();
            text.setStyle("-fx-font-size: 1.5em;");
            vbox.getChildren().add(text);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initAlocks() {
        try {
            List<AlarmClock> alarms = AlarmClock.readAlarmClocks();
            if (alarms != null) {
                for (AlarmClock alarm : alarms) {
                    if (alarm.isIsActive()) {
                        AlarmClock.scehduleAlarmClock(alarm);
                    }
                }
                if (scheduledTasks != null && scheduledTasks.size() > 0) {
                    bottomLabel.setText(MessageFormat.format(AppVariables.message("AlarmClocksRunning"), scheduledTasks.size()));
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private void showMenu(Region box, MouseEvent event) {
        if (popMenu == null || popMenu.isShowing()) {
            return;
        }
        FxmlControl.locateCenter(box, popMenu);
    }

    public void locateImage(Node region, boolean right) {
        if (!imageCheck.isSelected()) {
            imagePop.hide();
            return;
        }
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        double x = right ? (bounds.getMaxX() + 200) : (bounds.getMinX() - 550);
        imagePop.show(region, x, bounds.getMinY() - 50);
        FxmlControl.refreshStyle(imagePop.getOwnerNode().getParent());
    }

    @FXML
    private void hideMenu(MouseEvent event) {
        if (popMenu != null) {
            popMenu.hide();
            popMenu = null;
        }
        imagePop.hide();
    }

    @FXML
    private void showPdfMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem pdfHtmlViewer = new MenuItem(AppVariables.message("PdfHtmlViewer"));
        pdfHtmlViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfHtmlViewerFxml);
            }
        });

        MenuItem pdfView = new MenuItem(AppVariables.message("PdfView"));
        pdfView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfViewFxml);
            }
        });

        MenuItem PDFAttributes = new MenuItem(AppVariables.message("PDFAttributes"));
        PDFAttributes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfAttributesFxml);
            }
        });

        MenuItem PDFAttributesBatch = new MenuItem(AppVariables.message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfAttributesBatchFxml);
            }
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(AppVariables.message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractImagesBatchFxml);
            }
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(AppVariables.message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractTextsBatchFxml);
            }
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(AppVariables.message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfConvertImagesBatchFxml);
            }
        });

        MenuItem pdfOcrBatch = new MenuItem(AppVariables.message("PdfOCRBatch"));
        pdfOcrBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfOCRBatchFxml);
            }
        });

        MenuItem pdfConvertHtmlsBatch = new MenuItem(AppVariables.message("PdfConvertHtmlsBatch"));
        pdfConvertHtmlsBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfConvertHtmlsBatchFxml);
            }
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVariables.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombinePdfFxml);
            }
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(AppVariables.message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfCompressImagesBatchFxml);
            }
        });

        MenuItem pdfMerge = new MenuItem(AppVariables.message("MergePdf"));
        pdfMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfMergeFxml);
            }
        });

        MenuItem PdfSplitBatch = new MenuItem(AppVariables.message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfSplitBatchFxml);
            }
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                pdfHtmlViewer, pdfView, new SeparatorMenuItem(),
                pdfConvertHtmlsBatch, pdfConvertImagesBatch, pdfExtractImagesBatch, pdfExtractTextsBatch,
                pdfOcrBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch
        );

        showMenu(pdfBox, event);

        view.setImage(new Image("img/PdfTools.png"));
        text.setText(message("PDFToolsImageTips"));
        text.setWrappingWidth(500);
        locateImage(pdfBox, true);

    }

    @FXML
    private void showImageMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem imageViewer = new MenuItem(AppVariables.message("ImageViewer"));
        imageViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageViewerFxml);
            }
        });

        MenuItem imagesBrowser = new MenuItem(AppVariables.message("ImagesBrowser"));
        imagesBrowser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesBrowserFxml);
            }
        });

        MenuItem imageData = new MenuItem(AppVariables.message("ImageData"));
        imageData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageDataFxml);
            }
        });

        MenuItem ImageManufacture = new MenuItem(AppVariables.message("ImageManufacture"));
        ImageManufacture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureFxml);
            }
        });

        MenuItem imageConverterBatch = new MenuItem(AppVariables.message("ImageConverterBatch"));
        imageConverterBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageConverterBatchFxml);
            }
        });

        MenuItem imageStatistic = new MenuItem(AppVariables.message("ImageStatistic"));
        imageStatistic.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageStatisticFxml);
            }
        });

        MenuItem imageOCR = new MenuItem(AppVariables.message("ImageOCR"));
        imageOCR.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageOCRFxml);
            }
        });

        MenuItem imageOCRBatch = new MenuItem(AppVariables.message("ImageOCRBatch"));
        imageOCRBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageOCRBatchFxml);
            }
        });

        MenuItem convolutionKernelManager = new MenuItem(AppVariables.message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ConvolutionKernelManagerFxml);
            }
        });

        MenuItem pixelsCalculator = new MenuItem(AppVariables.message("PixelsCalculator"));
        pixelsCalculator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.PixelsCalculatorFxml);
            }
        });

        MenuItem colorPalette = new MenuItem(AppVariables.message("ColorPalette"));
        colorPalette.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                paletteController = (ColorPaletteController) openStage(CommonValues.ColorPaletteFxml);
            }
        });

        Menu manufactureBatchMenu = makeImageBatchToolsMenu();
        Menu framesMenu = makeImageFramesMenu();
        Menu partMenu = makeImagePartMenu();
        Menu mergeMenu = makeImageMergeMenu();
        Menu csMenu = makeColorSpaceMenu();

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        String os = System.getProperty("os.name").toLowerCase();
        popMenu.getItems().addAll(
                imageViewer, imagesBrowser, imageData, new SeparatorMenuItem(),
                ImageManufacture, manufactureBatchMenu,
                imageConverterBatch, imageOCR, imageOCRBatch, new SeparatorMenuItem(),
                framesMenu, mergeMenu, partMenu, new SeparatorMenuItem(),
                //                imageStatistic, new SeparatorMenuItem(),
                convolutionKernelManager, pixelsCalculator, colorPalette, csMenu);

        showMenu(imageBox, event);

        view.setImage(new Image("img/ImageTools.png"));
        text.setText(message("ImageToolsImageTips"));
        locateImage(imageBox, true);

    }

    private Menu makeImageBatchToolsMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem imageSizeMenu = new MenuItem(AppVariables.message("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchSizeFxml);
            }
        });

        MenuItem imageCropMenu = new MenuItem(AppVariables.message("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchCropFxml);
            }
        });

        MenuItem imageColorMenu = new MenuItem(AppVariables.message("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchColorFxml);
            }
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVariables.message("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchEffectsFxml);
            }
        });

        MenuItem imageEnhancementMenu = new MenuItem(AppVariables.message("Enhancement"));
        imageEnhancementMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchEnhancementFxml);
            }
        });

        MenuItem imageReplaceColorMenu = new MenuItem(AppVariables.message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchReplaceColorFxml);
            }
        });

        MenuItem imageTextMenu = new MenuItem(AppVariables.message("Text"));
        imageTextMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchTextFxml);
            }
        });

        MenuItem imageArcMenu = new MenuItem(AppVariables.message("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchArcFxml);
            }
        });

        MenuItem imageShadowMenu = new MenuItem(AppVariables.message("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchShadowFxml);
            }
        });

        MenuItem imageTransformMenu = new MenuItem(AppVariables.message("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchTransformFxml);
            }
        });

        MenuItem imageMarginsMenu = new MenuItem(AppVariables.message("Margins"));
        imageMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchMarginsFxml);
            }
        });

        Menu manufactureBatchMenu = new Menu(AppVariables.message("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imageColorMenu,
                imageEffectsMenu, imageEnhancementMenu, imageReplaceColorMenu, imageTextMenu,
                imageArcMenu, imageShadowMenu, imageTransformMenu, imageMarginsMenu);
        return manufactureBatchMenu;

    }

    private Menu makeImageFramesMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem imageGifViewer = new MenuItem(AppVariables.message("ImageGifViewer"));
        imageGifViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageGifViewerFxml);
            }
        });

        MenuItem imageGifEditer = new MenuItem(AppVariables.message("ImageGifEditer"));
        imageGifEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageGifEditerFxml);
            }
        });

        MenuItem imageTiffEditer = new MenuItem(AppVariables.message("ImageTiffEditer"));
        imageTiffEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageTiffEditerFxml);
            }
        });

        MenuItem imageFramesViewer = new MenuItem(AppVariables.message("ImageFramesViewer"));
        imageFramesViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageFramesViewerFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("MultipleFramesImageFile"));
        manufactureSubMenu.getItems().addAll(imageFramesViewer, imageTiffEditer, imageGifViewer, imageGifEditer);
        return manufactureSubMenu;

    }

    private Menu makeImagePartMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem ImageSplit = new MenuItem(AppVariables.message("ImageSplit"));
        ImageSplit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageSplitFxml);
            }
        });

        MenuItem ImageSample = new MenuItem(AppVariables.message("ImageSubsample"));
        ImageSample.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageSampleFxml);
            }
        });

        MenuItem imageAlphaExtract = new MenuItem(AppVariables.message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageAlphaExtractBatchFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("ImagePart"));
        manufactureSubMenu.getItems().addAll(ImageSplit, ImageSample, imageAlphaExtract);
        return manufactureSubMenu;

    }

    private Menu makeImageMergeMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }

        MenuItem ImageCombine = new MenuItem(AppVariables.message("ImagesCombine"));
        ImageCombine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombineFxml);
            }
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVariables.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombinePdfFxml);
            }
        });

        MenuItem imageAlphaAdd = new MenuItem(AppVariables.message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageAlphaAddBatchFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVariables.message("MergeImages"));
        manufactureSubMenu.getItems().addAll(ImageCombine, imagesCombinePdf, imageAlphaAdd);
        return manufactureSubMenu;

    }

    private Menu makeColorSpaceMenu() {
        if (popMenu != null) {
            popMenu.hide();
        }
        MenuItem IccEditor = new MenuItem(AppVariables.message("IccProfileEditor"));
        IccEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.IccProfileEditorFxml);
            }
        });

        MenuItem ChromaticityDiagram = new MenuItem(AppVariables.message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ChromaticityDiagramFxml);
            }
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(AppVariables.message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ChromaticAdaptationMatrixFxml);
            }
        });

        MenuItem ColorPalette = new MenuItem(AppVariables.message("ColorPalette"));
        ColorPalette.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.ColorPaletteFxml);
            }
        });

        MenuItem ColorConversion = new MenuItem(AppVariables.message("ColorConversion"));
        ColorConversion.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ColorConversionFxml);
            }
        });

        MenuItem RGBColorSpaces = new MenuItem(AppVariables.message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RGBColorSpacesFxml);
            }
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(AppVariables.message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RGB2XYZConversionMatrixFxml);
            }
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(AppVariables.message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RGB2RGBConversionMatrixFxml);
            }
        });

        MenuItem Illuminants = new MenuItem(AppVariables.message("Illuminants"));
        Illuminants.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.IlluminantsFxml);
            }
        });

        Menu csMenu = new Menu(AppVariables.message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    @FXML
    void showNetworkMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem htmlEditor = new MenuItem(AppVariables.message("HtmlEditor"));
        htmlEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HtmlEditorController controller
                        = (HtmlEditorController) loadScene(CommonValues.HtmlEditorFxml);
//                controller.switchBroswerTab();
            }
        });

        MenuItem weiboSnap = new MenuItem(AppVariables.message("WeiboSnap"));
        weiboSnap.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WeiboSnapController controller
                        = (WeiboSnapController) loadScene(CommonValues.WeiboSnapFxml);
            }
        });

        MenuItem markdownEditor = new MenuItem(AppVariables.message("MarkdownEditer"));
        markdownEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.MarkdownEditorFxml);
            }
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                htmlEditor, markdownEditor, new SeparatorMenuItem(),
                weiboSnap
        );
        showMenu(networkBox, event);

        view.setImage(new Image("img/NetworkTools.png"));
        text.setText(message("NetworkToolsImageTips"));
        locateImage(networkBox, true);

    }

    @FXML
    void showFileMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem filesRename = new MenuItem(AppVariables.message("FilesRename"));
        filesRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesRenameFxml);
            }
        });

        MenuItem dirSynchronize = new MenuItem(AppVariables.message("DirectorySynchronize"));
        dirSynchronize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.DirectorySynchronizeFxml);
            }
        });

        MenuItem filesArrangement = new MenuItem(AppVariables.message("FilesArrangement"));
        filesArrangement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesArrangementFxml);
            }
        });

        MenuItem textEditer = new MenuItem(AppVariables.message("TextEditer"));
        textEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextEditerFxml);
            }
        });

        MenuItem textEncodingBatch = new MenuItem(AppVariables.message("TextEncodingBatch"));
        textEncodingBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextEncodingBatchFxml);
            }
        });

        MenuItem textLineBreakBatch = new MenuItem(AppVariables.message("TextLineBreakBatch"));
        textLineBreakBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextLineBreakBatchFxml);
            }
        });

        MenuItem bytesEditer = new MenuItem(AppVariables.message("BytesEditer"));
        bytesEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.BytesEditerFxml);
            }
        });

        MenuItem fileCut = new MenuItem(AppVariables.message("FileCut"));
        fileCut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FileCutFxml);
            }
        });

        MenuItem filesMerge = new MenuItem(AppVariables.message("FilesMerge"));
        filesMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesMergeFxml);
            }
        });

        MenuItem filesDelete = new MenuItem(AppVariables.message("FilesDelete"));
        filesDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesDeleteFxml);
            }
        });

        MenuItem filesCopy = new MenuItem(AppVariables.message("FilesCopy"));
        filesCopy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesCopyFxml);
            }
        });

        MenuItem filesMove = new MenuItem(AppVariables.message("FilesMove"));
        filesMove.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesMoveFxml);
            }
        });

        MenuItem filesFind = new MenuItem(AppVariables.message("FilesFind"));
        filesFind.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesFindFxml);
            }
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                textEditer, bytesEditer, new SeparatorMenuItem(),
                filesFind, filesArrangement, dirSynchronize,
                filesRename, filesDelete, filesCopy, filesMove,
                textEncodingBatch, textLineBreakBatch, new SeparatorMenuItem(),
                fileCut, filesMerge);

        showMenu(fileBox, event);

        view.setImage(new Image("img/FileTools.png"));
        text.setText(message("FileToolsImageTips"));
        locateImage(fileBox, false);
    }

    @FXML
    void showSettingsMenu(MouseEvent event) {
        hideMenu(event);

        ToggleGroup langGroup = new ToggleGroup();
        RadioMenuItem English = new RadioMenuItem("English");
        English.setToggleGroup(langGroup);
        English.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppVariables.setLanguage("en");
                loadScene(myFxml);
            }
        });
        RadioMenuItem Chinese = new RadioMenuItem("中文");
        Chinese.setToggleGroup(langGroup);
        Chinese.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppVariables.setLanguage("zh");
                loadScene(myFxml);
            }
        });

        CheckMenuItem disableHidpi = new CheckMenuItem(message("DisableHiDPI"));
        disableHidpi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (isSettingValues) {
                    return;
                }
                AppVariables.disableHiDPI = disableHidpi.isSelected();
                ConfigTools.writeConfigValue("DisableHidpi", AppVariables.disableHiDPI ? "true" : "false");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MyBox.restart();
                        } catch (Exception e) {
                            logger.debug(e.toString());
                        }
                    }
                });
            }
        });
        isSettingValues = true;
        AppVariables.disableHiDPI = "true".equals(ConfigTools.readConfigValue("DisableHidpi"));
        disableHidpi.setSelected(AppVariables.disableHiDPI);
        isSettingValues = false;

        MenuItem settings = new MenuItem(AppVariables.message("SettingsDot"));
        settings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                BaseController c = openStage(CommonValues.SettingsFxml);
            }
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(English, Chinese, new SeparatorMenuItem(), disableHidpi,
                new SeparatorMenuItem(), settings);

        showMenu(settingsBox, event);

        view.setImage(new Image("img/Settings.png"));
        text.setText(message("SettingsImageTips"));
        locateImage(settingsBox, true);
    }

    @FXML
    void showRecentMenu(MouseEvent event) {
        hideMenu(event);

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(getRecentMenu());
        showMenu(recentBox, event);

        view.setImage(new Image("img/RecentAccess.png"));
        text.setText(message("RecentAccessImageTips"));
        locateImage(recentBox, true);
    }

    @FXML
    void showDataMenu(MouseEvent event) {
        hideMenu(event);

        Menu csMenu = makeColorSpaceMenu();

        MenuItem imageData = new MenuItem(AppVariables.message("ImageData"));
        imageData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageDataFxml);
            }
        });

        MenuItem MatricesCalculation = new MenuItem(AppVariables.message("MatricesCalculation"));
        MatricesCalculation.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.MatricesCalculationFxml);
            }
        });

        MenuItem barcodeCreator = new MenuItem(AppVariables.message("BarcodeCreator"));
        barcodeCreator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.BarcodeCreatorFxml);
            }
        });

        MenuItem barcodeDecoder = new MenuItem(AppVariables.message("BarcodeDecoder"));
        barcodeDecoder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.BarcodeDecoderFxml);
            }
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                MatricesCalculation, new SeparatorMenuItem(),
                imageData, csMenu, new SeparatorMenuItem(),
                barcodeCreator, barcodeDecoder);

        showMenu(dataBox, event);

        view.setImage(new Image("img/DataTools.png"));
        text.setText(message("DataToolsImageTips"));
        locateImage(dataBox, true);
    }

    @FXML
    void showMediaMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem recordImages = new MenuItem(AppVariables.message("RecordImagesInSystemClipBoard"));
        recordImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RecordImagesInSystemClipboardFxml);
            }
        });

        MenuItem alarmClock = new MenuItem(AppVariables.message("AlarmClock"));
        alarmClock.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.AlarmClockFxml);
            }
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                recordImages, new SeparatorMenuItem(),
                alarmClock);

        showMenu(mediaBox, event);

        view.setImage(new Image("img/MediaTools.png"));
        text.setText(message("MediaToolsImageTips"));
        locateImage(mediaBox, false);
    }

    @FXML
    private void showAboutImage(MouseEvent event) {
        hideMenu(event);

        view.setImage(new Image("img/About56.png"));
        text.setText(message("AboutImageTips"));
        locateImage(aboutBox, false);
    }

    @FXML
    private void showAbout(MouseEvent event) {
        hideMenu(event);
        openStage(CommonValues.AboutFxml);
    }

}
