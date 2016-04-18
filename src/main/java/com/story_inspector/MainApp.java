package com.story_inspector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.story_inspector.controllers.misc.ApplicationAware;
import com.story_inspector.fxmlUtil.FxmlLoaderHelper;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Main JavaFX application class, sets up spring and launches the application.
 *
 * @author mizitch
 *
 */
public class MainApp extends Application {

	private static final String STORY_INSPECTOR_TITLE = "Story Inspector";
	private static final Logger log = LoggerFactory.getLogger(MainApp.class);
	/**
	 * Set up the application context. It is annotation driven, so the file is basically a stub.
	 */
	private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("app-config.xml");

	/**
	 * Utility method for IDEs. Not actually used by JavaFX, so don't put anything important in here.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		launch(args);
	}

	/**
	 * Start the application.
	 */
	@Override
	public void start(final Stage stage) throws Exception {

		log.info("Starting Story Inspector");

		initializeApplicationAwareBeans();

		final String fxmlFile = "/fxml/MainPage.fxml";
		log.debug("Loading FXML for main view from: {}", fxmlFile);
		final FxmlLoaderHelper loader = applicationContext.getBean(FxmlLoaderHelper.class);
		final Parent rootNode = loader.load(fxmlFile);

		log.debug("Showing JFX scene");
		final Scene scene = new Scene(rootNode, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

		stage.setTitle(STORY_INSPECTOR_TITLE);
		stage.setScene(scene);

		stage.show();
	}

	/**
	 * Beans that implement {@link ApplicationAware} need access to the JavaFX Application class (this class). This method bootstraps those beans.
	 */
	private void initializeApplicationAwareBeans() {
		for (final ApplicationAware applicationAwareBean : applicationContext.getBeansOfType(ApplicationAware.class).values()) {
			applicationAwareBean.setApplication(this);
		}
	}
}
