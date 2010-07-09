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

//EclipseLink imports
import org.eclipse.persistence.sessions.UnitOfWork;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.KeywordExpression;

public class AddKeywordsHandler extends AbstractHandler implements IHandler {

    protected HashSet<KeywordExpression> additionalKeywords;
    protected HashSet<KeywordExpression> selectedKeywords = new HashSet<KeywordExpression>();

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID, KeywordsView.ID);
    	AnalysesView analysesView = (AnalysesView)views.get(0);
        Analysis currentAnalysis = analysesView.getCurrentAnalysis();
        List<KeywordExpression> currentKeywords = currentAnalysis.getExpressions();
        List<KeywordExpression> allKeywordsFromDB = 
            KSATRoot.defaultInstance().getCurrentSession().readAllObjects(KeywordExpression.class);
        additionalKeywords = new HashSet<KeywordExpression>(allKeywordsFromDB);
        additionalKeywords.removeAll(currentKeywords);
        Shell activeShell = HandlerUtil.getActiveShell(event);
        AddKeywordsDialog dialog = new AddKeywordsDialog(activeShell, this, allKeywordsFromDB,
            additionalKeywords);
        int status = dialog.open();
        if (status == Window.OK) {
            KeywordsView keywordsView = (KeywordsView)views.get(1);
            if (selectedKeywords.size() > 0) {
                UnitOfWork uow = KSATRoot.defaultInstance().getCurrentSession().acquireUnitOfWork();
                Analysis currentAnalysisClone = (Analysis)uow.registerObject(currentAnalysis);
                Vector<KeywordExpression> selectedKeywordsClones = 
                    uow.registerAllObjects(selectedKeywords);
                for (KeywordExpression expression : selectedKeywordsClones) {
                    currentAnalysisClone.addKeywordExpression(expression);
                }
                uow.commit();
                keywordsView.setKeywords(currentAnalysis.getExpressions());
            }
        }
        return null;
    }

    public void addSelectedExpression(KeywordExpression selectedExpression) {
        selectedKeywords.add(selectedExpression);
    }

}