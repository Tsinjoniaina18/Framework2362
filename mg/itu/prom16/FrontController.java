package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.mapping.Mapping;
import mg.itu.prom16.mapping.ModelView;
import mg.itu.prom16.mapping.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Package;
import java.lang.reflect.Method;
import java.net.URL;

public class FrontController extends HttpServlet{
    private String controllerPackage;
    private ArrayList<String> doublons;
    private Map<String , Mapping> annotedGetFunction = new HashMap<>();

    public void init ()throws ServletException{
        try{
            this.controllerPackage = getServletConfig().getInitParameter("Controllers");

            this.doublons = Utils.allAnnotedGetFunction(this.annotedGetFunction , this.controllerPackage);
        }
        catch(Exception e){

        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("servlet post");

        processRequest(req, res);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("servlet get");

        processRequest(req, res);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        if(this.doublons.size()>0){
            String error = "";
            for(int i = 0 ; i<doublons.size() ; i++){
                error += "\"/"+doublons.get(i)+"\"";
                if(i<doublons.size()-1){
                    error += ", ";
                }
            }
            out.print(Utils.ErrorPage("Error 405: Url forbidden", "The root "+error+" already exist and must be declared once"));

            return;
        }

        if(this.controllerPackage!=null){
            String url = req.getRequestURL().toString();
            // out.println(url);

            /*ArrayList<String> listControler = Utils.allControlerName(this.controllerPackage);
            out.println("");
            out.println("Liste des controllers : ");
            for(int i=0 ; i<listControler.size() ; i++){
                out.println(listControler.get(i));
            }*/

            out.println("");
            out.print("Fonction correspondant a l'url : \t");
            String slach = "/";
            String[] urlSplited = url.split(slach);
            Mapping map = annotedGetFunction.get(urlSplited[urlSplited.length-1]);  
            if(map != null){
                out.println(map.getClassName()+" -> "+map.getFunctionName());

                Object returned = Utils.callFunction(map);
                if(returned.getClass().getName().equals("java.lang.String")){
                    out.println("Valeur de retour : "+returned);
                }
                else if(returned.getClass().getName().equals("mg.itu.prom16.mapping.ModelView")){

                    ModelView mv = (ModelView)returned;
                    // out.println(mv.getUrl());

                    mv.getObject().forEach(
                        (cle , valeur)->req.setAttribute(cle , valeur)
                        // (cle , valeur)->out.println(cle+" -> "+valeur)
                    );

                    RequestDispatcher dispat = req.getRequestDispatcher(mv.getUrl());
                    dispat.forward(req,res);

                }
                else{
                    String title = "Error 1802: Invalid return value";
                    String cause = "Get Funtion must return String or ModelView";
                    String error = Utils.ErrorPage(title, cause);
                    out.println(error);
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