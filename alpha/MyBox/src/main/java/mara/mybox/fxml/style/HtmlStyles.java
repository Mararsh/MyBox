package mara.mybox.fxml.style;

import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class HtmlStyles {

    public enum HtmlStyle {
        Table, Default, Article, Console, Blackboard, Ago, Book, Grey
    }

    public static final String TableStyle
            = " body { margin:0 auto; } \n"
            + "              table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;} \n"
            + "              th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;} \n"
            + "              th { font-weight:bold;  text-align:center;} \n"
            + "              tr { height: 1.2em;  } \n"
            + "              .center { text-align:center;  max-width:95%; } \n"
            + "              .valueBox { border-style: solid; border-width:1px; border-color:black; padding: 5px; border-radius:5px;} \n"
            + "              .boldText { font-weight:bold;  } \n";
    public static final String ArticleStyle
            = TableStyle
            + " body { width: 900px; } \n"
            + "              img { max-width: 900px; } \n";
    public static final String DefaultStyle
            = ArticleStyle
            + "              .valueText { color:#2e598a;  } \n";
    public static final String ConsoleStyle
            = ArticleStyle
            + " body { background-color:black; color:#CCFF99; }\n"
            + "              table, th, td { border: #CCFF99; }\n"
            + "              a:link {color: dodgerblue}\n"
            + "              a:visited  {color: #DDDDDD}\n"
            + "              .valueBox { border-color:#CCFF99;}\n"
            + "              .valueText { color:skyblue;  }\n";
    public static final String BlackboardStyle
            = ArticleStyle
            + " body { background-color:#336633; color:white; }\n"
            + "              table, th, td { border: white; }\n"
            + "              a:link {color: aqua}\n"
            + "              a:visited  {color: #DDDDDD}\n"
            + "              .valueBox { border-color:white; }\n"
            + "              .valueText { color:wheat;  }\n";
    public static final String AgoStyle
            = ArticleStyle
            + " body { background-color:darkblue; color:white;  }\n"
            + "              table, th, td { border: white; }\n"
            + "              a:link {color: springgreen}\n"
            + "              a:visited  {color: #DDDDDD}\n"
            + "              .valueBox {  border-color:white;}\n"
            + "              .valueText { color:yellow;  }\n";
    public static final String BookStyle
            = ArticleStyle
            + " body { background-color:#F6F1EB; color:black;  }\n";
    public static final String GreyStyle
            = ArticleStyle
            + " body { background-color:#ececec; color:black;  }\n";
    public static final String LinkStyle
            = ArticleStyle
            + " body { background-color:transparent;  }\n"
            + "              table { border-collapse:collapse; max-width:95%; }\n"
            + "              table, th, td { border: 0px solid; }\n"
            + "              td { padding:20px;  }\n";

    public static HtmlStyles.HtmlStyle styleName(String styleName) {
        for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
            if (style.name().equals(styleName) || message(style.name()).equals(styleName)) {
                return style;
            }
        }
        return HtmlStyles.HtmlStyle.Default;
    }

    public static String styleValue(HtmlStyles.HtmlStyle style) {
        switch (style) {
            case Table:
                return HtmlStyles.TableStyle;
            case Default:
                return HtmlStyles.DefaultStyle;
            case Article:
                return HtmlStyles.ArticleStyle;
            case Console:
                return HtmlStyles.ConsoleStyle;
            case Blackboard:
                return HtmlStyles.BlackboardStyle;
            case Ago:
                return HtmlStyles.AgoStyle;
            case Book:
                return HtmlStyles.BookStyle;
            case Grey:
                return HtmlStyles.GreyStyle;
        }
        return HtmlStyles.DefaultStyle;
    }

    public static String styleValue(String styleName) {
        return styleValue(styleName(styleName));
    }

}
