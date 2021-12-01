package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Network extends MyBoxController_File {

    @FXML
    protected void showNetworkMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem weiboSnap = new MenuItem(Languages.message("WeiboSnap"));
        weiboSnap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WeiboSnapFxml);
        });

        MenuItem webBrowserHtml = new MenuItem(Languages.message("WebBrowser"));
        webBrowserHtml.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.WebBrowserFxml);
        });

        MenuItem WebFavorites = new MenuItem(Languages.message("WebFavorites"));
        WebFavorites.setOnAction((ActionEvent event1) -> {
            WebFavoritesController.oneOpen();
            closeStage();
        });

        MenuItem WebHistories = new MenuItem(Languages.message("WebHistories"));
        WebHistories.setOnAction((ActionEvent event1) -> {
            WebHistoriesController.oneOpen();
            closeStage();
        });

        MenuItem ConvertUrl = new MenuItem(Languages.message("ConvertUrl"));
        ConvertUrl.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NetworkConvertUrlFxml);
        });

        MenuItem QueryAddress = new MenuItem(Languages.message("QueryNetworkAddress"));
        QueryAddress.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NetworkQueryAddressFxml);
        });

        MenuItem QueryDNSBatch = new MenuItem(Languages.message("QueryDNSBatch"));
        QueryDNSBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.NetworkQueryDNSBatchFxml);
        });

        MenuItem DownloadFirstLevelLinks = new MenuItem(Languages.message("DownloadFirstLevelLinks"));
        DownloadFirstLevelLinks.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DownloadFirstLevelLinksFxml);
        });

        MenuItem SecurityCertificates = new MenuItem(Languages.message("SecurityCertificates"));
        SecurityCertificates.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SecurityCertificatesFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                webBrowserHtml, WebFavorites, WebHistories, new SeparatorMenuItem(),
                QueryAddress, QueryDNSBatch, ConvertUrl, new SeparatorMenuItem(),
                DownloadFirstLevelLinks, weiboSnap, new SeparatorMenuItem(),
                SecurityCertificates
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(networkBox, event);

        view.setImage(new Image("img/NetworkTools.png"));
        text.setText(Languages.message("NetworkToolsImageTips"));
        locateImage(networkBox, true);

    }

}
