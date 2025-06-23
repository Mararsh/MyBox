package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.IconTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataWebFavorite extends BaseDataValuesController {

    @FXML
    protected TextField addressInput;
    @FXML
    protected ControlFileSelecter iconController;
    @FXML
    protected ImageView iconView;

    @Override
    public void initEditor() {
        try {

            addressInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    valueChanged(true);
                }
            });

            iconController.isDirectory(false).isSource(true)
                    .mustExist(true).permitNull(true)
                    .type(VisitHistory.FileType.Image)
                    .parent(this, baseName + "Icon");
            iconController.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (isSettingValues) {
                        return;
                    }
                    updateIcon(iconController.text());
                    valueChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            isSettingValues = true;
            if (nodeEditor.currentNode != null) {
                addressInput.setText(nodeEditor.currentNode.getStringValue("address"));
                updateIcon(nodeEditor.currentNode.getStringValue("icon"));
            } else {
                addressInput.clear();
                updateIcon(null);
            }
            iconController.input(null);
            isSettingValues = false;
            valueChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {
            String address = addressInput.getText();
            String icon = iconController.fileInput.getText();
            node.setValue("address", address == null ? null : address.trim());
            node.setValue("icon", icon == null ? null : icon.trim());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void updateIcon(String icon) {
        try {
            iconView.setImage(null);
            if (icon != null) {
                File file = new File(icon);
                if (file.exists()) {
                    BufferedImage image = ImageFileReaders.readImage(null, file);
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
        String address;
        try {
            URI uri = new URI(addressInput.getText());
            address = uri.toString();
        } catch (Exception e) {
            popError(message("InvalidData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private File iconFile;

            @Override
            protected boolean handle() {
                iconFile = IconTools.readIcon(this, address, true);
                return iconFile != null && iconFile.exists();
            }

            @Override
            protected void whenSucceeded() {
                iconController.input(iconFile.getAbsolutePath());
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void goAction() {
        String address = addressInput.getText();
        if (address == null || address.isBlank()) {
            popError(message("InvalidData") + ": " + message("Address"));
            return;
        }
        WebBrowserController.openAddress(address, true);
    }

    public void load(String title, String address) {
        nodeEditor.titleInput.setText(title);
        addressInput.setText(address);
    }

    @FXML
    @Override
    public void startAction() {
        goAction();
    }

    /*
        static
     */
    public static DataTreeNodeEditorController open(BaseController parent, String title, String address) {
        try {
            DataTreeNodeEditorController controller
                    = DataTreeNodeEditorController.openTable(parent, new TableNodeWebFavorite());
            ((ControlDataWebFavorite) controller.valuesController).load(title, address);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
