package bp.server;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 */
public class Main {

  private static final String BASE_URI = "http://127.0.0.1:8082/routing/";


  public static HttpServer startServer() {
    final ResourceConfig rc = new ResourceConfig().packages("bp.server");
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
  }

  /**
   * Main method.
   */
  public static void main(String[] args) throws IOException {
    final HttpServer server = startServer();
  }
}

