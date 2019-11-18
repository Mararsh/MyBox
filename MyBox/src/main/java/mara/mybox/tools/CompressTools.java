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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.FileInformation;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;

/**
 *
 * @author mara
 */
public class CompressTools {

    public static String compressorByExtension(String ext) {
        switch (ext) {
            case "bz2":
                return "bzip2";

            case "pack":
                return "pack200";

            case "sz":
                return "snappy-framed";

            default:
                return ext;
        }
    }

    public static String extensionByCompressor(String compressor) {
        switch (compressor) {
            case "bzip2":
                return "bz2";

            case "pack200":
                return "pack";

            case "snappy-framed":
                return "sz";

            default:
                if (compressor.startsWith("lz4")) {
                    return "lz4";
                } else {
                    return compressor;
                }
        }
    }

    public static String detectCompressor(File srcFile) {
        if (srcFile == null) {
            return null;
        }
        String ext = FileTools.getFileSuffix(srcFile).toLowerCase();
        return detectCompressor(srcFile, compressorByExtension(ext));
    }

    public static String detectCompressor(File srcFile, String name) {
        try {
//            logger.debug("uncompress: " + compressorChoice);
            if (srcFile == null || "none".equals(name)) {
                return null;
            }
            String compressor;
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
            }
            return compressor;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return null;
        }
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
            logger.debug("uncompress: " + nameIn);
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
//                        logger.debug(e.toString());
                        try {
                            String defectValue = CompressorStreamFactory.detect(fileIn);
                            try ( CompressorInputStream in = cFactory.createCompressorInputStream(defectValue, fileIn)) {
                                decompressedFile = decompress(in, targetFile);
                                if (decompressedFile != null) {
                                    compressor = defectValue;
                                    detect = true;
                                }
                            } catch (Exception ex) {
                                logger.debug(ex.toString());
                            }
                        } catch (Exception ex) {
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
//                            logger.debug(ex.toString());
                        }
                    } catch (Exception ex) {
//                        logger.debug(ex.toString());
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
//            logger.debug(e.toString());
            return null;
        }
    }

    public static File decompress(CompressorInputStream compressorInputStream, File targetFile) {
        try {
            if (compressorInputStream == null) {
                return null;
            }
            File file = (targetFile == null) ? FileTools.getTempFile() : targetFile;
            if (file.exists()) {
                file.delete();
            }
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                final byte[] buf = new byte[CommonValues.IOBufferLength];
                int len = -1;
                while (-1 != (len = compressorInputStream.read(buf))) {
                    out.write(buf, 0, len);
                }
            }
            return file;
        } catch (Exception e) {
//            logger.debug(e.toString());
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
        try {
//            logger.debug("archiveExt: " + name);
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
//                            logger.debug(ex.toString());
                        }
                    }
                } else {
                    try {
                        archiver = ArchiveStreamFactory.detect(fileIn);
                    } catch (Exception ex) {
//                            logger.debug(ex.toString());
                    }
                }
            }
            return archiver;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return null;
        }
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
//            logger.debug(e.toString());
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
//            logger.debug(e.toString());
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
                    logger.debug("Can not Read entry Data:" + entry.getName());
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
                    logger.debug(e.toString());
                }
            }
            return entries;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return entries;
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
//            logger.debug(e.toString());
            return null;
        }
    }

}
