package fedora.utilities.install.container;

import java.io.File;

import fedora.utilities.install.Distribution;
import fedora.utilities.install.InstallOptions;

/**
 * A static factory that returns a Container depending on InstallOptions
 *
 */
public class ContainerFactory {
	private ContainerFactory() {}
	
	public static Container getContainer(Distribution dist, InstallOptions options) {
		String servletEngine = options.getValue(InstallOptions.SERVLET_ENGINE);
		if (servletEngine.equals(InstallOptions.INCLUDED)) {
			return new BundledTomcat(dist, options);
		} else if (servletEngine.equals(InstallOptions.EXISTING_TOMCAT)) {
			File tomcatHome = new File(options.getValue(InstallOptions.TOMCAT_HOME));
        	File dbcp55 = new File(tomcatHome, "common/lib/naming-factory-dbcp.jar");
        	File dbcp6 = new File(tomcatHome, "lib/tomcat-dbcp.jar");
        	if (dbcp55.exists() || dbcp6.exists()) {
        		return new ExistingTomcat(dist, options);
        	} else {
        		return new ExistingTomcat50(dist, options);
        	}
		} else {
			return new DefaultContainer(dist, options);
		}
	}
}
