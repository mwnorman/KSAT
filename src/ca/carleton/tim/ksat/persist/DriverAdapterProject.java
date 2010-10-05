package ca.carleton.tim.ksat.persist;

//javase imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

//Java extension libraries
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

//EclipeLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.mappings.PropertyAssociation;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeDirectCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.sessions.Project;

//KSAT imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.client.DriverInfo;

@SuppressWarnings({"rawtypes"})
public class DriverAdapterProject extends Project {

    protected NamespaceResolver ns;

    public DriverAdapterProject() {
        setName("DriverAdapterProject");

        ns = new NamespaceResolver();
        ns.put("xsd", W3C_XML_SCHEMA_NS_URI);

        addDescriptor(buildPropertyAssociationDescriptor());
        addDescriptor(buildDriversDescriptor());
        addDescriptor(buildDriverAdapterDescriptor());
        addDescriptor(buildDriverInfoDescriptor());

	    for (Iterator descriptors = getDescriptors().values().iterator(); descriptors.hasNext();) {
	        XMLDescriptor descriptor = (XMLDescriptor)descriptors.next();
	        descriptor.setNamespaceResolver(ns);
	    }
    }

    protected ClassDescriptor buildPropertyAssociationDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(PropertyAssociation.class);
        descriptor.setDefaultRootElement("property");

        XMLDirectMapping keyMapping = new XMLDirectMapping();
        keyMapping.setAttributeName("key");
        keyMapping.setGetMethodName("getKey");
        keyMapping.setSetMethodName("setKey");
        keyMapping.setXPath("@name");
        descriptor.addMapping(keyMapping);

        XMLDirectMapping valueMapping = new XMLDirectMapping();
        valueMapping.setIsCDATA(true);
        valueMapping.setAttributeName("value");
        valueMapping.setGetMethodName("getValue");
        valueMapping.setSetMethodName("setValue");
        valueMapping.setXPath("text()");
        descriptor.addMapping(valueMapping);

        return descriptor;
    }

	protected ClassDescriptor buildDriversDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Drivers.class);
        descriptor.setDefaultRootElement("drivers");

        XMLCompositeCollectionMapping driversMapping = new XMLCompositeCollectionMapping();
        driversMapping.setAttributeName("drivers");
        driversMapping.setXPath("driver");
        driversMapping.setReferenceClass(DriverAdapter.class);
        driversMapping.useMapClass(LinkedHashMap.class, "getName");
        descriptor.addMapping(driversMapping);

		return descriptor;
		
	}

	protected ClassDescriptor buildDriverAdapterDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(DriverAdapter.class);
        descriptor.setDefaultRootElement("driver");

        XMLDirectMapping nameMapping = new XMLDirectMapping();
        nameMapping.setAttributeName("name");
        nameMapping.setXPath("@name");
        descriptor.addMapping(nameMapping);
        
        XMLCompositeCollectionMapping exampleURLsMapping = new XMLCompositeCollectionMapping();
        exampleURLsMapping.setAttributeName("exampleURLs");
        exampleURLsMapping.setReferenceClass(PropertyAssociation.class);
        exampleURLsMapping.setAttributeAccessor(new AttributeAccessor() {
            @SuppressWarnings("unchecked")
			public Object getAttributeValueFromObject(Object object) {
            	DriverAdapter da = (DriverAdapter)object;
                Vector propertyAssociations = new NonSynchronizedVector();
                for (Iterator i = da.getExampleURLs().entrySet().iterator(); i.hasNext();) {
                    Map.Entry me = (Map.Entry)i.next();
                    PropertyAssociation propertyAssociation = new PropertyAssociation();
                    propertyAssociation.setKey(me.getKey());
                    propertyAssociation.setValue(me.getValue());
                    propertyAssociations.add(propertyAssociation);
                }
                return propertyAssociations;
            }
			public void setAttributeValueInObject(Object object, Object value) {
            	DriverAdapter da = (DriverAdapter)object;
                Vector propertyAssociations = (Vector)value;
                for (int i = 0; i < propertyAssociations.size(); i++) {
                    PropertyAssociation propertyAssociation = (PropertyAssociation)propertyAssociations.get(i);
                    da.addExampleURL((String)propertyAssociation.getKey(),(String)propertyAssociation.getValue());
                }
            }
        });
        exampleURLsMapping.setXPath("example-urls/url");
        descriptor.addMapping(exampleURLsMapping);
        
        XMLCompositeDirectCollectionMapping platformsMapping = 
        	new XMLCompositeDirectCollectionMapping();
        platformsMapping.setAttributeName("platforms");
        platformsMapping.setXPath("eclipselink-platforms/platform/text()");
        platformsMapping.useCollectionClass(ArrayList.class);
        descriptor.addMapping(platformsMapping);
        
        XMLCompositeObjectMapping infoMapping = new XMLCompositeObjectMapping();
        infoMapping.setAttributeName("driverInfo");
        infoMapping.setReferenceClass(DriverInfo.class);
        infoMapping.setXPath("driver-info");
        descriptor.addMapping(infoMapping);

		return descriptor;
	}

	protected ClassDescriptor buildDriverInfoDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(DriverInfo.class);
        descriptor.setDefaultRootElement("driver-info");

        XMLDirectMapping driverClassMapping = new XMLDirectMapping();
        driverClassMapping.setAttributeName("driverClass");
        driverClassMapping.setXPath("driver-class/text()");
        descriptor.addMapping(driverClassMapping);

        XMLCompositeDirectCollectionMapping jarPathsMapping = 
        	new XMLCompositeDirectCollectionMapping();
        jarPathsMapping.setAttributeName("jarFilePaths");
        jarPathsMapping.setIsCDATA(true);
        jarPathsMapping.setXPath("paths/jar-file-path/text()");
        jarPathsMapping.useCollectionClass(ArrayList.class);
        descriptor.addMapping(jarPathsMapping);
        
		return descriptor;
	}
}