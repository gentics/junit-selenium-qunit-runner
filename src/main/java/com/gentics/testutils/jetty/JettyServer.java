package com.gentics.testutils.jetty;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;

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
		port = Integer.parseInt(System.getProperty("jetty.port", String.valueOf(getRandomPort())));
	}

	/**
	 * Not the most elegant or efficient solution, but works.
	 * 
	 * @param port
	 * @return
	 */
	private int getRandomPort() {
		ServerSocket socket = null;

		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException ioe) {
			return -1;
		} finally {
			// if we did open it cause it's available, close it
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// ignore
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Start a Jetty server with some sensible(?) defaults
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {

		server = new Server(port);

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
