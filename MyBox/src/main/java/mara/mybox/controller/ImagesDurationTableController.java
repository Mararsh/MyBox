package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.LongStringConverter;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2020-1-14
 * @License Apache License Version 2.0
 */
public class ImagesDurationTableController extends ImagesTableController {

    protected long duration;

    @FXML
    protected TableColumn<ImageInformation, Long> durationColumn;
    @FXML
    protected ComboBox<String> durationSelector;

    public ImagesDurationTableController() {
    }

    @Override
    public void initTable() {
        super.initTable();

        duration = -1;
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

    @Override
    public void initColumns() {
        try {
            super.initColumns();

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

        } catch (Exception e) {
            logger.error(e.toString());
        }
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
            logger.error(e.toString());
        }

    }

    @Override
    public void updateLabel() {
        long d = 0, pixels = 0;
        for (ImageInformation m : tableData) {
            pixels += m.getWidth() * m.getHeight();
            if (m.getDuration() > 0) {
                d += m.getDuration();
            }
        }
        String s = message("TotalDuration") + ": " + DateTools.timeMsDuration(d) + "  "
                + message("TotalPixels") + ": " + StringTools.format(pixels) + "  ";
        s += MessageFormat.format(message("TotalFilesNumberSize"),
                totalFilesNumber, FileTools.showFileSize(totalFilesSize));
        if (viewFileButton != null) {
            s += "  " + message("DoubleClickToView");
        }
        tableLabel.setText(s);
    }

}
