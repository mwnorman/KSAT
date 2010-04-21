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
import java.util.List;
import java.util.Vector;

//RCP imports
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

//KSAT domain imports
import ca.carleton.tim.ksat.client.AnalysesView.KSATInvisibleRoot;
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.AnalysisResult;

public class AnalysesContentProvider implements IStructuredContentProvider, ITreeContentProvider {

    protected IViewSite viewSite;
    
    public AnalysesContentProvider(IViewSite viewSite) {
        super();
        this.viewSite = viewSite;
    }

    @SuppressWarnings("unchecked")
    public Object[] getChildren(Object parent) {
        if (parent == viewSite) {
            return KSATInvisibleRoot.defaultInstance().children;
        }
        else if (parent == KSATRoot.defaultInstance()) {
            return KSATRoot.defaultInstance().databases.toArray();
        }
        else if (parent instanceof AnalysisDatabase) {
            AnalysisDatabase database = (AnalysisDatabase)parent;
            Vector<Analysis> analyses = database.getSession().readAllObjects(Analysis.class);
            int len = analyses.size();
            Object[] children = new Object[len];
            for (int i = 0; i < len; i++) {
                AnalysisAdapter analysisAdapter = new AnalysisAdapter(database);
                analysisAdapter.setAnalysis(analyses.get(i));
                children[i] = analysisAdapter;
            }
            return children;
        }
        else if (parent instanceof AnalysisAdapter) {
            AnalysisAdapter analysisAdapter = (AnalysisAdapter)parent;
            List<AnalysisResult> results = analysisAdapter.getAnalysis().getResults();
            return results.toArray();
        }
        return null;
    }

    public Object getParent(Object element) {
        if (element == KSATRoot.defaultInstance()) {
            return KSATInvisibleRoot.defaultInstance();
        }
        else if (element instanceof AnalysisDatabase) {
            return KSATRoot.defaultInstance();
        }
        else if (element instanceof AnalysisAdapter) {
            return ((AnalysisAdapter)element).getParent();
        }
        else if (element instanceof AnalysisResult) {
            return ((AnalysisResult)element).getOwner();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element == KSATRoot.defaultInstance()) {
            return KSATRoot.defaultInstance().getDatabases().size() > 0;
        }
        else if (element instanceof AnalysisDatabase) {
            return ((AnalysisDatabase)element).getAnalyses().size() > 0;
        }
        else if (element instanceof AnalysisAdapter) {
            return ((AnalysisAdapter)element).getAnalysis().getResults().size() > 0;
        }
        return false;
    }

    public Object[] getElements(Object parent) {
        if (parent == null) {
            return null;
        }
        return getChildren(parent);
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}