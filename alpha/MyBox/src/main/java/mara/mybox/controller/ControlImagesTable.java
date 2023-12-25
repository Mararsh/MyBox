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
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableImageInfoCell;
import mara.mybox.fxml.converter.LongStringFromatConverter;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @License Apache License Version 2.0
 */
public class ControlImagesTable extends BaseBatchTableController<ImageInformation> {

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
        setFileType(VisitHistory.FileType.ImagesList, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (tableThumbCheck != null && imageColumn != null) {
                tableThumbCheck.setSelected(UserConfig.getBoolean(baseName + "Thumbnail", true));
                checkThumb();
                tableThumbCheck.selectedProperty().addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        checkThumb();
                        UserConfig.setBoolean(baseName + "Thumbnail", tableThumbCheck.isSelected());
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    public void checkThumb() {
        if (tableThumbCheck.isSelected()) {
            if (!tableView.getColumns().contains(imageColumn)) {
                tableView.getColumns().add(2, imageColumn);
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
                            = new TableAutoCommitCell<ImageInformation, Long>(new LongStringFromatConverter()) {

                        @Override
                        public boolean valid(Long value) {
                            return value != null && value > 0;
                        }

                        @Override
                        public boolean setCellValue(Long value) {
                            try {
                                if (!valid(value) || !isEditingRow()) {
                                    cancelEdit();
                                    return false;
                                }
                                ImageInformation row = tableData.get(editingRow);
                                if (value == row.getDuration()) {
                                    cancelEdit();
                                    return false;
                                }
                                row.setDuration(value);
                                if (!isSettingValues) {
                                    Platform.runLater(() -> {
                                        updateTableLabel();
                                    });
                                }
                                return super.setCellValue(value);
                            } catch (Exception e) {
                                MyBoxLog.debug(e);
                                return false;
                            }
                        }
                    };
                    return cell;
                });
                durationColumn.getStyleClass().add("editable-column");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void tableChanged() {
        super.tableChanged();
        hasSampled.set(hasSampled());
    }

    @Override
    protected void checkButtons() {
        super.checkButtons();
        try {
            infoButton.setDisable(true);
            metaButton.setDisable(true);
            ImageInformation info = selectedItem();
            if (info == null || info.getFile() == null) {
                return;
            }
            String suffix = FileNameTools.suffix(info.getFile().getName());
            if (suffix == null) {
                return;
            }
            boolean invalid = isNoneSelected()
                    || suffix.equalsIgnoreCase("ppt")
                    || suffix.equalsIgnoreCase("pptx")
                    || suffix.equalsIgnoreCase("pdf");
            infoButton.setDisable(invalid);
            metaButton.setDisable(invalid);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void updateTableLabel() {
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
        if (viewButton != null) {
            s += "  " + message("DoubleClickToOpen");
        }
        int selected = tableView.getSelectionModel().getSelectedIndices().size();
        if (selected > 0) {
            s += "  " + message("Selected") + ": " + selected;
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
    protected ImageInformation create(FxTask currentTask, File file) {
        ImageFileInformation finfo = ImageFileInformation.create(currentTask, file);
        return finfo != null ? finfo.getImageInformation() : null;
    }

    @Override
    public List<ImageInformation> createFiles(FxTask currentTask, List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return null;
            }
            List<ImageInformation> infos = new ArrayList<>();
            for (File file : files) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return infos;
                }
                currentTask.setInfo(file.getAbsolutePath());
                ImageFileInformation finfo = ImageFileInformation.create(currentTask, file);
                if (finfo != null && finfo.getImagesInformation() != null) {
                    infos.addAll(finfo.getImagesInformation());

                }
                recordFileAdded(file);
            }
            return infos;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void addDirectory(int index, File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        try {
            File[] list = directory.listFiles();
            if (list == null || list.length == 0) {
                popInformation(message("NoData"));
                return;
            }
            List<File> files = new ArrayList<>();
            for (File file : list) {
                if (file.exists() && file.isFile() && FileTools.isSupportedImage(file)) {
                    files.add(file);
                }
            }
            addFiles(index, files);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void viewAction() {
        try {
            ImageInformation info = selectedItem();
            if (info == null || info.getFile() == null) {
                return;
            }
            String suffix = FileNameTools.suffix(info.getFile().getName());
            if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                PdfViewController controller = (PdfViewController) openStage(Fxmls.PdfViewFxml);
                controller.loadFile(info.getFile(), null, info.getIndex());

            } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                PptViewController controller = (PptViewController) openStage(Fxmls.PptViewFxml);
                controller.loadFile(info.getFile(), info.getIndex());

            } else {
                ImageEditorController.openImageInfo(info);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void editAction() {
        try {
            ImageInformation info = selectedItem();
            if (info == null) {
                return;
            }
            ImageEditorController.openImageInfo(info);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void infoAction() {
        if (tableData.isEmpty()) {
            return;
        }
        ImageInformation info = selectedItem();
        if (info == null) {
            info = tableData.get(0);
        }
        ImageInformationController.open(info);
    }

    @FXML
    @Override
    public void metaAction() {
        if (tableData.isEmpty()) {
            return;
        }
        ImageInformation info = selectedItem();
        if (info == null) {
            info = tableData.get(0);
        }
        ImageMetaDataController.open(info);
    }

    @FXML
    @Override
    public void pasteAction() {
        Image clip = ImageClipboardTools.fetchImageInClipboard(false);
        if (clip == null) {
            popError(message("NoImageInClipboard"));
            return;
        }
        ImageInformation info = new ImageInformation(clip);
        tableData.add(info);
        popDone();
    }

    protected void checkDuration() {
        try {
            int v = Integer.parseInt(durationSelector.getValue());
            if (v > 0) {
                duration = v;
                ValidationTools.setEditorNormal(durationSelector);
            } else {
                ValidationTools.setEditorBadStyle(durationSelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(durationSelector);
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
            List<ImageInformation> rows = selectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = tableData;
            }
            for (ImageInformation info : rows) {
                info.setDuration(duration);
            }
            isSettingValues = false;
            tableView.refresh();
            updateTableLabel();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

}
