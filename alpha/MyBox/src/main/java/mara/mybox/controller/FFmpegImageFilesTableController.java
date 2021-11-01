package mara.mybox.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.LongStringConverter;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */

/*
    T must be subClass of FileInformation
 */
public class FFmpegImageFilesTableController extends FilesTableController {

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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
            durationColumn.setCellFactory((TableColumn<FileInformation, Long> param) -> {
                TableAutoCommitCell<FileInformation, Long> cell
                        = new TableAutoCommitCell<FileInformation, Long>(new LongStringConverter()) {
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
            durationColumn.setOnEditCommit(new EventHandler<CellEditEvent<FileInformation, Long>>() {
                @Override
                public void handle(CellEditEvent<FileInformation, Long> t) {
                    try {
                        FileInformation row = t.getRowValue();
                        Long v = t.getNewValue();
                        if (row == null || v == null || v <= 0) {
                            return;
                        }
                        row.setDuration(v);
                        if (!isSettingValues) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateLabel();
                                }
                            });
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            });
            durationColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            List<FileInformation> rows = tableView.getSelectionModel().getSelectedItems();
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
            MyBoxLog.error(e.toString());
        }

    }

}
