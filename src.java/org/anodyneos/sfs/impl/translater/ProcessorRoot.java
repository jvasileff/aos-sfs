package org.anodyneos.sfs.impl.translater;

import java.util.ArrayList;

import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.JavaClass;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorRoot extends TranslaterProcessor {

    private JavaClass jc = new JavaClass();

    private ArrayList importProcessors = new ArrayList();
    private ArrayList interfaceProcessors = new ArrayList();
    private CDATAProcessor structureProcessor;

    static final String E_IMPORT = "import";
    static final String E_INTERFACE = "interface";
    static final String E_STRUCTURE = "structure";
    static final String E_METHOD = "method";

    static final String A_CLASS = "class";
    static final String A_EXTENDS = "extends";

    public ProcessorRoot(TranslaterContext ctx) {
        super(ctx);
        structureProcessor = new CDATAProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if (URI_SFS.equals(uri)) {
            if (E_STRUCTURE.equals(localName)) {
                return structureProcessor;
            } else if (E_IMPORT.equals(localName)) {
                CDATAProcessor p = new CDATAProcessor(getTranslaterContext());
                importProcessors.add(p);
                return p;
            } else if (E_INTERFACE.equals(localName)) {
                CDATAProcessor p = new CDATAProcessor(getTranslaterContext());
                interfaceProcessors.add(p);
                return p;
            } else if (E_METHOD.equals(localName)) {
                printJavaHeader();
                return new ProcessorMethod(getTranslaterContext());
                //return new NullProcessor(ctx);
            } else {
                return(super.getProcessorFor(uri, localName, qName));
            }
        } else {
            return(super.getProcessorFor(uri, localName, qName));
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // null is not ok - need validation
        jc.setFullClassName(attributes.getValue(A_CLASS));
        getTranslaterContext().setFullClassName(attributes.getValue(A_CLASS));
        // null is ok for setExtends
        jc.setExtends(attributes.getValue(A_EXTENDS));
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        printJavaFooter();
    }

    private boolean doneHeader = false;
    private void printJavaHeader() {
        if (! doneHeader) {
            // class header
            if(importProcessors.size() > 0) {
                for (int i = 0; i < importProcessors.size(); i++) {
                    jc.addImport(((CDATAProcessor)importProcessors.get(i)).getCDATA());
                }
            }
            if(interfaceProcessors.size() > 0) {
                for (int i = 0; i < interfaceProcessors.size(); i++) {
                    jc.addInterface(((CDATAProcessor)interfaceProcessors.get(i)).getCDATA());
                }
            }
            CodeWriter out = getTranslaterContext().getCodeWriter();
            jc.printHeader(out);

            // constructor()
            //out.printIndent().println("public " + c.getClassName() + "() {");
            //out.indentPlus();
            //out.printIndent().println("// super();");
            //out.endBlock();
            //out.println();

            // class level <xp:logic>
            out.printIndent().println("// CLASS LEVEL JAVA CODE: <sfs:structure>");
            if (null != structureProcessor.getCDATA()) {
                out.println(structureProcessor.getCDATA());
            }
            out.printIndent().println("// END CLASS LEVEL JAVA CODE: </sfs:structure>");
            out.println();

            // main()
            //out.printIndent().println("public static void main(String[] args) throws Exception {");
            //out.indentPlus();
            //out.printIndent().println("long start;");
            //out.printIndent().println("start = System.currentTimeMillis();");
            //out.printIndent().println("mc.xpTest.XpTest obj = new mc.xpTest.XpTest();");
            //out.printIndent().println("mc.xp.util.XMLStreamer.process(new mc.xp.util.XpXMLReader(obj), System.out);");
            //out.printIndent().println("System.out.println(\"Completed in \" + (System.currentTimeMillis() - start) + \" milliseconds\");");
            //out.endBlock();
            //out.println();

            doneHeader = true;
        }
    }

    private void printJavaFooter() {
        printJavaHeader();
        CodeWriter out = getTranslaterContext().getCodeWriter();

        /*
        String[] templates = getTranslaterContext().getTemplateNames();
        out.printIndent().println("public final void _xpService(mc.xp.XpContext _xpContext, java.util.HashMap _xpArgs, String template) throws org.xml.sax.SAXException {");
        out.indentPlus();
        if (null != templates) {
            for (int i = 0; i < templates.length; i++) {
                if (i == 0) {
                    out.printIndent().print("if(");
                } else {
                    out.indentMinus();
                    out.printIndent().print("} else if(");
                }
                out.println(Util.escapeStringQuoted(templates[i]) + ".equals(template)) {");
                out.indentPlus();
                out.printIndent().println("template" + (i+1) + "(_xpContext, _xpArgs);");
            }
            // TODO: ELSE THROW EXCEPTION: NO TEMPLATE
            out.endBlock();
        }
        */

        out.endBlock();
    }
}
