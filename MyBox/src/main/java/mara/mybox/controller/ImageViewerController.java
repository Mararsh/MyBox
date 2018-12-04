package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.fxml.FxmlImageTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.imagefile.ImageFileWriters;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageBaseController {

    protected double mouseX, mouseY;
    protected int xZoomStep = 50, yZoomStep = 50;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;

    @FXML
    protected TextField imageFile;
    @FXML
    protected HBox sourceBox;
    @FXML
    protected VBox contentBox;
    @FXML
    protected Button iButton, mButton, gButton, pButton, inButton, outButton, lButton, rButton,
            previousButton, nextButton, wButton, oButton;
    @FXML
    protected Button tButton, sButton, mrButton, mlButton, upButton, downButton, infoButton, metaButton;
    @FXML
    protected ToolBar toolbar, navBar, infoBar;
    @FXML
    protected ToggleGroup sortGroup;

    @Override
    protected void initializeNext() {
        try {
            if (toolbar != null && imageView != null) {
                toolbar.disableProperty().bind(
                        Bindings.isNull(imageView.imageProperty())
                );
            }
//            setTips();

            initializeNext2();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file.isDirectory()) {
            AppVaribles.setUserConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVaribles.setUserConfigValue(sourcePathKey, file.getParent());
        }
        loadImage(file, false);
    }

    @Override
    protected void afterInfoLoaded() {
        if (infoBar != null) {
            infoBar.setDisable(imageInformation == null);
        }
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }

    }

    @Override
    public void afterImageLoaded() {
        try {
            afterInfoLoaded();
            if (image == null) {
                return;
            }

            imageView.setPreserveRatio(true);
            imageView.setImage(image);
            xZoomStep = (int) image.getWidth() / 10;
            yZoomStep = (int) image.getHeight() / 10;

            fitSize();
            if (imageFile != null && sourceFile != null) {
                imageFile.setText(sourceFile.getName());
            }

            if (imageInformation != null && imageInformation.isIsSampled()) {
                handleSampledImage();
                if (imageFile != null && sourceFile != null) {
                    imageFile.setText(sourceFile.getName() + " " + getMessage("Sampled"));
                }
            } else {
                if (sourceFile != null) {
                    if (imageInformation != null && imageInformation.getIndex() > 0) {
                        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                                + " - " + getMessage("Image") + " " + imageInformation.getIndex());
                    } else {
                        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                    }
                } else {
                    getMyStage().setTitle(getBaseTitle());
                }
                if (bottomLabel != null) {
                    bottomLabel.setText("");
                }
            }

            if (sourceFile != null && navBar != null) {
                navBar.setDisable(false);
                checkImageNevigator();
            }

        } catch (Exception e) {
            logger.error(e.toString());
            imageView.setImage(null);
            alertInformation(AppVaribles.getMessage("NotSupported"));
        }
    }

    protected void handleSampledImage() {
//            logger.debug(availableMem + "  " + pixelsSize + "  " + requiredMem + " " + sampledWidth + " " + sampledSize);
        if (imageInformation.getIndex() > 0) {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                    + " - " + getMessage("Image") + " " + imageInformation.getIndex() + " " + getMessage("Sampled"));
        } else {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath() + " " + getMessage("Sampled"));
        }

        if (sizes == null) {
            if (bottomLabel != null) {
                bottomLabel.setText(getMessage("ImageSampled"));
            }
            return;
        }

        int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
        String msg = MessageFormat.format(getMessage("ImageTooLarge"),
                imageInformation.getWidth(), imageInformation.getHeight(), imageInformation.getColorChannels(),
                sizes.get("pixelsSize"), sizes.get("requiredMem"), sizes.get("availableMem"),
                (int) image.getWidth(), (int) image.getHeight(), sampledSize);

        if (bottomLabel != null) {
            bottomLabel.setText(msg);
        }

        VBox box = new VBox();
        Label label = new Label(msg);
        Hyperlink helpLink = new Hyperlink(getMessage("Help"));
        helpLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showHelp(event);
            }
        });
        box.getChildren().add(label);
        box.getChildren().add(helpLink);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(getMyStage().getTitle());
        alert.getDialogPane().setPrefWidth(800);
        alert.getDialogPane().setContent(box);
        alert.setContentText(msg);

        ButtonType buttonExit = new ButtonType(AppVaribles.getMessage("Exit"));
        ButtonType buttonSplit = new ButtonType(AppVaribles.getMessage("ImageSplit"));
        ButtonType buttonSample = new ButtonType(AppVaribles.getMessage("ImageSubsample"));
        ButtonType buttonView = new ButtonType(AppVaribles.getMessage("ImageViewer"));
        ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("SaveSampledImage"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonExit, buttonSample, buttonSplit, buttonView, buttonSave, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonExit) {
            closeStage();

        } else if (result.get() == buttonSplit) {
            ImageSplitController controller
                    = (ImageSplitController) reloadStage(CommonValues.ImageSplitFxml, AppVaribles.getMessage("ImageSplit"));
            controller.setSizes(sizes);
            controller.loadImage(sourceFile, image, imageInformation);

        } else if (result.get() == buttonSample) {
            ImageSampleController controller
                    = (ImageSampleController) reloadStage(CommonValues.ImageSampleFxml, AppVaribles.getMessage("ImageSubsample"));
            controller.setSizes(sizes);
            controller.loadImage(sourceFile, image, imageInformation);

        } else if (result.get() == buttonView) {
            ImageViewerController controller
                    = (ImageViewerController) reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
            controller.loadImage(sourceFile, image, imageInformation);
        } else if (result.get() == buttonSave) {
            saveAs();
        }

    }

    @Override
    protected void handleMultipleImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(getMessage("MultipleFramesImagesInfo"));

        ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSure) {
            final ImageFramesViewerController controller
                    = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml, false, true);
            controller.setBaseTitle(AppVaribles.getMessage("ImageFramesViewer"));
            controller.openFile(sourceFile);
        }
    }

    @FXML
    public void popImageInformation() {
        showImageInformation(imageInformation);
    }

    @FXML
    public void nextAction() {
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void previousAction() {
        if (previousFile != null) {
            loadImage(previousFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void browseAction() {
        openImagesBrowserInNew(sourceFile.getParentFile(), 16);
    }

    @FXML
    public void popMetaData() {
        showImageMetaData(imageInformation);
    }

    @FXML
    public void zoomIn() {
        double currentWidth = imageView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = imageView.getImage().getWidth();
        }
        imageView.setFitWidth(currentWidth + xZoomStep);
        double currentHeight = imageView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = imageView.getImage().getHeight();
        }
        imageView.setFitHeight(currentHeight + yZoomStep);
    }

    @FXML
    public void zoomOut() {
        double currentWidth = imageView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = imageView.getImage().getWidth();
        }
        if (currentWidth <= xZoomStep) {
            return;
        }
        imageView.setFitWidth(currentWidth - xZoomStep);
        double currentHeight = imageView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = imageView.getImage().getHeight();
        }
        if (currentHeight <= yZoomStep) {
            return;
        }
        imageView.setFitHeight(currentHeight - yZoomStep);
    }

    @FXML
    public void imageSize() {
        imageView.setFitWidth(-1);
        imageView.setFitHeight(-1);
    }

    @FXML
    public void paneSize() {
        imageView.setFitWidth(scrollPane.getWidth() - 1);
        imageView.setFitHeight(scrollPane.getHeight() - 5);
    }

    public void fitSize() {
        if (imageView.getImage() == null) {
            return;
        }
        if (scrollPane.getHeight() < imageView.getImage().getHeight()
                || scrollPane.getWidth() < imageView.getImage().getWidth()) {
            paneSize();
        } else {
            imageSize();
        }
    }

    @FXML
    public void moveRight() {
        FxmlTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        currentAngle = (currentAngle + rotateAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void rotateLeft() {
        currentAngle = (360 - rotateAngle + currentAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void turnOver() {
        currentAngle = (180 + currentAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void straighten() {
        currentAngle = 0;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void saveAs() {
        try {
            if (imageInformation.isIsSampled()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(getMessage("SureSaveSampled"));

                ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
                alert.getButtonTypes().setAll(buttonSure, buttonCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                }
            }

            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(image);
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            popInformation(AppVaribles.getMessage("Successful"));
                        }
                    });
                    return null;
                }
            };
            openHandlingStage(saveTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void setQuickTips() {
        if (iButton != null) {
            FxmlTools.quickTooltip(iButton, new Tooltip(AppVaribles.getMessage("ImageInformation")));
            FxmlTools.quickTooltip(mButton, new Tooltip(AppVaribles.getMessage("ImageMetaData")));
        }
        if (pButton != null) {
            FxmlTools.quickTooltip(pButton, new Tooltip(AppVaribles.getMessage("PaneSize")));
            FxmlTools.quickTooltip(gButton, new Tooltip(AppVaribles.getMessage("ImageSize")));
            FxmlTools.quickTooltip(inButton, new Tooltip(AppVaribles.getMessage("ZoomIn")));
            FxmlTools.quickTooltip(outButton, new Tooltip(AppVaribles.getMessage("ZoomOut")));
            FxmlTools.quickTooltip(lButton, new Tooltip(AppVaribles.getMessage("RotateLeft")));
            FxmlTools.quickTooltip(rButton, new Tooltip(AppVaribles.getMessage("RotateRight")));
            FxmlTools.quickTooltip(tButton, new Tooltip(AppVaribles.getMessage("TurnOver")));
            FxmlTools.quickTooltip(sButton, new Tooltip(AppVaribles.getMessage("Straighten")));
        }
        if (mrButton != null) {
            FxmlTools.quickTooltip(mrButton, new Tooltip(AppVaribles.getMessage("MoveRight")));
            FxmlTools.quickTooltip(mlButton, new Tooltip(AppVaribles.getMessage("MoveLeft")));
            FxmlTools.quickTooltip(upButton, new Tooltip(AppVaribles.getMessage("MoveUp")));
            FxmlTools.quickTooltip(downButton, new Tooltip(AppVaribles.getMessage("MoveDown")));
        }
    }

    public void setTips() {
        if (iButton != null) {
            iButton.setTooltip(new Tooltip(AppVaribles.getMessage("ImageInformation")));
            mButton.setTooltip(new Tooltip(AppVaribles.getMessage("ImageMetaData")));
        }
        if (pButton != null) {
            pButton.setTooltip(new Tooltip(AppVaribles.getMessage("PaneSize")));
            gButton.setTooltip(new Tooltip(AppVaribles.getMessage("ImageSize")));
            inButton.setTooltip(new Tooltip(AppVaribles.getMessage("ZoomIn")));
            outButton.setTooltip(new Tooltip(AppVaribles.getMessage("ZoomOut")));
            lButton.setTooltip(new Tooltip(AppVaribles.getMessage("RotateRight")));
            rButton.setTooltip(new Tooltip(AppVaribles.getMessage("RotateRight")));
            tButton.setTooltip(new Tooltip(AppVaribles.getMessage("TurnOver")));
            sButton.setTooltip(new Tooltip(AppVaribles.getMessage("Straighten")));
        }
        if (mrButton != null) {
            mrButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveRight")));
            mlButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveLeft")));
            upButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveUp")));
            downButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveDown")));
        }
    }

    public void checkImageNevigator() {
        checkImageNevigator(sourceFile);
    }

    public void checkImageNevigator(File currentfile) {
        try {
            if (currentfile == null) {
                previousFile = null;
                previousButton.setDisable(true);
                nextFile = null;
                nextButton.setDisable(true);
                return;
            }
            File path = currentfile.getParentFile();
            List<File> sortedFiles = new ArrayList<>();
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    sortedFiles.add(file);
                }
            }
            RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
            if (getMessage("OriginalFileName").equals(sort.getText())) {
                FileTools.sortFiles(sortedFiles, FileTools.FileSortType.FileName);

            } else if (getMessage("CreateTime").equals(sort.getText())) {
                FileTools.sortFiles(sortedFiles, FileTools.FileSortType.CreateTime);

            } else if (getMessage("ModifyTime").equals(sort.getText())) {
                FileTools.sortFiles(sortedFiles, FileTools.FileSortType.ModifyTime);

            } else if (getMessage("Size").equals(sort.getText())) {
                FileTools.sortFiles(sortedFiles, FileTools.FileSortType.Size);
            }

            for (int i = 0; i < sortedFiles.size(); i++) {
                if (sortedFiles.get(i).getAbsoluteFile().equals(currentfile.getAbsoluteFile())) {
                    if (i < sortedFiles.size() - 1) {
                        nextFile = sortedFiles.get(i + 1);
                        nextButton.setDisable(false);
                    } else {
                        nextFile = null;
                        nextButton.setDisable(true);
                    }
                    if (i > 0) {
                        previousFile = sortedFiles.get(i - 1);
                        previousButton.setDisable(false);
                    } else {
                        previousFile = null;
                        previousButton.setDisable(true);
                    }
                    return;
                }
            }
            previousFile = null;
            previousButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    public Button getiButton() {
        return iButton;
    }

    public void setiButton(Button iButton) {
        this.iButton = iButton;
    }

    public Button getmButton() {
        return mButton;
    }

    public void setmButton(Button mButton) {
        this.mButton = mButton;
    }

    public Button getgButton() {
        return gButton;
    }

    public void setgButton(Button gButton) {
        this.gButton = gButton;
    }

    public Button getpButton() {
        return pButton;
    }

    public void setpButton(Button pButton) {
        this.pButton = pButton;
    }

    public Button getInButton() {
        return inButton;
    }

    public void setInButton(Button inButton) {
        this.inButton = inButton;
    }

    public Button getOutButton() {
        return outButton;
    }

    public void setOutButton(Button outButton) {
        this.outButton = outButton;
    }

    public Button getlButton() {
        return lButton;
    }

    public void setlButton(Button lButton) {
        this.lButton = lButton;
    }

    public Button getrButton() {
        return rButton;
    }

    public void setrButton(Button rButton) {
        this.rButton = rButton;
    }

    public int getxZoomStep() {
        return xZoomStep;
    }

    public void setxZoomStep(int xZoomStep) {
        this.xZoomStep = xZoomStep;
    }

    public int getyZoomStep() {
        return yZoomStep;
    }

    public void setyZoomStep(int yZoomStep) {
        this.yZoomStep = yZoomStep;
    }

}
