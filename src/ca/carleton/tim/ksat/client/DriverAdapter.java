package ca.carleton.tim.ksat.client;

//javase imports
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//EclipseLink imports
import org.eclipse.persistence.internal.libraries.asm.ClassAdapter;
import org.eclipse.persistence.internal.libraries.asm.ClassReader;
import org.eclipse.persistence.internal.libraries.asm.ClassVisitor;

//KSAT imports
import ca.carleton.tim.ksat.utils.EmptyVisitor;

public class DriverAdapter {
	
	public static Map<String, DriverAdapter> DRIVER_REGISTRY = new HashMap<String, DriverAdapter>();
	
	static final String DRIVERS_META_INF = "META-INF/services/java.sql.Driver";

	protected String name;
	protected DriverInfo driverInfo = new DriverInfo();
	protected String exampleURL;
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
		                // ignore
		            }
		        }
			}
			if (driverClass == null) {
				// jar is old - look for classes that implement java.sql.Driver interface
				List<File> files = new ArrayList<File>();
				for (String jarFileName : driverInfo.jarFilePaths) {
			        if (jarFileName != null && jarFileName.length() >0) {
			        	File f = new File(jarFileName);
			        	if (f.exists() && !f.isDirectory()) {
							files.add(f);
			        	}
			        }
				}
				if (files.size() > 0) {
					for (String jarFileName : driverInfo.jarFilePaths) {
				        if (jarFileName != null && jarFileName.length() >0) {
				        	File file = new File(jarFileName);
			            	try {
								JarFile jarFile = new JarFile(file);
								for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				            		JarEntry entry = entries.nextElement();
				            		if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					            		InputStream is = jarFile.getInputStream(entry);
					            		DriverClassAdapter dca = new DriverClassAdapter(null);
					            		new ClassReader(is).accept(dca, true);
					            		if (dca.getDriverClass() != null) {
					            			driverClass = dca.getDriverClass().replace('/', '.');
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
	
	class DriverClassAdapter extends ClassAdapter {
		String driverClass = null;
		public DriverClassAdapter(ClassVisitor cv) {
			super(new EmptyVisitor());
		}
		public String getDriverClass() {
			return driverClass;
		}
		@Override
		public void visit(int version, int access, String name,
			String superName, String[] interfaces, String sourceFile) {
			if (interfaces != null && interfaces.length > 0) {
				for (String intf : interfaces) {
					if (Driver.class.getName().replace('.', '/').equals(intf)) {
						driverClass = name;
						break;
					}
				}
			}
			super.visit(version, access, name, superName, interfaces, sourceFile);
		}
		
	}

}