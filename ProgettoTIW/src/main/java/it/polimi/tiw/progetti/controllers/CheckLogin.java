package it.polimi.tiw.progetti.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.progetti.beans.User;
import it.polimi.tiw.progetti.dao.UserDAO;
import it.polimi.tiw.progetti.utils.ConnectionHandler;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/CheckLogin")
//@MultipartConfig
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckLogin() {
		super();
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		String usrn = null;
		String pwd = null;
		usrn = request.getParameter("username");
		pwd = request.getParameter("pwd");
		//controllo validità parametri
		if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Crendenziali errate");
			request.getSession().setAttribute("error_message","Le credenziali non possono essere vuote");
			return;
		}
		//controllo la presenza delle credenziali nel database
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(usrn, pwd);
		} catch (SQLException e) {
			e.printStackTrace(); 
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
			IWebExchange webExchange = application.buildExchange(request, response);
			WebContext ctx = new WebContext(webExchange, request.getLocale());
			ctx.setVariable("error_message", "Credenziali errate");
		    templateEngine.process("loginPage.html", ctx, response.getWriter());

			return;
		}

		//utente non presente nel database
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
			IWebExchange webExchange = application.buildExchange(request, response);
			WebContext ctx = new WebContext(webExchange, request.getLocale());
			ctx.setVariable("error_message", "Credenziali errate");
		    templateEngine.process("loginPage.html", ctx, response.getWriter());
		} else if (user.getRole().equals("studente")) {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + "/StudenteHomePage");
		} else if (user.getRole().equals("docente")) {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + "/DocenteHomePage");
		}

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
