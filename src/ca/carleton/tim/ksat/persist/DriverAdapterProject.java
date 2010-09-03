package ca.carleton.tim.ksat.persist;

//javase imports
import java.util.HashMap;
import java.util.Iterator;

//Java extension libraries
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

//EclipeLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
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

        addDescriptor(buildDriversDescriptor());
        addDescriptor(buildDriverAdapterDescriptor());
        addDescriptor(buildDriverInfoDescriptor());

	    for (Iterator descriptors = getDescriptors().values().iterator(); descriptors.hasNext();) {
	        XMLDescriptor descriptor = (XMLDescriptor)descriptors.next();
	        descriptor.setNamespaceResolver(ns);
	    }
    }
    
	protected ClassDescriptor buildDriversDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Drivers.class);
        descriptor.setDefaultRootElement("drivers");

        XMLCompositeCollectionMapping driversMapping = new XMLCompositeCollectionMapping();
        driversMapping.setAttributeName("drivers");
        driversMapping.setXPath("driver");
        driversMapping.setReferenceClass(DriverAdapter.class);
        driversMapping.useMapClass(HashMap.class, "getName");
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

        XMLDirectMapping exampleURLMapping = new XMLDirectMapping();
        exampleURLMapping.setAttributeName("exampleURL");
        exampleURLMapping.setXPath("example-url/text()");
        exampleURLMapping.setIsCDATA(true);
        descriptor.addMapping(exampleURLMapping);
        
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
        descriptor.addMapping(jarPathsMapping);
        
		return descriptor;
	}
}