package ca.carleton.tim.keywordanalyzer.plugin.prefs;

import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class KeywordAnalyzerPropertiesPages implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench arg0) {
    }

    @Override
    public Point computeSize() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean okToLeave() {
        return false;
    }

    @Override
    public boolean performCancel() {
        return false;
    }

    @Override
    public boolean performOk() {
        return false;
    }

    @Override
    public void setContainer(IPreferencePageContainer arg0) {
    }

    @Override
    public void setSize(Point arg0) {
    }

    @Override
    public void createControl(Composite arg0) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Control getControl() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void performHelp() {
    }

    @Override
    public void setDescription(String arg0) {
    }

    @Override
    public void setImageDescriptor(ImageDescriptor arg0) {
    }

    @Override
    public void setTitle(String arg0) {
    }

    @Override
    public void setVisible(boolean arg0) {
    }
}