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
package ca.carleton.tim.ksat.test;

//javase imports
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;

//JUnit 4 imports
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//EclipseLink imports
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.platform.xml.XMLComparer;
import org.eclipse.persistence.platform.xml.XMLParser;
import org.eclipse.persistence.platform.xml.XMLPlatform;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;

//KSAT imports
import ca.carleton.tim.ksat.client.DriverAdapter;
import ca.carleton.tim.ksat.persist.DriverAdapterProject;
import ca.carleton.tim.ksat.persist.Drivers;
import ca.carleton.tim.ksat.utils.FileUtil;

public class DriverAdapterXMLTestSuite {

	//static fixtures
    static XMLComparer comparer = new XMLComparer();
    static XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
    static XMLParser xmlParser = xmlPlatform.newXMLParser();
    
	static XMLContext xc; 
	
	@BeforeClass
	public static void setUp() throws Exception {
		xc = new XMLContext(new DriverAdapterProject());
	}

	@Test
	public void writeTest() {
		DriverAdapter da1 = new DriverAdapter("one");
		da1.addJarPath("foobar.jar");
		da1.addJarPath("c:/temp/blaz.jar");
		da1.setDriverClass("org.foo.bar.MyJDBCDriver");
		DriverAdapter da2 = new DriverAdapter("derby");
		da2.addJarPath("C:\\Program Files\\Sun\\JavaDB\\lib\\derby.jar");
		da2.setDriverClass("org.apache.derby.jdbc.AutoloadedDriver");
		da2.addExampleURL("server","jdbc:derby://<host>:<port>/<dbname>");
		
		Drivers drivers = new Drivers();
		drivers.setDrivers(new HashMap<String, DriverAdapter>());
		drivers.getDrivers().put("one", da1);
		drivers.getDrivers().put("derby", da2);
		
        Document doc = xmlPlatform.createDocument();
		xc.createMarshaller().marshal(drivers,doc);
        Document controlDoc = xmlParser.parse(new StringReader(CONTROL_DOC));
        assertTrue("control document not same as instance document",
            comparer.isNodeEqual(controlDoc, doc));
	}
	
	static final String CONTROL_DOC =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<drivers xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
		   "<driver name=\"one\">" +
		      "<driver-info>" +
		         "<driver-class>org.foo.bar.MyJDBCDriver</driver-class>" +
		         "<paths>" +
		            "<jar-file-path><![CDATA[foobar.jar]]></jar-file-path>" +
		            "<jar-file-path><![CDATA[c:/temp/blaz.jar]]></jar-file-path>" +
		         "</paths>" +
		      "</driver-info>" +
		   "</driver>" +
		   "<driver name=\"derby\">" +
		      "<example-urls>" +
		          "<url name=\"server\"><![CDATA[jdbc:derby://<host>:<port>/<dbname>]]></url>" +
		      "</example-urls>" +
		      "<driver-info>" +
		         "<driver-class>org.apache.derby.jdbc.AutoloadedDriver</driver-class>" +
		         "<paths>" +
		            "<jar-file-path><![CDATA[C:/Program Files/Sun/JavaDB/lib/derby.jar]]></jar-file-path>" +
		         "</paths>" +
		      "</driver-info>" +
		   "</driver>" +
		"</drivers>";
	
	@Test
	public void readTest() {
		Drivers drivers = (Drivers)xc.createUnmarshaller().unmarshal(new StringReader(CONTROL_DOC));
		Map<String, DriverAdapter> driversMap = drivers.getDrivers();
		assertNotNull(driversMap);
		Set<Map.Entry<String, DriverAdapter>> entrySet = driversMap.entrySet();
		Iterator<Map.Entry<String, DriverAdapter>> iterator = entrySet.iterator();
		Map.Entry<String, DriverAdapter> nextMe = iterator.next();
		assertEquals("one", nextMe.getKey());
		DriverAdapter da1 = nextMe.getValue();
		assertEquals("one", da1.getName());
		assertTrue(da1.getExampleURLs().isEmpty());
		assertEquals("org.foo.bar.MyJDBCDriver", da1.getDriverClass());
		List<String> jarPaths = da1.getJarPaths();
		assertTrue(jarPaths.size() == 2);
		assertEquals("foobar.jar", jarPaths.get(0));
		assertEquals(FileUtil.normalize("c:\\temp\\blaz.jar"), jarPaths.get(1));
		nextMe = iterator.next();
		assertEquals("derby", nextMe.getKey());
		DriverAdapter da2 = nextMe.getValue();
		assertEquals("derby", da2.getName());
		//assertEquals("jdbc:derby://<host>:<port>/<dbname>;create=true;", da2.getExampleURL());
		assertEquals("org.foo.bar.MyJDBCDriver", da1.getDriverClass());
		jarPaths = da2.getJarPaths();
		assertTrue(jarPaths.size() == 1);
		assertEquals(FileUtil.normalize("C:\\Program Files\\Sun\\JavaDB\\lib\\derby.jar"), jarPaths.get(0));
	}
}
