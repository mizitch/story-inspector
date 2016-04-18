package com.story_inspector.controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.reports.ReportSectionSpec;
import com.story_inspector.analysis.reports.ReportSpec;
import com.story_inspector.analysis.serialization.AnalysisSerializer;
import com.story_inspector.controllers.misc.AnalyzerSpecificationHelper;
import com.story_inspector.controllers.misc.ControllerUtils;
import com.story_inspector.controllers.saveManagement.FileSystemSaveManager;
import com.story_inspector.controllers.saveManagement.ReportSpecSaveManager;
import com.story_inspector.controllers.saveManagement.SavableContainer;
import com.story_inspector.controllers.saveManagement.SaveManager;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for edit pane within report spec edit page. Handles tab management and interfaces with the {@link SaveManager} for {@link ReportSpec}s.
 *
 * @author mizitch
 *
 */
@Component
public class ReportSpecEditPaneController implements SavableContainer<ReportSpec> {

	/**
	 * Binding for {@link ReportSpec} that generates one based on the values present in the name field, description text area and current section
	 * tabs. Triggers updates when those UI elements change.
	 *
	 * @author mizitch
	 *
	 */
	private class ObservableReportSpec extends ObjectBinding<ReportSpec> {
		private final Set<Observable> dependencies = new HashSet<Observable>();

		private ObservableReportSpec() {
			this.dependencies.add(ReportSpecEditPaneController.this.nameTextField.textProperty());
			this.dependencies.add(ReportSpecEditPaneController.this.descriptionTextArea.textProperty());
			this.dependencies.add(ReportSpecEditPaneController.this.reportSectionsTabPane.getTabs());
			this.dependencies.addAll(ReportSpecEditPaneController.this.reportSectionsTabPane.getTabs().stream()
					.map(this::extractObservableSectionSpec).collect(Collectors.toList()));
			final Observable[] dependenciesArray = new Observable[this.dependencies.size()];
			this.dependencies.toArray(dependenciesArray);
			bind(dependenciesArray);
		}

		@Override
		protected ReportSpec computeValue() {
			final List<ReportSectionSpec> sectionSpecs = ReportSpecEditPaneController.this.reportSectionsTabPane.getTabs().stream()
					.map(this::extractReportSectionSpec).collect(Collectors.toList());

			return new ReportSpec(ReportSpecEditPaneController.this.nameTextField.getText(),
					ReportSpecEditPaneController.this.descriptionTextArea.getText(), sectionSpecs);
		}

		@Override
		public void dispose() {
			final Observable[] dependenciesArray = new Observable[this.dependencies.size()];
			this.dependencies.toArray(dependenciesArray);
			super.unbind(dependenciesArray);
		}

		private void bindNewTab(final Tab t) {
			final ObservableValue<ReportSectionSpec> observableSectionSpec = extractObservableSectionSpec(t);
			this.dependencies.add(observableSectionSpec);
			super.bind(observableSectionSpec);
		}

		private void unbindTab(final Tab t) {
			final ObservableValue<ReportSectionSpec> observableSectionSpec = extractObservableSectionSpec(t);
			this.dependencies.remove(observableSectionSpec);
			super.unbind(observableSectionSpec);
		}

		private ReportSectionSpec extractReportSectionSpec(final Tab t) {
			return ((ReportSectionTab) t).getReportSectionSpec();
		}

		private ObservableValue<ReportSectionSpec> extractObservableSectionSpec(final Tab t) {
			return ((ReportSectionTab) t).observableSectionSpec();
		}
	}

	private ObservableReportSpec reportSpecObservableValue;

	private FileSystemSaveManager<ReportSpec> saveManager;

	@FXML
	private TabPane reportSectionsTabPane;

	@FXML
	private TextField nameTextField;

	@FXML
	private TextArea descriptionTextArea;

	@FXML
	private Button selectDocumentButton;

	@FXML
	private Button executeButton;

	@FXML
	private Label documentSelectedLabel;

	@Autowired
	private AnalyzerSpecificationHelper analyzerSpecPaneHelper;

	@Autowired
	private AnalysisSerializer analysisSerializer;

	@Autowired
	private AnalyzerPaneController analyzerPaneController;

	/**
	 * Do initialization after fx components have been generated.
	 */
	@FXML
	private void initialize() {
		this.reportSpecObservableValue = new ObservableReportSpec();

		// If we detect the user deleted a tab, then make our observable report spec no longer depend on that tab
		this.reportSectionsTabPane.getTabs().addListener((final Change<? extends Tab> c) -> {
			while (c.next()) {
				if (c.wasRemoved()) {
					for (final Tab t : c.getRemoved()) {
						this.reportSpecObservableValue.unbindTab(t);
					}
				}
			}
		});

		this.saveManager = new ReportSpecSaveManager(this, this.analysisSerializer);

		newEntity();
		this.saveManager.syncSavedEntity();

		ControllerUtils.performWithStage(this.reportSectionsTabPane, stage -> {
			stage.setOnCloseRequest(e -> {
				final boolean safeToProceed = this.saveManager
						.promptSave("Save changes to current report before closing? Otherwise your current changes will be lost.");

				if (!safeToProceed)
					e.consume();
			});
		});
	}

	/**
	 * Returns an observable {@link ReportSpec}. Can attach listeners to get new value when it changes based on changes to UI.
	 *
	 * @return An observable {@link ReportSpec}
	 */
	public ObservableValue<ReportSpec> getObservableReportSpec() {
		return this.reportSpecObservableValue;
	}

	/**
	 * Gets the value of the current {@link ReportSpec} represented by this UI.
	 *
	 * @return
	 */
	public ReportSpec getReportSpec() {
		return this.reportSpecObservableValue.getValue();
	}

	/**
	 * Sets the current {@link ReportSpec} and makes appropriate changes to the UI.
	 *
	 * @param spec
	 */
	public void setReportSpec(final ReportSpec spec) {
		this.nameTextField.setText(spec.getName());
		this.descriptionTextArea.setText(spec.getDescription());
		this.reportSectionsTabPane.getTabs().clear();
		for (final ReportSectionSpec sectionSpec : spec.getSectionSpecs()) {
			final ReportSectionTab tab = new ReportSectionTab(sectionSpec, this.analyzerSpecPaneHelper, this.analyzerPaneController,
					this.analysisSerializer);
			this.reportSectionsTabPane.getTabs().add(tab);
			this.reportSpecObservableValue.bindNewTab(tab);
		}
	}

	/**
	 * Triggered by add tab button. Adds a new tab to the pane (and by extension to this report spec)
	 */
	@FXML
	private void addSection() {
		final ReportSectionSpec newSectionSpec = new ReportSectionSpec(generateNewSectionName(), "A new section.", Collections.emptyList());
		final ReportSectionTab tab = new ReportSectionTab(newSectionSpec, this.analyzerSpecPaneHelper, this.analyzerPaneController,
				this.analysisSerializer);
		this.reportSectionsTabPane.getTabs().add(tab);
		this.reportSpecObservableValue.bindNewTab(tab);
	}

	/**
	 * Returns the current selected tab in the UI.
	 *
	 * @return The current selected tab in the UI.
	 */
	public ReportSectionTab getCurrentTab() {
		return (ReportSectionTab) this.reportSectionsTabPane.getSelectionModel().getSelectedItem();
	}

	/**
	 * Generates a new section name. Guaranteed to be unique.
	 *
	 * @return
	 */
	private String generateNewSectionName() {
		final ReportSpec reportSpec = getReportSpec();
		final String base = "New section";
		final Set<String> sectionNames = reportSpec.getSectionSpecs().stream().map(s -> s.getName()).collect(Collectors.toSet());

		if (!sectionNames.contains(base))
			return base;

		// Since "New section" already exists, checks from "New section 2" through "New section n+1"
		for (int i = 2; i <= sectionNames.size() + 1; ++i) {
			final String name = base + " " + i;
			if (!sectionNames.contains(name))
				return name;
		}
		// If all of these exist, then there must be n+1 unique sections...which there are not
		throw new IllegalStateException("Really shouldn't be possible to reach here...");
	}

	@Override
	public ReportSpec getSavableEntity() {
		return this.reportSpecObservableValue.get();
	}

	@Override
	public void setSavableEntity(final ReportSpec newValue) {
		setReportSpec(newValue);
	}

	/**
	 * Returns the {@link FileSystemSaveManager} linked to this pane.
	 *
	 * @return The {@link FileSystemSaveManager} linked to this pane.
	 */
	public FileSystemSaveManager<ReportSpec> getSaveManager() {
		return this.saveManager;
	}

	@Override
	public void newEntity() {
		final ReportSpec defaultNewReportSpec = new ReportSpec("A report", "A report that currently does nothing",
				Collections.singletonList(new ReportSectionSpec("New section", "A new section", Collections.emptyList())));
		setReportSpec(defaultNewReportSpec);
	}
}
