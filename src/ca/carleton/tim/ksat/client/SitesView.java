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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
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
        final MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection();
                if (!selection.isEmpty()) {
                    selection.getFirstElement();
                    Action a1 = new Action("Edit Site ...") {};
                    mgr.add(a1);
                    mgr.add(new Separator());
                }
                Action a2 = new Action("Add Site") {};
                mgr.add(a2);
                Action a3 = new Action("Remove Site") {};
                mgr.add(a3);
            }
        });
        listViewer.getControl().setMenu(mgr.createContextMenu(listViewer.getControl()));
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
