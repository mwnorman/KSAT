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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Java extension libraries
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

//EclipeLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.internal.descriptors.InstantiationPolicy;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.oxm.mappings.XMLFragmentCollectionMapping;
import org.eclipse.persistence.sessions.Project;
import static org.eclipse.persistence.oxm.XMLConstants.DATE_TIME_QNAME;

//KSAT imports
import ca.carleton.tim.ksat.model.AnalysisResult;
import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;

public class AnalysisReportProject extends Project {

    protected NamespaceResolver ns;

    @SuppressWarnings("unchecked")
    public AnalysisReportProject() {
        setName("AnalysisReportProject");

        ns = new NamespaceResolver();
        ns.put("xsd", W3C_XML_SCHEMA_NS_URI);

        addDescriptor(buildSiteDescriptor());
        addDescriptor(buildAnalysisKeywordDescriptor());
        addDescriptor(buildAnalysisReportDescriptor());
        addDescriptor(buildAnalysisWrapperResultDescriptor());
        addDescriptor(buildAnalysisResultDescriptor());

        for (Iterator descriptors = getDescriptors().values().iterator(); descriptors.hasNext();) {
            XMLDescriptor descriptor = (XMLDescriptor)descriptors.next();
            descriptor.setNamespaceResolver(ns);
        }
    }

    protected ClassDescriptor buildSiteDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Site.class);
        descriptor.setDefaultRootElement("site");

        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        descriptor.addMapping(idMapping);

        XMLDirectMapping urlMapping = new XMLDirectMapping();
        urlMapping.setAttributeName("url");
        urlMapping.setXPath("text()");
        descriptor.addMapping(urlMapping);
        
        return descriptor;
    }
    
    protected ClassDescriptor buildAnalysisKeywordDescriptor() {
       
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(KeywordExpression.class);
        descriptor.setDefaultRootElement("keyword");

        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        descriptor.addMapping(idMapping);

        XMLDirectMapping expressionMapping = new XMLDirectMapping();
        expressionMapping.setAttributeName("expression");
        expressionMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                String decodedExpression = ((KeywordExpression)object).getExpression();
                try {
                    decodedExpression = URLDecoder.decode(decodedExpression, "UTF-8");
                }
                catch (Exception e) {
                    // ignore
                }
                return decodedExpression;
            }
        });
        expressionMapping.setXPath("text()");
        descriptor.addMapping(expressionMapping);
        
        return descriptor;
    }
    
    protected ClassDescriptor buildAnalysisReportDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(AnalysisReport.class);
        descriptor.setDefaultRootElement("analysis-report");

        XMLDirectMapping analysisIdMapping = new XMLDirectMapping();
        analysisIdMapping.setAttributeName("analysisId");
        analysisIdMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                return ((AnalysisReport)object).getAnalysisId();
            }
        });
        analysisIdMapping.setXPath("analysis/@id");
        descriptor.addMapping(analysisIdMapping);

        XMLDirectMapping analysisDescriptionMapping = new XMLDirectMapping();
        analysisDescriptionMapping.setAttributeName("analysisDescription");
        analysisDescriptionMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                return ((AnalysisReport)object).getAnalysisDescription();
            }
        });
        analysisDescriptionMapping.setXPath("analysis/@description");
        descriptor.addMapping(analysisDescriptionMapping);
        
        XMLCompositeCollectionMapping sitesMapping = new XMLCompositeCollectionMapping();
        sitesMapping.setAttributeName("analysisSites");
        sitesMapping.setReferenceClass(Site.class);
        sitesMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                return ((AnalysisReport)object).getAnalysisSites();
            }
        });
        sitesMapping.setXPath("analysis/sites/site");
        descriptor.addMapping(sitesMapping);

        XMLCompositeCollectionMapping keywordsMapping = new XMLCompositeCollectionMapping();
        keywordsMapping.setAttributeName("analysisKeywords");
        keywordsMapping.setReferenceClass(KeywordExpression.class);
        keywordsMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                return ((AnalysisReport)object).getAnalysisKeywords();
            }
        });
        keywordsMapping.setXPath("analysis/keywords/keyword");
        descriptor.addMapping(keywordsMapping);
        
        XMLCompositeObjectMapping resultsWrapperMapping = new XMLCompositeObjectMapping();
        resultsWrapperMapping.setAttributeName("analysisResults");
        resultsWrapperMapping.setReferenceClass(AnalysisResultWrapper.class);
        resultsWrapperMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                AnalysisReport report = (AnalysisReport)object;
                return new AnalysisResultWrapper(report.getAnalysisResults(), 
                    report.getDateTime());
            }
        });
        resultsWrapperMapping.setXPath("analysis/results");
        descriptor.addMapping(resultsWrapperMapping);
        
        return descriptor;
    }
    /*
     * This wrapper class is 'injected' between the analyisReport and its list of results
     * modeled as AnalysisReport --*--> AnalysisResult's
     * mapped as AnalysisReport --> AnalysisResultWrapper --*--> AnalysisResult's 
     * This is done so that the grouping XPath for the list shows up even when the list is empty:
       <analysis-report
         <analysis id="1" description="analysis1">
           <sites>
             <site id="1">http://awprofessional.com/</site>
             ...
           </sites>
           <keywords>
             <keyword id="1">%22associate+member%22</keyword>
             ...
             </keywords>
           <results/>
         </analysis>
       </analysis-report>
     */
    class AnalysisResultWrapper {
        protected Date dateTime;
        protected List<AnalysisResult> results;
        public AnalysisResultWrapper() {
        }
        public AnalysisResultWrapper(List<AnalysisResult> results, Date dateTime) {
            this.results = results;
            this.dateTime = dateTime;
        }
    }
    
    class AnalysisResultWrapperInstantiationPolicy extends InstantiationPolicy {
        AnalysisReportProject outer;
        AnalysisResultWrapperInstantiationPolicy(AnalysisReportProject outer) {
            this.outer = outer;
        }
        @Override
        public Object buildNewInstance() throws DescriptorException {
            return outer.new AnalysisResultWrapper();
        }
    }
    
    protected ClassDescriptor buildAnalysisWrapperResultDescriptor() {
    
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(AnalysisResultWrapper.class);
        // need policy 'cause AnalysisResultWrapper's default constructor is nested
        descriptor.setInstantiationPolicy(new AnalysisResultWrapperInstantiationPolicy(this));
        descriptor.setDefaultRootElement("result");
        
        XMLCompositeCollectionMapping resultsMapping = new XMLCompositeCollectionMapping();
        resultsMapping.setAttributeName("analysisResults");
        resultsMapping.setReferenceClass(AnalysisResult.class);
        resultsMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                AnalysisResultWrapper wrapper = (AnalysisResultWrapper)object;
                return wrapper.results;
            }
        });
        resultsMapping.setXPath("result");
        descriptor.addMapping(resultsMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildAnalysisResultDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(AnalysisResult.class);
        descriptor.setDefaultRootElement("result");

        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        descriptor.addMapping(idMapping);
        
        XMLDirectMapping timestampMapping = new XMLDirectMapping();
        timestampMapping.setAttributeName("dateTime");
        timestampMapping.setXPath("@timestamp");
        ((XMLField)timestampMapping.getField()).setSchemaType(DATE_TIME_QNAME);
        descriptor.addMapping(timestampMapping);

        XMLFragmentCollectionMapping rawResultFragmentMapping = new XMLFragmentCollectionMapping();
        rawResultFragmentMapping.setAttributeName("rawResults");
        rawResultFragmentMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
                // no-op - marshall 'out' only
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
                AnalysisResult analysisResult = (AnalysisResult)object;
                Node rawResults = analysisResult.getRawResults();
                ArrayList<Node> nodes = new ArrayList<Node>();
                Node node = rawResults.getFirstChild();
                NodeList list = node.getChildNodes();
                for (int i = 0, len = list.getLength(); i < len;) {
                    nodes.add(list.item(i++));
                }
                return nodes;
            }
        });
        rawResultFragmentMapping.setXPath("text()");
        descriptor.addMapping(rawResultFragmentMapping);
        
        return descriptor;
    }
}