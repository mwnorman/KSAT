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

//KSAT imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.AnalysisResult;

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
              "<h2>Analysis Report</h2>" +
              "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\">" +
              "<tr>" +
              "<th colspan=\"2\" scope=\"colgroup\" bgcolor=\"#2efbdf1\">Legend</th>" +
              "</tr>" +
              "<tr>" +
              "<td width=\"50%\">" +              
              "<table border=\"1\" cellpadding=\"1\" cellspacing=\"2\">" +
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
              "</table>" + 
              "</td>" +
              "<td width=\"50%\">" +   
              "<table border=\"1\" cellpadding=\"1\" cellspacing=\"2\">" +
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
              "</td>" +
              "</tr>" +
              "</table>" +
              "<table border=\"1\" cellpadding=\"1\" cellspacing=\"2\">" +
                "<xsl:variable name=\"numKeywords\" select=\"count(//analysis-report/analysis/keywords/keyword)\"/>" +
                "<xsl:variable name=\"numKeywordsPlus\" select=\"$numKeywords+1\"/>" +
                "<colgroup></colgroup>" +
                "<colgroup span=\"{$numKeywords}/\"></colgroup>" +
                "<tr>" +
                  "<th colspan=\"{$numKeywordsPlus}\" scope=\"colgroup\" bgcolor=\"#9acd32\">Results</th>" +
                "</tr>" +
                "<xsl:for-each select=\"//analysis-report/analysis/results/result\">" +
                  "<tr>" +
                    "<th colspan=\"{$numKeywordsPlus}\" scope=\"colgroup\" bgcolor=\"#9acd32\">" +
                      "Result <xsl:value-of select=\"@id\"/>" +
                      " (run time=<xsl:value-of select=\"@timestamp\"/>)</th>" +
                  "</tr>" +
                  "<tr>" +
                    "<th rowspan=\"2\">Site</th>" +
                    "<th colspan=\"{$numKeywords}\" bgcolor=\"#9acd32\">Page Counts</th>" +
                  "</tr>" +
                  "<tr>" +
                    "<xsl:for-each select=\"//analysis-report/analysis/keywords/keyword\">" +
                      "<th>keyword <xsl:value-of select=\"@id\"/></th>" +
                    "</xsl:for-each>" +
                  "</tr>" +
                  "<xsl:for-each select=\"//analysis-report/analysis/results/result/site-page-counts\">" +
                    "<tr>" +
                      "<xsl:choose>" +
                        "<xsl:when test=\"@estimated-total-pages='-1'\">" +
                          "<td bgcolor=\"red\"><xsl:value-of select=\"@site-id\"/> (site not responding)</td>" +
                            "<xsl:call-template name=\"printEmptyCells\">" +
                              "<xsl:with-param name=\"i\">" +
                                "<xsl:value-of select=\"number(1)\"/>" +
                              "</xsl:with-param>" +
                              "<xsl:with-param name=\"count\">" +
                                "<xsl:value-of select=\"$numKeywords\"/>" +
                              "</xsl:with-param>" +
                            "</xsl:call-template>" +
                        "</xsl:when>" +
                        "<xsl:otherwise>" +
                          "<td><xsl:value-of select=\"@site-id\"/>" +
                            " (estimated total number of pages=<xsl:value-of select=\"@estimated-total-pages\"/>)</td>" +
                          "</xsl:otherwise>" +
                       "</xsl:choose>" +
                      "<xsl:for-each select=\"./keyword-page-count\">" +
                        "<td><xsl:value-of select=\"text()\"/></td>" +
                      "</xsl:for-each>" +
                    "</tr>" +
                  "</xsl:for-each>" +
                "</xsl:for-each>" +
              "</table>" +
            "</body>" +
          "</html>" +
        "</xsl:template>" +

        "<xsl:template name=\"printEmptyCells\">" +
          "<xsl:param name=\"i\"/>" +
          "<xsl:param name=\"count\"/>" +
          "<xsl:if test=\"$i &lt;= $count\">" +
            "<td><xsl:text disable-output-escaping=\"yes\">&amp;nbsp;</xsl:text></td>" +
          "</xsl:if>" +
          "<xsl:if test=\"$i &lt;= $count\">" +
            "<xsl:call-template name=\"printEmptyCells\">" +
              "<xsl:with-param name=\"i\">" +
                "<xsl:value-of select=\"$i + 1\"/>" +
              "</xsl:with-param>" +
              "<xsl:with-param name=\"count\">" +
                "<xsl:value-of select=\"$count\"/>" +
              "</xsl:with-param>" +
            "</xsl:call-template>" +
          "</xsl:if>" +
        "</xsl:template>" +
      "</xsl:stylesheet>";
    
    static final String CSV_XSL =
        "<xsl:stylesheet " +
        "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " +
        "version=\"1.0\" " +
        ">" +
        "<xsl:strip-space elements=\"*\"/>" +
        "<xsl:output indent=\"no\" media-type=\"text/plain\" method=\"text\" omit-xml-declaration=\"yes\" />" +
        "<xsl:template match=\"/\">" +
          "Site id,Estimated Size of Site" +
          "<xsl:for-each select=\"analysis-report/analysis/keywords/keyword\">" +
            "<xsl:text>,</xsl:text>e<xsl:value-of select=\"@id\"/>" +
          "</xsl:for-each>" +
          "<xsl:text>&#13;</xsl:text>" +
          "<xsl:for-each select=\"analysis-report/analysis/results/result/site-page-counts\">" +
            "<xsl:value-of select=\"@site-id\"/>" +
            "<xsl:text>,</xsl:text><xsl:value-of select=\"@estimated-total-pages\"/>" +
            "<xsl:for-each select=\"keyword-page-count\">" +
              "<xsl:text>,</xsl:text><xsl:value-of select=\"text()\"/>" +
            "</xsl:for-each>" +
            "<xsl:text>&#13;</xsl:text>" +
          "</xsl:for-each>" +
        "</xsl:template>" +
      "</xsl:stylesheet>";
        
    protected String reportFormat;
    protected String analysisDescription;
    protected String reportDestination;

    public ReportAnalysisOperationModel() {
        super();
    }

    public String getReportFormat() {
        return reportFormat;
    }
    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public void build(AnalysisBuilder builder, UnitOfWork uow) {
        try {
            Analysis reportingAnalysis = (Analysis)uow.executeQuery("findByDescription",
                Analysis.class, analysisDescription);
            if (reportingAnalysis != null) {
                AnalysisResult result = reportingAnalysis.getResults().get(0);
                AnalysisReport analysisReport = (AnalysisReport)uow.registerNewObject(new AnalysisReport());
                uow.assignSequenceNumber(analysisReport);
                analysisReport.setDateTime(result.getDateTime());
                analysisReport.setReportingAnalysis(reportingAnalysis);
                XMLContext context = new XMLContext(new AnalysisReportProject());
                XMLMarshaller marshaller = context.createMarshaller();
                File destination = new File(reportDestination);
                if (REPORT_XML_FORMAT.equalsIgnoreCase(reportFormat)) {
                    marshaller.marshal(analysisReport, new StreamResult(destination));
                }
                else if (REPORT_HTML_FORMAT.equalsIgnoreCase(reportFormat)) {
                    Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                    marshaller.marshal(analysisReport, doc);
                    StreamSource xslSource = new StreamSource(new StringReader(HTML_XSL));
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                    DOMSource domSource = new DOMSource(doc);
                    transformer.transform(domSource, new StreamResult(destination));
                }
                else if (REPORT_CSV_FORMAT.equalsIgnoreCase(reportFormat)) {
                    Document doc = XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                    marshaller.marshal(result, doc);
                    StreamSource xslSource = new StreamSource(new StringReader(CSV_XSL));
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
                    DOMSource domSource = new DOMSource(doc);
                    transformer.transform(domSource, new StreamResult(destination));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        uow.revertAndResume();
    }
}