package mara.mybox.db.data;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.controller.BaseController;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import static mara.mybox.db.data.InfoNode.parseInfo;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlReadTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class DataNode extends BaseData {

    public static final String RootIdentify = "MyBoxTreeRoot;;;";
    public static final String TitleSeparater = " > ";
    public static final String IDPrefix = "ID:";
    public static final String TimePrefix = "Time:";
    public static final String TagsPrefix = "Tags:";
    public static final String TagSeparater = ";;;";
    public static final String ValueSeparater = "_:;MyBoxNodeValue;:_";
    public static final String MoreSeparater = "MyBoxTreeNodeMore:";
    public static final String Root = "Root";
    public static final int RootID = -9;

    protected BaseDataTable dataTable;
    protected long nodeid, parentid;
    protected String title;
    protected Date updateTime;

    private void init() {
        dataTable = null;
        nodeid = -1;
        parentid = -2;
        title = null;
        updateTime = new Date();
    }

    public DataNode() {
        init();
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    @Override
    public boolean valid() {
        return true;
    }

    public DataValues dataValues(Connection conn) {
        return dataValues(conn, this);
    }

    public boolean isRoot() {
        return parentid == nodeid;
    }

    public DataNode copy() {
        return copy(this);
    }

    public String texts() {
        return title;
    }

    public String toText() {
        return title;
    }

    public String toHtml() {
        return title;
    }

    public String getValue() {
        return title;
    }

    /*
        Static methods
     */
    public static DataNode create() {
        return new DataNode();
    }

    public static boolean setValue(DataNode node, String column, Object value) {
        if (node == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    node.setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    node.setTitle(value == null ? null : (String) value);
                    return true;
                case "parentid":
                    node.setParentid(value == null ? -1 : (long) value);
                    return true;
                case "update_time":
                    node.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(DataNode node, String column) {
        if (node == null || column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return node.getNodeid();
            case "title":
                return node.getTitle();
            case "parentid":
                return node.getParentid();
            case "update_time":
                return node.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(DataNode node) {
        return node != null;
    }

    public static DataNode createRoot(BaseDataTable dataTable) {
        if (dataTable == null) {
            return null;
        }
        DataNode root = new DataNode()
                .setDataTable(dataTable)
                .setParentid(RootID)
                .setNodeid(RootID)
                .setTitle(dataTable.getTableTitle());
        return root;
    }

    public static DataNode createChild(DataNode parent) {
        return create().
                setDataTable(parent.getDataTable())
                .setParentid(parent.getNodeid());
    }

    public static DataNode createChild(DataNode parent, String title) {
        return createChild(parent).setTitle(title);
    }

    public static DataNode copy(DataNode node) {
        return create().
                setDataTable(node.getDataTable())
                .setParentid(node.getParentid())
                .setTitle(node.getTitle() + " " + message("Copy"));
    }

    public static DataValues dataValues(Connection conn, DataNode node) {
        try {
            DataValues values = (DataValues) node.getDataTable().query(conn, node.getNodeid());
            values.setTable(node.getDataTable());
            return values;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String valuesHtml(FxTask task, BaseController controller,
            String category, String s, boolean showIcon, boolean singleNotIn) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String html = "";
        switch (category) {
            case InfoNode.Notebook:
                html = HtmlReadTools.body(s, false);
                break;
            case InfoNode.WebFavorite: {
                Map<String, String> values = InfoNode.parseInfo(category, s);
                if (values != null) {
                    String address = values.get("Address");
                    if (address != null && !address.isBlank()) {
                        html = "<A href=\"" + address + "\">";
                        String icon = values.get("Icon");
                        if (showIcon && icon != null && !icon.isBlank()) {
                            try {
                                String base64 = FxImageTools.base64(null, new File(icon), "png");
                                if (base64 != null) {
                                    html += "<img src=\"data:image/png;base64," + base64 + "\" width=" + 40 + " >";
                                }
                            } catch (Exception e) {
                            }
                        }
                        html += address + "</A>\n";
                    }
                }
                break;
            }
            case InfoNode.Data2DDefinition: {
                DataFileCSV csv = Data2DDefinitionTools.definitionFromXML(task, controller, s);
                if (csv != null) {
                    html = Data2DDefinitionTools.definitionToHtml(csv);
                }
                break;
            }
            case InfoNode.ImageScope: {
                ImageScope scope = ImageScopeTools.fromXML(task, controller, s);
                if (scope != null) {
                    html = ImageScopeTools.toHtml(task, scope);
                }
                break;
            }
            default: {
                Map<String, String> values = parseInfo(category, s);
                if (values != null) {
                    StringTable table = new StringTable();
                    String pv = s;
                    for (String key : values.keySet()) {
                        String v = values.get(key);
                        if (v == null || v.isBlank()) {
                            continue;
                        }
                        pv = "<PRE><CODE>" + v + "</CODE></PRE>";
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(message(key), pv));
                        table.add(row);
                    }
                    if (!table.isEmpty()) {
                        if (singleNotIn && table.size() == 1) {
                            html = pv;
                        } else {
                            html = table.div();
                        }
                    }
                }
                break;
            }
        }
        return html;
    }


    /*
        get/set
     */
    public BaseDataTable getDataTable() {
        return dataTable;
    }

    public DataNode setDataTable(BaseDataTable dataTable) {
        this.dataTable = dataTable;
        return this;
    }

    public long getNodeid() {
        return nodeid;
    }

    public DataNode setNodeid(long nodeid) {
        this.nodeid = nodeid;
        return this;
    }

    public long getParentid() {
        return parentid;
    }

    public DataNode setParentid(long parentid) {
        this.parentid = parentid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DataNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public DataNode setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
