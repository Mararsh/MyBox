package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Actions extends BaseImageController_Image {

    protected int currentAngle = 0, rotateAngle = 90;
    protected Color bgColor = Color.WHITE;

    protected void initCheckboxs() {
        try {
            if (selectAreaCheck != null) {
                selectAreaCheck.setSelected(UserConfig.getBoolean(baseName + "SelectArea", false));
                selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "SelectArea", selectAreaCheck.isSelected());
                        finalRefineView();
                    }
                });
                finalRefineView();
            }

            if (pickColorCheck != null) {
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        isPickingColor = pickColorCheck.isSelected();
                        checkPickingColor();
                    }
                });
            }

            if (rulerXCheck != null) {
                rulerXCheck.setSelected(UserConfig.getBoolean("ImageRulerXY", false));
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageRulerXY", rulerXCheck.isSelected());
                        drawMaskRulers();
                    }
                });
            }
            if (gridCheck != null) {
                gridCheck.setSelected(UserConfig.getBoolean("ImageGridLines", false));
                gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageGridLines", gridCheck.isSelected());
                        drawMaskGrid();
                    }
                });
            }

            if (coordinateCheck != null) {
                coordinateCheck.setSelected(UserConfig.getBoolean("ImagePopCooridnate", false));
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImagePopCooridnate", coordinateCheck.isSelected());
                        checkCoordinate();
                    }
                });
            }

            if (renderController != null) {
                renderController.setParentController(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Image scopeImage() {
        try {
            if (imageView == null) {
                return null;
            }
            Image inImage = imageView.getImage();
            if (inImage == null) {
                return null;
            }
            if (maskRectangle != null && maskRectangle.isVisible() && maskRectangleData != null) {
                if ((int) maskRectangleData.getX() == 0
                        && (int) maskRectangleData.getY() == 0
                        && (int) maskRectangleData.getMaxX() == (int) inImage.getWidth()
                        && (int) maskRectangleData.getMaxY() == (int) inImage.getHeight()) {
                    return inImage;
                }
                return CropTools.cropOutsideFx(inImage, maskRectangleData, bgColor);

            } else {
                return inImage;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return image;
        }
    }

    public Image imageToHandle() {
        Image selected = null;
        if (UserConfig.getBoolean(baseName + "HandleSelectArea", true)) {
            selected = scopeImage();
        }
        if (selected == null) {
            selected = imageView.getImage();
        }
        return selected;
    }

    public Object imageToSaveAs() {
        return imageToHandle();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        if (!checkBeforeNextAction()) {
            return;
        }
        Image clip = ImageClipboardTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(message("NoImageInClipboard"));
            return;
        }
        loadImage(clip);
    }

    @FXML
    public void viewAction() {
        ImageViewerController controller = (ImageViewerController) openStage(Fxmls.ImageViewerFxml);
        checkImage(controller);
    }

    @FXML
    @Override
    public boolean popAction() {
        ImagePopController.openImage(this, imageToHandle());
        return true;
    }

    @FXML
    protected void manufactureAction() {
        ImageManufactureController controller = (ImageManufactureController) openStage(Fxmls.ImageManufactureFxml);
        checkImage(controller);
    }

    @FXML
    public void browseAction() {
        ImagesBrowserController controller = (ImagesBrowserController) openStage(Fxmls.ImagesBrowserFxml);
        File file = imageFile();
        if (file != null) {
            controller.loadImages(file.getParentFile(), 9);
        }
    }

    @FXML
    public void statisticAction() {
        ImageAnalyseController controller = (ImageAnalyseController) openStage(Fxmls.ImageAnalyseFxml);
        checkImage(controller);
    }

    @FXML
    public void ocrAction() {
        ImageOCRController controller = (ImageOCRController) openStage(Fxmls.ImageOCRFxml);
        checkImage(controller);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller = (ImageSplitController) openStage(Fxmls.ImageSplitFxml);
        checkImage(controller);
    }

    @FXML
    public void repeatAction() {
        ImageRepeatController controller = (ImageRepeatController) openStage(Fxmls.ImageRepeatFxml);
        checkImage(controller);
    }

    @FXML
    public void sampleAction() {
        ImageSampleController controller = (ImageSampleController) openStage(Fxmls.ImageSampleFxml);
        checkImage(controller);
    }

    @FXML
    public void convertAction() {
        File file = imageFile();
        if (file != null) {
            ImageConverterController.open(file);
        }
    }

    @FXML
    public void svgAction() {
        File file = imageFile();
        if (file == null) {
            return;
        }
        SvgFromImageController.open(file);
    }

    @FXML
    public void settings() {
        SettingsController controller = SettingsController.oneOpen(this);
        controller.tabPane.getSelectionModel().select(controller.imageTab);
    }

    @FXML
    @Override
    public void copyAction() {
        copyToSystemClipboard();
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image areaImage;

            @Override
            protected boolean handle() {
                areaImage = imageToHandle();
                return areaImage != null;
            }

            @Override
            protected void whenSucceeded() {
                ImageClipboardTools.copyToSystemClipboard(myController, areaImage);
            }

        };
        start(task);
    }

    @Override
    public void copyToMyBoxClipboard() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image areaImage;

            @Override
            protected boolean handle() {
                areaImage = imageToHandle();
                return ImageClipboard.add(areaImage, ImageClipboard.ImageSource.Copy) != null;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("CopiedInMyBoxClipBoard"));
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null || maskRectangle == null || !maskRectangle.isVisible()) {
            return;
        }
        maskRectangleData = DoubleRectangle.xywh(0, 0, imageWidth(), imageHeight());
        drawMaskRectangle();
    }

    @FXML
    @Override
    public void infoAction() {
        if (imageInformation == null) {
            return;
        }
        ImageInformationController.open(imageInformation);
    }

    @FXML
    public void metaAction() {
        if (imageInformation == null) {
            return;
        }
        ImageMetaDataController.open(imageInformation);
    }

    public void checkImage(BaseImageController imageController) {
        if (imageView == null || imageView.getImage() == null || imageController == null) {
            return;
        }
        imageController.requestMouse();
        if (maskRectangle == null || !maskRectangle.isVisible()) {
            if (imageChanged) {
                imageController.loadImage(imageView.getImage());

            } else {
                if (imageInformation != null && imageInformation.getRegion() != null) {
                    imageController.loadRegion(imageInformation);
                } else if (operateOriginalSize()) {
                    imageController.loadImage(imageFile(), imageInformation, imageView.getImage(), imageChanged);
                } else if (imageInformation != null && imageInformation.isIsScaled()) {
                    imageController.loadImage(imageView.getImage());
                } else {
                    imageController.loadImage(imageFile(), imageInformation, imageView.getImage(), imageChanged);
                }
            }
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image targetImage;

            @Override
            protected boolean handle() {
                targetImage = imageToHandle();
                return targetImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.loadImage(targetImage);
            }
        };
        start(task);
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("ImageFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;

            if (selectAreaCheck != null) {
                CheckMenuItem handleSelectCheck = new CheckMenuItem(message("ImageHandleSelectedArea"), StyleTools.getIconImageView("iconRectangle.png"));
                handleSelectCheck.setSelected(UserConfig.getBoolean(baseName + "HandleSelectArea", true));
                handleSelectCheck.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean(baseName + "HandleSelectArea", handleSelectCheck.isSelected());
                    }
                });
                items.add(handleSelectCheck);
                items.add(new SeparatorMenuItem());
            }

            Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
            items.add(viewMenu);

            menu = new MenuItem(message("Pop"), StyleTools.getIconImageView("iconPop.png"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
            viewMenu.getItems().add(menu);

            menu = new MenuItem(message("View"), StyleTools.getIconImageView("iconView.png"));
            menu.setOnAction((ActionEvent event) -> {
                viewAction();
            });
            viewMenu.getItems().add(menu);

            if (imageInformation != null) {
                menu = new MenuItem(message("Information"), StyleTools.getIconImageView("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                viewMenu.getItems().add(menu);

                menu = new MenuItem(message("MetaData"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    metaAction();
                });
                viewMenu.getItems().add(menu);

            }

            if (imageFile() != null) {
                menu = new MenuItem(message("Browse"), StyleTools.getIconImageView("iconBrowse.png"));
                menu.setOnAction((ActionEvent event) -> {
                    browseAction();
                });
                viewMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent event) -> {
                manufactureAction();
            });
            items.add(menu);

            Menu handleMenu = new Menu(message("Operation"), StyleTools.getIconImageView("iconAnalyse.png"));
            items.add(handleMenu);

            menu = new MenuItem(message("Statistic"), StyleTools.getIconImageView("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                statisticAction();

            });
            handleMenu.getItems().add(menu);

            menu = new MenuItem(message("OCR"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                ocrAction();
            });
            handleMenu.getItems().add(menu);

            menu = new MenuItem(message("Split"), StyleTools.getIconImageView("iconSplit.png"));
            menu.setOnAction((ActionEvent event) -> {
                splitAction();
            });
            handleMenu.getItems().add(menu);

            menu = new MenuItem(message("Sample"), StyleTools.getIconImageView("iconSample.png"));
            menu.setOnAction((ActionEvent event) -> {
                sampleAction();

            });
            handleMenu.getItems().add(menu);

            menu = new MenuItem(message("Repeat"), StyleTools.getIconImageView("iconRepeat.png"));
            menu.setOnAction((ActionEvent event) -> {
                repeatAction();
            });
            handleMenu.getItems().add(menu);

            if (imageFile() != null) {
                menu = new MenuItem(message("Convert"), StyleTools.getIconImageView("iconDelimiter.png"));
                menu.setOnAction((ActionEvent event) -> {
                    convertAction();
                });
                handleMenu.getItems().add(menu);

                menu = new MenuItem("SVG", StyleTools.getIconImageView("iconDelimiter.png"));
                menu.setOnAction((ActionEvent event) -> {
                    svgAction();
                });
                handleMenu.getItems().add(menu);
            }

            Menu copyMenu = new Menu(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
            items.add(copyMenu);

            menu = new MenuItem(message("CopyToSystemClipboard"), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyToSystemClipboard();
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("CopyToMyBoxClipboard"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyToMyBoxClipboard();
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("ImagesInSystemClipboard"), StyleTools.getIconImageView("iconSystemClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageInSystemClipboardController.oneOpen();
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("ImagesInMyBoxClipboard"), StyleTools.getIconImageView("iconClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageInMyBoxClipboardController.oneOpen();

            });
            copyMenu.getItems().add(menu);

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("Settings"), StyleTools.getIconImageView("iconSetting.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                settings();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean("ImageFunctionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("ImageFunctionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(fevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
        MenuImageBaseController.open((BaseImageController) this, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    @FXML
    public void rotateRight() {
        rotate(90);
    }

    @FXML
    public void rotateLeft() {
        rotate(270);
    }

    @FXML
    public void turnOver() {
        rotate(180);
    }

    public void rotate(final int rotateAngle) {
        if (imageView.getImage() == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        currentAngle = rotateAngle;
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.rotateImage(imageView.getImage(), rotateAngle);
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageView.setImage(newImage);
                finalRefineView();
                setImageChanged(true);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void playAction() {
        try {
            ImagesPlayController controller = (ImagesPlayController) openStage(Fxmls.ImagesPlayFxml);
            controller.sourceFileChanged(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        saveImage(imageFile(), file, imageToSaveAs());
    }

    public void saveImage(File srcFile, File newfile, Object imageToSave) {
        if (newfile == null || imageToSave == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                BufferedImage bufferedImage;
                if (imageToSave instanceof Image) {
                    bufferedImage = SwingFXUtils.fromFXImage((Image) imageToSave, null);
                } else if (imageToSave instanceof BufferedImage) {
                    bufferedImage = (BufferedImage) imageToSave;
                } else {
                    return false;
                }
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                if (srcFile != null && framesNumber > 1) {
                    error = ImageFileWriters.writeFrame(srcFile, frameIndex, bufferedImage, newfile, null);
                    return error == null;
                } else {
                    return ImageFileWriters.writeImageFile(bufferedImage, newfile);
                }
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Saved"));
                recordFileWritten(newfile);

                if (saveAsType == SaveAsType.Load) {
                    sourceFileChanged(newfile);

                } else if (saveAsType == SaveAsType.Open) {
                    ImageViewerController.openFile(newfile);

                }
            }
        };
        start(task);
    }

}
