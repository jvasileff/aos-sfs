package org.anodyneos.sfs.impl.translater;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.anodyneos.commons.xml.sax.BaseParser;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.xml.sax.InputSource;

public class Translater extends BaseParser {

    public Translater() {
        // super();
    }

    public static void main(String[] args) throws Exception {
        // translate
        OutputStream os;
        Translater obj = new Translater();
        long start = System.currentTimeMillis();
        os = new FileOutputStream(args[1]);
        obj.process(new InputSource(args[0]), os);
        os.close();
        System.out.println("Completed in " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

    public TranslaterResult process(InputSource is, OutputStream os) throws Exception {
        CodeWriter out = new CodeWriter(os);
        TranslaterContext ctx = new TranslaterContext(is, out);
        TranslaterProcessor proc = new ProcessorRoot(ctx);
        process(is, proc);
        out.flush();
        return (TranslaterResult) ctx;
    }

}
