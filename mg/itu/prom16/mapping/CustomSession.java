package mg.itu.prom16.mapping;

import jakarta.servlet.http.HttpSession;

public class CustomSession {
    private HttpSession session;

    public CustomSession (HttpSession h){
        this.setValues(h);
    }

    public HttpSession getSession(){
        return this.session;
    }

    public void setValues(HttpSession h){
        this.session = h;
    }

    public void add (String key , Object value){
        this.getSession().setAttribute(key, value);
    }

    public void delete (String key){
        this.getSession().removeAttribute(key);
    }

    public void update (String key , Object value){
        this.getSession().setAttribute(key, value);
    }

    public Object get (String key){
        return this.getSession().getAttribute(key);
    }

    public void destroy (){
        this.getSession().invalidate();
    }
}
