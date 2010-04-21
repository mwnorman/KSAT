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

//javase imports
import java.util.ArrayList;
import java.util.List;

//Graphics (JFaces/SWT) imports
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

//RCP imports
import org.eclipse.ui.part.ViewPart;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Site;

public class SitesView extends ViewPart {

    public static final String ID = "ca.carleton.tim.ksat.client.views.sites";

    protected List<Site> input = new ArrayList<Site>();
    protected ListViewer listViewer;
    
    public SitesView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));
        listViewer = new ListViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        listViewer.setLabelProvider(new LabelProvider());
        listViewer.setContentProvider(new ListContentProvider());
        listViewer.setInput(input);

        hookContextMenu(); 
    }
    /**
     * Setup Context Menu.
     */
    private void hookContextMenu() {
        MenuManager menuManager = new MenuManager("#PopupMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu menu = menuManager.createContextMenu(listViewer.getControl());
        listViewer.getControl().setMenu(menu);
        menuManager.createContextMenu(listViewer.getControl());
        getSite().registerContextMenu(ID, menuManager, listViewer);
    }

    @Override
    public void setFocus() {
    }

    public void setSites(java.util.List<Site> sites) {
        input.clear();
        input.addAll(sites);
        listViewer.refresh(true);
    }
}
