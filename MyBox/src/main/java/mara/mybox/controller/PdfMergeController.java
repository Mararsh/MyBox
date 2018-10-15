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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfMergeController extends PdfBaseController {

    final private String AuthorKey;
    private int pageWidth, pageHeight;
    private File targetFile;
    private PDRectangle pageSize;
    private boolean notChange;
    private File tempdir;

    @FXML
    private Button openTargetButton, saveButton;
    @FXML
    private TableView<FileInformation> sourceTable;
    @FXML
    private TableColumn<FileInformation, String> fileColumn, pixelsColumn, modifyTimeColumn, sizeColumn, createTimeColumn;
    @FXML
    private TextField authorInput, tempDirInput;
    @FXML
    private CheckBox deleteCheck;

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
                        AppVaribles.setConfigValue(targetPathKey, targetFile.getParent());
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
                AppVaribles.setConfigValue(AuthorKey, newValue);
            }
        });
        authorInput.setText(AppVaribles.getConfigValue(AuthorKey, System.getProperty("user.name")));

    }

    @FXML
    private void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setConfigValue(LastPathKey, path);
            AppVaribles.setConfigValue(targetPathKey, path);
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

    @FXML
    protected void selectTargetFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            targetFile = file;
            AppVaribles.setConfigValue(LastPathKey, targetFile.getParent());
            AppVaribles.setConfigValue(targetPathKey, targetFile.getParent());

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
        Task saveTask = new Task<Void>() {
            private boolean fail;
            private PDDocument document;
            private String errorString;

            @Override
            protected Void call() throws Exception {
                try {
                    final MemoryUsageSetting memSettings = AppVaribles.PdfMemUsage.setTempDir(tempdir);

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
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();
    }

}
