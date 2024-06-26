package org.jvnet.basicjaxb.test.episodes.b;

import static java.lang.String.format;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_SYSTEM;

import java.util.ArrayList;

import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

@Order(1)
public class RunEpisodeBPluginTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
        // An "META-INF/sun-jaxb.episode" file is generated by the XJC (XML
        // Schema to Java) compiler. It is a schema bindings that associates
        // schema types with existing classes. It is useful when you have one
        // XML schema that is imported by other schemas, as it prevents the
        // model from being regenerated. XJC will scan JARs for '*.episode
        // files', then use them as binding files automatically.
        final Dependency episode = new Dependency();
        episode.setGroupId("org.patrodyne.jvnet");
		episode.setArtifactId("hisrc-basicjaxb-test-episodes-a");
        episode.setVersion(getProjectVersion());
        episode.setSystemPath(format("../a/target/%s-%s.jar", episode.getArtifactId(), episode.getVersion()));
        episode.setScope(SCOPE_SYSTEM);

		//
		// Dependencies
		//

		final Dependency basicjaxb = new Dependency();
		basicjaxb.setGroupId("org.patrodyne.jvnet");
		basicjaxb.setArtifactId("hisrc-basicjaxb-plugins");
		basicjaxb.setVersion(getProjectVersion());
		basicjaxb.setScope(SCOPE_RUNTIME);

		//
		// MOJO Execution
		//

		HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		mojo.setSchemaDirectory(fullpath("src/main/resources"));
		mojo.setGenerateDirectory(fullpath("target/generated-sources/xjc")); 
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setRemoveOldOutput(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);
		mojo.setExtension(true);
		mojo.setArgs(new ArrayList<>());
		mojo.getArgs().add("-XfixOtherAttributes");
		mojo.getArgs().add("-XhashCode");
		mojo.getArgs().add("-Xequals");
		mojo.getArgs().add("-XtoString");
		mojo.getArgs().add("-Xcopyable");
		mojo.getArgs().add("-Xmergeable");

		mojo.setStrict(false);
		mojo.setCatalog(fullpath("src/main/resources/catalog.xml"));
		mojo.setEpisodes(new Dependency[] { episode });
		
		mojo.setPlugins(new Dependency[] { basicjaxb });

		mojo.execute();
	}
}
