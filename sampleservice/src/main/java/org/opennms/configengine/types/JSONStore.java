package org.opennms.configengine.types;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JSONStore {
	public class ModelException extends Exception {
		private static final long serialVersionUID = 3729239728226689171L;

		public ModelException(String msg) {
			super(msg);
		}
		public ModelException(Exception e) {
			super(e);
		}
	}
	
	private Map<String, Map> rootmap = new HashMap<String, Map>();
	
	public JSONStore() {
	}
		
	public String setService(String servicename, Map<String, Map> config) throws ModelException {
		rootmap.put(servicename, config);
		return "";
	}
	
	public Map<String, Map> getService(String servicename) {
		if (rootmap.containsKey(servicename)) {
			return rootmap.get(servicename);
		}
		return null;
	}

	public String updateService(String servicename, String[] path, Map json) {
		Object item = rootmap.get(servicename);
		for (int i = 0; i < path.length; i++) {
			if (! (item instanceof Map)) {
				return "INVALID PATH";
			}
			@SuppressWarnings("rawtypes")
			Map itemmap = (Map) item;
			System.out.println("Looking for '" + path[i] + "' in:" + itemmap);
			if (! itemmap.containsKey(path[i])) {
				return "ERROR";
			}
			item = itemmap.get(path[i]);
		}
		
		return updateJsonItem(item, json);
	}

	private String updateJsonItem(Object item, Map json) {
		if (item instanceof String) {
			return "ERROR - string replacement not implemented";
		} else if (item instanceof ArrayList) {
			updateOrAddJsonArray((ArrayList) item, json);
		} else {
			System.out.println("Match json to type " + item.getClass().getName());
		}
		return "Done";
	}

	private String updateOrAddJsonArray(ArrayList item, Map json) {
		String key = (String) json.get("name");
		int index = 0;
		
		while (index < item.size()) {
			Object arrayItem = item.get(index);
			if (arrayItem instanceof Map) {
				String compareKey = (String) ((Map) arrayItem).get("name");
				if (key.equals(compareKey)) {
					item.set(index,  json);
					return "REPLACED";
				}
			}

			++index;
		}
		
		item.add(json);
		return "APPENDED";
	}
}
