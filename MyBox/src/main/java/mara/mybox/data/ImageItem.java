package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.imageio.ImageIO;

/**
 * @Author Mara
 * @CreateDate 2020-1-5
 * @License Apache License Version 2.0
 */
public class ImageItem {

    public static Map<String, String> preDefined;

    protected String address, comments;
    protected int index;
    protected SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    protected Image image;

    public ImageItem(String address, String comments) {
        this.address = address;
        this.comments = comments;
        readImage();
    }

    public boolean isInternel() {
        return address != null && address.startsWith("img/");
    }

    public boolean isColor() {
        return address != null && address.startsWith("color:");
    }

    public boolean isFile() {
        return address != null && new File(address).exists();
    }

    public boolean isPredefined() {
        return address != null && predefined().containsKey(address);
    }

    public final void readImage() {
        try {
            image = null;
            if (isInternel()) {
                image = new ImageView(new Image(address)).getImage();
            } else if (isFile()) {
                File file = new File(address);
                if (file.exists()) {
                    BufferedImage bf = ImageIO.read(file);
                    image = SwingFXUtils.toFXImage(bf, null);
                }
            }
        } catch (Exception e) {
        }
    }

    public Node makeNode(int size) {
        try {
            if (isColor()) {
                Rectangle rect = new Rectangle();
                rect.setFill(Color.web(address.substring(6)));
                rect.setWidth(size);
                rect.setHeight(size);
                rect.setStyle("-fx-padding: 10 10 10 10;-fx-background-radius: 10;");
                rect.setUserData(index);
                return rect;
            } else {
                if (image == null) {
                    readImage();
                }
                ImageView view = new ImageView(image);
                view.setPreserveRatio(false);
                view.setFitWidth(size);
                view.setFitHeight(size);
                view.setUserData(index);
                return view;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> predefined() {
        if (preDefined == null) {
            preDefined = new LinkedHashMap();
            preDefined.put("img/About.png", "AboutImageTips");
            preDefined.put("img/DataTools.png", "DataToolsImageTips");
            preDefined.put("img/Settings.png", "SettingsImageTips");
            preDefined.put("img/RecentAccess.png", "RecentAccessImageTips");
            preDefined.put("img/FileTools.png", "FileToolsImageTips");
            preDefined.put("img/ImageTools.png", "ImageToolsImageTips");
            preDefined.put("img/DocumentTools.png", "DocumentToolsImageTips");
            preDefined.put("img/MediaTools.png", "MediaToolsImageTips");
            preDefined.put("img/NetworkTools.png", "NetworkToolsImageTips");
            preDefined.put("img/sn1.png", "sn1ImageTips");
            preDefined.put("img/sn2.png", "sn2ImageTips");
            preDefined.put("img/sn3.png", "sn3ImageTips");
            preDefined.put("img/sn4.png", "sn4ImageTips");
            preDefined.put("img/sn5.png", "sn5ImageTips");
            preDefined.put("img/sn6.png", "sn6ImageTips");
            preDefined.put("img/sn7.png", "sn7ImageTips");
            preDefined.put("img/sn8.png", "sn8ImageTips");
            preDefined.put("img/sn9.png", "sn9ImageTips");
            preDefined.put("img/ww1.png", "ww1ImageTips");
            preDefined.put("img/ww2.png", "ww2ImageTips");
            preDefined.put("img/ww3.png", "ww3ImageTips");
            preDefined.put("img/ww4.png", "ww4ImageTips");
            preDefined.put("img/ww5.png", "ww5ImageTips");
            preDefined.put("img/ww6.png", "ww6ImageTips");
            preDefined.put("img/ww7.png", "ww7ImageTips");
            preDefined.put("img/ww8.png", "ww8ImageTips");
            preDefined.put("img/ww9.png", "ww9ImageTips");
            preDefined.put("img/jade.png", "jadeImageTips");
            preDefined.put("img/zz1.png", "zz1ImageTips");
            preDefined.put("img/MyBox.png", "");
            preDefined.put("color:#ffccfd", "");
            preDefined.put("color:#fd98a2", "");
            preDefined.put("color:#dff0fe", "");
            preDefined.put("color:#65b4fd", "");
            preDefined.put("color:#fdba98", "");
            preDefined.put("color:#8fbc8f", "");
            preDefined.put("color:#9370db", "");
            preDefined.put("color:#eee8aa", "");
        }
        return preDefined;
    }

    /*
        get/set
     */
    public String getAddress() {
        return address;
    }

    public ImageItem setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public ImageItem setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty getSelected() {
        return selected;
    }

    public ImageItem setSelected(SimpleBooleanProperty selected) {
        this.selected = selected;
        return this;
    }

    public ImageItem setSelected(boolean selected) {
        this.selected.set(selected);
        return this;
    }

//    Callback<ImageCheckboxItem, ObservableValue<Boolean>> ItemToBoolean
//            = (ImageCheckboxItem item) -> new SimpleBooleanProperty(item.selected);
//
//    public class ItemToBooleanCallBack
//            implements Callback<ImageCheckboxItem, ObservableValue<Boolean>> {
//
//        @Override
//        public ObservableValue<Boolean> call(ImageCheckboxItem item) {
//            return new SimpleBooleanProperty(item.selected);
//        }
//    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
