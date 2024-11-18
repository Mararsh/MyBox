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
import mara.mybox.value.AppVariables;
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
    protected DataNode rootNode;

    @FXML
    protected ToggleGroup existedGroup;
    @FXML
    protected RadioButton updateRadio, skipRadio, createRadio;
    @FXML
    protected Label formatLabel;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.XML);
    }

    public void setParamters(DataTreeController controller) {
        try {
            this.treeController = controller;
            nodeTable = treeController.nodeTable;
            nodeTagsTable = treeController.nodeTagsTable;
            tagTable = treeController.tagTable;

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
    public boolean makeMoreParameters() {
        rootNode = treeController.getRootNode();
        if (rootNode == null) {
            return false;
        }
        return super.makeMoreParameters();
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

    class DataTreeParser extends DefaultHandler {

        private Connection conn;
        private String currentTag;
        protected DataNode dataNode;
        protected DataTag dataTag;
        protected List<String> columnNames;
        protected long parentid, count;

        @Override
        public void startDocument() {
            try {
                conn = DerbyBase.getConnection();
                conn.setAutoCommit(false);
                columnNames = nodeTable.columnNames();
                count = 0;
                parentid = -1;
            } catch (Exception e) {

            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            try {
                if (localName == null || localName.isBlank()) {
                    return;
                }
                currentTag = localName.toLowerCase();
                switch (currentTag) {
                    case "node":
                        if (dataNode != null) {
                            parentid = dataNode.getNodeid();
                        }
                        dataNode = null;
                        break;
                    case "node_attributes":
                        dataNode = new DataNode();
                        break;
                }
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            try {
                if (dataNode == null || currentTag == null || ch == null) {
                    return;
                }
                String value = new String(ch, start, length);
                switch (currentTag) {
                    case "nodeid":
//                        long nodeid = -1;
//                        try {
//                            nodeid = Long.parseLong(value);
//                        } catch (Exception e) {
//                        }
//                        dataNode.setNodeid(nodeid);
                        break;
                    case "tag":
                        dataTag = DataTag.create().setTag(value);
                        break;
                    default:
                        if (columnNames.contains(currentTag)) {
                            dataNode.setValue(currentTag, value);
                        }
                }
            } catch (Exception e) {
                showLogs(e.toString());
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            try {
                if (dataNode == null
                        || localName == null || localName.isBlank()) {
                    return;
                }
                switch (localName.toLowerCase()) {
                    case "node_attributes":
                        if (parentid < 0) {
                            parentid = RootID;
                        }
                        dataNode.setParentid(parentid);
                        dataNode = nodeTable.insertData(conn, dataNode);
                        parentid = dataNode.getNodeid();
                        break;
                    case "node":
                        parentid = dataNode.getParentid();
                        dataNode = null;
                        break;
                    case "tag":
                        if (dataTag != null) {
                            dataTag = tagTable.insertData(conn, dataTag);
                            nodeTagsTable.insertData(conn,
                                    new DataNodeTag(dataNode.getNodeid(), dataTag.getTagid()));
                            dataTag = null;
                        }
                        dataNode = null;
                        break;
                }
                if (++count % Database.BatchSize == 0) {
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
        treeController.loadTree();
        tableView.refresh();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (!isPreview) {
            closeStage();
        }
        if (!AppVariables.isTesting) {
            treeController.popInformation(message("Imported") + ": " + totalItemsHandled);
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
