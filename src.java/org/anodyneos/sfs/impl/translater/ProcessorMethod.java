package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.JavaMethod;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ProcessorMethod extends TranslaterProcessor {

    private ProcessorContent processorContent;

    private boolean headerPrinted = false;
    private JavaMethod jm;
    private String readerMethod;

    public static final String E_PARAM = "param";
    public static final String E_METHOD = "method";

    public static final String A_NAME = "name";
    public static final String A_TYPE = "type";
    public static final String A_READER = "reader";

    public ProcessorMethod(TranslaterContext ctx) {
        super(ctx);
        processorContent = new ProcessorContent(ctx);
        jm = new JavaMethod();
        jm.setFinal(true);
        jm.addArgument("org.anodyneos.sfs.SfsContext", "sfsContext", true);
        jm.addException("org.xml.sax.SAXException");
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if(!headerPrinted && URI_SFS.equals(uri) && E_PARAM.equals(localName)) {
            // <sfs:param>
            return this;
        } else {
            printMethodHeader();
            return processorContent.getProcessorFor(uri, localName, qName);
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if(URI_SFS.equals(uri) && E_METHOD.equals(localName)) {
            // <sfs:method>
            jm.setName(attributes.getValue(A_NAME));
            readerMethod = attributes.getValue(A_READER);
        } else if(URI_SFS.equals(uri) && E_PARAM.equals(localName)) {
            // <sfs:param>
            jm.addArgument(attributes.getValue(A_TYPE), attributes.getValue(A_NAME));
        }

    }

    public void characters(char[] ch, int start, int length) {
        // this is the only content handled by the method processor
        processorContent.characters(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(URI_SFS.equals(uri) && E_METHOD.equals(localName)) {
            // <sfs:method>
            printMethodHeader(); // in case not already printed
            // end method block
            CodeWriter out = getTranslaterContext().getCodeWriter();
            out.endBlock();
            out.println();
            if (null != readerMethod) {
                printReaderMethod();
            }
        }
    }

    private void printMethodHeader() {
        if (! headerPrinted) {
            CodeWriter out = getTranslaterContext().getCodeWriter();
            jm.printSignature(out);
            out.printIndent().println("org.anodyneos.sfs.SfsContentHandler sfsContentHandler = sfsContext.getSfsContentHandler();");
            //out.printIndent().println("org.xml.sax.helpers.AttributesImpl _sfsAtts = new org.xml.sax.helpers.AttributesImpl();");
            //out.println();
        }
        headerPrinted = true;
    }

    private void printReaderMethod() {
        /*
            public org.xml.sax.XMLReader getHtmlReader(final Arg1Type arg1, final Arg2Type arg2...) throws org.xml.sax.SAXException {
                return new org.anodyneos.sfs.SfsXmlReader() {
                    public void parse(org.xml.sax.InputSource input) throws org.xml.sax.SAXException {
                        org.anodyneos.sfs.SfsContext sfsContext = org.anodyneos.sfs.SfsFactory.getDefaultFactory().getSfsContext(getContentHandler());
                        getContentHandler().startDocument();
                        org.anodyneos.sfs.sample.Sample.this.html(sfsContext, arg1, arg2...);
                        getContentHandler().endDocument();
                    }
                };
            }
        */

        TranslaterContext ctx = getTranslaterContext();
        CodeWriter out = ctx.getCodeWriter();
        JavaMethod jmReader = new JavaMethod();
        jmReader.setFinal(true);
        jmReader.setReturnType("org.xml.sax.XMLReader");
        jmReader.setName(readerMethod);
        jmReader.addException("org.xml.sax.SAXException");
        JavaMethod.Argument[] args = jm.getArguments();
        // start at 1 to skip sfsContext
        for (int i = 1; i < args.length; i++) {
            jmReader.addArgument(args[i].getType(), args[i].getName(), true);
        }
        jmReader.printSignature(out);
        out.printIndent().println("return new org.anodyneos.sfs.SfsXmlReader() {");
        out.indentPlus();
        out.printIndent().println("public void parse(org.xml.sax.InputSource input) throws org.xml.sax.SAXException {");
        out.indentPlus();
        out.printIndent().println("org.anodyneos.sfs.SfsContext sfsContext = org.anodyneos.sfs.SfsFactory.getDefaultFactory().getSfsContext(getContentHandler());");
        out.printIndent().println("getContentHandler().startDocument();");
        out.printIndent().print(ctx.getFullClassName() + ".this." + jm.getName());
        out.print("(sfsContext");
        // start at 1 to skip sfsContext
        for (int i = 1; i < args.length; i++) {
            out.print(", " + args[i].getName());
        }
        out.println(");");
        out.printIndent().println("getContentHandler().endDocument();");
        out.endBlock();
        out.indentMinus();
        out.printIndent().println("};");
        out.endBlock();
        out.println();
    }

}
