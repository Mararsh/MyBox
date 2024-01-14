package mara.mybox.controller;

import java.io.File;
import java.util.LinkedHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-1
 * @License Apache License Version 2.0
 */
public class FilesRenameResultsController extends BaseTaskController {

    protected ObservableList<FileNode> tableData;
    protected boolean invalid;

    @FXML
    protected TableView<FileNode> tableView;
    @FXML
    protected TableColumn<FileNode, String> pathColumn, oNameColumn, nNameColumn, invalidColumn;

    public FilesRenameResultsController() {
        baseTitle = message("FilesRename");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableData = FXCollections.observableArrayList();
            tableView.setItems(tableData);

            pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
            oNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            nNameColumn.setCellValueFactory(new PropertyValueFactory<>("nodename"));
            invalidColumn.setCellValueFactory(new PropertyValueFactory<>("permission"));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public void handleFiles(LinkedHashMap<String, String> names) {
        try {
            invalid = false;
            tableData.clear();
            if (names == null || names.isEmpty()) {
                return;
            }
            for (String name : names.keySet()) {
                File file = new File(name);
                FileNode node = new FileNode(file);
                String newname = names.get(name);
                node.setData(newname);
                if (newname == null || newname.isBlank()) {
                    node.setNodename(null);
                    node.setPermission(message("Null"));
                    invalid = true;
                } else if (name.equals(newname)) {
                    node.setNodename(file.getName());
                    node.setPermission(message("Same"));
                    invalid = true;
                } else {
                    File nfile = new File(newname);
                    node.setNodename(nfile.getName());
                    if (nfile.exists()) {
                        node.setPermission(message("AlreadyExisted"));
                        invalid = true;
                    } else {
                        node.setPermission("");
                    }
                }
                tableData.add(node);
            }
            names.clear();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (invalid && !PopTools.askSure(baseTitle, message("InvalidWillSkipped"))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            for (FileNode node : tableData) {
                if (currentTask == null || !currentTask.isWorking()) {
                    updateLogs(message("Canceled"));
                    return false;
                }
                String file = node.getFullName();
                String newname = node.getData();
                if (newname == null || newname.isBlank()) {
                    updateLogs(message("Skipped") + ": " + file);
                } else if (file.equals(newname)) {
                    updateLogs(message("Skipped") + ": " + file);
                } else {
                    File nfile = new File(newname);
                    if (nfile.exists()) {
                        updateLogs(message("Skipped") + ": " + file);
                    } else if (FileTools.rename(new File(file), nfile)) {
                        updateLogs(message("Rename") + ": " + file + " -> " + newname);
                        targetFileGenerated(nfile);
                    } else {
                        updateLogs(message("Failed") + ": " + file);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterTask() {
        recordTargetFiles();
        SoundTools.miao3();
        openTarget();
    }

}
