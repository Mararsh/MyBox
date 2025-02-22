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
import mara.mybox.value.InternalImages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class ControlImage extends BaseController {

    protected Image image;
    protected SimpleBooleanProperty notify;
    protected String defaultAddress, currentAddress;

    @FXML
    protected ImageView imageView;
    @FXML
    protected HBox viewBox;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    public void setParameter(BaseController parent, String inDefault, String inCurrent) {
        try {
            notify = new SimpleBooleanProperty();

            baseName = parent.baseName + "Image";
            defaultAddress = inDefault != null ? inDefault
                    : StyleTools.getIconPath() + "iconAdd.png";

            currentAddress = inCurrent != null ? inCurrent
                    : UserConfig.getString(baseName + "Address", defaultAddress);
            loadImageItem(new ImageItem(currentAddress), false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setDefault(String address) {
        defaultAddress = address;
    }

    public Image getImage() {
        image = imageView.getImage();
        return image;
    }

    public File getFile() {
        try {
            return new ImageItem(currentAddress).getFile();
        } catch (Exception e) {
            return null;
        }
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

            private String inAddress;
            private Image inImage;

            @Override
            protected boolean handle() {
                try {
                    inImage = item.readImage();
                    if (inImage == null) {
                        if (defaultAddress == null) {
                            defaultAddress = StyleTools.getIconPath() + "iconAdd.png";
                        }
                        inAddress = defaultAddress;
                        inImage = new Image(currentAddress);
                    } else {
                        inAddress = item.getAddress();
                    }
                    UserConfig.setString(baseName + "Address", inAddress);
                    return inImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                currentAddress = inAddress;
                image = inImage;
                imageView.setImage(image);
                if (fireNotify) {
                    notify.set(!notify.get());
                }
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
        if (defaultAddress == null) {
            defaultAddress = InternalImages.exampleIcon();
        }
        loadImageItem(new ImageItem(defaultAddress), true);
    }

}
