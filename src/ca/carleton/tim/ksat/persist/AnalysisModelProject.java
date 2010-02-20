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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

//Java extension libraries
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

//EclipsLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.internal.queries.ListContainerPolicy;
import org.eclipse.persistence.mappings.Association;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.mappings.converters.ObjectTypeConverter;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLChoiceCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.sessions.Project;

public class AnalysisModelProject extends Project {

    protected NamespaceResolver ns;

    @SuppressWarnings("unchecked")
    public AnalysisModelProject() {
        setName("AnalysisModelProject");

        ns = new NamespaceResolver();
        ns.put("xsd", W3C_XML_SCHEMA_NS_URI);

        addDescriptor(buildAssociationDescriptor());
        addDescriptor(buildAnalysisModelDescriptor());
        addDescriptor(buildNewAnalysisModelDescriptor());
        addDescriptor(buildReportAnalysisDescriptor());
        addDescriptor(buildUpdateAnalysisDescriptor());
        addDescriptor(buildRunAnalysisDescriptor());

        for (Iterator descriptors = getDescriptors().values().iterator(); descriptors.hasNext();) {
            XMLDescriptor descriptor = (XMLDescriptor)descriptors.next();
            descriptor.setNamespaceResolver(ns);
        }
    }

    protected ClassDescriptor buildAssociationDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Association.class);
        descriptor.setDefaultRootElement("property");

        XMLDirectMapping keyMapping = new XMLDirectMapping();
        keyMapping.setAttributeName("key");
        keyMapping.setXPath("@name");
        descriptor.addMapping(keyMapping);

        XMLDirectMapping valueMapping = new XMLDirectMapping();
        valueMapping.setAttributeName("value");
        valueMapping.setXPath("text()");
        descriptor.addMapping(valueMapping);

        return descriptor;
    }

	protected ClassDescriptor buildAnalysisModelDescriptor() {
		
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(AnalysisBuilder.class);
        descriptor.setDefaultRootElement("analysis-model");

        XMLCompositeCollectionMapping propertiesMapping = new XMLCompositeCollectionMapping();
        propertiesMapping.setReferenceClass(Association.class);
        propertiesMapping.setAttributeAccessor(new AttributeAccessor() {
            @Override
            public String getAttributeName() {
                return "properties";
            }
            @Override
            public Object getAttributeValueFromObject(Object object) throws DescriptorException {
            	AnalysisBuilder model = (AnalysisBuilder)object;
                Vector<Association> associations =
                    new Vector<Association>();
                for (Map.Entry<String, String> me : model.properties.entrySet()) {
                    associations.add(new Association(me.getKey(), me.getValue()));
                }
                return associations;
            }
            @SuppressWarnings("unchecked")
            @Override
            public void setAttributeValueInObject(Object object, Object value)
                throws DescriptorException {
            	AnalysisBuilder model = (AnalysisBuilder)object;
                Vector<Association> associations =
                    (Vector<Association>)value;
                for (Association a : associations) {
                    model.properties.put((String)a.getKey(), (String)a.getValue());
                }
            }
        });
        propertiesMapping.setXPath("properties/property");
        descriptor.addMapping(propertiesMapping);

        XMLChoiceCollectionMapping operationsMapping = new XMLChoiceCollectionMapping();
        operationsMapping.setAttributeName("operations");
        operationsMapping.setContainerPolicy(new ListContainerPolicy(ArrayList.class));
        operationsMapping.addChoiceElement("new-analysis", NewAnalysisOperationModel.class);
        operationsMapping.addChoiceElement("report-analysis", ReportAnalysisOperationModel.class);
        operationsMapping.addChoiceElement("update-analysis", UpdateAnalysisOperationModel.class);
        operationsMapping.addChoiceElement("run-analysis", RunAnalysisOperationModel.class);
        descriptor.addMapping(operationsMapping);

        return descriptor;
	}

    protected ClassDescriptor buildNewAnalysisModelDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(NewAnalysisOperationModel.class);
        descriptor.setDefaultRootElement("new-analysis");

        XMLDirectMapping sitesMapping = new XMLDirectMapping();
        sitesMapping.setAttributeName("sitesFilename");
        sitesMapping.setXPath("@sites-filename");
        descriptor.addMapping(sitesMapping);

        XMLDirectMapping expressionsMapping = new XMLDirectMapping();
        expressionsMapping.setAttributeName("expressionsFilename");
        expressionsMapping.setXPath("@expressions-filename");
        descriptor.addMapping(expressionsMapping);

        ObjectTypeConverter converter = new ObjectTypeConverter();
        converter.addConversionValue("true", Boolean.TRUE);
        converter.addConversionValue("false", Boolean.FALSE);
        converter.setFieldClassification(String.class);

        XMLDirectMapping createTablesMapping = new XMLDirectMapping();
        createTablesMapping.setAttributeName("createTables");
        createTablesMapping.setConverter(converter);
        createTablesMapping.setNullValue(Boolean.FALSE);
        createTablesMapping.setXPath("@create-tables");
        descriptor.addMapping(createTablesMapping);

        XMLDirectMapping descriptionMapping = new XMLDirectMapping();
        descriptionMapping.setAttributeName("analysisDescription");
        descriptionMapping.setXPath("@analysis-description");
        descriptor.addMapping(descriptionMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildReportAnalysisDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(ReportAnalysisOperationModel.class);
        descriptor.setDefaultRootElement("report-analysis");
        
        XMLDirectMapping formatMapping = new XMLDirectMapping();
        formatMapping.setAttributeName("reportFormat");
        formatMapping.setXPath("@report-format");
        descriptor.addMapping(formatMapping);
        
        XMLDirectMapping descriptionMapping = new XMLDirectMapping();
        descriptionMapping.setAttributeName("analysisDescription");
        descriptionMapping.setXPath("@analysis-description");
        descriptor.addMapping(descriptionMapping);
        
        XMLDirectMapping reportDestinationMapping = new XMLDirectMapping();
        reportDestinationMapping.setAttributeName("reportDestination");
        reportDestinationMapping.setXPath("@report-destination");
        descriptor.addMapping(reportDestinationMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildUpdateAnalysisDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(UpdateAnalysisOperationModel.class);
        descriptor.setDefaultRootElement("update-analysis");
        
        return descriptor;
    }

    protected ClassDescriptor buildRunAnalysisDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(RunAnalysisOperationModel.class);
        descriptor.setDefaultRootElement("run-analysis");

        XMLDirectMapping descriptionMapping = new XMLDirectMapping();
        descriptionMapping.setAttributeName("analysisDescription");
        descriptionMapping.setXPath("@analysis-description");
        descriptor.addMapping(descriptionMapping);
        
        return descriptor;
    }
}