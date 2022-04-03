package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileReaders;
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
    protected TextField addressInput;

    public WebAddressController() {
        baseTitle = message("WebBrowser");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            if (addressInput != null) {
                tableWebHistory = new TableWebHistory();
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
        if (!checkBeforeNextAction() || addressInput == null) {
            return;
        }
        boolean ret = webViewController.loadAddress(addressInput.getText());
        if (ret) {
            sourceFile = webViewController.sourceFile;
        }
    }

    @Override
    public void addressChanged() {
        if (addressInput != null) {
            Platform.runLater(() -> {
                addressInput.setStyle(null);
                String address;
                if (webViewController != null) {
                    address = webViewController.address;
                    addressInput.setText(address);
                } else {
                    address = addressInput.getText();
                }
                writeHis(address);
            });
        }
    }

    public void writeHis(String address) {
        if (address == null || address.isBlank()) {
            return;
        }
        SingletonTask bgTask = new SingletonTask<Void>(this) {

            private ImageView tabImage = null;

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    TableStringValues.add(conn, "WebAddressHistories", address);

                    WebHistory his = new WebHistory();
                    his.setAddress(address);
                    his.setVisitTime(new Date());
                    String title = webEngine.getTitle();
                    his.setTitle(title != null ? title : "");
                    his.setIcon("");
                    if (failedAddress == null || !failedAddress.contains(address)) {
                        try {
                            File iconFile = IconTools.readIcon(address, true);
                            if (iconFile != null && iconFile.exists()) {
                                his.setIcon(iconFile.getAbsolutePath());
                                if (addressTab != null) {
                                    BufferedImage image = ImageFileReaders.readImage(iconFile);
                                    if (image != null) {
                                        tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    if (tabImage == null) {
                        if (failedAddress == null) {
                            failedAddress = new ArrayList<>();
                        }
                        failedAddress.add(address);
                    }
                    tableWebHistory.insertData(conn, his);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (addressTab != null) {
                    if (tabImage == null) {
                        tabImage = StyleTools.getIconImage("iconMyBox.png");
                    }
                    tabImage.setFitWidth(20);
                    tabImage.setFitHeight(20);
                    addressTab.setGraphic(tabImage);
                }
            }

        };
        start(bgTask, false);
    }

    @Override
    public void addressInvalid() {
        super.addressInvalid();
        if (addressInput != null) {
            Platform.runLater(() -> {
                addressInput.setStyle(UserConfig.badStyle());
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

    @FXML
    protected void popAddressHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, addressInput, mouseEvent, "WebAddressHistories", true);
    }

    @Override
    public boolean keyEnter() {
        if (addressInput != null && addressInput.isFocused()) {
            goAction();
            return true;
        }
        return false;
    }

}
