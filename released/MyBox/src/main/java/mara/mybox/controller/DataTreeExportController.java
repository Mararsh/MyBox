package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataNodeTools;
import mara.mybox.db.data.VisitHistory;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class DataTreeExportController extends BaseDataTreeHandleController {

    protected TableDataNodeTag nodeTagsTable;
    protected DataNode sourceNode;
    protected long sourceid;
    protected File treeXmlFile, treeHtmlFile, treeJsonFile, listJsonFile,
            listHtmlFile, listXmlFile, listCsvFile, framesetFile, framesetNavFile;
    protected FileWriter treeHtmlWriter, treeXmlWriter, treeJsonWriter,
            listJsonWriter, listHtmlWriter, listXmlWriter, framesetNavWriter;
    protected CSVPrinter csvPrinter;
    protected String hierarchyNumber, language;
    protected int count, level, childrenNumber;
    protected Charset charset;
    protected Stack<Integer> childrenNumberStack;

    @FXML
    protected CheckBox idCheck, hierarchyCheck, timeCheck, tagsCheck, orderCheck,
            parentCheck, dataCheck, treeXmlCheck, treeHtmlCheck, treeJsonCheck,
            listJsonCheck, listCsvCheck, listXmlCheck, listHtmlCheck, framesetCheck,
            dataFormatCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected TextArea styleInput;
    @FXML
    protected Label nodeLabel;

    public void setParameters(BaseDataTreeController parent, DataNode node) {
        try {
            super.setParameters(parent);

            this.nodeTagsTable = parent.nodeTagsTable;
            sourceNode = node;
            sourceNode = node != null ? node : dataController.rootNode;
            sourceid = sourceNode.getNodeid();

            baseTitle = nodeTable.getTreeName() + " - "
                    + message("Export") + " : " + sourceNode.getTitle();
            chainName = sourceNode.shortDescription();

            setControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setControls() {
        try {
            setTitle(baseTitle);

            nodeLabel.setText(message("Node") + ": " + chainName);

            dataFormatCheck.setSelected(UserConfig.getBoolean(baseName + "Format", true));
            dataFormatCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Format", dataFormatCheck.isSelected());
                }
            });

            idCheck.setSelected(UserConfig.getBoolean(baseName + "ID", false));
            idCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "ID", idCheck.isSelected());
                }
            });

            hierarchyCheck.setSelected(UserConfig.getBoolean(baseName + "Hierarcy", false));
            hierarchyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Hierarcy", hierarchyCheck.isSelected());
                }
            });

            timeCheck.setSelected(UserConfig.getBoolean(baseName + "Time", false));
            timeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Time", timeCheck.isSelected());
                }
            });

            tagsCheck.setSelected(UserConfig.getBoolean(baseName + "Tags", true));
            tagsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Tags", tagsCheck.isSelected());
                }
            });

            orderCheck.setSelected(UserConfig.getBoolean(baseName + "Order", true));
            orderCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Order", orderCheck.isSelected());
                }
            });

            parentCheck.setSelected(UserConfig.getBoolean(baseName + "Parent", false));
            parentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Parent", parentCheck.isSelected());
                }
            });

            dataCheck.setSelected(UserConfig.getBoolean(baseName + "Data", true));
            dataCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Data", dataCheck.isSelected());
                }
            });

            treeHtmlCheck.setSelected(UserConfig.getBoolean(baseName + "TreeHtml", true));
            treeHtmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "TreeHtml", treeHtmlCheck.isSelected());
                }
            });

            treeXmlCheck.setSelected(UserConfig.getBoolean(baseName + "TreeXml", true));
            treeXmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "TreeXml", treeXmlCheck.isSelected());
                }
            });

            treeJsonCheck.setSelected(UserConfig.getBoolean(baseName + "TreeJson", true));
            treeJsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "TreeJson", treeJsonCheck.isSelected());
                }
            });

            listHtmlCheck.setSelected(UserConfig.getBoolean(baseName + "ListHtml", false));
            listHtmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "ListHtml", listHtmlCheck.isSelected());
                }
            });

            listXmlCheck.setSelected(UserConfig.getBoolean(baseName + "ListXML", false));
            listXmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "ListXML", listXmlCheck.isSelected());
                }
            });

            listCsvCheck.setSelected(UserConfig.getBoolean(baseName + "ListCSV", false));
            listCsvCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "ListCSV", listCsvCheck.isSelected());
                }
            });

            listJsonCheck.setSelected(UserConfig.getBoolean(baseName + "ListJson", false));
            listJsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "ListJson", listJsonCheck.isSelected());
                }
            });

            framesetCheck.setSelected(UserConfig.getBoolean(baseName + "Frameset", false));
            framesetCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Frameset", framesetCheck.isSelected());
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            try {
                charset = Charset.forName(UserConfig.getString(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setString(baseName + "Charset", charset.name());
                }
            });

            styleInput.setText(UserConfig.getString(baseName + "Style", HtmlStyles.styleValue("Default")));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (sourceNode == null) {
            close();
            return false;
        }
        treeXmlFile = null;
        treeHtmlFile = null;
        treeJsonFile = null;
        listJsonFile = null;
        listHtmlFile = null;
        listXmlFile = null;
        listCsvFile = null;
        framesetFile = null;
        framesetNavFile = null;
        treeXmlWriter = null;
        treeHtmlWriter = null;
        treeJsonWriter = null;
        listHtmlWriter = null;
        listXmlWriter = null;
        listJsonWriter = null;
        framesetNavWriter = null;
        csvPrinter = null;
        level = count = 0;
        if (!listHtmlCheck.isSelected() && !framesetCheck.isSelected()
                && !treeXmlCheck.isSelected() && !treeHtmlCheck.isSelected()
                && !treeJsonCheck.isSelected() && !listCsvCheck.isSelected()
                && !listXmlCheck.isSelected() && !listJsonCheck.isSelected()) {
            popError(message("NothingSave"));
            return false;
        }

        targetPath = targetPathController.pickFile();
        if (targetPath == null) {
            popError(message("InvalidParameters") + ": " + message("TargetPath"));
            return false;
        }
        return true;
    }

    @FXML
    public void popDefaultStyle(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                menu = new MenuItem(message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        styleInput.setText(HtmlStyles.styleValue(style));
                        UserConfig.setString(baseName + "Style", styleInput.getText());
                    }
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            popEventMenu(mouseEvent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void clearStyle() {
        styleInput.clear();
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        if (sourceNode == null || targetPath == null) {
            return false;
        }
        count = level = 0;
        childrenNumberStack = new Stack();
        childrenNumber = 0;
        try (Connection conn = DerbyBase.getConnection()) {
            chainName = nodeTable.chainName(currentTask, conn, sourceNode);
            showLogs("Export started. Node: " + sourceid + " - " + chainName);
            if (!openWriters()) {
                closeWriters();
                return false;
            }
            conn.setAutoCommit(true);
            exportNode(currentTask, conn, sourceid, chainName, "");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return closeWriters();
    }

    protected boolean openWriters() {
        if (sourceNode == null || targetPath == null || chainName == null) {
            return false;
        }
        try {
            String prefix = chainName.replaceAll(DataNode.TitleSeparater, "-");

            if (treeXmlCheck.isSelected()) {
                treeXmlFile = makeTargetFile(prefix + "_tree", ".xml", targetPath);
                if (treeXmlFile != null) {
                    showLogs(message("Writing") + " " + treeXmlFile.getAbsolutePath());
                    treeXmlWriter = new FileWriter(treeXmlFile, charset);
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(charset.name()).append("\"?>\n")
                            .append("<").append(nodeTable.getTableName()).append(">\n");
                    treeXmlWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (listXmlCheck.isSelected()) {
                listXmlFile = makeTargetFile(prefix + "_list", ".xml", targetPath);
                if (listXmlFile != null) {
                    showLogs(message("Writing") + " " + listXmlFile.getAbsolutePath());
                    listXmlWriter = new FileWriter(listXmlFile, charset);
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(charset.name()).append("\"?>\n")
                            .append("<").append(nodeTable.getTableName()).append(">\n");
                    listXmlWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (treeJsonCheck.isSelected()) {
                treeJsonFile = makeTargetFile(prefix + "_tree", ".json", targetPath);
                if (treeJsonFile != null) {
                    showLogs(message("Writing") + " " + treeJsonFile.getAbsolutePath());
                    treeJsonWriter = new FileWriter(treeJsonFile, Charset.forName("UTF-8"));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"").append(nodeTable.getTreeName()).append("\": \n");
                    treeJsonWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (listJsonCheck.isSelected()) {
                listJsonFile = makeTargetFile(prefix + "_list", ".json", targetPath);
                if (listJsonFile != null) {
                    showLogs(message("Writing") + " " + listJsonFile.getAbsolutePath());
                    listJsonWriter = new FileWriter(listJsonFile, Charset.forName("UTF-8"));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"").append(nodeTable.getTreeName()).append("\": [\n");
                    listJsonWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (listCsvCheck.isSelected()) {
                listCsvFile = makeTargetFile(prefix, ".csv", targetPath);
                if (listCsvFile != null) {
                    showLogs(message("Writing") + " " + listCsvFile.getAbsolutePath());
                    csvPrinter = new CSVPrinter(new FileWriter(listCsvFile, charset), CsvTools.csvFormat());

                    List<String> names = new ArrayList<>();
                    if (idCheck.isSelected()) {
                        names.add(message("NodeID"));
                        names.add(message("ParentID"));
                    }
                    if (parentCheck.isSelected()) {
                        names.add(message("ParentNode"));
                    }
                    if (hierarchyCheck.isSelected()) {
                        names.add(message("HierarchyNumber"));
                    }
                    names.add(message("Title"));
                    if (orderCheck.isSelected()) {
                        names.add(message("OrderNumber"));
                    }
                    if (timeCheck.isSelected()) {
                        names.add(message("Update_Time"));
                    }
                    if (tagsCheck.isSelected()) {
                        names.add(message("Tags"));
                    }
                    if (dataCheck.isSelected()) {
                        for (String name : nodeTable.dataColumnNames()) {
                            names.add(name);
                        }
                    }
                    csvPrinter.printRecord(names);

                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (treeHtmlCheck.isSelected()) {
                treeHtmlFile = makeTargetFile(prefix + "_tree", ".html", targetPath);
                if (treeHtmlFile != null) {
                    showLogs(message("Writing") + " " + treeHtmlFile.getAbsolutePath());
                    treeHtmlWriter = new FileWriter(treeHtmlFile, charset);
                    writeHtmlHead(treeHtmlWriter, chainName);
                    treeHtmlWriter.write(Indent + "<BODY>\n" + Indent + Indent + "<H2>" + chainName + "</H2>\n");
                    treeHtmlWriter.write(DataNodeTools.htmlControls(true));
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (listHtmlCheck.isSelected()) {
                listHtmlFile = makeTargetFile(prefix + "_list", ".html", targetPath);
                if (listHtmlFile != null) {
                    showLogs(message("Writing") + " " + listHtmlFile.getAbsolutePath());
                    listHtmlWriter = new FileWriter(listHtmlFile, charset);
                    writeHtmlHead(listHtmlWriter, chainName);
                    listHtmlWriter.write(Indent + "<BODY>\n" + Indent + Indent + "<H2>" + chainName + "</H2>\n");
                    listHtmlWriter.write(DataNodeTools.htmlControls(false));
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

            if (framesetCheck.isSelected()) {
                framesetFile = makeTargetFile(prefix, "_frameset.html", targetPath);
                if (framesetFile != null) {
                    showLogs(message("Writing") + " " + framesetFile.getAbsolutePath());
                    StringBuilder s;
                    String subPath = FileNameTools.filter(prefix) + "-frameset";
                    File path = new File(targetPath + File.separator + subPath + File.separator);
                    path.mkdirs();
                    framesetNavFile = new File(path.getAbsolutePath() + File.separator + "nav.html");
                    File coverFile = new File(path.getAbsolutePath() + File.separator + "cover.html");
                    try (FileWriter coverWriter = new FileWriter(coverFile, charset)) {
                        writeHtmlHead(coverWriter, chainName);
                        coverWriter.write("<BODY>\n<BR><BR><BR><BR><H1>" + message("Notes") + "</H1>\n</BODY></HTML>");
                        coverWriter.flush();
                    }
                    try (FileWriter framesetWriter = new FileWriter(framesetFile, charset)) {
                        writeHtmlHead(framesetWriter, chainName);
                        s = new StringBuilder();
                        s.append("<FRAMESET border=2 cols=240,240,*>\n")
                                .append("<FRAME name=nav src=\"").append(subPath).append("/").append(framesetNavFile.getName()).append("\" />\n")
                                .append("<FRAME name=booknav />\n")
                                .append("<FRAME name=main src=\"").append(subPath).append("/cover.html\" />\n</HTML>\n");
                        framesetWriter.write(s.toString());
                        framesetWriter.flush();
                    }
                    framesetNavWriter = new FileWriter(framesetNavFile, charset);
                    writeHtmlHead(framesetNavWriter, chainName);
                    s = new StringBuilder();
                    s.append(Indent).append("<BODY>\n");
                    s.append(Indent).append(Indent).append("<H2>").append(chainName).append("</H2>\n");
                    framesetNavWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    showLogs(message("Skipped"));
                }
            }

        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
        return true;
    }

    protected boolean closeWriters() {
        if (sourceNode == null || targetPath == null) {
            return false;
        }
        boolean well = true;

        if (treeXmlWriter != null) {
            try {
                treeXmlWriter.write("</" + nodeTable.getTableName() + ">\n");
                treeXmlWriter.flush();
                treeXmlWriter.close();
                treeXmlWriter = null;
                targetFileGenerated(treeXmlFile, VisitHistory.FileType.XML);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (listXmlWriter != null) {
            try {
                listXmlWriter.write("</" + nodeTable.getTableName() + ">\n");
                listXmlWriter.flush();
                listXmlWriter.close();
                listXmlWriter = null;
                targetFileGenerated(listXmlFile, VisitHistory.FileType.XML);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (treeJsonWriter != null) {
            try {
                treeJsonWriter.write("\n}\n");
                treeJsonWriter.flush();
                treeJsonWriter.close();
                treeJsonWriter = null;
                targetFileGenerated(treeJsonFile, VisitHistory.FileType.JSON);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (listJsonWriter != null) {
            try {
                listJsonWriter.write("\n]}\n");
                listJsonWriter.flush();
                listJsonWriter.close();
                listJsonWriter = null;
                targetFileGenerated(listJsonFile, VisitHistory.FileType.JSON);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (csvPrinter != null) {
            try {
                csvPrinter.flush();
                csvPrinter.close();
                csvPrinter = null;
                targetFileGenerated(listCsvFile, VisitHistory.FileType.CSV);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (treeHtmlWriter != null) {
            try {
                treeHtmlWriter.write(Indent + "</BODY>\n</HTML>\n");
                treeHtmlWriter.flush();
                treeHtmlWriter.close();
                treeHtmlWriter = null;
                targetFileGenerated(treeHtmlFile, VisitHistory.FileType.Html);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (listHtmlWriter != null) {
            try {
                listHtmlWriter.write(Indent + "</BODY>\n</HTML>\n");
                listHtmlWriter.flush();
                listHtmlWriter.close();
                listHtmlWriter = null;
                targetFileGenerated(listHtmlFile, VisitHistory.FileType.Html);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        if (framesetNavWriter != null) {
            try {
                framesetNavWriter.write(Indent + "</BODY>\n</HTML>\n");
                framesetNavWriter.flush();
                framesetNavWriter.close();
                framesetNavWriter = null;
                targetFileGenerated(framesetFile, VisitHistory.FileType.Html);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }

        return well;
    }

    public void exportNode(FxTask currentTask, Connection conn, long nodeid,
            String parentChainName, String hierarchyNumber) {
        level++;
        if (conn == null || nodeid < 0) {
            return;
        }
        try {
            count++;

            DataNode node = nodeTable.query(conn, nodeid);
            if (hierarchyCheck.isSelected()) {
                node.setHierarchyNumber(hierarchyNumber);
            }
            String nodeChainName = null;
            if (parentChainName != null && !parentChainName.isBlank()) {
                nodeChainName = parentChainName + DataNode.TitleSeparater + node.getTitle();
            } else {
                nodeChainName = node.getTitle();
            }
            if (parentCheck.isSelected()) {
                node.setChainName(nodeChainName);
            }
            if (isLogsVerbose()) {
                showLogs("Handling node: " + nodeid + " - " + node.getTitle());
            }
            List<DataNodeTag> tags = null;
            if (tagsCheck.isSelected()) {
                tags = nodeTagsTable.nodeTags(conn, nodeid);
            }
            String nodePrefix = "";
            for (int i = 0; i < level; i++) {
                nodePrefix += Indent;
            }

            if (treeXmlWriter != null) {
                if (nodeid != RootID) {
                    treeXmlWriter.write(nodePrefix + "<TreeNode>\n");
                }
                writeTreeXML(currentTask, conn, node, tags, nodePrefix);
            }
            if (listXmlWriter != null) {
                writeListXML(currentTask, conn, node, tags);
            }
            if (treeJsonWriter != null) {
                writeTreeJson(currentTask, conn, node, tags, nodePrefix);
            }
            if (listJsonWriter != null) {
                writeListJson(currentTask, conn, node, tags);
            }
            if (csvPrinter != null) {
                writeListCsv(currentTask, conn, node, tags);
            }
            if (treeHtmlWriter != null) {
                writeTreeHtml(currentTask, conn, node, tags);
            }
            if (listHtmlWriter != null) {
                writeListHtml(currentTask, conn, node, tags, listHtmlWriter);
            }

            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }

            if (framesetNavWriter != null) {
                String nodeTitle = node.getTitle() + "_" + node.getNodeid();
                File bookNavFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(nodeTitle) + "_nav.html");
                FileWriter bookNavWriter = new FileWriter(bookNavFile, charset);
                writeHtmlHead(bookNavWriter, nodeTitle);
                bookNavWriter.write(Indent + "<BODY>\n");

                File nodeFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(nodeTitle) + ".html");
                FileWriter bookWriter = new FileWriter(nodeFile, charset);
                writeHtmlHead(bookWriter, nodeTitle);
                bookWriter.write(Indent + "<BODY>\n");
                String prefix = "";
                for (int i = 1; i < level; i++) {
                    prefix += "&nbsp;&nbsp;&nbsp;&nbsp;";
                }
                framesetNavWriter.write(prefix + "<A href=\"" + bookNavFile.getName() + "\"  target=booknav>" + node.getTitle() + "</A><BR>\n");

                writeListHtml(currentTask, conn, node, tags, bookWriter);
                try {
                    bookWriter.write(Indent + "\n</BODY>\n</HTML>");
                    bookWriter.flush();
                    bookWriter.close();
                } catch (Exception e) {
                    showLogs(e.toString());
                }

                String sql = "SELECT nodeid, title FROM " + nodeTable.getTableName()
                        + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + nodeTable.getOrderColumns();
                boolean hasChildren = false;
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, nodeid);
                    try (ResultSet results = statement.executeQuery()) {
                        String title;
                        while (results != null && results.next()) {
                            if (currentTask == null || !currentTask.isWorking()) {
                                return;
                            }
                            title = results.getString("title");
                            File childFile = new File(framesetNavFile.getParent() + File.separator
                                    + FileNameTools.filter(title + "_" + results.getLong("nodeid")) + ".html");
                            bookNavWriter.write("<A href=\"" + childFile.getName() + "\"  target=main>" + title + "</A><BR>\n");
                            hasChildren = true;
                        }
                    } catch (Exception e) {
                        showLogs(e.toString());
                    }
                } catch (Exception e) {
                    showLogs(e.toString());
                }

                if (!hasChildren) {
                    bookNavWriter.write("<A href=\"" + nodeFile.getName() + "\"  target=main>" + node.getTitle() + "</A><BR>\n");
                }
                try {
                    bookNavWriter.write(Indent + "\n</BODY>\n</HTML>");
                    bookNavWriter.flush();
                    bookNavWriter.close();
                } catch (Exception e) {
                    showLogs(e.toString());
                }
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }

            childrenNumberStack.push(childrenNumber);
            childrenNumber = 0;

            if (treeHtmlWriter != null) {
                treeHtmlWriter.write(Indent + "<DIV class=\"Children\" id='children" + nodeid + "'>\n");
            }
            String sql = "SELECT nodeid FROM " + nodeTable.getTableName()
                    + " WHERE parentid=? AND parentid<>nodeid  ORDER BY " + nodeTable.getOrderColumns();
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, nodeid);
                try (ResultSet results = statement.executeQuery()) {
                    String ps = hierarchyNumber == null || hierarchyNumber.isBlank() ? "" : hierarchyNumber + ".";
                    while (results != null && results.next()) {
                        if (currentTask == null || !currentTask.isWorking()) {
                            return;
                        }
                        childrenNumber++;
                        exportNode(currentTask, conn, results.getLong("nodeid"),
                                nodeChainName, ps + childrenNumber);
                    }
                } catch (Exception e) {
                    showLogs(e.toString());
                }
            } catch (Exception e) {
                showLogs(e.toString());
            }
            if (treeHtmlWriter != null) {
                treeHtmlWriter.write(Indent + "</DIV>\n");
            }
            if (treeJsonWriter != null) {
                if (childrenNumber > 0) {
                    treeJsonWriter.write(nodePrefix + Indent + "]\n");
                    treeJsonWriter.write(nodePrefix + Indent + "}\n");
                } else {
                    treeJsonWriter.write("\n" + nodePrefix + Indent + "}\n");
                }
            }

            childrenNumber = childrenNumberStack.pop();

            if (nodeid != RootID && treeXmlWriter != null) {
                treeXmlWriter.write(nodePrefix + "</TreeNode>\n");
            }

        } catch (Exception e) {
            showLogs(e.toString());
        }
        level--;
    }

    protected void writeHtmlHead(FileWriter writer, String title) {
        try {
            writer.write(HtmlWriteTools.htmlPrefix(title, charset.name(), styleInput.getText()));
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeTreeXML(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags, String prefix) {
        try {
            String xml = DataNodeTools.toXML(currentTask, conn,
                    myController, nodeTable, node, tags,
                    prefix,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected(), dataFormatCheck.isSelected());
            treeXmlWriter.write(xml);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeListXML(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags) {
        try {
            listXmlWriter.write(Indent + "<TreeNode>\n");
            String xml = DataNodeTools.toXML(currentTask, conn,
                    myController, nodeTable, node, tags,
                    Indent + Indent,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected(), dataFormatCheck.isSelected());
            listXmlWriter.write(xml);
            listXmlWriter.write(Indent + "</TreeNode>\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeTreeHtml(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags) {
        try {
            String html = DataNodeTools.treeNodeHtml(currentTask, conn,
                    myController, nodeTable, node, tags,
                    4 * level,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected());
            treeHtmlWriter.write(html);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeListHtml(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags, FileWriter writer) {
        try {
            String html = DataNodeTools.listNodeHtml(currentTask, conn,
                    myController, nodeTable, node, tags,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected());
            writer.write(html);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeTreeJson(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags, String prefix) {
        try {
            if (childrenNumber == 1) {
                if (node.getNodeid() != RootID && sourceid != node.getNodeid()) {
                    treeJsonWriter.write(",\n");
                }
                treeJsonWriter.write(prefix + "\"" + message("ChildrenNodes") + "\":[\n");
            } else if (childrenNumber > 1) {
                treeJsonWriter.write(prefix + Indent + ",\n");
            }
            treeJsonWriter.write(prefix + Indent + "{\n");
            String json = DataNodeTools.toJson(currentTask, conn,
                    myController, nodeTable, node, tags,
                    prefix + Indent,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected(), dataFormatCheck.isSelected());
            treeJsonWriter.write(json);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeListJson(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags) {
        try {
            if (node.getNodeid() != RootID && sourceid != node.getNodeid()) {
                listJsonWriter.write(Indent + ",\n");
            }
            listJsonWriter.write(Indent + "{\n");
            String json = DataNodeTools.toJson(currentTask, conn,
                    myController, nodeTable, node, tags,
                    Indent,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected(), dataFormatCheck.isSelected());
            listJsonWriter.write(json);
            listJsonWriter.write("\n");
            listJsonWriter.write(Indent + "}\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeListCsv(FxTask currentTask, Connection conn,
            DataNode node, List<DataNodeTag> tags) {
        try {
            List<String> row = DataNodeTools.toCsv(currentTask, conn,
                    myController, nodeTable, node, tags,
                    idCheck.isSelected(), timeCheck.isSelected(),
                    orderCheck.isSelected(), dataCheck.isSelected(), dataFormatCheck.isSelected());
            if (row != null) {
                csvPrinter.printRecord(row);
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void afterSuccess() {
        try {
            if (openCheck.isSelected()) {
                if (framesetFile != null && framesetFile.exists()) {
                    WebBrowserController.openFile(framesetFile);
                }
                if (listHtmlFile != null && listHtmlFile.exists()) {
                    WebBrowserController.openFile(listHtmlFile);
                }
                if (treeHtmlFile != null && treeHtmlFile.exists()) {
                    WebBrowserController.openFile(treeHtmlFile);
                }

                if (listCsvFile != null && listCsvFile.exists()) {
                    Data2DManufactureController.openCSVFile(listCsvFile);
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (listJsonFile != null && listJsonFile.exists()) {
                                JsonEditorController.open(listJsonFile);
                            }
                            if (listXmlFile != null && listXmlFile.exists()) {
                                XmlEditorController.open(listXmlFile);
                            }
                        });
                    }
                }, 1000);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (treeJsonFile != null && treeJsonFile.exists()) {
                                JsonEditorController.open(treeJsonFile);
                            }
                            if (treeXmlFile != null && treeXmlFile.exists()) {
                                XmlEditorController.open(treeXmlFile);
                            }
                        });
                    }
                }, 2000);

            }
            showLogs(message("Count") + ": " + count);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void openTarget() {
        browseURI(targetPath.toURI());
    }

    @FXML
    public void selectAllFormatAction() {
        selectAllFormat(true);
    }

    @FXML
    public void selectNoneFormatAction() {
        selectAllFormat(false);
    }

    public void selectAllFormat(boolean select) {
        treeXmlCheck.setSelected(select);
        treeHtmlCheck.setSelected(select);
        treeJsonCheck.setSelected(select);
        listJsonCheck.setSelected(select);
        listCsvCheck.setSelected(select);
        listXmlCheck.setSelected(select);
        listHtmlCheck.setSelected(select);
        framesetCheck.setSelected(select);
    }

    @FXML
    public void selectAllValueAction() {
        selectAllValue(true);
    }

    @FXML
    public void selectNoneValueAction() {
        selectAllValue(false);
    }

    public void selectAllValue(boolean select) {
        idCheck.setSelected(select);
        hierarchyCheck.setSelected(select);
        timeCheck.setSelected(select);
        tagsCheck.setSelected(select);
        orderCheck.setSelected(select);
        parentCheck.setSelected(select);
        dataCheck.setSelected(select);
    }

    @Override
    public boolean needStageVisitHistory() {
        return false;
    }

    /*
        static methods
     */
    public static DataTreeExportController open(BaseDataTreeController parent, DataNode node) {
        DataTreeExportController controller
                = (DataTreeExportController) WindowTools.referredStage(parent, Fxmls.DataTreeExportFxml);
        controller.setParameters(parent, node);
        controller.requestMouse();
        return controller;
    }
}
