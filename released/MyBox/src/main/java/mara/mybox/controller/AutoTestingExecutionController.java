package mara.mybox.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.dev.TestCase;
import mara.mybox.dev.TestCase.Status;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
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
    protected BaseController currentController;
    protected int currentIndex, delay = 200;
    protected TestCase currentCase;
    protected List<TestCase> testCases;
    protected boolean canceled;
    protected ChangeListener<Boolean> caseListener;
    protected final SimpleBooleanProperty caseNotify;
    protected final Object lock = new Object();

    @FXML
    protected TableColumn<TestCase, Integer> aidColumn;
    @FXML
    protected TableColumn<TestCase, String> typeColumn, operationColumn,
            objectColumn, versionColumn, stageColumn, statusColumn;

    public AutoTestingExecutionController() {
        baseTitle = Languages.message("TestExecution");
        caseNotify = new SimpleBooleanProperty(false);
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

            tableView.setRowFactory((TableView<TestCase> param) -> {
                try {
                    return new TableRow<TestCase>() {
                        @Override
                        protected void updateItem(TestCase item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                setTextFill(null);
                                setStyle(null);
                                return;
                            }
                            if (this.isSelected()) {
                                setStyle(NodeStyleTools.selectedRowStyle());
                            } else {
                                switch (item.getStatus()) {
                                    case Success:
                                        setStyle("-fx-background-color: honeydew");
                                        break;
                                    case Fail:
                                        setStyle("-fx-background-color: aliceblue");
                                        break;
                                    case Testing:
                                        setStyle("-fx-background-color: lightyellow");
                                        break;
                                    default:
                                        setStyle(null);
                                }
                            }
                        }
                    };
                } catch (Exception e) {
                    return null;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            caseListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goCurrentCase();
                }
            };

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void caseNotify() {
        caseNotify.set(!caseNotify.get());
    }

    public void setParameters(AutoTestingCasesController parent, List<TestCase> testCases) {
        try {
            this.casesController = parent;
            this.testCases = testCases;
            tableData.setAll(testCases);

            startAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (startButton.getUserData() != null) {
            stopCases();
            return;
        }
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        Window window = getMyWindow();
        window.setX(0);
        window.setY(0);

        synchronized (lock) {
            for (TestCase testCase : tableData) {
                testCase.setStatus(Status.NotTested);
            }
            canceled = false;
            currentIndex = 0;
            caseNotify.addListener(caseListener);
            AppVariables.autoTestingController = this;
        }
        tableView.refresh();
        caseNotify();
    }

    public void goCurrentCase() {
        if (canceled || testCases == null
                || currentIndex < 0 || currentIndex >= testCases.size()) {
            stopCases();
            return;
        }
        synchronized (lock) {
            try {
                currentCase = tableData.get(currentIndex);
                currentCase.setStatus(Status.Testing);
            } catch (Exception e) {
                MyBoxLog.console(e.toString());
            }
        }
        tableData.set(currentIndex, currentCase);
        tableView.scrollTo(currentIndex - 5);
        runCurrentCase();
    }

    public void runCurrentCase() {
        try {
            if (canceled || currentCase == null) {
                stopCases();
                return;
            }
            String fxml = currentCase.getFxml();
            if (fxml.endsWith("/GeographyCode.fxml")) {
                GeographyCodeController.open().autoTesting();

            } else if (fxml.endsWith("/DataTree.fxml")) {
                BaseNodeTable table = BaseNodeTable.create(currentCase.getObject());
                if (table == null) {
                    endCase(false);
                    return;
                }
                DataTreeController.open().autoTesting(table);

            } else {
                synchronized (lock) {
                    currentController = openStage(fxml);
                }
                if (currentController == null) {
                    endCase(false);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            endCase(false);
        }
    }

    public void sceneLoaded() {
        if (canceled || currentCase == null) {
            stopCases();
            return;
        }
        endCase(true);
    }

    public void errorHappened() {
        if (canceled || currentCase == null) {
            stopCases();
            return;
        }
        if (currentController != null) {
            currentController.close();
        }
        endCase(false);
    }

    public void endCase(boolean success) {
        if (!success && currentCase != null) {
            MyBoxLog.console(currentIndex + "   " + currentCase.getFxml() + ": Failed");
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    synchronized (lock) {
                        currentController = null;
                        if (currentCase != null) {
                            currentCase.setStatus(success ? Status.Success : Status.Fail);
                            tableData.set(currentIndex, currentCase);
                        }
                        currentIndex++;
                    }
                    caseNotify();
                });
                Platform.requestNextPulse();
            }
        }, delay);
    }

    public void stopCases() {
        synchronized (lock) {
            canceled = true;
            caseNotify.removeListener(caseListener);
            currentIndex = -1;
            currentCase = null;
            currentController = null;
            AppVariables.autoTestingController = null;
        }
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
    }

    @Override
    public void cleanPane() {
        try {
            stopCases();
            caseListener = null;
            casesController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static
     */
    public static AutoTestingExecutionController open(AutoTestingCasesController parent, List<TestCase> testCases) {
        try {
            AutoTestingExecutionController controller = (AutoTestingExecutionController) WindowTools.referredTopStage(
                    parent, Fxmls.AutoTestingExecutionFxml);
            controller.setParameters(parent, testCases);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
