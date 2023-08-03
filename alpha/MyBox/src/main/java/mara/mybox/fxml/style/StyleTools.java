package mara.mybox.fxml.style;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleData.StyleColor;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Colors.color;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleTools {

    public static String ButtonsSourcePath = "buttons/";

    /*
        Style Data
     */
    public static StyleData getStyleData(Node node) {
        if (node == null || node.getId() == null) {
            return null;
        }
        String id = node.getId();
        StyleData style;
        if (id.startsWith("color")) {
            style = StylePrefix.color(node, id);

        } else if (id.startsWith("his")) {
            style = StylePrefix.his(node, id);

        } else if (id.startsWith("settings")) {
            style = StylePrefix.settings(node, id);

        } else if (id.startsWith("scope")) {
            style = StylePrefix.scope(node, id);

        } else {
            style = getStyleData(node, id);

        }
        return style;
    }

    public static StyleData getStyleData(Node node, String id) {
        if (node == null || id == null) {
            return null;
        }
        StyleData style = null;
        if (node instanceof ImageView) {
            style = StyleImageView.imageViewStyle(id);

        } else if (node instanceof RadioButton) {
            style = StyleRadioButton.radioButtonStyle(id);

        } else if (node instanceof CheckBox) {
            style = StyleCheckBox.checkBoxStyle(id);

        } else if (node instanceof ToggleButton) {
            style = StyleToggleButton.toggleButtonStyle(id);

        } else if (node instanceof Button) {
            style = StyleButton.buttonStyle(id);
        }
        return style;
    }

    public static void setStyle(Node node) {
        if (node == null) {
            return;
        }
        setStyle(node, node.getId());
    }

    public static void setStyle(Node node, String id) {
        setStyle(node, id, false);
    }

    public static void setStyle(Node node, String id, boolean mustStyle) {
        if (node == null) {
            return;
        }
        StyleData style = getStyleData(node);
        setTips(node, style);
        setIcon(node, StyleTools.getIconImageView(style));
        //        if (mustStyle || AppVariables.ControlColor != ColorStyle.Default) {
        //            setStyleColor(node, style, AppVariables.ControlColor);
        //        }
        setTextStyle(node, style, AppVariables.ControlColor);
    }

    public static void setTextStyle(Node node, StyleData StyleData, StyleColor colorStyle) {
        try {
            if (node == null || StyleData == null || !(node instanceof ButtonBase)) {
                return;
            }
            ButtonBase button = (ButtonBase) node;
            if (button.getGraphic() == null) {
                return;
            }
            if (AppVariables.controlDisplayText) {
                String name = StyleData.getName();
                if (name != null && !name.isEmpty()) {
                    button.setText(name);
                } else {
                    button.setText(StyleData.getComments());
                }
            } else {
                button.setText(null);
            }
        } catch (Exception e) {
            MyBoxLog.debug(node.getId() + " " + e.toString());
        }
    }

    /*
        Color
     */
    public static StyleColor getConfigStyleColor() {
        return getColorStyle(UserConfig.getString("AnchorColor", "red"));
    }

    public static void setConfigStyleColor(BaseController controller, String value) {
        AppVariables.ControlColor = getColorStyle(value);
        UserConfig.setString("AnchorColor", AppVariables.ControlColor.name());
        if (AppVariables.ControlColor == StyleColor.Customize) {
            SingletonTask task = new SingletonTask<Void>(controller) {

                @Override
                protected boolean handle() {
                    try {
                        List<String> iconNames = FxFileTools.getResourceFiles(StyleTools.ButtonsSourcePath + "Red/");
                        if (iconNames == null || iconNames.isEmpty()) {
                            return true;
                        }
                        String targetPath = AppVariables.MyboxDataPath + "/buttons/";
                        new File(targetPath).mkdirs();
                        for (String iconName : iconNames) {
                            String tname = targetPath + iconName;
                            if (new File(tname).exists()) {
                                continue;
                            }
                            BufferedImage image = makeIcon(StyleColor.Customize, iconName);
                            if (image != null) {
                                ImageFileWriters.writeImageFile(image, "png", tname);
                                setInfo(MessageFormat.format(message("FilesGenerated"), tname));
                            }
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    WindowTools.refreshInterfaceAll();
                }

            };
            controller.start(task);
        } else {
            WindowTools.refreshInterfaceAll();
        }
    }

    /*
        Icon
     */
    public static String getIconPath() {
        return getIconPath(AppVariables.ControlColor);
    }

    public static String getIconPath(StyleColor colorStyle) {
        try {
            if (colorStyle == null) {
                colorStyle = StyleColor.Red;
            }
            if (colorStyle == StyleColor.Customize) {
                return AppVariables.MyboxDataPath + "/buttons/";
            } else {
                return ButtonsSourcePath + colorStyle.name() + "/";
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static ImageView getIconImageView(StyleData style) {
        try {
            if (style == null || style.getIconName() == null || style.getIconName().isEmpty()) {
                return null;
            }
            return StyleTools.getIconImageView(style.getIconName());
        } catch (Exception e) {
            MyBoxLog.error(e, style.getIconName());
            return null;
        }
    }

    public static ImageView getIconImageView(String iconName) {
        if (iconName == null || iconName.isBlank()) {
            return null;
        }
        try {
            String stylePath = getIconPath();
            ImageView view = null;
            if (AppVariables.icons40px && iconName.endsWith(".png") && !iconName.endsWith("_40.png")) {
                view = getIconImageView(stylePath, iconName.substring(0, iconName.length() - 4) + "_40.png");
            }
            if (view == null) {
                view = getIconImageView(stylePath, iconName);
            }
            if (view != null) {
                view.setFitWidth(AppVariables.iconSize);
                view.setFitHeight(AppVariables.iconSize);
            }
            return view;
        } catch (Exception e) {
            return null;
        }
    }

    public static ImageView getIconImageView(String stylePath, String iconName) {
        try {
            return new ImageView(ButtonsSourcePath + iconName);
        } catch (Exception e) {
            try {
                return new ImageView(stylePath + iconName);
            } catch (Exception ex) {
                try {
                    return new ImageView(ButtonsSourcePath + "Red/" + iconName);
                } catch (Exception exx) {
                    return null;
                }
            }
        }
    }

    public static Image getIconImage(String iconName) {
        ImageView view = getIconImageView(iconName);
        return view == null ? null : view.getImage();
    }

    public static Image getSourceImage(String iconName) {
        try {
            if (iconName == null) {
                return null;
            }
            ImageView view = getIconImageView(ButtonsSourcePath + "Red/", iconName);
            return view.getImage();
        } catch (Exception e) {
            MyBoxLog.error(e, iconName);
            return null;
        }
    }

    public static BufferedImage getSourceBufferedImage(String iconName) {
        try {
            Image image = getSourceImage(iconName);
            if (image == null) {
                return null;
            }
            return SwingFXUtils.fromFXImage(image, null);
        } catch (Exception e) {
            MyBoxLog.error(e, iconName);
            return null;
        }
    }

    public static void setIcon(Node node, ImageView imageView) {
        try {
            if (node == null || imageView == null) {
                return;
            }
            if (node instanceof Labeled) {
                if (((Labeled) node).getGraphic() != null) {
                    if (node.getStyleClass().contains("big")) {
                        imageView.setFitWidth(AppVariables.iconSize * 2);
                        imageView.setFitHeight(AppVariables.iconSize * 2);
                    } else if (node.getStyleClass().contains("halfBig")) {
                        imageView.setFitWidth(AppVariables.iconSize * 1.5);
                        imageView.setFitHeight(AppVariables.iconSize * 1.5);
                    } else {
                        imageView.setFitWidth(AppVariables.iconSize);
                        imageView.setFitHeight(AppVariables.iconSize);
                    }
                    ((Labeled) node).setGraphic(imageView);
                }
            } else if (node instanceof ImageView) {
                ImageView nodev = (ImageView) node;
                nodev.setImage(imageView.getImage());
                if (node.getStyleClass().contains("big")) {
                    nodev.setFitWidth(AppVariables.iconSize * 2);
                    nodev.setFitHeight(AppVariables.iconSize * 2);
                } else {
                    nodev.setFitWidth(AppVariables.iconSize * 1.2);
                    nodev.setFitHeight(AppVariables.iconSize * 1.2);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, node.getId());
        }
    }

    public static StyleColor getColorStyle(String color) {
        if (color == null) {
            return StyleColor.Red;
        }
        for (StyleColor style : StyleColor.values()) {
            if (style.name().equalsIgnoreCase(color)) {
                return style;
            }
        }
        return StyleColor.Red;
    }

    public static void setStyleColor(Node node) {
        StyleData StyleData = getStyleData(node);
        setIcon(node, StyleTools.getIconImageView(StyleData));
    }

    public static BufferedImage makeIcon(StyleColor style, String iconName) {
        try {
            if (iconName == null) {
                return null;
            }
            if (style == StyleColor.Red) {
                return StyleTools.getSourceBufferedImage(iconName);
            }
            return makeIcon(iconName, color(style, true), color(style, false));
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static BufferedImage makeIcon(String iconName, Color darkColor, Color lightColor) {
        try {
            BufferedImage srcImage = StyleTools.getSourceBufferedImage(iconName);
            return makeIcon(srcImage, darkColor, lightColor);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static BufferedImage makeIcon(BufferedImage srcImage, Color darkColor, Color lightColor) {
        try {
            if (srcImage == null || darkColor == null || lightColor == null) {
                return null;
            }
            PixelsOperation operation = PixelsOperationFactory.replaceColorOperation(srcImage,
                    color(StyleColor.Red, true), darkColor, 20);
            operation = PixelsOperationFactory.replaceColorOperation(operation.operate(),
                    color(StyleColor.Red, false), lightColor, 20);
            return operation.operate();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static Image makeImage(String iconName,
            javafx.scene.paint.Color darkColor, javafx.scene.paint.Color lightColor) {
        try {
            BufferedImage targetImage = makeIcon(iconName,
                    FxColorTools.toAwtColor(darkColor), FxColorTools.toAwtColor(lightColor));
            if (targetImage == null) {
                return null;
            }
            return SwingFXUtils.toFXImage(targetImage, null);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    /*
        Tips
     */
    public static void setTips(Node node) {
        StyleData style = getStyleData(node);
        setTips(node, style);
    }

    public static void setTips(Node node, String tips) {
        if (tips == null || tips.isEmpty()) {
            return;
        }
        NodeStyleTools.setTooltip(node, new Tooltip(tips));
    }

    public static void setTips(Node node, StyleData style) {
        if (node == null || style == null) {
            return;
        }
        if (node instanceof Control && ((Control) node).getTooltip() != null) {
            return;
        }
        setTips(node, getTips(node, style));
    }

    public static String getTips(Node node, StyleData style) {
        if (style == null) {
            return null;
        }
        String tips = "";
        String name = style.getName();
        String comments = style.getComments();
        String shortcut = style.getShortcut();
        if (comments != null && !comments.isEmpty()) {
            tips = comments;
            if (shortcut != null && !shortcut.isEmpty()) {
                tips += "\n" + shortcut;
            }
        } else if (name != null && !name.isEmpty()) {
            tips = name;
            if (shortcut != null && !shortcut.isEmpty()) {
                tips += "\n" + shortcut;
            }
        } else if (shortcut != null && !shortcut.isEmpty()) {
            tips = shortcut;
        }
        if (node instanceof Button && ((Button) node).isDefaultButton()) {
            tips += "\nENTER";
        }
        return tips;
    }

    public static void setNameIcon(Node node, String name, String iconName) {
        setIconName(node, iconName);
        StyleData style = getStyleData(node);
        if (style != null) {
            style.setName(name);
        }
        setTips(node, style);
    }

    public static void setName(Node node, String name) {
        StyleData style = getStyleData(node);
        style.setName(name);
        setTips(node, style);
    }

    public static void setIconTooltips(Node node, String iconName, String tips) {
        setIconName(node, iconName);
        setTips(node, tips);
    }

    public static void setIconName(Node node, String iconName) {
        setIcon(node, StyleTools.getIconImageView(iconName));
    }

    /*
        contents
     */
    public static ContentDisplay getControlContent(String value) {
        if (value == null) {
            return ContentDisplay.GRAPHIC_ONLY;
        }
        switch (value.toLowerCase()) {
            case "graphic":
                return ContentDisplay.GRAPHIC_ONLY;
            case "text":
                return ContentDisplay.TEXT_ONLY;
            case "top":
                return ContentDisplay.TOP;
            case "left":
                return ContentDisplay.LEFT;
            case "right":
                return ContentDisplay.RIGHT;
            case "bottom":
                return ContentDisplay.BOTTOM;
            case "center":
                return ContentDisplay.CENTER;
            default:
                return ContentDisplay.GRAPHIC_ONLY;
        }
    }

    public static ContentDisplay getConfigControlContent() {
        return getControlContent(UserConfig.getString("ControlContent", "image"));
    }

}
