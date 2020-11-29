package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-31
 * @License Apache License Version 2.0
 */
public class MessageDigestController extends BaseController {

    protected InputType inputType;
    protected String algorithm;
    protected byte[] digest;

    protected enum InputType {
        File, Input
    }

    @FXML
    protected ToggleGroup inputGroup, algorithmGroup;
    @FXML
    protected VBox handleBox, outputBox;
    @FXML
    protected HBox fileBox;
    @FXML
    protected TextArea inputArea, resultArea;
    @FXML
    protected CheckBox formatCheck;

    public MessageDigestController() {
        baseTitle = AppVariables.message("MessageDigest");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            inputGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkInputType();
                }
            });
            checkInputType();

            algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkAlgorithm();
                }
            });
            checkAlgorithm();

            formatCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    display();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void checkInputType() {
        try {
            clear();
            String selected = ((RadioButton) inputGroup.getSelectedToggle()).getText();
            handleBox.getChildren().clear();
            if (message("File").equals(selected)) {
                handleBox.getChildren().addAll(fileBox, outputBox);
                inputType = InputType.File;

            } else {
                handleBox.getChildren().addAll(inputArea, outputBox);
                inputType = InputType.Input;
                sourceFileInput.setStyle(null);
            }

            FxmlControl.refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // https://docs.oracle.com/javase/10/docs/specs/security/standard-names.html#messagedigest-algorithms
    private void checkAlgorithm() {
        try {
            clear();
            algorithm = ((RadioButton) algorithmGroup.getSelectedToggle()).getText();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void clear() {
        resultArea.clear();
        bottomLabel.setText("");
        digest = null;
    }

    private void display() {
        if (digest == null) {
            resultArea.clear();
            return;
        }
        String result;
        if (formatCheck.isSelected()) {
            result = ByteTools.bytesToHexFormat(digest);
        } else {
            result = ByteTools.bytesToHex(digest);
        }
        resultArea.setText(result);
    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFile = file;
        clear();
    }

    @FXML
    public void dmHelp() {
        try {
            String link;
            switch (AppVariables.getLanguage()) {
                case "zh":
                    link = "https://baike.baidu.com/item/%E6%95%B0%E5%AD%97%E6%91%98%E8%A6%81/4069118";
                    break;
                default:
                    link = "https://en.wikipedia.org/wiki/Message_digest";
            }
            browseURI(new URI(link));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (inputType == InputType.File) {
            if (sourceFile == null || badStyle.equals(sourceFileInput.getStyle())) {
                resultArea.clear();
                popError(message("InvalidData"));
                return;
            }
        } else {
            if (inputArea.getText().isEmpty()) {
                resultArea.clear();
                popError(message("InvalidData"));
                return;
            }
        }
        try {
            synchronized (this) {
                if (task != null && !task.isQuit() ) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private long datalen;

                    @Override
                    protected boolean handle() {
                        try {
                            if (inputType == InputType.File) {
                                digest = SystemTools.messageDigest(sourceFile, algorithm);
                                datalen = sourceFile.length();
                            } else {
                                byte[] data = inputArea.getText().getBytes();
                                digest = SystemTools.messageDigest(data, algorithm);
                                datalen = data.length;
                            }
                            return true;
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        display();
                        String s = MessageFormat.format(message("DigestResult"),
                                datalen, digest.length);
                        s += "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost);
                        bottomLabel.setText(s);
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
