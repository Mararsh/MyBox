package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-21
 * @License Apache License Version 2.0
 */
public abstract class ImagesBrowserController_Load extends ImageViewerController {

    protected final ObservableList<File> imageFileList = FXCollections.observableArrayList();
    protected final ObservableList<ImageInformation> tableData = FXCollections.observableArrayList();
    protected int rowsNum, colsNum, filesNumber, thumbWidth, currentIndex;
    protected List<VBox> imageBoxList;
    protected List<ScrollPane> imageScrollList;
    protected List<ImageView> imageViewList;
    protected List<Label> imageTitleList;

    protected TableView<ImageInformation> tableView;
    protected TableColumn<ImageInformation, ImageInformation> imageColumn;
    protected TableColumn<ImageInformation, String> fileColumn, formatColumn, pixelsColumn, csColumn, loadColumn;
    protected TableColumn<ImageInformation, Integer> indexColumn;
    protected TableColumn<ImageInformation, Long> fileSizeColumn, modifiedTimeColumn, createTimeColumn;
    protected TableColumn<ImageInformation, Boolean> isScaledColumn, isMutipleFramesColumn;

    protected List<File> nextFiles, previousFiles;
    protected List<Integer> selectedIndexes;
    protected int maxShow = 100;
    protected File path;
    protected DisplayMode displayMode;

    protected enum DisplayMode {
        ImagesGrid, FilesList, ThumbnailsList, None
    }

    @FXML
    protected VBox imagesPane, mainBox, viewBox, gridOptionsBox;
    @FXML
    protected ComboBox<String> colsnumBox, filesBox, thumbWidthSelector;
    @FXML
    protected CheckBox saveRotationCheck;
    @FXML
    protected Label totalLabel;
    @FXML
    protected ToggleGroup popGroup;
    @FXML
    protected Button zoomOutAllButton, zoomInAllButton, imageSizeAllButton, paneSizeAllButton;
    @FXML
    protected ImageViewerController imageController;

    @FXML
    @Override
    public void selectSourcePath() {
        File defaultPath = UserConfig.getPath(baseName + "SourcePath");
        selectSourcePath(defaultPath);
    }

    @Override
    public void selectSourcePath(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            fileChooser.setTitle("Select multiple files");
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileOpened(files.get(0));

            loadImages(files);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public void loadFiles(List<String> fileNames) {
        try {
            List<File> files = new ArrayList<>();
            for (int i = 0; i < fileNames.size(); ++i) {
                File file = new File(fileNames.get(i));
                files.add(file);
            }
            loadImages(files);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<File> files) {
        try {
            imageFileList.clear();
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); ++i) {
                    File file = files.get(i);
                    if (file.exists() && file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() >= maxShow) {
                            break;
                        }
                    }
                }
                filesNumber = imageFileList.size();
                colsNum = (int) Math.sqrt(filesNumber);
                colsNum = Math.max(colsNum, filesNumber / colsNum);
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<File> files, int cols) {
        try {
            imageFileList.clear();
            colsNum = cols;
            if (files != null && cols > 0) {
                for (int i = 0; i < files.size(); ++i) {
                    File file = files.get(i);
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() >= maxShow) {
                            break;
                        }
                    }
                }
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(File path, int number) {
        try {
            imageFileList.clear();
            if (path != null && path.isDirectory() && path.exists() && number > 0) {
                File[] pfiles = path.listFiles();
                if (pfiles != null) {
                    for (File file : pfiles) {
                        if (file.isFile() && FileTools.isSupportedImage(file)) {
                            imageFileList.add(file);
                            if (imageFileList.size() == number || imageFileList.size() >= maxShow) {
                                break;
                            }
                        }
                    }
                }
                if (!imageFileList.isEmpty()) {
                    filesNumber = imageFileList.size();
                    colsNum = (int) Math.sqrt(filesNumber);
                    colsNum = Math.max(colsNum, filesNumber / colsNum);
                }
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages() {
        try {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }
            path = null;
            filesNumber = 0;
            totalLabel.setText("");
            getMyStage().setTitle(getBaseTitle());
            if (imageFileList == null || imageFileList.isEmpty() || colsNum <= 0) {
                return;
            }
            isSettingValues = true;
            path = imageFileList.get(0).getParentFile();
            filesBox.getItems().clear();
            int total = 0;
            File[] pfiles = path.listFiles();
            if (pfiles != null) {
                for (File file : pfiles) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        total++;
                    }
                }
            }
            List<Integer> fvalues = Arrays.asList(9, 16, 12, 15, 25, 4, 3, 8, 6, 10,
                    36, 30, 24, 48);
            for (int fn : fvalues) {
                if (fn <= total) {
                    filesBox.getItems().add(fn + "");
                }
            }
            if (!filesBox.getItems().contains(total + "")) {
                if (filesBox.getItems().size() > 6) {
                    filesBox.getItems().add(6, total + "");
                } else {
                    filesBox.getItems().add(total + "");
                }
            }
            filesNumber = imageFileList.size();
            if (!filesBox.getItems().contains(filesNumber + "")) {
                filesBox.getItems().add(0, filesNumber + "");
            }
            filesBox.getSelectionModel().select(filesNumber + "");
            if (!colsnumBox.getItems().contains(colsNum + "")) {
                colsnumBox.getItems().add(0, colsNum + "");
            }
            colsnumBox.getSelectionModel().select(colsNum + "");
            isSettingValues = false;

            getMyStage().setTitle(getBaseTitle() + " " + path.getAbsolutePath());
            totalLabel.setText("/" + total);
            displayMode = DisplayMode.ImagesGrid;

            makeImagesNevigator(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected ImageInformation loadInfo(File file) {
        if (displayMode == ImagesBrowserController_Load.DisplayMode.FilesList) {
            ImageFileInformation finfo = ImageFileInformation.create(file);
            if (finfo == null) {
                return null;
            }
            return finfo.getImageInformation();
        } else {
            return ImageFileReaders.makeInfo(file, thumbWidth);
        }
    }

    protected abstract void makeImagesNevigator(boolean makeCurrentList);

    public void viewImage(File file) {
        imageController.loadImageFile(file);
        updateStageTitle(file);
    }

}
