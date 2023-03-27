package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Network extends MyBoxController_File {

    @FXML
    protected void showNetworkMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem weiboSnap = new MenuItem(message("WeiboSnap"));
        weiboSnap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WeiboSnapFxml);
        });

        MenuItem webBrowserHtml = new MenuItem(message("WebBrowser"));
        webBrowserHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WebBrowserFxml);
        });

        MenuItem WebFavorites = new MenuItem(message("WebFavorites"));
        WebFavorites.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WebFavoritesFxml);
        });

        MenuItem WebHistories = new MenuItem(message("WebHistories"));
        WebHistories.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WebHistoriesFxml);
        });

        MenuItem ConvertUrl = new MenuItem(message("ConvertUrl"));
        ConvertUrl.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NetworkConvertUrlFxml);
        });

        MenuItem QueryAddress = new MenuItem(message("QueryNetworkAddress"));
        QueryAddress.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NetworkQueryAddressFxml);
        });

        MenuItem QueryDNSBatch = new MenuItem(message("QueryDNSBatch"));
        QueryDNSBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NetworkQueryDNSBatchFxml);
        });

        MenuItem DownloadFirstLevelLinks = new MenuItem(message("DownloadHtmls"));
        DownloadFirstLevelLinks.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DownloadFirstLevelLinksFxml);
        });

        MenuItem RemotePathManage = new MenuItem(message("RemotePathManage"));
        RemotePathManage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.RemotePathManageFxml);
        });

        MenuItem RemotePathSynchronizeFromLocal = new MenuItem(message("RemotePathSynchronizeFromLocal"));
        RemotePathSynchronizeFromLocal.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.RemotePathSynchronizeFromLocalFxml);
        });

        MenuItem SecurityCertificates = new MenuItem(message("SecurityCertificates"));
        SecurityCertificates.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SecurityCertificatesFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                webBrowserHtml, WebFavorites, WebHistories, new SeparatorMenuItem(),
                RemotePathManage, RemotePathSynchronizeFromLocal, new SeparatorMenuItem(),
                QueryAddress, QueryDNSBatch, ConvertUrl, SecurityCertificates, new SeparatorMenuItem(),
                DownloadFirstLevelLinks, weiboSnap
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(networkBox, event);

    }

}
