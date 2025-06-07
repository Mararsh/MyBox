package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.data.ImageItem;
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.image.ImageViewTools;
import mara.mybox.fxml.image.TransformTools;
import mara.mybox.image.data.ImageFileInformation;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
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

    @FXML
    public void zoomIn() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        ImageViewTools.zoomIn(scrollPane, imageView, xZoomStep, yZoomStep);
        refinePane();
    }

    @FXML
    public void zoomOut() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        ImageViewTools.zoomOut(scrollPane, imageView, xZoomStep, yZoomStep);
        refinePane();
    }

    @FXML
    public void moveRight() {
        NodeTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        NodeTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        NodeTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        NodeTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
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
    @Override
    public boolean popAction() {
        if (imageToHandle() == null) {
            return false;
        }
        ImagePopController.openSource((BaseImageController) this);
        return true;
    }

    @FXML
    protected void editAction() {
        ImageEditorController controller = (ImageEditorController) openStage(Fxmls.ImageEditorFxml);
        checkImage(controller);
    }

    @FXML
    public void browseAction() {
        if (sourceFile != null) {
            ImagesBrowserController.openPath(sourceFile.getParentFile());
        }
    }

    @FXML
    public void statisticAction() {
        ImageAnalyseController controller = (ImageAnalyseController) openStage(Fxmls.ImageAnalyseFxml);
        checkImage(controller.imageController);
    }

    @FXML
    public void ocrAction() {
        ImageOCRController controller = (ImageOCRController) openStage(Fxmls.ImageOCRFxml);
        checkImage(controller.sourceController);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller = (ImageSplitController) openStage(Fxmls.ImageSplitFxml);
        checkImage(controller);
    }

    @FXML
    public void repeatAction() {
        ImageRepeatController controller = (ImageRepeatController) openStage(Fxmls.ImageRepeatFxml);
        checkImage(controller.sourceController);
    }

    @FXML
    public void sampleAction() {
        ImageSampleController controller = (ImageSampleController) openStage(Fxmls.ImageSampleFxml);
        checkImage(controller);
    }

    @FXML
    public void svgAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        SvgFromImageController.open(imageView.getImage());
    }

    @FXML
    @Override
    public void copyAction() {
        ImageCopyController.open((BaseImageController) this);
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
        task = new FxSingletonTask<Void>(this) {

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
        task = new FxSingletonTask<Void>(this) {
            private Image areaImage;

            @Override
            protected boolean handle() {
                areaImage = imageToHandle();
                return ImageClipboard.add(this,
                        areaImage, ImageClipboard.ImageSource.Copy) != null;
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
    public boolean infoAction() {
        if (imageInformation == null) {
            return false;
        }
        ImageInformationController.open(imageInformation);
        return true;
    }

    @FXML
    public void metaAction() {
        if (imageInformation == null) {
            return;
        }
        ImageMetaDataController.open(imageInformation);
    }

    public Image imageToHandle() {
        if (imageView == null) {
            return null;
        }
        return imageView.getImage();
    }

    public Object imageToSaveAs(FxTask currentTask) {
        return imageToHandle();
    }

    public void checkImage(BaseImageController imageController) {
        if (imageView == null || imageView.getImage() == null || imageController == null) {
            return;
        }
        imageController.requestMouse();
        if (imageChanged) {
            imageController.loadImage(imageView.getImage());

        } else {
            if (imageInformation != null && imageInformation.getRegion() != null) {
                imageController.loadRegion(imageInformation);
            } else if (operateOriginalSize()) {
                imageController.loadImage(sourceFile, imageInformation, imageView.getImage(), imageChanged);
            } else if (imageInformation != null && imageInformation.isIsScaled()) {
                imageController.loadImage(imageView.getImage());
            } else {
                imageController.loadImage(sourceFile, imageInformation, imageView.getImage(), imageChanged);
            }
        }
    }

    @FXML
    public void editFrames() {
        ImagesEditorController.openFile(sourceFile);
    }

    @FXML
    public void nextFrame() {
        loadFrame(frameIndex + 1);
    }

    @FXML
    public void previousFrame() {
        loadFrame(frameIndex - 1);
    }

    @FXML
    @Override
    public boolean menuAction(Event event) {
        popContextMenu(event);
        return true;
    }

    protected void popContextMenu(Event event) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        Point2D everntCoord = LocateTools.coordinate(event);
        popContextMenu(everntCoord.getX(), everntCoord.getY());
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

    public void rotate(int rotateAngle) {
        if (imageView.getImage() == null || rotateAngle == 0) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        currentAngle = rotateAngle;
        task = new FxSingletonTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.rotateImage(this, imageView.getImage(), rotateAngle, true);
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageView.setImage(newImage);
                refinePane();
                setImageChanged(true);
            }

        };
        start(task);
    }

    @FXML
    public void horizontalAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.horizontalImage(this, imageView.getImage());
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                updateImage(message("MirrorHorizontal"), newImage);
            }

        };
        start(task);
    }

    @FXML
    public void verticalAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.verticalImage(this, imageView.getImage());
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                updateImage(message("MirrorVertical"), newImage);
            }

        };
        start(task);
    }

    @FXML
    public void renameAction() {
        try {
            if (imageChanged) {
                saveAction();
            }
            if (sourceFile == null) {
                return;
            }
            FileRenameController controller = (FileRenameController) openStage(Fxmls.FileRenameFxml);
            controller.set(sourceFile);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(newFile);
                });
                Platform.requestNextPulse();
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    public void fileRenamed(File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            popSuccessful();
            sourceFile = newFile;
            recordFileOpened(sourceFile);
            if (imageInformation != null) {
                imageInformation.setFile(sourceFile);
            }
            updateLabelsTitle();
            notifyLoad();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        if (sourceFile == null) {
            return;
        }
        if (!PopTools.askSure(getTitle(), message("Delete"), sourceFile.getAbsolutePath())) {
            return;
        }
        File focusFile = nextFile();
        if (focusFile == null) {
            focusFile = previousFile();
        }
        if (FileDeleteTools.delete(null, sourceFile)) {
            popSuccessful();
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (focusFile != null) {
                sourceFileChanged(focusFile);
            }
        } else {
            popFailed();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        imageView.setImage(image);
        setImageChanged(false);
        popInformation(message("Recovered"));
    }

    @FXML
    @Override
    public void pasteAction() {
        loadContentInSystemClipboard();
    }

    @FXML
    public void selectPixels() {
        ImageSelectPixelsController.open((BaseImageController) this);
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
    public void saveAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveButton != null && saveButton.isDisabled())) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        File srcFile = sourceFile;
        if (srcFile == null) {
            targetFile = saveCurrentFile();
            if (targetFile == null) {
                return;
            }
        } else {
            targetFile = srcFile;
        }
        if (imageInformation != null && imageInformation.isIsScaled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("SureSaveScaled") + "\n"
                    + message("OriginalSize") + ":" + (int) imageInformation.getWidth() + "x" + (int) imageInformation.getHeight() + "\n"
                    + message("CurrentSize") + ":" + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }
        }

        task = new FxSingletonTask<Void>(this) {

            private Image savedImage;
            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                savedImage = imageView.getImage();
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(savedImage, null);
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                needBackup = srcFile != null && UserConfig.getBoolean(baseName + "BackupWhenSave", true);
                if (needBackup) {
                    backup = addBackup(this, srcFile);
                }
                String format = FileNameTools.ext(targetFile.getName());
                if (framesNumber > 1) {
                    error = ImageFileWriters.writeFrame(this,
                            targetFile, frameIndex, bufferedImage, targetFile, null);
                    ok = error == null;
                } else {
                    ok = ImageFileWriters.writeImageFile(this,
                            bufferedImage, format, targetFile.getAbsolutePath());
                }
                if (!ok || task == null || isCancelled()) {
                    return false;
                }
                ImageFileInformation finfo = ImageFileInformation.create(this, targetFile);
                if (finfo == null || finfo.getImageInformation() == null) {
                    return false;
                }
                imageInformation = finfo.getImageInformation();
                return true;
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = targetFile;
                recordFileWritten(sourceFile);
                if (srcFile == null) {
                    if (savedImage != imageView.getImage()) {
                        ImageEditorController.openFile(sourceFile);
                    } else {
                        sourceFileChanged(sourceFile);
                    }
                } else {
                    image = savedImage;
                    imageView.setImage(image);
                    setImageChanged(false);
                    if (needBackup) {
                        if (backup != null && backup.getBackup() != null) {
                            popInformation(message("SavedAndBacked"));
                            FileBackupController.updateList(sourceFile);
                        } else {
                            popError(message("FailBackup"));
                        }
                    } else {
                        popInformation(sourceFile + "   " + message("Saved"));
                    }
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        ImageConverterController.open((BaseImageController) this);
    }

    public void saveImage(File srcFile, File newfile, Object imageToSave) {
        if (newfile == null || imageToSave == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

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
                    error = ImageFileWriters.writeFrame(this,
                            srcFile, frameIndex, bufferedImage, newfile, null);
                    return error == null;
                } else {
                    return ImageFileWriters.writeImageFile(this, bufferedImage, newfile);
                }
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Saved"));
                recordFileWritten(newfile);

                if (saveAsType == SaveAsType.Load) {
                    sourceFileChanged(newfile);

                } else if (saveAsType == SaveAsType.Open) {
                    ImageEditorController.openFile(newfile);

                }
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        ImageCanvasInputController controller = ImageCanvasInputController.open(this, baseTitle);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                Image canvas = controller.getCanvas();
                if (canvas != null) {
                    create(canvas);
                }
                controller.close();
            }
        });
    }

    public void create(Image inImage) {
        if (inImage == null) {
            return;
        }
        sourceFile = null;
        imageInformation = null;
        imageView.setImage(inImage);
        saveAction();
    }

    @FXML
    public void exampleAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        ImageExampleSelectController controller = ImageExampleSelectController.open(this, false);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                ImageItem item = controller.selectedItem();
                if (item == null) {
                    popError(message("SelectToHandle"));
                    return;
                }
                controller.close();
                create(item.readImage());
            }
        });
    }

    @FXML
    public void options() {
        ImageOptionsController.open((BaseImageController) this);
    }

}
