package mara.mybox.controller;

import java.text.MessageFormat;
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
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-12-9
 * @Description
 * @License Apache License Version 2.0
 */
public class FFmpegImagesTableController extends ImagesTableController {

    protected long duration;

    @FXML
    protected TableColumn<ImageInformation, Long> durationColumn;
    @FXML
    protected TextField durationInput;

    public FFmpegImagesTableController() {
    }

    @Override
    public void initTable() {
        super.initTable();

        duration = -1;
        durationInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkDuration();
            }
        });
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
            durationColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(TableColumn<ImageInformation, Long> param) {
                    TableAutoCommitCell<ImageInformation, Long> cell = new TableAutoCommitCell();
                    cell.setConverter(new LongStringConverter());
                    return cell;
                }
            });
            durationColumn.setOnEditCommit(new EventHandler<CellEditEvent<ImageInformation, Long>>() {
                @Override
                public void handle(CellEditEvent<ImageInformation, Long> t) {
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
            for (ImageInformation info : tableData) {
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
            if (m.getDuration() > 0) {
                d += m.getDuration();
            }
            pixels += m.getWidth() * m.getHeight();
        }
        String s = message("TotalDuration") + ": " + DateTools.showDuration(d) + "  "
                + message("TotalPixels") + ": " + StringTools.formatData(pixels) + "  "
                + MessageFormat.format(message("TotalFilesNumberSize"),
                        totalFilesNumber, FileTools.showFileSize(totalFilesSize));
        if (viewFileButton != null) {
            s += "  " + message("DoubleClickToView");
        }
        tableLabel.setText(s);
    }

}
