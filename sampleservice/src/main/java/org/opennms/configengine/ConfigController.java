package org.opennms.configengine;

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

import com.github.underscore.lodash.U;

import org.json.*;
import org.opennms.configengine.types.JSONStore;
import org.opennms.configengine.types.XMLStore;
import org.opennms.configengine.types.XMLStore.ModelException;

@RestController
public class ConfigController {
	static XMLStore xmlstore = new XMLStore();
	static JSONStore jsonstore = new JSONStore();

	
	@GetMapping("/configuration/services")
	public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return "{ \"key\": \"worked\" }";
	}
	
	/* @PostMapping("/configuration/services")
	public String postit(@RequestBody LinkedHashMap body) {
		System.out.println("Received " + body.getClass().getName());
		Set<String> keys = body.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			System.out.println("- " + it.next());
		}
		Collection values = body.values();
		it = values.iterator();
		while (it.hasNext()) {
			System.out.println("+ Type: " + it.next().getClass().getName());
		}
		return "WORKED";
	} */
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
	}

	///////////// JSON //////////////////
	@PostMapping(path = "/configuration/services/{service}", consumes=MediaType.APPLICATION_JSON_VALUE)
	public String postjsonservice(@PathVariable("service") String servicename, @RequestBody Map json) {
		System.out.println("Received service " + servicename);
		try {
			jsonstore.setService(servicename, json);
		} catch (JSONStore.ModelException e) {
			return "ERROR";
		}
		return "WORKED";
	}
	
	@GetMapping(path = "/configuration/services/{service}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map getjsonservice(@PathVariable("service") String servicename) {
		System.out.println("Received service " + servicename);
		return jsonstore.getService(servicename);
	}

	@PostMapping(path = "/configuration/services/{service}/**", consumes=MediaType.APPLICATION_JSON_VALUE)
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
		return jsonstore.updateService(servicename, parts, json);
	}

}
