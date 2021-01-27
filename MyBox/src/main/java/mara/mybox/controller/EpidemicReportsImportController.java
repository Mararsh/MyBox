package mara.mybox.controller;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-04-15
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportController extends DataImportController<EpidemicReport> {

    public EpidemicReportsImportController() {
        baseTitle = AppVariables.message("ImportEpidemicReportJHUTimes");
    }

    @Override
    public BaseTable getTableDefinition() {
        if (tableDefinition == null) {
            tableDefinition = new TableEpidemicReport();
        }
        return tableDefinition;
    }

    @Override
    public void afterSuccessful() {
        if (statisticCheck == null) {
            return;
        }
        if (statisticCheck.isSelected()) {
            startStatistic();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("MyBox");
            alert.setContentText(message("EpidemicReportStatistic"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonOK = new ButtonType(AppVariables.message("OK"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonOK, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonOK) {
                startStatistic();
            }
        }
    }

    protected void startStatistic() {
        EpidemicReportsController savedParent = (EpidemicReportsController) parent;
        parent = null;
        EpidemicReportsStatisticController controller
                = (EpidemicReportsStatisticController) openStage(CommonValues.EpidemicReportsStatisticFxml);
        if (savedParent != null) {
            controller.parent = savedParent;
        }
        controller.start(EpidemicReport.COVID19JHU);
        controller.getMyStage().toFront();
    }

}
