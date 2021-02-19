package org.opennms.xsdconverter.openapi;

import java.util.ArrayList;
import java.util.Iterator;

public class SchemaHolder {
    ArrayList<SchemaItem> schemaItems = new ArrayList<>();
    String servName;
    SchemaItem rootItem = null;

    public SchemaHolder(String serviceName) {
    	servName = serviceName;
    }

    public void addChild(SchemaItem schemaItem) {
        schemaItems.add(schemaItem);
    }

    public ArrayList<SchemaItem> getSchemaItems() {
        return schemaItems;
    }

    public String generateJSONOpenapiDefinitions() {
        SchemaWriter writer = new SchemaWriter(true);
        writer.writeentry(0, "\"components\": {");
        writer.writeentry(1, "\"schemas\": {");

        // Now go through all the top level schema items and write them out
        for (Iterator<SchemaItem> iterator = schemaItems.iterator(); iterator.hasNext(); ) {
            SchemaItem schemaItem = iterator.next();
            schemaItem.generateJSONSchema(writer, 2);
        }

        writer.writeentry(1, "}");
        writer.writeentry(0, "}");

        return writer.getResult();
    }

    public String generateYamlOpenapiDefinitions() {
		SchemaWriter writer = new SchemaWriter(false);

		generateYamlInfo(writer);
		generateYamlEndpoints(writer);
        generateYamlOpenApiComponents(writer);
        generateYamlServers(writer);
        return writer.getResult();
    }

	private void generateYamlServers(SchemaWriter writer) {
		writer.writeentry(0, "servers:");
		writer.writeentry(1, "- url: 'http://localhost:8080/configuration/services'");
	}

	private void generateYamlInfo(SchemaWriter writer) {
		writer.writeentry(0,  "openapi: 3.0.3");
		writer.writeentry(0, "info:");
		writer.writeentry(1, "description: OpenNMS Data Model");
		writer.writeentry(1, "version: 1.0.0");
		writer.writeentry(1, "title: OpenNMS " + servName + " Model");
	}

	private void generateYamlEndpoints(SchemaWriter writer) {
		writer.writeentry(0,  "paths:");
		
		// Write out the root element, then let it traverse for all underneath it
		rootItem.generateYamlEndpoints(writer, 1, "/" + servName, schemaItems, false,
				new SchemaResultHolder(), true, "");
	}

	private void generateYamlOpenApiComponents(SchemaWriter writer) {
        writer.writeentry(0, "components:");
        writer.writeentry(1, "schemas:");

        // Now go through all the top level schema items and write them out
        for (Iterator<SchemaItem> iterator = schemaItems.iterator(); iterator.hasNext(); ) {
            SchemaItem schemaItem = iterator.next();
            schemaItem.generateYamlSchema(writer, 2);
        }
	}

	public void setRootElement(SchemaItem element) {
		rootItem = element;
	}

}
