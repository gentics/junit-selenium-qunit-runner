package com.gentics.testutils.maven.selenium.qunit;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class QUnitSeleniumRunner {

	private WebDriver driver;
	private QUnitModuleTestSuite qUnitTestSuite;
	private static Logger logger = Logger.getLogger(QUnitModuleTestSuite.class);
	private String baseURL;

	public QUnitSeleniumRunner(WebDriver driver, String baseURL, QUnitModuleTestSuite qUnitTestSuite) {
		this.driver = driver;
		this.qUnitTestSuite = qUnitTestSuite;
		this.baseURL = baseURL;
	}

	private QUnitTest addTest(String name) {
		QUnitTest test = new QUnitTest(name);
		qUnitTestSuite.addTest(test);
		return test;
	}

	public Object execJavaScript(String code) {
		JavascriptExecutor js = (JavascriptExecutor) this.driver;
		Object result = js.executeScript(code);
		return result;
	};

	/**
	 * Checks whether any javascript errors were detected
	 * 
	 * @throws Exception
	 */
	private void checkForJavaScriptError() {
		Object response = execJavaScript("var tmp = window.err;window.err=''; return tmp");
		if (response != null) {
			try {
				Map<String, Object> map = (Map<String, Object>) response;
				if (map != null) {
					JavaScriptError error = new JavaScriptError(map);
					QUnitException e = new QUnitException(error.toString());
					logger.error("Encountered javascript error.", e);
					QUnitTest test = new QUnitTest("SCRIPT-ERROR");
					test.setException(e);
					qUnitTestSuite.addTest(test);
				}
			} catch (ClassCastException e) {
				logger.debug("Could not handle javascript error information.", e);
			}
		}
	}

	/**
	 * Opens the testpage and checks for javascript errors up in front and after the tests have run
	 * 
	 * @throws Exception
	 */
	public void executeTest() throws QUnitException {

		String testURL = baseURL + "/" + qUnitTestSuite.getName() + ".html";
		logger.info("Using url: " + testURL);

		this.driver.get(testURL);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		checkForJavaScriptError();

		WebElement testRootElement = driver.findElement(By.id("qunit-tests"));

		boolean testTimeoutReached = waitForTestCompletion(testRootElement);
		List<WebElement> testCaseElements = testRootElement.findElements(By.xpath("//ol[@id='qunit-tests']/li"));

		// Iterate over all testcases
		logger.debug("Start parsing of testresults");
		for (WebElement testCaseElement : testCaseElements) {
			parseTestCaseElement(testCaseElement);
		}
		logger.debug("Test results parsed");
		// if (testTimeoutReached) {
		// QUnitException e = new QUnitException("The test execution timeout was reached.");
		// logger.error("Encountered timeout.", e);
		// QUnitTest test = new QUnitTest("TIMEOUT-ERROR");
		// test.setException(e);
		// qUnitTestSuite.addTest(test);
		//
		// }
		// checkForJavaScriptError();

	}

	/**
	 * Wait for the test completion
	 * 
	 * @param testRootElement
	 * @return true if the test timeout was reached, false if not
	 * @throws Exception
	 */
	private boolean waitForTestCompletion(WebElement testRootElement) {

		final int TEST_TIMEOUT = 120;
		int testDuration = 0;
		while (true) {

			List<WebElement> testCaseElements = testRootElement.findElements(By.xpath("//ol[@id='qunit-tests']/li"));

			boolean runningTestFound = false;
			for (WebElement testCaseElement : testCaseElements) {
				String testCaseState = testCaseElement.getAttribute("class");
				// Check whether the test is stuck
				if (testCaseState.equalsIgnoreCase("running")) {
					runningTestFound = true;
				}
			}

			if (!runningTestFound) {
				testDuration = 0;
				logger.debug("No more running tests were found");
				break;
			} else {
				if (testDuration >= TEST_TIMEOUT) {
					logger.error("The test timeout of {" + TEST_TIMEOUT + "} was reached.");
					return true;
				} else {
					testDuration++;
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		return false;

	}

	/**
	 * Parses a single testcase element. This method will also take care of setting the correct exception in the log
	 * 
	 * @param testCaseElement
	 */
	private void parseTestCaseElement(WebElement testCaseElement) {
		String testName = testCaseElement.findElement(By.className("test-name")).getText();
		try {
			WebElement moduleNameElement = testCaseElement.findElement(By.className("module-name"));
			// Append the module name of one could be found
			if (moduleNameElement != null) {
				String moduleName = moduleNameElement.getText();
				if (!StringUtil.isBlank(moduleName)) {
					testName = "[" + moduleName + "] - " + testName;
				}
			}
		} catch (NoSuchElementException e) {
			logger.debug("Modulename for test {" + testName + "} could not be found.");
		}

		QUnitTest test = addTest(testName);
		String testCaseState = testCaseElement.getAttribute("class");
		// Check whether the test is stuck
		if (testCaseState.equalsIgnoreCase("running")) {
			test.setException(new Exception("Test is still running."));
			return;
		}
		String failed = testCaseElement.findElement(By.className("failed")).getText();
		String passed = testCaseElement.findElement(By.className("passed")).getText();

		handleAssertions(testCaseElement, test, failed, passed, testCaseState);
	}

	/**
	 * Handle the assertions for the testcase
	 * 
	 * @param testCaseElement
	 * @param test
	 * @param failed
	 * @param passed
	 * @param testCaseState
	 */
	private void handleAssertions(WebElement testCaseElement, QUnitTest test, String failed, String passed, String testCaseState) {

		List<WebElement> assertElements = testCaseElement.findElements(By.xpath(".//li"));

		if (Integer.parseInt(failed) > 0) {
			// Iterate over all assertions
			for (WebElement assertElement : assertElements) {
				QUnitAssertion assertion = new QUnitAssertion(assertElement);
				if (assertion.isFailure()) {
					test.setException(assertion.getException());
				}
			}
		}
		if ("failed".equalsIgnoreCase(testCaseState) || Integer.parseInt(failed) > 0) {
			test.setException(new Exception("The test failed with no specified message. Failed assertions {" + failed + "}. Passed assertion {"
					+ passed + "}"));
		}
	}

}

/**
 * Simple class that represents an javascript error
 * 
 * @author johannes2
 * 
 */
class JavaScriptError {

	String filename;
	String colno;
	Long lineno;
	String message;
	String stack;

	/**
	 * Creates a new javascript error object
	 * 
	 * @param map
	 */
	public JavaScriptError(Map<String, Object> map) {
		message = (String) map.get("messsage");
		filename = (String) map.get("filename");
		colno = (String) map.get("colno");
		lineno = (Long) map.get("lineno");
		stack = (String) map.get("stack");
	}

	@Override
	public String toString() {
		return "JavaScript Error: " + filename + ":" + lineno + "," + colno + " # " + message + "\nStacktrace:\n" + stack;
	}

}

/**
 * Simple class that is used to parse the assertions
 * 
 * @author johannes2
 * 
 */
class QUnitAssertion {

	private static Logger logger = Logger.getLogger(QUnitModuleTestSuite.class);
	private String message;
	private String source;
	private String expectedValue;
	private String actualValue;
	private String diff;
	private WebElement assertElement;

	public QUnitAssertion(WebElement assertElement) {
		this.assertElement = assertElement;
		this.message = getValue("test-message");
		this.source = getValue("test-source");
		this.expectedValue = getValue("test-expected");
		this.actualValue = getValue("test-actual");
		this.expectedValue = getValue(By.xpath(".//pre"));
		this.diff = getValue(By.className("test-diff"));
	}

	public Exception getException() {
		if (isFailure()) {
			return new Exception("The test failed with message {" + message + "}. The expected assertionvalue was {" + expectedValue + "}");
		} else {
			return null;
		}
	}

	/**
	 * Returns whether this assertion has failed
	 * 
	 * @return
	 */
	public boolean isFailure() {
		String cssClazz = assertElement.getAttribute("class");
		return "fail".equalsIgnoreCase(cssClazz);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getExpectedValue() {
		return expectedValue;
	}

	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}

	public String getActualValue() {
		return actualValue;
	}

	public void setActualValue(String actualValue) {
		this.actualValue = actualValue;
	}

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public WebElement getAssertElement() {
		return assertElement;
	}

	public void setAssertElement(WebElement assertElement) {
		this.assertElement = assertElement;
	}

	private String getValue(By selector) {
		try {
			return assertElement.findElement(selector).getText();
		} catch (NoSuchElementException e) {
			logger.debug("Could not find assertion element for selector {" + selector + "}");
		}
		return "";
	}

	private String getValue(String name) {
		return getValue(By.className(name));
	}

}