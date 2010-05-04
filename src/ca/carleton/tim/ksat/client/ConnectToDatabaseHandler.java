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
import java.util.Vector;

//Graphics (SWT/JFace) imports
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

//RCP imports
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

//KSAT domain imports
import ca.carleton.tim.ksat.model.Analysis;

public class ConnectToDatabaseHandler extends AbstractHandler implements IHandler {

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	Shell activeShell = HandlerUtil.getActiveShell(event);
        IStructuredSelection currentSelection = 
            (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
        AnalysisDatabase analysisDatabase = (AnalysisDatabase)currentSelection.getFirstElement();
        try {
			analysisDatabase.connect();
	        Vector<Analysis> analyses = analysisDatabase.getSession().readAllObjects(Analysis.class);
	        List<AnalysisAdapter> analysisAdapters = new ArrayList<AnalysisAdapter>();
	        for (Analysis analysis : analyses ) {
	            AnalysisAdapter analysisAdapter = new AnalysisAdapter(analysisDatabase);
	            analysisAdapter.setAnalysis(analysis);
	        }
	        analysisDatabase.setAnalyses(analysisAdapters);
	        KSATApplication.resetViewsOnConnectToDatabase();
		}
        catch (Exception e) {
			Status status = new Status(IStatus.ERROR, AnalysesView.ID, e.getMessage(), e);
    		ErrorDialog.openError(activeShell, "Error connecting to Database", 
    				"Error connecting to Database", status);
		}
        return analysisDatabase;
    }

}
