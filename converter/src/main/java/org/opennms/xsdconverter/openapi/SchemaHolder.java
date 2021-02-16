package org.opennms.xsdconverter.openapi;

import java.util.ArrayList;
import java.util.Iterator;

public class SchemaHolder {
    ArrayList<SchemaItem> schemaItems = new ArrayList<>();

    public SchemaHolder() {
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
        writer.writeentry(0, "components:");
        writer.writeentry(1, "schemas:");

        // Now go through all the top level schema items and write them out
        for (Iterator<SchemaItem> iterator = schemaItems.iterator(); iterator.hasNext(); ) {
            SchemaItem schemaItem = iterator.next();
            schemaItem.generateYamlSchema(writer, 2);
        }

        writer.writeentry(1, "}");
        writer.writeentry(0, "}");

        return writer.getResult();
    }

}
