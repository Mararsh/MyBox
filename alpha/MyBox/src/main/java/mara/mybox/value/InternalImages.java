package mara.mybox.value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import mara.mybox.data.ImageItem;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-2-22
 * @License Apache License Version 2.0
 */
public class InternalImages {

    public static List<ImageItem> all() {
        return all(AppVariables.CurrentLangName);
    }

    public static List<ImageItem> allWithColors() {
        List<ImageItem> images = all();
        images.addAll(colors());
        return images;
    }

    public static List<ImageItem> all(String lang) {
        List<ImageItem> images = imgPath(lang);
        images.addAll(buttonsPath(lang, StyleTools.getIconPath()));
        images.addAll(buttonsPath(lang, StyleTools.ButtonsSourcePath));
        return images;
    }

    public static List<ImageItem> imgPath(String lang) {
        List<ImageItem> images = new ArrayList<>();
        try {
            for (int y = AppValues.AppYear; y >= 2018; y--) {
                for (int i = 1; i <= (y == 2018 ? 6 : 9); i++) {
                    String name = "cover" + y + "g" + i;
                    ImageItem item = new ImageItem()
                            .setName(name + ".png")
                            .setAddress("img/" + name + ".png")
                            .setComments(y == 2018 ? null : message(lang, name));
                    images.add(item);
                }
            }

            ImageItem item = new ImageItem()
                    .setName("jade.png").setAddress("img/jade.png")
                    .setComments(message(lang, "jadeImageTips"));
            images.add(item);
            item = new ImageItem()
                    .setName("exg1.png").setAddress("img/exg1.png")
                    .setComments(message(lang, "exg1ImageTips"));
            images.add(item);
            item = new ImageItem()
                    .setName("exg2.png").setAddress("img/exg2.png")
                    .setComments(message(lang, "exg2ImageTips"));
            images.add(item);
            item = new ImageItem()
                    .setName("MyBox.png").setAddress("img/MyBox.png")
                    .setComments(message(lang, "MyBoxImageTips"));
            images.add(item);

            images.add(new ImageItem().setName("Gadwalls.png").setAddress("img/Gadwalls.png"));
            images.add(new ImageItem().setName("SpermWhale.png").setAddress("img/SpermWhale.png"));
        } catch (Exception e) {
        }
        return images;
    }

    public static List<ImageItem> buttonsPath(String lang, String path) {
        List<ImageItem> images = new ArrayList<>();
        try {
            List<String> icons = FxFileTools.getResourceFiles(path);
            if (icons == null || icons.isEmpty()) {
                return null;
            }
            String name;
            for (String icon : icons) {
                if (!icon.startsWith("icon") || !icon.endsWith(".png") || icon.contains("_40")) {
                    continue;
                }
                name = icon.substring(4, icon.length() - 4);
                ImageItem item = new ImageItem()
                        .setName(name + ".png")
                        .setAddress(path + icon)
                        .setComments(message(lang, "icon" + name));
                images.add(item);
            }
        } catch (Exception e) {
        }
        return images;
    }

    public static List<ImageItem> colors() {
        List<ImageItem> colors = new ArrayList<>();
        colors.add(new ImageItem().setAddress("color:#ffccfd"));
        colors.add(new ImageItem().setAddress("color:#fd98a2"));
        colors.add(new ImageItem().setAddress("color:#dff0fe"));
        colors.add(new ImageItem().setAddress("color:#65b4fd"));
        colors.add(new ImageItem().setAddress("color:#fdba98"));
        colors.add(new ImageItem().setAddress("color:#8fbc8f"));
        colors.add(new ImageItem().setAddress("color:#9370db"));
        colors.add(new ImageItem().setAddress("color:#eee8aa"));
        return colors;
    }

    public static String exampleImageName() {
        return "img/cover" + AppValues.AppYear + "g5.png";
    }

    public static Image exampleImage() {
        return new Image(exampleImageName());
    }

    public static File exampleImageFile() {
        return FxFileTools.getInternalFile("/" + exampleImageName(), "image", "Example.png");
    }

    public static String exampleIcon() {
        return StyleTools.getIconPath() + "iconAdd.png";
    }

}
