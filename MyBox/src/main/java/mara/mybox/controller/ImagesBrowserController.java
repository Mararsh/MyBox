package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.ImageInformation;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesBrowserController extends ImageViewerController {

    private final ObservableList<File> imageFileList = FXCollections.observableArrayList();
    private int rowsNum, colsNum, filesNumber;
    private List<Pane> imagePaneList;
    private List<ImageViewerIController> imageControllerList;
    protected List<File> nextFiles, previousFiles;
    protected List<Integer> selectedFiles;
    protected String ImageSortTypeKey = "ImageSortType";
    protected int maxShow = 100;

    @FXML
    protected VBox imagesPane;
    @FXML
    protected Button selectButton, viewButton;
    @FXML
    protected ToolBar setBar, fileOpBar;
    @FXML
    protected ComboBox<String> colsnumBox, filesBox;
    @FXML
    private HBox opBox;

    public ImagesBrowserController() {
        TipsLabelKey = "ImageBrowserTips";
    }

    @Override
    protected void initializeNext2() {
        try {
            List<String> values = Arrays.asList("3", "4", "2", "5", "6", "1", "7", "8", "9", "10",
                    "16", "25", "20", "12", "15");
            colsnumBox.getItems().addAll(values);
            colsnumBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        colsNum = Integer.valueOf(newValue);
                        if (colsNum > 0) {
                            colsnumBox.getEditor().setStyle(null);
                            makeImagesPane(); // ????? colsnumBox Can not be editable due to unkown issue cause by this call.
                        } else {
                            colsnumBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        colsnumBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            List<String> fvalues = Arrays.asList("9", "4", "3", "8", "6", "10", "16", "2", "5", "12", "15", "25",
                    "36", "30", "24");
            filesBox.getItems().addAll(fvalues);
            filesBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        filesNumber = Integer.valueOf(newValue);
                        if (filesNumber > 0) {
                            filesBox.getEditor().setStyle(null);
                            makeNevigator(true);
                            makeImagesPane();
                        } else {
                            filesBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        filesBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            FxmlTools.quickTooltip(selectButton, new Tooltip(AppVaribles.getMessage("ImagesMutipleTips")));

            opBox.disableProperty().bind(
                    Bindings.isEmpty(imageFileList)
            );
            navBox.disableProperty().bind(
                    Bindings.isEmpty(imageFileList)
            );
            setBar.disableProperty().bind(
                    Bindings.isEmpty(imageFileList)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectImages(ActionEvent event) {
        try {

            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            isSettingValues = true;
            imageFileList.clear();
            imageFileList.addAll(files);
            filesNumber = imageFileList.size();
            if (!filesBox.getItems().contains(filesNumber + "")) {
                filesBox.getItems().add(0, filesNumber + "");
            }
            filesBox.getSelectionModel().select(filesNumber + "");
            colsNum = (int) Math.sqrt(filesNumber);
            colsNum = Math.max(colsNum, (int) (filesNumber / colsNum));
            colsnumBox.getSelectionModel().select(colsNum + "");
            isSettingValues = false;
            makeNevigator(false);
            makeImagesPane();

            String path = imageFileList.get(0).getParent();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            AppVaribles.setUserConfigValue(sourcePathKey, path);

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
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.paneSize();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.paneSize();
            }
        }
    }

    @FXML
    @Override
    public void imageSize() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.imageSize();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.imageSize();
            }
        }
    }

    @FXML
    @Override
    public void zoomIn() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.zoomIn();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.zoomIn();
            }
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.zoomOut();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.zoomOut();
            }
        }
    }

    @FXML
    @Override
    public void moveRight() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.moveRight();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.moveRight();
            }
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.moveLeft();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.moveLeft();
            }
        }
    }

    @FXML
    @Override
    public void moveUp() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.moveUp();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.moveUp();
            }
        }
    }

    @FXML
    @Override
    public void moveDown() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.moveDown();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.moveDown();
            }
        }
    }

    @FXML
    @Override
    public void rotateLeft() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.rotateLeft();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.rotateLeft();
            }
        }
    }

    @FXML
    @Override
    public void rotateRight() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.rotateRight();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.rotateRight();
            }
        }
    }

    @FXML
    @Override
    public void turnOver() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.turnOver();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.turnOver();
            }
        }
    }

    @FXML
    @Override
    public void straighten() {
        if (imageControllerList == null) {
            return;
        }
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (int index : selectedFiles) {
                ImageViewerIController c = imageControllerList.get(index);
                if (c != null) {
                    c.straighten();
                }
            }
        } else {
            for (ImageViewerIController c : imageControllerList) {
                c.straighten();
            }
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFiles != null) {
            previousFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(nextFiles);
            makeNevigator(false);
            makeImagesPane();
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFiles != null) {
            nextFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(previousFiles);
            makeNevigator(false);
            makeImagesPane();
        }
    }

    @FXML
    public void viewAction() {
        try {
            if (selectedFiles == null || selectedFiles.isEmpty()) {
                fileOpBar.setDisable(true);
                return;
            }
            int index = selectedFiles.get(0);
            ImageViewerIController c = imageControllerList.get(index);
            if (c != null) {
                File file = c.getImageInformation().getImageFileInformation().getFile();
                FxmlStage.openImageViewer(getClass(), null, file);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void renameAction() {
        try {
            if (selectedFiles == null || selectedFiles.isEmpty()) {
                fileOpBar.setDisable(true);
                return;
            }
            int index = selectedFiles.get(0);
            ImageViewerIController c = imageControllerList.get(index);
            if (c != null) {
                File file = c.getImageInformation().getImageFileInformation().getFile();
                if (renameFile(file) != null) {
                    imageFileList.remove(file);
                    makeNevigator(true);
                    makeImagesPane();
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        try {
            if (selectedFiles == null || selectedFiles.isEmpty()) {
                fileOpBar.setDisable(true);
                return;
            }
            int index = selectedFiles.get(0);
            ImageViewerIController c = imageControllerList.get(index);
            if (c != null) {
                File file = c.getImageInformation().getImageFileInformation().getFile();
                if (deleteFile(file)) {
                    imageFileList.remove(file);
                    makeNevigator(true);
                    makeImagesPane();
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void infoAction() {
        try {
            if (selectedFiles == null || selectedFiles.isEmpty()) {
                fileOpBar.setDisable(true);
                return;
            }
            int index = selectedFiles.get(0);
            ImageViewerIController c = imageControllerList.get(index);
            if (c != null) {
                showImageInformation(c.getImageInformation());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeImagesPane() {
        if (colsNum <= 0) {
            return;
        }
        imagesPane.getChildren().clear();
        imagePaneList = new ArrayList();
        imageControllerList = new ArrayList();
        selectedFiles = new ArrayList();
        rowsNum = 0;
        fileOpBar.setDisable(true);
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        try {
            int num = imageFileList.size();
            HBox line = new HBox();
            for (int i = 0; i < num; i++) {
                if (i % colsNum == 0) {
                    line = new HBox();
                    line.setAlignment(Pos.TOP_CENTER);
                    line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    line.setSpacing(5);
                    imagesPane.getChildren().add(line);
                    VBox.setVgrow(line, Priority.ALWAYS);
                    HBox.setHgrow(line, Priority.ALWAYS);
                    rowsNum++;
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageViewerIFxml), AppVaribles.CurrentBundle);
                Pane imagePane = fxmlLoader.load();
                ImageViewerIController imageController = fxmlLoader.getController();
                imageController.setMyStage(myStage);
                imageController.loadImage(imageFileList.get(i), false);
                imageController.setParentController(this);
                imagePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(imagePane, Priority.ALWAYS);
                HBox.setHgrow(imagePane, Priority.ALWAYS);
                imagePane.setPadding(new Insets(5, 5, 5, 5));

                line.getChildren().add(imagePane);
                imagePaneList.add(imagePane);
                imageControllerList.add(imageController);
            }

            double w = imagesPane.getWidth() / colsNum - 5;
            double h = imagesPane.getHeight() / rowsNum - 5;
            for (int i = 0; i < imagePaneList.size(); i++) {
                Pane p = imagePaneList.get(i);
                p.setPrefWidth(w);
                p.setPrefHeight(h);
                final int index = i;
                p.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        ImageInformation info = imageControllerList.get(index).getImageInformation();
                        File file = info.getImageFileInformation().getFile();
                        String str = info.getFilename() + " "
                                + AppVaribles.getMessage("Format") + ":" + info.getImageFormat() + "  "
                                + AppVaribles.getMessage("Pixels") + ":" + info.getWidth() + "x" + info.getHeight() + "  "
                                + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(file.length()) + "  "
                                + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(file.lastModified());
                        bottomLabel.setText(str);
                    }
                });
                p.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Pane pane = imagePaneList.get(index);

                        Integer o = new Integer(index);
                        if (selectedFiles.contains(o)) {
                            selectedFiles.remove(o);
                            pane.setStyle(null);
                        } else {
                            selectedFiles.add(o);
                            pane.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                        }
                        fileOpBar.setDisable(selectedFiles.isEmpty());
                        viewButton.setDisable(selectedFiles.size() > 1);
                        infoButton.setDisable(selectedFiles.size() > 1);
                        renameButton.setDisable(selectedFiles.size() > 1);

                        if (event.getClickCount() > 1) {
                            File file = imageControllerList.get(index).getImageInformation().getImageFileInformation().getFile();
                            FxmlStage.openImageViewer(getClass(), null, file);
                        }
                    }
                });
            }
            paneSize();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void makeNevigator(boolean reload) {

        if ((imageFileList == null || imageFileList.isEmpty()) && (filesNumber <= 0)) {
            previousFiles = null;
            nextFiles = null;
            return;
        }
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        LoadingController loadingController = null;
        try {

            loadingController = openHandlingStage(Modality.WINDOW_MODAL);

            File firstFile = imageFileList.get(0);
            File path = firstFile.getParentFile();
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

            if (reload) {
                int start = sortedFiles.indexOf(firstFile);

                imageFileList.clear();
                for (int k = start; k < sortedFiles.size(); k++) {
                    imageFileList.add(sortedFiles.get(k));
                    if (imageFileList.size() == filesNumber) {
                        break;
                    }
                }
                if (imageFileList.size() < filesNumber) {
                    for (int k = start - 1; k >= 0; k--) {
                        imageFileList.add(0, sortedFiles.get(k));
                        if (imageFileList.size() == filesNumber) {
                            break;
                        }
                    }
                }
            }

            if (sortedFiles.size() > filesNumber) {

                for (int i = 0; i < sortedFiles.size(); i++) {
                    if (sortedFiles.get(i).getAbsoluteFile().equals(firstFile.getAbsoluteFile())) {
                        if (i < sortedFiles.size() - imageFileList.size()) {
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
                        if (loadingController != null && loadingController.getMyStage() != null) {
                            loadingController.getMyStage().close();
                        }
                        return;
                    }
                }
            }
            previousFiles = null;
            previousButton.setDisable(true);
            nextFiles = null;
            nextButton.setDisable(true);
            if (loadingController != null && loadingController.getMyStage() != null) {
                loadingController.getMyStage().close();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            if (loadingController != null && loadingController.getMyStage() != null) {
                loadingController.getMyStage().close();
            }
        }
    }

    public void loadImages(File path, int number) {
        try {
            imageFileList.clear();
            filesNumber = 0;
            if (path == null || !path.isDirectory() || !path.exists() || number <= 0) {
                return;
            }
            for (File file : path.listFiles()) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    imageFileList.add(file);
                }
                if (imageFileList.size() == number || imageFileList.size() >= maxShow) {
                    break;
                }
            }
            if (imageFileList.isEmpty()) {
                return;
            }
            isSettingValues = true;
            filesNumber = imageFileList.size();
            if (!filesBox.getItems().contains(filesNumber + "")) {
                filesBox.getItems().add(0, filesNumber + "");
            }
            filesBox.getSelectionModel().select(filesNumber + "");
            colsNum = (int) Math.sqrt(filesNumber);
            colsNum = Math.max(colsNum, (int) (filesNumber / colsNum));
            if (!colsnumBox.getItems().contains(colsNum + "")) {
                colsnumBox.getItems().add(0, colsNum + "");
            }
            colsnumBox.getSelectionModel().select(colsNum + "");
            isSettingValues = false;
            makeNevigator(false);
            makeImagesPane();

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void loadImages(List<String> fileNames, int cols) {
        try {
            imageFileList.clear();
            if (fileNames == null || cols <= 0) {
                return;
            }
            int len = Math.min(maxShow, fileNames.size());
            for (int i = 0; i < len; i++) {
                File file = new File(fileNames.get(i));
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    imageFileList.add(file);
                }
            }
            if (imageFileList.isEmpty()) {
                return;
            }
            isSettingValues = true;
            filesNumber = imageFileList.size();
            if (!filesBox.getItems().contains(filesNumber + "")) {
                filesBox.getItems().add(0, filesNumber + "");
            }
            filesBox.getSelectionModel().select(filesNumber + "");
            colsNum = cols;
            if (!colsnumBox.getItems().contains(colsNum + "")) {
                colsnumBox.getItems().add(0, colsNum + "");
            }
            colsnumBox.getSelectionModel().select(colsNum + "");
            isSettingValues = false;
            makeNevigator(false);
            makeImagesPane();

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void loadImages(List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            int cols = (int) Math.sqrt(fileNames.size());
            cols = Math.max(cols, (int) (fileNames.size() / cols));
            loadImages(fileNames, cols);

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

}
