package org.opennms.configengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleRequest.Builder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.underscore.lodash.U;

import org.json.*;
import org.opennms.configengine.store.JSONStore;
import org.opennms.configengine.store.ServiceRegistry;
import org.opennms.configengine.store.XMLStore;
import org.opennms.configengine.store.XMLStore.ModelException;
import org.opennms.configengine.validation.ServiceValidator;

@RestController
public class ConfigController {
	static XMLStore xmlstore = new XMLStore();
	static JSONStore jsonstore = new JSONStore();
	static ServiceRegistry registry = new ServiceRegistry();
	static ServiceValidator validator = new ServiceValidator(registry);

	public ConfigController() {
		// Force registration of a vacuumd spec
		String spec;
		try {
			spec = new String(Files.readAllBytes(Paths.get("src/models/vacuumd.yaml")));
			//registry.RegisterService("vacuumd", spec);
		} catch (IOException e) {
			
		}
	}
	

	/*
	@PostMapping(path = "/configuration/xmlservices/{service}", consumes=MediaType.APPLICATION_JSON_VALUE)
	public String postit(@PathVariable("service") String servicename, @RequestBody String json) {
		System.out.println("Received service " + servicename);
		try {
			xmlstore.setXML(servicename, json);
		} catch (ModelException e) {
			return "ERROR";
		}
		return "WORKED";
	}
	
	@GetMapping(path = "/configuration/xmlservices/{service}", produces=MediaType.APPLICATION_JSON_VALUE)
	public String getit(@PathVariable("service") String servicename) {
		System.out.println("Received service " + servicename);
		return xmlstore.getXML(servicename);
	} */

	///////////// JSON //////////////////
	
	@PostMapping(path = "/configuration/schemas/{service}", consumes=MediaType.APPLICATION_JSON_VALUE)
	public String registerServiceSchema(@PathVariable("service") String serviceName, @RequestBody String jsonSchema) {
		registry.RegisterService(serviceName, jsonSchema);
		validator.updatedService(serviceName);
		return "";
	}
	
	@GetMapping("/configuration/schemas")
	public String getServiceList() {
		Set<String> services = registry.getServiceList();
		Iterator it = services.iterator();
		String result = "";
		while (it.hasNext()) {
			result += it.next() + "\n";
		}
		return result;
	}

	@PostMapping(path = "/configuration/services/{service}", consumes=MediaType.APPLICATION_JSON_VALUE)
	public String postjsonservice(HttpServletRequest servletRequest, @PathVariable("service") String servicename, @RequestBody String json) {
		System.out.println("Received service " + servicename);
		
		if (! validator.isRequestValid(servicename, servletRequest, json)) {
			return "Invalid request";
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> mappedJson = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
			jsonstore.setService(servicename, mappedJson);
		} catch (JSONStore.ModelException e) {
			return "ERROR: " + e.getLocalizedMessage();
		} catch (JsonMappingException e) {
			return "ERROR: " + e.getLocalizedMessage();
		} catch (JsonProcessingException e) {
			return "ERROR: " + e.getLocalizedMessage();
		}
		return "WORKED";
	}
	
	@GetMapping(path = "/configuration/services/{service}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map getjsonservice(@PathVariable("service") String servicename) {
		System.out.println("Received service " + servicename);
		return jsonstore.getService(servicename);
	}

	/* @PostMapping(path = "/configuration/services/{service}/**", consumes=MediaType.APPLICATION_JSON_VALUE)
	public String postjsonservicedetails(HttpServletRequest request, @PathVariable("service") String servicename, @RequestBody Map json) {
		String path = request.getServletPath();
		// Remove the /configuration/services/service part
		String startpath = "/configuration/services/" + servicename + "/";
		if (! path.startsWith(startpath)) {
			return "PATH ERROR";
		}
		String target = path.substring(startpath.length());
		System.out.println("Target: " + target);
		String[] parts = target.split("/");
		for (int i = 0; i < parts.length; i++) {
			System.out.println("" + i + ": " + parts[i]);
		}
		
		//Builder requestBuilder = SimpleRequest.Builder.post(path).withBody(request.)
		return jsonstore.updateService(servicename, parts, json);
	} */

	@PostMapping(path = "/configuration/services/{service}/**", consumes=MediaType.APPLICATION_JSON_VALUE)
	public String postjsonservicedetails(HttpServletRequest servletRequest, @PathVariable("service") String servicename, @RequestBody String json) {
		String path = servletRequest.getServletPath();
		// Remove the /configuration/services/service part
		String startpath = "/configuration/services/" + servicename + "/";
		if (! path.startsWith(startpath)) {
			return "PATH ERROR";
		}
		String target = path.substring(startpath.length());
		System.out.println("Target: " + target);
		String[] parts = target.split("/");
		for (int i = 0; i < parts.length; i++) {
			System.out.println("" + i + ": " + parts[i]);
		}
				
		if (! validator.isRequestValid(servicename, servletRequest, json)) {
			return "Invalid request";
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> mappedJson = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
			return jsonstore.updateService(servicename, parts, mappedJson);
		} catch (JsonMappingException e) {
			return "ERROR: " + e.getLocalizedMessage();
		} catch (JsonProcessingException e) {
			return "ERROR: " + e.getLocalizedMessage();
		}
	}

}
