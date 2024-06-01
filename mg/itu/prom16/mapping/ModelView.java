package mg.itu.prom16.mapping;

import java.util.HashMap;
import java.util.Map;
import java.lang.Object;

public class ModelView {
    private String url;
    private Map<String , Object> object = new HashMap<String , Object>();

    public ModelView(String url){
        this.setUrl(url);
    }

    public ModelView(){}

    public String getUrl(){
        return this.url;
    }

    public void setUrl(String u){
        this.url = u;
    }

    public Map<String , Object> getObject(){
        return this.object;
    }

    public void setObject(Map<String , Object> map){
        this.object = map;
    }

    public void addObject(String name , Object obj){
        this.getObject().put(name, obj);
    }
}
