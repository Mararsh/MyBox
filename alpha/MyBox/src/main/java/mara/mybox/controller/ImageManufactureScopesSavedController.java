package mara.mybox.controller;

import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class ImageManufactureScopesSavedController extends ImageViewerController {

    protected ImageManufactureController imageController;
    protected ImageManufactureScopeController scopeController;

    @FXML
    protected Button deleteScopesButton, useScopeButton;
    @FXML
    protected ListView<ImageScope> scopesList;

    public void setParameters(ImageManufactureController parent) {
        this.parentController = parent;
        imageController = parent;
        scopeController = imageController.scopeController;
        baseName = imageController.baseName;
        baseTitle = imageController.baseTitle;
        sourceFile = imageController.sourceFile;
        imageInformation = imageController.imageInformation;
        image = imageController.image;
        initScopesBox();
        refreshStyle();

        loadScopes();
    }

    public void initScopesBox() {
        try {
            scopesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            scopesList.setCellFactory(new Callback<ListView<ImageScope>, ListCell<ImageScope>>() {
                @Override
                public ListCell<ImageScope> call(ListView<ImageScope> param) {
                    return new ImageScopeCell();
                }
            });

            scopesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        useScope();
                    }
                }
            });

            deleteScopesButton.disableProperty().bind(
                    scopesList.getSelectionModel().selectedItemProperty().isNull()
            );
            useScopeButton.disableProperty().bind(deleteScopesButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public class ImageScopeCell extends ListCell<ImageScope> {

        public ImageScopeCell() {
            setContentDisplay(ContentDisplay.LEFT);
        }

        @Override
        protected void updateItem(ImageScope item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getScopeType() == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            String icon;
            try {
                switch (item.getScopeType()) {
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
                    case RectangleColor:
                        icon = "iconRectangleFilled.png";
                        break;
                    case CircleColor:
                        icon = "iconCircleFilled.png";
                        break;
                    case EllipseColor:
                        icon = "iconEllipseFilled.png";
                        break;
                    case PolygonColor:
                        icon = "iconStarFilled.png";
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
                String s = item.getName();
                if (scope != null && s.equals(scope.getName())) {
                    setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    s = "** " + Languages.message("CurrentScope") + " " + s;
                } else {
                    setStyle("");
                }
                setGraphic(StyleTools.getIconImage(icon));
                setText(s);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                setText(null);
                setGraphic(null);
            }

        }
    }

    public void loadScopes() {
        scopesList.getItems().clear();
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            scopesList.getItems().clear();
            SingletonTask scopesTask = new SingletonTask<Void>(this) {
                List<ImageScope> list;

                @Override
                protected boolean handle() {
                    list = TableImageScope.read(sourceFile.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (list != null && !list.isEmpty()) {
                        scopesList.getItems().setAll(list);
//                        scopesList.getSelectionModel().selectFirst();
                    }
                }
            };
            start(scopesTask, false);
        }
    }

    @FXML
    public void deleteScopes() {
        List<ImageScope> selected = scopesList.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return TableImageScope.delete(selected);
                }

                @Override
                protected void whenSucceeded() {
                    for (ImageScope scope : selected) {
                        scopesList.getItems().remove(scope);
                    }
                    scopesList.refresh();
//                    loadScopes();
                }
            };
            parentController.start(task);
        }
    }

    @FXML
    public void clearScopes() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    TableImageScope.clearScopes(sourceFile.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    scopesList.getItems().clear();
                    scopesList.refresh();
                }
            };
            parentController.start(task);
        }
    }

    @FXML
    public void useScope() {
        ImageScope selected = scopesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        scope = selected;
        // Force listView to refresh
        // https://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes?r=SearchResults
        for (int i = 0; i < scopesList.getItems().size(); ++i) {
            scopesList.getItems().set(i, scopesList.getItems().get(i));
        }
        scopeController.showScope(scope);
        imageController.tabPane.getSelectionModel().select(imageController.scopeTab);
    }

    @FXML
    public void refreshScopes() {
        loadScopes();
    }

}
