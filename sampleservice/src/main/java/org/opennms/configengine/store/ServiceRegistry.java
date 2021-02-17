package org.opennms.configengine.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ServiceRegistry {
	private HashMap<String, String> serviceSpecMap = new HashMap<String, String>();
	
	public ServiceRegistry() {
	}
	
	public void RegisterService(String servicename, String openapiSpec) {
		serviceSpecMap.put(servicename, openapiSpec);
	}
	
	public String getServiceSpec(String servicename) {
		return serviceSpecMap.get(servicename);
	}

	public Set<String> getServiceList() {
		return new HashSet<String>(serviceSpecMap.keySet());
	}
}
