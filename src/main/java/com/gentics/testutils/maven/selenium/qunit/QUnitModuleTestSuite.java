package com.gentics.testutils.maven.selenium.qunit;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

public class QUnitModuleTestSuite extends TestSuite implements Test {

	private static Logger logger = Logger.getLogger(QUnitModuleTestSuite.class);

	public QUnitModuleTestSuite(String name) throws Exception {
		super(name);
		SeleniumRunner runner = new SeleniumRunner(this);
		runner.start();
		try {
			logger.info("Test {" + name + "} starting.");
			runner.runTest();
			logger.info("Test {" + name + "} executed.");
		} catch (Exception e) {
			logger.error("Error while executing test {" + name + "}", e);
		} finally {
			runner.stop();
			logger.debug("SeleniumRunner for test {" + name + "} stopped");
		}

	}
}
