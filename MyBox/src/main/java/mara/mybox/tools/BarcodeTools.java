package mara.mybox.tools;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.Compaction;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.dev.MyBoxLog;
import org.krysalis.barcode4j.ChecksumMode;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.fourstate.RoyalMailCBCBean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import thridparty.QRCodeWriter;

/**
 * @Author Mara
 * @CreateDate 2019-9-24
 * @Description
 * @License Apache License Version 2.0
 */
public class BarcodeTools {

    public enum BarcodeType {
        QR_Code, PDF_417, DataMatrix,
        Code39, Code128, Codabar, Interleaved2Of5, ITF_14, POSTNET, EAN13, EAN8, EAN_128, UPCA, UPCE,
        Royal_Mail_Customer_Barcode, USPS_Intelligent_Mail
    }

    public static double defaultModuleWidth(BarcodeType type) {
        switch (type) {
            case PDF_417:
                return new PDF417Bean().getModuleWidth();
            case Code39:
                return new Code39Bean().getModuleWidth();
            case Code128:
                return new Code128Bean().getModuleWidth();
            case Codabar:
                return new CodabarBean().getModuleWidth();
            case Interleaved2Of5:
                return new Interleaved2Of5Bean().getModuleWidth();
            case POSTNET:
                return new POSTNETBean().getModuleWidth();
            case EAN13:
                return new EAN13Bean().getModuleWidth();
            case EAN8:
                return new EAN8Bean().getModuleWidth();
            case UPCA:
                return new UPCABean().getModuleWidth();
            case UPCE:
                return new UPCEBean().getModuleWidth();
            case Royal_Mail_Customer_Barcode:
                return new RoyalMailCBCBean().getModuleWidth();
            case DataMatrix:
                return new DataMatrixBean().getModuleWidth();
        }
        return 0.20f;
    }

    public static double defaultBarRatio(BarcodeType type) {
        switch (type) {
            case Code39:
                return new Code39Bean().getWideFactor();
            case Codabar:
                return new CodabarBean().getWideFactor();
            case Interleaved2Of5:
                return new Interleaved2Of5Bean().getWideFactor();
        }
        return 2.0;
    }

    public static BufferedImage QR(String code,
            ErrorCorrectionLevel qrErrorCorrectionLevel,
            int qrWidth, int qrHeight, int qrMargin, File picFile) {
        try {
            HashMap hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, qrErrorCorrectionLevel);
            hints.put(EncodeHintType.MARGIN, qrMargin);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(code,
                    BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            if (picFile != null) {
                BufferedImage pic = ImageFileReaders.readImage(picFile);
                if (pic != null) {
                    double ratio = 2;
                    switch (qrErrorCorrectionLevel) {
                        case L:
                            ratio = 0.16;
                            break;
                        case M:
                            ratio = 0.20;
                            break;
                        case Q:
                            ratio = 0.25;
                            break;
                        case H:
                            ratio = 0.30;
                            break;
                    }
                    // https://www.cnblogs.com/tuyile006/p/3416008.html
//                    ratio = Math.min(2 / 7d, ratio);
                    int width = (int) ((qrImage.getWidth() - writer.getLeftPadding() * 2) * ratio);
                    int height = (int) ((qrImage.getHeight() - writer.getTopPadding() * 2) * ratio);
                    return BarcodeTools.centerPicture(qrImage, pic, width, height);
                }
            }
            return qrImage;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage centerPicture(BufferedImage source,
            BufferedImage picture, int w, int h) {
        try {
            if (w <= 0 || h <= 0 || picture == null || picture.getWidth() == 0) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int ah = h, aw = w;
            if (w * 1.0f / h > picture.getWidth() * 1.0f / picture.getHeight()) {
                ah = picture.getHeight() * w / picture.getWidth();
            } else {
                aw = picture.getWidth() * h / picture.getHeight();
            }
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.drawImage(picture, (width - aw) / 2, (height - ah) / 2, aw, ah, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage PDF417(String code, int pdf417ErrorCorrectionLevel,
            Compaction pdf417Compact,
            int pdf417Width, int pdf417Height, int pdf417Margin) {
        try {
            HashMap hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, pdf417ErrorCorrectionLevel);
            hints.put(EncodeHintType.MARGIN, pdf417Margin);
            if (pdf417Compact == null) {
                hints.put(EncodeHintType.PDF417_COMPACT, false);
            } else {
                hints.put(EncodeHintType.PDF417_COMPACT, true);
                hints.put(EncodeHintType.PDF417_COMPACTION, pdf417Compact);
            }

            BitMatrix bitMatrix = new MultiFormatWriter().encode(code,
                    BarcodeFormat.PDF_417, pdf417Width, pdf417Height, hints);

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage DataMatrix(String code,
            int dmWidth, int dmHeigh) {
        try {
            HashMap hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(code,
                    BarcodeFormat.DATA_MATRIX, dmWidth, dmHeigh, hints);

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage createBarcode(
            BarcodeType type, String code,
            double height, double narrowWidth, double barRatio, double quietWidth,
            HumanReadablePlacement textPostion, String fontName, int fontSize,
            int dpi, boolean antiAlias, int orientation, boolean checksum, boolean startstop) {
        try {

            AbstractBarcodeBean bean = null;
            switch (type) {
                case Code39:
                    Code39Bean code39 = new Code39Bean();
                    code39.setWideFactor(barRatio);
                    code39.setDisplayChecksum(checksum);
                    code39.setDisplayStartStop(startstop);
                    code39.setExtendedCharSetEnabled(true);
                    code39.setChecksumMode(ChecksumMode.CP_ADD);
                    bean = code39;
                    break;
                case Code128:
                    Code128Bean code128 = new Code128Bean();
                    bean = code128;
                    break;
                case Codabar:
                    CodabarBean codabar = new CodabarBean();
                    codabar.setWideFactor(barRatio);
                    bean = codabar;
                    break;
            }
            if (bean == null) {
                return null;
            }

            bean.setFontName(code);
            bean.setFontSize(fontSize);
            bean.setModuleWidth(narrowWidth);
            bean.setHeight(height);
            bean.setQuietZone(quietWidth);
            bean.setMsgPosition(textPostion);

            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    dpi, BufferedImage.TYPE_BYTE_BINARY, antiAlias, orientation);
            bean.generateBarcode(canvas, code);
            canvas.finish();

            return canvas.getBufferedImage();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage createCode39(
            BarcodeType type, String code,
            double height, double narrowWidth, double barRatio, double quietWidth,
            HumanReadablePlacement textPostion, String fontName, int fontSize,
            int dpi, boolean antiAlias, int orientation, boolean checksum, boolean startstop) {
        try {

            Code39Bean code39 = new Code39Bean();
            code39.setWideFactor(barRatio);
            code39.setDisplayChecksum(checksum);
            code39.setDisplayStartStop(startstop);
            code39.setExtendedCharSetEnabled(true);
            code39.setChecksumMode(ChecksumMode.CP_ADD);

            code39.setFontName(code);
            code39.setFontSize(fontSize);
            code39.setModuleWidth(narrowWidth);
            code39.setHeight(height);
            code39.setQuietZone(quietWidth);
            code39.setMsgPosition(textPostion);

            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    dpi, BufferedImage.TYPE_BYTE_BINARY, antiAlias, orientation);
            code39.generateBarcode(canvas, code);
            canvas.finish();

            return canvas.getBufferedImage();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
