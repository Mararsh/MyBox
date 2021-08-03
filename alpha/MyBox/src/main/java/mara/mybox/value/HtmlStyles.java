package mara.mybox.value;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class HtmlStyles {

    public enum HtmlStyle {
        Default, Console, Blackboard, Ago, Book
    }

    public static final String BaseStyle
            = ".center { text-align:center;  max-width:95%; }\n"
            + "table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;}\n"
            + "th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;}\n"
            + "th { font-weight:bold;  text-align:center;}\n"
            + "tr { height: 1.2em;  }\n"
            + ".boldText { font-weight:bold;  }\n";
    public static final String DefaultStyle
            = BaseStyle
            + ".valueText { color:#2e598a;  }\n";
    public static final String ConsoleStyle
            = "body { background-color:black; color:#CCFF99; }\n"
            + "table, th, td { border: #CCFF99; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:skyblue;  }\n"
            + BaseStyle;
    public static final String BlackboardStyle
            = "body { background-color:#336633; color:white; }\n"
            + "table, th, td { border: white; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:wheat;  }\n"
            + BaseStyle;
    public static final String LinkStyle
            = "body { background-color:transparent;  }\n"
            + "table { border-collapse:collapse; max-width:95%; }\n"
            + "table, th, td { border: 0px solid; }\n"
            + "td { padding:20px;  }\n"
            + BaseStyle;
    public static final String AgoStyle
            = "body { background-color:darkblue; color:white;  }\n"
            + "table, th, td { border: white; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:wheat;  }\n"
            + BaseStyle;
    public static final String BookStyle
            = "body { background-color:#F6F1EB; color:black;  }\n"
            + BaseStyle;

    public static HtmlStyles.HtmlStyle styleName(String styleName) {
        for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
            if (style.name().equals(styleName) || Languages.message(style.name()).equals(styleName)) {
                return style;
            }
        }
        return HtmlStyles.HtmlStyle.Default;
    }

    public static String styleValue(HtmlStyles.HtmlStyle style) {
        switch (style) {
            case Default:
                return HtmlStyles.DefaultStyle;
            case Console:
                return HtmlStyles.ConsoleStyle;
            case Blackboard:
                return HtmlStyles.BlackboardStyle;
            //            case Link:
            //                return LinkStyle;
            case Ago:
                return HtmlStyles.AgoStyle;
            case Book:
                return HtmlStyles.BookStyle;
        }
        return HtmlStyles.DefaultStyle;
    }

    public static String styleValue(String styleName) {
        return styleValue(styleName(styleName));
    }

}
