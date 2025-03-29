package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageConvolution extends BaseController {

    protected SimpleBooleanProperty doubleClickedNotify;

    @FXML
    protected ListView<ConvolutionKernel> listView;

    @Override
    public void initControls() {
        try {
            super.initControls();

            doubleClickedNotify = new SimpleBooleanProperty();

            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            listView.setCellFactory(new Callback<ListView<ConvolutionKernel>, ListCell<ConvolutionKernel>>() {
                @Override
                public ListCell<ConvolutionKernel> call(ListView<ConvolutionKernel> param) {

                    ListCell<ConvolutionKernel> cell = new ListCell<ConvolutionKernel>() {
                        @Override
                        public void updateItem(ConvolutionKernel item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(null);
                            setGraphic(null);
                            if (empty || item == null) {
                                return;
                            }
                            setText(item.getName());
                        }
                    };
                    return cell;
                }
            });
            listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        doubleClickedNotify.set(!doubleClickedNotify.get());
                    }
                }
            });

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            List<ConvolutionKernel> kernels;

            @Override
            protected boolean handle() {
                kernels = TableConvolutionKernel.read();
                return true;
            }

            @Override
            protected void whenSucceeded() {
                listView.getItems().setAll(kernels);
            }
        };
        start(task);
    }

    @FXML
    public void manageAction() {
        popStage(Fxmls.ConvolutionKernelManagerFxml);
        setIconified(true);
    }

    public ConvolutionKernel pickValues() {
        ConvolutionKernel kernel = listView.getSelectionModel().getSelectedItem();
        if (kernel == null) {
            popError(message("SelectToHandle"));
            return null;
        }
        return kernel;
    }

}
