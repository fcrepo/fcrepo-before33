package fedora.server.config.webxml;

import java.util.ArrayList;
import java.util.List;

public class ServletMapping {
	private String servletName;
	/**
	 * Only one url-pattern per servlet-mapping is supported pre-Servlet 2.5.
	 */
	private List<String> urlPatterns;
	
	public ServletMapping() {
		urlPatterns = new ArrayList<String>();
	}

	public String getServletName() {
		return servletName;
	}

	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	public List<String> getUrlPatterns() {
		return urlPatterns;
	}

	public void addUrlPattern(String urlPattern) {
		urlPatterns.add(urlPattern);
	}
}
