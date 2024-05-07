package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.io.PrintWriter;

public class FrontController extends HttpServlet{

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println("servlet post");

        processRequest(req, res);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println("servlet get");

        processRequest(req, res);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println(req.getRequestURL().toString());
    }
}