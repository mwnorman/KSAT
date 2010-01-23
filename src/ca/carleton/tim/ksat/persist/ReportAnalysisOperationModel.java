/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, Mike Norman
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
package ca.carleton.tim.ksat.persist;

//javase imports
import java.io.File;
import java.io.StringReader;
import org.w3c.dom.Document;

//java eXtension imports
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//EclipseLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.sessions.UnitOfWork;

//domain import (KSAT)
import ca.carleton.tim.ksat.model.Analysis;

public class ReportAnalysisOperationModel extends AnalysisOperationModel {

    public static final String REPORT_XML_FORMAT = "xml";
    public static final String REPORT_HTML_FORMAT = "html";
    public static final String REPORT_CSV_FORMAT = "csv";
    
    static final String HTML_XSL =
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
      "<xsl:stylesheet " +
        "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " +
        "version=\"1.0\" " +
        ">" +
        "<xsl:template match=\"/\">" +
          "<html>" +
            "<body>" +
              "<h2>Anaylsis Report</h2>" +
              "<table border=\"1\" cellpadding=\"5\" cellspacing=\"2\">" +
                "<tr>" +
                  "<th colspan=\"2\" scope=\"colgroup\" bgcolor=\"#ee34df2\">Legend</th>" +
                "</tr>" +
                "<tr>" +
                  "<th scope=\"col\" align=\"left\" bgcolor=\"#9acd32\">Site</th>" +
                  "<th scope=\"col\" align=\"left\" bgcolor=\"#9acd32\">Url</th>" +
                "</tr>" +
              "<xsl:for-each select=\"analysis-report/analysis/sites/site\">" +
                "<tr>" +
                  "<td scope=\"row\"><xsl:value-of select=\"@id\"/></td>" +
                  "<td><xsl:value-of select=\"text()\"/></td>" +
                "</tr>" +
              "</xsl:for-each>" +
                "<tr>" +
                  "<th scope=\"col\" align=\"left\" bgcolor=\"#9acd32\">Keyword</th>" +
                  "<th scope=\"col\" align=\"left\" bgcolor=\"#9acd32\">Expression</th>" +
                "</tr>" +
              "<xsl:for-each select=\"analysis-report/analysis/keywords/keyword\">" +
                "<tr>" +
                  "<td scope=\"row\"><xsl:value-of select=\"@id\"/></td>" +
                  "<td><xsl:value-of select=\"text()\"/></td>" +
                "</tr>" +
              "</xsl:for-each>" +
              "</table>" +
            "</body>" +
          "</html>" +
        "</xsl:template>" +
      "</xsl:stylesheet>";
    
    protected String reportFormat;
    protected String analysisDescription;
    protected String reportDestination;

    public ReportAnalysisOperationModel() {
        super();
    }

    public void build(AnalysisBuilder builder, UnitOfWork uow) {
        try {
            Analysis reportingAnalysis = (Analysis)uow.executeQuery("findByDescription",
                Analysis.class, analysisDescription);
            if (reportingAnalysis != null) {
                AnalysisReport report = new AnalysisReport();
                report.setReportingAnalysis(reportingAnalysis);
                XMLContext context = new XMLContext(new AnalysisReportProject());
                XMLMarshaller marshaller = context.createMarshaller();
                File destination = new File(reportDestination);
                if (REPORT_XML_FORMAT.equalsIgnoreCase(reportFormat)) {
                    marshaller.marshal(report, new StreamResult(destination));
                }
                else if (REPORT_HTML_FORMAT.equalsIgnoreCase(reportFormat)) {
                    Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                    marshaller.marshal(report, doc);
                    StreamSource xslSource = new StreamSource(new StringReader(HTML_XSL));
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                    DOMSource domSource = new DOMSource(doc);
                    transformer.transform(domSource, new StreamResult(destination));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
