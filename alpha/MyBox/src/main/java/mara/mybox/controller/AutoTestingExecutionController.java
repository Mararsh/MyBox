package mara.mybox.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import mara.mybox.data.TestCase;
import mara.mybox.data.TestCase.Status;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.AppVariables.errorNotify;
import static mara.mybox.value.AppVariables.isTesting;
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
                                setStyle(NodeStyleTools.selectedData);
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
                    if (isTesting && currentCase != null && currentIndex >= 0) {
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
            isTesting = false;
            StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            canceled = true;
            return;
        }
        isTesting = true;
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
                        if (currentController != null) {
                            currentController.close();
                        }
                        if (currentCase != null && currentCase.getStatus() != Status.Fail) {
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
            currentController = openStage(currentCase.getFxml());

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
