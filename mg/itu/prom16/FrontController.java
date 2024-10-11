package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import mg.itu.prom16.annotation.RestAPI;
import mg.itu.prom16.mapping.ClassMethod;
import mg.itu.prom16.mapping.Mapping;
import mg.itu.prom16.mapping.ModelView;
import mg.itu.prom16.mapping.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

import com.google.gson.Gson;

public class FrontController extends HttpServlet{
    private String controllerPackage;
    private String error = "";
    private Map<String , Mapping> annotedGetFunction = new HashMap<>();

    public void init ()throws ServletException{
        try{
            this.controllerPackage = getServletConfig().getInitParameter("Controllers");

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

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/json");
        PrintWriter out = res.getWriter();
        
        out.println(req.getMethod());

        if(!this.error.equals("")){
            
            req.setAttribute("error", Utils.ErrorPage("Error 500: Scan Error", "Error when scan all project because of "+this.error+" !"));

            RequestDispatcher dispatch = req.getRequestDispatcher("error/error.jsp");
            dispatch.forward(req, res);
            
            out.print(Utils.ErrorPage("Error 500: Scan Error", "Error when scan all project because of "+this.error+" !"));

            return;
        }

        if(this.controllerPackage!=null){
            String url = req.getRequestURL().toString();

            String slach = "/";
            String[] urlSplited = url.split(slach);
            Mapping map = annotedGetFunction.get(urlSplited[urlSplited.length-1]);  
            if(map != null){
                // out.println(map.getVerb());
                String verb = req.getMethod();
                ClassMethod classMethod = map.classMethodByVerb(verb);

                if(classMethod == null){
                    out.print(Utils.ErrorPage("Error 500: Invalid Method", "Invalid method, it does not match or does not exist"));
                    return;
                }

                Gson gson = new Gson();
                Method method = Utils.callFunction(map , req.getSession(), req);

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
                    ArrayList<String> parameterNames = new ArrayList<String>();
                    try{
                        parameterNames = Utils.parameterNames(method);
                    }catch(Exception e){
                        String title = "Error 2362";
                        String cause = e.getMessage();
                        String error = Utils.ErrorPage(title, cause);
                        out.println(error);
                        
                        return;
                    }
                    ArrayList<String> requestValues = new ArrayList<String>();
                    for(int i=0 ; i<parameterNames.size() ; i++){
                        if(!parameterNames.get(i).equals("Session Traitement")){
                            requestValues.add(req.getParameter(parameterNames.get(i)));
                            out.print(parameterNames.get(i)+" : ");
                            out.print(requestValues.get(i)+" , ");
                        }
                    }
                    try{
                        ModelView mv = (ModelView)Utils.callFunction2(map , method , requestValues , req.getSession(false), req);
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