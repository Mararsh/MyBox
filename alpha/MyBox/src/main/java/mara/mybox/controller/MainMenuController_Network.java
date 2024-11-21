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
        loadScene(Fxmls.WeiboSnapFxml);
    }

    @FXML
    protected void openWebBrowser(ActionEvent event) {
        loadScene(Fxmls.WebBrowserFxml);
    }

    @FXML
    protected void webFavorites(ActionEvent event) {
        DataTreeController.webFavorite(parentController, false);
    }

    @FXML
    protected void webHistories(ActionEvent event) {
        loadScene(Fxmls.WebHistoriesFxml);
    }

    @FXML
    protected void openConvertUrl(ActionEvent event) {
        loadScene(Fxmls.NetworkConvertUrlFxml);
    }

    @FXML
    protected void queryNetworkAddress(ActionEvent event) {
        loadScene(Fxmls.NetworkQueryAddressFxml);
    }

    @FXML
    protected void queryDNSBatch(ActionEvent event) {
        loadScene(Fxmls.NetworkQueryDNSBatchFxml);
    }

    @FXML
    protected void openSecurityCertificates(ActionEvent event) {
        loadScene(Fxmls.SecurityCertificatesFxml);
    }

    @FXML
    protected void downloadFirstLevelLinks(ActionEvent event) {
        loadScene(Fxmls.DownloadFirstLevelLinksFxml);
    }

    @FXML
    protected void RemotePathManage(ActionEvent event) {
        loadScene(Fxmls.RemotePathManageFxml);
    }

    @FXML
    protected void RemotePathSynchronizeFromLocal(ActionEvent event) {
        loadScene(Fxmls.RemotePathSynchronizeFromLocalFxml);
    }

}
