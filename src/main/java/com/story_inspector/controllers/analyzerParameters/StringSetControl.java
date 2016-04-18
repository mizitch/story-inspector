package com.story_inspector.controllers.analyzerParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.story_inspector.analysis.ParameterSpec;
import com.story_inspector.analysis.parameterTypes.StringSet;
import com.story_inspector.controllers.misc.ControllerUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

/**
 * {@link AnalyzerParameterControl} for {@link StringSet} parameters. Implemented using a {@link ListView}.
 *
 * @author mizitch
 *
 */
public class StringSetControl extends VBox implements AnalyzerParameterControl<StringSet> {
	private final ParameterSpec<StringSet> spec;

	@FXML
	private ListView<String> listView;

	@FXML
	private Label nameLabel;

	@FXML
	private Tooltip nameLabelTooltip;

	@FXML
	private Tooltip listViewTooltip;

	@FXML
	private TextField addTextField;

	@FXML
	private Button addButton;

	/**
	 * Creates a new instance.
	 *
	 * @param spec
	 *            The {@link ParameterSpec} for the parameter the new control should represent.
	 */
	public StringSetControl(final ParameterSpec<StringSet> spec) {
		this.spec = spec;

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/parameterControls/StringListControl.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}

		this.nameLabel.setText(spec.getName() + ":");
		this.nameLabelTooltip.setText(spec.getDescription());
		this.listViewTooltip.setText(spec.getDescription());

		this.listView.setCellFactory((v) -> new StringCell());
		this.listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.listView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				deleteCurrentSelection();
			}
		});

		this.listView.setOnEditCommit(e -> {
			final List<String> list = this.listView.getItems();
			final String newValue = e.getNewValue();
			final String oldValue = list.get(e.getIndex());

			if (e.getIndex() < 0 || e.getIndex() > list.size())
				return;

			// No change, nothing to do
			if (StringUtils.equals(oldValue, newValue))
				return;

			// List already contains new value, just need to remove element that was changed
			if (list.contains(newValue)) {
				deleteString(newValue);
			} else {
				// Effectively removes the old value and adds the new value
				replaceString(oldValue, newValue);
			}
		});

		if (spec.hasDefaultValue())
			this.setValue(spec.getDefaultValue());
	}

	/**
	 * Custom {@link ListCell} implementation for strings in {@link StringSet}.
	 *
	 * @author mizitch
	 *
	 */
	private class StringCell extends TextFieldListCell<String> {
		private StringCell() {
			super(new DefaultStringConverter());
		}

		@Override
		public void updateItem(final String item, final boolean empty) {
			super.updateItem(item, empty);

			if (empty || item == null) {
				setGraphic(null);
				setText("");
				setContextMenu(null);
			} else {
				setText(item);
				setContextMenu(generateStringCellContextMenu(item));
			}
		}

		private ContextMenu generateStringCellContextMenu(final String item) {
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem edit = new MenuItem("Edit");
			edit.setOnAction(e -> StringSetControl.this.listView.edit(StringSetControl.this.listView.getItems().indexOf(item)));
			final MenuItem delete = new MenuItem("Delete");
			delete.setOnAction(e -> deleteCurrentSelection());
			contextMenu.getItems().add(edit);
			contextMenu.getItems().add(delete);
			return contextMenu;
		}

	}

	/**
	 * Deletes all currently selected strings from set.
	 */
	private void deleteCurrentSelection() {
		final List<String> selectedItems = new ArrayList<String>(this.listView.getSelectionModel().getSelectedItems());
		if (selectedItems != null) {
			deleteStrings(selectedItems);
		}
	}

	/**
	 * Deletes provided strings from set.
	 */
	private void deleteStrings(final Collection<String> strings) {
		if (checkValidity(s -> s.removeAll(strings))) {
			this.listView.getItems().removeAll(strings);
		}
	}

	/**
	 * Deletes provided string from set.
	 */
	private void deleteString(final String string) {
		if (checkValidity(s -> s.remove(string))) {
			this.listView.getItems().remove(string);
		}
	}

	private void replaceString(final String oldValue, final String newValue) {
		if (checkValidity(s -> {
			s.remove(oldValue);
			s.add(newValue);
		})) {
			this.listView.getItems().set(this.listView.getItems().indexOf(oldValue), newValue);
			this.listView.getItems().sort(String::compareTo);
		}
	}

	@Override
	public ObservableValue<StringSet> getObservableParameterValue() {
		return Bindings.createObjectBinding(() -> {
			final StringSet toReturn = new StringSet();
			toReturn.addAll(this.listView.getItems());
			return toReturn;
		}, this.listView.getItems());
	}

	@Override
	public void setValue(final StringSet value) {
		this.listView.getItems().clear();
		this.listView.getItems().addAll(value);
		this.listView.getItems().sort(String::compareTo);
	}

	/**
	 * Triggered by add button. Adds string in field to list view.
	 */
	@FXML
	private void addString() {
		if (!StringUtils.isBlank(this.addTextField.getText()) && !this.listView.getItems().contains(this.addTextField.getText())
				&& checkValidity(s -> s.add(this.addTextField.getText()))) {
			this.listView.getItems().add(this.addTextField.getText());
			this.listView.getItems().sort(String::compareTo);
		}
	}

	private boolean checkValidity(final Consumer<StringSet> modifyAction) {
		return ControllerUtils.checkValidity(generateNewValue(modifyAction), this.spec.getParameterValidator());
	}

	/**
	 * Generates a new {@link StringSet} by generating the current one and applying the provided modification action.
	 */
	private StringSet generateNewValue(final Consumer<StringSet> modifyAction) {
		final StringSet value = new StringSet();
		value.addAll(this.listView.getItems());
		modifyAction.accept(value);
		return value;
	}

	@Override
	public ParameterSpec<StringSet> getSpec() {
		return this.spec;
	}

	@Override
	public void setValidated(final boolean validated) {
		if (validated)
			this.getStyleClass().removeAll("invalidated-control");
		else
			this.getStyleClass().add("invalidated-control");
	}
}
