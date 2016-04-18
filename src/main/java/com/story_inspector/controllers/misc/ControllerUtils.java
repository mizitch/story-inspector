package com.story_inspector.controllers.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.ParameterValidator;
import com.story_inspector.analysis.ValidationResult;
import com.story_inspector.analysis.serialization.AnalysisSerializer;
import com.story_inspector.controllers.analyzerParameters.AnalyzerParameterControl;
import com.story_inspector.fxmlUtil.FxmlLoaderHelper;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * Collection of static utility methods for controllers.
 *
 * @author mizitch
 *
 */
public class ControllerUtils {
	private static final Logger log = LoggerFactory.getLogger(ControllerUtils.class);

	private ControllerUtils() {
		throw new UnsupportedOperationException("Collection of static utility methods, cannot instantiate");
	}

	/**
	 * Used to prevent a parameter control from having an invalid value. Will check to see if the new value is valid and if not, returns false and
	 * displays an alert to the user.
	 *
	 * @param newValue
	 *            The new value for the parameter control.
	 * @param validator
	 *            The validator for this parameter
	 * @return Whether the new value is valid.
	 * @see AnalyzerParameterControl
	 */
	public static <T> boolean checkValidity(final T newValue, final ParameterValidator<T> validator) {
		final ValidationResult result = validator.validateParameter(newValue);
		if (result.isValid())
			return true;
		else {
			final Alert alert = new Alert(AlertType.INFORMATION, generateValidationMessage(result.getValidationErrors()));
			alert.setTitle("Cannot complete action");
			alert.showAndWait();
			return false;
		}
	}

	private static String generateValidationMessage(final List<String> validationErrors) {
		final StringBuilder builder = new StringBuilder("Cannot complete action:");
		for (final String error : validationErrors) {
			builder.append("\n\t").append(error);
		}
		return builder.toString();
	}

	/**
	 * Performs the provided action on the stage when the provided Node is placed within a stage.
	 *
	 * @param node
	 *            The node whose stage an action should be performed on.
	 * @param onCreation
	 *            The action to perform on the stage.
	 */
	public static void performWithStage(final Node node, final Consumer<Stage> onCreation) {
		if (node.getScene() != null)
			performWithStage(node.getScene(), onCreation);
		else
			node.sceneProperty().addListener((ob, o, n) -> {
				if (n != null)
					performWithStage(n, onCreation);
			});
	}

	/**
	 * Performs the provided action on the stage when the provided Scene is placed within a stage.
	 *
	 * @param scene
	 *            The scene whose stage an action should be performed on.
	 * @param onCreation
	 *            The action to perform on the stage.
	 */
	public static void performWithStage(final Scene scene, final Consumer<Stage> onCreation) {
		if (scene.getWindow() != null)
			onCreation.accept((Stage) scene.getWindow());
		else
			scene.windowProperty().addListener((ob, o, n) -> {
				if (n != null) {
					onCreation.accept((Stage) n);
				}
			});
	}

	/**
	 * Given a mouse event handler, make a wrapper handler that only activates the internal handler on double clicks.
	 *
	 * @param handlerToWrap
	 *            The internal handler
	 * @return The new handler that only activates on double clicks.
	 */
	public static EventHandler<? super MouseEvent> generateDoubleClickHandler(final EventHandler<? super MouseEvent> handlerToWrap) {
		return e -> {
			if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
				handlerToWrap.handle(e);
			}
		};
	}

	/**
	 * Transform the list of provided analyzers to a single string.
	 *
	 * @param analyzers
	 *            The analyzers to serialize
	 * @param serializer
	 *            The serializer to use
	 * @return The serialized analyzers.
	 */
	public static String serializeAnalyzersToString(final List<Analyzer<?>> analyzers, final AnalysisSerializer serializer) {
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			serializer.writeAnalyzers(analyzers, outStream);
			return outStream.toString(StandardCharsets.UTF_8.name());
		} catch (final IOException e) {
			log.error("Failed to serialize analyzers to string " + analyzers, e);
			return null;
		}
	}

	/**
	 * Transform the provided string to a list of analyzers.
	 *
	 * @param string
	 *            The serialized analyzers
	 * @param serializer
	 *            The deserializer to use
	 * @return The list of deserialized analyzers.
	 */
	public static List<Analyzer<?>> deserializeAnalyzersFromString(final String string, final AnalysisSerializer serializer) {
		final ByteArrayInputStream inStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
		try {
			return serializer.readAnalyzers(inStream);
		} catch (final IOException e) {
			log.error("Failed to deserialize analyzers from string " + string, e);
			return null;
		}
	}

	/**
	 * Open the provided FXML reference in a new window with the provided title.
	 *
	 * @param fxmlLoaderHelper
	 *            The {@link FxmlLoaderHelper} to use.
	 * @param fxmlResource
	 *            The fxml resource path
	 * @param title
	 *            The title for the new window.
	 */
	public static void openFxmlInWindow(final FxmlLoaderHelper fxmlLoaderHelper, final String fxmlResource, final String title) {
		final Stage stage = setupFxmlInWindow(fxmlLoaderHelper, fxmlResource, title);
		stage.showAndWait();
	}

	/**
	 * Create a stage using the provided FXML reference and title, but do not show the stage.
	 *
	 * @param fxmlLoaderHelper
	 *            The {@link FxmlLoaderHelper} to use.
	 * @param fxmlResource
	 *            The fxml resource path
	 * @param title
	 *            The title for the new window.
	 * @return The new stage
	 */
	public static Stage setupFxmlInWindow(final FxmlLoaderHelper fxmlLoaderHelper, final String fxmlResource, final String title) {
		final Parent root = fxmlLoaderHelper.load(fxmlResource);
		final Stage stage = new Stage();
		final Scene scene = new Scene(root, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(title);
		stage.setScene(scene);

		return stage;
	}

	/**
	 * Open the provided FXML reference in a new window with the provided title and controller
	 *
	 * @param controller
	 *            The controller to use.
	 * @param fxmlResource
	 *            The fxml resource path
	 * @param title
	 *            The title for the new window.
	 */
	public static void openFxmlInWindowWithController(final Object controller, final String fxmlResource, final String title) {
		final Stage stage = setupFxmlInWindowWithController(controller, fxmlResource, title);
		stage.showAndWait();
	}

	/**
	 * Create a stage using the provided FXML reference, title and controller, but do not show the stage.
	 *
	 * @param controller
	 *            The controller to use.
	 * @param fxmlResource
	 *            The fxml resource path
	 * @param title
	 *            The title for the new window.
	 * @return The new stage
	 */
	public static Stage setupFxmlInWindowWithController(final Object controller, final String fxmlResource, final String title) {
		final FXMLLoader fxmlLoader = new FXMLLoader(ControllerUtils.class.getResource(fxmlResource));

		fxmlLoader.setController(controller);

		try {
			final Parent root = fxmlLoader.load();
			final Stage stage = new Stage();
			final Scene scene = new Scene(root, -1, -1);

			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle(title);
			stage.setScene(scene);

			return stage;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Close the window the provided node is within. If the node is not currently within a window, this will do nothing.
	 *
	 * @param node
	 *            The node whose window should be closed.
	 */
	public static void closeWindow(final Node node) {
		final Stage window = getStage(node);
		if (window != null)
			closeWindow(window);
	}

	/**
	 * Close the provided window
	 *
	 * @param window
	 *            The window to close.
	 */
	public static void closeWindow(final Window window) {
		window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	/**
	 * Get the stage the node is currently within. If the node is not currently within a stage, returns null.
	 *
	 * @param node
	 *            The node whose stage should be retrieved
	 * @return The node's stage or null if the node is not currently within a stage.
	 */
	public static Stage getStage(final Node node) {
		if (node.getScene() != null && node.getScene().getWindow() != null) {
			return (Stage) node.getScene().getWindow();
		} else {
			return null;
		}
	}
}
