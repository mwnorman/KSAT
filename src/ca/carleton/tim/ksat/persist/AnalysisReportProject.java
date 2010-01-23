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

//Java extension libraries
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

//javase imports
import java.util.Iterator;

//EclipsLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.sessions.Project;

import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;

public class AnalysisReportProject extends Project {

    protected NamespaceResolver ns;

    @SuppressWarnings("unchecked")
    public AnalysisReportProject() {
        setName("AnalysisReportProject");

        ns = new NamespaceResolver();
        ns.put("xsd", W3C_XML_SCHEMA_NS_URI);

        addDescriptor(buildAnalysisReportDescriptor());
        addDescriptor(buildSiteDescriptor());
        addDescriptor(buildAnalysisKeywordDescriptor());

        for (Iterator descriptors = getDescriptors().values().iterator(); descriptors.hasNext();) {
            XMLDescriptor descriptor = (XMLDescriptor)descriptors.next();
            descriptor.setNamespaceResolver(ns);
        }
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
        
        return descriptor;
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
        expressionMapping.setXPath("text()");
        descriptor.addMapping(expressionMapping);
        
        return descriptor;
    }
    
}