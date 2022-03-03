package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.List;
import static mara.mybox.value.Languages.message;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @Author Mara
 * @CreateDate 2019-11-14
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FileUnarchive {

    protected File sourceFile, targetPath;
    protected String archiver, charset, error;
    protected List<String> selected;
    protected int archiveSuccess, archiveFail;
    protected boolean charsetIncorrect, verbose;
    protected long totalFiles, totalSize;
    protected BaseTaskController taskController;
    protected ArchiveStreamFactory aFactory;

    public FileUnarchive() {
        totalFiles = totalSize = archiveFail = archiveSuccess = 0;
        charsetIncorrect = false;
        error = null;
        selected = null;
    }

    public FileUnarchive create() {
        return new FileUnarchive();
    }

    public boolean start() {
        try {
            if (taskController == null || sourceFile == null || archiver == null || charset == null) {
                return false;
            }
            // archiver should have been determinied at this moment
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                unarchive7z();
            } else if (archiver.equalsIgnoreCase(ArchiveStreamFactory.ZIP)) {
                unarchiveZip();
            } else {
                unarchive();
            }
            return true;
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return false;
        }
    }

    protected void unarchive() {
        if (aFactory == null) {
            aFactory = new ArchiveStreamFactory();
        }
        try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(sourceFile))) {
            if (archiver != null && aFactory.getInputStreamArchiveNames().contains(archiver)) {
                try ( ArchiveInputStream in = aFactory.createArchiveInputStream(archiver, fileIn, charset)) {
                    unarchive(in);
                } catch (Exception e) {
                    unarchive(fileIn);
                }
            } else {
                unarchive(fileIn);
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
    }

    public void unarchive(BufferedInputStream fileIn) {
        try {
            archiver = ArchiveStreamFactory.detect(fileIn);
            if (archiver == null) {
                return;
            }
            try ( ArchiveInputStream in = aFactory.createArchiveInputStream(archiver, fileIn, charset)) {
                unarchive(in);
            } catch (Exception ex) {
                taskController.updateLogs(ex.toString());
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
    }

    protected void unarchive(ArchiveInputStream in) {
        try {
            if (archiver == null || !aFactory.getInputStreamArchiveNames().contains(archiver)) {
                return;
            }
            ArchiveEntry entry;
            File tfile;
            while ((entry = in.getNextEntry()) != null) {
                if (selected != null && !selected.contains(entry.getName())) {
                    continue;
                }
                if (verbose) {
                    taskController.updateLogs(message("Handling...") + ":   " + entry.getName());
                }
                if (!in.canReadEntryData(entry)) {
                    archiveFail++;
                    taskController.updateLogs(message("CanNotReadEntryData" + ":" + entry.getName()));
                    continue;
                }
                try {
                    tfile = new File(targetPath + File.separator + entry.getName());
                    tfile = taskController.makeTargetFile(tfile, tfile.getParentFile());
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (entry.isDirectory()) {
                    archiveSuccess++;
                    continue;
                }
                try ( OutputStream o = Files.newOutputStream(tfile.toPath())) {
                    IOUtils.copy(in, o);
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (verbose) {
                    taskController.targetFileGenerated(tfile, false);
                }
                archiveSuccess++;
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void unarchive7z() {
        try ( SevenZFile sevenZFile = new SevenZFile(sourceFile)) {
            SevenZArchiveEntry entry;
            File tfile;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (selected != null && !selected.contains(entry.getName())) {
                    continue;
                }
                if (verbose) {
                    taskController.updateLogs(message("Handling...") + ":   " + entry.getName());
                }
                try {
                    tfile = new File(targetPath + File.separator + entry.getName());
                    tfile = taskController.makeTargetFile(tfile, tfile.getParentFile());
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (entry.isDirectory()) {
                    archiveSuccess++;
                    continue;
                }
                try ( FileOutputStream out = new FileOutputStream(tfile)) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (verbose) {
                    taskController.targetFileGenerated(tfile, false);
                }
                archiveSuccess++;
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void unarchiveZip() {
        try ( ZipFile zipFile = new ZipFile(sourceFile, charset)) {
            Enumeration<ZipArchiveEntry> zEntries = zipFile.getEntries();
            File tfile;
            while (zEntries.hasMoreElements()) {
                ZipArchiveEntry entry = zEntries.nextElement();
                if (selected != null && !selected.contains(entry.getName())) {
                    continue;
                }
                if (verbose) {
                    taskController.updateLogs(message("Handling...") + ":   " + entry.getName());
                }
                try {
                    tfile = new File(targetPath + File.separator + entry.getName());
                    tfile = taskController.makeTargetFile(tfile, tfile.getParentFile());
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (entry.isDirectory()) {
                    archiveSuccess++;
                    continue;
                }
                try ( FileOutputStream out = new FileOutputStream(tfile);
                         InputStream in = zipFile.getInputStream(entry)) {
                    if (in != null) {
                        IOUtils.copy(in, out);
                        if (verbose) {
                            taskController.targetFileGenerated(tfile, false);
                        }
                    }
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                archiveSuccess++;
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void recordError(String error) {
        taskController.updateLogs(error);
        archiveFail++;
        if (error.contains("java.nio.charset.MalformedInputException")
                || error.contains("Illegal char")) {
            charsetIncorrect = true;
        }
    }

    /*
        get/set
     */
    public File getSourceFile() {
        return sourceFile;
    }

    public FileUnarchive setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
        return this;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public FileUnarchive setTargetPath(File targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public String getArchiver() {
        return archiver;
    }

    public FileUnarchive setArchiver(String archiver) {
        this.archiver = archiver;
        return this;
    }

    public String getError() {
        return error;
    }

    public FileUnarchive setError(String error) {
        this.error = error;
        return this;
    }

    public List<String> getSelected() {
        return selected;
    }

    public FileUnarchive setSelected(List<String> selected) {
        this.selected = selected;
        return this;
    }

    public int getArchiveSuccess() {
        return archiveSuccess;
    }

    public FileUnarchive setArchiveSuccess(int archiveSuccess) {
        this.archiveSuccess = archiveSuccess;
        return this;
    }

    public int getArchiveFail() {
        return archiveFail;
    }

    public FileUnarchive setArchiveFail(int archiveFail) {
        this.archiveFail = archiveFail;
        return this;
    }

    public boolean isCharsetIncorrect() {
        return charsetIncorrect;
    }

    public FileUnarchive setCharsetIncorrect(boolean charsetIncorrect) {
        this.charsetIncorrect = charsetIncorrect;
        return this;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public FileUnarchive setTotalFiles(long totalFiles) {
        this.totalFiles = totalFiles;
        return this;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public FileUnarchive setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public BaseTaskController getTaskController() {
        return taskController;
    }

    public FileUnarchive setTaskController(BaseTaskController taskController) {
        this.taskController = taskController;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public FileUnarchive setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public FileUnarchive setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public ArchiveStreamFactory getaFactory() {
        return aFactory;
    }

    public FileUnarchive setaFactory(ArchiveStreamFactory aFactory) {
        this.aFactory = aFactory;
        return this;
    }

}
