package org.geogebra.web.full.gui.dialog.template;

import org.geogebra.common.main.App;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.web.html5.main.AppW;

import java.util.ArrayList;

public class TemplateChooserController {
    private ArrayList<TemplatePreviewCard> templates;
    private TemplatePreviewCard selected;

    /**
     * @param app see {@link AppW}
     */
    public TemplateChooserController(AppW app) {
        templates = new ArrayList<>();
        templates.add(new TemplatePreviewCard(app, null, false,
                new AsyncOperation<TemplatePreviewCard>() {

                    @Override
                    public void callback(TemplatePreviewCard card) {
                        setSelected(card);
                    }
                }));
        templates.add(new TemplatePreviewCard(app, null, true,
                new AsyncOperation<TemplatePreviewCard>() {

            @Override
            public void callback(TemplatePreviewCard card) {
                setSelected(card);
            }
        }));
        templates.add(new TemplatePreviewCard(app, null, true,
                new AsyncOperation<TemplatePreviewCard>() {

            @Override
            public void callback(TemplatePreviewCard card) {
                setSelected(card);
            }
        }));
        templates.add(new TemplatePreviewCard(app, null, true,
                new AsyncOperation<TemplatePreviewCard>() {

            @Override
            public void callback(TemplatePreviewCard card) {
                setSelected(card);
            }
        }));
        templates.add(new TemplatePreviewCard(app, null, true,
                new AsyncOperation<TemplatePreviewCard>() {

            @Override
            public void callback(TemplatePreviewCard card) {
                setSelected(card);
            }
        }));
        setSelected(templates.get(0));
    }

    /**
     * @return currently selected card
     */
    public TemplatePreviewCard getSelected() {
        return selected;
    }

    /**
     * Store selected template card
     * @param newSelected currently selected card
     */
    public void setSelected(TemplatePreviewCard newSelected) {
        if (selected != null) {
            this.selected.setSelected(false);
        }
        newSelected.setSelected(true);
        this.selected = newSelected;
    }

    /**
     * @return list of template cards
     */
    public ArrayList<TemplatePreviewCard> getTemplates() {
        return templates;
    }

    /**
     * Action to take when create btn clicked
     * @param app see {@link AppW}
     */
    public void onCreate(App app) {
        if (selected.getMaterial() == null) {
            app.fileNew();
        } else {
            selected.getController().loadOnlineFile();
        }
    }
}
