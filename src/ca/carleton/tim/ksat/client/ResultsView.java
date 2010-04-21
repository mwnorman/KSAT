package ca.carleton.tim.ksat.client;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;

public class ResultsView extends ViewPart {
    
    public ResultsView() {
    }

    public static final String ID = "ca.carleton.tim.ksat.client.views.results";

    protected Browser browser; 
    
    @Override
    public void createPartControl(Composite parent) {
        
        CTabFolder tabFolder = new CTabFolder(parent, SWT.BORDER);
        tabFolder.setTabPosition(SWT.BOTTOM);
        tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
        
        CTabItem htmlReportTab = new CTabItem(tabFolder, SWT.NONE);
        htmlReportTab.setText("HTML Report");
        browser = new Browser(tabFolder, SWT.NONE);
        htmlReportTab.setControl(browser);
        
        CTabItem sourceXmlTab = new CTabItem(tabFolder, SWT.NONE);
        sourceXmlTab.setText("Source XML");
        Text text = 
            new Text(tabFolder, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        sourceXmlTab.setControl(text);
    }

    @Override
    public void setFocus() {
    }

}
