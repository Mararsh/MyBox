package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.tools.HtmlTools;
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
    protected int colorIndex = -1;  // 0-based
    protected static String indent = "    ";

    public StringTable() {

    }

    public StringTable(List<String> names, String title) {
        this.names = names;
        this.title = title;
    }

    public StringTable(List<String> names, String title, int colorIndex) {
        this.names = names;
        this.title = title;
        this.colorIndex = colorIndex;
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

    public void display() {
        display(this);
    }

    public StringTable title(String title) {
        this.title = title;
        return this;
    }

    public StringTable names(List<String> names) {
        this.names = names;
        return this;
    }

    public StringTable style(String style) {
        this.style = style;
        return this;
    }

    public StringTable data(List<List<String>> data) {
        this.data = data;
        return this;
    }

    public StringTable colorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
        return this;
    }


    /*
        Static methods
     */
    public static StringTable create() {
        return new StringTable();
    }

    public static String tableHtml(StringTable table) {
        if (table == null || table.getData() == null) {
            return "";
        }
        return HtmlTools.html(table.getTitle(), table.style, tableDiv(table));
    }

    public static String tableDiv(StringTable table) {
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
        if (names != null) {
            s.append(indent).append(indent).append(indent).append(indent).append("<TR>");
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                s.append("<TH>").append(name).append("</TH>");
                if (i == table.getColorIndex()) {
                    s.append("<TH>").append(message("Color")).append("</TH>");
                }
            }

            s.append("</TR>\n");
        }
        for (List<String> row : table.getData()) {
            s.append(indent).append(indent).append(indent).append(indent).append("<TR>");
            for (int i = 0; i < row.size(); i++) {
                String value = row.get(i);
                if (i == table.getColorIndex()) {
                    s.append("<TD>").append(value).append(" </TD>");
                    s.append("<TD align=\"center\"><DIV style=\"width: 50px;  background-color:").
                            append(value).append("; \">&nbsp;&nbsp;&nbsp;</DIV></TD>");
                } else {
                    s.append("<TD>").append(value).append("</TD>");
                }
            }
            s.append("</TR>\n");
        }
        if (names != null && table.getData().size() > 15) {
            s.append(indent).append(indent).append(indent).append(indent).append("<TR>");
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                s.append("<TH>").append(name).append("</TH>");
                if (i == table.getColorIndex()) {
                    s.append("<TH>").append(message("Color")).append("</TH>");
                }
            }
            s.append("</TR>\n");
        }
        s.append(indent).append(indent).append(indent).append("</TABLE>\n");
        s.append(indent).append(indent).append("</DIV>\n");
        return s.toString();
    }

    public static void display(StringTable table) {
        HtmlTools.displayHtml(tableHtml(table));
    }

    /*
        get/set
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

}
