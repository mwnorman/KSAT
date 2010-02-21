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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

//java eXtension imports
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//EclipseLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.exceptions.ConversionException;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.mappings.converters.SerializedObjectConverter;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.Session;

//KSAT imports
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.AnalysisResult;
import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;

public class AnalysisProject extends Project {

    public AnalysisProject() {
        setName("AnalysisProject");

        addDescriptor(buildAnalysisDescriptor());
        addDescriptor(buildSiteDescriptor());
        addDescriptor(buildKeywordExpressionDescriptor());
        addDescriptor(buildAnalysisResultDescriptor());
    }

    protected ClassDescriptor buildAnalysisDescriptor() {

        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(Analysis.class);
        descriptor.addTableName("KSAT_ANALYSIS_TABLE");
        descriptor.addPrimaryKeyFieldName("KSAT_ANALYSIS_TABLE.ID");
        descriptor.setSequenceNumberFieldName("KSAT_ANALYSIS_TABLE.ID");
        descriptor.setSequenceNumberName("ANALYSIS_SEQ");
        descriptor.setAlias("Analysis");
        descriptor.getQueryManager().checkCacheForDoesExist();
        
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setGetMethodName("getId");
        idMapping.setSetMethodName("setId");
        idMapping.setFieldName("KSAT_ANALYSIS_TABLE.ID");
        descriptor.addMapping(idMapping);
        
        DirectToFieldMapping descriptionMapping = new DirectToFieldMapping();
        descriptionMapping.setAttributeName("description");
        descriptionMapping.setGetMethodName("getDescription");
        descriptionMapping.setSetMethodName("setDescription");
        descriptionMapping.setFieldName("KSAT_ANALYSIS_TABLE.DESCRIPT");
        descriptor.addMapping(descriptionMapping);

        ManyToManyMapping sitesMapping = new ManyToManyMapping();
        sitesMapping.setAttributeName("sites");
        sitesMapping.setGetMethodName("getSites");
        sitesMapping.setSetMethodName("setSites");
        sitesMapping.setReferenceClass(ca.carleton.tim.ksat.model.Site.class);
        sitesMapping.dontUseIndirection();
        sitesMapping.useBatchReading();
        sitesMapping.useCollectionClass(java.util.ArrayList.class);
        sitesMapping.addAscendingOrdering("id");
        sitesMapping.setRelationTableName("KSAT_ANALYSIS_SITE");
        sitesMapping.addSourceRelationKeyFieldName("KSAT_ANALYSIS_SITE.ANALYSIS_ID", "KSAT_ANALYSIS_TABLE.ID");
        sitesMapping.addTargetRelationKeyFieldName("KSAT_ANALYSIS_SITE.SITE_ID", "KSAT_SITE_TABLE.ID");
        descriptor.addMapping(sitesMapping);
        
        DirectToFieldMapping expressionCountMapping = new DirectToFieldMapping();
        expressionCountMapping.setAttributeName("expressionCount");
        expressionCountMapping.setGetMethodName("getExpressionCount");
        expressionCountMapping.setSetMethodName("setExpressionCount");
        expressionCountMapping.setFieldName("KSAT_ANALYSIS_TABLE.KWRDCOUNT");
        expressionCountMapping.setNullValue(Integer.valueOf(0));
        descriptor.addMapping(expressionCountMapping);

        ManyToManyMapping expressionsMapping = new ManyToManyMapping();
        expressionsMapping.setAttributeName("expressions");
        expressionsMapping.setGetMethodName("getExpressions");
        expressionsMapping.setSetMethodName("setExpressions");
        expressionsMapping.setReferenceClass(ca.carleton.tim.ksat.model.KeywordExpression.class);
        expressionsMapping.dontUseIndirection();
        expressionsMapping.useBatchReading();
        expressionsMapping.useCollectionClass(java.util.ArrayList.class);
        expressionsMapping.addAscendingOrdering("id");
        expressionsMapping.setRelationTableName("KSAT_ANALYSIS_KEYWORD");
        expressionsMapping.addSourceRelationKeyFieldName("KSAT_ANALYSIS_KEYWORD.ANALYSIS_ID", "KSAT_ANALYSIS_TABLE.ID");
        expressionsMapping.addTargetRelationKeyFieldName("KSAT_ANALYSIS_KEYWORD.KEYWORD_ID", "KSAT_KEYWORD_TABLE.ID");
        descriptor.addMapping(expressionsMapping);
        
        OneToManyMapping analysisRunsMapping = new OneToManyMapping();
        analysisRunsMapping.setAttributeName("results");
        analysisRunsMapping.setGetMethodName("getResults");
        analysisRunsMapping.setSetMethodName("setResults");
        analysisRunsMapping.setReferenceClass(AnalysisResult.class);
        analysisRunsMapping.dontUseIndirection();
        analysisRunsMapping.useBatchReading();
        analysisRunsMapping.privateOwnedRelationship();
        analysisRunsMapping.useCollectionClass(ArrayList.class);
        analysisRunsMapping.addAscendingOrdering("id");
        analysisRunsMapping.addTargetForeignKeyFieldName("KSAT_RESULT_TABLE.ANALYSIS_ID",
            "KSAT_ANALYSIS_TABLE.ID");
        descriptor.addMapping(analysisRunsMapping);
        
        // Named Query -- findByDescription
        ReadObjectQuery findByDescriptionQuery = new ReadObjectQuery(Analysis.class);
        findByDescriptionQuery.setName("findByDescription");
        findByDescriptionQuery.setShouldBindAllParameters(true);
        ExpressionBuilder builder = findByDescriptionQuery.getExpressionBuilder();
        findByDescriptionQuery.setSelectionCriteria(
            builder.get("description").equal(builder.getParameter("description")));
        findByDescriptionQuery.addArgument("description", String.class);
        descriptor.getQueryManager().addQuery("findByDescription", findByDescriptionQuery);
        
        return descriptor;
    }

    protected ClassDescriptor buildSiteDescriptor() {

        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(Site.class);
        descriptor.setTableName("KSAT_SITE_TABLE");
        descriptor.addPrimaryKeyFieldName("KSAT_SITE_TABLE.ID");
        descriptor.setSequenceNumberFieldName("KSAT_SITE_TABLE.ID");
        descriptor.setSequenceNumberName("SITE_SEQ");
        descriptor.setAlias("Site");
        descriptor.getQueryManager().checkCacheForDoesExist();
        
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setGetMethodName("getId");
        idMapping.setSetMethodName("setId");
        idMapping.setFieldName("KSAT_SITE_TABLE.ID");
        descriptor.addMapping(idMapping);
        
        DirectToFieldMapping descriptionMapping = new DirectToFieldMapping();
        descriptionMapping.setAttributeName("description");
        descriptionMapping.setGetMethodName("getDescription");
        descriptionMapping.setSetMethodName("setDescription");
        descriptionMapping.setFieldName("KSAT_SITE_TABLE.DESCRIPT");
        descriptor.addMapping(descriptionMapping);
        
        DirectToFieldMapping urlMapping = new DirectToFieldMapping();
        urlMapping.setAttributeName("url");
        urlMapping.setGetMethodName("getUrl");
        urlMapping.setSetMethodName("setUrl");
        urlMapping.setFieldName("KSAT_SITE_TABLE.URL");
        descriptor.addMapping(urlMapping);

        return descriptor;
    }

    protected ClassDescriptor buildKeywordExpressionDescriptor() {

        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(KeywordExpression.class);
        descriptor.addTableName("KSAT_KEYWORD_TABLE");
        descriptor.addPrimaryKeyFieldName("KSAT_KEYWORD_TABLE.ID");
        descriptor.setSequenceNumberFieldName("KSAT_KEYWORD_TABLE.ID");
        descriptor.setSequenceNumberName("KEYWORD_SEQ");
        descriptor.setAlias("KeywordExpression");
        
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setGetMethodName("getId");
        idMapping.setSetMethodName("setId");
        idMapping.setFieldName("KSAT_KEYWORD_TABLE.ID");
        descriptor.addMapping(idMapping);
        
        DirectToFieldMapping expressionMapping = new DirectToFieldMapping();
        expressionMapping.setAttributeName("expression");
        expressionMapping.setGetMethodName("getExpression");
        expressionMapping.setSetMethodName("setExpression");
        expressionMapping.setFieldName("KSAT_KEYWORD_TABLE.EXPRESSION");
        descriptor.addMapping(expressionMapping);

        return descriptor;
    }

    protected ClassDescriptor buildAnalysisResultDescriptor() {

        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(AnalysisResult.class);
        descriptor.addTableName("KSAT_RESULT_TABLE");
        descriptor.addPrimaryKeyFieldName("KSAT_RESULT_TABLE.ID");
        descriptor.setSequenceNumberFieldName("KSAT_RESULT_TABLE.ID");
        descriptor.setSequenceNumberName("RESULT_SEQ");
        descriptor.setAlias("AnalysisResult");
        
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setGetMethodName("getId");
        idMapping.setSetMethodName("setId");
        idMapping.setFieldName("KSAT_RESULT_TABLE.ID");
        descriptor.addMapping(idMapping);
        
        DirectToFieldMapping dateMapping = new DirectToFieldMapping();
        dateMapping.setAttributeName("dateTime");
        dateMapping.setGetMethodName("getDateTime");
        dateMapping.setSetMethodName("setDateTime");
        dateMapping.setFieldName("KSAT_RESULT_TABLE.RUN_DATE");
        descriptor.addMapping(dateMapping);
        
        DirectToFieldMapping rawResultMapping = new DirectToFieldMapping();
        rawResultMapping.setAttributeName("rawResults");
        rawResultMapping.setGetMethodName("getRawResults");
        rawResultMapping.setSetMethodName("setRawResults");
        rawResultMapping.setFieldName("KSAT_RESULT_TABLE.RAW_RESULT");
        rawResultMapping.setConverter(new SerializedObjectConverter() {
            @Override
            public Object convertDataValueToObjectValue(Object fieldValue, Session session)
                throws DescriptorException {
                Node n = null;
                try {
                    byte[] bytes = null;
                    bytes = (byte[])((AbstractSession)session).getDatasourcePlatform().convertObject(
                        fieldValue, ClassConstants.APBYTE);
                    if ((bytes == null) || (bytes.length == 0)) {
                        return null;
                    }
                    String rawResults = new String(bytes);
                    Document tmp = 
                        XMLPlatformFactory.getInstance().getXMLPlatform().createDocument();
                    DocumentFragment fragment = tmp.createDocumentFragment();
                    DOMResult dr = new DOMResult(fragment);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    Source source = new StreamSource(new StringReader(rawResults));
                    transformer.transform(source, dr);
                    n = dr.getNode();
                }
                catch (Exception e) {
                    if (!(e instanceof ConversionException)) {
                        throw ConversionException.couldNotBeConverted(fieldValue, Node.class, e);
                    }
                    throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(),
                        (ConversionException)e);
                }
                return n;
            }
            @Override
            public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
                byte[] bytes = null;
                if (attributeValue instanceof Node) {
                    Node rawResults = (Node)attributeValue;
                    StringWriter sw = new StringWriter();
                    try {
                        Transformer t = TransformerFactory.newInstance().newTransformer();
                        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        t.transform(new DOMSource(rawResults), new StreamResult(sw));
                        bytes = sw.toString().getBytes();
                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
                return bytes;
            }
        });
        descriptor.addMapping(rawResultMapping);
        
        OneToOneMapping ownerMapping = new OneToOneMapping();
        ownerMapping.setAttributeName("owner");
        ownerMapping.setGetMethodName("getOwner");
        ownerMapping.setSetMethodName("setOwner");
        ownerMapping.setReferenceClass(Analysis.class);
        ownerMapping.dontUseIndirection();
        ownerMapping.addForeignKeyFieldName("KSAT_RESULT_TABLE.ANALYSIS_ID",
            "KSAT_ANALYSIS_TABLE.ID");
        descriptor.addMapping(ownerMapping);
        
        return descriptor;
    }
}