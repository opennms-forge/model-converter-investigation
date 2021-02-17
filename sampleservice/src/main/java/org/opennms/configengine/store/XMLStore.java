package org.opennms.configengine.store;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLStore {
	public class ModelException extends Exception {
		private static final long serialVersionUID = 3729239728226689170L;

		public ModelException(String msg) {
			super(msg);
		}
		public ModelException(Exception e) {
			super(e);
		}
	}
	
	private Map<String, String> rootmap = new HashMap<String, String>();
	
	public XMLStore() {
	}
	
	private Document convertStringToDocument(String xmlstr) throws ModelException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document xmldoc = builder.parse(new InputSource(new StringReader(xmlstr)));
			return xmldoc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.out.println("Internal exception parsing input data/model");
			throw new ModelException(e);
		}
	}
	
	public String setXML(String servicename, String xml) throws ModelException {
		rootmap.put(servicename, xml);
		return xml;
	}
	
	public String getXML(String servicename) {
		if (rootmap.containsKey(servicename)) {
			return rootmap.get(servicename);
		}
		return "";
	}
}
