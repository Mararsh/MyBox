package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableImageCell;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlImagesTable extends BaseBatchTableController<ImageInformation> {

    protected boolean isOpenning;
    protected SimpleBooleanProperty hasSampled;
    protected Image image;

    @FXML
    protected TableColumn<ImageInformation, String> colorSpaceColumn, pixelsColumn;
    @FXML
    protected TableColumn<ImageInformation, ImageInformation> imageColumn;
    @FXML
    protected TableColumn<ImageInformation, Integer> indexColumn;
    @FXML
    protected CheckBox tableThumbCheck;

    public ControlImagesTable() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);
        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            hasSampled = new SimpleBooleanProperty(false);
        } catch (Exception e) {
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (tableThumbCheck != null && imageColumn != null) {
                tableThumbCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Thumbnail", true));
                checkThumb();
                tableThumbCheck.selectedProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        checkThumb();
                        AppVariables.setUserConfigValue(baseName + "Thumbnail", tableThumbCheck.isSelected());
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    public void checkThumb() {
        if (tableThumbCheck.isSelected()) {
            if (!tableView.getColumns().contains(imageColumn)) {
                tableView.getColumns().add(0, imageColumn);
            }
        } else {
            if (tableView.getColumns().contains(imageColumn)) {
                tableView.getColumns().remove(imageColumn);
            }
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            if (imageColumn != null) {
                imageColumn.setCellValueFactory(new PropertyValueFactory<>("self"));
                imageColumn.setCellFactory(new TableImageCell());
            }

            if (pixelsColumn != null) {
                pixelsColumn.setCellValueFactory(new PropertyValueFactory<>("pixelsString"));
            }

            if (colorSpaceColumn != null) {
                colorSpaceColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpace"));
            }

            if (indexColumn != null) {
                indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void tableChanged() {
        super.tableChanged();
        hasSampled.set(hasSampled());
    }

    @Override
    public void updateLabel() {
        if (tableLabel == null) {
            return;
        }
        long d = 0, pixels = 0;
        for (ImageInformation m : tableData) {
            pixels += m.getWidth() * m.getHeight();
            if (m.getDuration() > 0) {
                d += m.getDuration();
            }
        }
        String s = message("TotalPixels") + ": " + StringTools.format(pixels) + "  ";
        s += MessageFormat.format(message("TotalFilesNumberSize"),
                totalFilesNumber, FileTools.showFileSize(totalFilesSize));
        if (viewFileButton != null) {
            s += "  " + message("DoubleClickToView");
        }
        tableLabel.setText(s);
    }

    public boolean hasSampled() {
        for (ImageInformation info : tableData) {
            if (info.isIsSampled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ImageInformation create(File file) {
        ImageInformation d = new ImageInformation(file);
        return d;
    }

    @Override
    public void addFiles(final int index, final List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        recordFileAdded(files);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<ImageInformation> infos;
                private boolean hasSampled;

                @Override
                protected boolean handle() {
                    infos = new ArrayList<>();
                    error = null;
                    hasSampled = false;
                    for (File file : files) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        final String fileName = file.getPath();
                        ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(fileName);
                        String format = finfo.getImageFormat();
                        if ("raw".equals(format)) {
                            continue;
                        }
                        for (int i = 0; i < finfo.getNumberOfImages(); ++i) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            ImageInformation minfo = finfo.getImagesInformation().get(i);
                            if (minfo.isIsSampled()) {
                                hasSampled = true;
                            }
                            infos.add(minfo);
                        }
                    }
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    if (index < 0 || index >= tableData.size()) {
                        tableData.addAll(infos);
                    } else {
                        tableData.addAll(index, infos);
                    }
                    tableView.refresh();
                    isOpenning = false;
                    if (hasSampled) {
                        alertWarning(AppVariables.message("ImageSampled"));
                    }
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void viewFileAction() {
        try {
            ImageInformation info = tableView.getSelectionModel().getSelectedItem();
            if (info == null) {
                return;
            }
            FxmlStage.openImageViewer(info);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void infoAction() {
        if (tableData.isEmpty()) {
            return;
        }
        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            info = tableData.get(0);
        }
        FxmlStage.openImageInformation(null, info);
    }

    @FXML
    @Override
    public void metaAction() {
        if (tableData.isEmpty()) {
            return;
        }
        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            info = tableData.get(0);
        }
        FxmlStage.openImageMetaData(null, info);
    }

}
