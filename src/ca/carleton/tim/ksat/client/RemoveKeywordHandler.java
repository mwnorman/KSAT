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

import java.net.URLDecoder;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.KeywordExpression;

public class RemoveKeywordHandler extends AbstractHandler implements IHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell activeShell = HandlerUtil.getActiveShell(event);
        List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID, KeywordsView.ID);
        AnalysesView analysesView = (AnalysesView)views.get(0);
        KeywordsView keywordsView = (KeywordsView)views.get(1);
        IStructuredSelection selection = (IStructuredSelection)keywordsView.getTableViewer().getSelection();
        KeywordExpression keywordToRemove = (KeywordExpression)selection.getFirstElement();
        String decodedExpression = "";
		try {
			decodedExpression = URLDecoder.decode(keywordToRemove.getExpression(), "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
        boolean confirm = MessageDialog.openConfirm(activeShell, "Remove Keyword Expression",
        	"Are you sure you wish to remove Expression\n" + decodedExpression + "?");
        if (confirm) {
            Analysis currentAnalysis = analysesView.getCurrentAnalysis();
            UnitOfWork uow = KSATRoot.defaultInstance().getCurrentSession().acquireUnitOfWork();
            Analysis currentAnalysisClone = (Analysis)uow.registerObject(currentAnalysis);
            KeywordExpression keywordToRemoveClone = 
            	(KeywordExpression)uow.registerObject(keywordToRemove);
            currentAnalysisClone.removeKeywordExpression(keywordToRemoveClone);
            uow.commit();
            keywordsView.setKeywords(currentAnalysis.getExpressions());
        }
       
        return null;
    }

}
