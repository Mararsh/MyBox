package mara.mybox.controller;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.color.ColorBase;
import mara.mybox.controller.base.BatchController;
import mara.mybox.value.AppVaribles;
import mara.mybox.data.FileInformation;
import mara.mybox.image.ImageManufacture;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesBatchController extends BatchController<FileInformation> {

    /**
     * Methods to be implemented
     */
    // SubClass should use either "makeSingleParameters()" or "makeBatchParameters()"
    @Override
    public void makeMoreParameters() {

    }
    // "targetFiles" and "actualParameters.finalTargetName" should be written by this method

    public String handleCurrentFile(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    public String handleCurrentDirectory(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    public FilesBatchController() {
    }

    /* ----Method may need updated ------------------------------------------------- */
    @Override
    public void initializeNext2() {

    }

    @Override
    public void initOptionsSection() {

    }

    /* ------Method need not updated commonly ----------------------------------------------- */
    @Override
    public void addFiles(int index, List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileAdded(files.get(0));

            List<FileInformation> infos = new ArrayList<>();
            for (File file : files) {
                FileInformation info = new FileInformation(file);
                infos.add(info);
            }
            if (infos.isEmpty()) {
                return;
            }
            if (index < 0 || index >= tableData.size()) {
                tableData.addAll(infos);
            } else {
                tableData.addAll(index, infos);
            }
            tableView.refresh();

            test(files.get(0));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void test(final File file) {

        task = new Task<Void>() {
            private boolean ok;

            @Override
            public Void call() {
                try {
                    BufferedImage inImage = null;
                    try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                        while (readers.hasNext()) {
                            ImageReader reader = readers.next();
                            logger.debug(reader.getClass() + "  " + reader.canReadRaster() + "  " + reader.getOriginatingProvider().getPluginClassName());
                        }
                        if (readers != null && readers.hasNext()) {

                            ImageReader reader = readers.next();
                            reader.setInput(iis);
                            int num = reader.getNumImages(true);
                            for (int i = 0; i < num; i++) {
                                Iterator<ImageTypeSpecifier> types = reader.getImageTypes(i);
                                if (types != null) {
                                    while (types.hasNext()) {
                                        ImageTypeSpecifier imageTypeSpecifier = types.next();
                                        logger.debug(" ---------- imageTypeSpecifier ---------");
                                        try {
                                            ColorModel cm = imageTypeSpecifier.getColorModel();
                                            logger.debug("getNumComponents:" + cm.getNumComponents() + " getPixelSize:" + cm.getPixelSize());
                                            ColorSpace cs = cm.getColorSpace();
                                            logger.debug(" colorSpaceType:" + ColorBase.colorSpaceType(cs.getType()));
                                        } catch (Exception e) {
                                            logger.error(e.toString());
                                        }
                                    }
                                }
                            }
                            try {
                                ImageTypeSpecifier imageTypeSpecifier = reader.getRawImageType(0);
                                logger.debug(" ---------- getRawImageType ---------");
                                ColorModel cm = imageTypeSpecifier.getColorModel();
                                logger.debug("getNumComponents:" + cm.getNumComponents() + " getPixelSize:" + cm.getPixelSize());
                                ColorSpace cs = cm.getColorSpace();
                                logger.debug(" colorSpaceType:" + ColorBase.colorSpaceType(cs.getType()));
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                            try {

                                inImage = ImageManufacture.removeAlpha(reader.read(0));
                                logger.debug(inImage.getType());
                                ColorModel cm = inImage.getColorModel();
                                ColorSpace cs = cm.getColorSpace();
                                logger.debug(ColorBase.colorSpaceType(cs.getType()) + " " + cm.getNumComponents() + " " + cm.getPixelSize());
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }

                            reader.dispose();

                        }

                    }
                    if (inImage != null) {
                        // https://stackoverflow.com/questions/3123574/how-to-convert-from-cmyk-to-rgb-in-java-correctly/12132630?r=SearchResults#12132630
                        ICC_Profile cmykProfile = ICC_Profile.getInstance(getClass().getResourceAsStream("/data/eciCMYK.icc"));
                        if (cmykProfile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
                            logger.debug(cmykProfile.getProfileClass());
                            byte[] profileData = cmykProfile.getData();
                            if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
                                intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, ICC_Profile.icHdrDeviceClass); // Header is first
                                cmykProfile = ICC_Profile.getInstance(profileData);
                            }
                        }
                        ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
                        ColorConvertOp rgb2cmyk = new ColorConvertOp(cmykCS, null);
                        BufferedImage cmykImage = rgb2cmyk.filter(inImage, null);

                        logger.debug(cmykProfile.getColorSpaceType() + " " + cmykCS.getType() + " " + cmykImage.getType());
                        ColorModel cm = cmykImage.getColorModel();
                        ColorSpace cs = cm.getColorSpace();
                        logger.debug(ColorBase.colorSpaceType(cs.getType()) + " " + cm.getNumComponents() + " " + cm.getPixelSize());
                        String fname = file.getAbsoluteFile() + "-cmyk.tiff";
                        ImageIO.write(cmykImage, "tiff", new File(fname));
                        logger.debug(fname);

//                        ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
//                        ImageWriteParam param = writer.getDefaultWriteParam();
//                        IIOMetadata metaData
//                                = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(cmykImage), writer.getDefaultWriteParam());
//                        logger.debug(fname);
//                        if (metaData != null && !metaData.isReadOnly()) {
//                            String format = metaData.getNativeMetadataFormatName(); // "javax_imageio_png_1.0"
//                            IIOMetadataNode tree = (IIOMetadataNode) metaData.getAsTree(format);
//                            IIOMetadataNode iccp = new IIOMetadataNode("iCCP");
//                            iccp.setUserObject(getAsDeflatedBytes(cmykCS));
//                            iccp.setAttribute("profileName", "AdobeRGB1998");
//                            iccp.setAttribute("compressionMethod", "deflate");
//                            tree.appendChild(iccp);
//                            metaData.mergeTree(format, tree);
//                        }
//                        try (ImageOutputStream out = ImageIO.createImageOutputStream(new File(fname))) {
//                            writer.setOutput(out);
//                            logger.debug(fname);
////                            writer.write(cmykImage);
//                            writer.write(metaData, new IIOImage(cmykImage, null, metaData), param);
//                            out.flush();
//                        }
//                        writer.dispose();
                    }

                } catch (Exception e) {
                    logger.debug(e.toString());
                }

                ok = true;
                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void cancelled() {
                super.cancelled();
            }

            @Override
            public void failed() {
                super.failed();
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    static void intToBigEndian(int value, byte[] array, int index) {
        array[index] = (byte) (value >> 24);
        array[index + 1] = (byte) (value >> 16);
        array[index + 2] = (byte) (value >> 8);
        array[index + 3] = (byte) (value);
    }

    private static byte[] getAsDeflatedBytes(ICC_ColorSpace colorSpace) throws IOException {
        byte[] data = colorSpace.getProfile().getData();

        ByteArrayOutputStream deflated = new ByteArrayOutputStream();
        DeflaterOutputStream deflater = new DeflaterOutputStream(deflated);
        deflater.write(data);
        deflater.flush();
        deflater.close();

        return deflated.toByteArray();
    }

    @FXML
    @Override
    public void viewFileAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > tableData.size() - 1) {
                continue;
            }
            FileInformation info = tableData.get(index);
            if (info.getNewName() != null && !info.getNewName().isEmpty()) {
                view(info.getNewName());
            } else {
                view(info.getFile());
            }
        }
    }

    public FileInformation getCurrentData() {
        return getData(currentParameters.currentIndex);
    }

    public FileInformation getData(int index) {
        try {
            return tableData.get(sourcesIndice.get(index));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean handleCurrentFile() {
        FileInformation d = getCurrentData();
        if (d == null) {
            return false;
        }
        File file = d.getFile();
        currentParameters.sourceFile = file;
        String result;
        if (!file.exists()) {
            result = AppVaribles.getMessage("NotFound");
        } else if (file.isFile()) {
            result = handleCurrentFile(d);
        } else {
            result = handleCurrentDirectory(d);
        }
        d.setHandled(result);
        tableView.refresh();
        currentParameters.currentTotalHandled++;
        return true;
    }

    @Override
    public FileInformation getData(File directory) {
        return new FileInformation(directory);
    }

    @Override
    public File getTableFile(int index) {
        return tableData.get(index).getFile();
    }

    public void markFileHandled(int index) {
        if (tableView == null) {
            return;
        }
        FileInformation d = getData(index);
        if (d == null) {
            return;
        }
        d.setHandled(getMessage("Yes"));
        tableView.refresh();
    }

    public void markFileHandled(int index, String message) {
        if (tableView == null) {
            return;
        }
        FileInformation d = getData(index);
        if (d == null) {
            return;
        }
        d.setHandled(message);
        tableView.refresh();
    }

}
