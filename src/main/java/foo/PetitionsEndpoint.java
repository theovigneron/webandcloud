package foo;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "petitionsApi",
     version = "v1",
     audiences = "757565010727-on2ha2ftblhh257cqpg1tsrij3c8v68n.apps.googleusercontent.com",
  	 clientIds = "757565010727-on2ha2ftblhh257cqpg1tsrij3c8v68n.apps.googleusercontent.com",
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )

public class PetitionsEndpoint {
    @ApiMethod(name = "petitions", httpMethod = HttpMethod.GET)
	public List<Entity> petitions() {
		Query q = new Query("Petition");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(20));
		return result;
	}

    @ApiMethod(name = "petitionsGetName", httpMethod = HttpMethod.GET)
	public Entity petitionsGetName(@Named("petitionName") String petitionName) {
        System.out.print(petitionName);
		Query q = new Query("Petition").setFilter(new FilterPredicate("namePetition", FilterOperator.EQUAL, petitionName));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		return result;
	}

    @ApiMethod(name = "petitionIsSignByUser", httpMethod = HttpMethod.GET)
	public Entity petitionIsSignByUser(@Named("petitionName") String petitionName, @Named("email") String email) {
		Query q = new Query("UserSignPetition").setFilter(new FilterPredicate("signName", FilterOperator.EQUAL, email+"_"+petitionName));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		Entity result = pq.asSingleEntity();
		return result;
	}

    @ApiMethod(name = "addSignature", httpMethod = HttpMethod.GET)
	public Entity addSignature(@Named("petitionName") String petitionName, @Named("email") String email) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        //Creation de l'association userPetition
		Entity userSignPetitionEntity = new Entity("UserSignPetition", email+"_"+petitionName);
		userSignPetitionEntity.setProperty("signName", email+"_"+petitionName);
        userSignPetitionEntity.setProperty("email", email);
		userSignPetitionEntity.setProperty("namePetition", petitionName);
        
        //Creation de l'association petitionUser
        Entity petitionSignByUser = new Entity("PetitionSignByUser", petitionName+"_"+email);
		petitionSignByUser.setProperty("signName", petitionName+"_"+email);
        petitionSignByUser.setProperty("email", email);
		petitionSignByUser.setProperty("namePetition", petitionName);
        
        //Incrementation des votes
        Query incrementQuery = new Query("Petition").setFilter(new FilterPredicate("namePetition", FilterOperator.EQUAL, petitionName));
        PreparedQuery pq = datastore.prepare(incrementQuery);
        Entity result = pq.asSingleEntity();
        result.setProperty("voteCount", ( (Number) result.getProperty("voteCount")).intValue() + 1);
        
        datastore.put(result);
        datastore.put(petitionSignByUser);
        datastore.put(userSignPetitionEntity);
		return  result;
	}
    @ApiMethod(name = "createPetition", httpMethod = HttpMethod.GET)
	public Entity createPetition(@Named("namePetition") String namePetition, @Named("email") String email, @Named("description") String description) {
		Entity e = new Entity("Petition", ""+namePetition);
		e.setProperty("namePetition", namePetition);
        e.setProperty("email", email);
		e.setProperty("description", description);
        e.setProperty("voteCount", 0);
        e.setProperty("creationDate", System.currentTimeMillis());
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		return  e;
	}

    @ApiMethod(name = "topPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> topPetitions() {
		Query q = new Query("Petition").addSort("voteCount", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}

    @ApiMethod(name = "getUserPetition", httpMethod = HttpMethod.GET)
	public List<Entity> getUserPetition(@Named("predicatmail") String predicatmail) {
		Query q = new Query("UserSignPetition").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, predicatmail));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(20));
		return result;
	}

}
