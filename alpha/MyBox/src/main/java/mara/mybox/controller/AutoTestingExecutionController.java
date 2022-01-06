package mara.mybox.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import mara.mybox.data.TestCase;
import mara.mybox.data.TestCase.Status;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.AppVariables.errorNotify;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-6
 * @License Apache License Version 2.0
 */
public class AutoTestingExecutionController extends BaseTableViewController<TestCase> {

    protected AutoTestingCasesController casesController;
    protected int currentIndex;
    protected TestCase currentCase;
    protected List<TestCase> testCases;
    protected BaseController currentController;
    protected boolean canceled;

    @FXML
    protected TableColumn<TestCase, Integer> aidColumn;
    @FXML
    protected TableColumn<TestCase, String> typeColumn, operationColumn, objectColumn, versionColumn, stageColumn, statusColumn;

    public AutoTestingExecutionController() {
        baseTitle = Languages.message("TestExecution");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            aidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            operationColumn.setCellValueFactory(new PropertyValueFactory<>("operationName"));
            objectColumn.setCellValueFactory(new PropertyValueFactory<>("object"));
            versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
            stageColumn.setCellValueFactory(new PropertyValueFactory<>("stage"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusName"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            errorNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (currentCase == null && currentIndex >= 0) {
                        currentCase.setStatus(Status.Fail);
                        tableData.set(currentIndex, currentCase);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(AutoTestingCasesController parent, List<TestCase> testCases) {
        try {
            this.casesController = parent;
            this.testCases = testCases;
            tableData.setAll(testCases);

            errorNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (currentCase != null && currentIndex >= 0) {
                        currentCase.setStatus(Status.Fail);
                        tableData.set(currentIndex, currentCase);
                    }
                }
            });

            startAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (startButton.getUserData() != null) {
            StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            canceled = true;
            return;
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        canceled = false;

        Window window = getMyWindow();
        window.setX(0);
        window.setY(0);
        currentIndex = 0;
        for (TestCase testCase : tableData) {
            testCase.setStatus(Status.NotTested);
        }
        tableView.refresh();
        goCurrentCase();
    }

    public void goCurrentCase() {
        try {
            currentCase = null;
            currentController = null;
            if (canceled || testCases == null || currentIndex < 0 || currentIndex >= testCases.size()) {
                currentIndex = -1;
                StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
                startButton.applyCss();
                startButton.setUserData(null);
                return;
            }
            currentCase = tableData.get(currentIndex);
            currentCase.setController(null);
            currentCase.setStatus(Status.Testing);
            tableData.set(currentIndex, currentCase);

            currentController = runCurrentCase();

            if (currentController == null) {
                currentCase.setStatus(Status.Fail);
                tableData.set(currentIndex, currentCase);
                currentIndex++;
                goCurrentCase();
                return;
            }
            currentCase.setController(currentController);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        currentController.close();
                        if (currentCase.getStatus() != Status.Fail) {
                            currentCase.setStatus(Status.Success);
                            tableData.set(currentIndex, currentCase);
                        }
                        if (canceled) {
                            return;
                        }
                        currentIndex++;
                        goCurrentCase();
                    });
                }
            }, 2000);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public BaseController runCurrentCase() {
        try {
            if (canceled || currentCase == null) {
                return null;
            }
            currentController = checkDocumentsCases();

            return currentController;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    public BaseController checkDocumentsCases() {
        try {
            if (canceled || currentCase == null) {
                return null;
            }
            if (message("Notes").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.NotesFxml);
                }

            } else if (message("PdfView").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfViewFxml);
                }

            } else if (message("PdfConvertImagesBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfConvertImagesBatchFxml);
                }

            } else if (message("PdfImagesConvertBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfImagesConvertBatchFxml);
                }

            } else if (message("PdfCompressImagesBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfCompressImagesBatchFxml);
                }

            } else if (message("PdfConvertHtmlsBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfConvertHtmlsBatchFxml);
                }

            } else if (message("PdfExtractImagesBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfExtractImagesBatchFxml);
                }

            } else if (message("PdfExtractTextsBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfExtractTextsBatchFxml);
                }

            } else if (message("PdfOCRBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfOCRBatchFxml);
                }

            } else if (message("PdfSplitBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfSplitBatchFxml);
                }

            } else if (message("MergePdf").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfMergeFxml);
                }

            } else if (message("PDFAttributes").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfAttributesFxml);
                }

            } else if (message("PDFAttributesBatch").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.PdfAttributesBatchFxml);
                }

            } else if (message("MarkdownEditer").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.MarkdownEditorFxml);
                }

            } else if (message("MarkdownToHtml").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.MarkdownToHtmlFxml);
                }

            } else if (message("MarkdownToText").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.MarkdownToTextFxml);
                }

            } else if (message("MarkdownToPdf").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.MarkdownToPdfFxml);
                }

            } else if (message("HtmlEditor").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlEditorFxml);
                }

            } else if (message("WebFind").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlFindFxml);
                }

            } else if (message("WebElements").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlElementsFxml);
                }

            } else if (message("HtmlSnap").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlSnapFxml);
                }

            } else if (message("HtmlExtractTables").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlExtractTablesFxml);
                }

            } else if (message("HtmlToMarkdown").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlToMarkdownFxml);
                }

            } else if (message("HtmlToText").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlToTextFxml);
                }

            } else if (message("HtmlToPdf").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlToPdfFxml);
                }

            } else if (message("HtmlSetCharset").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlSetCharsetFxml);
                }

            } else if (message("HtmlSetStyle").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlSetStyleFxml);
                }

            } else if (message("HtmlMergeAsHtml").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlMergeAsHtmlFxml);
                }

            } else if (message("HtmlMergeAsMarkdown").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlMergeAsMarkdownFxml);
                }

            } else if (message("HtmlMergeAsPDF").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlMergeAsPDFFxml);
                }

            } else if (message("HtmlMergeAsText").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlMergeAsTextFxml);
                }

            } else if (message("HtmlFrameset").equals(currentCase.getObject())) {
                if (currentCase.getOperation() == TestCase.Operation.OpenInterface) {
                    currentController = openStage(Fxmls.HtmlFramesetFxml);
                }

            }
            return currentController;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    /*
        static
     */
    public static AutoTestingExecutionController open(AutoTestingCasesController parent, List<TestCase> testCases) {
        try {
            AutoTestingExecutionController controller = (AutoTestingExecutionController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.AutoTestingExecutionFxml, false);
            controller.setParameters(parent, testCases);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
