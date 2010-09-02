package ca.carleton.tim.ksat.client;

//javase imports
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JDBCDriver {
	
	public static final String DRIVERS_META_INF = "META-INF/services/java.sql.Driver";

	protected String name;
	protected DriverInfo driverInfo = new DriverInfo();
	protected Boolean checked = null;
	
	public JDBCDriver() {
	}
	
	public JDBCDriver(String name) {
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
			driverInfo.jarFilePaths.add(jarPath);
		}
	}
	
	public void reset() {
		checked = null;
	}
	
	public boolean isOk() {
		if (checked == null) {
			checked = new Boolean(false);
	        String driverClass = null;
	        // try JDBC 3.0: look in META-INF/services/java.sql.Driver file for driverClass
			for (String jarFileName : driverInfo.jarFilePaths) {
		        File file = null;
		        if (jarFileName != null && jarFileName.length() >0) {
		            try {
		            	file = new File(jarFileName);
		            	JarFile jarFile = new JarFile(file);
		            	JarEntry driverEntry = jarFile.getJarEntry(DRIVERS_META_INF);
						if (driverEntry != null) {
				            BufferedReader zipReader = new BufferedReader(
				                 new InputStreamReader(jarFile.getInputStream(driverEntry)));
				            if (zipReader.ready()) {
				            	driverClass = zipReader.readLine();
					            zipReader.close();
					            break;
				            }
						}
		            }
		            catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
			}
			// jar is old - look for classes that implement java.sql.Driver
			if (driverClass == null) {
				List<URL> urls = new ArrayList<URL>();
				for (String jarFileName : driverInfo.jarFilePaths) {
			        if (jarFileName != null && jarFileName.length() >0) {
			        	File f = new File(jarFileName);
			        	if (f.exists() && !f.isDirectory()) {
							try {
				        		URL url = f.toURI().toURL();
				        		urls.add(url);
							}
							catch (MalformedURLException e) {
								e.printStackTrace();
							}
			        	}
			        }
				}
				if (urls.size() > 0) {
					URL urlArray[] = new URL[urls.size()];
					URLClassLoader clazzLoader = URLClassLoader.newInstance(
						urls.toArray(urlArray));
					for (String jarFileName : driverInfo.jarFilePaths) {
				        if (jarFileName != null && jarFileName.length() >0) {
				        	File file = new File(jarFileName);
			            	try {
								JarFile jarFile = new JarFile(file);
								for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				            		JarEntry entry = entries.nextElement();
				            		String name = entry.getName();
				            		if (name.endsWith(".class")) {
				            			String cname = name.replaceFirst(".class", "").replaceAll("/", ".");
				            			Class<?> clazz = clazzLoader.loadClass(cname);
				        				if (Driver.class.isAssignableFrom(clazz) && !Driver.class.equals(clazz)) {
				        					driverClass = cname;
				        					break;
				        				}
				            		}
				            	}
						        if (driverClass != null && driverClass.length() > 0) {
						        	break;
						        }
							}
			            	catch (Exception e) {
								e.printStackTrace();
							}
				        }
					}
				}
			}
	        if (driverClass != null && driverClass.length() > 0) {
	        	driverInfo.driverClass = driverClass;
	        	checked = new Boolean(true);
	        }
		}
		return checked.booleanValue();
	}

}