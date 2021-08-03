package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import mara.mybox.db.data.WebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.IconTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class ControlWebBrowserBox extends BaseWebViewController {

    protected Tab tab;
    protected List<String> failedAddress;

    public ControlWebBrowserBox() {
        baseTitle = Languages.message("WebBrowser");
    }

    public void initTab(BaseController parent, Tab tab) {
        setParameters(parent);
        this.tab = tab;
    }

    @Override
    protected void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            if (tab != null) {
                String title = webEngine.getTitle();
                tab.setText(title != null ? title.substring(0, Math.min(10, title.length())) : "");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
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
                    if (tab != null) {
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
            if (tab != null) {
                if (tabImage == null) {
                    tabImage = new ImageView("img/MyBox.png");
                }
                tabImage.setFitWidth(20);
                tabImage.setFitHeight(20);
                tab.setGraphic(tabImage);
            }
            return his;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
