package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.IconTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class WebAddressController extends BaseWebViewController {

    protected TableWebHistory tableWebHistory;
    protected Tab addressTab;
    protected List<String> failedAddress;

    @FXML
    protected ComboBox<String> urlSelector;

    public WebAddressController() {
        baseTitle = message("WebBrowser");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            if (urlSelector != null) {
                tableWebHistory = new TableWebHistory();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (urlSelector == null) {
                return;
            }
            List<String> his = tableWebHistory.recent(20);
            if (!his.isEmpty()) {
                urlSelector.getItems().addAll(his);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTab(WebBrowserController parent, Tab tab) {
        webViewController.setParent(parent);
        this.baseName = parent.baseName;
        this.addressTab = tab;
    }

    @Override
    public boolean validAddress(String value) {
        try {
            URL url = new URL(value);
            if (urlSelector != null) {
                urlSelector.getEditor().setStyle(null);
            }
        } catch (Exception e) {
            popError(message("InvalidLink"));
            if (urlSelector != null) {
                urlSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            }
            return false;
        }
        return checkBeforeNextAction();
    }

    @Override
    public void addressChanged() {
        if (urlSelector != null) {
            Platform.runLater(() -> {
                tableWebHistory.insertData(makeHis(getAddress()));
                urlSelector.getItems().clear();
                List<String> urls = tableWebHistory.recent(20);
                if (!urls.isEmpty()) {
                    urlSelector.getItems().addAll(urls);
                    urlSelector.getEditor().setText(getAddress());
                }
            });
        }
    }

    @Override
    public void goAction() {
        if (urlSelector == null) {
            return;
        }
        webViewController.goAddress(urlSelector.getEditor().getText());
    }

    @FXML
    public void refreshAction() {
        webViewController.refreshAction();
    }

    @Override
    protected void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            if (addressTab != null) {
                String title = webEngine.getTitle();
                addressTab.setText(title != null ? title.substring(0, Math.min(10, title.length())) : "");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public WebHistory makeHis(String value) {
        try {
            WebHistory his = new WebHistory();
            his.setAddress(value);
            his.setVisitTime(new Date());
            String title = webEngine.getTitle();
            his.setTitle(title != null ? title : "");
            his.setIcon("");
            ImageView tabImage = null;
            if (failedAddress == null || !failedAddress.contains(value)) {
                File iconFile = IconTools.readIcon(value, true);
                if (iconFile != null && iconFile.exists()) {
                    his.setIcon(iconFile.getAbsolutePath());
                    if (addressTab != null) {
                        BufferedImage image = ImageIO.read(iconFile);
                        if (image != null) {
                            tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                        }
                    }
                } else {
                    if (failedAddress == null) {
                        failedAddress = new ArrayList<>();
                    }
                    failedAddress.add(value);
                }
            }
            if (addressTab != null) {
                if (tabImage == null) {
                    tabImage = new ImageView("img/MyBox.png");
                }
                tabImage.setFitWidth(20);
                tabImage.setFitHeight(20);
                addressTab.setGraphic(tabImage);
            }
            return his;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public boolean keyEnter() {
        if (urlSelector.isFocused()) {
            goAction();
            return true;
        }
        return false;
    }

}
