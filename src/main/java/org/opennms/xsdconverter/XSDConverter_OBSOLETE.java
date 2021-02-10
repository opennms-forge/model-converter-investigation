package org.opennms.xsdconverter;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;


public class XSDConverter_OBSOLETE {
    File schemaFile;
    public XSDConverter_OBSOLETE(File schema) {
        schemaFile = schema;
    }

    public void parseXSD() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            Document doc = builder.parse(schemaFile);

            Element docElement = doc.getDocumentElement();
            processNodes(docElement.getChildNodes(), 1);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void outputItem(int level, String value) {
        System.out.printf("%-" + (2 * level) + "s%s\n", " ", value);
    }

    private void processNodes(NodeList childNodes, int level) {
        int nodes = childNodes.getLength();
        int index = 0;
        while (index < nodes) {
            Node node = childNodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // Handle the element type
                if (node.getNodeName().equals("element")) {
                    processElement(node, level);
                }
                /* System.out.println("Found element type");
                processElementAttributes(node);
                System.out.println("Found node: " + node.getNodeName());
                processNodes(node.getChildNodes()); */
            }
            ++index;
        }
    }

    private void processElement(Node element, int level) {
        // Get the element's attributes (i.e. name)
        NamedNodeMap attrs = element.getAttributes();

        // Find any defined bounds
        int maxOccurs = getMaxOccurs(attrs); // Will be -1 for unbounded
        boolean unbounded = (maxOccurs == -1);
        int minOccurs = getMinOccurs(attrs);

        // Element should be a reference to something else, or have a name
        Node refAttr = attrs.getNamedItem("ref");
        Node nameAttr = attrs.getNamedItem("name");
        String name = "";
        String reference = "";
        if (nameAttr != null) {
            name = attrs.getNamedItem("name").getNodeValue();
        }
        if (refAttr != null) {
            String rawReference = refAttr.getNodeValue();
            reference = convertReference(rawReference);
            name = nameFromReference(rawReference);
        }

        outputItem(level, name + ":");

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
        }
    }

    private void processComplexTypeAttributes(int level, Node container) {
        // Go through the container and process attributes as if they were elements
        NodeList children = container.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node node = children.item(index);
            if ((node.getNodeType() == Node.ELEMENT_NODE) && ("attribute".equals(node.getNodeName()))) {
                System.out.println("FOUND ATTR " + node.getNodeValue());
            }
        }
    }

    private int getMinOccurs(NamedNodeMap attrs) {
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

    private Node getComplexTypeChildContainer(Node complexTypeNode) {
        // Figure out the type
        NodeList childNodes = complexTypeNode.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);
            if ("sequence".equals(item.getNodeName())) {
                return item;
            }
        }

        return null;
    }

    private Node getInterestingElementType(Node element) {
        // Figure out the type
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node item = childNodes.item(index);
            if ("complexType".equals(item.getNodeName())) {
                return item;
            }
        }

        return null;
    }

    private void processElementAttributes(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        int attrLen = attrs.getLength();
        for (int index = 0; index < attrLen; index++) {
            Node attr = attrs.item(index);
            System.out.println("Attr node name: " + attr.getNodeName() + ", " + attr.getNodeValue());
        }
    }
}
