package com.story_inspector.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.reports.ReportSectionSpec;
import com.story_inspector.analysis.serialization.AnalysisSerializer;
import com.story_inspector.controllers.misc.AnalyzerSpecificationHelper;
import com.story_inspector.controllers.misc.ControllerUtils;
import com.story_inspector.controllers.misc.DescribableListCell;

import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

/**
 * Control for a report section tab.
 *
 * @author mizitch
 *
 */
@DefaultProperty("children")
public class ReportSectionTab extends Tab {

	/**
	 * ListCell for an {@link Analyzer} in the section. Handles drag and drop.
	 *
	 * @author mizitch
	 *
	 */
	private class AnalyzerCell extends DescribableListCell<Analyzer<?>> {
		private static final String dragCssClassBottom = "drag-hover-list-item-bottom";
		private static final String dragCssClassTop = "drag-hover-list-item-top";

		public AnalyzerCell() {
			super();

			this.setOnDragOver(e -> {
				if (e.getGestureSource() != this && e.getDragboard().hasString()) {
					e.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE);
					updateStyleOnDrag(e);
				}
				e.consume();
			});
			this.setOnDragEntered(e -> {
				if (e.getGestureSource() != this && e.getDragboard().hasString()) {
					updateStyleOnDrag(e);
					e.consume();
				}
			});
			this.setOnDragExited(e -> {
				if (e.getGestureSource() != this && e.getDragboard().hasString()) {
					removeDragStyling();
					e.consume();
				}
			});
			this.setOnDragDropped(e -> {
				if (e.getGestureSource() != this && e.getDragboard().hasString()) {
					final List<Analyzer<?>> analyzers = ControllerUtils.deserializeAnalyzersFromString(e.getDragboard().getString(),
							ReportSectionTab.this.analysisSerializer);
					ReportSectionTab.this.analyzerListView.getItems().addAll(getInsertionIndex(e), analyzers);
					removeDragStyling();
					e.setDropCompleted(true);
					e.consume();
				}
			});
		}

		/**
		 * Based on position within cell determines whether drop location is before or after cell.
		 *
		 * @param e
		 * @return
		 */
		private int getInsertionIndex(final DragEvent e) {
			if (this.getItem() == null) {
				return ReportSectionTab.this.analyzerListView.getItems().size();
			} else if (bottomHalfOfCell(e)) {
				return this.getIndex() + 1;
			} else {
				return this.getIndex();
			}
		}

		/**
		 * Updates style based on drop location.
		 */
		private void updateStyleOnDrag(final DragEvent e) {
			if (bottomHalfOfCell(e)) {
				addStyle(dragCssClassBottom);
				removeStyle(dragCssClassTop);
			} else {
				addStyle(dragCssClassTop);
				removeStyle(dragCssClassBottom);
			}
		}

		/**
		 * Removes all drag styling from cell.
		 */
		private void removeDragStyling() {
			removeStyle(dragCssClassTop);
			removeStyle(dragCssClassBottom);
		}

		/**
		 * Adds CSS style to cell.
		 */
		private void addStyle(final String style) {
			if (!this.getStyleClass().contains(style)) {
				this.getStyleClass().add(style);
			}
		}

		/**
		 * Removes CSS style from cell
		 */
		private void removeStyle(final String style) {
			this.getStyleClass().removeAll(style);
		}

		/**
		 * Whether this drag event occurs in the bottom half of the cell.
		 */
		private boolean bottomHalfOfCell(final DragEvent e) {
			return e.getY() > this.getHeight() / 2;
		}

		@Override
		protected ContextMenu generateContextMenu(final Analyzer<?> analyzer) {
			final ContextMenu menu = new ContextMenu();

			final MenuItem edit = new MenuItem("Edit");
			edit.setOnAction(e -> editAnalyzer(analyzer));

			final MenuItem save = new MenuItem("Save As Custom");
			save.setOnAction(e -> saveAnalyzers(ReportSectionTab.this.analyzerListView.getSelectionModel().getSelectedItems()));

			final MenuItem delete = new MenuItem("Delete");
			delete.setOnAction(e -> deleteAnalyzers(ReportSectionTab.this.analyzerListView.getSelectionModel().getSelectedItems()));

			menu.getItems().add(edit);
			menu.getItems().add(save);
			menu.getItems().add(delete);
			return menu;
		}

		@Override
		protected EventHandler<? super MouseEvent> getMouseClickedHandler(final Analyzer<?> item) {
			return ControllerUtils.generateDoubleClickHandler(e -> editAnalyzer(item));
		}
	}

	@FXML
	private TextArea descriptionTextArea;

	@FXML
	private ListView<Analyzer<?>> analyzerListView;

	private final Label tabLabel = new Label();

	private final TextField tabTextField = new TextField();

	private ObservableValue<ReportSectionSpec> observableSectionSpec;

	private final AnalyzerSpecificationHelper analyzerSpecificationHelper;

	private final AnalyzerPaneController analyzerPaneController;

	private final AnalysisSerializer analysisSerializer;

	private final List<Analyzer<?>> draggedAnalyzers;

	/**
	 * Creates a new instance
	 *
	 * @param sectionSpec
	 *            The initial value of the {@link ReportSectionSpec}
	 * @param analyzerSpecPaneHelper
	 *            Helper for editing {@link Analyzer}s
	 * @param analyzerPaneController
	 *            Controller for {@link Analyzer} pane.
	 * @param analysisSerializer
	 *            Helper for serializing and deserializing {@link Analyzer}s.
	 */
	public ReportSectionTab(final ReportSectionSpec sectionSpec, final AnalyzerSpecificationHelper analyzerSpecificationHelper,
			final AnalyzerPaneController analyzerPaneController, final AnalysisSerializer analysisSerializer) {
		Validate.notNull(sectionSpec);
		Validate.notNull(analyzerSpecificationHelper);
		Validate.notNull(analyzerPaneController);
		Validate.notNull(analysisSerializer);

		this.analyzerSpecificationHelper = analyzerSpecificationHelper;
		this.analyzerPaneController = analyzerPaneController;
		this.analysisSerializer = analysisSerializer;
		this.draggedAnalyzers = new ArrayList<>();

		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ReportTabPane.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}

		initialize(sectionSpec);
	}

	/**
	 * Initialize controls based on initial spec. Also set up drag and drop, event handlers, etc.
	 */
	private void initialize(final ReportSectionSpec initialSpec) {
		this.tabLabel.setText(initialSpec.getName());
		this.tabLabel.setOnMouseClicked(ControllerUtils.generateDoubleClickHandler(e -> rename()));
		this.setGraphic(this.tabLabel);

		this.tabTextField.setPrefWidth(Region.USE_COMPUTED_SIZE);
		this.tabTextField.setOnAction(e -> {
			finishRename();
		});
		this.tabTextField.focusedProperty().addListener((v, o, n) -> {
			if (!n) {
				finishRename();
			}
		});

		this.descriptionTextArea.setText(initialSpec.getDescription());
		this.analyzerListView.getItems().addAll(initialSpec.getAnalyzers());

		this.analyzerListView.setCellFactory(lv -> new AnalyzerCell());
		this.analyzerListView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				final List<Analyzer<?>> selectedItems = this.analyzerListView.getSelectionModel().getSelectedItems();
				if (selectedItems != null) {
					deleteAnalyzers(selectedItems);
				}
			}
		});
		this.analyzerListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.analyzerListView.setOnDragDetected(e -> {
			final Dragboard db = this.analyzerListView.startDragAndDrop(TransferMode.MOVE);

			final ClipboardContent content = new ClipboardContent();
			content.putString(ControllerUtils.serializeAnalyzersToString(this.analyzerListView.getSelectionModel().getSelectedItems(),
					this.analysisSerializer));
			this.draggedAnalyzers.addAll(this.analyzerListView.getSelectionModel().getSelectedItems());

			db.setContent(content);
			e.consume();
		});
		this.analyzerListView.setOnDragDone(e -> {
			if (e.getTransferMode() == TransferMode.MOVE) {
				this.analyzerListView.getItems().removeAll(this.draggedAnalyzers);
				this.draggedAnalyzers.clear();
				e.consume();
			}
		});
		this.analyzerListView.setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				e.acceptTransferModes(TransferMode.COPY);
				e.consume();
			}
		});
		this.analyzerListView.setOnDragDropped(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				final List<Analyzer<?>> analyzers = ControllerUtils.deserializeAnalyzersFromString(e.getDragboard().getString(),
						ReportSectionTab.this.analysisSerializer);
				ReportSectionTab.this.analyzerListView.getItems().addAll(analyzers);
				e.consume();
			}
		});

		this.observableSectionSpec = Bindings.createObjectBinding(
				() -> new ReportSectionSpec(this.tabLabel.getText(), this.descriptionTextArea.getText(), this.analyzerListView.getItems()),
				this.tabLabel.textProperty(), this.descriptionTextArea.textProperty(), this.analyzerListView.getItems());

		this.setContextMenu(createContextMenu());
	}

	private ContextMenu createContextMenu() {
		final MenuItem rename = new MenuItem("Rename");
		rename.setOnAction(e -> rename());

		final MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(e -> delete());

		final ContextMenu menu = new ContextMenu();
		menu.getItems().add(rename);
		menu.getItems().add(delete);
		return menu;
	}

	/**
	 * Delete this section. Triggered by context menu.
	 */
	private void delete() {
		final Alert confirmation = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this section?\nThis cannot be undone.");
		final Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			this.getTabPane().getTabs().remove(this);
		}
	}

	/**
	 * Rename this section. Triggered by context menu or double click. Makes name editable
	 */
	private void rename() {
		this.tabTextField.setText(this.tabLabel.getText());
		this.setGraphic(this.tabTextField);
		this.tabTextField.selectAll();
		this.tabTextField.requestFocus();
	}

	/**
	 * Finishes rename, makes name no longer editable.
	 */
	private void finishRename() {
		this.tabLabel.setText(this.tabTextField.getText());
		this.setGraphic(this.tabLabel);
	}

	/**
	 * Get the observable {@link ReportSectionSpec} that is generated based on the values in the UI controls.
	 *
	 * @return
	 */
	public ObservableValue<ReportSectionSpec> observableSectionSpec() {
		return this.observableSectionSpec;
	}

	/**
	 * Gets the current value of the {@link ReportSectionSpec} represented by this tab.
	 *
	 * @return
	 */
	public ReportSectionSpec getReportSectionSpec() {
		return this.observableSectionSpec.getValue();
	}

	/**
	 * Deletes the analyzers from this section.
	 */
	private void deleteAnalyzers(final Collection<Analyzer<?>> analyzers) {
		final Alert confirmation = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete these analyzers?\nThis cannot be undone.");
		final Optional<ButtonType> result = confirmation.showAndWait();
		if (result.get() == ButtonType.OK) {
			this.analyzerListView.getItems().removeAll(analyzers);
		}
	}

	/**
	 * Allows the user to edit the analyzer.
	 */
	private void editAnalyzer(final Analyzer<?> analyzer) {
		final Analyzer<?> changedAnalyzer = this.analyzerSpecificationHelper.editAnalyzer(analyzer);
		if (changedAnalyzer != null) {
			final int analyzerIndex = this.analyzerListView.getItems().indexOf(analyzer);
			this.analyzerListView.getItems().set(analyzerIndex, changedAnalyzer);
		}
	}

	/**
	 * Saves the provided {@link Analyzer}s to the custom analyzer library.
	 */
	private void saveAnalyzers(final Collection<Analyzer<?>> analyzers) {
		this.analyzerPaneController.addCustomAnalyzers(analyzers);
	}

	/**
	 * Adds the provided {@link Analyzer}s to the end of this section.
	 */
	public void addAnalyzers(final List<Analyzer<?>> analyzers) {
		this.analyzerListView.getItems().addAll(analyzers);
	}

}
