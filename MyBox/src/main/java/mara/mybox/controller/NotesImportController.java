package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-7
 * @License Apache License Version 2.0
 */
public class NotesImportController extends BaseBatchFileController {

    protected NotesController notesController;
    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected boolean isImportExamples;
    @FXML
    protected CheckBox timeCheck;
    @FXML
    protected RadioButton overrideRadio, createRadio, skipRadio;

    public NotesImportController() {
        baseTitle = AppVariables.message("ImportNotesFile");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            timeCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "HaveTime", true));
            timeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.setUserConfigValue(baseName + "HaveTime", timeCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void importExamples(NotesController notesController) {
        this.notesController = notesController;
        tableNotebook = notesController.tableNotebook;
        String lang = AppVariables.isChinese() ? "zh" : "en";
        File file = FxmlControl.getInternalFile("/data/db/Notes_Examples_" + lang + ".txt",
                "data", "Notes_Examples_" + lang + ".txt", true);
        isSettingValues = true;
        timeCheck.setSelected(false);
        overrideRadio.fire();
        isSettingValues = false;
        isImportExamples = true;
        startFile(file);
    }

    @Override
    public boolean makeMoreParameters() {
        if (tableNotebook == null) {
            tableNotebook = new TableNotebook();
        }
        if (tableNote == null) {
            tableNote = new TableNote();
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            long count = importNotes(srcFile);
            if (count >= 0) {
                totalItemsHandled += count;
                return message("Imported") + ": " + count;
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public long importNotes(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        long count = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 BufferedReader reader = new BufferedReader(new FileReader(file, FileTools.charset(file)))) {
            conn.setAutoCommit(false);
            String line, title, html;
            Date time;
            long baseTime = new Date().getTime();
            Notebook notebook;
            Map<String, Notebook> owners = new HashMap<>();
            while (true) {
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                if (owners.containsKey(line)) {
                    notebook = owners.get(line);
                } else {
                    notebook = tableNotebook.findAndCreateChain(conn, line);
                    if (notebook == null) {
                        break;
                    }
                    owners.put(line, notebook);
                }
                long notebookid = notebook.getNbid();
                title = reader.readLine();
                if (title == null || title.isBlank()) {
                    continue;
                }
                Note exist = null;
                if (!title.isBlank() && !createRadio.isSelected()) {
                    exist = tableNote.find(conn, notebookid, title);
                }
                if (timeCheck.isSelected()) {
                    line = reader.readLine();
                    if (line == null || title.isBlank()) {
                        continue;
                    }
                    time = DateTools.stringToDatetime(line);
                    if (time == null) {
                        updateLogs(message("InvalidData") + ": " + message("Datetime")
                                + ". " + message("Entry") + " " + count);
                        continue;
                    }
                } else {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
                }
                html = null;
                while ((line = reader.readLine()) != null && !line.isBlank()) {
                    if (html == null) {
                        html = line;
                    } else {
                        html += System.lineSeparator() + line;
                    }
                }
                if (exist != null) {
                    if (overrideRadio.isSelected()) {
                        exist.setHtml(html);
                        exist.setUpdateTime(time);
                        if (tableNote.updateData(conn, exist) != null) {
                            count++;
                        }
                    }
                } else if (tableNote.insertData(conn, new Note(notebookid, title, html, time)) != null) {
                    count++;
                }
                if (line == null) {
                    break;
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return count;
    }

    @Override
    public void donePost() {
        if (notesController != null) {
            notesController.notebooksController.loadTree();
            notesController.alertInformation(message("Imported") + ": " + totalItemsHandled);
            closeStage();
        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                FxmlControl.miao3();
            }
        }
    }

}
