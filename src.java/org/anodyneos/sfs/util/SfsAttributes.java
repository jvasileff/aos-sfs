package org.anodyneos.sfs.util;

import org.xml.sax.helpers.AttributesImpl;

public class SfsAttributes extends AttributesImpl {

    public SfsAttributes addAttribute2(String qName, String value) {
        addAttribute("", "", qName, "CDATA", value);
        return this;
    }

}
