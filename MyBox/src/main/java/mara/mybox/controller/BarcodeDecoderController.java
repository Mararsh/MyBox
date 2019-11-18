package mara.mybox.controller;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.util.HashMap;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import mara.mybox.tools.BarcodeTools.BarcodeType;
import mara.mybox.tools.ByteTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-24
 * @Description
 * @License Apache License Version 2.0
 */
public class BarcodeDecoderController extends ImageViewerController {

    protected BarcodeType codeType;

    @FXML
    protected ComboBox<String> typeSelecor;
    @FXML
    protected TextArea codeInput;

    public BarcodeDecoderController() {
        baseTitle = AppVariables.message("BarcodeDecoder");
        needNotRulers = true;
        needNotCoordinates = true;
    }

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();
        codeInput.setText("");
    }

    @FXML
    @Override
    public void startAction() {
        if (imageView.getImage() == null) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private Result result;

                    @Override
                    protected boolean handle() {
                        try {
                            LuminanceSource source = new BufferedImageLuminanceSource(
                                    SwingFXUtils.fromFXImage(imageView.getImage(), null));
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                            HashMap hints = new HashMap();
                            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                            result = new MultiFormatReader().decode(bitmap, hints);

                            return result != null;

                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        String s = "---------" + message("Contents") + "---------\n"
                                + result.getText()
                                + "\n\n---------" + message("MetaData") + "---------\n"
                                + message("Type") + ": "
                                + result.getBarcodeFormat().name();
                        if (result.getResultMetadata() != null) {
                            for (ResultMetadataType type : result.getResultMetadata().keySet()) {
                                Object value = result.getResultMetadata().get(type);
                                switch (type) {
                                    case PDF417_EXTRA_METADATA:
//                                        PDF417ResultMetadata pdf417meta
//                                            = (PDF417ResultMetadata) result.getResultMetadata().get(ResultMetadataType.PDF417_EXTRA_METADATA);
                                        break;
                                    case BYTE_SEGMENTS:
                                        s += "\n" + message("BYTE_SEGMENTS") + ": ";
                                        for (byte[] bytes : (List<byte[]>) value) {
                                            s += ByteTools.bytesToHexFormat(bytes) + "        ";
                                        }
                                        break;
                                    default:
                                        s += "\n" + message(type.name()) + ": " + value;
                                }
                            }
                        }
                        result.getTimestamp();
                        codeInput.setText(s);

                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
