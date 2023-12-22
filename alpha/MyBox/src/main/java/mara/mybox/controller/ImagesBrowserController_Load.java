package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-21
 * @License Apache License Version 2.0
 */
public abstract class ImagesBrowserController_Load extends BaseImageController {

    protected final ObservableList<File> imageFileList = FXCollections.observableArrayList();
    protected final ObservableList<ImageInformation> tableData = FXCollections.observableArrayList();
    protected int thumbWidth;
    protected List<VBox> imageBoxList;
    protected List<ScrollPane> imageScrollList;
    protected List<ImageView> imageViewList;
    protected List<Label> imageTitleList;

    protected TableView<ImageInformation> tableView;
    protected TableColumn<ImageInformation, ImageInformation> imageColumn;
    protected TableColumn<ImageInformation, String> dirColumn, fileColumn, formatColumn, pixelsColumn, csColumn, loadColumn;
    protected TableColumn<ImageInformation, Integer> indexColumn;
    protected TableColumn<ImageInformation, Long> fileSizeColumn, modifiedTimeColumn, createTimeColumn;
    protected TableColumn<ImageInformation, Boolean> isScaledColumn, isMutipleFramesColumn;

    protected List<String> selectedIndexes;
    protected int maxShow = 100;
    protected File path;
    protected DisplayMode displayMode;

    protected enum DisplayMode {
        ImagesGrid, FilesList, ThumbnailsList, None
    }

    @FXML
    protected TitledPane viewPane, browsePane;
    @FXML
    protected VBox imagesPane;
    @FXML
    protected FlowPane opPane, flowPane;
    @FXML
    protected ToggleGroup popGroup;
    @FXML
    protected Button zoomOutAllButton, zoomInAllButton, imageSizeAllButton, paneSizeAllButton;

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
//            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            getMyStage().setTitle(getBaseTitle());
            if (imageFileList == null || imageFileList.isEmpty()) {
                return;
            }
            path = imageFileList.get(0).getParentFile();
            getMyStage().setTitle(getBaseTitle() + " " + path.getAbsolutePath());
            displayMode = DisplayMode.ThumbnailsList;

            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected ImageInformation loadInfo(FxTask currentTask, File file) {
        if (displayMode == ImagesBrowserController_Load.DisplayMode.FilesList) {
            ImageFileInformation finfo = ImageFileInformation.create(currentTask, file);
            if (finfo == null) {
                return null;
            }
            return finfo.getImageInformation();
        } else {
            return ImageFileReaders.makeInfo(currentTask, file, thumbWidth);
        }
    }

    public void viewImage(File file) {
        loadImageFile(file);
    }

}
