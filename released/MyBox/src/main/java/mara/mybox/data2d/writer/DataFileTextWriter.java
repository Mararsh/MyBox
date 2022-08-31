package mara.mybox.data2d.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import mara.mybox.data2d.DataFileText;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileTextWriter extends Data2DWriter {

    protected DataFileText sourceText;
    protected BufferedReader textReader;
    protected BufferedWriter textWriter;
    protected String delimiter;

    public DataFileTextWriter(DataFileText data) {
        this.sourceText = data;
        init(data);
        delimiter = data.getDelimiter();
    }

    @Override
    public void scanData() {
        if (!FileTools.hasData(sourceFile)) {
            return;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File validFile = FileTools.removeBOM(sourceFile);
        rowIndex = 0;
        count = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(validFile, sourceText.getCharset()));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceText.getCharset(), false))) {
            textReader = reader;
            textWriter = writer;
            failed = !handleRows();
            textWriter = null;
            textReader = null;
            writer.close();
            reader.close();
            if (failed) {
                FileDeleteTools.delete(tmpFile);
            } else {
                failed = !FileTools.rename(tmpFile, sourceFile, false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            failed = true;
        }
        if (failed) {
            writerStopped = true;
        }
    }

    public boolean handleRows() {
        if (textReader == null) {
            return false;
        }
        try {
            List<String> names = data2D.columnNames();
            if (data2D.isHasHeader() && names != null) {
                sourceText.readValidLine(textReader);
                TextFileTools.writeLine(textWriter, names, delimiter);
            }
            if (isClearData()) {
                count = data2D.getDataSize();
                return true;
            }
            String line;
            while ((line = textReader.readLine()) != null && !writerStopped()) {
                sourceRow = sourceText.parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                ++rowIndex;
                handleRow();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeRow() {
        try {
            if (writerStopped() || targetRow == null) {
                return;
            }
            TextFileTools.writeLine(textWriter, targetRow, delimiter);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

}
