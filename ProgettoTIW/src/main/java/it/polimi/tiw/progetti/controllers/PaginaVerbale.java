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
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.progetti.beans.InfoIscritti;
import it.polimi.tiw.progetti.beans.User;
import it.polimi.tiw.progetti.beans.Verbale;
import it.polimi.tiw.progetti.dao.AppelloDAO;
import it.polimi.tiw.progetti.dao.VerbaleDAO;
import it.polimi.tiw.progetti.utils.ConnectionHandler;

@WebServlet("/PaginaVerbale")
public class PaginaVerbale extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public PaginaVerbale() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		VerbaleDAO verbaleDAO;
		List<Integer> studentidaAggiornare;
		int appid;
		Verbale verbale = new Verbale();
		List<InfoIscritti> studentiaggiornati = new ArrayList<InfoIscritti>();
		try {
			String appelloIdParam = request.getParameter("appId");
			appid = Integer.parseInt(appelloIdParam);

			AppelloDAO appelloDAO = new AppelloDAO(connection, appid);
			// se l'appello non è del professore viene mandato errore
			int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
			if (docenteCorretto != user.getId()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il verbale a cui vuoi accedere non è tuo");
				return;
			}

			verbaleDAO = new VerbaleDAO(connection, appid);

			// lista formata dagli id degli studenti che hanno lo stato di valutazione a
			// pubblicato o rifiutato

			studentidaAggiornare = verbaleDAO.cercaIdStudentiPubbORif();

			// aggiorno nel database lo stato degli studenti con stato di valutazione
			// mettendolo a verbalizzato
			// nel caso in cui uno studente abbia rifiutato, viene posto a rimandato

			verbaleDAO.aggiornaverbalizzato();
			// carico le informazioni complete degli studenti di cui avevo preso l'id in
			// studentidaaggiornare

			studentiaggiornati = verbaleDAO.infoStudentiAggiornati(appid, studentidaAggiornare);

			// creo il verbale

			verbaleDAO.creaverbale();

			// carica le informazioni relative all'ultimo verbale creato

			verbale = verbaleDAO.idVerb();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore nel recuperare il verbale creato.");
			return;
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro appid deve essere un intero valido");
			return;
		}

		JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
		IWebExchange webExchange = application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, request.getLocale());

		ctx.setVariable("verbale", verbale);
		ctx.setVariable("infoverbalizzati", studentiaggiornati);
		templateEngine.process("/WEB-INF/verbale.html", ctx, response.getWriter());

	}

}
