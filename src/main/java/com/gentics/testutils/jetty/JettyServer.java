package com.gentics.testutils.jetty;

import java.io.FileInputStream;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * Main class that is used to start the jetty server with the deployed webapp
 * 
 * @author johannes2
 * 
 */
public class JettyServer {

	private final int port;
	Server server;

	public JettyServer() {
		try {
			String configFile = System.getProperty("config", "jetty.properties");
			System.getProperties().load(new FileInputStream(configFile));
		} catch (Exception ignored) {
		}
		//TODO make port dynamic
		port = Integer.parseInt(System.getProperty("jetty.port", "8881"));
	}

	/**
	 * Start a Jetty server with some sensible(?) defaults
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {

		server = new Server(8881);

		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setWelcomeFiles(new String[] { "index.html" });
		resource_handler.setResourceBase(".");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
		server.setHandler(handlers);
		server.start();

	}

	public void stop() throws Exception {
		server.stop();
	}

	public int getPort() {
		return this.port;
	}

}
