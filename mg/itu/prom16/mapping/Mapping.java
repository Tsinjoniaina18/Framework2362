package mg.itu.prom16.mapping;

public class Mapping {
    private String className;
    private String functionName;
    private String verb;

    public Mapping(String c , String function, String verb){
        this.setClassName(c);
        this.setFunctionName(function);
        this.setVerb(verb);
    }

    public Mapping(){

    }

    public String getClassName(){
        return this.className;
    }

    public String getFunctionName(){
        return this.functionName;
    }

    public void setClassName(String c){
        this.className = c;
    }

    public void setFunctionName(String f){
        this.functionName = f;
    }

    public String getVerb(){
        return this.verb;
    }

    public void setVerb(String s){
        this.verb = s;
    }
}
