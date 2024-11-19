package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class DataTreeImportController extends BaseBatchFileController {

    protected DataTreeController treeController;
    protected BaseNodeTable nodeTable;
    protected TableDataNodeTag nodeTagsTable;
    protected TableDataTag tagTable;
    protected TreeItem<DataNode> parentItem;
    protected String dataName;

    @FXML
    protected ToggleGroup existedGroup;
    @FXML
    protected RadioButton updateRadio, skipRadio, createRadio;
    @FXML
    protected Label parentLabel, formatLabel;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.XML);
    }

    public void setParamters(DataTreeController controller, TreeItem<DataNode> item) {
        try {
            if (controller == null) {
                close();
                return;
            }
            treeController = controller;
            parentItem = item != null ? item : treeController.treeView.getRoot();

            nodeTable = treeController.nodeTable;
            nodeTagsTable = treeController.nodeTagsTable;
            tagTable = treeController.tagTable;
            dataName = treeController.dataName;

            baseName = baseName + "_" + dataName;
            baseTitle = nodeTable.getTreeName() + " - "
                    + message("Import") + " : " + parentItem.getValue().getTitle();

            setControls();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setControls() {
        try {
            setTitle(baseTitle);

            parentLabel.setText(message("ParentNode") + ": "
                    + parentItem.getValue().getHierarchyNumber() + " - "
                    + parentItem.getValue().getNodeid() + " - "
                    + treeController.chainName(parentItem));

            String existed = UserConfig.getString(baseName + "Existed", "Update");
            if ("Create".equalsIgnoreCase(existed)) {
                createRadio.setSelected(true);
            } else if ("Skip".equalsIgnoreCase(existed)) {
                skipRadio.setSelected(true);
            } else {
                updateRadio.setSelected(true);
            }
            existedGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle bv) {
                    if (isSettingValues) {
                        return;
                    }
                    if (createRadio.isSelected()) {
                        UserConfig.setString(baseName + "Existed", "Create");
                    } else if (skipRadio.isSelected()) {
                        UserConfig.setString(baseName + "Existed", "Skip");
                    } else {
                        UserConfig.setString(baseName + "Existed", "Update");
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void importExamples() {
        File file = nodeTable.exampleFile();
        if (file == null) {
            return;
        }
        isSettingValues = true;
        updateRadio.setSelected(true);
        isSettingValues = false;
        startFile(file);
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        File validFile = FileTools.removeBOM(currentTask, srcFile);
        if (validFile == null) {
            return message("Failed");
        }
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(validFile, new DataTreeParser());

//            if (count >= 0) {
//                totalItemsHandled += count;
//                return message("Imported") + ": " + count;
//            } else {
//                return message("Failed");
//            }
            return message("Successful");
        } catch (Exception e) {
            return e.toString();
        }
    }

    public DataNode saveNode(Connection conn, DataNode node) {
        try {
            return nodeTable.insertData(conn, node);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    class DataTreeParser extends DefaultHandler {

        protected Connection conn;
        protected String currentTag;
        protected StringBuilder value;
        protected DataNode dataNode;
        protected DataTag dataTag;
        protected List<String> columnNames;
        protected long parentid;

        @Override
        public void startDocument() {
            try {
                conn = DerbyBase.getConnection();
                conn.setAutoCommit(false);
                columnNames = nodeTable.columnNames();
                totalItemsHandled = 0;
                parentid = parentItem.getValue().getNodeid();
                value = new StringBuilder();
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            try {
                if (qName == null || qName.isBlank()) {
                    return;
                }
                currentTag = qName;
                switch (currentTag) {
                    case "TreeNode":
                        if (dataNode != null) {
                            parentid = dataNode.getNodeid();
                        }
                        dataNode = null;
                        break;
                    case "NodeAttributes":
                        dataNode = new DataNode();
                        break;
                }
                value.setLength(0);
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            try {
                if (ch == null) {
                    return;
                }
                value.append(ch, start, length);
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            try {
                if (dataNode == null || qName == null || qName.isBlank()) {
                    return;
                }
                String s = value.toString().trim();
                switch (qName) {
                    case "title":
                        dataNode.setTitle(s);
                        break;
                    case "NodeTag":
                        dataTag = DataTag.create().setTag(s);
                        dataTag = tagTable.insertData(conn, dataTag);
                        nodeTagsTable.insertData(conn,
                                new DataNodeTag(dataNode.getNodeid(), dataTag.getTagid()));
                        dataTag = null;
                        dataNode = null;
                        break;
                    case "NodeAttributes":
                        if (parentid < 0) {
                            parentid = RootID;
                        }
                        dataNode.setParentid(parentid);
                        dataNode = nodeTable.insertData(conn, dataNode);
                        parentid = dataNode.getNodeid();
                        break;
                    case "TreeNode":
                        parentid = dataNode.getParentid();
                        dataNode = null;
                        break;
                    default:
                        if (columnNames.contains(qName)) {
                            dataNode.setValue(qName, s);
                        }
                }
                if (++totalItemsHandled % Database.BatchSize == 0) {
                    conn.commit();
                }
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void endDocument() {
            try {
                if (conn != null) {
                    conn.commit();
                    conn.close();
                }
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void warning(SAXParseException e) {
            showLogs(e.toString());
        }

        @Override
        public void error(SAXParseException e) {
            showLogs(e.toString());
        }

        @Override
        public void fatalError(SAXParseException e) {
            showLogs(e.toString());
        }

    }

    @Override
    public void afterTask(boolean ok) {
        showCost();
        treeController.refreshItem(parentItem);
        tableView.refresh();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
    }

    @FXML
    public void exampleData() {
        File file = nodeTable.exampleFile();
        if (file == null) {
            file = nodeTable.exampleFile("TextTree");
        }
        TextEditorController.open(file);
    }

    @FXML
    public void aboutTreeInformation() {
        openHtml(HelpTools.aboutTreeInformation());
    }

}
