package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.converter.LongStringConverter;
import mara.mybox.data.FileInformation;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
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
            logger.error(e.toString());
        }
    }

    protected void checkDuration() {
        try {
            long v = Long.parseLong(durationInput.getText());
            if (v <= 0) {
                durationInput.setStyle(badStyle);
            } else {
                durationInput.setStyle(null);
                duration = v;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
            durationColumn.setCellFactory(new Callback<TableColumn<FileInformation, Long>, TableCell<FileInformation, Long>>() {
                @Override
                public TableCell<FileInformation, Long> call(TableColumn<FileInformation, Long> param) {
                    TableAutoCommitCell<FileInformation, Long> cell = new TableAutoCommitCell();
                    cell.setConverter(new LongStringConverter());
                    return cell;
                }
            });
            durationColumn.setOnEditCommit(new EventHandler<CellEditEvent<FileInformation, Long>>() {
                @Override
                public void handle(CellEditEvent<FileInformation, Long> t) {
                    try {
                        long v = (long) (t.getNewValue());
                        if (v > 0) {
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setDuration(v);
                            if (!isSettingValues) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateLabel();
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void setDurationAllAction() {
        try {
            if (duration <= 0) {
                popError(message("InvalidData"));
                return;
            }
            isSettingValues = true;
            for (FileInformation info : tableData) {
                info.setDuration(duration);
            }
            isSettingValues = false;
            tableView.refresh();
            updateLabel();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
