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
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;
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

    public static String detectCompressor(File srcFile) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileTools.getFileSuffix(srcFile).toLowerCase();
        return detectCompressor(srcFile, compressorByExtension(ext));
    }

    public static String detectCompressor(File srcFile, String name) {
        if (srcFile == null || "none".equals(name)) {
            return null;
        }
        String compressor = null;
        String namein = (name != null) ? name.toLowerCase() : null;
        try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
            CompressorStreamFactory cFactory = new CompressorStreamFactory();
            if (namein != null && cFactory.getInputStreamCompressorNames().contains(namein)) {
                try ( CompressorInputStream in = cFactory.createCompressorInputStream(namein, fileIn)) {
                    compressor = namein;
                } catch (Exception e) {
                    compressor = detectCompressor(fileIn, namein);
                }
            } else {
                compressor = detectCompressor(fileIn, namein);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, srcFile.getAbsolutePath());
        }
        return compressor;
    }

    public static String detectCompressor(BufferedInputStream fileIn, String name) {
        String compressor = null;
        try {
            compressor = CompressorStreamFactory.detect(fileIn);
        } catch (Exception ex) {
            if ("lz4".equals(name)) {
                try ( CompressorInputStream in = new BlockLZ4CompressorInputStream(fileIn)) {
                    compressor = "lz4-block";
                } catch (Exception e) {
                    MyBoxLog.debug(e, name);
                }
            }
        }
        return compressor;
    }

    public static Map<String, Object> decompress(File srcFile, File targetFile) {
        String ext = FileTools.getFileSuffix(srcFile).toLowerCase();
        return CompressTools.decompress(srcFile, CompressTools.compressorByExtension(ext), targetFile);
    }

    public static Map<String, Object> decompress(File srcFile, String nameIn, File targetFile) {
        try {
            File decompressedFile = null;
            String compressor = null;
            boolean detect = false;
            String name = (nameIn != null) ? nameIn.toLowerCase() : null;
            try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
                CompressorStreamFactory cFactory = new CompressorStreamFactory();
                if (name != null && cFactory.getInputStreamCompressorNames().contains(name)) {
                    try ( CompressorInputStream in = cFactory.createCompressorInputStream(name, fileIn)) {
                        decompressedFile = decompress(in, targetFile);
                        if (decompressedFile != null) {
                            compressor = name;
                            detect = false;
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        try {
                            String defectValue = CompressorStreamFactory.detect(fileIn);
                            try ( CompressorInputStream in = cFactory.createCompressorInputStream(defectValue, fileIn)) {
                                decompressedFile = decompress(in, targetFile);
                                if (decompressedFile != null) {
                                    compressor = defectValue;
                                    detect = true;
                                }
                            } catch (Exception ex) {
                                MyBoxLog.debug(ex.toString());
                            }
                        } catch (Exception ex) {
                            MyBoxLog.debug(ex.toString());
                        }
                    }
                } else {
                    try {
                        String defectValue = CompressorStreamFactory.detect(fileIn);
                        try ( CompressorInputStream in = cFactory.createCompressorInputStream(defectValue, fileIn)) {
                            decompressedFile = decompress(in, targetFile);
                            if (decompressedFile != null) {
                                compressor = defectValue;
                                detect = true;
                            }
                        } catch (Exception ex) {
                            MyBoxLog.debug(ex.toString());
                        }
                    } catch (Exception ex) {
                        MyBoxLog.debug(ex.toString());
                    }
                }
            }
            if (compressor != null && decompressedFile != null) {
                Map<String, Object> decompress = new HashMap<>();
                decompress.put("compressor", compressor);
                decompress.put("decompressedFile", decompressedFile);
                decompress.put("detect", detect);
                return decompress;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static File decompress(CompressorInputStream compressorInputStream, File targetFile) {
        try {
            if (compressorInputStream == null) {
                return null;
            }
            File file = (targetFile == null) ? FileTools.getTempFile() : targetFile;
            FileTools.delete(file);
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                final byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = compressorInputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String detectArchiver(File srcFile) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileTools.getFileSuffix(srcFile).toLowerCase();
        return detectArchiver(srcFile, ext);
    }

    public static String detectArchiver(File srcFile, String nameIn) {
        if (srcFile == null || "none".equals(nameIn)) {
            return null;
        }
        String archiver = null;
        String name = (nameIn != null) ? nameIn.toLowerCase() : null;
        try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
            ArchiveStreamFactory aFactory = new ArchiveStreamFactory();
            if (name != null && aFactory.getInputStreamArchiveNames().contains(name)) {
                try ( ArchiveInputStream in = aFactory.createArchiveInputStream(name, fileIn)) {
                    archiver = name;
                } catch (Exception e) {
                    try {
                        archiver = ArchiveStreamFactory.detect(fileIn);
                    } catch (Exception ex) {
                        MyBoxLog.debug(ex, nameIn);
                    }
                }
            } else {
                try {
                    archiver = ArchiveStreamFactory.detect(fileIn);
                } catch (Exception ex) {
                    MyBoxLog.debug(ex, nameIn);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, nameIn);
        }
        return archiver;
    }

    public static Map<String, Object> readEntries(File srcFile, String encoding) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileTools.getFileSuffix(srcFile).toLowerCase();
        return readEntries(srcFile, ext, encoding);
    }

    public static Map<String, Object> readEntries(File srcFile, String nameIn, String encodingIn) {
        try {
            if (srcFile == null || "none".equals(nameIn)) {
                return null;
            }
            Map<String, Object> unarchive = null;
            String name = (nameIn != null) ? nameIn.toLowerCase() : null;
            if (ArchiveStreamFactory.SEVEN_Z.equals(name)) {
                return readEntries7z(srcFile);
            } else if (ArchiveStreamFactory.ZIP.equals(name)) {
                return readEntriesZip(srcFile);
            }
            String encoding = (encodingIn != null) ? encodingIn
                    : AppVariables.getUserConfigValue("FilesUnarchiveEncoding", Charset.defaultCharset().name());
            try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
                ArchiveStreamFactory aFactory = new ArchiveStreamFactory();
                if (name != null && aFactory.getInputStreamArchiveNames().contains(name)) {
                    try ( ArchiveInputStream in = aFactory.createArchiveInputStream(name, fileIn, encoding)) {
                        List<FileInformation> entires = readEntries(in);
                        String archiver = name;
                        if (entires != null && !entires.isEmpty()) {
                            unarchive = new HashMap<>();
                            unarchive.put("archiver", archiver);
                            unarchive.put("entries", entires);
                        }
                    } catch (Exception e) {
                        unarchive = readEntries(srcFile, aFactory, fileIn, encoding);
                    }
                } else {
                    unarchive = readEntries(srcFile, aFactory, fileIn, encoding);
                }
            }
            return unarchive;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static Map<String, Object> readEntries(
            File srcFile, ArchiveStreamFactory aFactory,
            BufferedInputStream fileIn, String encoding) {
        try {
            if (aFactory == null || fileIn == null) {
                return null;
            }
            Map<String, Object> unarchive = null;
            String detected = ArchiveStreamFactory.detect(fileIn);
            if (ArchiveStreamFactory.SEVEN_Z.equals(detected)) {
                unarchive = readEntries7z(srcFile);
            } else if (ArchiveStreamFactory.ZIP.equals(detected)) {
                unarchive = readEntriesZip(srcFile);
            } else {
                try ( ArchiveInputStream in = aFactory.createArchiveInputStream(detected, fileIn, encoding)) {
                    List<FileInformation> entires = readEntries(in);
                    String archiver = detected;
                    if (entires != null && !entires.isEmpty()) {
                        unarchive = new HashMap<>();
                        unarchive.put("archiver", archiver);
                        unarchive.put("entries", entires);
                    }
                }
            }
            return unarchive;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<FileInformation> readEntries(ArchiveInputStream archiveInputStream) {
        List<FileInformation> entries = new ArrayList();
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
                    FileInformation file = new FileInformation();
                    file.setFileName(entry.getName());
                    file.setModifyTime(entry.getLastModifiedDate().getTime());
                    file.setFileSize(entry.getSize());
                    file.setFileType(entry.isDirectory() ? "dir" : "file");
                    entries.add(file);
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
            return entries;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return entries;
    }

    public static Map<String, Object> readEntriesZip(File srcFile) {
        try {
            if (srcFile == null) {
                return null;
            }

            Map<String, Object> unarchive;
            try ( ZipFile zipFile = new ZipFile(srcFile)) {

                List<FileInformation> fileEntries = new ArrayList();
                Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    FileInformation file = new FileInformation();
                    file.setFileName(entry.getName());
                    file.setModifyTime(entry.getLastModifiedDate().getTime());
                    file.setFileSize(entry.getSize());
                    file.setFileType(entry.isDirectory() ? "dir" : "file");
                    fileEntries.add(file);
                }
                unarchive = new HashMap<>();
                unarchive.put("archiver", ArchiveStreamFactory.ZIP);
                unarchive.put("entries", fileEntries);
            }
            return unarchive;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static Map<String, Object> readEntries7z(File srcFile) {
        try {
            if (srcFile == null) {
                return null;
            }
            List<FileInformation> entries;
            Map<String, Object> unarchive;
            try ( SevenZFile sevenZFile = new SevenZFile(srcFile)) {
                SevenZArchiveEntry entry;
                entries = new ArrayList();
                while ((entry = sevenZFile.getNextEntry()) != null) {
                    FileInformation file = new FileInformation();
                    file.setFileName(entry.getName());
                    file.setModifyTime(entry.getLastModifiedDate().getTime());
                    file.setFileSize(entry.getSize());
                    file.setFileType(entry.isDirectory() ? "dir" : "file");
                    entries.add(file);
                }
                unarchive = new HashMap<>();
                unarchive.put("archiver", ArchiveStreamFactory.SEVEN_Z);
                unarchive.put("entries", entries);
            }
            return unarchive;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

}
