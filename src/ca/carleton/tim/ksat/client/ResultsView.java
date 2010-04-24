/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2010, Mike Norman
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
 * USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
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
    protected Text text; 
    
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
        text = new Text(tabFolder, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        sourceXmlTab.setControl(text);
    }

    @Override
    public void setFocus() {
    }

}