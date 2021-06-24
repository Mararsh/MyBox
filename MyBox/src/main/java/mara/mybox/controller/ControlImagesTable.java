package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.util.converter.LongStringConverter;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableImageInfoCell;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @License Apache License Version 2.0
 */
public class ControlImagesTable extends BaseBatchTableController<ImageInformation> {

    protected boolean isOpenning;
    protected SimpleBooleanProperty hasSampled;
    protected Image image;
    protected long duration;

    @FXML
    protected TableColumn<ImageInformation, String> colorSpaceColumn, pixelsColumn;
    @FXML
    protected TableColumn<ImageInformation, ImageInformation> imageColumn;
    @FXML
    protected TableColumn<ImageInformation, Integer> indexColumn;
    @FXML
    protected TableColumn<ImageInformation, Long> durationColumn;
    @FXML
    protected CheckBox tableThumbCheck;
    @FXML
    protected ComboBox<String> durationSelector;

    public ControlImagesTable() {

    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            hasSampled = new SimpleBooleanProperty(false);
            duration = -1;
            if (durationSelector != null) {
                List<String> values = Arrays.asList("500", "300", "100", "200", "1000", "2000", "3000", "5000", "10000");
                durationSelector.getItems().addAll(values);
                durationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkDuration();
                    }
                });
                durationSelector.getSelectionModel().select(0);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
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
                imageColumn.setCellFactory(new TableImageInfoCell());
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

            if (durationColumn != null) {
                durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
                durationColumn.setCellFactory((TableColumn<ImageInformation, Long> param) -> {
                    TableAutoCommitCell<ImageInformation, Long> cell
                            = new TableAutoCommitCell<ImageInformation, Long>(new LongStringConverter()) {
                        @Override
                        public void commitEdit(Long val) {
                            if (val <= 0) {
                                cancelEdit();
                            } else {
                                super.commitEdit(val);
                            }
                        }
                    };
                    return cell;
                });
                durationColumn.setOnEditCommit((TableColumn.CellEditEvent<ImageInformation, Long> t) -> {
                    if (t == null) {
                        return;
                    }
                    if (t.getNewValue() > 0) {
                        ImageInformation row = t.getRowValue();
                        row.setDuration(t.getNewValue());
                        if (!isSettingValues) {
                            Platform.runLater(() -> {
                                updateLabel();
                            });
                        }
                    }
                });
                durationColumn.getStyleClass().add("editable-column");
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
        if (durationColumn != null) {
            s += message("TotalDuration") + ": " + DateTools.timeMsDuration(d) + "  ";
        }
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
            thread.setDaemon(false);
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
    public void editFileAction() {
        try {
            ImageInformation info = tableView.getSelectionModel().getSelectedItem();
            if (info == null) {
                return;
            }
            ImageManufactureController controller
                    = (ImageManufactureController) openStage(CommonValues.ImageManufactureFxml);
            controller.loadImageInfo(info);
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

    @FXML
    public void loadSystemClipboardImage() {
        Image clip = SystemTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(message("NoImageInClipboard"));
            return;
        }
        ImageInformation info = new ImageInformation(clip);
        tableData.add(info);
    }

    protected void checkDuration() {
        try {
            int v = Integer.valueOf(durationSelector.getValue());
            if (v > 0) {
                duration = v;
                FxmlControl.setEditorNormal(durationSelector);
            } else {
                FxmlControl.setEditorBadStyle(durationSelector);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(durationSelector);
        }
    }

    @FXML
    public void setDurationAction() {
        try {
            if (duration <= 0) {
                popError(message("InvalidData"));
                return;
            }
            isSettingValues = true;
            List<ImageInformation> rows = tableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = tableData;
            }
            for (ImageInformation info : rows) {
                info.setDuration(duration);
            }
            isSettingValues = false;
            tableView.refresh();
            updateLabel();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
