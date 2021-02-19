package org.opennms.configengine.store;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.configengine.store.JSONStore.ModelException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.underscore.lodash.Json.JsonArray;

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
	
	private Map<String, JSONObject> rootmap = new HashMap<String, JSONObject>();
	
	public JSONStore() {
	}
		
	public String setService(String servicename, JSONObject json) throws ModelException {
		rootmap.put(servicename, json);
		return "";
	}
	
	public JSONObject getService(String servicename) {
		if (rootmap.containsKey(servicename)) {
			return rootmap.get(servicename);
		}
		return null;
	}
	
	public String addServiceConfig(String servicename, String[] parts, Object jsonobj) throws ModelException {
		Object currentLevel = rootmap.get(servicename);
		
		// Go down the 'parts' path to the correct level/node.
		// Need to update the second-last since it points to the one we need to change
		int endLevel = parts.length - 1;
		currentLevel = traversePath(parts, currentLevel, endLevel);
		
		// Now add if it doesn't yet exist...
		String key = parts[parts.length-1];
		if (currentLevel instanceof JSONObject) {
			JSONObject obj = (JSONObject) currentLevel;
			if (obj.has(key)) {
				Object matchobj = obj.get(key);
				// This is valid if the key is an array -> adding an item to it
				if (matchobj instanceof JSONArray) {
					JSONArray array = (JSONArray) matchobj;
					array.put(jsonobj);
					return "";
				} else {
					return "Error: Already exists";
				}
			} else {
				obj.put(key, jsonobj);
				return "";
			}
		} else if (currentLevel instanceof JSONArray) {
			// Cannot do a post against an actual array
			return "Error: Invalid post";
		}
		return "Unknown json type";
	}

	public String updateServiceConfig(String servicename, String[] parts, Object jsonobj) {
		Object currentLevel = rootmap.get(servicename);
		
		// Go down the 'parts' path to the correct level/node.
		// Need to update the second-last since it points to the one we need to change
		int endLevel = parts.length - 1;
		try {
			currentLevel = traversePath(parts, currentLevel, endLevel);
		} catch (ModelException e) {
			return "Error: " + e.getLocalizedMessage();
		}
		
		// Now replace...
		String key = parts[parts.length-1];
		if (currentLevel instanceof JSONObject) {
			// Make sure it exists, then replace
			JSONObject obj = (JSONObject) currentLevel;
			if (obj.has(key)) {
				obj.put(key, jsonobj);
				return "";
			} else {
				return "No such item";
			}
		} else if (currentLevel instanceof JSONArray) {
			// Make sure the index is valid
			int index = Integer.parseInt(key);
			JSONArray array = (JSONArray) currentLevel;
			if (array.length() > index) {
				array.put(index, jsonobj);
				return "";
			} else {
				return "Index out of range";
			}
		}
		return "Unknown json type";
	}

	private Object traversePath(String[] levelKeys, Object jsonObj, int numberOfLevels) throws ModelException {
		for (int i = 0; i < numberOfLevels; i++) {
			String part = levelKeys[i];
			if (jsonObj instanceof JSONObject) {
				JSONObject obj = (JSONObject) jsonObj;
				if (obj.has(part)) {
					jsonObj = obj.get(part);
				} else {
					throw new ModelException("Error, " + part + " does not exist");
				}
			} else if (jsonObj instanceof JSONArray) {
				// We're going over an array, the url should be an index
				int index = Integer.parseInt(part);
				JSONArray jsonarray = (JSONArray) jsonObj;
				if (index < jsonarray.length()) {
					jsonObj = jsonarray.get(index);
				} else {
					throw new ModelException("Error, index " + part + " out of range");
				}
			}
		}
		return jsonObj;
	}

	public String getExistingService(String servicename, String[] parts) {
		// Get the item of interest, and return it as a string
		try {
			Object item = traversePath(parts, rootmap.get(servicename), parts.length);
			if (item == null) {
				return "Error";
			} else {
				return item.toString();
			}
		} catch (ModelException e) {
			return e.getLocalizedMessage();
		}	
	}

	public String deleteServiceConfig(String servicename, String[] parts)  {
		// Get the parent of the item of interest
		int level = parts.length - 1;
		
		Object item;
		try {
			item = traversePath(parts, rootmap.get(servicename), level);
		} catch (ModelException e) {
			return e.getLocalizedMessage();
		}
		String key = parts[parts.length - 1];
		if (item != null) {
			if (item instanceof JSONObject) {
				JSONObject obj = (JSONObject) item;
				obj.remove(key);
				return "Removed";
			} else if (item instanceof JSONArray) {
				JSONArray array = (JSONArray) item;
				int index = Integer.parseInt(key);
				if (index >= array.length()) {
					return "Index out of range";
				} else {
					System.out.println("Before\n" + array.toString());
					array.remove(index);
					System.out.println("After removing " + index + ":\n" + array.toString());
					return "Removed";
				}
			}
		}
		return "Error";
	}

}
