package ca.carleton.tim.ksat.client;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;

import ca.carleton.tim.ksat.model.Site;
import org.eclipse.swt.layout.FillLayout;

public class AddSitesDialog extends Dialog {

    protected HashSet<Site> allSitesSet;
    protected CheckboxTableViewer tableViewer;

    /**
     * @wbp.parser.constructor
     */
    public AddSitesDialog(Shell parent) {
        super(parent);
        init();
    }

    public AddSitesDialog(IShellProvider parentShell) {
        super(parentShell);
        init();
    }
    
    @SuppressWarnings("unchecked")
    protected void init() {
        List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
        AnalysesView analysesView = (AnalysesView) views.get(0);
        AnalysisAdapter currentAnalysis = analysesView.getCurrentAnalysis();
        List<Site> currentSites = currentAnalysis.getAnalysis().getSites();
        HashSet<Site> currentSitesSet = new HashSet<Site>(currentSites);
        Vector<Site> allSites = analysesView.getCurrentDatabase().getSession()
                .readAllObjects(Site.class);
        allSitesSet = new HashSet<Site>(allSites);
        allSitesSet.removeAll(currentSitesSet);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.RESIZE | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite superComposite = (Composite) super.createDialogArea(parent);
        superComposite.setLayout(new GridLayout(1, false));
        return superComposite;
    }
    
}