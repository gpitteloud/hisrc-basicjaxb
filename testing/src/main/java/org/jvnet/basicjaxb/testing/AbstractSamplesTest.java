package org.jvnet.basicjaxb.testing;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static java.util.Arrays.sort;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A test harness to test XML sample files in a JAXB context.
 */
public abstract class AbstractSamplesTest
{
	public static final String DEFAULT_SAMPLES_DIRECTORY_NAME = "src/test/samples";

	// Represents the Logger for this class and sub-classes.
	private Logger logger = LoggerFactory.getLogger(getTestClass());
	protected Logger getLogger() { return logger; }
	
	private Boolean failFast = false;
	public Boolean isFailFast() { return failFast; }
	public void setFailFast(Boolean failFast) { this.failFast = failFast; }

	/**
	 * Get this class or configure a test class by override.
	 * 
	 * @return The test class.
	 */
	protected Class<? extends Object> getTestClass()
	{
		return getClass();
	}

	/**
	 * Get the JAXB context path or configure a path by override.
	 * 
	 * @return The JAXB context path.
	 */
	protected String getContextPath()
	{
		return getTestClass().getPackage().getName();
	}

	/**
	 * Get the default samples directory name or configure by override.
	 * 
	 * @return The sample directory name.
	 */
	protected String getSamplesDirectoryName()
	{
		return DEFAULT_SAMPLES_DIRECTORY_NAME;
	}

	/**
	 * Get the base directory for the configured test class.
	 * 
	 * @return The base directory for the configured test class.
	 */
	protected File getBaseDir()
	{
		try
		{
			return getMavenProjectDir(getTestClass());
		}
		catch (Exception ex)
		{
			throw new AssertionError(ex);
		}
	}
	
	/**
	 * Get the samples directory file for the configured base directory and samples directory name.
	 * 
	 * @return The samples directory file for the configured base directory and samples directory name.
	 */
	protected File getSamplesDirectory()
	{
		return new File(getBaseDir(), getSamplesDirectoryName());
	}

	/**
	 * Get the sorted array of sample file(s).
	 * 
	 * @return The sorted array of sample file(s).
	 */
	protected File[] getSampleFiles()
	{
		File samplesDirectory = getSamplesDirectory();
		getLogger().debug("Sample directory [" + samplesDirectory.getAbsolutePath() + "].");
		final Collection<File> files = FileUtils.listFiles(samplesDirectory, new String[] { "xml" }, true);
		File[] fileArray = files.toArray(new File[files.size()]);
		sort(fileArray);
		return fileArray;
	}

	// Represents a map of sample file(s) by file name(s).
	private Map<String, File> sampleMap = null;
	protected Map<String, File> getSampleMap()
	{
		if ( sampleMap == null )
		{
			setSampleMap(new HashMap<>());
			for ( File sampleFile : getSampleFiles() )
				getSampleMap().put(sampleFile.getName(), sampleFile);
		}
		return sampleMap;
	}
	protected void setSampleMap(Map<String, File> sampleMap)
	{
		this.sampleMap = sampleMap;
	}

	/**
	 * Get the JAXB context class loader for the configured test class.
	 * 
	 * @return The JAXB context class loader for the configured test class.
	 */
	protected ClassLoader getContextClassLoader()
	{
		return getTestClass().getClassLoader();
	}

	/**
	 * Configure a map of JAXB context properties.
	 * 
	 * @return A map of JAXB context properties.
	 */
	protected Map<String, ?> getContextProperties()
	{
		return null;
	}

	/**
	 * Create a JAXB context for the configured class path.
	 * 
	 * @return A new instance of a {@link JAXBContext}.
	 * 
	 * @throws JAXBException When a {@link JAXBContext} cannot be created.
	 */
	public JAXBContext createContext()
		throws JAXBException
	{
		final String contextPath = getContextPath();
		final ClassLoader classLoader = getContextClassLoader();
		final Map<String, ?> properties = getContextProperties();
		if (classLoader == null)
			return JAXBContext.newInstance(contextPath);
		else
		{
			if (properties == null)
				return JAXBContext.newInstance(contextPath, classLoader);
			else
				return JAXBContext.newInstance(contextPath, classLoader, properties);
		}
	}

	// Represents the {@link JAXBContext}.
	private JAXBContext jaxbContext;
	protected JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
			setJaxbContext(createContext());
		return jaxbContext;
	}
	protected void setJaxbContext(JAXBContext jaxbContext)
	{
		this.jaxbContext = jaxbContext;
	}

	// Represents the JAXB {@link Unmarshaller}.
	private Unmarshaller unmarshaller = null;
	protected Unmarshaller getUnmarshaller() throws JAXBException
	{
		if ( unmarshaller == null )
			setUnmarshaller(getJaxbContext().createUnmarshaller());
		return unmarshaller;
	}
	protected void setUnmarshaller(Unmarshaller unmarshaller)
	{
		this.unmarshaller = unmarshaller;
	}

	// Represents the JAXB {@link Marshaller}.
	private Marshaller marshaller;
	protected Marshaller getMarshaller() throws JAXBException
	{
		if ( marshaller == null )
		{
			setMarshaller(getJaxbContext().createMarshaller());
			getMarshaller().setProperty(JAXB_FORMATTED_OUTPUT, true);
		}
		return marshaller;
	}
	protected void setMarshaller(Marshaller marshaller)
	{
		this.marshaller = marshaller;
	}

	/**
	 * An abstract method to be implements by the sub-class.
	 * The implementor should read the given sample file and assert expectations.
	 * 
	 * @param sample A sample file to be tested.
	 * 
	 * @throws Exception When the test aborts.
	 */
	protected abstract void checkSample(File sample)
		throws Exception;

	/**
	 * Scan the samples path for all file(s) and call the <code>checkSample(sampleFile)</code>
	 * method. Count the failures, if any, and assert a summary.
	 * 
	 * @throws Exception When the <code>checkSample(sampleFile)</code> aborts.
	 */
	@Test
	public void testSamples()
		throws Exception
	{
		getLogger().debug("Testing samples, start");
		int failed = 0;
		final File[] sampleFiles = getSampleFiles();
		for (final File sampleFile : sampleFiles)
		{
			getLogger().debug("Testing sample, start [" + sampleFile.getName() + "].");
			String result = "SUCCESS";
			try
			{
				checkSample(sampleFile);
			}
			catch (Throwable ex)
			{
				getLogger().error("Testing sample [" + sampleFile.getName() + "] failed the check.", ex);
				failed++;
				result = "FAILURE";
				if ( isFailFast() )
					throw ex;
			}
			getLogger().info("Testing sample, " + result + " [" + sampleFile.getName() + "].");
		}
		getLogger().debug("Testing samples, finish");
		String summary = "Testing summary [" + failed + "/" + sampleFiles.length + "] failed";
		
		// logger.info(summary);
		assertTrue(failed == 0, summary + " the check. Check previous errors for details.");
	}
	
    /**
     * Get the Maven project directory for a given target class.
     * 
	 * <p><b>Strategy:</b> Start with any project class and get the code source for its
	 * protection domain. For standard Maven projects, the code source is
	 * <code>"target/project-classes"</code> which is two sub-directories below the project
	 * directory. <em>Note:</em> The original project class can be at any package depth.</p>
     * 
     * @param targetClass A Maven project target class.
     * 
     * @return The Maven project directory.
     * 
     * @throws URISyntaxException When the @{link CodeSource} location cannot be resolved.
     */
	public static File getMavenProjectDir(Class<?> targetClass) throws URISyntaxException
	{
		ProtectionDomain targetProtectionDomain = targetClass.getProtectionDomain();
		CodeSource targetCodeSource = targetProtectionDomain.getCodeSource();
		File targetClassesDir = new File(targetCodeSource.getLocation().toURI());
		File targetClassesDirParentFile = targetClassesDir.getParentFile();
		File targetClassesDirParentParentFile = targetClassesDirParentFile.getParentFile();
		File targetClassesDirAbsoluteParentParentFile = targetClassesDirParentParentFile.getAbsoluteFile();
		return targetClassesDirAbsoluteParentParentFile;
	}
}
