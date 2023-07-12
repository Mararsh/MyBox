package mara.mybox.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.converter.LongStringFromatConverter;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */

/*
    T must be subClass of FileInformation
 */
public class FFmpegImageFilesTableController extends ControlFilesTable {

    protected long duration;

    @FXML
    protected TableColumn<FileInformation, Long> durationColumn;
    @FXML
    protected TextField durationInput;

    public FFmpegImageFilesTableController() {
    }

    @Override
    public void initTable() {
        try {
            super.initTable();

            duration = -1;
            durationInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkDuration();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkDuration() {
        try {
            long v = Long.parseLong(durationInput.getText());
            if (v <= 0) {
                durationInput.setStyle(UserConfig.badStyle());
            } else {
                durationInput.setStyle(null);
                duration = v;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
            durationColumn.setCellFactory((TableColumn<FileInformation, Long> param) -> {
                TableAutoCommitCell<FileInformation, Long> cell
                        = new TableAutoCommitCell<FileInformation, Long>(new LongStringFromatConverter()) {

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
                            FileInformation row = tableData.get(editingRow);
                            if (value == row.getDuration()) {
                                cancelEdit();
                                return false;
                            }
                            row.setDuration(value);
                            if (!isSettingValues) {
                                Platform.runLater(() -> {
                                    updateLabel();
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void setDurationAction() {
        try {
            if (duration <= 0) {
                popError(Languages.message("InvalidData"));
                return;
            }
            isSettingValues = true;
            List<FileInformation> rows = selectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = tableData;
            }
            for (FileInformation info : rows) {
                info.setDuration(duration);
            }
            isSettingValues = false;
            tableView.refresh();
            updateLabel();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

}
