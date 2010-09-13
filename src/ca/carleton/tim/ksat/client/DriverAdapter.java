package ca.carleton.tim.ksat.client;

//javase imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DriverAdapter {
	
	public static Map<String, DriverAdapter> DRIVER_REGISTRY = new HashMap<String, DriverAdapter>();
	
	protected String name;
	protected DriverInfo driverInfo = new DriverInfo();
	protected String exampleURL;
	protected List<String> platforms = new ArrayList<String>();
	protected Boolean checked = null;
	
	public DriverAdapter() {
	}
	
	public DriverAdapter(String name) {
		this();
		setName(name);
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public List<String> getJarPaths() {
		return driverInfo.jarFilePaths;
	}
	public void addJarPath(String jarPath) {
		if (!driverInfo.jarFilePaths.contains(jarPath)) {
			driverInfo.addJarPath(jarPath);
		}
	}

	public void setDriverClass(String driverClass) {
		driverInfo.driverClass = driverClass;
	}
	public String getDriverClass() {
		return driverInfo.driverClass;
	}

	public void setExampleURL(String exampleURL) {
		this.exampleURL = exampleURL;
	}
	public String getExampleURL() {
		return exampleURL;
	}

	public List<String> getPlatforms() {
		return platforms;
	}
	public void addPlatform(String platform) {
		if (!platforms.contains(platform)) {
			platforms.add(platform);
		}
	}
	
	public void reset() {
		checked = null;
	}
	
	public boolean isOk() {
		if (checked == null) {
			checked = new Boolean(false);
	        boolean found = false;
			for (String jarFileName : driverInfo.jarFilePaths) {
		        File file = null;
		        if (jarFileName != null && jarFileName.length() >0) {
		            try {
		            	file = new File(jarFileName);
		            	JarFile jarFile = new JarFile(file);
		            	JarEntry driverEntry = jarFile.getJarEntry(
		            		driverInfo.driverClass.replace('.', '/') + ".class");
						if (driverEntry != null) {
							found = true;
							break;
						}
					}
		            catch (Exception e) {
		            	// ignore
		            }
		        }
			}
			if (found) {
	        	checked = new Boolean(true);
	        }
		}
		return checked.booleanValue();
	}

}