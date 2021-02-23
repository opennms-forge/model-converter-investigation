package org.opennms.configengine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.fasterxml.jackson.databind.JsonNode;
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
	


	///////////// Configuring service schemas
	
	@PostMapping(path = "/configuration/schemas/{service}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JsonNode> registerServiceSchema(@PathVariable("service") String serviceName, @RequestBody String jsonSchema) {
		registry.RegisterService(serviceName, jsonSchema);
		validator.updatedService(serviceName);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping(path="/configuration/schemas", produces=MediaType.TEXT_PLAIN_VALUE)
	public String getServiceList() {
		Set<String> services = registry.getServiceList();
		Iterator it = services.iterator();
		String result = "";
		while (it.hasNext()) {
			result += it.next() + "\n";
		}
		return result;
	}

	
	/////////////////////// Top level configuration of services
	@PostMapping(path = "/configuration/services/{service}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JsonNode> postjsonservice(HttpServletRequest servletRequest, @PathVariable("service") String servicename, @RequestBody String json) {
		if (! validator.isRequestValid(servicename, servletRequest, json)) {
			return ResponseEntity.badRequest().build();
		}

		JSONObject jsonobj = new JSONObject(json);
		try {
			jsonstore.setService(servicename, jsonobj);
		} catch (org.opennms.configengine.store.JSONStore.ModelException e) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok().build();
	}
	
	@GetMapping(path = "/configuration/services/{service}", produces=MediaType.APPLICATION_JSON_VALUE)
	public String getjsonservice(@PathVariable("service") String servicename) {
		JSONObject serviceObj = jsonstore.getService(servicename);
		if (serviceObj == null) {
			return "Error";
		}
		return serviceObj.toString();
	}

	/////////// Endpoints for  inside the service
	@PostMapping(path = "/configuration/services/{service}/**", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JsonNode> postjsonservicedetails(HttpServletRequest servletRequest, @PathVariable("service") String servicename, @RequestBody String json) {
		if (! validator.isRequestValid(servicename, servletRequest, json)) {
			return ResponseEntity.badRequest().build();
		}

		String path = servletRequest.getServletPath();
		// Remove the /configuration/services/service part
		String startpath = "/configuration/services/" + servicename + "/";
		if (! path.startsWith(startpath)) {
			return ResponseEntity.notFound().build();
		}
		String target = path.substring(startpath.length());
		String[] parts = target.split("/");
				
		JSONTokener jsontokener = new JSONTokener(json);
		Object jsonobj = jsontokener.nextValue();
		
		try {
			jsonstore.addServiceConfig(servicename, parts, jsonobj);
			return ResponseEntity.ok().build();
		} catch (org.opennms.configengine.store.JSONStore.ModelException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping(path = "/configuration/services/{service}/**", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JsonNode> putjsonservicedetails(HttpServletRequest servletRequest, @PathVariable("service") String servicename, @RequestBody String json) {
		if (! validator.isRequestValid(servicename, servletRequest, json)) {
			return ResponseEntity.badRequest().build();
		}

		String path = servletRequest.getServletPath();
		// Remove the /configuration/services/service part
		String startpath = "/configuration/services/" + servicename + "/";
		if (! path.startsWith(startpath)) {
			return ResponseEntity.notFound().build();
		}
		String target = path.substring(startpath.length());
		String[] parts = target.split("/");
				
		JSONTokener jsontokener = new JSONTokener(json);
		Object jsonobj = jsontokener.nextValue();
		
		try {
			jsonstore.updateServiceConfig(servicename, parts, jsonobj);
		} catch (org.opennms.configengine.store.JSONStore.ModelException e) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/configuration/services/{service}/**", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JsonNode> getjsonservicedetails(HttpServletRequest servletRequest, @PathVariable("service") String servicename) {
		if (! validator.isRequestValid(servicename, servletRequest, "")) {
			return ResponseEntity.badRequest().build();
		}

		String path = servletRequest.getServletPath();
		// Remove the /configuration/services/service part
		String startpath = "/configuration/services/" + servicename + "/";
		if (! path.startsWith(startpath)) {
			return ResponseEntity.notFound().build();
		}
		String target = path.substring(startpath.length());
		String[] parts = target.split("/");
				
		String strjson = jsonstore.getExistingService(servicename, parts);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode json = mapper.readTree(strjson);
			System.out.println("Response: " + strjson);
			return ResponseEntity.ok(json);
		} catch (JsonMappingException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (JsonProcessingException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping(path = "/configuration/services/{service}/**", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JsonNode> deletejsonservicedetails(HttpServletRequest servletRequest, @PathVariable("service") String servicename) {
		if (! validator.isRequestValid(servicename, servletRequest, "")) {
			return ResponseEntity.badRequest().build();
		}

		String path = servletRequest.getServletPath();
		// Remove the /configuration/services/service part
		String startpath = "/configuration/services/" + servicename + "/";
		if (! path.startsWith(startpath)) {
			return ResponseEntity.notFound().build();
		}
		String target = path.substring(startpath.length());
		String[] parts = target.split("/");
				
		try {
			jsonstore.deleteServiceConfig(servicename, parts);
		} catch (org.opennms.configengine.store.JSONStore.ModelException e) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok().build();
	}


}
