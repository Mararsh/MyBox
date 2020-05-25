package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.EpidemicReport;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-04-15
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportController extends DataImportController {

    public EpidemicReportsImportController() {
        baseTitle = AppVariables.message("ImportEpidemicReportJHUTimes");
    }

    @Override
    public long importFile(File file) {
        int count = 1;
        while (count++ < 5) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                return importFile(conn, file);
            } catch (Exception e) {
                logger.debug(count + "  " + e.toString());
                try {
                    Thread.sleep(500 * count);
                } catch (Exception ex) {
                }
            }
        }
        return -1;
    }

    protected long importFile(Connection conn, File file) {
        return -1;
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
