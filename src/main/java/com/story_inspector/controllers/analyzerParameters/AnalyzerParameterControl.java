package com.story_inspector.controllers.analyzerParameters;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.ParameterSpec;

import javafx.beans.value.ObservableValue;

/**
 * A control that allows the user to set the value of an {@link Analyzer} parameter. Generated based on a {@link ParameterSpec}.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of the parameter.
 */
public interface AnalyzerParameterControl<T> {

	/**
	 * Returns the observable parameter value. Updated based on current state of this UI control.
	 *
	 * @return The observable parameter value. Updated based on current state of this UI control.
	 */
	ObservableValue<T> getObservableParameterValue();

	/**
	 * Returns the current parameter value based on the state of this control.
	 *
	 * @return The current parameter value based on the state of this control.
	 */
	public default T getValue() {
		return getObservableParameterValue().getValue();
	}

	/**
	 * Sets the current parameter value to the provided value. Updates the control appropriately.
	 *
	 * @param value
	 *            The value to set
	 */
	public void setValue(T value);

	/**
	 * Sets whether the parameter value represented by this control is "valid." If not, the control will update its visual style to indicate to the
	 * user that it is not currently valid.
	 *
	 * @param validated
	 *            Whether the control's value is valid
	 */
	public void setValidated(boolean validated);

	/**
	 * Returns the {@link ParameterSpec} for the parameter this control represents.
	 *
	 * @return The {@link ParameterSpec} for the parameter this control represents.
	 */
	public ParameterSpec<T> getSpec();
}
