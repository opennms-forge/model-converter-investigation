package org.opennms.xsdconverter;

import org.opennms.xsdconverter.openapi.SchemaHolder;
import org.opennms.xsdconverter.openapi.SchemaItem;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XSDLoader {
    public XSDLoader() {
    }

    public SchemaHolder loadXSD(String serviceName, File schemaFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            Document doc = builder.parse(schemaFile);

            Element docElement = doc.getDocumentElement();
            SchemaHolder processedSchema = new SchemaHolder(serviceName);
            processTopLevelNodes(docElement.getChildNodes(), processedSchema);
            return processedSchema;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processTopLevelNodes(NodeList childNodes, SchemaHolder processedSchema) {
        int nodes = childNodes.getLength();
        
        //  Using the first top level element as the 'root' of the schema - won't work for all cases
        boolean foundRoot = false;
        
        int index = 0;
        while (index < nodes) {
            Node node = childNodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // Handle the element type
                if (node.getNodeName().equals("element")) {
                    SchemaItem element = processElement(node, "");
                    if (element != null) {
                        if (! foundRoot) {
                        	foundRoot = true;
                        	processedSchema.setRootElement(element);
                        }
                        processedSchema.addChild(element);
                    }
                }
            }
            ++index;
        }
    }

    private SchemaItem processElement(Node element, String schemaPath) {
        // Get the element's attributes (i.e. name)
        NamedNodeMap attrs = element.getAttributes();
        if (attrs == null) {
            System.out.println("Ignoring element - no attrs");
            return null;
        }


        // Element should be a reference to something else, or have a name
        Node refAttr = attrs.getNamedItem("ref");
        Node nameAttr = attrs.getNamedItem("name");
        String name = "";
        String reference = null;
        if (nameAttr != null) {
            name = attrs.getNamedItem("name").getNodeValue();
        }
        if (refAttr != null) {
            String rawReference = refAttr.getNodeValue();
            reference = convertReference(rawReference);
            name = nameFromReference(rawReference);
        }
        if (!name.isEmpty()) {
        	schemaPath += name;
        }
        
        SchemaItem processedElement = new SchemaItem(name);
        processedElement.setSchemaPath(schemaPath);
        setItemType(processedElement, attrs); // Sets max/min also

        if (reference != null) {
            processedElement.setReference(reference); // Automatically sets the type also
        }

        // Now go through and find any children
        processChildren(processedElement, element, schemaPath + "/properties/" );
        return processedElement;
    }

    private void setItemType(SchemaItem processedElement, NamedNodeMap attrs) {
        // Find any defined bounds
        int maxOccurs = getMaxOccurs(attrs); // Will be -1 for unbounded
        boolean unbounded = (maxOccurs == -1);
        int minOccurs = getMinOccurs(attrs);

        processedElement.setArrayMax(maxOccurs);
        processedElement.setArrayMin(minOccurs);
        if (maxOccurs != 1) {
            processedElement.setType(SchemaItem.Type.arrayType);
        }


        String xsdType = getAttributeValue(attrs, "type");
        setTypes(processedElement, xsdType);
    }

    private void setTypes(SchemaItem item, String xsdType) {
        SchemaItem.Type type = SchemaItem.Type.undefined;
        String format = "";

        switch(xsdType) {
            case "string": type = SchemaItem.Type.stringType;
                break;
            case "boolean": type = SchemaItem.Type.booleanType;
                break;
            case "decimal": type = SchemaItem.Type.integerType;
            case "double": type = SchemaItem.Type.numberType;
                format = "double";
                break;
            case "integer":
            case "int": type = SchemaItem.Type.integerType;
                break;
            case "long": type = SchemaItem.Type.integerType;
                format = "int64";
                break;

            case "base64Binary":
            case "hexBinary":
            case "float":
            case "anyURI":
            case "QName":
            case "NOTATION":
            case "duration":
            case "dateTime":
            case "time":
            case "date":
            case "gYearMonth":
            case "gYear":
            case "gMonthDay":
            case "gDay":
            case "gMonth":
            case "normalizedString":
            case "token":
            case "language":
            case "Name":
            case "NCName":
            case "NMTOKEN":
            case "NMTOKENS":
            case "ID":
            case "IDREF":
            case "IDREFS":
            case "ENTITY":
            case "ENTITIES":
            case "nonPositiveInteger":
            case "negativeInteger":
            case "short":
            case "byte":
            case "nonNegativeInteger":
            case "unsignedLong":
            case "positiveInteger":
            case "unsignedInt":
            case "unsignedShort":
            case "unsignedByte":
            default:
        }
        if (type != SchemaItem.Type.undefined) {
            item.setType(type);
            if (! format.isEmpty()) {
                item.setFormat(format);
            }
        }

    }

    private void processChildren(SchemaItem processedParent, Node element, String schemaPath) {
        // Different places for processing children: attributes (mapping as if elements), complexType
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "complexType".equals(child.getNodeName())) {
                processComplexType(processedParent, element, child, schemaPath);
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "simpleType".equals(child.getNodeName())) {
                processSimpleType(processedParent, child);
            }
        }
    }

    private void processSimpleType(SchemaItem processedParent, Node simpleTypeNode) {
        NodeList children = simpleTypeNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "restriction".equals(child.getNodeName())) {
                processTypeRestriction(processedParent, child);
            }
        }
    }

    private void processTypeRestriction(SchemaItem processedParent, Node restrictionNode) {
        NamedNodeMap attrs = restrictionNode.getAttributes();
        if (attrs == null) {
            return;
        }

        String baseType = getAttributeValue(attrs, "base");
        if (baseType != null && !baseType.isEmpty()) {
            setTypes(processedParent, baseType);
        }
    }

    private void processComplexType(SchemaItem processedParent, Node element, Node complexTypeChild,
    		String schemaPath) {
        // Complex type can have several different things in it, including attributes
        NodeList children = complexTypeChild.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "sequence".equals(child.getNodeName())) {
                processSequence(processedParent, element, child, schemaPath);
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "attribute".equals(child.getNodeName())) {
                processAttribute(processedParent, element, child, schemaPath);
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "simpleContent".equals(child.getNodeName())) {
                processSimpleContent(processedParent, child);
            }
        }
    }

    private void processSimpleContent(SchemaItem processedParent, Node simpleContentNode) {
        // Should contain an extension with the base type - apply this to the parent as we're really generating it's type
        NodeList children = simpleContentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "extension".equals(child.getNodeName())) {
                if (processTypeExtension(processedParent, child)) {
                    return;
                }
            }
        }
    }

    private boolean processTypeExtension(SchemaItem processedParent, Node extensionNode) {
        NamedNodeMap attrs = extensionNode.getAttributes();

        if (attrs == null) {
            return false;
        }

        String baseType = getAttributeValue(attrs, "base");
        setTypes(processedParent, baseType);
        return true;
    }

    private void processAttribute(SchemaItem processedParent, Node parentNode, Node attributeNode, String schemaPath) {
        // Handle the same as an element for now. Name will be the same - other stuff will differ
        SchemaItem processedAttribute = processElement(attributeNode, schemaPath);
        if (processedAttribute != null) {
            processedParent.addChild(processedAttribute);
            if (processedParent.getType() == SchemaItem.Type.undefined) {
                processedParent.setType(SchemaItem.Type.objectType);
            }
        }
    }

    private void processSequence(SchemaItem processedParent, Node element, Node sequence, String schemaPath) {
        NodeList sequenceChildren = sequence.getChildNodes();
        for (int i = 0; i < sequenceChildren.getLength(); i++) {
            Node child = sequenceChildren.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "element".equals(child.getNodeName())) {
                SchemaItem processedElement = processElement(child, schemaPath);
                if (processedElement != null) {
                    processedParent.addChild(processedElement);
                    if (processedParent.getType() == SchemaItem.Type.undefined) {
                        processedParent.setType(SchemaItem.Type.objectType);
                    }
                }
            }
        }
    }

    private String getAttributeValue(NamedNodeMap attrs, String attrName) {
        if (attrs == null) {
            return "";
        }

        Node node = attrs.getNamedItem(attrName);
        if (node == null) {
            // Not defined, use default of 1
            return "";
        }

        return node.getNodeValue();
    }

    private int getMinOccurs(NamedNodeMap attrs) {
        if (attrs == null) {
            return 1;
        }

        Node minNode = attrs.getNamedItem("minOccurs");
        if (minNode == null) {
            // Not defined, use default of 1
            return 1;
        }

        String minValue = minNode.getNodeValue();
        try {
            return Integer.parseInt(minValue);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing minOccurs of " + minValue);
        }
        return 1;
    }

    private int getMaxOccurs(NamedNodeMap attrs) {
        if (attrs == null) {
            return 1;
        }
        // -1 corresponds to unbounded
        Node maxNode = attrs.getNamedItem("maxOccurs");
        if (maxNode == null) {
            // Not defined, use default of 1
            return 1;
        }

        String maxValue = maxNode.getNodeValue();
        if ("unbounded".equals(maxValue)) {
            return -1;
        }
        try {
            return Integer.parseInt(maxValue);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing maxOccurs of " + maxValue);
        }
        return 1;
    }

    private String nameFromReference(String ref) {
        return ref.split(":")[1];
    }

    private String convertReference(String rawReference) {
        // Right now just assume all references are local
        // Will need to understand how the namespaces come in to play with combined schemas eventually
        return "#/components/schemas/" + rawReference.split(":")[1];
    }

}
