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

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import ca.carleton.tim.ksat.impl.GoogleRESTSearcher;
import ca.carleton.tim.ksat.impl.GoogleRESTSearcher.KeywordPageCount;
import ca.carleton.tim.ksat.impl.GoogleRESTSearcher.SitePageCount;
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.AnalysisResult;

public class RunAnalysisHandler extends AbstractHandler implements IHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        KSATRoot root = KSATRoot.defaultInstance();
        final Analysis currentAnalysis = root.getCurrentAnalysis();
        final UnitOfWork uow = root.getCurrentSession().acquireUnitOfWork();
        final Analysis currAnalysisClone = (Analysis) uow.registerObject(currentAnalysis);
        final GoogleRESTSearcher googleRESTSearcher = new GoogleRESTSearcher(currAnalysisClone);
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            public void run() {
	            Map<String, SitePageCount> rESTResults = googleRESTSearcher.getRESTResults();
	            AnalysisResult result = (AnalysisResult)uow.registerNewObject(new AnalysisResult());
	            result.setOwner(currentAnalysis); 
	            result.setDateTime(new Date(System.currentTimeMillis()));
	            uow.assignSequenceNumber(result);
	            StringBuilder sb = new StringBuilder(200);
	            if (!rESTResults.isEmpty()) {
	                sb.append("<raw-result ");
	                sb.append("id=\"");
	                sb.append(result.getId());
	                sb.append("\"");
	                sb.append(" timestamp=\"");
	                sb.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(result.getDateTime()));
	                sb.append("\">");
	                for (Map.Entry<String, SitePageCount> me : rESTResults.entrySet()) {
	                    SitePageCount spc = me.getValue();
	                    sb.append("<site-page-counts site-id=\"");
	                    sb.append(spc.getSite().getId());
	                    sb.append("\" estimated-total-pages=\"");
	                    long sitePageCount = spc.getSitePageCount();
	                    sb.append(sitePageCount);
	                    if (sitePageCount > 0) {
	                        sb.append("\">");
	                        for (KeywordPageCount kpc : spc.getPageCounts()) {
	                            sb.append("<keyword-page-count keyword-id=\"");
	                            sb.append(kpc.getExpression().getId());
	                            sb.append("\">");
	                            sb.append(kpc.getPageCount());
	                            sb.append("</keyword-page-count>");
	                        }
	                        sb.append("</site-page-counts>");
	                    }
	                    else {
	                        sb.append("\"/>");
	                    }
	                }
	                sb.append("</raw-result>");
	            }
	            else {
	                sb.append("<raw-result/>");
	            }
	            try {
	                Document tmp = 
	                    XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
	                DocumentFragment fragment = tmp.createDocumentFragment();
	                DOMResult dr = new DOMResult(fragment);
	                Transformer transformer = TransformerFactory.newInstance().newTransformer();
	                Source source = new StreamSource(new StringReader(sb.toString()));
	                transformer.transform(source, dr);
	                result.setRawResults(dr.getNode());
	            }
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	            if (result.getRawResults() != null) {
	            	currAnalysisClone.addAnalysisResult(result);
	            	uow.commit();
	            }
            }
        });
        uow.release();
		List<IViewPart> views = KSATApplication.getViews(AnalysesView.ID);
        AnalysesView analysesView = (AnalysesView)views.get(0);
		analysesView.analysesViewer.refresh(true);
        return null;
    }

}