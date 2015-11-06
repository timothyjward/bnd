package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

@RunWith(JUnit4.class)
public class ExampleTest {

    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    @Test
    public void testUUID() throws Exception {
        assertNotNull(context.getProperty(Constants.FRAMEWORK_UUID));
    }

    @Test
    public void testDependency() throws Exception {
    	boolean found = false;
    	
    	for (Bundle bundle : context.getBundles()) {
			if("javax.servlet-api".equals(bundle.getSymbolicName()) && 
					"3.0.1".equals(bundle.getVersion().toString())) {
				found = true;
				break;
			}
		}
    	assertTrue(found);
    }
  
}
