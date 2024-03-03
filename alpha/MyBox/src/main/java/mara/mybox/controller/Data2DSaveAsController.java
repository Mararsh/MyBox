package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveAsController extends BaseData2DSaveAsController {

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected ControlNewDataTable dbController;
    @FXML
    protected CheckBox currentSheetOnlyCheck;
    @FXML
    protected Tab csvTab, excelTab, textTab, htmlTab, pdfTab, dbTab;
    @FXML
    protected RadioButton zeroNonnumericRadio, blankNonnumericRadio;
    @FXML
    protected VBox optionsBox, csvBox, excelBox, textBox, htmlBox, pdfBox, dbBox;

    public void setParameters(BaseData2DLoadController controller) {
        try {
            if (controller == null || controller.getData2D() == null) {
                close();
                return;
            }
            tableController = controller;
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return;
            }
            tabPane.getTabs().removeAll(csvTab, excelTab, textTab, htmlTab, pdfTab, dbTab);
            initControls(baseName);

            export = Data2DExport.create(data2D);

            targetController.setParameters(this, tableController);
            targetController.formatNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    formatChanged();
                }
            });
            format = targetController.target;

            dbController.setParameters(this, data2D);
            dbController.setColumns(data2D.columnIndices());
            dbController.nameInput.setText(targetName);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void formatChanged() {
        try {
            if (tableController == null || !tableController.isShowing()) {
                close();
                return;
            }
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return;
            }
            optionsBox.getChildren().clear();
            format = targetController.target;
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
            if (tableController == null || !tableController.isShowing()) {
                close();
                return false;
            }
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return false;
            }
            if (!targetController.validateTarget()) {
                return false;
            }
            format = targetController.target;
            if (format == TargetType.DatabaseTable) {
                return pickDB();
            }
            switch (format) {
                case CSV:
                    if (!pickCSV(export)) {
                        return false;
                    }
                    break;
                case Excel:
                    if (!pickExcel(export)) {
                        return false;
                    }
                    break;
                case Text:
                    if (!pickText(export)) {
                        return false;
                    }
                    break;
                case HTML:
                    if (!pickHtml(export)) {
                        return false;
                    }
                    break;
                case PDF:
                    if (!pickPDF(export)) {
                        return false;
                    }
                    break;
            }
            targetName = targetController.name();
            checkTargets();
            if (format != TargetType.DatabaseTable) {
                export.initParameters(format);
                export.setDataName(targetName);
                return export.initPath(targetController.targetFileController,
                        data2D.getColumns(), targetName);
            } else {
                return true;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickDB() {
        try (Connection conn = DerbyBase.getConnection()) {
            boolean ok = dbController.checkOptions(conn, false);
            if (!ok) {
                tabPane.getSelectionModel().select(dbTab);
            }
            return ok;
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
            if (format == TargetType.DatabaseTable) {
                dataTable = null;
                try (Connection conn = DerbyBase.getConnection()) {
                    if (!dbController.createTable(currentTask, conn)) {
                        return false;
                    }
                    if (data2D.isMutiplePages()) {
                        dbController.importAllData(currentTask, conn, invalidAs);
                    } else {
                        dbController.importData(conn, null, invalidAs);
                    }
                    dataTable = dbController.dataTable;
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
                return dataTable != null;
            } else {
                data2D.startTask(currentTask, null);
                export.setCols(data2D.columnIndices()).setTask(currentTask).start();
                data2D.stopTask();
                return !export.failed();
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
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
