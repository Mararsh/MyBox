package mara.mybox.controller;

import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mara.mybox.data.CertificateBypass;
import mara.mybox.db.TableBrowserBypassSSL;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class SecurityCertificatesBypassController extends TableManageController<CertificateBypass> {

    @FXML
    protected TableColumn<CertificateBypass, String> hostColumn;
    @FXML
    protected TableColumn<CertificateBypass, Long> timeColumn;

    public SecurityCertificatesBypassController() {
        baseTitle = AppVariables.message("SSLVerificationBypassList");
    }

    @Override
    protected void initColumns() {
        try {

            hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            timeColumn.setCellFactory(new TableDateCell());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void load() {
        tableData.clear();
        tableData.addAll(TableBrowserBypassSSL.read());
        checkSelected();
    }

    @Override
    protected boolean deleteSelectedData() {
        List<CertificateBypass> selected = tableView.getSelectionModel().getSelectedItems();
        return TableBrowserBypassSSL.delete(selected);
    }

    @Override
    protected boolean clearData() {
        return new TableBrowserBypassSSL().clear();
    }

    @FXML
    @Override
    public void addAction() {
        try {
            TextInputDialog dialog = new TextInputDialog("docs.oracle.com");
            dialog.setTitle(message("SSLVerificationByPass"));
            dialog.setHeaderText(message("InputAddress"));
            dialog.setContentText("");
            dialog.getEditor().setPrefWidth(500);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return;
            }
            String address = result.get().trim();
            if (address.isBlank()) {
                return;
            }
            for (CertificateBypass p : tableData) {
                if (p.getHost().equals(address)) {
                    return;
                }
            }
            if (TableBrowserBypassSSL.write(address)) {
                CertificateBypass newdata = TableBrowserBypassSSL.read(address);
                if (newdata != null) {
                    tableData.add(newdata);
                    tableView.refresh();
                    popSuccessful();
                } else {
                    popFailed();
                }
            } else {
                popFailed();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
