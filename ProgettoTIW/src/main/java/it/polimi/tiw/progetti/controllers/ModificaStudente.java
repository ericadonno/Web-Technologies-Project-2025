package it.polimi.tiw.progetti.controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.progetti.beans.InfoStudenteAppello;
import it.polimi.tiw.progetti.dao.StudenteDAO;
import it.polimi.tiw.progetti.utils.ConnectionHandler;

/**
 * Servlet implementation class Iscritti
 */
@WebServlet("/ModificaStudente")
public class ModificaStudente extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModificaStudente() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
    	this.connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();

		  JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);    
		  WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);

		  templateResolver.setTemplateMode(TemplateMode.HTML);
		  this.templateEngine = new TemplateEngine();
		  this.templateEngine.setTemplateResolver(templateResolver);
		  templateResolver.setSuffix(".html");
    }
		
	


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//User user = (User) request.getSession().getAttribute("user");
		String studenteIdParam = request.getParameter("studenteId");
		int studenteid = Integer.parseInt(studenteIdParam);
		String appelloIdParam = request.getParameter("appId");
		int appid = Integer.parseInt(appelloIdParam);
		StudenteDAO studenteDAO = new StudenteDAO(connection, studenteid);
		

		
		JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
		IWebExchange webExchange = application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, request.getLocale());
		//ctx.setVariable("username", user.getUsername());
		try {
			InfoStudenteAppello infostud = studenteDAO.cercoInfoStudentePubblicatoperAppello(appid);
			ctx.setVariable("iscritto", infostud);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare i dati di questo studente per questo appello");
			return;
		}
		String errorMsg = (String) request.getSession().getAttribute("error_message");
		if (errorMsg != null) {
		    ctx.setVariable("error_message", errorMsg);
		    request.getSession().removeAttribute("error_message");
		}
		
            
	
	    templateEngine.process("/WEB-INF/modificaStudente.html", ctx, response.getWriter());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String studenteIdParam = request.getParameter("studenteId");
		int studenteid = Integer.parseInt(studenteIdParam);
		String appelloIdParam = request.getParameter("appId");
		int appid = Integer.parseInt(appelloIdParam);
		 String voto = request.getParameter("voto");
		StudenteDAO studenteDAO = new StudenteDAO(connection, studenteid);
		Set<String> votiValidi = Set.of(
			    "18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
			    "28", "29", "30", "30L", "1", "2", "3", "4", "5", "6", "7",
			    "8", "9", "10", "11", "12", "13", "14", "15", "16", "17"
			);
				
		if(!votiValidi.contains(voto)) {
			request.getSession().setAttribute("error_message", "Voto non accettato, riprova");
			response.sendRedirect(getServletContext().getContextPath() + "/ModificaStudente?studenteId=" + studenteid + "&appId="+appid);
			return;
		}
		
		try {
			studenteDAO.aggiornaVotoEStato(appid, voto);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile modificare il voto");
			return;
		}
		
		JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
		IWebExchange webExchange = application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, request.getLocale());
		ctx.setVariable("error_message", "Incorrect credentials");
		
		response.sendRedirect(getServletContext().getContextPath() + "/Iscritti?appId=" + appid);

	}

}
