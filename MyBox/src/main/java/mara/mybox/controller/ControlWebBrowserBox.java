package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javax.imageio.ImageIO;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class ControlWebBrowserBox extends BaseWebViewController {

    protected TableWebHistory tableWebHistory;
    protected Tab tab;
    protected String status;
    protected boolean fetchIcon;

    @FXML
    protected ComboBox<String> urlBox;
    @FXML
    protected HBox addressBox;

    public ControlWebBrowserBox() {
        baseTitle = AppVariables.message("WebBrowser");
        fetchIcon = false;
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initURLBox();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void initURLBox() {
        try {
            tableWebHistory = new TableWebHistory();
            List<String> urls = tableWebHistory.recent(20);
            if (!urls.isEmpty()) {
                isSettingValues = true;
                urlBox.getItems().addAll(urls);
                isSettingValues = false;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void initTab(BaseController parent, Tab tab) {
        setParameters(parent);
        this.tab = tab;
        fetchIcon = true;
    }

    @Override
    public void sourceFileChanged(final File file) {
        loadFile(file);
    }

    @Override
    public void setAddress(String value) {
        super.setAddress(value);
        urlBox.getEditor().setText(address);
    }

    public void setFile(File file) {
        if (file == null) {
            return;
        }
        sourceFile = file;
        setAddress(HtmlTools.decodeURL(file, Charset.defaultCharset()));
    }

    @FXML
    @Override
    public void goAction() {
        try {
            String value = urlBox.getEditor().getText();
            if (parentController != null && parentController instanceof BaseHtmlController) {
                ((BaseHtmlController) parentController).updateTitle(false);
            }
            if (value == null || value.isBlank()) {
                popError(message("InvalidData"));
                return;
            }
            try {
                URL url = new URL(value);
                urlBox.getEditor().setStyle(null);
            } catch (Exception e) {
                urlBox.getEditor().setStyle(badStyle);
                return;
            }
            setAddress(value);
            webEngine.getLoadWorker().cancel();
            bottomLabel.setText(AppVariables.message("Loading..."));
            webEngine.load(address);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            String title = webEngine.getTitle();
            if (tab != null) {
                tab.setText(title != null ? title.substring(0, Math.min(10, title.length())) : "");
            }
            if (address == null) {
                return;
            }
            isSettingValues = true;
            urlBox.getItems().clear();
            List<String> urls = tableWebHistory.recent(20);
            if (!urls.isEmpty()) {
                urlBox.getItems().addAll(urls);
                urlBox.getEditor().setText(address);
            }
            isSettingValues = false;

            WebHistory his = new WebHistory();
            his.setAddress(address);
            his.setVisitTime(new Date());
            his.setTitle(title != null ? title : "");
            if (fetchIcon) {
                ImageView tabImage = null;
                File iconFile = HtmlTools.readIcon(address, true);
                if (iconFile != null && iconFile.exists()) {
                    his.setIcon(iconFile.getAbsolutePath());
                    if (tab != null) {
                        BufferedImage image = ImageIO.read(iconFile);
                        if (image != null) {
                            tabImage = new ImageView(SwingFXUtils.toFXImage(image, null));
                        }
                    }
                } else {
                    his.setIcon("");
                }
                if (tab != null) {
                    if (tabImage == null) {
                        tabImage = new ImageView("img/MyBox.png");
                    }
                    tabImage.setFitWidth(20);
                    tabImage.setFitHeight(20);
                    tab.setGraphic(tabImage);
                }
            }
            tableWebHistory.insertData(his);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
