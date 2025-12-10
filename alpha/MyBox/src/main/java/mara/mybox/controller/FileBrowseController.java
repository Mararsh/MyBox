package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.MenuTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableFileNameCell;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.cell.TableTimeCell;
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-12
 * @License Apache License Version 2.0
 */
public class FileBrowseController extends BaseController {

    protected ObservableList<FileInformation> tableData;
    protected FileSortMode sortMode;

    @FXML
    protected TableView<FileInformation> tableView;
    @FXML
    protected TableColumn<FileInformation, String> fileColumn, typeColumn;
    @FXML
    protected TableColumn<FileInformation, Long> sizeColumn, timeColumn;
    @FXML
    protected Label topLabel;
    @FXML
    protected Button refreshButton;

    public FileBrowseController() {
        baseTitle = message("BrowseFiles");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableData = FXCollections.observableArrayList();
            tableView.setItems(tableData);

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("suffix"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            sizeColumn.setCellFactory(new TableFileSizeCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            timeColumn.setCellFactory(new TableTimeCell());

            tableView.setOnMouseClicked((MouseEvent event) -> {
                boolean doubleClickToOpen = "DoubleClick".equals(
                        UserConfig.getString(baseName + "HowOpen", "leftClick"));
                if (event.getClickCount() > 1) {
                    if (doubleClickToOpen) {
                        openItem();
                    }
                } else {
                    if (!doubleClickToOpen) {
                        openItem();
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameter(BaseController parent) {
        try {
            parentController = parent;
            sortMode = FileSortMode.NameAsc;

            if (parentController instanceof BaseImageController) {
                fileColumn.setCellFactory(new TableFileNameCell(this));
            }

            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            if (parentController != null) {
                setFileType(parentController.getSourceFileType());
                baseName = parentController.baseName;
                sourceFile = parentController.sourceFile;
            }
            if (sourceFile == null) {
                topLabel.setText("");
                bottomLabel.setText("");
                openSourceButton.setDisable(true);
                refreshButton.setDisable(true);
                return;
            }
            openSourceButton.setDisable(false);
            refreshButton.setDisable(false);
            tableData.clear();
            tableView.getSortOrder().clear();

            topLabel.setText(message("Directory") + ": " + sourceFile.getParent());
            List<File> files = FileSortTools.siblingFiles(sourceFile, SourceFileType, sortMode);
            String info = message("Total") + ": ";
            if (files == null || files.isEmpty()) {
                info += "0";
            } else {
                info += files.size();
                for (File file : files) {
                    tableData.add(new FileInformation(file));
                }
            }
            bottomLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void openItem() {
        FileInformation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        String currentWhere = UserConfig.getString(baseName + "WhereOpen", "NewWindow");
        if ("SystemMethod".equals(currentWhere)) {
            browse(selected.getFile());
        } else if (parentController == null || "NewWindow".equals(currentWhere)) {
            ControllerTools.openTarget(selected.getFile().getAbsolutePath());
        } else if ("Pop".equals(currentWhere)) {
            ControllerTools.popTarget(parentController, selected.getFile().getAbsolutePath(), true);
        } else if ("Load".equals(currentWhere)) {
            parentController.selectSourceFile(selected.getFile());
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            List<MenuItem> items = MenuTools.initMenu(message("View"));

            MenuItem menu;

            menu = new MenuItem(message("How"));
            menu.setStyle(attributeTextStyle());
            items.add(menu);

            ToggleGroup howGroup = new ToggleGroup();
            String currentHow = UserConfig.getString(baseName + "HowOpen", "leftClick");

            RadioMenuItem doubleClickMenu = new RadioMenuItem(message("WhenDoubleClickNode"));
            doubleClickMenu.setSelected("DoubleClick".equals(currentHow));
            doubleClickMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "HowOpen", "DoubleClick");
                }
            });
            doubleClickMenu.setToggleGroup(howGroup);
            items.add(doubleClickMenu);

            RadioMenuItem leftClickMenu = new RadioMenuItem(message("WhenLeftClickNode"));
            leftClickMenu.setSelected(!"DoubleClick".equals(currentHow));
            leftClickMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "HowOpen", "leftClick");
                }
            });
            leftClickMenu.setToggleGroup(howGroup);
            items.add(leftClickMenu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Where"));
            menu.setStyle(attributeTextStyle());
            items.add(menu);

            ToggleGroup whereGroup = new ToggleGroup();
            String currentWhere = UserConfig.getString(baseName + "WhereOpen", "NewWindow");

            RadioMenuItem newWindowMenu = new RadioMenuItem(message("OpenInNewWindow"));
            newWindowMenu.setSelected("NewWindow".equals(currentWhere));
            newWindowMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "WhereOpen", "NewWindow");
                }
            });
            newWindowMenu.setToggleGroup(whereGroup);
            items.add(newWindowMenu);

            RadioMenuItem loadMenu = new RadioMenuItem(message("Load"));
            loadMenu.setSelected("Load".equals(currentWhere));
            loadMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "WhereOpen", "Load");
                }
            });
            loadMenu.setToggleGroup(whereGroup);
            items.add(loadMenu);

            RadioMenuItem popOpenMenu = new RadioMenuItem(message("Pop"));
            popOpenMenu.setSelected("Pop".equals(currentWhere));
            popOpenMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "WhereOpen", "Pop");
                }
            });
            popOpenMenu.setToggleGroup(whereGroup);
            items.add(popOpenMenu);

            RadioMenuItem systemMethodMenu = new RadioMenuItem(message("SystemMethod"));
            systemMethodMenu.setSelected("SystemMethod".equals(currentWhere));
            systemMethodMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "WhereOpen", "SystemMethod");
                }
            });
            systemMethodMenu.setToggleGroup(whereGroup);
            items.add(systemMethodMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }


    /*
        static
     */
    public static FileBrowseController open(BaseController parent) {
        try {
            if (parent == null) {
                return null;
            }
            FileBrowseController controller = (FileBrowseController) WindowTools.referredTopStage(
                    parent, Fxmls.FileBrowseFxml);
            if (controller != null) {
                controller.setParameter(parent);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
