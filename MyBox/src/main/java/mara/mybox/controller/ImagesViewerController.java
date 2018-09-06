package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesViewerController extends ImageViewerController {

    private List<File> imageFileList;
    private int rowsNum, colsNum, filesNumber;
    private List<Pane> imagePaneList;
    private List<ImageViewerIController> imageControllerList;
    protected List<File> nextFiles, previousFiles;
    protected String ImageSortTypeKey = "ImageSortType";
    private int cols;

    @FXML
    protected VBox imagesPane;
    @FXML
    protected Button selectButton;
    @FXML
    protected ToolBar setBar, opBar;
    @FXML
    protected ComboBox colsBox;
    @FXML
    private CheckBox eachCheck;

    @Override
    protected void initializeNext2() {
        try {
            fileExtensionFilter = CommonValues.ImageExtensionFilter;

            List<Integer> values = Arrays.asList(3, 4, 2, 5, 6, 1, 7, 8, 9, 10, 12, 16);
            colsBox.getItems().addAll(values);
            colsBox.setVisibleRowCount(values.size());
            colsBox.valueProperty().addListener(new ChangeListener<Integer>() {
                @Override
                public void changed(ObservableValue ov, Integer oldValue, Integer newValue) {
                    try {
                        cols = newValue;
                        createImagesPane();
                    } catch (Exception e) {
                        cols = -1;
                    }
                }
            });

            eachCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    createImagesPane();
                }
            });

            FxmlTools.quickTooltip(selectButton, new Tooltip(AppVaribles.getMessage("ImagesMutipleTips")));
            setTips();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectImages(ActionEvent event) {
        try {
            opBar.setDisable(true);
            navBar.setDisable(true);
            setBar.setDisable(true);

            final FileChooser fileChooser = new FileChooser();
            String defaultPath = AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"));
            fileChooser.setInitialDirectory(new File(AppVaribles.getConfigValue(sourcePathKey, defaultPath)));
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            imageFileList = fileChooser.showOpenMultipleDialog(getMyStage());
            makeImagesPane();
            filesNumber = 0;
            if (imageFileList == null || imageFileList.isEmpty()) {
                return;
            }
            filesNumber = imageFileList.size();
            checkImagesNevigator();
            String path = imageFileList.get(0).getParent();
            AppVaribles.setConfigValue("LastPath", path);
            AppVaribles.setConfigValue(sourcePathKey, path);
            bottomLabel.setText(AppVaribles.getMessage("ImagesComments"));

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void paneSize() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.paneSize();
        }
    }

    @FXML
    @Override
    public void imageSize() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.imageSize();
        }
    }

    @FXML
    @Override
    public void zoomIn() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.zoomIn();
        }

    }

    @FXML
    @Override
    public void zoomOut() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.zoomOut();
        }

    }

    @FXML
    @Override
    public void moveRight() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.moveRight();
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.moveLeft();
        }
    }

    @FXML
    @Override
    public void moveUp() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.moveUp();
        }
    }

    @FXML
    @Override
    public void moveDown() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.moveDown();
        }
    }

    @FXML
    @Override
    public void rotateLeft() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.rotateLeft();
        }

    }

    @FXML
    @Override
    public void rotateRight() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.rotateRight();
        }

    }

    @FXML
    @Override
    public void turnOver() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.turnOver();
        }

    }

    @FXML
    @Override
    public void straighten() {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.straighten();
        }

    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFiles != null) {
            previousFiles = imageFileList;
            imageFileList = nextFiles;
            checkImagesNevigator();
            createImagesPane();
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFiles != null) {
            nextFiles = imageFileList;
            imageFileList = previousFiles;
            checkImagesNevigator();
            createImagesPane();
        }
    }

    private void makeImagesPane() {
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        try {
            int num = imageFileList.size();
            cols = (int) Math.sqrt(num);
            colsBox.getSelectionModel().select(cols);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeImagesPane(int cols) {
        colsBox.getSelectionModel().select(new Integer(cols));
    }

    private void createImagesPane() {
        if (cols <= 0) {
            return;
        }
        imagesPane.getChildren().clear();
        imagePaneList = new ArrayList();
        imageControllerList = new ArrayList();
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        try {
            int num = imageFileList.size();

            HBox line = new HBox();
            for (int i = 0; i < num; i++) {
                if (i % cols == 0) {
                    line = new HBox();
                    line.setAlignment(Pos.TOP_CENTER);
                    line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    line.setSpacing(10);
                    imagesPane.getChildren().add(line);
                    VBox.setVgrow(line, Priority.ALWAYS);
                    HBox.setHgrow(line, Priority.ALWAYS);
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageViewerIFxml), AppVaribles.CurrentBundle);
                Pane imagePane = fxmlLoader.load();
                ImageViewerIController imageController = fxmlLoader.getController();
                if (!eachCheck.isSelected()) {
                    imageController.removeButtons();
                }
                imageController.loadImage(imageFileList.get(i), false);
                imageController.setParentController(this);

                imagePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(imagePane, Priority.ALWAYS);
                HBox.setHgrow(imagePane, Priority.ALWAYS);
                line.getChildren().add(imagePane);
                imagePane.setPrefWidth(line.getWidth() / cols - 10);
//                imagePane.setPrefHeight(imagesPane.getHeight() / 2 - 10);

                imagePaneList.add(imagePane);
                imageControllerList.add(imageController);
            }

            paneSize();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkImagesNevigator() {
        if (imageFileList == null || imageFileList.isEmpty()) {
            previousFiles = null;
            nextFiles = null;
            opBar.setDisable(true);
            navBar.setDisable(true);
            setBar.setDisable(true);
            return;
        }
        opBar.setDisable(false);
        navBar.setDisable(false);
        setBar.setDisable(false);
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        Stage handlingStage = null;
        try {

            handlingStage = openHandlingStage(Modality.WINDOW_MODAL);

            File firstFile = imageFileList.get(0);
            File path = firstFile.getParentFile();
            List<File> sortedFiles = new ArrayList<>();
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    sortedFiles.add(file);
                }
            }
            if (sortedFiles.size() > filesNumber) {
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
                    if (sortedFiles.get(i).getAbsoluteFile().equals(firstFile.getAbsoluteFile())) {
                        if (i < sortedFiles.size() - imageFileList.size() - 1) {
                            nextFiles = new ArrayList();
                            for (int k = i + imageFileList.size(); k < sortedFiles.size(); k++) {
                                nextFiles.add(sortedFiles.get(k));
                                if (nextFiles.size() == filesNumber) {
                                    break;
                                }
                            }
                            nextButton.setDisable(false);
                        } else {
                            nextFiles = null;
                            nextButton.setDisable(true);
                        }
                        if (i > 0) {
                            previousFiles = new ArrayList();
                            for (int k = i - 1; k >= 0; k--) {
                                previousFiles.add(0, sortedFiles.get(k));
                                if (previousFiles.size() == filesNumber) {
                                    break;
                                }
                            }
                            previousButton.setDisable(false);
                        } else {
                            previousFiles = null;
                            previousButton.setDisable(true);
                        }
                        if (handlingStage != null) {
                            handlingStage.close();
                        }
                        return;
                    }
                }
            }
            previousFiles = null;
            previousButton.setDisable(true);
            nextFiles = null;
            nextButton.setDisable(true);
            if (handlingStage != null) {
                handlingStage.close();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            if (handlingStage != null) {
                handlingStage.close();
            }
        }
    }

    public void loadImages(File path, int number) {
        try {
            opBar.setDisable(true);
            navBar.setDisable(true);
            setBar.setDisable(true);

            if (path == null || !path.isDirectory() || !path.exists() || number <= 0) {
                return;
            }
            imageFileList = new ArrayList<>();
            for (File file : path.listFiles()) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    imageFileList.add(file);
                }
                if (imageFileList.size() == number) {
                    break;
                }
            }
            makeImagesPane();
            filesNumber = imageFileList.size();
            if (imageFileList.isEmpty()) {
                return;
            }

            bottomLabel.setText(AppVaribles.getMessage("ImagesComments"));
            checkImagesNevigator();
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void loadImages(List<String> fileNames, int cols) {
        try {
            opBar.setDisable(true);
            navBar.setDisable(true);
            setBar.setDisable(true);

            if (fileNames == null) {
                return;
            }
            imageFileList = new ArrayList<>();
            for (String name : fileNames) {
                File file = new File(name);
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    imageFileList.add(file);
                }
            }
            makeImagesPane(cols);
            logger.debug(cols);
            filesNumber = imageFileList.size();
            if (imageFileList.isEmpty()) {
                return;
            }
            bottomLabel.setText(AppVaribles.getMessage("ImagesComments"));
            checkImagesNevigator();
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

}
