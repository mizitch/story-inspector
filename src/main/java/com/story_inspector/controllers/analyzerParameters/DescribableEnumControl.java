package com.story_inspector.controllers.analyzerParameters;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.Validate;

import com.story_inspector.analysis.Describable;
import com.story_inspector.analysis.ParameterSpec;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * {@link AnalyzerParameterControl} for {@link Describable} {@link Enum} parameters. Implemented as a {@link ChoiceBox} with a {@link Label}.
 *
 * @author mizitch
 *
 */
public class DescribableEnumControl<T extends Enum<T> & Describable> extends VBox implements AnalyzerParameterControl<T> {
	private final ParameterSpec<T> spec;

	@FXML
	private ChoiceBox<T> choiceBox;

	@FXML
	private Label nameLabel;

	@FXML
	private Tooltip choiceBoxTooltip;

	@FXML
	private Tooltip nameLabelTooltip;

	/**
	 * Creates a new instance.
	 *
	 * @param spec
	 *            The {@link ParameterSpec} for the parameter the new control should represent.
	 */
	public DescribableEnumControl(final ParameterSpec<T> spec) {
		this.spec = spec;

		validateUniqueNames();

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/parameterControls/DescribableEnumControl.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}

		this.nameLabel.setText(spec.getName() + ":");
		this.nameLabelTooltip.setText(spec.getDescription());
		this.choiceBoxTooltip.setText(spec.getDescription());

		this.choiceBox.getItems().addAll(spec.getParameterType().getEnumConstants());
		this.choiceBox.setConverter(new StringConverter<T>() {

			@Override
			public String toString(final T object) {
				return object.getName();
			}

			@Override
			public T fromString(final String string) {
				return Arrays.stream(spec.getParameterType().getEnumConstants()).filter(c -> c.getName().equals(string)).findFirst().get();
			}
		});
		if (spec.hasDefaultValue())
			this.setValue(spec.getDefaultValue());
	}

	private void validateUniqueNames() {
		final long numUniqueNames = Arrays.stream(this.spec.getParameterType().getEnumConstants()).map(enumVal -> enumVal.getName()).distinct()
				.count();
		Validate.isTrue(numUniqueNames == this.spec.getParameterType().getEnumConstants().length,
				"Cannot support describable enums with duplicated names");
	}

	@Override
	public ObservableValue<T> getObservableParameterValue() {
		return this.choiceBox.valueProperty();
	}

	@Override
	public void setValue(final T value) {
		this.choiceBox.setValue(value);
	}

	@Override
	public void setValidated(final boolean validated) {
		if (validated)
			this.getStyleClass().remove("invalidated-control");
		else
			this.getStyleClass().add("invalidated-control");
	}

	@Override
	public ParameterSpec<T> getSpec() {
		return this.spec;
	}
}
