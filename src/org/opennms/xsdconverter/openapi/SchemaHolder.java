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

    public String generateOpenapiDefinitions() {
        SchemaWriter writer = new SchemaWriter();
        writer.writeentry(0, "components:");
        writer.writeentry(1, "schemas");

        // Now go through all the top level schema items and write them out
        for (Iterator<SchemaItem> iterator = schemaItems.iterator(); iterator.hasNext(); ) {
            SchemaItem schemaItem = iterator.next();
            schemaItem.generateSchema(writer, 2);
        }

        return writer.getResult();
    }
}
