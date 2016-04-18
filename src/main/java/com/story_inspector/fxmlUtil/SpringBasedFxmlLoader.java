package com.story_inspector.fxmlUtil;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Implementation of {@link FxmlLoaderHelper} that retrieves the controller for the FXML component from spring.
 * 
 * @author mizitch
 *
 */
@Component
public class SpringBasedFxmlLoader implements FxmlLoaderHelper, ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public Parent load(final String fxmlFileLocation) {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(c -> this.applicationContext.getBean(c));
		fxmlLoader.setLocation(getClass().getResource("/fxml/"));
		final InputStream fxmlInput = getClass().getResourceAsStream(fxmlFileLocation);
		try {
			return fxmlLoader.load(fxmlInput);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
