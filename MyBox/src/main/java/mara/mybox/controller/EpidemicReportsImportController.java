package mara.mybox.controller;

import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-04-15
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportController extends BaseImportCsvController<EpidemicReport> {

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
            if (FxmlControl.askSure(getBaseTitle(), message("EpidemicReportStatistic"))) {
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
        controller.getMyStage().requestFocus();
    }

}
