package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.data2d.operate.Data2DSavePage;
import mara.mybox.data2d.tools.Data2DConvertTools;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.SystemClipboardWriter;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveAsController extends BaseDataConvertController {

    protected BaseData2DLoadController tableController;
    protected Data2DExport export;
    protected Data2D data2D;
    protected TargetType format;
    protected String targetName;
    protected ColumnDefinition.InvalidAs invalidAs = ColumnDefinition.InvalidAs.Blank;
    protected Data2DWriter writer;

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected Tab csvTab, excelTab, textTab, htmlTab, pdfTab, dbTab;
    @FXML
    protected RadioButton zeroNonnumericRadio, blankNonnumericRadio;
    @FXML
    protected VBox optionsBox, csvBox, excelBox, textBox, htmlBox, pdfBox, dbBox;
    @FXML
    protected ControlNewDataTable dbController;

    public Data2DSaveAsController() {
        baseTitle = message("SaveAs");
    }

    public boolean isInvalid() {
        return tableController == null
                || !tableController.isShowing()
                || tableController.data2D == null
                || !tableController.data2D.isValidDefinition()
                || (data2D != null && tableController.data2D.getD2did() != data2D.getD2did());
    }

    public void setParameters(BaseData2DLoadController controller) {
        try {
            tableController = controller;
            if (isInvalid()) {
                close();
                return;
            }
            data2D = tableController.data2D.cloneAll();

            tabPane.getTabs().removeAll(csvTab, excelTab, textTab, htmlTab, pdfTab, dbTab);
            initControls(baseName);

            dbController.setParameters(this, data2D);
            dbController.setColumns(data2D.columnIndices());
            dbController.nameInput.setText(data2D.getDataName());

            targetController.setParameters(this, tableController);
            targetController.formatNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    formatChanged();
                }
            });
            formatChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void formatChanged() {
        try {
            if (isInvalid()) {
                close();
                return;
            }
            optionsBox.getChildren().clear();
            format = targetController.format;
            if (format == null) {
                return;
            }
            switch (format) {
                case CSV:
                    optionsBox.getChildren().add(csvBox);
                    break;
                case Excel:
                    optionsBox.getChildren().add(excelBox);
                    currentSheetOnlyCheck.setVisible(data2D.isExcel());
                    break;
                case Text:
                    optionsBox.getChildren().add(textBox);
                    break;
                case HTML:
                    optionsBox.getChildren().add(htmlBox);
                    break;
                case PDF:
                    optionsBox.getChildren().add(pdfBox);
                    break;
                case DatabaseTable:
                    optionsBox.getChildren().add(dbBox);
                    break;
                default:
                    break;
            }
            refreshStyle(tabPane);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            writer = null;
            if (isInvalid()) {
                close();
                return false;
            }
            if (!targetController.validateTarget()) {
                return false;
            }
            targetName = targetController.name();
            if (targetName == null || targetName.isBlank()) {
                targetName = data2D.dataName();
            }
            if (targetName == null || targetName.isBlank()) {
                targetName = "Data2D";
            } else if (targetName.startsWith(TmpTable.TmpTablePrefix)
                    || targetName.startsWith(TmpTable.TmpTablePrefix.toLowerCase())) {
                targetName = targetName.substring(TmpTable.TmpTablePrefix.length());
            }
            format = targetController.format;
            if (format == TargetType.DatabaseTable) {
                writer = dbController.pickTableWriter();
            } else {
                targetFile = targetController.file();
                if (targetFile == null) {
                    targetFile = Data2DConvertTools.targetFile(targetName, format);
                }
                if (targetFile == null) {
                    popError(message("InvalidParameter") + ": " + message("TargetFile"));
                    return false;
                }
                writer = pickWriter(format);
            }
            if (writer == null) {
                return false;
            }
            writer.setColumns(data2D.getColumns())
                    .setHeaderNames(data2D.columnNames())
                    .setDataName(targetName)
                    .setTargetFile(targetFile)
                    .setRecordTargetFile(true)
                    .setRecordTargetData(true);
            if (writer instanceof SystemClipboardWriter) {
                ((SystemClipboardWriter) writer).setController(tableController);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        if (zeroNonnumericRadio.isSelected()) {
            invalidAs = ColumnDefinition.InvalidAs.Zero;
        } else {
            invalidAs = ColumnDefinition.InvalidAs.Blank;
        }
        tabPane.getSelectionModel().select(logsTab);
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            data2D.startTask(currentTask, null);
            Data2DSavePage operate = Data2DSavePage.writeTo(data2D, writer);
            if (operate == null) {
                return false;
            }
            operate.setController(this)
                    .setTask(currentTask)
                    .start();
            return !operate.isFailed();
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        writer.showResult();
    }

    @Override
    public void afterTask() {
        if (data2D != null) {
            data2D.stopTask();
        }
        if (successed) {
            tableController.popInformation(message("Done"));
            close();
        } else {
            popError(message("Failed"));
        }
    }

    /*
        static
     */
    public static Data2DSaveAsController open(BaseData2DLoadController tableController) {
        try {
            Data2DSaveAsController controller
                    = (Data2DSaveAsController) WindowTools.openStage(Fxmls.Data2DSaveAsFxml);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
