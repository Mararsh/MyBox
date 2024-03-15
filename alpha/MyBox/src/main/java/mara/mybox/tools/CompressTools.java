/*
 * Apache License Version 2.0
 */
package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.data.FileInformation.FileType;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 *
 * @author mara
 */
public class CompressTools {

    public static Map<String, String> CompressExtension;
    public static Map<String, String> ExtensionCompress;
    public static Map<String, String> ArchiveExtension;
    public static Map<String, String> ExtensionArchive;

    public static Map<String, String> compressExtension() {
        if (CompressExtension != null) {
            return CompressExtension;
        }
        CompressExtension = new HashMap<>();
        CompressExtension.put("gz", "gz");
        CompressExtension.put("bzip2", "bz2");
        CompressExtension.put("pack200", "pack");
        CompressExtension.put("xz", "xz");
        CompressExtension.put("lzma", "lzma");
        CompressExtension.put("snappy-framed", "sz");
        CompressExtension.put("snappy-raw", "sz");
        CompressExtension.put("z", "z");
        CompressExtension.put("deflate", "deflate");
        CompressExtension.put("deflate64", "deflate");
        CompressExtension.put("lz4-block", "lz4");
        CompressExtension.put("lz4-framed", "lz4");
//        CompressExtension.put("zstandard", "zstd");
//        CompressExtension.put("brotli", "br");
        return CompressExtension;
    }

    public static Map<String, String> extensionCompress() {
        if (ExtensionCompress != null) {
            return ExtensionCompress;
        }
        Map<String, String> formats = compressExtension();
        ExtensionCompress = new HashMap<>();
        for (String format : formats.keySet()) {
            ExtensionCompress.put(formats.get(format), format);
        }
        return ExtensionCompress;
    }

    public static Map<String, String> archiveExtension() {
        if (ArchiveExtension != null) {
            return ArchiveExtension;
        }
        ArchiveExtension = new HashMap<>();
        ArchiveExtension.put("zip", "zip");
        ArchiveExtension.put("7z", "7z");
        ArchiveExtension.put("jar", "jar");
        ArchiveExtension.put("tar", "tar");
        ArchiveExtension.put("ar", "ar");
        ArchiveExtension.put("arj", "arj");
        ArchiveExtension.put("cpio", "cpio");
        ArchiveExtension.put("dump", "dump");
        return ArchiveExtension;
    }

    public static Map<String, String> extensionArchive() {
        if (ExtensionArchive != null) {
            return ExtensionArchive;
        }
        Map<String, String> formats = archiveExtension();
        ExtensionArchive = new HashMap<>();
        for (String format : formats.keySet()) {
            ExtensionArchive.put(formats.get(format), format);
        }
        return ExtensionArchive;
    }

    public static String extensionByCompressor(String compressor) {
        if (compressor == null) {
            return null;
        }
        return compressExtension().get(compressor.toLowerCase());
    }

    public static String compressorByExtension(String ext) {
        if (ext == null) {
            return null;
        }
        return extensionCompress().get(ext.toLowerCase());
    }

    public static Set<String> compressFormats() {
        return compressExtension().keySet();
    }

    public static Set<String> archiveFormats() {
        return archiveExtension().keySet();
    }

    public static String decompressedName(BaseTaskController taskController, File srcFile) {
        return decompressedName(taskController, srcFile, detectCompressor(taskController, srcFile));
    }

    public static String decompressedName(BaseTaskController taskController, File srcFile, String compressor) {
        try {
            if (srcFile == null || compressor == null) {
                return null;
            }
            String ext = CompressTools.extensionByCompressor(compressor);
            if (ext == null) {
                return null;
            }
            String fname = srcFile.getName();
            if (fname.toLowerCase().endsWith("." + ext)) {
                return fname.substring(0, fname.length() - ext.length() - 1);
            } else {
                return fname + "." + ext;
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return null;
        }
    }

    public static String detectCompressor(BaseTaskController taskController, File srcFile) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileNameTools.ext(srcFile.getName()).toLowerCase();
        return detectCompressor(taskController, srcFile, compressorByExtension(ext));
    }

    public static String detectCompressor(BaseTaskController taskController, File srcFile, String extIn) {
        if (srcFile == null || "none".equals(extIn)) {
            return null;
        }
        String compressor = null;
        String ext = (extIn != null) ? extIn.toLowerCase() : null;
        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
            CompressorStreamFactory cFactory = new CompressorStreamFactory();
            if (ext != null && cFactory.getInputStreamCompressorNames().contains(ext)) {
                try (CompressorInputStream in = cFactory.createCompressorInputStream(ext, fileIn)) {
                    compressor = ext;
                } catch (Exception e) {
                    compressor = detectCompressor(taskController, fileIn, ext);
                }
            } else {
                compressor = detectCompressor(taskController, fileIn, ext);
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
        return compressor;
    }

    public static String detectCompressor(BaseTaskController taskController, BufferedInputStream fileIn, String extIn) {
        String compressor = null;
        try {
            compressor = CompressorStreamFactory.detect(fileIn);
        } catch (Exception ex) {
            if ("lz4".equals(extIn)) {
                try (CompressorInputStream in = new BlockLZ4CompressorInputStream(fileIn)) {
                    compressor = "lz4-block";
                } catch (Exception e) {
                    taskController.updateLogs(message("NotCompressFormat"));
                }
            } else {
                taskController.updateLogs(message("NotCompressFormat"));
            }
        }
        return compressor;
    }

    public static Map<String, Object> decompress(BaseTaskController taskController, File srcFile, File targetFile) {
        String ext = FileNameTools.ext(srcFile.getName()).toLowerCase();
        return CompressTools.decompress(taskController, srcFile, CompressTools.compressorByExtension(ext), targetFile);
    }

    public static Map<String, Object> decompress(BaseTaskController taskController, File srcFile, String extIn, File targetFile) {
        Map<String, Object> decompress = null;
        try {
            File decompressedFile = null;
            String compressor = null;
            boolean detect = false;
            String ext = (extIn != null) ? extIn.toLowerCase() : null;
            try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
                CompressorStreamFactory cFactory = new CompressorStreamFactory();
                if (ext != null && cFactory.getInputStreamCompressorNames().contains(ext)) {
                    try (CompressorInputStream in = cFactory.createCompressorInputStream(ext, fileIn)) {
                        decompressedFile = decompress(taskController, in, targetFile);
                        if (decompressedFile != null) {
                            compressor = ext;
                            detect = false;
                        }
                    } catch (Exception e) {
//                        taskController.updateLogs(e.toString());
                        try {
                            String defectValue = CompressorStreamFactory.detect(fileIn);
                            try (CompressorInputStream in = cFactory.createCompressorInputStream(defectValue, fileIn)) {
                                decompressedFile = decompress(taskController, in, targetFile);
                                if (decompressedFile != null) {
                                    compressor = defectValue;
                                    detect = true;
                                }
                            } catch (Exception ex) {
//                                taskController.updateLogs(ex.toString());
                            }
                        } catch (Exception ex) {
//                            taskController.updateLogs(ex.toString());
                        }
                    }
                } else {
                    try {
                        String defectValue = CompressorStreamFactory.detect(fileIn);
                        try (CompressorInputStream in = cFactory.createCompressorInputStream(defectValue, fileIn)) {
                            decompressedFile = decompress(taskController, in, targetFile);
                            if (decompressedFile != null) {
                                compressor = defectValue;
                                detect = true;
                            }
                        } catch (Exception ex) {
//                            taskController.updateLogs(ex.toString());
                        }
                    } catch (Exception ex) {
//                        taskController.updateLogs(ex.toString());
                    }
                }
            }
            if (compressor != null && decompressedFile != null && decompressedFile.exists()) {
                decompress = new HashMap<>();
                decompress.put("compressor", compressor);
                decompress.put("decompressedFile", decompressedFile);
                decompress.put("detect", detect);
            } else {
                taskController.updateLogs(message("NotCompressFormat"));
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
        return decompress;
    }

    public static File decompress(BaseTaskController taskController, CompressorInputStream compressorInputStream, File targetFile) {
        if (compressorInputStream == null) {
            return null;
        }
        File file = FileTmpTools.getTempFile();
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(compressorInputStream, out);
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
            return null;
        }
        if (targetFile == null) {
            return file;
        } else if (FileTools.override(file, targetFile)) {
            return targetFile;
        } else {
            return null;
        }

    }

    public static String detectArchiver(BaseTaskController taskController, File srcFile) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileNameTools.ext(srcFile.getName()).toLowerCase();
        return detectArchiver(taskController, srcFile, ext);
    }

    public static String detectArchiver(BaseTaskController taskController, File srcFile, String extIn) {
        if (srcFile == null || "none".equals(extIn)) {
            return null;
        }
        String archiver = null;
        String ext = (extIn != null) ? extIn.toLowerCase() : null;
        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
            ArchiveStreamFactory aFactory = new ArchiveStreamFactory();
            if (ext != null && aFactory.getInputStreamArchiveNames().contains(ext)) {
                try (ArchiveInputStream in = aFactory.createArchiveInputStream(ext, fileIn)) {
                    archiver = ext;
                } catch (Exception e) {
                    try {
                        archiver = ArchiveStreamFactory.detect(fileIn);
                    } catch (Exception ex) {
                        taskController.updateLogs(ex.toString());
                    }
                }
            } else {
                try {
                    archiver = ArchiveStreamFactory.detect(fileIn);
                } catch (Exception ex) {
                    taskController.updateLogs(ex.toString());
                }
            }
        } catch (Exception e) {
            taskController.updateLogs(e.toString());
        }
        return archiver;
    }

    public static Map<String, Object> readEntries(BaseTaskController taskController, File srcFile, String encoding) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileNameTools.ext(srcFile.getName()).toLowerCase();
        return readEntries(taskController, srcFile, ext, encoding);
    }

    public static Map<String, Object> readEntries(BaseTaskController taskController, File srcFile, String extIn, String encodingIn) {
        Map<String, Object> unarchive = new HashMap<>();
        try {
            String encoding = (encodingIn != null) ? encodingIn
                    : UserConfig.getString("FilesUnarchiveEncoding", Charset.defaultCharset().name());
            if (srcFile == null || "none".equals(extIn) || encoding == null) {
                return null;
            }
            String ext = extIn;
            if (ArchiveStreamFactory.SEVEN_Z.equals(ext)) {
                return readEntries7z(taskController, srcFile, encoding);
            } else if (ArchiveStreamFactory.ZIP.equals(ext)) {
                return readEntriesZip(taskController, srcFile, encoding);
            }
            try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile));) {
                if (ext == null) {
                    ext = ArchiveStreamFactory.detect(fileIn);
                }
                if (ext != null && !ArchiveStreamFactory.SEVEN_Z.equals(ext)
                        && !ArchiveStreamFactory.ZIP.equals(ext)) {
                    ArchiveStreamFactory aFactory = new ArchiveStreamFactory();
                    if (aFactory.getInputStreamArchiveNames().contains(ext)) {
                        try (ArchiveInputStream in = aFactory.createArchiveInputStream(ext, fileIn, encoding)) {
                            List<FileNode> entires = readEntries(taskController, in);
                            if (entires != null && !entires.isEmpty()) {
                                unarchive.put("archiver", ext);
                                unarchive.put("entries", entires);
                            }
                        } catch (Exception e) {
                            unarchive.put("error", e.toString());
                            taskController.updateLogs(e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                unarchive.put("error", e.toString());
                taskController.updateLogs(e.toString());
            }
            if (ext != null && unarchive.get("entries") == null) {
                if (ArchiveStreamFactory.SEVEN_Z.equals(ext)) {
                    return readEntries7z(taskController, srcFile, encoding);
                } else if (ArchiveStreamFactory.ZIP.equals(ext)) {
                    return readEntriesZip(taskController, srcFile, encoding);
                }
            }
        } catch (Exception e) {
            unarchive.put("error", e.toString());
            taskController.updateLogs(e.toString());
        }
        return unarchive;
    }

    public static List<FileNode> readEntries(BaseTaskController taskController, ArchiveInputStream archiveInputStream) {
        List<FileNode> entries = new ArrayList();
        try {
            if (archiveInputStream == null) {
                return null;
            }
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (!archiveInputStream.canReadEntryData(entry)) {
                    MyBoxLog.debug("Can not Read entry Data:" + entry.getName());
                    continue;
                }
                try {
                    FileNode file = new FileNode();
                    file.setData(entry.getName());
                    file.setModifyTime(entry.getLastModifiedDate().getTime());
                    file.setFileSize(entry.getSize());
                    file.setFileType(entry.isDirectory() ? FileType.Directory : FileType.File);
                    entries.add(file);
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return entries;
    }

    public static Map<String, Object> readEntriesZip(BaseTaskController taskController, File srcFile, String encoding) {
        Map<String, Object> unarchive = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(srcFile, encoding)) {
            List<FileNode> fileEntries = new ArrayList();
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                FileNode file = new FileNode();
                file.setData(entry.getName());
                file.setModifyTime(entry.getLastModifiedDate().getTime());
                file.setFileSize(entry.getSize());
                file.setFileType(entry.isDirectory() ? FileType.Directory : FileType.File);
                fileEntries.add(file);
            }
            unarchive.put("archiver", ArchiveStreamFactory.ZIP);
            unarchive.put("entries", fileEntries);
        } catch (Exception e) {
            unarchive.put("error", e.toString());
        }
        return unarchive;
    }

    public static Map<String, Object> readEntries7z(BaseTaskController taskController, File srcFile, String encoding) {
        Map<String, Object> unarchive = new HashMap<>();
        try (SevenZFile sevenZFile = new SevenZFile(srcFile)) {
            SevenZArchiveEntry entry;
            List<FileNode> entries = new ArrayList();
            while ((entry = sevenZFile.getNextEntry()) != null) {
                FileNode file = new FileNode();
                file.setData(entry.getName());
                file.setModifyTime(entry.getLastModifiedDate().getTime());
                file.setFileSize(entry.getSize());
                file.setFileType(entry.isDirectory() ? FileType.Directory : FileType.File);
                entries.add(file);
            }
            unarchive.put("archiver", ArchiveStreamFactory.SEVEN_Z);
            unarchive.put("entries", entries);
        } catch (Exception e) {
            unarchive.put("error", e.toString());
        }
        return unarchive;
    }

}
