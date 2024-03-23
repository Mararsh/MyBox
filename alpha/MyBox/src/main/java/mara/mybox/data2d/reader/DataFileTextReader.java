package mara.mybox.data2d.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import mara.mybox.data2d.DataFileText;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileTextReader extends Data2DReader {

    protected DataFileText readerText;
    protected BufferedReader textReader;

    public DataFileTextReader(DataFileText data) {
        this.readerText = data;
        sourceData = data;
    }

    @Override
    public void scanData() {
        File validFile = FileTools.removeBOM(task(), sourceFile);
        if (validFile == null || isStopped()) {
            return;
        }
        readerText.checkForLoad();
        try (BufferedReader reader = new BufferedReader(new FileReader(validFile, readerText.getCharset()))) {
            textReader = reader;
            operate.handleData();
            textReader = null;
            reader.close();
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    protected List<String> readValidLine() {
        return readerText.readValidLine(textReader);
    }

    protected List<String> parseFileLine(String line) {
        return readerText.parseFileLine(line);
    }

    @Override
    public void readColumnNames() {
        try {
            String line;
            while ((line = textReader.readLine()) != null && !isStopped()) {
                sourceRow = parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                handleHeader();
                return;
            }
        } catch (Exception e) {
            handleError(e.toString());
        }
    }

    @Override
    public void readTotal() {
        try {
            String line;
            rowIndex = 0;
            skipHeader();
            while ((line = textReader.readLine()) != null) {
                if (isStopped()) {
                    rowIndex = 0;
                    return;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    ++rowIndex;
                }
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    public void skipHeader() {
        if (readerHasHeader) {
            readValidLine();
        }
    }

    // rowIndex is 1-base while pageStartIndex and pageEndIndex are 0-based
    @Override
    public void readPage() {
        if (textReader == null) {
            return;
        }
        try {
            skipHeader();
            rowIndex = 0;
            String line;
            while ((line = textReader.readLine()) != null && !isStopped()) {
                sourceRow = parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                if (rowIndex++ < pageStartIndex) {
                    continue;
                }
                if (rowIndex > pageEndIndex) {
                    stop();
                    break;
                }
                handlePageRow();
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readRows() {
        try {
            if (textReader == null) {
                return;
            }
            skipHeader();
            rowIndex = 0;
            String line;
            while ((line = textReader.readLine()) != null && !isStopped()) {
                sourceRow = parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                ++rowIndex;
                handleRow();
            }
        } catch (Exception e) {
            handleError(e.toString());
            setFailed();
        }
    }

}
