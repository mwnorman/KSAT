package ca.carleton.tim.ksat.test;

//JUnit 4 imports
import org.junit.Test;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import ca.carleton.tim.ksat.client.JDBCDriver;

public class DriverTestSuite {

	@Test
	public void test1() {
		JDBCDriver driver = new JDBCDriver("test");
		driver.addJarPath("");
		assertFalse(driver.isOk());
	}
	
	@Test
	public void test2() {
		JDBCDriver driver = new JDBCDriver("test2");
		driver.addJarPath("C:/Program Files/Sun/JavaDB/lib/derby.jar");
		assertTrue(driver.isOk());
	}
	
	@Test
	public void test3() {
		JDBCDriver driver = new JDBCDriver("test3");
		driver.addJarPath("C:/_eclipselink/extension.oracle.lib.external/ojdbc14dms_10.jar");
		assertTrue(driver.isOk());
	}
	
	@Test
	public void test4() {
		JDBCDriver driver = new JDBCDriver("test4");
		driver.addJarPath("C:/downloads/mssqlserver.jar");
		driver.addJarPath("C:/downloads/msutil.jar");
		driver.addJarPath("C:/downloads/msbase.jar");
		assertTrue(driver.isOk());
	}

}