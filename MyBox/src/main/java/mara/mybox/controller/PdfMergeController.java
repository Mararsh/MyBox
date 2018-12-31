package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import static mara.mybox.objects.AppVaribles.getUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfMergeController extends PdfBaseController {

    final private String AuthorKey;
    private File targetFile;

    @FXML
    private Button openTargetButton, saveButton;
    @FXML
    private TableView<FileInformation> sourceTable;
    @FXML
    private TableColumn<FileInformation, String> fileColumn, modifyTimeColumn, sizeColumn, createTimeColumn;
    @FXML
    private TextField authorInput;
    @FXML
    private CheckBox deleteCheck;
    @FXML
    protected HBox pdfMemBox;
    @FXML
    private ToggleGroup pdfMemGroup;
    @FXML
    private RadioButton pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;

    public PdfMergeController() {
        AuthorKey = "AuthorKey";
    }

    @Override
    protected void initializeNext2() {
        try {
            allowPaused = false;

            initSourceSection();
            initTargetSection();
            initOptionsSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initSourceSection() {
        try {
            sourceFilesInformation = FXCollections.observableArrayList();
            sourceFilesInformation.addListener(new ListChangeListener<FileInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends FileInformation> change) {
                    long size = 0;
                    if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()) {
                        size = 0;
                    } else {
                        for (FileInformation source : sourceFilesInformation) {
                            size += source.getFile().length();
                        }
                    }
                    bottomLabel.setText(AppVaribles.getMessage("TotalSize") + ": " + FileTools.showFileSize(size));
                }
            });

            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("modifyTime"));
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("createTime"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileSize"));

            sourceTable.setItems(sourceFilesInformation);
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initTargetSection() {
        try {
            targetFileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    openTargetButton.setDisable(true);
                    try {
                        targetFile = new File(newValue);
                        if (!newValue.toLowerCase().endsWith(".pdf")) {
                            targetFile = null;
                            targetFileInput.setStyle(badStyle);
                            return;
                        }
                        targetFileInput.setStyle(null);
                        AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());
                    } catch (Exception e) {
                        targetFile = null;
                        targetFileInput.setStyle(badStyle);
                    }
                }
            });
            saveButton.disableProperty().bind(Bindings.isEmpty(sourceFilesInformation)
                    .or(Bindings.isEmpty(targetFileInput.textProperty()))
                    .or(targetFileInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void initOptionsSection() {

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVaribles.setUserConfigValue(AuthorKey, newValue);
            }
        });
        authorInput.setText(AppVaribles.getUserConfigValue(AuthorKey, System.getProperty("user.name")));

        Tooltip tips = new Tooltip(getMessage("PdfMemComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(pdfMemBox, tips);

        checkPdfMem();

    }

    @FXML
    private void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getUserConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            List<FileInformation> infos = new ArrayList<>();
            for (File file : files) {
                FileInformation info = new FileInformation(file);
                infos.add(info);
            }
            sourceFilesInformation.addAll(infos);
            sourceTable.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void deleteAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }
            sourceFilesInformation.remove(index);
        }
        sourceTable.refresh();
    }

    @FXML
    private void clearAction() {
        sourceFilesInformation.clear();
        sourceTable.refresh();
    }

    @FXML
    private void openAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > sourceFilesInformation.size() - 1) {
                continue;
            }

            FileInformation info = sourceFilesInformation.get(index);
            try {
                Desktop.getDesktop().browse(info.getFile().toURI());
            } catch (Exception e) {

            }

        }
    }

    @FXML
    private void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            sourceFilesInformation.set(index, sourceFilesInformation.get(index - 1));
            sourceFilesInformation.set(index - 1, info);
        }
        for (Integer index : selected) {
            if (index > 0) {
                sourceTable.getSelectionModel().select(index - 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    private void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == sourceFilesInformation.size() - 1) {
                continue;
            }
            FileInformation info = sourceFilesInformation.get(index);
            sourceFilesInformation.set(index, sourceFilesInformation.get(index + 1));
            sourceFilesInformation.set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < sourceFilesInformation.size() - 1) {
                sourceTable.getSelectionModel().select(index + 1);
            }
        }
        sourceTable.refresh();
    }

    protected void checkPdfMem() {
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdfMem1GRadio.setSelected(true);
                break;
            case "2GB":
                pdfMem2GRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfMemUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdfMem500MRadio.setSelected(true);
        }
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVaribles.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVaribles.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVaribles.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVaribles.setPdfMem("Unlimit");
    }

    @FXML
    protected void mouseEnterPane(MouseEvent event) {
        checkPdfMem();
    }

    @FXML
    protected void selectTargetFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            targetFile = file;
            AppVaribles.setUserConfigValue(LastPathKey, targetFile.getParent());
            AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());

            if (targetFileInput != null) {
                targetFileInput.setText(targetFile.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    protected void openTargetAction(ActionEvent event) {
        if (!targetFile.exists()) {
            openTargetButton.setDisable(true);
            return;
        }
        openTargetButton.setDisable(false);
        try {
            Desktop.getDesktop().browse(targetFile.toURI());
        } catch (Exception e) {

        }
    }

    @FXML
    protected void saveAction(ActionEvent event) {
        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()
                || targetFile == null) {
            return;
        }
        task = new Task<Void>() {
            private boolean fail;
            private PDDocument document;
            private String errorString;

            @Override
            protected Void call() throws Exception {
                try {
                    final MemoryUsageSetting memSettings = AppVaribles.PdfMemUsage.setTempDir(AppVaribles.getTempPathFile());

                    PDFMergerUtility mergePdf = new PDFMergerUtility();
                    for (FileInformation source : sourceFilesInformation) {
                        mergePdf.addSource(source.getFile());
                    }
                    mergePdf.setDestinationFileName(targetFile.getAbsolutePath());
                    mergePdf.mergeDocuments(memSettings);

                    try (PDDocument doc = PDDocument.load(targetFile, memSettings)) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(authorInput.getText());
                        doc.setDocumentInformation(info);
                        document = doc;

                        PDPage page = doc.getPage(0);
                        PDPageXYZDestination dest = new PDPageXYZDestination();
                        dest.setPage(page);
                        dest.setZoom(1f);
                        dest.setTop((int) page.getCropBox().getHeight());
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        doc.getDocumentCatalog().setOpenAction(action);

                        doc.save(targetFile);
                    }

                    if (deleteCheck.isSelected()) {
                        for (FileInformation source : sourceFilesInformation) {
                            try {
                                source.getFile().delete();
                            } catch (Exception e) {
                            }
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                clearAction();
                            }
                        });
                    }

                    fail = false;

                } catch (Exception e) {
                    fail = true;
                    errorString = e.toString();
                    logger.error(e.toString());
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!fail && targetFile.exists()) {
                                Desktop.getDesktop().browse(targetFile.toURI());
                                openTargetButton.setDisable(false);
                            } else {
                                popError(errorString);
                            }
                        } catch (Exception e) {
                            logger.error(e.toString());
                            popError(e.toString());
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

}
