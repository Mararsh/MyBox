package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.Member;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.db.TableMember;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-13
 * @License Apache License Version 2.0
 */
public class GeographyRegionEditController extends GeographyCodeController {

    protected GeographyRegionController parent;
    protected SingletonTask regionsTask;
    protected String currentRegion;

    @FXML
    protected ListView<String> regionList;
    @FXML
    protected TextField regionInput;
    @FXML
    protected TableColumn<GeographyCode, Boolean> selectColumn;
    @FXML
    protected Button dataButton;

    public GeographyRegionEditController() {
        baseTitle = message("RegionMembers");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            regionInput.setOnAction((ActionEvent event) -> {
                loadRegions(regionInput.getText());
            });
            loadRegions(null);

            regionList.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String t, String t1) -> {
                        loadMembers(t1);
                    });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
            selectColumn.setCellFactory((TableColumn<GeographyCode, Boolean> p) -> {
                CheckBoxTableCell<GeographyCode, Boolean> cell = new CheckBoxTableCell<>();
                cell.setSelectedStateCallback((Integer index) -> tableData.get(index).getSelectedProperty());
                return cell;
            });
            selectColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void itemDoubleClicked() {
        locationAction();
    }

    public void loadRegions(String region) {
        if (isSettingValues) {
            return;
        }
        synchronized (this) {
            if (regionsTask != null) {
                return;
            }
            regionList.getItems().clear();
            regionsTask = new SingletonTask<Void>() {
                private List<String> regions;

                @Override
                protected boolean handle() {
                    regions = TableGeographyCode.readAddressLike(region);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    if (regions != null && !regions.isEmpty()) {
                        regionList.getItems().addAll(regions);
                    }
                    isSettingValues = false;
                    loadMembers(null);
                }

                @Override
                protected void taskQuit() {
                    regionsTask = null;
                }
            };
            openHandlingStage(regionsTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(regionsTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void loadMembers(String region) {
        if (isSettingValues) {
            return;
        }
        currentRegion = region;
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<String> members;

                @Override
                protected boolean handle() {
                    if (currentRegion == null) {
                        members = null;
                    } else {
                        members = TableMember.read("GeographyRegion", currentRegion);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    regionList.getSelectionModel().select(currentRegion);
                    for (GeographyCode code : tableData) {
                        code.setSelected(members != null && members.contains(code.getAddress()));
                    }
                    isSettingValues = false;
//                    tableView.refresh();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        String region = regionList.getSelectionModel().getSelectedItem();
        if (region == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    List<Member> selects = new ArrayList();
                    List<Member> unselects = new ArrayList();
                    for (GeographyCode code : tableData) {
                        if (code.getSelected()) {
                            selects.add(new Member("GeographyRegion", region, code.getAddress()));
                        } else {
                            unselects.add(new Member("GeographyRegion", region, code.getAddress()));
                        }
                    }
                    TableMember.add(selects);
                    TableMember.delete(unselects);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (parent != null) {
                        parent.getMyStage().toFront();
                    }
                    if (saveCloseCheck.isSelected()) {
                        closeStage();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void dataAction() {
        try {
            GeographyCodeController controller = (GeographyCodeController) openStage(CommonValues.GeographyCodeFxml);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void unselectAction() {
        for (GeographyCode code : tableData) {
            code.setSelected(false);
        }

    }

}
