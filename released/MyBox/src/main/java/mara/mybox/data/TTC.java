package mara.mybox.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.tools.ByteTools.bytesToHex;
import static mara.mybox.tools.ByteTools.bytesToUInt;
import static mara.mybox.tools.ByteTools.bytesToUshort;
import static mara.mybox.tools.ByteTools.subBytes;
import static mara.mybox.tools.ByteTools.uIntToBytes;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Languages;

/**
 * Reference: https://github.com/fermi1981/TTC_TTF
 *
 * @Author Mara
 * @CreateDate 2020-12-03
 * @License Apache License Version 2.0
 */
public class TTC {

    protected File ttcFile;
    protected String tag, sig;
    protected long version, ttfCount, sigLength, sigOffset;
    protected long[] offsets;
    protected List<TTFInfo> ttfInfos;
    protected List<List<DataInfo>> dataInfos;

    public class TTFInfo {

        String tag;
        int numTables, searchRange, entrySelector, rangeShift;

    }

    public class DataInfo {

        String tag;
        long checkSum, offset, length, writeOffset, writeLength;

    }

    public TTC(File file) {
        ttcFile = file;
    }

    public void parseFile() {
        ttfCount = 0;
        ttfInfos = null;
        dataInfos = null;
        if (ttcFile == null || !ttcFile.exists() || !ttcFile.isFile()) {
            return;
        }
        try ( RandomAccessFile inputStream = new RandomAccessFile(ttcFile, "r")) {
            ttfInfos = new ArrayList<>();
            dataInfos = new ArrayList<>();
            byte[] buf = new byte[4];
            int readLen = inputStream.read(buf);
            if (readLen != 4) {
                return;
            }
            tag = new String(subBytes(buf, 0, 4));
            if (tag.equals("ttcf")) {
                parseTTC(inputStream);
            } else {
                boolean isTTF = false;
                if (buf[0] == 0 && buf[1] == 1 && buf[2] == 0 && buf[3] == 0) {
                    isTTF = true;
                } else if (!tag.toLowerCase().equals("otto")) {
                    isTTF = true;
                }
                if (isTTF) {
                    ttfCount = 1;
                    parseTTF(inputStream, 1);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void parseTTC(RandomAccessFile inputStream) {
        try {
            byte[] buf = new byte[8];
            int readLen = inputStream.read(buf);
            if (readLen != 8) {
                return;
            }
            version = bytesToUInt(subBytes(buf, 0, 4));
            ttfCount = bytesToUInt(subBytes(buf, 4, 4));
            offsets = new long[(int) ttfCount];
            buf = new byte[4];
            for (int i = 0; i < ttfCount; i++) {
                readLen = inputStream.read(buf);
                if (readLen != 4) {
                    return;
                }
                offsets[i] = bytesToUInt(buf);
            }
            buf = new byte[12];
            readLen = inputStream.read(buf);
            if (readLen != 12) {
                return;
            }
            sig = new String(subBytes(buf, 0, 4));
            if (sig.equals("DSIG")) {
                sigLength = bytesToUInt(subBytes(buf, 4, 4));
                sigOffset = bytesToUInt(subBytes(buf, 8, 4));
//                MyBoxLog.console(sig + " " + sigOffset + " " + sigLength);
            }

            for (int i = 0; i < ttfCount; i++) {
                parseTTF(inputStream, i);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Looks working well without alignment.
    public long ceil4(long length) {
        long mod = length % 4;
        if (mod > 0) {
            return length + 4 - mod;
        } else {
            return length;
        }
    }

    public TTFInfo parseTTF(RandomAccessFile inputStream, int index) {
        try {
            inputStream.seek(offsets[index]);
//            MyBoxLog.console("seek:" + offsets[index]);
            byte[] buf = new byte[12];
            int readLen = inputStream.read(buf);
            if (readLen != 12) {
                return null;
            }
            TTFInfo ttfInfo = new TTFInfo();
            ttfInfo.tag = bytesToHex(subBytes(buf, 0, 4));
            ttfInfo.numTables = bytesToUshort(subBytes(buf, 4, 2));
            ttfInfo.searchRange = bytesToUshort(subBytes(buf, 6, 2));
            ttfInfo.entrySelector = bytesToUshort(subBytes(buf, 8, 2));
            ttfInfo.rangeShift = bytesToUshort(subBytes(buf, 10, 2));
            List<DataInfo> ttfData = new ArrayList<>();
            long dataOffset = 12 + ttfInfo.numTables * 16;
            for (int i = 0; i < ttfInfo.numTables; i++) {
                buf = new byte[16];
                readLen = inputStream.read(buf);
                if (readLen != 16) {
                    MyBoxLog.error("Invalid");
                    return null;
                }
                DataInfo dataInfo = new DataInfo();
                dataInfo.tag = new String(subBytes(buf, 0, 4));
                dataInfo.checkSum = bytesToUInt(subBytes(buf, 4, 4));
                dataInfo.offset = bytesToUInt(subBytes(buf, 8, 4));
                dataInfo.length = bytesToUInt(subBytes(buf, 12, 4));
                dataInfo.writeOffset = dataOffset;
                dataInfo.writeLength = ceil4(dataInfo.length);
                dataOffset += dataInfo.writeLength;
                ttfData.add(dataInfo);
            }
            ttfInfos.add(ttfInfo);
            dataInfos.add(ttfData);
            return ttfInfo;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String html() {
        try {
            if (ttcFile == null || ttfInfos == null || ttfInfos.isEmpty()) {
                return null;
            }
            String html = "TTC: " + ttcFile.getAbsolutePath() + "<BR>"
                    + Languages.message("Tag") + ":" + tag + " "
                    + Languages.message("Count") + ":" + ttfCount + "<BR>";

            List<String> names = Arrays.asList("index", Languages.message("Offset"), Languages.message("Tag"),
                    "numTables", "searchRange", "entrySelector", "rangeShift");
            StringTable table = new StringTable(names);
            for (int i = 0; i < ttfInfos.size(); i++) {
                TTFInfo ttf = ttfInfos.get(i);
                List<String> row = Arrays.asList(i + "", offsets[i] + "", ttf.tag,
                        ttf.numTables + "", ttf.searchRange + "", ttf.entrySelector + "", ttf.rangeShift + "");
                table.add(row);
            }
            html += table.div() + "<BR><HR><BR>";
            for (int i = 0; i < ttfInfos.size(); i++) {
                html += "<P align=center>TTF " + (i + 1) + "</P><BR>";
                List<DataInfo> ttfData = dataInfos.get(i);
                names = Arrays.asList("index", Languages.message("Tag"), "checkSum",
                        "offset", "length", "target offset", "target length");
                table = new StringTable(names);
                for (int j = 0; j < ttfData.size(); j++) {
                    DataInfo info = ttfData.get(j);
                    List<String> row = Arrays.asList(j + "", info.tag, info.checkSum + "",
                            info.offset + "", info.length + "", info.writeOffset + "", info.writeLength + "");
                    table.add(row);
                }
                html += table.div() + "<BR><BR>";
            }
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<File> extract(File targetPath) {
        if (ttcFile == null) {
            return null;
        }
        if (ttfInfos == null || ttfInfos.isEmpty()) {
            parseFile();
        }
        if (ttfInfos == null || ttfInfos.isEmpty()) {
            return null;
        }
        String namePrefix = FileNameTools.prefix(ttcFile.getName());
        try ( RandomAccessFile inputStream = new RandomAccessFile(ttcFile, "r")) {
            List<File> files = new ArrayList<>();
            for (int i = 0; i < ttfInfos.size(); i++) {
                long offset = offsets[i];
                inputStream.seek(offset);
                List<DataInfo> ttfData = dataInfos.get(i);
                File ttfFile;
                if (targetPath != null) {
                    ttfFile = new File(targetPath.getAbsoluteFile() + File.separator + namePrefix + "_" + i + ".ttf");
                } else {
                    ttfFile = FileTmpTools.getTempFile(".ttf");
                }
                try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(ttfFile))) {
                    int headerLen = 12 + ttfData.size() * 16;
                    byte[] head = new byte[headerLen];
                    int readLen = inputStream.read(head);
                    if (readLen != headerLen) {
                        MyBoxLog.error(i + " offset:" + offset + " headerLen:" + headerLen + " readLen:" + readLen);
                        continue;
                    }
//                    MyBoxLog.console(bytesToHexFormat(head));
                    for (int j = 0; j < ttfData.size(); j++) {
                        DataInfo dataInfo = ttfData.get(j);
                        byte[] offsetBytes = uIntToBytes(dataInfo.writeOffset);
                        System.arraycopy(offsetBytes, 0, head, 12 + j * 16 + 8, 4);
                        byte[] lenBytes = uIntToBytes(dataInfo.writeLength);
                        System.arraycopy(lenBytes, 0, head, 12 + j * 16 + 12, 4);
//                        MyBoxLog.console(" dataOffset:" + dataInfo.wOffset + " offsetBytes:" + bytesToHexFormat(offsetBytes)
//                                + " fixedLength:" + dataInfo.wLength + " lenBytes:" + bytesToHexFormat(lenBytes));
                    }
//                    MyBoxLog.console(bytesToHexFormat(head));
                    outputStream.write(head);
                    for (int j = 0; j < ttfData.size(); j++) {
                        DataInfo dataInfo = ttfData.get(j);
                        inputStream.seek(dataInfo.offset);
                        byte[] readData = new byte[(int) dataInfo.length]; // Assume length value is smaller than Integer.MAX_VALUE
                        byte[] writeFata = new byte[(int) dataInfo.writeLength];
                        readLen = inputStream.read(readData);
                        if (readLen > 0) {
                            System.arraycopy(readData, 0, writeFata, 0, readLen);
                        }
                        outputStream.write(writeFata);
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    continue;
                }
                files.add(ttfFile);
            }
            return files;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    /*
        get/set
     */
    public File getTtcFile() {
        return ttcFile;
    }

    public void setTtcFile(File ttcFile) {
        this.ttcFile = ttcFile;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getTtfCount() {
        return ttfCount;
    }

    public void setTtfCount(long ttfCount) {
        this.ttfCount = ttfCount;
    }

    public long[] getOffsets() {
        return offsets;
    }

    public void setOffsets(long[] offsets) {
        this.offsets = offsets;
    }

    public long getSigLength() {
        return sigLength;
    }

    public void setSigLength(long sigLength) {
        this.sigLength = sigLength;
    }

    public long getSigOffset() {
        return sigOffset;
    }

    public void setSigOffset(long sigOffset) {
        this.sigOffset = sigOffset;
    }

    public List<TTFInfo> getTtfInfos() {
        return ttfInfos;
    }

    public void setTtfInfos(List<TTFInfo> ttfInfos) {
        this.ttfInfos = ttfInfos;
    }

    public List<List<DataInfo>> getDataInfos() {
        return dataInfos;
    }

    public void setDataInfos(List<List<DataInfo>> dataInfos) {
        this.dataInfos = dataInfos;
    }

}
