package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataTreeController;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class NetworkToolsMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem weiboSnap = new MenuItem(message("WeiboSnap"));
        weiboSnap.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.WeiboSnapFxml);
        });

        MenuItem webBrowserHtml = new MenuItem(message("WebBrowser"));
        webBrowserHtml.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.WebBrowserFxml);
        });

        MenuItem WebFavorites = new MenuItem(message("WebFavorites"));
        WebFavorites.setOnAction((ActionEvent event) -> {
            DataTreeController.webFavorite(controller, true);
        });

        MenuItem WebHistories = new MenuItem(message("WebHistories"));
        WebHistories.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.WebHistoriesFxml);
        });

        MenuItem ConvertUrl = new MenuItem(message("ConvertUrl"));
        ConvertUrl.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.NetworkConvertUrlFxml);
        });

        MenuItem QueryAddress = new MenuItem(message("QueryNetworkAddress"));
        QueryAddress.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.NetworkQueryAddressFxml);
        });

        MenuItem QueryDNSBatch = new MenuItem(message("QueryDNSBatch"));
        QueryDNSBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.NetworkQueryDNSBatchFxml);
        });

        MenuItem DownloadFirstLevelLinks = new MenuItem(message("DownloadHtmls"));
        DownloadFirstLevelLinks.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DownloadFirstLevelLinksFxml);
        });

        MenuItem RemotePathManage = new MenuItem(message("RemotePathManage"));
        RemotePathManage.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.RemotePathManageFxml);
        });

        MenuItem RemotePathSynchronizeFromLocal = new MenuItem(message("RemotePathSynchronizeFromLocal"));
        RemotePathSynchronizeFromLocal.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.RemotePathSynchronizeFromLocalFxml);
        });

        MenuItem SecurityCertificates = new MenuItem(message("SecurityCertificates"));
        SecurityCertificates.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SecurityCertificatesFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(webBrowserHtml, WebFavorites, WebHistories, new SeparatorMenuItem(),
                RemotePathManage, RemotePathSynchronizeFromLocal, new SeparatorMenuItem(),
                QueryAddress, QueryDNSBatch, ConvertUrl, SecurityCertificates, new SeparatorMenuItem(),
                DownloadFirstLevelLinks, weiboSnap));

        return items;

    }

}
