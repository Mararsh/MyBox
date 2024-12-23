package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Network extends MainMenuController_File {

    @FXML
    protected void openWeiboSnap(ActionEvent event) {
        openScene(Fxmls.WeiboSnapFxml);
    }

    @FXML
    protected void openWebBrowser(ActionEvent event) {
        openScene(Fxmls.WebBrowserFxml);
    }

    @FXML
    protected void webFavorites(ActionEvent event) {
        DataTreeController.webFavorite(parentController, false);
    }

    @FXML
    protected void webHistories(ActionEvent event) {
        openScene(Fxmls.WebHistoriesFxml);
    }

    @FXML
    protected void openConvertUrl(ActionEvent event) {
        openScene(Fxmls.NetworkConvertUrlFxml);
    }

    @FXML
    protected void queryNetworkAddress(ActionEvent event) {
        openScene(Fxmls.NetworkQueryAddressFxml);
    }

    @FXML
    protected void queryDNSBatch(ActionEvent event) {
        openScene(Fxmls.NetworkQueryDNSBatchFxml);
    }

    @FXML
    protected void openSecurityCertificates(ActionEvent event) {
        openScene(Fxmls.SecurityCertificatesFxml);
    }

    @FXML
    protected void downloadFirstLevelLinks(ActionEvent event) {
        openScene(Fxmls.DownloadFirstLevelLinksFxml);
    }

    @FXML
    protected void RemotePathManage(ActionEvent event) {
        openScene(Fxmls.RemotePathManageFxml);
    }

    @FXML
    protected void RemotePathSynchronizeFromLocal(ActionEvent event) {
        openScene(Fxmls.RemotePathSynchronizeFromLocalFxml);
    }

}
