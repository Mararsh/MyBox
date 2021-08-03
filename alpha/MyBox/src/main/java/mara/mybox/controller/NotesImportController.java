package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.fxml.NodeTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Languages;

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
    protected RadioButton overrideRadio, createRadio, skipRadio;

    public NotesImportController() {
        baseTitle = Languages.message("ImportNotesFiles");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void importExamples(NotesController notesController) {
        this.notesController = notesController;
        tableNotebook = notesController.tableNotebook;
        String lang = Languages.isChinese() ? "zh" : "en";
        File file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/db/Notes_Examples_" + lang + ".txt",
                "data", "Notes_Examples_" + lang + ".txt");
        isSettingValues = true;
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
                return Languages.message("Imported") + ": " + count;
            } else {
                return Languages.message("Failed");
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public long importNotes(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 BufferedReader reader = new BufferedReader(new FileReader(file, TextFileTools.charset(file)))) {
            conn.setAutoCommit(false);
            String line;
            while ((line = reader.readLine()) != null && line.isBlank()) {
            }
            if (line.equals(AppValues.MyBoxSeparator)) {
                return importNotes(conn, reader);
            } else {
                return importNotesOfOldVersions(conn, reader, line);
            }
        } catch (Exception e) {
            updateLogs(e.toString());
            return -1;
        }
    }

    public long importNotes(Connection conn, BufferedReader reader) {
        if (conn == null || reader == null) {
            return -1;
        }
        long count = 0;
        try {
            conn.setAutoCommit(false);
            String line, title, html;
            Date time;
            long baseTime = new Date().getTime();
            Notebook notebook;
            Map<String, Notebook> owners = new HashMap<>();
            while ((line = reader.readLine()) != null && !line.isBlank()) {
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
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                title = line;
                Note exist = null;
                if (!createRadio.isSelected()) {
                    exist = tableNote.find(conn, notebookid, title);
                }
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                time = null;
                html = null;
                if (line != null) {
                    if (line.startsWith(Note.NoteTimePrefix)) {
                        time = DateTools.stringToDatetime(line.substring(Note.NoteTimePrefix.length()));
                    }
                    if (time == null) {
                        html = line;
                    }
                    while ((line = reader.readLine()) != null && !line.equals(AppValues.MyBoxSeparator)) {
                        if (html == null) {
                            html = line;
                        } else {
                            html += System.lineSeparator() + line;
                        }
                    }
                }
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
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

    public long importNotesOfOldVersions(Connection conn, BufferedReader reader, String firstLine) {
        if (conn == null || reader == null) {
            return -1;
        }
        long count = 0;
        try {
            conn.setAutoCommit(false);
            String line = firstLine, title, html;
            Date time;
            long baseTime = new Date().getTime();
            Notebook notebook;
            Map<String, Notebook> owners = new HashMap<>();
            while (line != null) {
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
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                title = line;
                Note exist = null;
                if (!createRadio.isSelected()) {
                    exist = tableNote.find(conn, notebookid, title);
                }
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                time = null;
                html = null;
                if (line != null) {
                    time = DateTools.stringToDatetime(line);
                    if (time == null) {
                        html = line;
                    }
                    while ((line = reader.readLine()) != null && !line.isBlank()) {
                        if (html == null) {
                            html = line;
                        } else {
                            html += System.lineSeparator() + line;
                        }
                    }
                }
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
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
                while ((line = reader.readLine()) != null && line.isBlank()) {
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
            notesController.alertInformation(Languages.message("Imported") + ": " + totalItemsHandled);
            closeStage();
        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                SoundTools.miao3();
            }
        }
    }

}
