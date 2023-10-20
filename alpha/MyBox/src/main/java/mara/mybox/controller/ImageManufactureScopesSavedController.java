package mara.mybox.controller;

import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class ImageManufactureScopesSavedController extends BaseSysTableController<ImageScope> {

    protected TableImageScope tableImageScope;
    protected ImageManufactureController imageController;
    protected ImageManufactureScopeController scopeController;

    @FXML
    protected TableColumn<ImageScope, String> nameColumn, colorTypeColumn, fileColumn;
    @FXML
    protected TableColumn<ImageScope, ScopeType> scopeTypeColumn;
    @FXML
    protected TableColumn<ImageScope, Boolean> areaExcludeColumn, colorExcludeColumn;
    @FXML
    protected TableColumn<ImageScope, Date> modifyColumn, createColumn;
    @FXML
    protected Button useButton;
    @FXML
    protected CheckBox shareCheck;
    @FXML
    protected Label fileLabel;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
            scopeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("scopeType"));
            scopeTypeColumn.setCellFactory(new Callback<TableColumn<ImageScope, ScopeType>, TableCell<ImageScope, ScopeType>>() {
                @Override
                public TableCell<ImageScope, ScopeType> call(TableColumn<ImageScope, ScopeType> param) {
                    TableCell<ImageScope, ScopeType> cell = new TableCell<ImageScope, ScopeType>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        public void updateItem(ScopeType item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(null);
                            setGraphic(null);
                            if (empty || item == null) {
                                return;
                            }
                            String icon;
                            try {
                                switch (item) {
                                    case Rectangle:
                                        icon = "iconRectangle.png";
                                        break;
                                    case Circle:
                                        icon = "iconCircle.png";
                                        break;
                                    case Ellipse:
                                        icon = "iconEllipse.png";
                                        break;
                                    case Polygon:
                                        icon = "iconStar.png";
                                        break;
                                    case Color:
                                        icon = "iconColorWheel.png";
                                        break;
                                    case Matting:
                                        icon = "iconColorFill.png";
                                        break;
                                    case Outline:
                                        icon = "IconButterfly.png";
                                        break;
                                    default:
                                        return;
                                }
                                setGraphic(StyleTools.getIconImageView(icon));
                            } catch (Exception e) {
                            }
                        }
                    };
                    return cell;
                }
            });
            colorTypeColumn.setCellValueFactory(new PropertyValueFactory<>("colorTypeName"));
            areaExcludeColumn.setCellValueFactory(new PropertyValueFactory<>("areaExcluded"));
            areaExcludeColumn.setCellFactory(new TableBooleanCell());
            colorExcludeColumn.setCellValueFactory(new PropertyValueFactory<>("colorExcluded"));
            colorExcludeColumn.setCellFactory(new TableBooleanCell());

            modifyColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifyColumn.setCellFactory(new TableDateCell());
            createColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            createColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setTableDefinition() {
        tableImageScope = new TableImageScope();
        tableDefinition = tableImageScope;
    }

    public void setParameters(ImageManufactureController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            this.parentController = parent;
            imageController = parent;
            scopeController = imageController.scopeController;
            baseName = imageController.baseName;
            baseTitle = imageController.baseTitle;
            sourceFile = imageController.sourceFile;

            if (sourceFile != null) {
                fileLabel.setText(sourceFile.getAbsolutePath());
                setTitle(baseTitle + " - " + sourceFile.getAbsolutePath());
            }

            initScopesBox();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initScopesBox() {
        try {
            loadInBackground = true;
            orderColumns = " modify_time DESC ";
            queryConditions = null;

            shareCheck.setSelected(UserConfig.getBoolean(baseName + "ShareInAllImages", true));
            shareCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "ShareInAllImages", shareCheck.isSelected());
                    checkQueryConditions();
                    loadTableData();
                }
            });

            checkQueryConditions();
            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkQueryConditions() {
        if (shareCheck.isSelected()) {
            queryConditions = null;
        } else {
            if (sourceFile == null) {
                queryConditions = " image_location='Unknown' ";
            } else {
                queryConditions = " image_location='" + sourceFile.getAbsolutePath() + "' ";
            }
        }
    }

    protected boolean validSource() {
        return scopeController != null
                && imageController != null
                && imageController.isShowing();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean none = isNoneSelected();
        useButton.setDisable(none || !validSource());
    }

    @Override
    public void itemDoubleClicked() {
        useScope();
    }

    @FXML
    public void useScope() {
        if (!validSource()) {
            popError(message("InvalidData"));
            return;
        }
        ImageScope selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        scopeController.showScope(selected);
        imageController.tabPane.getSelectionModel().select(imageController.scopeTab);
        close();
    }

    /*
        static
     */
    public static ImageManufactureScopesSavedController load(ImageManufactureController parent) {
        try {
            ImageManufactureScopesSavedController controller = (ImageManufactureScopesSavedController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageManufactureScopeSavedFxml, false);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
}
