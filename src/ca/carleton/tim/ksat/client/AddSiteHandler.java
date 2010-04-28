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
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

//Graphics (SWT/JFaces) imports
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

//RCP imports
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.window.Window;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Site;

public class AddSiteHandler extends AbstractHandler implements IHandler {

    protected HashSet<Site> additionalSites;
    protected HashSet<Site> selectedSites = new HashSet<Site>();

    @SuppressWarnings("unchecked")
    @Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
		AnalysesView analysesView = (AnalysesView) views.get(0);
		AnalysisAdapter currentAnalysis = analysesView.getCurrentAnalysis();
		List<Site> currentSites = currentAnalysis.getAnalysis().getSites();
		HashSet<Site> currentSitesSet = new HashSet<Site>(currentSites);
		Vector<Site> allSitesFromDB = 
		    analysesView.getCurrentDatabase().getSession().readAllObjects(Site.class);
		additionalSites = new HashSet<Site>(allSitesFromDB);
		additionalSites.removeAll(currentSitesSet);
		Shell activeShell = HandlerUtil.getActiveShell(event);
		AddSitesDialog dialog = new AddSitesDialog(activeShell, this);
        int status = dialog.open();
        if (status == Window.OK) {
            System.identityHashCode(selectedSites);
            // TODO - add the sites to currentAnalysis, persist to db ...
        }
		return null;
	}

    public void addSelectedSite(Site selectedSite) {
        selectedSites.add(selectedSite);
    }

}