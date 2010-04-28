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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.Site;

public class AddSiteHandler extends AbstractHandler implements IHandler {

	private HashSet<Site> allSitesSet;

    @SuppressWarnings("unchecked")
    @Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
		AnalysesView analysesView = (AnalysesView) views.get(0);
		AnalysisAdapter currentAnalysis = analysesView.getCurrentAnalysis();
		List<Site> currentSites = currentAnalysis.getAnalysis().getSites();
		HashSet<Site> currentSitesSet = new HashSet<Site>(currentSites);
		Vector<Site> allSitesFromDB = analysesView.getCurrentDatabase().getSession()
				.readAllObjects(Site.class);
		allSitesSet = new HashSet<Site>(allSitesFromDB);
		allSitesSet.removeAll(currentSitesSet);

		AddSitesWizard wizard = new AddSitesWizard(allSitesSet, allSitesFromDB);
		NonmodalWizardDialog dialog = new NonmodalWizardDialog(HandlerUtil.getActiveShell(event), wizard);
        dialog.setHelpAvailable(false);
        dialog.open();
        List<Site> newSites = dialog.getResults();
        for (Site newSite : newSites) {
            currentAnalysis.getAnalysis().addSite(newSite);
        }

		return null;
	}
    static class NonmodalWizardDialog extends WizardDialog {
        NonmodalWizardDialog(Shell shell, IWizard wizard) {
            super(shell, wizard);
            setShellStyle(SWT.SHELL_TRIM | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
            setBlockOnOpen(false);
        }
        List<Site> getResults() {
            AddSitesWizard addSitesWizard = (AddSitesWizard)getWizard();
            SitesFromDBPage sitesFromDBPage = (SitesFromDBPage)addSitesWizard.getPages()[0];
            TableItem[] selections = sitesFromDBPage.table.getSelection();
            for (TableItem tableItem : selections) {
                Object data = tableItem.getData();
            }
            return null;
        }
    }

}
