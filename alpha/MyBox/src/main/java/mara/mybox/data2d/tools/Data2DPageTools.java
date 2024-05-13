package mara.mybox.data2d.tools;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-12
 * @License Apache License Version 2.0
 */
public class Data2DPageTools {

    public static String pageToHtml(Data2D data2d, DataFilter styleFilter,
            boolean isForm, boolean showColumns, boolean showRowNumber, boolean showTitle) {
        if (isForm) {
            return pageToHtmlForm(data2d, styleFilter, showColumns, showRowNumber, showTitle);
        } else {
            return pageToHtmlTable(data2d, styleFilter, showColumns, showRowNumber, showTitle);
        }
    }

    public static String pageToHtmlTable(Data2D data2d, DataFilter styleFilter,
            boolean showColumns, boolean showRowNumber, boolean showTitle) {
        try {
            int rNumber = data2d.tableRowsNumber();
            int cNumber = data2d.tableColsNumber();
            if (rNumber <= 0 || cNumber <= 0) {
                return null;
            }
            List<String> names;
            if (showColumns) {
                names = new ArrayList<>();
                if (showRowNumber) {
                    names.add(message("TableRowNumber"));
                    names.add(message("DataRowNumber"));
                }
                for (int i = 0; i < cNumber; i++) {
                    names.add(data2d.columnName(i));
                }
            } else {
                names = null;
            }
            String title = null;
            if (showTitle) {
                title = data2d.titleName();
            }
            StringTable table = new StringTable(names, title);

            for (int i = 0; i < rNumber; i++) {
                List<String> dataRow = data2d.tableRow(i, true, true);
                List<String> htmlRow = new ArrayList<>();
                if (showRowNumber) {
                    htmlRow.add("" + (i + 1));
                    htmlRow.add(dataRow.get(0));
                }

                for (int col = 0; col < cNumber; col++) {
                    String value = dataRow.get(col + 1);
                    value = StringTools.replaceHtmlLineBreak(value);
                    String style = data2d.cellStyle(styleFilter, i, data2d.columnName(col));
                    if (style != null && !style.isBlank()) {
                        style = style.replace("-fx-font-size:", "font-size:")
                                .replace("-fx-text-fill:", "color:")
                                .replace("-fx-background-color:", "background-color:")
                                .replace("-fx-font-weight: bolder", "font-weight:bold");
                        value = "<SPAN style=\"" + style + "\">" + value + "</SPAN>";
                    }
                    htmlRow.add(value);
                }
                table.add(htmlRow);
            }
            return table.html();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String pageToHtmlForm(Data2D data2d, DataFilter styleFilter,
            boolean showColumns, boolean showRowNumber, boolean showTitle) {
        try {
            int rNumber = data2d.tableRowsNumber();
            int cNumber = data2d.tableColsNumber();
            if (rNumber <= 0 || cNumber <= 0) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            if (showTitle) {
                s.append("<H2>").append(data2d.titleName()).append("</H2>\n");
            }
            for (int r = 0; r < rNumber; r++) {
                StringTable table = new StringTable();
                List<String> dataRow = data2d.tableRow(r, true, true);
                if (showRowNumber) {
                    List<String> htmlRow = new ArrayList<>();
                    if (showColumns) {
                        htmlRow.add(message("TableRowNumber"));
                    }
                    htmlRow.add("" + (r + 1));
                    table.add(htmlRow);

                    htmlRow = new ArrayList<>();
                    if (showColumns) {
                        htmlRow.add(message("DataRowNumber"));
                    }
                    htmlRow.add(dataRow.get(0));
                    table.add(htmlRow);
                }

                for (int col = 0; col < cNumber; col++) {
                    List<String> htmlRow = new ArrayList<>();
                    if (showColumns) {
                        htmlRow.add(data2d.columnName(col));
                    }
                    String value = dataRow.get(col + 1);
                    value = StringTools.replaceHtmlLineBreak(value);
                    String style = data2d.cellStyle(styleFilter, r, data2d.columnName(col));
                    if (style != null && !style.isBlank()) {
                        style = style.replace("-fx-font-size:", "font-size:")
                                .replace("-fx-text-fill:", "color:")
                                .replace("-fx-background-color:", "background-color:")
                                .replace("-fx-font-weight: bolder", "font-weight:bold");
                        value = "<SPAN style=\"" + style + "\">" + value + "</SPAN>";
                    }
                    htmlRow.add(value);
                    table.add(htmlRow);
                }
                s.append(table.div()).append("\n<BR><BR>\n");
            }
            return HtmlWriteTools.html(data2d.titleName(),
                    "utf-8", HtmlStyles.DefaultStyle, s.toString());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String pageToTexts(Data2D data2d, String delimiterName,
            boolean isForm, boolean showColumns, boolean showRowNumber, boolean showTitle) {
        if (isForm) {
            return pageToTextsForm(data2d, showColumns, showRowNumber, showTitle);
        } else {
            return pageToTextsTable(data2d, delimiterName, showColumns, showRowNumber, showTitle);
        }
    }

    public static String pageToTextsTable(Data2D data2d, String delimiterName,
            boolean showColumns, boolean showRowNumber, boolean showTitle) {
        String texts = data2d.encodeCSV(null, delimiterName,
                showRowNumber, showColumns, true);
        String title = showTitle ? data2d.titleName() : null;
        if (title != null && !title.isBlank()) {
            return title + "\n\n" + texts;
        } else {
            return texts;
        }
    }

    public static String pageToTextsForm(Data2D data2d,
            boolean showColumns, boolean showRowNumber, boolean showTitle) {
        StringBuilder s = new StringBuilder();
        if (showTitle) {
            s.append(data2d.titleName()).append("\n\n");
        }
        for (int r = 0; r < data2d.tableRowsNumber(); r++) {
            List<String> drow = data2d.tableRow(r, true, true);
            if (drow == null) {
                continue;
            }
            if (showRowNumber) {
                if (showColumns) {
                    s.append(message("TableRowNumber")).append(": ");
                }
                s.append(r + 1).append("\n");
                if (showColumns) {
                    s.append(message("DataRowNumber")).append(": ");
                }
                s.append(drow.get(0)).append("\n");
            }

            for (int col = 0; col < data2d.columnsNumber(); col++) {
                if (showColumns) {
                    s.append(data2d.columnName(col)).append(": ");
                }
                String v = drow.get(col + 1);
                if (v == null) {
                    continue;
                }
                s.append(StringTools.replaceLineBreak(v, "\\\\n")).append("\n");
            }
            s.append("\n");
        }
        return s.toString();
    }

}
