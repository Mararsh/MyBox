package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Network extends MyBoxController_File {

    @FXML
    public void popNetworkMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showNetworkMenu(event);
        }
    }

    @FXML
    protected void showNetworkMenu(Event event) {
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

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(webBrowserHtml, WebFavorites, WebHistories, new SeparatorMenuItem(),
                RemotePathManage, RemotePathSynchronizeFromLocal, new SeparatorMenuItem(),
                QueryAddress, QueryDNSBatch, ConvertUrl, SecurityCertificates, new SeparatorMenuItem(),
                DownloadFirstLevelLinks, weiboSnap));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(networkBox, items);

    }

}
