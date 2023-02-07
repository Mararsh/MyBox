package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.imageio.ImageIO;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-5
 * @License Apache License Version 2.0
 */
public class ImageItem {

    protected String name, address, comments;
    protected int index;
    protected SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    public ImageItem() {
        init();
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
        return address != null && address.startsWith("buttons/") ? 100 : 500;
    }

    public boolean isColor() {
        return address != null && address.startsWith("color:");
    }

    public boolean isFile() {
        return address != null && new File(address).exists();
    }

    public boolean isPredefined() {
        return address != null && predefined().contains(this);
    }

    public Image readImage() {
        Image image = null;
        try {
            if (address == null || isColor()) {
                return null;
            }
            if (isInternal()) {
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
        return image;
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
                Image image = readImage();
                if (image == null) {
                    return null;
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

    public File getFile() {
        try {
            File file = null;
            if (isInternal()) {
                file = FxFileTools.getInternalFile("/" + address, "image", name);
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
    public static List<ImageItem> predefined() {
        List<ImageItem> preDefined = new ArrayList<>();
        try {
            for (int y = AppValues.AppYear; y >= 2018; y--) {
                for (int i = 1; i <= (y == 2018 ? 6 : 9); i++) {
                    String name = "cover" + y + "g" + i;
                    ImageItem item = new ImageItem()
                            .setName(name + ".png")
                            .setAddress("img/" + name + ".png")
                            .setComments(y == 2018 ? null : message(name));
                    preDefined.add(item);
                }
            }

            ImageItem item = new ImageItem()
                    .setName("jade.png").setAddress("img/jade.png")
                    .setComments(message("jadeImageTips"));
            preDefined.add(item);
            item = new ImageItem()
                    .setName("exg1.png").setAddress("img/exg1.png")
                    .setComments(message("exg1ImageTips"));
            preDefined.add(item);
            item = new ImageItem()
                    .setName("exg2.png").setAddress("img/exg2.png")
                    .setComments(message("exg2ImageTips"));
            preDefined.add(item);

            preDefined.add(new ImageItem().setName("Gadwalls.png").setAddress("img/Gadwalls.png"));
            preDefined.add(new ImageItem().setName("SpermWhale.png").setAddress("img/SpermWhale.png"));
            preDefined.add(new ImageItem().setName("MyBox.png").setAddress("img/MyBox.png"));

            List<String> icons = new ArrayList<>();
            icons.addAll(Arrays.asList("Add", "Analyse", "Cancel", "Cat", "Clear", "Clipboard", "Copy",
                    "Data", "Default", "Delete", "Delimiter", "Demo", "DoubleLeft", "Edit", "Examples", "Export",
                    "Function", "Go", "Import", "Menu", "NewItem", "OK", "Open", "Panes", "Play", "Query",
                    "Random", "Recover", "Refresh", "Sampled", "Save", "Style", "Tips", "Undo"));
            for (String name : icons) {
                item = new ImageItem()
                        .setName("icon" + name + "_100.png")
                        .setAddress("buttons/Red/icon" + name + "_100.png")
                        .setComments(message("icon" + name));
                preDefined.add(item);
            }
            item = new ImageItem()
                    .setName("iconClaw.png").setAddress("buttons/iconClaw.png")
                    .setComments(message("iconClaw"));
            preDefined.add(item);

            preDefined.add(new ImageItem().setAddress("color:#ffccfd"));
            preDefined.add(new ImageItem().setAddress("color:#fd98a2"));
            preDefined.add(new ImageItem().setAddress("color:#dff0fe"));
            preDefined.add(new ImageItem().setAddress("color:#65b4fd"));
            preDefined.add(new ImageItem().setAddress("color:#fdba98"));
            preDefined.add(new ImageItem().setAddress("color:#8fbc8f"));
            preDefined.add(new ImageItem().setAddress("color:#9370db"));
            preDefined.add(new ImageItem().setAddress("color:#eee8aa"));

        } catch (Exception e) {
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

    public String getName() {
        return name;
    }

    public ImageItem setName(String name) {
        this.name = name;
        return this;
    }

}
