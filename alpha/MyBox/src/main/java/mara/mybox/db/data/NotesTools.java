package mara.mybox.db.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.db.data.Notebook.NotebookNameSeparater;
import mara.mybox.tools.TmpFileTools;

/**
 * @Author Mara
 * @CreateDate 2021-3-2
 * @License Apache License Version 2.0
 */
public class NotesTools {

    public static boolean exportNotes(TableNotebook tableNotebook, TableNote tableNote, Notebook book,
            String baseName, File file, boolean includeTime) {
        if (tableNotebook == null || tableNote == null || book == null || file == null) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( Connection conn = DerbyBase.getConnection();
                 FileWriter writer = new FileWriter(tmpFile, Charset.forName("utf-8"))) {
            exportNotes(conn, writer, tableNotebook, tableNote, book, baseName, includeTime);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
        return FileTools.rename(tmpFile, file);
    }

    public static void exportNotes(Connection conn, FileWriter writer,
            TableNotebook tableNotebook, TableNote tableNote,
            Notebook book, String baseName, boolean includeTime) {
        if (conn == null || writer == null
                || tableNotebook == null || tableNote == null || book == null) {
            return;
        }
        try {
            String bookName = book.getName();
            if (baseName != null) {
                bookName = baseName + NotebookNameSeparater + bookName;
            }
            List<Note> notes = tableNote.notes(conn, book.getNbid());
            if (notes != null) {
                for (Note note : notes) {
                    writer.write(bookName + "\n");
                    writer.write(note.getTitle() + "\n");
                    if (includeTime) {
                        writer.write(DateTools.datetimeToString(note.getUpdateTime()) + "\n");
                    }
                    writer.write(StringTools.discardBlankLines(note.getHtml()) + "\n\n");
                }
            }
            List<Notebook> children = tableNotebook.children(conn, book.getNbid());
            if (children != null) {
                for (Notebook child : children) {
                    exportNotes(conn, writer, tableNotebook, tableNote, child, bookName, includeTime);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
