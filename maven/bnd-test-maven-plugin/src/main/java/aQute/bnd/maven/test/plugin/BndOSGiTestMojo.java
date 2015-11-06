package aQute.bnd.maven.test.plugin;

import static aQute.bnd.build.Workspace.createStandaloneWorkspace;
import static org.apache.maven.plugins.annotations.LifecyclePhase.INTEGRATION_TEST;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.RepositorySystem;

import aQute.bnd.build.Run;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Processor;

/**
 * Baseline a project against a previous version
 */
@Mojo(name = "osgi-test", defaultPhase = INTEGRATION_TEST)
public class BndOSGiTestMojo extends AbstractMojo {
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${settings}", readonly = true, required = true)
	private Settings settings;

	@Parameter(property = "bnd.test.bndrun", defaultValue = "${project.basedir}/test.bndrun", readonly = true)
	private File bndrunFile;

	@Component
	private RepositorySystem system;

	public void execute() throws MojoExecutionException, MojoFailureException {
    	try {
    		Properties beanProperties = new BeanProperties();
    		beanProperties.put("project", project);
    		beanProperties.put("settings", settings);
    		Properties mavenProperties = new Properties(beanProperties);
    		mavenProperties.putAll(project.getProperties());
    		
    		mavenProperties.put(Constants.SOURCEPATH, project.getBuild().getSourceDirectory());
    		mavenProperties.put(Constants.DEFAULT_PROP_BIN_DIR, project.getBuild().getOutputDirectory());
    		mavenProperties.put(Constants.DEFAULT_PROP_TARGET_DIR, project.getBuild().getDirectory() + "/bnd-test");
    		mavenProperties.put(Constants.RUNSTORAGE, project.getBuild().getDirectory() + "/bnd-test/fw");
    		
    		Processor processor = new Processor(mavenProperties);
    		processor.setProperties(bndrunFile);
			try(Run run = new Run(createStandaloneWorkspace(processor, bndrunFile.toURI()), bndrunFile)) {
				run.setParent(processor);
				run.test();
				reportErrorsAndWarnings(run);
			}
		} catch (Exception e) {
			throw new MojoExecutionException("The bnd test execution failed", e);
		}
    }
	
	/* Everything below here is copied from the bnd maven plugin - perhaps we need some common utilities? */
	
	private void reportErrorsAndWarnings(Run run) throws MojoExecutionException {
		Log log = getLog();

		List<String> warnings = run.getWarnings();
		for (String warning : warnings) {
			log.warn(warning);
		}
		List<String> errors = run.getErrors();
		for (String error : errors) {
			log.error(error);
		}
		if (!run.isOk()) {
			if (errors.size() == 1)
				throw new MojoExecutionException(errors.get(0));
			else
				throw new MojoExecutionException("Errors in bnd processing, see log for details.");
		}
	}
	
	private class BeanProperties extends Properties {
		private static final long serialVersionUID = 1L;

		BeanProperties() {
			super();
		}
		
		@Override
		public String getProperty(String key) {
			final int i = key.indexOf('.');
			final String name = (i > 0) ? key.substring(0, i) : key;
			Object value = get(name);
			if ((value != null) && (i > 0)) {
				value = getField(value, key.substring(i + 1));
			}
			if (value == null) {
				return null;
			}
			return value.toString();
		}
		
		private Object getField(Object target, String key) {
			final int i = key.indexOf('.');
			final String fieldName = (i > 0) ? key.substring(0, i) : key;
			final String getterSuffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			Object value = null;
			try {
				Class<?> targetClass = target.getClass();
				while (!Modifier.isPublic(targetClass.getModifiers())) {
					targetClass = targetClass.getSuperclass();
				}
				Method getter;
				try {
					getter = targetClass.getMethod("get" + getterSuffix);
				} catch (NoSuchMethodException nsme) {
					getter = targetClass.getMethod("is" + getterSuffix);
				}
				value = getter.invoke(target);
			} catch (Exception e) {
				getLog().debug("Could not find getter method for field: " + fieldName, e);
			}
			if ((value != null) && (i > 0)) {
				value = getField(value, key.substring(i + 1));
			}
			return value;
		}
	}
}
