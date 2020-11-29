package mara.mybox.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.tools.HtmlTools;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-19
 * @License Apache License Version 2.0
 */
public class StringTable {

    protected String title, style = HtmlTools.DefaultStyle;
    protected List<List<String>> data;
    protected List<String> names;
    protected List<Integer> colorIndices;    // Color column should be RGBA
    protected static String indent = "    ";

    public StringTable() {

    }

    public StringTable(List<String> names) {
        this.names = names;
    }

    public StringTable(List<String> names, String title) {
        this.names = names;
        this.title = title;
        colorIndices = new ArrayList<>();
        data = new ArrayList<>();
    }

    public StringTable(List<String> names, String title, int colorIndex) {
        this.names = names;
        this.title = title;
        colorIndices = new ArrayList<>();
        colorIndices.add(colorIndex);
    }

    public static StringTable create() {
        return new StringTable();
    }

    public List<List<String>> add(List<String> row) {
        if (data == null) {
            data = new ArrayList<>();
        }
        if (row != null) {
            data.add(row);
        }
        return data;
    }

    public List<List<String>> remove(int index) {
        if (data == null) {
            data = new ArrayList<>();
        }
        if (index >= 0 && index < data.size()) {
            data.remove(index);
        }
        return data;
    }

    public String html() {
        return tableHtml(this);
    }

    public void editHtml() {
        HtmlTools.editHtml(tableHtml(this));
    }

    public void newLinkRow(String name, String link) {
        List<String> row = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            row.addAll(Arrays.asList(message(name), "<a href=\"" + link + "\" target=_blank>" + link + "</a>"));
        } else {
            row.addAll(Arrays.asList("", "<a href=\"" + link + "\" target=_blank>" + link + "</a>"));
        }
        add(row);
    }

    /*
        Static methods
     */
    public static String tableHtml(StringTable table) {
        if (table == null || table.getData() == null) {
            return "";
        }
        return HtmlTools.htmlStyleValue(table.getTitle(), table.style, tableDiv(table));
    }

    public static String tableDiv(StringTable table) {
        try {
            if (table == null || table.getData() == null) {
                return "";
            }
            StringBuilder s = new StringBuilder();
            String title = table.getTitle();
            if (title != null && !title.trim().isEmpty()) {
                s.append(indent).append(indent).append("<H2 class=\"center\">").append(title).append("</H2>\n");
            }
            s.append(indent).append(indent).append("<DIV align=\"center\">\n");
            s.append(indent).append(indent).append(indent).append("<TABLE >\n");
            List<String> names = table.getNames();
            List<Integer> colorIndices = table.getColorIndices();
            if (names != null) {
                s.append(indent).append(indent).append(indent).append(indent).
                        append("<TR  style=\"font-weight:bold; \">");
                for (int i = 0; i < names.size(); ++i) {
                    String name = names.get(i);
                    s.append("<TH>").append(name).append("</TH>");
                    if (colorIndices != null && colorIndices.contains(i)) {
                        s.append("<TH>").append(name).append(" - RGBA</TH>");
                        s.append("<TH>").append(name).append(" - RGB</TH>");
                    }
                }
                s.append("</TR>\n");
            }
            for (List<String> row : table.getData()) {
                s.append(indent).append(indent).append(indent).append(indent).append("<TR>");
                for (int i = 0; i < row.size(); ++i) {
                    String value = row.get(i);
                    if (colorIndices != null && colorIndices.contains(i)) {
                        String rgb = "#" + value.substring(2, 8);                  // Color column should be RGBA
                        s.append("<TD align=\"center\"><DIV style=\"width: 50px;  background-color:").
                                append(rgb).append("; \">&nbsp;&nbsp;&nbsp;</DIV></TD>");
                        s.append("<TD>").append(value).append(" </TD>");
                        s.append("<TD>").append(rgb).append(" </TD>");
                    } else {
                        s.append("<TD>").append(value).append("</TD>");
                    }
                }
                s.append("</TR>\n");
            }
            if (names != null && table.getData().size() > 15) {
                s.append(indent).append(indent).append(indent).append(indent).
                        append("<TR  style=\"font-weight:bold; \">");
                for (int i = 0; i < names.size(); ++i) {
                    String name = names.get(i);
                    s.append("<TH>").append(name).append("</TH>");
                    if (colorIndices != null && colorIndices.contains(i)) {
                        s.append("<TH>").append(name).append(" - RGBA</TH>");
                        s.append("<TH>").append(name).append(" - RGB</TH>");
                    }
                }
                s.append("</TR>\n");
            }
            s.append(indent).append(indent).append(indent).append("</TABLE>\n");
            s.append(indent).append(indent).append("</DIV>\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    /*
        get/set
     */
    public String getTitle() {
        return title;
    }

    public StringTable setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<List<String>> getData() {
        return data;
    }

    public StringTable setData(List<List<String>> data) {
        this.data = data;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public StringTable setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public String getStyle() {
        return style;
    }

    public StringTable setStyle(String style) {
        this.style = style;
        return this;
    }

    public List<Integer> getColorIndices() {
        return colorIndices;
    }

    public StringTable setColorIndices(List<Integer> colorIndices) {
        this.colorIndices = colorIndices;
        return this;
    }

    public String getIndent() {
        return indent;
    }

    public StringTable setIndent(String indent) {
        this.indent = indent;
        return this;
    }

}
