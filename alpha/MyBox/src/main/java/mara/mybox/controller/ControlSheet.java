package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet extends ControlSheet_Calculation {

    @Override
    public void initValues() {
        try {
            super.initValues();
            widthChange = 10;

            columns = new ArrayList<>();
            tableDataDefinition = new TableDataDefinition();
            tableDataColumn = new TableDataColumn();
            sheetChangedNotify = new SimpleBooleanProperty(false);
            dataChangedNotify = new SimpleBooleanProperty(false);
            noDataLabel = new Label(Languages.message("NoData"));
            noDataLabel.setStyle("-fx-text-fill: gray;");
            inputStyle = "-fx-border-radius: 10; -fx-background-radius: 0;";

            pagesNumber = 1;
            pageSize = 50;
            currentRow = currentCol = 0;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initHtmlControls();
            initTextControls();
            initCalculationControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.removeTooltip(sizeSheetButton);
            NodeStyleTools.removeTooltip(copySheetButton);
            NodeStyleTools.removeTooltip(equalSheetButton);
            NodeStyleTools.removeTooltip(deleteSheetButton);
            NodeStyleTools.setTooltip(editSheetButton, message("EditPageRows"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
            controller.setParameters(this);
            controller.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    newSheet(controller.rowsNumber, controller.colsNumber);
                    controller.closeStage();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void setDisplayData() {
        try {
            makeDefintionPane();
            validate();
            updateHtml();
            updateText();
            updateCalculation();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    public void csvAction() {
        synchronized (this) {
            SingletonTask csvTask = new SingletonTask<Void>() {
                File tmpFile;

                @Override
                protected boolean handle() {
                    pageData = pickData();
                    if (pageData == null || pageData.length < 1) {
                        error = message("NoData");
                        return false;
                    }
                    tmpFile = TmpFileTools.getTempFile(".csv");
                    tmpFile = TextFileTools.writeFile(tmpFile, TextTools.dataText(pageData, ",", columnNames(), null));
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return tmpFile != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController.open(tmpFile, true, ',');
                }

            };
            csvTask.setSelf(csvTask);
            Thread thread = new Thread(csvTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void excelAction() {
        synchronized (this) {
            SingletonTask excelTask = new SingletonTask<Void>() {
                File tmpFile;

                @Override
                protected boolean handle() {
                    pageData = pickData();
                    if (pageData == null || pageData.length < 1) {
                        error = message("NoData");
                        return false;
                    }
                    tmpFile = TmpFileTools.getTempFile(".xlsx");
                    try ( Workbook targetBook = new XSSFWorkbook();
                             FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                        Sheet targetSheet = targetBook.createSheet();
                        int index = 0;
                        for (String[] row : pageData) {
                            Row targetRow = targetSheet.createRow(index++);
                            for (int col = 0; col < row.length; col++) {
                                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                                targetCell.setCellValue(row[col]);
                            }
                        }
                        targetBook.write(fileOut);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return tmpFile != null && tmpFile.exists();
                }

                @Override
                protected void whenSucceeded() {
                    DataFileExcelController controller = (DataFileExcelController) openStage(Fxmls.DataFileExcelFxml);
//                    controller.setFile(tmpFile, false);
                }

            };
            excelTask.setSelf(excelTask);
            Thread thread = new Thread(excelTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void editPageAllRows() {
//        DataClipboardController.open(this);
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!dataChangedNotify.get()) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            if (task != null) {
                task.cancel();
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            dataChangedNotify.set(false);
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            tableDataDefinition = null;
            tableDataColumn = null;
            dataDefinition = null;
            sheetInputs = null;
            colsCheck = null;
            rowsCheck = null;
            pageData = null;
            columns = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
