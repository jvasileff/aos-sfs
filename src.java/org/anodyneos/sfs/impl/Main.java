package org.anodyneos.sfs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.anodyneos.sfs.impl.translater.Translater;
import org.anodyneos.sfs.impl.translater.TranslaterResult;
import org.xml.sax.InputSource;

/**
 *  Provides commandline utility similar to javac that translates SFS source
 *  files into Java source files.
 *  <div>
 *  Usage: <code>sfsc [-h] [--help] [-v] [-d directory] &lt;source files&gt;</code>
 *  </div><p>
 *  Existing java source files will be overwritten.  If a directory is not
 *  specified, java source files will be written to the directory of the sfs
 *  source file.
 *  </p>
 *
 */

public class Main {

    public static final void main(String[] args) {
        try {
            translate(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static final void translate(String[] args) throws Exception {
        boolean help = false;
        boolean verbose = false;
        String directory = null;
        ArrayList sources = new ArrayList();
        ArrayList sourceFiles = new ArrayList();
        String which = null;
        for(int i = 0; i < args.length; i++) {
            String param = args[i];
            if      (param.equals("-d"))        { which = param; }
            else if (param.equals("--help"))    { which = null; help = true; }
            else if (param.equals("-h"))        { which = null; help = true; }
            else if (param.equals("-v"))        { which = null; verbose = true; }
            else if ("-d".equals(which))        { which = null; directory = param; }
            else if (null == which) {
                File file = new File(new File(param).getAbsolutePath());
                if(file.isFile() && file.canRead()) {
                    sourceFiles.add(file);
                } else {
                    throw new Exception("invalid flag: " + param);
                }
            }
        }
        // translate each file
        Translater translater = new Translater();
        for (int i = 0; i < sourceFiles.size(); i++) {
            File tmpFile = null;
            InputStream is = null;
            OutputStream os = null;
            try {
                if (verbose) {
                    System.out.println("Translating: " + sourceFiles.get(i));
                }

                // InputSource
                File sourceFile = (File) sourceFiles.get(i);
                is = new FileInputStream(sourceFile);
                InputSource inputSource = new InputSource(is);
                inputSource.setSystemId(sourceFile.getAbsolutePath());

                // OutputStream
                File tmpDirectory = sourceFile.getParentFile();
                tmpFile = File.createTempFile("sfs.", ".tmp", tmpDirectory);
                os = new FileOutputStream(tmpFile);

                // translate
                TranslaterResult result;
                result = translater.process(inputSource, os);
                is.close();
                os.close();

                // rename
                File javaFile;
                if (null == directory) {
                    javaFile = new File(tmpDirectory, result.getClassName() + ".java");
                } else {
                    String pathPart = result.getFullClassName().replace('.', File.separatorChar);
                    javaFile = new File(directory, pathPart + ".java");
                    File dir = javaFile.getParentFile();
                    dir.mkdirs();
                }
                // Windows cannot rename over an existing file.
                javaFile.delete();
                boolean success = tmpFile.renameTo(javaFile);
            } catch (Exception e) {
                throw e;
            } finally {
                if (tmpFile != null) tmpFile.delete();
                try { if (is != null) is.close(); } catch (Exception e) { }
                try { if (os != null) os.close(); } catch (Exception e) { }
            }
        }
    }

}
