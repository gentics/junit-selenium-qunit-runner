package com.gentics.testutils.maven.selenium.qunit;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

public class QUnitTest implements Test {

	protected String name;
	protected Exception exception;

	public QUnitTest(String name) {
		this.name = name;
	}

	public int countTestCases() {
		return 1;
	}

	public void run(TestResult result) {
		result.startTest(this);
		result.runProtected(this, new Protectable() {
			public void protect() throws Throwable {
				if (exception != null) {
					throw exception;
				}
			}
		});

		result.endTest(this);
	}

	public String getName() {
		return toString();
	}

	public String toString() {
		return name;
	}

	/**
	 * Sets the exception that will be thrown inside of the test
	 * @param exception
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

}
