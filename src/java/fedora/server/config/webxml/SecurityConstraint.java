package fedora.server.config.webxml;

import java.util.ArrayList;
import java.util.List;

public class SecurityConstraint {
	private List<WebResourceCollection> webResourceCollections;
	private AuthConstraint authConstraint;
	private UserDataConstraint userDataConstraint;
	
	public SecurityConstraint() {
		webResourceCollections = new ArrayList<WebResourceCollection>();
	}
	
	public List<WebResourceCollection> getWebResourceCollections() {
		return webResourceCollections;
	}
	
	public void addWebResourceCollection(WebResourceCollection webResourceCollection) {
		webResourceCollections.add(webResourceCollection);
	}
	
	public void removeWebResourceCollection(WebResourceCollection webResourceCollection) {
		webResourceCollections.remove(webResourceCollection);
	}

	public AuthConstraint getAuthConstraint() {
		return authConstraint;
	}

	public void setAuthConstraint(AuthConstraint authConstraint) {
		this.authConstraint = authConstraint;
	}

	public UserDataConstraint getUserDataConstraint() {
		return userDataConstraint;
	}

	public void setUserDataConstraint(UserDataConstraint userDataConstraint) {
		this.userDataConstraint = userDataConstraint;
	}
}