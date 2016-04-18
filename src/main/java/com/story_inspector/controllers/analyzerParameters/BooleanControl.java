package com.story_inspector.controllers.analyzerParameters;

import java.io.IOException;

import com.story_inspector.analysis.ParameterSpec;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * {@link AnalyzerParameterControl} for {@link Boolean} parameters. Implemented as a {@link CheckBox}.
 *
 * @author mizitch
 *
 */
public class BooleanControl extends VBox implements AnalyzerParameterControl<Boolean> {

	private final ParameterSpec<Boolean> spec;

	@FXML
	private CheckBox checkBox;

	@FXML
	private Tooltip checkBoxTooltip;

	/**
	 * Creates a new instance.
	 *
	 * @param spec
	 *            The {@link ParameterSpec} for the parameter the new control should represent.
	 */
	public BooleanControl(final ParameterSpec<Boolean> spec) {
		this.spec = spec;

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/parameterControls/BooleanControl.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}

		this.checkBox.setText(spec.getName());
		this.checkBoxTooltip.setText(spec.getDescription());

		if (spec.hasDefaultValue())
			this.setValue(spec.getDefaultValue());
	}

	@Override
	public ObservableValue<Boolean> getObservableParameterValue() {
		return this.checkBox.selectedProperty();
	}

	@Override
	public void setValue(final Boolean value) {
		this.checkBox.setSelected(value);
	}

	@Override
	public void setValidated(final boolean validated) {
		if (validated)
			this.getStyleClass().remove("invalidated-control");
		else
			this.getStyleClass().add("invalidated-control");
	}

	@Override
	public ParameterSpec<Boolean> getSpec() {
		return this.spec;
	}

}
