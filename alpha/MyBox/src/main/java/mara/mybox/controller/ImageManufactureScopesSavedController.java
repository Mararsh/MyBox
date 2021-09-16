package mara.mybox.controller;

import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
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

        private final ImageView view;

        public ImageScopeCell() {
            setContentDisplay(ContentDisplay.LEFT);
            view = new ImageView();
            view.setPreserveRatio(true);
            view.setFitWidth(20);
        }

        @Override
        protected void updateItem(ImageScope item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getScopeType() == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Image icon;
            try {
                switch (item.getScopeType()) {
                    case Rectangle:
                        icon = new Image(StyleTools.getIcon("iconRectangle.png"));
                        break;
                    case Circle:
                        icon = new Image(StyleTools.getIcon("iconCircle.png"));
                        break;
                    case Ellipse:
                        icon = new Image(StyleTools.getIcon("iconEllipse.png"));
                        break;
                    case Polygon:
                        icon = new Image(StyleTools.getIcon("iconStar.png"));
                        break;
                    case RectangleColor:
                        icon = new Image(StyleTools.getIcon("iconRectangleFilled.png"));
                        break;
                    case CircleColor:
                        icon = new Image(StyleTools.getIcon("iconCircleFilled.png"));
                        break;
                    case EllipseColor:
                        icon = new Image(StyleTools.getIcon("iconEllipseFilled.png"));
                        break;
                    case PolygonColor:
                        icon = new Image(StyleTools.getIcon("iconStarFilled.png"));
                        break;
                    case Color:
                        icon = new Image(StyleTools.getIcon("iconColorWheel.png"));
                        break;
                    case Matting:
                        icon = new Image(StyleTools.getIcon("iconColorFill.png"));
                        break;
                    case Outline:
                        icon = new Image(StyleTools.getIcon("IconButterfly.png"));
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
                view.setImage(icon);
                setGraphic(view);
                setText(s);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                setText(null);
                setGraphic(null);
            }

        }
    }

    public void loadScopes() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            scopesList.getItems().clear();
            task = new SingletonTask<Void>() {
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
            parentController.start(task);
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
            task = new SingletonTask<Void>() {

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
            task = new SingletonTask<Void>() {
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
    }

    @FXML
    public void refreshScopes() {
        loadScopes();
    }

}
