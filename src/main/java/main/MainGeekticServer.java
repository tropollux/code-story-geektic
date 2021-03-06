package main;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;
import geeks.Geek;
import geeks.Geeks;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import resources.MainResource;
import resources.SearchResource;
import resources.StaticResource;
import twitter.GeektickHashTagListener;
import twitter.TwitterCommands;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Objects.firstNonNull;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS;
import static java.lang.Integer.parseInt;

public class MainGeekticServer {
  private Module[] modules;
  private Injector injector;

  public MainGeekticServer(Module... modules) {
    this.modules = modules;
  }

  public static void main(String[] args) throws IOException {
    int port = parseInt(firstNonNull(System.getenv("PORT"), "8080"));

    MainGeekticServer geekticServer = new MainGeekticServer(new MainGeekticConfiguration());
    geekticServer.start(port);
    geekticServer.injector.getInstance(GeektickHashTagListener.class).start();
  }

  public void start(int port) throws IOException {
    System.out.println("Starting server on port: " + port);

    SimpleServerFactory.create("http://localhost:" + port, configuration());
  }

  private ResourceConfig configuration() throws IOException {
    DefaultResourceConfig config = new DefaultResourceConfig();

    config.getClasses().add(JacksonJsonProvider.class);

    injector = Guice.createInjector(modules);

    Geeks geeks = injector.getInstance(Geeks.class);
		geeks.load();

    config.getSingletons().add(injector.getInstance(MainResource.class));
    config.getSingletons().add(injector.getInstance(StaticResource.class));
    config.getSingletons().add(injector.getInstance(SearchResource.class));

    config.getProperties().put(PROPERTY_CONTAINER_REQUEST_FILTERS, GZIPContentEncodingFilter.class);
    config.getProperties().put(PROPERTY_CONTAINER_RESPONSE_FILTERS, GZIPContentEncodingFilter.class);

    return config;
  }
}
