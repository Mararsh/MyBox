package mara.mybox.bufferedimage;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorComponentTools {

    public static enum ColorComponent {
        Gray, RedChannel, GreenChannel, BlueChannel, Hue, Saturation, Brightness, AlphaChannel
    }

    public static Map<ColorComponent, Color> ComponentColor;

    public static Color color(ColorComponent c) {
        if (ComponentColor == null) {
            ComponentColor = new HashMap<>();
            ComponentColor.put(ColorComponent.RedChannel, Color.RED);
            ComponentColor.put(ColorComponent.GreenChannel, Color.GREEN);
            ComponentColor.put(ColorComponent.BlueChannel, Color.BLUE);
            ComponentColor.put(ColorComponent.Hue, Color.PINK);
            ComponentColor.put(ColorComponent.Brightness, Color.ORANGE);
            ComponentColor.put(ColorComponent.Saturation, Color.CYAN);
            ComponentColor.put(ColorComponent.AlphaChannel, Color.YELLOW);
            ComponentColor.put(ColorComponent.Gray, Color.GRAY);
        }
        if (c == null) {
            return null;
        }
        return ComponentColor.get(c);
    }

    public static Color color(ColorComponent component, int index) {
        switch (component) {
            case RedChannel:
                return new Color(index, 0, 0, 255);
            case GreenChannel:
                return new Color(0, index, 0, 255);
            case BlueChannel:
                return new Color(0, 0, index, 255);
            case AlphaChannel:
                Color aColor = ColorComponentTools.color(component);
                return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), index);
            case Gray:
                return new Color(index, index, index, 255);
            case Hue:
                return ColorConvertTools.hsb2rgb(index / 360.0F, 1.0F, 1.0F);
            case Saturation:
                float h1 = ColorConvertTools.getHue(ColorComponentTools.color(component));
                return ColorConvertTools.hsb2rgb(h1, index / 100.0F, 1.0F);
            case Brightness:
                float h2 = ColorConvertTools.getHue(ColorComponentTools.color(component));
                return ColorConvertTools.hsb2rgb(h2, 1.0F, index / 100.0F);
        }
        return null;
    }

    public static Color color(String name, int index) {
        return color(ColorComponentTools.component(name), index);
    }

    public static Color componentColor(String name) {
        return color(component(name));
    }

    public static ColorComponent component(String name) {
        for (ColorComponent c : ColorComponent.values()) {
            if (c.name().equals(name) || Languages.message(c.name()).equals(name)) {
                return c;
            }
        }
        return null;
    }

}
