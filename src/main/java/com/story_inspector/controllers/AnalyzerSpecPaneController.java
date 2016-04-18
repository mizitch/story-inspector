package com.story_inspector.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerCreationResult;
import com.story_inspector.analysis.AnalyzerSpec;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.ParameterSpec;
import com.story_inspector.analysis.ValidationResult;
import com.story_inspector.controllers.analyzerParameters.AnalyzerParameterControl;
import com.story_inspector.controllers.analyzerParameters.AnalyzerParameterControlFactory;
import com.story_inspector.controllers.misc.ControllerUtils;
import com.story_inspector.controllers.saveManagement.SavableContainer;
import com.story_inspector.controllers.saveManagement.SaveManagerBase;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * Controller for panel that allows user to specify analyzer details. Used to create a new analyzer or edit an existing one.
 *
 * Lets user specify name, description, any parameters defined by the {@link AnalyzerType} and whether to suppress comment recording (if the analyzer
 * is one which records comments).
 *
 * Displays validation errors if analyzer creation fails.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of analyzer the user is creating or editing.
 */
public class AnalyzerSpecPaneController<T extends AnalyzerType<T>> implements SavableContainer<AnalyzerSpec<T>> {

	private static final String VALIDATION_INTRO_TEXT = "Can't save due to following errors:";

	private static final String VALIDATION_MESSAGE_STYLE_CLASS = "validation-message";

	/**
	 * Save manager for this controller. The analyzer is saved in the result field of this class.
	 *
	 * @author mizitch
	 *
	 */
	private class AnalyzerSaveManager extends SaveManagerBase<AnalyzerSpec<T>> {

		private AnalyzerSaveManager() {
			super(AnalyzerSpecPaneController.this);
		}

		@Override
		protected boolean forceSave() {
			tryCreateAnalyzer();
			return AnalyzerSpecPaneController.this.result != null;
		}
	}

	@FXML
	private Label analyzerTypeLabel;

	@FXML
	private Tooltip analyzerTypeLabelTooltip;

	@FXML
	private TextField nameTextField;

	@FXML
	private TextArea descriptionTextArea;

	@FXML
	private CheckBox suppressCommentRecordingCheckBox;

	@FXML
	private VBox parameterContainer;

	@FXML
	private VBox validationErrorContainer;

	private final T analyzerType;
	private final AnalyzerSpec<T> initialAnalyzerSpec;
	private final AnalyzerParameterControlFactory parameterControlFactory;
	private final Map<ParameterSpec<?>, AnalyzerParameterControl<?>> specControlMap;
	private Analyzer<T> result;
	private AnalyzerSaveManager saveManager;
	private final boolean startWithUnsavedChanges;

	private AnalyzerSpecPaneController(final T analyzerType, final AnalyzerSpec<T> analyzerSpec,
			final AnalyzerParameterControlFactory parameterControlFactory, final boolean startWithUnsavedChanges) {
		this.analyzerType = analyzerType;
		this.initialAnalyzerSpec = analyzerSpec;
		this.parameterControlFactory = parameterControlFactory;
		this.specControlMap = new HashMap<>();
		this.startWithUnsavedChanges = startWithUnsavedChanges;
	}

	/**
	 * Creates a new instance that handles creating a new {@link Analyzer} for the provided {@link AnalyzerType}.
	 *
	 * @param analyzerType
	 *            The type of analyzer the user wants to create.
	 * @param parameterControlFactory
	 *            Used to create controls for analyzer parameters.
	 */
	public AnalyzerSpecPaneController(final T analyzerType, final AnalyzerParameterControlFactory parameterControlFactory) {
		this(analyzerType, null, parameterControlFactory, false);
	}

	/**
	 * Creates a new instance that handles editing an existing {@link Analyzer}.
	 *
	 * @param analyzerSpec
	 *            Details of the existing {@link Analyzer}.
	 * @param parameterControlFactory
	 *            Used to create controls for analyzer parameters.
	 * @param startWithUnsavedChanges
	 *            Whether the "unsaved changes" flag (which prompts users to save if they attempt to exit) should be set initially.
	 */
	public AnalyzerSpecPaneController(final AnalyzerSpec<T> analyzerSpec, final AnalyzerParameterControlFactory parameterControlFactory,
			final boolean startWithUnsavedChanges) {
		this(analyzerSpec.getAnalyzerType(), analyzerSpec, parameterControlFactory, startWithUnsavedChanges);
	}

	// INITIALIZATION

	@FXML
	private void initialize() {
		initialize(this.initialAnalyzerSpec);
	}

	private void initialize(final AnalyzerSpec<T> initialSpec) {
		// Only show the suppress comments checkbox if this analyzer type actually produces comments
		this.suppressCommentRecordingCheckBox.setVisible(this.analyzerType.producesComments());
		this.suppressCommentRecordingCheckBox.setManaged(this.analyzerType.producesComments());

		this.analyzerTypeLabel.setText(this.analyzerType.getName());
		this.analyzerTypeLabelTooltip.setText(this.analyzerType.getDescription());

		// Register save manager with stage when stage is initialized
		this.saveManager = new AnalyzerSaveManager();
		ControllerUtils.performWithStage(this.nameTextField, stage -> {
			stage.setOnCloseRequest(e -> {
				final boolean safeToProceed = this.saveManager
						.promptSave("Save changes to current analyzer before closing? Otherwise your current changes will be lost.");

				if (!safeToProceed)
					e.consume();
			});
		});

		initializeParameterControls();
		if (initialSpec != null)
			initializeSpec(initialSpec);

		if (!this.startWithUnsavedChanges)
			this.saveManager.syncSavedEntity();
	}

	/**
	 * Create, register and add all parameter controls.
	 */
	private void initializeParameterControls() {
		this.parameterContainer.getChildren().clear();
		this.specControlMap.clear();
		for (final ParameterSpec<?> spec : this.analyzerType.getParameterSpecs()) {
			initializeParameterControl(spec);
		}
	}

	private <V, W extends Node & AnalyzerParameterControl<V>> void initializeParameterControl(final ParameterSpec<V> spec) {
		final W control = this.parameterControlFactory.createAnalyzerParameterControl(spec);
		this.specControlMap.put(spec, control);
		this.parameterContainer.getChildren().add(control);
	}

	/**
	 * Set all controls to values contained in provided analyzer spec.
	 */
	private void initializeSpec(final AnalyzerSpec<T> analyzerSpec) {
		Validate.isTrue(analyzerSpec.getAnalyzerType().tryCreateAnalyzer(analyzerSpec).wasSuccessful(),
				"Can't initialize analyzer spec pane with invalid analyzer spec");

		this.nameTextField.setText(analyzerSpec.getName());
		this.descriptionTextArea.setText(analyzerSpec.getDescription());
		this.suppressCommentRecordingCheckBox.setSelected(analyzerSpec.isCommentRecordingSuppressed());

		for (final AnalyzerParameterControl<?> parameterControl : this.specControlMap.values()) {
			initializeControlValue(parameterControl, analyzerSpec);
		}
	}

	private <V> void initializeControlValue(final AnalyzerParameterControl<V> control, final AnalyzerSpec<T> analyzerSpec) {
		final ParameterSpec<V> parameterSpec = control.getSpec();
		final Object value = analyzerSpec.getAnalyzerParameterValues().get(parameterSpec.getId());
		control.setValue(parameterSpec.getParameterType().cast(value));
	}

	// BUTTON ACTIONS

	@FXML
	private void okay() {
		// Try saving and close pane if successful
		if (this.saveManager.save())
			ControllerUtils.closeWindow(this.parameterContainer);
	}

	@FXML
	private void cancel() {
		// Save manager will prompt user for confirmation if there are unsaved changes
		ControllerUtils.closeWindow(this.parameterContainer);
	}

	// SAVABLE CONTAINER METHODS

	@Override
	public AnalyzerSpec<T> getSavableEntity() {
		return generateSpec();
	}

	@Override
	public void setSavableEntity(final AnalyzerSpec<T> newValue) {
		initialize(newValue);
	}

	@Override
	public void newEntity() {
		initialize(null);
	}

	// ANALYZER CREATION & RETRIEVAL

	/**
	 * Returns the result of the user's analyzer creation or modification. If the user canceled or exited without saving, will return null.
	 *
	 * @return The result of the user's analyzer creation or modification. If the user canceled or exited without saving, will be null.
	 */
	public Analyzer<T> getResult() {
		return this.result;
	}

	/**
	 * Try creating the analyzer based on the inputs. If successful, set the result field, otherwise display validation errors.
	 */
	private void tryCreateAnalyzer() {
		final AnalyzerCreationResult<T> creationResults = this.analyzerType.tryCreateAnalyzer(generateSpec());
		if (!creationResults.wasSuccessful()) {
			final List<String> validationErrors = new ArrayList<>();
			validationErrors.addAll(creationResults.getGlobalResult().getValidationErrors());
			for (final ParameterSpec<?> spec : this.analyzerType.getParameterSpecs()) {
				final ValidationResult specResult = creationResults.getParameterResults().get(spec);

				this.specControlMap.get(spec).setValidated(specResult.isValid());
				validationErrors.addAll(specResult.getValidationErrors());
			}
			setValidationErrors(validationErrors);
		} else {
			this.result = creationResults.getAnalyzer();
		}
	}

	/**
	 * Clears any previous validation errors and sets validation errors to the provided list of strings.
	 */
	private void setValidationErrors(final List<String> validationErrors) {
		this.validationErrorContainer.getChildren().clear();

		this.validationErrorContainer.getChildren().add(generateValidationMessageLabel(VALIDATION_INTRO_TEXT));
		for (final String msg : validationErrors) {
			this.validationErrorContainer.getChildren().add(generateValidationMessageLabel("\t" + msg));
		}
	}

	/**
	 * Generates a label that contains a validation message.
	 */
	private Label generateValidationMessageLabel(final String message) {
		final Label l = new Label(message);
		l.setWrapText(true);
		l.getStyleClass().add(VALIDATION_MESSAGE_STYLE_CLASS);
		return l;
	}

	private AnalyzerSpec<T> generateSpec() {
		return new AnalyzerSpec<T>(this.nameTextField.getText(), this.descriptionTextArea.getText(), this.analyzerType,
				this.suppressCommentRecordingCheckBox.isSelected(), generateParameterValues());
	}

	/**
	 * Generate map from parameter id to parameter value based on the parameter spec controls.
	 *
	 * @return
	 */
	private Map<String, Object> generateParameterValues() {
		return this.specControlMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getId(), e -> e.getValue().getValue()));
	}

}
