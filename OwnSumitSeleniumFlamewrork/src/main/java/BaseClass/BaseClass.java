package BaseClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

import ActionDriver.ActionDriver;
import utilities.ExtentManager;
import utilities.LoggerManager;

public class BaseClass {

	protected static Properties prop;

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private static ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();

	public static final Logger logger = LoggerManager.getLogger(BaseClass.class);

	protected ThreadLocal<SoftAssert> softAssert = ThreadLocal.withInitial(SoftAssert::new);

	public SoftAssert getSoftAssert() {
		return softAssert.get();
	}

	@BeforeSuite
	public void loadConfig() throws IOException {

		prop = new Properties();

		FileInputStream fis = new FileInputStream(
				System.getProperty("user.dir") + "/src/main/resources/config.properties");

		prop.load(fis);

		logger.info("config.properties loaded");
	}

	@BeforeMethod
	@Parameters("browser")
	public synchronized void setup(@Optional("chrome") String browser) {

		System.out.println("Launching Browser: " + browser);

		launchBrowser(browser);
		configureBrowser();

		actionDriver.set(new ActionDriver(getDriver()));

		logger.info("Driver initialized for thread: " + Thread.currentThread().getId());
	}

	private void launchBrowser(String browser) {

		if (browser.equalsIgnoreCase("chrome")) {

			ChromeOptions options = new ChromeOptions();
			options.addArguments("--disable-notifications");
//			options.addArguments("--headless"); // Run Chrome in headless mode
//			options.addArguments("--disable-gpu"); // Disable GPU for headless mode
			//options.addArguments("--window-size=1920,1080"); // Set window size
//			options.addArguments("--no-sandbox"); // Required for some CI environments like Jenkins
//			options.addArguments("--disable-dev-shm-usage"); // Resolve issues in resource-limited environments
			Map<String, Object> prefs = new HashMap<>();
			prefs.put("profile.default_content_setting_values.notifications", 1);
			options.setExperimentalOption("prefs", prefs);
            // this Is send my Krushna To Notification Off
			driver.set(new ChromeDriver(options)); // Option Obj Passed Here

			logger.info("ChromeDriver started");

		}

		else if (browser.equalsIgnoreCase("firefox")) {

			FirefoxOptions options = new FirefoxOptions();

//			options.addArguments("--headless"); // Run Firefox in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU rendering (useful for headless mode)
			options.addArguments("--width=1920"); // Set browser width
			options.addArguments("--height=1080"); // Set browser height
			options.addArguments("--disable-notifications"); // Disable browser notifications
			options.addArguments("--no-sandbox"); // Needed for CI/CD environments
			options.addArguments("--disable-dev-shm-usage"); // Prevent crashes in low-resource environments


			driver.set(new FirefoxDriver(options)); // Option Obj Passed Here

			logger.info("FirefoxDriver started");

		}

		else if (browser.equalsIgnoreCase("edge")) {

			EdgeOptions options = new EdgeOptions();
//			options.addArguments("--headless"); // Run Edge in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU acceleration
			options.addArguments("--window-size=1920,1080"); // Set window size
			options.addArguments("--disable-notifications"); // Disable pop-up notifications
			options.addArguments("--no-sandbox"); // Needed for CI/CD
			options.addArguments("--disable-dev-shm-usage"); // Prevent resource-limited crashes

			driver.set(new EdgeDriver(options));  // Option Obj Passed Here

			logger.info("EdgeDriver started");

		}

		else {
			throw new IllegalArgumentException("Browser not supported: " + browser);
		}

		ExtentManager.registerDriver(getDriver());
	}

	private void configureBrowser() {

		int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));

		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

		getDriver().manage().window().maximize();

		getDriver().get(prop.getProperty("url_base"));
	}

	@AfterMethod
	public void tearDown() {

		if (getDriver() != null) {

			getDriver().quit();

			logger.info("Browser closed");
		}

		driver.remove();
		actionDriver.remove();
	}

	public static WebDriver getDriver() {

		if (driver.get() == null) {
			throw new IllegalStateException("Driver not initialized");
		}

		return driver.get();
	}

	public static ActionDriver getActionDriver() {

		if (actionDriver.get() == null) {
			throw new IllegalStateException("ActionDriver not initialized");
		}

		return actionDriver.get();
	}

	public static Properties getProp() {
		return prop;
	}

	public void staticWait(int seconds) {
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
	}
}