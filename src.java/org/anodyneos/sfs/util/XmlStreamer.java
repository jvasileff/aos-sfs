package org.anodyneos.sfs.util;

import java.io.OutputStream;
import java.io.PrintStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

public class XmlStreamer {

    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public static void process(XMLReader xmlReader, PrintStream out) throws Exception {
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        OutputStream os = System.out;
        transformer.transform(
                new javax.xml.transform.sax.SAXSource(xmlReader, new org.xml.sax.InputSource("")),
                new javax.xml.transform.stream.StreamResult(os));
    }

}
