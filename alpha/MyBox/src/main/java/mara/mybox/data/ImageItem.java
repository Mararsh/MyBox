package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.imageio.ImageIO;
import mara.mybox.value.AppValues;

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

    /*
        static
     */
    public static List<Image> internalImages() {
        List<Image> list = new ArrayList<>();
        for (int y = AppValues.AppYear; y >= 2018; y--) {
            for (int i = 1; i <= (y == 2018 ? 6 : 9); i++) {
                list.add(new Image("img/cover" + y + "g" + i + ".png"));
            }
        }
        list.add(new Image("img/jade.png"));
        list.add(new Image("img/exg1.png"));
        list.add(new Image("img/exg2.png"));
        list.add(new Image("img/Gadwalls.png"));
        list.add(new Image("img/SpermWhale.png"));
        return list;
    }

    public static Map<String, String> predefined() {
        if (preDefined == null) {
            preDefined = new LinkedHashMap();
            for (int y = AppValues.AppYear; y >= 2018; y--) {
                for (int i = 1; i <= (y == 2018 ? 6 : 9); i++) {
                    String name = "cover" + y + "g" + i;
                    preDefined.put("img/" + name + ".png", name);
                }
            }
            preDefined.put("img/jade.png", "jadeImageTips");
            preDefined.put("img/exg1.png", "exg1ImageTips");
            preDefined.put("img/exg2.png", "exg2ImageTips");
            preDefined.put("img/Gadwalls.png", "");
            preDefined.put("img/SpermWhale.png", "");
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
