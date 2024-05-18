package mara.mybox.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public class PptViewController extends BaseFileImagesController {

    @FXML
    protected TextArea slideArea, notesArea, masterArea, commentsArea;
    @FXML
    protected Label slideLabel, notesLabel, masterLabel, commentsLabel;
    @FXML
    protected VBox imageBox;

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

            imageBox.disableProperty().bind(imageController.imageView.imageProperty().isNull());
            leftPane.disableProperty().bind(imageController.imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.debug(e);
        }
    }

    public void loadInformation() {
        if (task != null) {
            task.cancel();
        }
        if (sourceFile == null) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                setTotalPages(0);
                try (SlideShow ppt = SlideShowFactory.create(sourceFile)) {
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
                loadThumbs();
            }

        };
        start(task, message("LoadingFileInfo"));
    }

    @Override
    protected void loadPage() {
        if (task != null) {
            task.cancel();
        }
        notesArea.clear();
        notesLabel.setText("");
        slideArea.clear();
        slideLabel.setText("");
        masterArea.clear();
        masterLabel.setText("");
        commentsArea.clear();
        commentsLabel.setText("");
        initCurrentPage();
        if (sourceFile == null) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private String slideTexts, notes, master, comments;
            private Image image;

            @Override
            protected boolean handle() {
                image = null;
                slideTexts = "";
                notes = "";
                try (SlideShow ppt = SlideShowFactory.create(sourceFile)) {
                    Slide slide = (Slide) (ppt.getSlides().get(frameIndex));
                    int width = ppt.getPageSize().width;
                    int height = ppt.getPageSize().height;
                    BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = slideImage.createGraphics();
                    if (AppVariables.ImageHints != null) {
                        g.addRenderingHints(AppVariables.ImageHints);
                    }
                    slide.draw(g);
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
                    extractor.setNotesByDefault(false);
                    extractor.setMasterByDefault(true);
                    master = extractor.getText(slide);
                    extractor.setMasterByDefault(false);
                    extractor.setCommentsByDefault(true);
                    comments = extractor.getText(slide);
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
                notesLabel.setText(message("CharactersNumber") + ": " + notes.length());
                slideArea.setText(slideTexts);
                slideLabel.setText(message("CharactersNumber") + ": " + slideTexts.length());
                masterArea.setText(master);
                masterLabel.setText(message("CharactersNumber") + ": " + master.length());
                commentsArea.setText(comments);
                commentsLabel.setText(message("CharactersNumber") + ": " + comments.length());
            }
        };
        start(task, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
    }

    @Override
    protected boolean loadThumbs(List<Integer> missed) {
        try (SlideShow ppt = SlideShowFactory.create(sourceFile)) {
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
                Graphics2D g = slideImage.createGraphics();
                if (AppVariables.ImageHints != null) {
                    g.addRenderingHints(AppVariables.ImageHints);
                }
                slide.draw(g);
                if (slideImage.getWidth() > thumbWidth) {
                    slideImage = ScaleTools.scaleImageWidthKeep(slideImage, thumbWidth);
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

    @FXML
    @Override
    public void playAction() {
        ImagesPlayController.playFile(sourceFile);
    }

    /*
        static
     */
    public static PptViewController open() {
        try {
            PptViewController controller = (PptViewController) WindowTools.openStage(Fxmls.PptViewFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static PptViewController openFile(File file) {
        PptViewController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

}
