package com.story_inspector.test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initializes the application context. Superclass of limited integration tests that require a functional application context.
 *
 * @author mizitch
 *
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/app-config-test.xml" })
public abstract class SpringBasedTest {

	@BeforeClass
	public static void initializeDirectoryForTestGeneratedFiles() throws Exception {
		final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		tmpDir.mkdirs();
	}

	protected File createTestFile() throws IOException {
		return createTestFile(".sir");
	}

	protected File createTestFile(final String extension) throws IOException {
		final File testFile = File.createTempFile(UUID.randomUUID().toString(), extension);
		testFile.deleteOnExit();
		return testFile;
	}
}
