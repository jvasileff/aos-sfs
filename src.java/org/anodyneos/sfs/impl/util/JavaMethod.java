package org.anodyneos.sfs.impl.util;

import java.util.ArrayList;

public class JavaMethod {

    private String scope = "public";
    private String returnType = "void";
    private String name;
    private boolean isStatic = false;
    private boolean isFinal = false;
    private ArrayList args = new ArrayList();
    private ArrayList exceptions = new ArrayList();

    // constructors
    public JavaMethod() {
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getScope() {
        return scope;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    public String getReturnType() {
        return returnType;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    public boolean isStatic() {
        return isStatic;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
    public boolean isFinal() {
        return isFinal;
    }

    public void addArgument(String type, String name, boolean isFinal) {
        args.add(new Argument(type, name, isFinal));
    }
    public void addArgument(String type, String name) {
        args.add(new Argument(type, name, false));
    }
    public Argument[] getArguments() {
        if(args.size() == 0) {
            return null;
        } else {
            return (Argument[]) args.toArray(new Argument[args.size()]);
        }
    }

    public void addException(String name) {
        exceptions.add(name);
    }
    public String[] getExceptions() {
        if(exceptions.size() == 0) {
            return null;
        } else {
            return (String[]) exceptions.toArray(new String[exceptions.size()]);
        }
    }

    // printers
    public void printSignature(CodeWriter out) {
        out.printIndent();
        if(getScope() != null) {
            out.print(getScope());
            out.print(" ");
        }
        if(isStatic()) {
            out.print("static ");
        }
        if(isFinal()) {
            out.print("final ");
        }
        out.print(getReturnType());
        out.print(" ");
        out.print(getName());
        out.print("(");
        for(int i = 0; i < args.size(); i++) {
            Argument arg = (Argument) args.get(i);
            if(i != 0) {
                out.print(", ");
            }
            if(arg.isFinal) {
                out.print("final ");
            }
            out.print(arg.getType());
            out.print(" ");
            out.print(arg.getName());
        }
        out.print(")");
        if(exceptions.size() != 0) {
            out.print(" throws ");
            for(int i = 0; i < exceptions.size(); i++) {
                if(i != 0) {
                    out.print(", ");
                }
                out.print((String) exceptions.get(i));
            }
        }
        out.println(" {");
        out.indentPlus();

    }

    public class Argument {
        private String name;
        private String type;
        private boolean isFinal = false;

        public Argument(String type, String name, boolean isFinal) {
            this.type = type;
            this.name = name;
            this.isFinal = isFinal;
        }

        public String getName() {
            return name;
        }
        public String getType() {
            return type;
        }
        public boolean isFinal() {
            return isFinal;
        }
    }

}
