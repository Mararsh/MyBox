package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
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
import mara.mybox.tools.IconTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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

            if (urlSelector != null) {
                List<String> his = tableWebHistory.recent(20);
                if (!his.isEmpty()) {
                    urlSelector.getItems().addAll(his);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTab(WebBrowserController parent, Tab tab) {
        this.baseName = parent.baseName;
        this.addressTab = tab;
    }

    @Override
    public void goAction() {
        if (!checkBeforeNextAction() || urlSelector == null) {
            return;
        }
        boolean ret = webViewController.loadAddress(urlSelector.getEditor().getText());
        if (ret) {
            sourceFile = webViewController.sourceFile;
        }
    }

    @Override
    public void addressChanged() {
        if (urlSelector != null) {
            Platform.runLater(() -> {
                urlSelector.getEditor().setStyle(null);
                String address;
                if (webViewController != null) {
                    address = webViewController.address;
                } else {
                    address = urlSelector.getValue();
                }
                tableWebHistory.insertData(makeHis(address));
                urlSelector.getItems().clear();
                List<String> urls = tableWebHistory.recent(20);
                if (!urls.isEmpty()) {
                    urlSelector.getItems().addAll(urls);
                }
                urlSelector.getEditor().setText(address);
            });
        }
    }

    @Override
    public void addressInvalid() {
        super.addressInvalid();
        if (urlSelector != null) {
            Platform.runLater(() -> {
                urlSelector.getEditor().setStyle(UserConfig.badStyle());
            });
        }
    }

    @Override
    public void pageLoaded() {
        try {
            if (addressTab != null) {
                String title = webEngine.getTitle();
                addressTab.setText(title != null ? title.substring(0, Math.min(10, title.length())) : "");
            }
            super.pageLoaded();
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
        if (urlSelector != null && urlSelector.isFocused()) {
            goAction();
            return true;
        }
        return false;
    }

}
