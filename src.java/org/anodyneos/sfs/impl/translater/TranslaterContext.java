package org.anodyneos.sfs.impl.translater;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.anodyneos.commons.xml.sax.BaseContext;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.xml.sax.InputSource;

public class TranslaterContext extends BaseContext implements TranslaterResult {
    private CodeWriter codeWriter;
    private String className;
    private String packageName;
    private Map bufferedStartPrefixMappings = new HashMap();

    public TranslaterContext(InputSource is, CodeWriter codeWriter) {
        super(is);
        this.codeWriter = codeWriter;
    }

    public void setFullClassName(String fullClassName) {
        if (null == fullClassName) {
            this.packageName = null;
            this.className = null;
        } else {
            fullClassName = fullClassName.trim();
            int lastDot = fullClassName.lastIndexOf(".");
            if (lastDot != -1) {
                this.packageName = fullClassName.substring(0, lastDot);
                this.className = fullClassName.substring(lastDot + 1);
            } else {
                this.packageName = null;
                this.className = fullClassName;
            }
        }
    }
    public String getFullClassName() {
        if(packageName != null) {
            return packageName + "." + className;
        } else {
            return className;
        }
    }
    public void setPackageName(String packageName) {
        packageName = packageName.trim();
        this.packageName = packageName;
    }
    public String getPackageName() {
        return this.packageName;
    }
    public void setClassName(String className) {
        className = className.trim();
        this.className = className;
    }
    public String getClassName() {
        return this.className;
    }

    public CodeWriter getCodeWriter() {
        return codeWriter;
    }

    public Map getBufferedStartPrefixMappings() {
        return Collections.unmodifiableMap(bufferedStartPrefixMappings);
    }

    public void clearBufferedStartPrefixMappings() {
        bufferedStartPrefixMappings.clear();
    }

    public void bufferStartPrefixMapping(String prefix, String uri) {
        bufferedStartPrefixMappings.put(prefix, uri);
    }

}
