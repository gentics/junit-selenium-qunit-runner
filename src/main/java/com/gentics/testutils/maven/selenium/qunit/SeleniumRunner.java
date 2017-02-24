package com.gentics.testutils.maven.selenium.qunit;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.gentics.testutils.jetty.JettyServer;

public class SeleniumRunner {
	WebDriver driver;
	JettyServer server;
	QUnitModuleTestSuite module;

	private static Properties settings = new Properties();

	public SeleniumRunner() {
		super();
	}

	public SeleniumRunner(QUnitModuleTestSuite qUnitModuleTestSuite) {
		module = qUnitModuleTestSuite;
	}

	private WebDriver configureWebDriver(boolean isRemote) throws Exception {

		String browser = settings.getProperty("browser");

		// setup browser/driver
		if (isRemote) {
			DesiredCapabilities capabilities = new DesiredCapabilities();

			capabilities.setBrowserName(browser);

			if (settings.getProperty("platform") != null && !settings.getProperty("platform").equals("")) {
				capabilities.setPlatform(Platform.valueOf(settings.getProperty("platform")));
			}

			if (settings.getProperty("browser_version") != null && !settings.getProperty("browser_version").equals("")) {
				capabilities.setVersion(settings.getProperty("browser_version"));
			}

			if (browser.equals("chrome")) {
				capabilities.setCapability("chrome.switches", Arrays.asList("--disable-webgl", "--disable-popup-blocking"));
			}

			return new RemoteWebDriver(new URL(settings.getProperty("hub_location")), capabilities);
		} else {
			if (browser.equals("chrome")) {
				System.setProperty("webdriver.chrome.driver", settings.getProperty("path_to_local_driver"));
				driver = new ChromeDriver();
			} else if (browser.equals("internet explorer")) {
				// starting ie without driver-file is deprecated (supported only until selenium 2.6), but starting
				// it with driver-file resulted in strange behavior
				// System.setProperty("webdriver.ie.driver", path_to_IE_driver);
				driver = new InternetExplorerDriver();
			} else if (browser.equals("firefox")) {
				FirefoxProfile profile = new FirefoxProfile();
				driver = new FirefoxDriver(profile);
			}

			if (driver == null) {
				throw new Exception("No supported browser specified in Property-File");
			}

			return driver;
		}
	}

	public void start() throws Exception {
		driver = configureWebDriver(true);
		server = new JettyServer();
		server.start();
	}

	public void runTest() throws QUnitException, UnknownHostException {
		String basePath = settings.getProperty("basePath");

		QUnitSeleniumRunner runner = new QUnitSeleniumRunner(driver, "http://" + getLanWebServerIp() + ":" + server.getPort() + basePath, module);
		runner.executeTest();
	}

	/**
	 * Get the IP address of the LAN adapter that talks with the selenium Hub
	 * and node
	 * 
	 * @return IP address
	 * @throws QUnitException
	 */
	protected String getLanWebServerIp() throws QUnitException {
		String webserverIp = null;

		try {
			String seleniumHost = new URL(settings.getProperty("hub_location")).getHost();
			webserverIp = getLocalIpForRoutedRemoteIP(seleniumHost);
		} catch (SocketException | MalformedURLException | UnknownHostException e) {
			throw new QUnitException("Couldn't find out the local LAN IP for communication with the selenium hub/node");
		}

		return webserverIp;
	}

	/**
	 * Returns the IP of the network interface that is used to communicate with
	 * the given remote host/IP. Let's say we want to reach 8.8.8.8, it would
	 * return the IP of the local network adapter that is routed into the
	 * Internet.
	 * 
	 * @param destination
	 *            The remote host name or IP
	 * @return An IP of a local network adapter
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	protected String getLocalIpForRoutedRemoteIP(String destination) throws UnknownHostException, SocketException {
		byte[] ipBytes = InetAddress.getByName(destination).getAddress();

		try (DatagramSocket datagramSocket = new DatagramSocket()) {
			datagramSocket.connect(InetAddress.getByAddress(ipBytes), 0);

			return datagramSocket.getLocalAddress().getHostAddress();
		}
	}

	public void stop() throws Exception {
		driver.quit();
		server.stop();
	}

	public static void setSettings(Properties seleniumSettings) {
		settings = seleniumSettings;
	}
}
