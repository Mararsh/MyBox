package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public class PptViewController extends BaseFileImagesViewController {

    @FXML
    protected TextArea notesArea, slideArea;
    @FXML
    protected Label notesLabel, slideLabel;
    @FXML
    protected CheckBox notesCheck;
    @FXML
    protected VBox textsBox;
    @FXML
    protected TitledPane notesPane;

    public PptViewController() {
        baseTitle = message("PptView");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PPTS, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notesCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayNotes", true));
            notesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkNotes();
                    UserConfig.setBoolean(baseName + "DisplayNotes", notesCheck.isSelected());
                }
            });
            checkNotes();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkNotes() {
        if (notesCheck.isSelected()) {
            if (!textsBox.getChildren().contains(notesPane)) {
                textsBox.getChildren().add(notesPane);
            }
        } else {
            if (textsBox.getChildren().contains(notesPane)) {
                textsBox.getChildren().remove(notesPane);
            }
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        loadFile(file, 0);
    }

    public void loadFile(File file, int page) {
        try {
            initPage(file, page);
            if (file == null) {
                return;
            }
            loadInformation();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void loadInformation() {
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
                    setTotalPages(0);
                    try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
                        setTotalPages(ppt.getSlides().size());
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return framesNumber > 0;
                }

                @Override
                protected void whenSucceeded() {
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= framesNumber; i++) {
                        pages.add(i + "");
                    }
                    isSettingValues = true;
                    pageSelector.getItems().clear();
                    pageSelector.getItems().setAll(pages);
                    pageSelector.setValue("1");
                    pageLabel.setText("/" + framesNumber);
                    isSettingValues = false;
                    initCurrentPage();
                    loadPage();
                    checkThumbs();
                }

            };
            start(task, message("LoadingFileInfo"));
        }
    }

    @Override
    protected void loadPage() {
        notesArea.clear();
        notesLabel.setText("");
        slideArea.clear();
        slideLabel.setText("");
        initCurrentPage();
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private String slideTexts, notes;

                @Override
                protected boolean handle() {
                    image = null;
                    slideTexts = "";
                    notes = "";
                    try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
                        Slide slide = (Slide) (ppt.getSlides().get(frameIndex));
                        int width = ppt.getPageSize().width;
                        int height = ppt.getPageSize().height;
                        BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        slide.draw(slideImage.createGraphics());
                        if (dpi != 72) {
                            slideImage = ScaleTools.scaleImageByScale(slideImage, dpi / 72f);
                        }
                        image = SwingFXUtils.toFXImage(slideImage, null);

                        SlideShowExtractor extractor = new SlideShowExtractor(ppt);
                        extractor.setSlidesByDefault(true);
                        extractor.setMasterByDefault(false);
                        extractor.setNotesByDefault(false);
                        extractor.setCommentsByDefault(false);
                        slideTexts = extractor.getText(slide);
                        extractor.setSlidesByDefault(false);
                        extractor.setNotesByDefault(true);
                        notes = extractor.getText(slide);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    setImage(image, percent);
                    notesArea.setText(notes);
                    notesLabel.setText(message("Count") + ": " + notes.length());
                    slideArea.setText(slideTexts);
                    slideLabel.setText(message("Count") + ": " + slideTexts.length());
                }
            };
            start(task, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
        }
    }

    @Override
    protected boolean loadThumbs(List<Integer> missed) {
        try ( SlideShow ppt = SlideShowFactory.create(sourceFile)) {
            List<Slide> slides = ppt.getSlides();
            int width = ppt.getPageSize().width;
            int height = ppt.getPageSize().height;
            for (Integer index : missed) {
                if (thumbTask == null || thumbTask.isCancelled()) {
                    break;
                }
                ImageView view = (ImageView) thumbBox.getChildren().get(2 * index);
                if (view.getImage() != null) {
                    continue;
                }
                Slide slide = slides.get(index);
                BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                slide.draw(slideImage.createGraphics());
                if (slideImage.getWidth() > ThumbWidth) {
                    slideImage = ScaleTools.scaleImageWidthKeep(slideImage, ThumbWidth);
                }
                Image thumb = SwingFXUtils.toFXImage(slideImage, null);
                view.setImage(thumb);
                view.setFitHeight(view.getImage().getHeight());
            }
            ppt.close();
        } catch (Exception e) {
            thumbTask.setError(e.toString());
            MyBoxLog.debug(e);
            return false;
        }
        return true;
    }

}
