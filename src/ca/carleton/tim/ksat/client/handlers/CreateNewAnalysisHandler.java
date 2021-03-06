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
package ca.carleton.tim.ksat.client.handlers;

//javase imports
import java.util.List;

//Graphics (SWT/JFace) imports
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

//RCP imports
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

//EclipseLink imports
import org.eclipse.persistence.sessions.UnitOfWork;

//KSAT domain imports
import ca.carleton.tim.ksat.client.AnalysisAdapter;
import ca.carleton.tim.ksat.client.KSATApplication;
import ca.carleton.tim.ksat.client.KSATRoot;
import ca.carleton.tim.ksat.client.dialogs.CreateNewAnalysisDialog;
import ca.carleton.tim.ksat.client.views.AnalysesView;
import ca.carleton.tim.ksat.model.Analysis;

public class CreateNewAnalysisHandler extends AbstractHandler implements IHandler {

	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell = HandlerUtil.getActiveShell(event);
		CreateNewAnalysisDialog dialog = new CreateNewAnalysisDialog(activeShell,
			"Create New Analysis","Enter name of new Analysis","", this);
        int status = dialog.open();
    	String name = dialog.getValue();
        if (status == Window.OK && name != null && name.length() > 0) {
        	KSATRoot root = KSATRoot.defaultInstance();
            Analysis newAnalysis = new Analysis();
            UnitOfWork uow = root.getCurrentSession().acquireUnitOfWork();
        	Analysis newAnalysisClone = (Analysis) uow.registerNewObject(newAnalysis);
        	newAnalysisClone.setDescription(name);
        	uow.commit();
            List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
            AnalysesView analysesView = (AnalysesView)views.get(0);
        	AnalysisAdapter newAdapter = new AnalysisAdapter(root.getCurrentDatabase());
        	newAdapter.setAnalysis(newAnalysis);
        	analysesView.setCurrentAdapter(newAdapter);
        	analysesView.analysesViewer.refresh(true);
        }
        return null;
    }

}