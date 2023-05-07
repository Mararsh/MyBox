package mara.mybox.data;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlFileBackup;
import mara.mybox.data.FileEditInformation.Edit_Type;
import static mara.mybox.data.FindReplaceString.Operation.FindAll;
import static mara.mybox.data.FindReplaceString.Operation.ReplaceAll;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-9
 * @License Apache License Version 2.0
 */
public class FindReplaceFile extends FindReplaceString {

    protected FileEditInformation fileInfo;
    protected long position;
    protected LongRange fileRange;  // location in whole file
    protected ControlFileBackup backupController;
    protected DataFileCSV matchesData;

    public FindReplaceFile() {
    }

    @Override
    public FindReplaceString reset() {
        super.reset();
        fileRange = null;
        matchesData = null;
        return this;
    }

    public FindReplaceString findReplaceString() {
        FindReplaceString findReplaceString = new FindReplaceString()
                .setOperation(operation)
                .setInputString(inputString)
                .setFindString(findString)
                .setAnchor(anchor)
                .setUnit(unit)
                .setReplaceString(replaceString)
                .setIsRegex(isRegex)
                .setCaseInsensitive(caseInsensitive)
                .setMultiline(multiline)
                .setDotAll(dotAll)
                .setWrap(wrap);
        return findReplaceString;
    }

    public boolean shouldHandleAsString() {
        return fileInfo.pagesNumber < 2 && inputString != null && !inputString.isEmpty();
    }

    public boolean handlePage() {
        reset();
        if (operation == null || fileInfo == null
                || findString == null || findString.isEmpty()) {
            return false;
        }
        fileInfo.setFindReplace(this);
//        MyBoxLog.debug("operation:" + operation + " unit:" + unit
//                + " anchor:" + anchor + " position:" + position + " page:" + fileInfo.getCurrentPage());
        if (shouldHandleAsString()) {
            return handleString();
        }
        // try current page at first
        if (operation == Operation.FindNext || operation == Operation.ReplaceFirst
                || operation == Operation.FindPrevious) {
            FindReplaceString findReplaceString = findReplaceString().setWrap(false);
            findReplaceString.handleString();
            if (findReplaceString.getStringRange() != null) {
                stringRange = findReplaceString.getStringRange();
                lastMatch = findReplaceString.getLastMatch();
                outputString = findReplaceString.getOutputString();
                lastReplacedLength = findReplaceString.getLastReplacedLength();
                matches = findReplaceString.getMatches();
//                MyBoxLog.debug("stringRange:" + stringRange.getStart() + " " + stringRange.getEnd());
                fileRange = FindReplaceTextFile.fileRange(this);
//                MyBoxLog.debug("fileRange:" + fileRange.getStart() + " " + fileRange.getEnd());
                return true;
            }
        }
        return false;
    }

    public boolean handleFile() {
        reset();
        if (operation == null || fileInfo == null
                || findString == null || findString.isEmpty()) {
            return false;
        }
        fileInfo.setFindReplace(this);
//        MyBoxLog.debug("operation:" + operation + " unit:" + unit
//                + " anchor:" + anchor + " position:" + position + " page:" + fileInfo.getCurrentPage());
        if (shouldHandleAsString()) {
            return handleString();
        }

//        MyBoxLog.debug("findString.length()：" + findString.length());
//        MyBoxLog.debug(fileInfo.getEditType());
        if (fileInfo.getEditType() != Edit_Type.Bytes) {
//            MyBoxLog.debug("fileFindString.length()：" + fileFindString.length());
            switch (operation) {
                case Count:
                    return FindReplaceTextFile.countText(fileInfo, this);
                case FindNext:
                    return FindReplaceTextFile.findNextText(fileInfo, this);
                case FindPrevious:
                    return FindReplaceTextFile.findPreviousText(fileInfo, this);
                case ReplaceFirst:
                    return FindReplaceTextFile.replaceFirstText(fileInfo, this);
                case ReplaceAll:
                    return FindReplaceTextFile.replaceAllText(fileInfo, this);
                case FindAll:
                    return FindReplaceTextFile.findAllText(fileInfo, this);
                default:
                    break;
            }
        } else {
            switch (operation) {
                case Count:
                    return FindReplaceBytesFile.countBytes(fileInfo, this);
                case FindNext:
                    return FindReplaceBytesFile.findNextBytes(fileInfo, this);
                case FindPrevious:
                    return FindReplaceBytesFile.findPreviousBytes(fileInfo, this);
                case ReplaceFirst:
                    return FindReplaceBytesFile.replaceFirstBytes(fileInfo, this);
                case ReplaceAll:
                    return FindReplaceBytesFile.replaceAllBytes(fileInfo, this);
                case FindAll:
                    return FindReplaceBytesFile.findAllBytes(fileInfo, this);
                default:
                    break;
            }
        }
        return true;
    }

    public void backup(File file) {
        if (backupController != null && backupController.needBackup()) {
            backupController.addBackup(null, file);
        }
    }

    public boolean isMultiplePages() {
        return fileInfo != null && fileInfo.pagesNumber > 1;
    }

    public DataFileCSV initMatchesData(File sourceFile) {
        String dname = sourceFile.getName() + "_" + message("Find");
        matchesData = new DataFileCSV();
        File matchesFile = matchesData.tmpFile(dname, null, "csv");
        matchesData.setFile(matchesFile)
                .setDataName(dname)
                .setCharset(Charset.forName("UTF-8"))
                .setDelimiter(",").setHasHeader(true)
                .setColsNumber(3)
                .setComments(message("SourceFile") + ": " + sourceFile + "\n"
                        + message("Find") + ": " + findString);
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message("Start"), ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(message("End"), ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(message("String") + "1", ColumnDefinition.ColumnType.String));
        matchesData.setColumns(columns);
        return matchesData;
    }

    /*
        get/set
     */
    public FileEditInformation getFileInfo() {
        return fileInfo;
    }

    public FindReplaceFile setFileInfo(FileEditInformation fileInfo) {
        this.fileInfo = fileInfo;
        return this;
    }

    public LongRange getFileRange() {
        return fileRange;
    }

    public FindReplaceFile setFileRange(LongRange lastFound) {
        this.fileRange = lastFound;
        return this;
    }

    public ControlFileBackup getBackupController() {
        return backupController;
    }

    public FindReplaceFile setBackupController(ControlFileBackup backupController) {
        this.backupController = backupController;
        return this;
    }

    public long getPosition() {
        return position;
    }

    public FindReplaceFile setPosition(long position) {
        this.position = position;
        return this;
    }

    public DataFileCSV getMatchesData() {
        return matchesData;
    }

    public FindReplaceFile setMatchesData(DataFileCSV matchesData) {
        this.matchesData = matchesData;
        return this;
    }

}
