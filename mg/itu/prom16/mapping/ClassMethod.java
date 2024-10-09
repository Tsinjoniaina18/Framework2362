package mg.itu.prom16.mapping;

public class ClassMethod {
    private String className;
    private String functionName;
    private String verb;

    public ClassMethod(String classe, String method, String verb){
        this.setClassName(classe);
        this.setFunctionName(method);
        this.setVerb(verb);
    }

    public String getClassName(){
        return this.className;
    }

    public void setClassName(String s){
        this.className = s;
    }

    public String getFunctionName(){
        return this.functionName;
    }

    public void setFunctionName(String s){
        this.functionName = s;
    }

    public String getVerb(){
        return this.verb;
    }

    public void setVerb(String s){
        this.verb = s;
    }
}
