package org.opennms.configengine.validation;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.opennms.configengine.store.ServiceRegistry;

public class ServiceValidator {
	ServiceRegistry registry;
	HashMap<String, SwaggerValidator> validators = new HashMap<String, SwaggerValidator>();
	
	public ServiceValidator(ServiceRegistry reg) {
		registry = reg;
	}

	public boolean isRequestValid(String serviceName, HttpServletRequest req, String body) {
		SwaggerValidator validator = validators.get(serviceName);
		if (validator == null) {
			// None exist, try to make one
			String spec = registry.getServiceSpec(serviceName);
			if (spec != null && !spec.isEmpty()) {
				validator = new SwaggerValidator(spec);
				validators.put(serviceName, validator);
			}
		}
		
		if (validator == null) {
			System.out.println("No validator for " + serviceName);
			return false;
		}
		
		return validator.isRequestValid(req, body);
	}

	public void updatedService(String serviceName) {
		// Service will be auto loaded - just remove from the cache
		validators.remove(serviceName);
	}
}
