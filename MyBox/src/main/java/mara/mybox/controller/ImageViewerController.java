package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageBaseController {

    protected double mouseX, mouseY;
    protected int zoomStep = 10;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;

    @FXML
    protected TextField imageFile;
    @FXML
    protected HBox sourceBox;
    @FXML
    protected VBox contentBox;
    @FXML
    protected Button iButton, mButton, gButton, pButton, inButton, outButton, lButton, rButton, previousButton, nextButton;
    @FXML
    protected Button tButton, sButton, mrButton, mlButton, upButton, downButton, infoButton, metaButton;
    @FXML
    protected ToolBar toolbar, navBar, infoBar;
    @FXML
    protected ToggleGroup sortGroup;

    @Override
    protected void initializeNext2() {
        try {

            toolbar.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
//            setTips();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file.isDirectory()) {
            AppVaribles.setConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVaribles.setConfigValue(sourcePathKey, file.getParent());
        }
        loadImage(file, false);
    }

    @Override
    public void afterImageLoaded() {
        try {
            if (image == null) {
                return;
            }
            imageView.setPreserveRatio(true);
            imageView.setImage(image);
            fitSize();
            if (imageFile != null && sourceFile != null) {
                imageFile.setText(sourceFile.getName());
            }
            if (infoButton != null) {
                infoButton.setDisable(imageInformation == null);
            }
            if (metaButton != null) {
                metaButton.setDisable(imageInformation == null);
            }

            if (sourceFile != null && navBar != null) {
                navBar.setDisable(false);
                checkImageNevigator();
            }
            if (infoBar != null && imageInformation != null) {
                infoBar.setDisable(false);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            imageView.setImage(null);
            alertInformation(AppVaribles.getMessage("NotSupported"));
        }
    }

//    @FXML
//    public void imageMousePressed(MouseEvent event) {
//        mouseX = event.getX();
//        mouseY = event.getY();
//    }
//    @FXML
//    public void imageMouseReleased(MouseEvent event) {
//        FxmlTools.setScrollPane(scrollPane, mouseX - event.getX(), mouseY - event.getY());
//    }
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
            currentWidth = image.getWidth();
        }
        imageView.setFitWidth(currentWidth * (1 + zoomStep / 100.0f));
        double currentHeight = imageView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = image.getHeight();
        }
        imageView.setFitHeight(currentHeight * (1 + zoomStep / 100.0f));
    }

    @FXML
    public void zoomOut() {
        double currentWidth = imageView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = image.getWidth();
        }
        imageView.setFitWidth(currentWidth * (1 - zoomStep / 100.0f));
        double currentHeight = imageView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = image.getHeight();
        }
        imageView.setFitHeight(currentHeight * (1 - zoomStep / 100.0f));
    }

    @FXML
    public void imageSize() {
        imageView.setFitHeight(-1);
        imageView.setFitWidth(-1);
    }

    @FXML
    public void paneSize() {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
    }

    public void fitSize() {
        if (imageView.getImage() == null) {
            return;
        }
        if (scrollPane.getHeight() < imageView.getImage().getHeight()) {
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

    public void loadImage(final String fileName) {
        try {
            sourceFile = new File(fileName).getAbsoluteFile(); // Must convert to AbsoluteFile!
            if (sourceFileInput != null) {
                sourceFileInput.setText(sourceFile.getAbsolutePath());
            } else {
                loadImage(sourceFile, false);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadImage(Image inImage) {
        sourceFile = null;
        imageInformation = null;
        image = inImage;
        afterImageLoaded();
    }

    public void loadImage(File sourceFile, Image image, ImageFileInformation imageInformation) {
        this.sourceFile = sourceFile;
        this.imageInformation = imageInformation;
        this.image = image;
        afterImageLoaded();
    }

    public void showImageInformation(ImageFileInformation info) {
        try {
            if (info == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            final ImageInformationController controller = fxmlLoader.getController();
            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            imageInformationStage.setTitle(getMyStage().getTitle());
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

            controller.loadInformation(info);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageMetaData(ImageFileInformation info) {
        try {
            if (info == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageMetaDataFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            final ImageMetaDataController controller = fxmlLoader.getController();
            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            imageInformationStage.setTitle(getMyStage().getTitle());
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

            controller.loadData(info);

        } catch (Exception e) {
            logger.error(e.toString());
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

}
