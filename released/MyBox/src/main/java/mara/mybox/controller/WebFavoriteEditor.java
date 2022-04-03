package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.IconTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class WebFavoriteEditor extends TreeNodeEditor {

    @FXML
    protected ControlFileSelecter iconController;
    @FXML
    protected ImageView iconView;

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

//    @Override
//    public TreeNode pickNodeData() {
//        TreeNode node = super.pickNodeData();
//        if (node == null) {
//            return null;
//        }
//        String address = node.getValue();
//        if (address == null || address.isBlank()) {
//            popError(message("InvalidParameters") + ": " + message("Address"));
//            return null;
//        }
//        try {
//            URL url = new URL(address);
//        } catch (Exception e) {
//            popError(message("InvalidParemeters") + ": " + message("Address"));
//            return null;
//        }
//        return node;
//    }
    @FXML
    @Override
    public void goAction() {
        WebBrowserController.oneOpen(valueInput.getText(), true);
    }

}
