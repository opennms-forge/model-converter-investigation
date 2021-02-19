package org.opennms.xsdconverter.openapi;

import java.util.ArrayList;
import java.util.Iterator;

public class SchemaItem {
    String name;
    String componentPath;

    public enum Type {
        undefined,
        objectType,
        arrayType,
        stringType,
        numberType,
        integerType,
        booleanType
    }
    Type type = Type.undefined;
    boolean referenceType = false; // Can be combined with an object or array type

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
        // If no type, then default for object with a reference
        if (this.type == Type.undefined) {
        	this.type = Type.objectType;
        }
        this.referenceType = true;
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
    	if (referenceType) {
    		return null;
    	}
        switch (type) {
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
        if ((type != Type.undefined) && (! referenceType)) {
            writer.writeentry(level + 1, "\"type\": \"" + getOpenapiTypeName() + "\"");
            if (format != null && !format.isEmpty()) {
                writer.writeentry(level + 1, "\"format\": " + format);
            }
        }

        if (referenceType) {
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
        if ((type != Type.undefined) && (!referenceType)) {
            writer.writeentry(level + 1, "type: " + getOpenapiTypeName());
            if (format != null && !format.isEmpty()) {
                writer.writeentry(level + 1, "format: " + format);
            }
        }

        if (referenceType) {
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

	public void generateYamlEndpoints(SchemaWriter writer, int level, String url,
					ArrayList<SchemaItem> topSchemas, boolean parentArray,
					SchemaResultHolder parentParams, boolean rootElement,
					String tagName) {
        boolean isArray = (type == Type.arrayType);
        String schemaType;
        if (rootElement) {
        	tagName = name;
        }
		if (parentArray) {
			// Need to add the info for the parameter in the url
			parentParams.addLine(0,  "- in: path");
			parentParams.addLine(1,  "name: " + name + "Index");
			parentParams.addLine(1,  "schema:");
			parentParams.addLine(2,  "type: integer");
			parentParams.addLine(1,  "required: true");
			parentParams.addLine(1,  "description: Index of item in the array");
		}

		if (!referenceType || isArray) {
			// If the parent is an array, then we use an index for our level of the url instead of the name
			if (!rootElement) {
				// Don't add the name of the root element into the url
				if (parentArray) {
					url += "/{" + name + "Index}";  // assuming all name indexes will be unique - probably not valid
				} else {
					url += "/" + name;
				}
			}
			// TODO Auto-generated method stub
			writer.writeentry(level, url + ":");			
					
			if (!parentArray) {
				// POST
				writer.writeentry(level + 1,  "post:");
				writer.writeentry(level + 2,  "tags:");
				writer.writeentry(level + 3,  "- " + tagName);
				writer.writeentry(level + 2,  "summary: " + "Configure " + name);
				if (!parentParams.isEmpty()) {
					writer.writeentry(level + 2,  "parameters:");
					parentParams.writeLines(writer, level + 3);
				}
				writer.writeentry(level + 2,  "requestBody:");
				writer.writeentry(level + 3,  "content:");
				writer.writeentry(level + 4,  "application/json:");
				writer.writeentry(level + 5,  "schema:");
				//if (isArray) {
				//	writer.writeentry(level + 6,  "type: array");
				//	writer.writeentry(level + 6,  "items:");
				//	writer.writeentry(level + 7,  "$ref: \"#/components/schemas/" + componentPath + "\"");
				//} else {
					writer.writeentry(level + 6,  "$ref: \"#/components/schemas/" + componentPath + "\"");
				//}
				writer.writeentry(level + 5,  "example:");
				writer.writeentry(level + 2, "responses:");
				writer.writeentry(level + 3,  "'200':");
				writer.writeentry(level + 4,  "description: OK");
			}
			
			// PUT
			writer.writeentry(level + 1,  "put:");
			writer.writeentry(level + 2,  "tags:");
			writer.writeentry(level + 3,  "- " + tagName);
			writer.writeentry(level + 2,  "summary: " + "Configure " + name);
			if (!parentParams.isEmpty()) {
				writer.writeentry(level + 2,  "parameters:");
				parentParams.writeLines(writer, level + 3);
			}
			writer.writeentry(level + 2,  "requestBody:");
			writer.writeentry(level + 3,  "content:");
			writer.writeentry(level + 4,  "application/json:");
			writer.writeentry(level + 5,  "schema:");
			if (isArray) {
				writer.writeentry(level + 6,  "type: array");
				writer.writeentry(level + 6,  "items:");
				writer.writeentry(level + 7,  "$ref: \"#/components/schemas/" + componentPath + "\"");
			} else {
				writer.writeentry(level + 6,  "$ref: \"#/components/schemas/" + componentPath + "\"");
			}
			writer.writeentry(level + 5,  "example:");
			writer.writeentry(level + 2, "responses:");
			writer.writeentry(level + 3,  "'200':");
			writer.writeentry(level + 4,  "description: OK");
			
			// GET
			writer.writeentry(level + 1,  "get:");
			writer.writeentry(level + 2,  "tags:");
			writer.writeentry(level + 3,  "- " + tagName);
			writer.writeentry(level + 2,  "summary: " + "Get " + name + " configuration");
			if (!parentParams.isEmpty()) {
				writer.writeentry(level + 2,  "parameters:");
				parentParams.writeLines(writer, level + 3);
			}

			writer.writeentry(level + 2,  "responses:");
			writer.writeentry(level + 3,  "200:");
			writer.writeentry(level + 4,  "description: " + name + " configuration");
			writer.writeentry(level + 4,  "content:");
			writer.writeentry(level + 5,  "application/json:");
			writer.writeentry(level + 6,  "schema:");
			if (isArray) {
				writer.writeentry(level + 7,  "type: array");
				writer.writeentry(level + 7,  "items:");
				writer.writeentry(level + 8,  "$ref: \"#/components/schemas/" + componentPath + "\"");
			} else {
				writer.writeentry(level + 7,  "$ref: \"#/components/schemas/" + componentPath + "\"");
			}
			writer.writeentry(level + 6,  "example:");
			
			if (parentArray) {
				// DELETE
				writer.writeentry(level + 1,  "delete:");
				writer.writeentry(level + 2,  "tags:");
				writer.writeentry(level + 3,  "- " + tagName);
				writer.writeentry(level + 2,  "summary: " + "Delete " + name);
				if (!parentParams.isEmpty()) {
					writer.writeentry(level + 2,  "parameters:");
					parentParams.writeLines(writer, level + 3);
				}
				writer.writeentry(level + 2, "responses:");
				writer.writeentry(level + 3,  "'200':");
				writer.writeentry(level + 4,  "description: OK");

			}
	
			// Now go through all child elements for their endpoints
	        for (int i = 0; i < children.size(); i++) {
	            SchemaItem schemaItem = children.get(i);
	            String childTag;
	            if (rootElement) {
	            	childTag = schemaItem.getName();
	            } else {
	            	childTag = tagName;
	            }
	            schemaItem.generateYamlEndpoints(writer, level, url, topSchemas, isArray,
	            		new SchemaResultHolder(parentParams), false, childTag);
	        }
        }
        if (referenceType) {
        	SchemaItem refItem = findReference(topSchemas);
        	if (refItem != null) {
	            String childTag;
	            if (rootElement) {
	            	childTag = refItem.getName();
	            } else {
	            	childTag = tagName;
	            }

        		refItem.generateYamlEndpoints(writer, level, url, topSchemas, isArray,
        				new SchemaResultHolder(parentParams), false, childTag);
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
