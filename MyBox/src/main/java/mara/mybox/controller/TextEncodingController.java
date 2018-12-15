package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileEncoding;
import mara.mybox.tools.FileEncodingTools;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEncodingController extends TextEditorController {

    private FileEncoding fileEncoding, targetEncoding;
    private SaveAsType saveAsType;

    @FXML
    protected AnchorPane textPane;
    @FXML
    protected TextArea hexArea, lineArea;
    @FXML
    private ComboBox currentBox, targetBox;
    @FXML
    protected ToggleGroup targetEndianGroup, saveAsGroup;
    @FXML
    protected CheckBox hexCheck, targetBomCheck, confirmCheck, scrollCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected Label bomLabel;

    public enum SaveAsType {
        Load, Open, None
    }

    @Override
    protected void initializeNext() {
        try {
            fileEncoding = new FileEncoding();
            targetEncoding = new FileEncoding();
            fileChanged = new SimpleBooleanProperty(false);

            List<String> setNames = FileEncodingTools.getCharsetNames();
            currentBox.getItems().addAll(setNames);
            currentBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        fileEncoding.setCharset(Charset.forName(newValue));
                        setChanged(true);
                    }
                }
            });

            targetBox.getItems().addAll(setNames);
            targetBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    targetEncoding.setCharset(Charset.forName(newValue));
                    if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                            || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                            || "UTF-32LE".equals(newValue)) {
                        targetBomCheck.setDisable(false);
                    } else {
                        targetBomCheck.setDisable(true);
                        if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                            targetBomCheck.setSelected(true);
                        } else {
                            targetBomCheck.setSelected(false);
                        }
                    }
                }
            });
            targetBox.getSelectionModel().select(Charset.defaultCharset().name());

            hexCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    setHexPane();
                    scrollCheck.setDisable(!newValue);
                }
            });

            scrollCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (hexArea != null && newValue) {
                        hexArea.setScrollLeft(textArea.getScrollLeft());
                        hexArea.setScrollTop(textArea.getScrollTop());
                    }
                }
            });

            textArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        setChanged(true);
                    }
                }
            });
            textArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (!isSettingValues && hexArea != null && scrollCheck.isSelected()) {
                        isSettingValues = true;
                        hexArea.setScrollTop(newValue.doubleValue());
                        lineArea.setScrollTop(newValue.doubleValue());
                        isSettingValues = false;
                    }
                }
            });
            textArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (!isSettingValues && hexArea != null && scrollCheck.isSelected()) {
                        isSettingValues = true;
                        hexArea.setScrollLeft(newValue.doubleValue());
                        isSettingValues = false;
                    }
                }
            });
            textArea.selectionProperty().addListener(new ChangeListener<IndexRange>() {
                @Override
                public void changed(ObservableValue ov, IndexRange oldValue, IndexRange newValue) {
                    if (!isSettingValues) {
                        setHexSelection();
                    }
                }
            });

            saveAsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSaveAsType();
                }
            });

            checkSaveAsType();
            createAction();
            setHexPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkSaveAsType() {
        try {
            RadioButton selected = (RadioButton) saveAsGroup.getSelectedToggle();
            if (AppVaribles.getMessage("LoadAfterSaveAs").equals(selected.getText())) {
                saveAsType = SaveAsType.Load;
            } else if (AppVaribles.getMessage("OpenAfterSaveAs").equals(selected.getText())) {
                saveAsType = SaveAsType.Open;
            } else if (AppVaribles.getMessage("JustSaveAs").equals(selected.getText())) {
                saveAsType = SaveAsType.None;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void setHexPane() {
        if (hexCheck.isSelected()) {
            if (hexArea == null) {
                hexArea = new TextArea();
                hexArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(hexArea, Priority.ALWAYS);
                HBox.setHgrow(hexArea, Priority.ALWAYS);
                hexArea.setStyle("-fx-highlight-fill: black; -fx-highlight-text-fill: palegreen;");
                hexArea.setEditable(false);
                hexArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        if (!isSettingValues && scrollCheck.isSelected()) {
                            isSettingValues = true;
                            textArea.setScrollTop(newValue.doubleValue());
                            isSettingValues = false;
                        }
                    }
                });
//                hexArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
//                    @Override
//                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
//                        if (!isSettingValues && scrollCheck.isSelected()) {
//                            isSettingValues = true;
//                            textArea.setScrollLeft(newValue.doubleValue());
//                            isSettingValues = false;
//                        }
//                    }
//                });
            }
            if (!splitPane.getItems().contains(hexArea)) {
                splitPane.getItems().add(hexArea);
            }

            setHexArea(textArea.getText());

        } else {
            if (hexArea != null && splitPane.getItems().contains(hexArea)) {
                splitPane.getItems().remove(hexArea);
            }
        }

        switch (splitPane.getItems().size()) {
            case 3:
                splitPane.getDividers().get(0).setPosition(0.33333);
                splitPane.getDividers().get(1).setPosition(0.66666);
//                splitPane.setDividerPositions(0.33, 0.33, 0.33); // This way not work!
                break;
            case 2:
                splitPane.getDividers().get(0).setPosition(0.5);
//               splitPane.setDividerPositions(0.5, 0.5); // This way not work!
                break;
            default:
                splitPane.setDividerPositions(1);
                break;
        }
        splitPane.layout();
    }

    private void setHexArea(String text) {
        if (isSettingValues || hexArea == null || !splitPane.getItems().contains(hexArea)) {
            return;
        }
        isSettingValues = true;
        if (!text.isEmpty()) {
            String hex = ValueTools.bytesToHexFormat(text.getBytes(fileEncoding.getCharset()));
            if (fileEncoding.isWithBom()) {
                hex = FileEncodingTools.bomHex(fileEncoding.getCharset().name()) + " " + hex;
            }
            hexArea.setText(hex);
        } else {
            hexArea.clear();
        }
        isSettingValues = false;
    }

    private void setHexSelection() {
        if (isSettingValues
                || hexArea == null || !splitPane.getItems().contains(hexArea)) {
            return;
        }
        isSettingValues = true;
        hexArea.deselect();
        final String text = textArea.getText();
        if (!text.isEmpty()) {
            IndexRange hexRange = ValueTools.hexIndex(text, fileEncoding.getCharset(), textArea.getSelection());
            if (fileEncoding.isWithBom()) {
                String bom = FileEncodingTools.bomHex(fileEncoding.getCharset().name());
                hexArea.selectRange(hexRange.getStart() + bom.length() + 1, hexRange.getEnd() + bom.length() + 1);
            } else {
                hexArea.selectRange(hexRange.getStart(), hexRange.getEnd());
            }
            hexArea.setScrollTop(textArea.getScrollTop());
        }
        isSettingValues = false;
    }

    private void setLines(String text) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        if (!text.isEmpty()) {
            int linesNumber = ValueTools.countNumber(text, "\n") + 1;
            StringBuilder lines = new StringBuilder();
            for (int i = 1; i <= linesNumber; i++) {
                lines.append(i).append("\n");
            }
            lineArea.setText(lines.toString());
        } else {
            lineArea.clear();
        }
        isSettingValues = false;
    }

    private void setChanged(boolean changed) {
        fileChanged.set(changed);
        if (getMyStage() == null) {
            return;
        }
        String t = getBaseTitle();
        if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (fileChanged.getValue()) {
            getMyStage().setTitle(t + "*");
        } else {
            getMyStage().setTitle(t);
        }
        String text = textArea.getText();
        int len = text.length();
        bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
        setHexArea(text);
        setLines(text);
    }

    public void openFile(File file) {
        sourceFile = file;
        fileEncoding.setFile(file);
        boolean ok = FileEncodingTools.checkCharset(fileEncoding);
        if (!ok || fileEncoding == null) {
            return;
        }
        loadText();
    }

    private void loadText() {
        if (fileEncoding == null) {
            return;
        }
        task = new Task<Void>() {
            private String text;

            @Override
            protected Void call() throws Exception {
                text = FileEncodingTools.readText(fileEncoding);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (text != null) {
                            isSettingValues = true;
                            currentBox.getSelectionModel().select(fileEncoding.getCharset().name());
                            if (fileEncoding.isWithBom()) {
                                currentBox.setDisable(true);
                                bomLabel.setText(AppVaribles.getMessage("WithBom"));
                            } else {
                                currentBox.setDisable(false);
                                bomLabel.setText("");
                            }
                            textArea.setText(text);
                            isSettingValues = false;
                            setChanged(false);
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    @Override
    protected void openAction() {
        try {

            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(TextFilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
            AppVaribles.setUserConfigValue(TextFilePathKey, file.getParent());

            openFile(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    protected void saveAction() {
        if (sourceFile == null) {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(TextFilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
            AppVaribles.setUserConfigValue(TextFilePathKey, file.getParent());
            sourceFile = file;
            fileEncoding.setFile(file);
        } else {
            if (confirmCheck.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
                ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
                ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
                ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
                alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                } else if (result.get() == buttonSaveAs) {
                    saveAsAction();
                    return;
                }
            }
        }
        targetEncoding.setFile(sourceFile);
        targetEncoding.setWithBom(targetBomCheck.isSelected());
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = FileEncodingTools.writeText(targetEncoding, textArea.getText());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            popInformation(AppVaribles.getMessage("Successful"));
                            openFile(sourceFile);
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                        setChanged(false);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    @Override
    protected void saveAsAction() {

        final FileChooser fileChooser = new FileChooser();
        File path = new File(AppVaribles.getUserConfigValue(TextFilePathKey, CommonValues.UserFilePath));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
        AppVaribles.setUserConfigValue(TextFilePathKey, file.getParent());

        targetEncoding.setFile(file);
        targetEncoding.setWithBom(targetBomCheck.isSelected());
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = FileEncodingTools.writeText(targetEncoding, textArea.getText());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            if (saveAsType == SaveAsType.Load) {
                                sourceFile = file;
                                fileEncoding.setFile(file);
                                openFile(sourceFile);

                            } else if (saveAsType == SaveAsType.Open) {
                                final TextEncodingController controller
                                        = (TextEncodingController) openStage(CommonValues.TextEncodingFxml,
                                                AppVaribles.getMessage("TextEncoding"), false, true);
                                controller.openFile(file);
                            }
                            popInformation(AppVaribles.getMessage("Successful"));
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    @Override
    protected void createAction() {
        try {
            isSettingValues = true;
            sourceFile = null;
            fileEncoding = new FileEncoding();
            textArea.setText("");
            if (hexArea != null) {
                hexArea.setText("");
            }
            bomLabel.setText("");
            isSettingValues = false;
            currentBox.getSelectionModel().select(Charset.defaultCharset().name());
            currentBox.setDisable(false);
            setChanged(false);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
