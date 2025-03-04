package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.imageio.ImageIO;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.tools.FileNameTools;

/**
 * @Author Mara
 * @CreateDate 2020-1-5
 * @License Apache License Version 2.0
 */
public class ImageItem {

    protected String name, address, comments;
    protected int index, width;
    protected SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    public ImageItem() {
        init();
    }

    public ImageItem(String address) {
        init();
        this.address = address;
    }

    private void init() {
        name = null;
        address = null;
        comments = null;
        selected = new SimpleBooleanProperty(false);
    }

    public boolean isInternal() {
        return address != null
                && (address.startsWith("img/") || address.startsWith("buttons/"));
    }

    public int getWidth() {
        if (width > 0) {
            return width;
        }
        if (name != null && name.startsWith("icon")) {
            return 100;
        }
        return address != null && address.startsWith("buttons/") ? 100 : 500;
    }

    public boolean isColor() {
        return address != null && address.startsWith("color:");
    }

    public boolean isFile() {
        return address != null && new File(address).exists();
    }

    public Image readImage() {
        Image image = null;
        try {
            if (address == null || isColor()) {
                return null;
            }
            if (isInternal()) {
                image = new Image(address);
            } else if (isFile()) {
                File file = new File(address);
                if (file.exists()) {
                    BufferedImage bf = ImageIO.read(file);
                    image = SwingFXUtils.toFXImage(bf, null);
                }
            }
        } catch (Exception e) {
        }
        if (image != null) {
            width = (int) image.getWidth();
        }
        return image;
    }

    public Node makeNode(int size, boolean checkSize) {
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
                Image image = readImage();
                if (image == null) {
                    return null;
                }
                int w = size;
                if (checkSize && w > image.getWidth()) {
                    w = (int) image.getWidth();
                }
                ImageView view = new ImageView(image);
                view.setPreserveRatio(false);
                view.setFitWidth(w);
                view.setFitHeight(w);
                view.setUserData(index);
                return view;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public File getFile() {
        try {
            File file = null;
            if (isInternal()) {
                file = FxFileTools.getInternalFile("/" + address, "image",
                        name != null ? name : FileNameTools.name(address, "/"));
            } else if (isFile()) {
                file = new File(address);
            }
            if (file != null && file.exists()) {
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static
     */
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

    public String getName() {
        return name;
    }

    public ImageItem setName(String name) {
        this.name = name;
        return this;
    }

    public ImageItem setWidth(int width) {
        this.width = width;
        return this;
    }

}
