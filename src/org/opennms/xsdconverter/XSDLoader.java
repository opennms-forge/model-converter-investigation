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

    public SchemaHolder loadXSD(File schemaFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            Document doc = builder.parse(schemaFile);

            Element docElement = doc.getDocumentElement();
            SchemaHolder processedSchema = new SchemaHolder();
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
        int index = 0;
        while (index < nodes) {
            Node node = childNodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // Handle the element type
                if (node.getNodeName().equals("element")) {
                    SchemaItem element = processElement(node);
                    if (element != null) {
                        processedSchema.addChild(element);
                    }
                }
            }
            ++index;
        }
    }

    private SchemaItem processElement(Node element) {
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

        SchemaItem processedElement = new SchemaItem(name);
        setItemType(processedElement, attrs); // Sets max/min also

        if (reference != null) {
            processedElement.setReference(reference); // Automatically sets the type also
        }

        // Now go through and find any children
        processChildren(processedElement, element);
        return processedElement;
        //outputItem(level, name + ":");

            /*
            if (refAttr != null) {
                // If it is an array, the format is a little different than a single element
                if (maxOccurs == 1) {
                    outputItem(level + 1, "$ref: \"" + reference + "\"");
                } else {
                    outputItem(level + 1, "type: array");
                    outputItem(level + 1, "items:");
                    outputItem(level + 2, "$ref: \"" + reference + "\"");
                }
                return;
            }


            ++level; // All subsequent items indented from this parent

            Node typeContainer = getInterestingElementType(element);
            if (typeContainer != null) {
                if ("complexType".equals(typeContainer.getNodeName())) {
                    // Maps to an object type
                    outputItem(level, "type: object");

                    // The complexType can hold a bunch of attributes. Map them as if they were child elements in a sequence
                    processComplexTypeAttributes(level, typeContainer);

                    // If it contains a sequence of children, they go under a properties tag
                    Node complexTypeChild = getComplexTypeChildContainer(typeContainer);
                    if (complexTypeChild != null) {
                        if ("sequence".equals(complexTypeChild.getNodeName())) {
                            outputItem(level, "properties:");
                            processNodes(complexTypeChild.getChildNodes(), level + 1);
                        }
                    }
                }
            }*/
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
            System.out.println("Set array type, max is " + maxOccurs + " for node " + processedElement.getName());
        }


        String xsdType = getAttributeValue(attrs, "type");
        SchemaItem.Type convertedType = getOpenapiType(xsdType);
        if (convertedType != null) {
            processedElement.setType(convertedType);
        }
    }

    private SchemaItem.Type getOpenapiType(String xsdType) {
        switch(xsdType) {
            case "string": return SchemaItem.Type.stringType;
            case "boolean": return SchemaItem.Type.booleanType;
            case "decimal":
            case "double":
                return SchemaItem.Type.numberType;
            case "int": return SchemaItem.Type.integerType;

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
            case "integer":
            case "nonPositiveInteger":
            case "negativeInteger":
            case "long":
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
        return null;
    }

    private void processChildren(SchemaItem processedParent, Node element) {
        // Different places for processing children: attributes (mapping as if elements), complexType
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            System.out.println("CHILD TYPE " + child.getNodeType() + ", " + child.getNodeName() + ", " + child.getNodeValue());
            if (child.getNodeType() == Node.ELEMENT_NODE && "complexType".equals(child.getNodeName())) {
                processComplexType(processedParent, element, child);
            }
        }
    }

    private void processComplexType(SchemaItem processedParent, Node element, Node complexTypeChild) {
        // Complex type can have several different things in it, including attributes
        NodeList children = complexTypeChild.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            System.out.println("ComplexType child: " + child.getNodeType() + ", " + child.getNodeName() + ", " + child.getNodeValue());
            if (child.getNodeType() == Node.ELEMENT_NODE && "sequence".equals(child.getNodeName())) {
                processSequence(processedParent, element, child);
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "attribute".equals(child.getNodeName())) {
                processAttribute(processedParent, element, child);
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
        SchemaItem.Type openapiType = getOpenapiType(baseType);
        if (openapiType != null) {
            processedParent.setType(openapiType);
        }
        return true;
    }

    private void processAttribute(SchemaItem processedParent, Node parentNode, Node attributeNode) {
        // Handle the same as an element for now. Name will be the same - other stuff will differ
        SchemaItem processedAttribute = processElement(attributeNode);
        if (processedAttribute != null) {
            processedParent.addChild(processedAttribute);
            if (processedParent.getType() == SchemaItem.Type.undefined) {
                processedParent.setType(SchemaItem.Type.objectType);
            }
        }
    }

    private void processSequence(SchemaItem processedParent, Node element, Node sequence) {
        NodeList sequenceChildren = sequence.getChildNodes();
        for (int i = 0; i < sequenceChildren.getLength(); i++) {
            Node child = sequenceChildren.item(i);
            System.out.println("SEQUENCE CHILD: " + child.getNodeType() + ", " + child.getNodeName() + ", " + child.getNodeValue());
            SchemaItem processedElement = processElement(child);
            if (processedElement != null) {
                processedParent.addChild(processedElement);
                if (processedParent.getType() == SchemaItem.Type.undefined) {
                    processedParent.setType(SchemaItem.Type.objectType);
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
