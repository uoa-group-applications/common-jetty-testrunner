package nz.ac.auckland.war

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import java.lang.reflect.Method

/**
 * Not called TestRunnerTest so it doesn't try and run as a test.
 */
class TestRunner {

	public static final String HTTP_PORT_OPTION = "http.port";

	private int httpPort;

	protected static int getEnvPort(String name, int defport) {
		String httpPortStr = System.getProperty(WebAppRunner.WEBAPP_HTTP_PORT_PROPERTY)

		if (!httpPortStr)
			httpPortStr = System.getProperty(HTTP_PORT_OPTION)

		if (httpPortStr) {
			return Integer.parseInt(httpPortStr, 10)
		} else {
			String value = System.getenv(name);
			return value != null ? Integer.valueOf(value) : defport;
		}
	}

    /**
     * Emulate WebAppBooter's behaviour.
     * A temporary workaround for WebAppBooter class not found
     * (class loader issue? fix for one module breaks it for other modules?)
     */
    public static void runInitializers() {

        String initializers = System.getProperty('warbooter.initializers');
        System.out.println('Initializers: ' + initializers?:'NONE');

        if (initializers != null) {
            String[] inits = initializers.trim().split(',');

            for (String className : inits) {
                try {
                    Class clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                    Object init = clazz.newInstance();
                    Method method = clazz.getMethod("init");
                    method.invoke(init);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to initialize due to missing class $className", ex);
                }
            }
        }
    }

	@BeforeClass
	public static void initStatic() {
        runInitializers()
    }

	@Before
	public void init() {
		httpPort = getEnvPort("HTTP_PORT", 8090);
		System.setProperty(WebAppRunner.WEBAPP_HTTP_PORT_PROPERTY, String.valueOf(httpPort));
		if (System.getProperty(WebAppRunner.WEBAPP_CONTEXT_PROPERTY) == null)
			System.setProperty(WebAppRunner.WEBAPP_CONTEXT_PROPERTY, WebAppRunner.WEBAPP_CONTEXT_DEFAULT);
		System.setProperty(WebAppRunner.WEBAPP_SHUTDOWN_TIMEOUT_PROPERTY, "10000");

		WebAppRunner.run(null);
	}

	@Test
	public void letmewait() {
		Thread.sleep(Long.MAX_VALUE)
	}
}
