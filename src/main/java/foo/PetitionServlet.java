package foo;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;

@WebServlet(name = "PetitionServlet", urlPatterns = { "/CreatePetitions" })
public class PetitionServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		// Create users
		for (int i = 0; i < 50; i++) {
            Entity e = new Entity("Petition", "petition_name_" + i);
            e.setProperty("description", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." + i);
            e.setProperty("email", "email-"+i+"@gmail.com");
            e.setProperty("namePetition", "petition_name_"+i);
            e.setProperty("voteCount", 0);
            e.setProperty("creationDate", System.currentTimeMillis());
            datastore.put(e);
		}
	}
}