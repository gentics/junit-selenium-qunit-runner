package com.gentics.testutils.maven.selenium.qunit;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class TestingBotTest  {

	private WebDriver driver;

	@BeforeClass
	public static void setupOnce() {
		System.setProperty("webdriver.chrome.driver", "/opt/selenium/chromedriver_linux64_2.3");
		
	}

	@Before
	public void setUp() throws Exception {

		File projectSourceDirectory = new File(System.getProperty("user.dir"), "src");
		assertTrue(projectSourceDirectory.exists());

		// DesiredCapabilities capabillities = DesiredCapabilities.chrome();
		// capabillities.setCapability("version", "11");
		// capabillities.setCapability("platform", Platform.WINDOWS);
		// capabillities.setCapability("name", "Testing Selenium 2 Tunneling");

		// this.driver = new RemoteWebDriver(new
		// URL("http://dasfafs:67asfs@localhost:4445/wd/hub"), capabillities);
		// driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);


	}

	@Test
	public void testSimple() throws Exception {
		//this.driver.get("http://" + sandbox.getHostname() + "/DEV/test/js/folder-tests.html");
		Thread.sleep(10000);
	}

	@After
	public void tearDown() throws Exception {
		this.driver.quit();
	}
}
