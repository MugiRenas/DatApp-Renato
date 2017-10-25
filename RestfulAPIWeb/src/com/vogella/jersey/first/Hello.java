/**
 * 
 */
package com.vogella.jersey.first;

/**
 * @author Renas
 *
 */
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import main.Main;


//Sets the path to base URL + /hello
@Path("/json")
public class Hello {

  // This method is called if TEXT_PLAIN is request
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response sayPlainTextHello(@MatrixParam("param1") String param1, @MatrixParam("param2") String param2) throws Exception {
		String[] args = {param1, param2};
		Main.main(args);
		return Response.status(200).entity(Main.Json()).build();
	}
}
