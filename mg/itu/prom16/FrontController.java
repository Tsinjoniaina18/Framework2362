package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.MultipartConfig;
import mg.itu.prom16.annotation.Post;
import mg.itu.prom16.annotation.RestAPI;
import mg.itu.prom16.mapping.ClassMethod;
import mg.itu.prom16.mapping.Mapping;
import mg.itu.prom16.mapping.ModelView;
import mg.itu.prom16.mapping.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

import com.google.gson.Gson;

@MultipartConfig
public class FrontController extends HttpServlet{
    private String controllerPackage;
    private String validation;
    private String role;
    private String error = "";
    private Map<String , Mapping> annotedGetFunction = new HashMap<>();

    public void init ()throws ServletException{
        try{
            this.controllerPackage = getServletConfig().getInitParameter("Controllers");
            this.validation = getServletConfig().getInitParameter("Validation");
            this.role = getServletConfig().getInitParameter("Role");

            Utils.allAnnotedGetFunction(this.annotedGetFunction , this.controllerPackage);
        }
        catch(Exception e){
           this.error += e.getMessage();
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/json");
        PrintWriter out = res.getWriter();
        out.println("servlet post");

        processRequest(req, res);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/json");
        PrintWriter out = res.getWriter();
        out.println("servlet get");

        processRequest(req, res);
    } 
    // value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/json");
        PrintWriter out = res.getWriter();

        out.println(req.getMethod());

        if(!this.error.equals("")){
            
            out.print(Utils.ErrorPage("Error 500: Scan Error", "Error when scan all project because of "+this.error+" !"));

        }

        if(this.controllerPackage!=null){
            String url = req.getRequestURL().toString();

            String slach = "/";
            String[] urlSplited = url.split(slach);
            Mapping map = annotedGetFunction.get(urlSplited[urlSplited.length-1]);  
            if(map != null){
                String verb = req.getMethod();
                ClassMethod classMethod;
                classMethod = map.classMethodByVerb(verb);
                if(classMethod == null && req.getAttribute("error")!=null){
                    verb = "GET";
                    classMethod = map.classMethodByVerb(verb);
                }

                if(classMethod == null){
                    out.print(Utils.ErrorPage("Error 500: Invalid Method", "Invalid method, it does not match or does not exist"));
                    return;
                }

                Gson gson = new Gson();
                Method method;
                req.setAttribute("validation", this.validation);
                req.setAttribute("role", this.role);
                try {

                    method = Utils.callFunction(map , req.getSession(), req);
                    
                } catch (Exception e) {
                    out.println(e.getMessage());
                    e.printStackTrace();
                    return;
                }

                if(method.getParameterCount()==0){
                    Object returned = Utils.executeFunctionWithNoArgument(map, method, req);
                    if(returned instanceof java.lang.String){
                        if(method.isAnnotationPresent(RestAPI.class)){
                            returned = gson.toJson(returned);
                        }
                        out.println("Valeur de retour : "+returned);
                    }
                    else if(returned instanceof mg.itu.prom16.mapping.ModelView){
                        ModelView mv = (ModelView)returned;
                        if(method.isAnnotationPresent(RestAPI.class)){
                            String json = gson.toJson(mv.getObject());
                            out.println(json);
                        }
                        else{
                            mv.getObject().forEach(
                                (cle , valeur)->req.setAttribute(cle , valeur)
                            );
        
                            RequestDispatcher dispat = req.getRequestDispatcher(mv.getUrl());
                            dispat.forward(req,res);
                        }
                    }    
                    else{
                        String title = "Error 1802: Invalid return value";
                        String cause = "Get Funtion must return String or ModelView";
                        String error = Utils.ErrorPage(title, cause);
                        out.println(error);
                    }
                }
                else{
                    Object[] parameterValues;
                    try{
                        parameterValues = Utils.parameterNames(method, req);
                        if(req.getAttribute("error")!=null){
                            ModelView mv = (ModelView)Utils.callFunction2(map , method , parameterValues , req.getSession(false), req);
                            String origine = mv.getError();

                            RequestDispatcher dispatcher = req.getRequestDispatcher(origine);
                            dispatcher.forward(req, res);
                        }
                    }catch(Exception e){
                        String title = "Error 2362";
                        String cause = e.getMessage();
                        String error = Utils.ErrorPage(title, cause);
                        out.println(error);
                        e.printStackTrace();
                        
                        return;
                    }
                    try{
                        ModelView mv = (ModelView)Utils.callFunction2(map , method , parameterValues , req.getSession(false), req);
                        if(method.isAnnotationPresent(RestAPI.class)){
                            String json = gson.toJson(mv.getObject());
                            out.println(json);
                        }
                        else{
                            mv.getObject().forEach(
                                (cle , valeur)->req.setAttribute(cle , valeur)
                            );

                            RequestDispatcher dispat = req.getRequestDispatcher(mv.getUrl());
                            dispat.forward(req,res);
                        }
                    }catch(Exception e){
                        out.print(e);
                    }
                }
            }else {
                out.print("Aucune fonction correspondante");
                String title = "Error 404: Not Found";
                String cause = "Controller or Function not found";
                String error = Utils.ErrorPage(title, cause);
                out.println(error);
            }
        }
        else{
            String title = "Error 180206: No Init param found";
            String cause = "The web.xml must contain an \"init-param\" for the controller package name";
            String error = Utils.ErrorPage(title, cause);
            out.println(error);   
        }
    }
}