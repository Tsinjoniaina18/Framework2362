package mg.itu.prom16.mapping;

import java.util.ArrayList;

public class Mapping {
    private ArrayList<ClassMethod> classMethod;

    public Mapping(){
        this.classMethod = new ArrayList<ClassMethod>();
    }

    public ArrayList<ClassMethod> getClassMethod(){
        return this.classMethod;
    }

    public void setClassMethod(ArrayList<ClassMethod> classMethod){
        this.classMethod = classMethod;
    }

    public void addClassMethod(ClassMethod classMethod){
        this.getClassMethod().add(classMethod);
    }

    public ClassMethod classMethodByVerb(String verb){
        for(int i=0 ; i<this.getClassMethod().size() ; i++){
            if(this.getClassMethod().get(i).getVerb().equals(verb)){
                return this.getClassMethod().get(i);
            }
        }
        return null;
    }
}
