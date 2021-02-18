package org.opennms.xsdconverter.openapi;

import java.util.ArrayList;
import java.util.Iterator;

public class SchemaItem {
    String name;
    String componentPath;

    public enum Type {
        undefined,
        reference, // References can also be arrays
        objectType,
        arrayType,
        stringType,
        numberType,
        integerType,
        booleanType
    }
    Type type = Type.undefined;

    String format = ""; // Optional free-form in openapi spec

    ArrayList<SchemaItem> children = new ArrayList<>();
    String reference = "";
    int arrayMax = 1;
    int arrayMin = 1;

    public SchemaItem() {
    }

    public SchemaItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void addChild(SchemaItem child) {
        children.add(child);
    }

    public ArrayList<SchemaItem> getChildren() {
        return (ArrayList<SchemaItem>) children.clone();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
        this.type = Type.reference;
    }

    public int getArrayMax() {
        return arrayMax;
    }

    public void setArrayMax(int arrayMax) {
        this.arrayMax = arrayMax;
    }

    public int getArrayMin() {
        return arrayMin;
    }

    public void setArrayMin(int arrayMin) {
        this.arrayMin = arrayMin;
    }

    private String getOpenapiTypeName() {
        switch (type) {
            case reference: return null;
            case objectType: return "object";
            case arrayType: return "array";
            case stringType: return "string";
            case numberType: return "number";
            case integerType: return "integer";
            case booleanType: return "boolean";
            case undefined: return "UNDEFINED";
        }
        return null;
    }

    public void generateJSONSchema(SchemaWriter writer, int level) {
        writer.writeentry(level, "\"" + name + "\": {");
        if ((type != Type.undefined) && (type != Type.reference)) {
            writer.writeentry(level + 1, "\"type\": \"" + getOpenapiTypeName() + "\"");
            if (format != null && !format.isEmpty()) {
                writer.writeentry(level + 1, "\"format\": " + format);
            }
        }

        if (type == Type.reference) {
            if (arrayMax == 1) {
                writer.writeentry(level + 1, "\"$ref\": \"" + reference + "\"");
            } else {
                // If it is an array reference, formatting is a little different
                writer.writeentry(level + 1, "\"type\": \"array\"");
                writer.writeentry(level + 1, "\"items\": {");
                writer.writeentry(level + 2, "\"$ref\": \"" + reference+ "\"");
                writer.writeentry(level + 1, "}");
            }
        } else if (type == Type.objectType) {
            writer.writeentry(level + 1, "\"properties\": {");

            for (int i = 0; i < children.size(); i++) {
                SchemaItem schemaItem = children.get(i);
                schemaItem.generateJSONSchema(writer, level + 2);
            }
            writer.writeentry(level + 1, "}");
        }
        writer.writeentry(level, "}");
    }

    public void generateYamlSchema(SchemaWriter writer, int level) {
        writer.writeentry(level, name + ":");
        if ((type != Type.undefined) && (type != Type.reference)) {
            writer.writeentry(level + 1, "type: " + getOpenapiTypeName());
            if (format != null && !format.isEmpty()) {
                writer.writeentry(level + 1, "format: " + format);
            }
        }

        if (type == Type.reference) {
            if (arrayMax == 1) {
                writer.writeentry(level + 1, "$ref: '" + reference + "'");
            } else {
                // If it is an array reference, formatting is a little different
                writer.writeentry(level + 1, "type: array");
                writer.writeentry(level + 1, "items:");
                writer.writeentry(level + 2, "$ref: '" + reference+ "'");
            }
        } else if (type == Type.objectType) {
            writer.writeentry(level + 1, "properties:");

            for (int i = 0; i < children.size(); i++) {
                SchemaItem schemaItem = children.get(i);
                schemaItem.generateYamlSchema(writer, level + 2);
            }
        }
    }

	public void generateYamlEndpoints(SchemaWriter writer, int level, String url, ArrayList<SchemaItem> topSchemas) {
        if (type == Type.reference) {
        	SchemaItem refItem = findReference(topSchemas);
        	if (refItem != null) {
        		refItem.generateYamlEndpoints(writer, level, url, topSchemas);
        	}
        } else {
			url += "/" + name;
			// TODO Auto-generated method stub
			writer.writeentry(level, url + ":");
			// POST
			writer.writeentry(level + 1,  "post:");
			writer.writeentry(level + 2,  "tags:");
			writer.writeentry(level + 3,  "- " + name);
			writer.writeentry(level + 2,  "summary: " + "Configure " + name);
			writer.writeentry(level + 2,  "requestBody:");
			writer.writeentry(level + 3,  "content:");
			writer.writeentry(level + 4,  "application/json:");
			writer.writeentry(level + 5,  "schema:");
			writer.writeentry(level + 6,  "$ref: \"#/components/schemas/" + componentPath + "\"");
			writer.writeentry(level + 5,  "example:");
			writer.writeentry(level + 2, "responses:");
			writer.writeentry(level + 3,  "'200':");
			writer.writeentry(level + 4,  "description: OK");
			// GET
			writer.writeentry(level + 1,  "get:");
			writer.writeentry(level + 2,  "tags:");
			writer.writeentry(level + 3,  "- " + name);
			writer.writeentry(level + 2,  "summary: " + "Get " + name + " configuration");
			writer.writeentry(level + 2,  "responses:");
			writer.writeentry(level + 3,  "200:");
			writer.writeentry(level + 4,  "description: " + name + " configuration");
			writer.writeentry(level + 4,  "content:");
			writer.writeentry(level + 5,  "application/json:");
			writer.writeentry(level + 6,  "schema:");
			writer.writeentry(level + 7,  "$ref: \"#/components/schemas/" + componentPath + "\"");
			writer.writeentry(level + 6,  "example:");
	
			// Now go through all child elements for their endpoints
	        for (int i = 0; i < children.size(); i++) {
	            SchemaItem schemaItem = children.get(i);
	            schemaItem.generateYamlEndpoints(writer, level, url, topSchemas);
	        }
        }
	}

	private SchemaItem findReference(ArrayList<SchemaItem> topSchemas) {
		String refName = reference.replace("#/components/schemas/", "");
		Iterator<SchemaItem> it = topSchemas.iterator();
		while (it.hasNext()) {
			SchemaItem item = it.next();
			if (item.isReference(refName)) {
				return item;
			}
		}
		return null;
	}

	private boolean isReference(String refName) {
		return name.equals(refName);
	}

	public void setSchemaPath(String schemaPath) {
		componentPath = schemaPath;
	}
}
