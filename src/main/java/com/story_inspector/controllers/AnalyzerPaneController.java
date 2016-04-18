package com.story_inspector.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.AnalyzerTypeRegistry;
import com.story_inspector.analysis.Describable;
import com.story_inspector.analysis.serialization.AnalysisSerializer;
import com.story_inspector.controllers.analyzerRegistry.CustomAnalyzerRegistry;
import com.story_inspector.controllers.analyzerRegistry.DefaultAnalyzerRegistry;
import com.story_inspector.controllers.misc.AnalyzerSpecificationHelper;
import com.story_inspector.controllers.misc.ControllerUtils;
import com.story_inspector.controllers.misc.DescribableListCell;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Controller for panel with analyzer types, default analyzers and custom analyzers lists.
 *
 * Supports these list views, dragging and dropping default or custom analyzers into a report, etc.
 *
 * @author mizitch
 *
 */
@Component
public class AnalyzerPaneController {
	private static final Logger log = LoggerFactory.getLogger(AnalyzerPaneController.class);

	/**
	 *
	 * @author mizitch
	 *
	 * @param <T>
	 */
	private static class CustomAnalyzer<T extends AnalyzerType<T>> implements Describable {
		private final String id;
		private final Analyzer<T> analyzer;

		private CustomAnalyzer(final String id, final Analyzer<T> analyzer) {
			Validate.notBlank(id);
			Validate.notNull(analyzer);

			this.id = id;
			this.analyzer = analyzer;
		}

		public String getId() {
			return this.id;
		}

		public Analyzer<T> getAnalyzer() {
			return this.analyzer;
		}

		@Override
		public String getName() {
			return this.analyzer.getName();
		}

		@Override
		public String getDescription() {
			return this.analyzer.getDescription();
		}
	}

	/**
	 * List cell class for {@link AnalyzerType} list view.
	 *
	 * @author mizitch
	 *
	 * @param <T>
	 */
	private class AnalyzerTypeListCell extends DescribableListCell<AnalyzerType<?>> {

		@Override
		protected ContextMenu generateContextMenu(final AnalyzerType<?> analyzerType) {
			final ContextMenu menu = new ContextMenu();

			final MenuItem newAnalyzer = new MenuItem("New Analyzer");
			newAnalyzer.setOnAction(e -> newAnalyzer(analyzerType));

			menu.getItems().add(newAnalyzer);
			return menu;
		}

		@Override
		protected EventHandler<? super MouseEvent> getMouseClickedHandler(final AnalyzerType<?> item) {
			return ControllerUtils.generateDoubleClickHandler(e -> newAnalyzer(item));
		}
	}

	/**
	 * List cell class for default analyzer list view.
	 *
	 * @author mizitch
	 *
	 */
	private class DefaultAnalyzerListCell extends DescribableListCell<Analyzer<?>> {

		@Override
		protected ContextMenu generateContextMenu(final Analyzer<?> analyzer) {
			final ContextMenu menu = new ContextMenu();

			final MenuItem clone = new MenuItem("Clone");
			clone.setOnAction(e -> cloneAnalyzer(analyzer));

			final MenuItem add = new MenuItem("Add To Report");
			add.setOnAction(e -> addAnalyzers(AnalyzerPaneController.this.defaultAnalyzersListView.getSelectionModel().getSelectedItems()));

			menu.getItems().add(clone);
			menu.getItems().add(add);
			return menu;
		}

		@Override
		protected EventHandler<? super MouseEvent> getMouseClickedHandler(final Analyzer<?> item) {
			return ControllerUtils.generateDoubleClickHandler(e -> cloneAnalyzer(item));
		}
	}

	/**
	 * List cell class for custom analyzer list view.
	 *
	 * @author mizitch
	 *
	 */
	private class CustomAnalyzerListCell extends DescribableListCell<CustomAnalyzer<?>> {

		@Override
		protected ContextMenu generateContextMenu(final CustomAnalyzer<?> customAnalyzer) {
			final ContextMenu menu = new ContextMenu();

			final MenuItem edit = new MenuItem("Edit");
			edit.setOnAction(e -> editAnalyzer(customAnalyzer));
			final MenuItem clone = new MenuItem("Clone");
			clone.setOnAction(e -> cloneAnalyzer(customAnalyzer.getAnalyzer()));
			final MenuItem add = new MenuItem("Add To Report");
			add.setOnAction(e -> addAnalyzers(AnalyzerPaneController.this.customAnalyzersListView.getSelectionModel().getSelectedItems().stream()
					.map(c -> c.getAnalyzer()).collect(Collectors.toList())));
			final MenuItem delete = new MenuItem("Delete");
			delete.setOnAction(e -> deleteAnalyzers(AnalyzerPaneController.this.customAnalyzersListView.getSelectionModel().getSelectedItems()));

			menu.getItems().add(edit);
			menu.getItems().add(add);
			menu.getItems().add(clone);
			menu.getItems().add(delete);
			return menu;
		}

		@Override
		protected EventHandler<? super MouseEvent> getMouseClickedHandler(final CustomAnalyzer<?> item) {
			return ControllerUtils.generateDoubleClickHandler(e -> editAnalyzer(item));
		}
	}

	@Autowired
	private AnalyzerSpecificationHelper analyzerSpecPaneHelper;

	@Autowired
	private CustomAnalyzerRegistry customAnalyzerRegistry;

	@Autowired
	private DefaultAnalyzerRegistry defaultAnalyzerRegistry;

	@Autowired
	private AnalyzerTypeRegistry analyzerTypeRegistry;

	@Autowired
	private ReportSpecEditPaneController reportPaneController;

	@Autowired
	private AnalysisSerializer analysisSerializer;

	@FXML
	private ListView<AnalyzerType<?>> analyzerTypesListView;

	@FXML
	private ListView<Analyzer<?>> defaultAnalyzersListView;

	@FXML
	private ListView<CustomAnalyzer<?>> customAnalyzersListView;

	@FXML
	private Button importAnalyzerButton;

	@FXML
	private Button exportAnalyzerButton;

	private ObservableValue<ObservableList<CustomAnalyzer<?>>> customAnalyzerList;

	/**
	 * Creates a binding that generates the custom analyzers list for the custom analyzers list view based on the custom analyzers registry. So
	 * changes to the registry are immediately reflected in the list view. Executed as soon as the custom analyzer registry is injected by spring.
	 */
	@PostConstruct
	private void initializeCustomAnalyzerList() {
		this.customAnalyzerList = Bindings.createObjectBinding(
				() -> FXCollections.observableArrayList(this.customAnalyzerRegistry.getAllCustomAnalyzers().entrySet().stream()
						.map(entry -> createCustomAnalyzer(entry.getKey(), entry.getValue())) // convert to CustomAnalyzer class
						.sorted((a, b) -> a.getName().toLowerCase().compareTo(b.getName().toLowerCase())) // sort!
						.collect(Collectors.toList())), // collect as list
				this.customAnalyzerRegistry.getAllCustomAnalyzers()); // depend on the map of custom analyzers
	}

	private <T extends AnalyzerType<T>> CustomAnalyzer<T> createCustomAnalyzer(final String id, final Analyzer<T> analyzer) {
		return new CustomAnalyzer<T>(id, analyzer);
	}

	/**
	 * Sets up cell factories for list views, loads items into list views, sets selection model, adds drag and drop event hooks and delete key event
	 * hook for custom analyzers list view.
	 */
	@FXML
	private void initialize() {
		log.debug("Initializing controls");
		// Setup for analyzer types list view
		this.analyzerTypesListView.setCellFactory(lv -> new AnalyzerTypeListCell());
		this.analyzerTypesListView.getItems().addAll(this.analyzerTypeRegistry.getCurrentAnalyzerTypes());
		this.analyzerTypesListView.getItems().sort((a, b) -> a.getName().compareTo(b.getName()));

		// Setup for default analyzers list view
		this.defaultAnalyzersListView.setCellFactory(lv -> new DefaultAnalyzerListCell());
		this.defaultAnalyzersListView.getItems().addAll(this.defaultAnalyzerRegistry.getAllDefaultAnalyzers());
		this.defaultAnalyzersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.defaultAnalyzersListView.setOnDragDetected(e -> {
			final Dragboard db = this.defaultAnalyzersListView.startDragAndDrop(TransferMode.COPY);

			final ClipboardContent content = new ClipboardContent();
			content.putString(ControllerUtils.serializeAnalyzersToString(this.defaultAnalyzersListView.getSelectionModel().getSelectedItems(),
					this.analysisSerializer));

			db.setContent(content);
			e.consume();
		});

		// Setup for custom analyzers list view
		this.customAnalyzersListView.setCellFactory(lv -> new CustomAnalyzerListCell());
		this.customAnalyzersListView.itemsProperty().bind(this.customAnalyzerList);
		this.customAnalyzersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.customAnalyzersListView.setOnDragDetected(e -> {
			final Dragboard db = this.customAnalyzersListView.startDragAndDrop(TransferMode.COPY);

			final ClipboardContent content = new ClipboardContent();
			content.putString(ControllerUtils.serializeAnalyzersToString(this.customAnalyzersListView.getSelectionModel().getSelectedItems().stream()
					.map(c -> c.getAnalyzer()).collect(Collectors.toList()), this.analysisSerializer));

			db.setContent(content);
			e.consume();
		});

		this.customAnalyzersListView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				final List<CustomAnalyzer<?>> selectedItems = this.customAnalyzersListView.getSelectionModel().getSelectedItems();
				if (selectedItems != null) {
					deleteAnalyzers(selectedItems);
				}
			}
		});
		log.debug("Finished initializing controls");
	}

	/**
	 * Adds new custom analyzers to custom analyzer list.
	 *
	 * @param analyzers
	 *            Analyzers to add.
	 */
	public void addCustomAnalyzers(final Collection<Analyzer<?>> analyzers) {
		this.customAnalyzerRegistry.addCustomAnalyzers(analyzers);
	}

	/**
	 * Adds analyzers to current report tab.
	 */
	private void addAnalyzers(final List<Analyzer<?>> analyzers) {
		final ReportSectionTab currentTab = this.reportPaneController.getCurrentTab();
		if (currentTab != null)
			currentTab.addAnalyzers(analyzers);
	}

	/**
	 * Allows user to enter details for new analyzer and saves as custom analyzer if user completes action.
	 *
	 * @param analyzerType
	 *            Analyzer type for new analyzer.
	 */
	private <T extends AnalyzerType<T>> void newAnalyzer(final AnalyzerType<?> analyzerType) {
		@SuppressWarnings("unchecked")
		final Analyzer<?> newAnalyzer = this.analyzerSpecPaneHelper.newAnalyzer((T) analyzerType);
		if (newAnalyzer != null)
			this.customAnalyzerRegistry.addCustomAnalyzer(newAnalyzer);
	}

	/**
	 * Allows user to edit an existing custom analyzer and saves if user completes action.
	 *
	 * @param customAnalyzer
	 *            The custom analyzer to edit.
	 */
	private void editAnalyzer(final CustomAnalyzer<?> customAnalyzer) {
		final Analyzer<?> editedAnalyzer = this.analyzerSpecPaneHelper.editAnalyzer(customAnalyzer.getAnalyzer());
		if (editedAnalyzer != null)
			this.customAnalyzerRegistry.setCustomAnalyzer(customAnalyzer.getId(), editedAnalyzer);
	}

	/**
	 * Allows user to edit a copy of a custom or default analyzer and saves this copy as a new custom analyzer if the user completes the action.
	 *
	 * @param analyzer
	 *            Analyzer to clone.
	 */
	private void cloneAnalyzer(final Analyzer<?> analyzer) {
		final Analyzer<?> clonedAnalyzer = this.analyzerSpecPaneHelper.cloneAnalyzer(analyzer);
		if (clonedAnalyzer != null)
			this.customAnalyzerRegistry.addCustomAnalyzer(clonedAnalyzer);
	}

	/**
	 * Deletes the provided collection of custom analyzers.
	 *
	 * @param analyzers
	 *            Custom analyzers to delete.
	 */
	private void deleteAnalyzers(final Collection<CustomAnalyzer<?>> analyzers) {
		if (analyzers != null) {
			final Alert confirmation = new Alert(AlertType.CONFIRMATION,
					"Are you sure you want to delete these custom analyzers?\nThis cannot be undone.");
			final Optional<ButtonType> result = confirmation.showAndWait();
			if (result.get() == ButtonType.OK) {
				this.customAnalyzerRegistry.deleteCustomAnalyzers(analyzers.stream().map(CustomAnalyzer::getId).collect(Collectors.toList()));
			}
		}
	}
}
