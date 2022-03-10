package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableImageFileCell;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.IconTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-29
 * @License Apache License Version 2.0
 */
public class WebFavoritesController extends TreeManageController {

    @FXML
    protected ControlFileSelecter iconController;
    @FXML
    protected ImageView iconView;

    public WebFavoritesController() {
        baseTitle = message("WebFavorites");
        category = "WebFavorites";
        nameMsg = message("Title");
        valueMsg = message("Address");
        moreMsg = message("Icon");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            moreInput = iconController.fileInput;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            iconController.isDirectory(false).isSource(true).mustExist(true).permitNull(true)
                    .baseName(baseName).savedName(baseName + "Icon").type(VisitHistory.FileType.Image);
            iconController.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    updateIcon(iconController.text());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            moreColumn.setCellFactory(new TableImageFileCell(20));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            hideRightPane();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void updateIcon(String icon) {
        try {
            iconView.setImage(null);
            if (icon != null) {
                File file = new File(icon);
                if (file.exists()) {
                    BufferedImage image = ImageFileReaders.readImage(file);
                    if (image != null) {
                        iconView.setImage(SwingFXUtils.toFXImage(image, null));
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @FXML
    protected void downloadIcon() {
        synchronized (this) {
            String address;
            try {
                URL url = new URL(valueInput.getText());
                address = url.toString();
            } catch (Exception e) {
                popError(message("InvalidData"));
                return;
            }
            SingletonTask updateTask = new SingletonTask<Void>(this) {
                private File iconFile;

                @Override
                protected boolean handle() {
                    iconFile = IconTools.readIcon(address, true);
                    return iconFile != null && iconFile.exists();
                }

                @Override
                protected void whenSucceeded() {
                    iconController.input(iconFile.getAbsolutePath());
                }
            };
            start(updateTask);
        }
    }

    @FXML
    @Override
    public void goAction() {
        WebBrowserController.oneOpen(valueInput.getText(), true);
    }

    /*
        static methods
     */
    public static WebFavoritesController oneOpen() {
        WebFavoritesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebFavoritesController) {
                try {
                    controller = (WebFavoritesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebFavoritesController) WindowTools.openStage(Fxmls.WebFavoritesFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static WebFavoritesController oneOpen(TreeNode node) {
        WebFavoritesController controller = oneOpen();
        if (controller != null) {
            controller.loadTree(node);
        }
        return controller;
    }

}
