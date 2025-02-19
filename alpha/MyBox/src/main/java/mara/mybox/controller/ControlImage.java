package mara.mybox.controller;

import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import mara.mybox.data.ImageItem;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class ControlImage extends BaseController {

    protected Image image;
    protected SimpleBooleanProperty notify;
    protected String defaultImage;

    @FXML
    protected ImageView imageView;
    @FXML
    protected HBox viewBox;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    public void setParameter(BaseController parent, String defaultIm) {
        try {
            notify = new SimpleBooleanProperty();

            baseName = parent.baseName + "_" + baseName;
            defaultImage = defaultIm != null ? defaultIm
                    : StyleTools.getIconPath() + "iconAdd.png";

            String address = UserConfig.getString(baseName + "Address", defaultImage);

            loadImageItem(new ImageItem(address), false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Image getImage() {
        image = imageView.getImage();
        return image;
    }

    public void loadImage(Image im, boolean fireNotify) {
        image = im;
        imageView.setImage(image);
        if (fireNotify) {
            notify.set(!notify.get());
        }
    }

    public void loadImageItem(ImageItem item, boolean fireNotify) {
        if (item == null) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private Image inImage;

            @Override
            protected boolean handle() {
                try {
                    inImage = item.readImage();
                    if (inImage == null) {
                        return false;
                    }
                    UserConfig.setString(baseName + "Address", item.getAddress());
                    return true;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(inImage, fireNotify);
            }

        };
        start(task);
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        loadImageItem(new ImageItem(file.getAbsolutePath()), true);
    }

    @FXML
    public void exampleAction() {
        ImageExampleSelectController controller = ImageExampleSelectController.open(this, false);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                loadImageItem(controller.selectedItem(), true);
                controller.close();
            }
        });
    }

    @FXML
    public void defaultAction() {
        loadImageItem(new ImageItem(defaultImage), true);
    }

}
