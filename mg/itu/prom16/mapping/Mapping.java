package mg.itu.prom16.mapping;

public class Mapping {
    private String className;
    private String functionName;

    public Mapping(String c , String function){
        this.setClassName(c);
        this.setFunctionName(function);
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
}
