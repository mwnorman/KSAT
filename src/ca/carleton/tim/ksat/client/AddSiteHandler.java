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

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ca.carleton.tim.ksat.model.Site;

public class AddSiteHandler extends AbstractHandler implements IHandler {

    /**
     * @wbp.parser.entryPoint
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
        AnalysesView analysesView = (AnalysesView)views.get(0);
        AnalysisAdapter currentAnalysis = analysesView.getCurrentAnalysis();List<Site> currentSites = currentAnalysis.getAnalysis().getSites();
        HashSet<Site> currentSitesSet = new HashSet<Site>(currentSites);
        Vector<Site> allSites = 
            analysesView.getCurrentDatabase().getSession().readAllObjects(Site.class);
        HashSet<Site> allSitesSet = new HashSet<Site>(allSites);
        allSitesSet.removeAll(currentSitesSet);
        
        Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
        ListSelectionDialog lsd = new ListSelectionDialog(shell, allSitesSet, 
            new ArrayContentProvider(), new LabelProvider(),
            "Available sites to add to Analysis " +  currentAnalysis.getAnalysis().getDescription());
        lsd.setTitle("Select Site(s)");
        lsd.open();
        Object[] results = lsd.getResult();
        for (Object result : results) {
            System.out.println(result.toString());
        }
        return null;
    }

}
