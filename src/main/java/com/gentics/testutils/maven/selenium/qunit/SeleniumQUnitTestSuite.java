package com.gentics.testutils.maven.selenium.qunit;

import java.util.Properties;

import junit.framework.TestSuite;

public class SeleniumQUnitTestSuite extends TestSuite {

	public SeleniumQUnitTestSuite(Properties settings) {
		super();
		SeleniumRunner.setSettings(settings);
	}

	public void addQunitModule(String moduleName) throws Exception {
		QUnitModuleTestSuite domSuite = new QUnitModuleTestSuite(moduleName);
		this.addTest(domSuite);
	}
}
