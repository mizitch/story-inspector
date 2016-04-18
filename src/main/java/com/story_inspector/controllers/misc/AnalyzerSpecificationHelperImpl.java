package com.story_inspector.controllers.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerSpec;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.controllers.AnalyzerSpecPaneController;
import com.story_inspector.controllers.analyzerParameters.AnalyzerParameterControlFactory;

/**
 * Default implementation of {@link AnalyzerSpecificationHelper}. Creates a new window using {@link AnalyzerSpecPane} to allow the user to specify
 * {@link Analyzer} details.
 *
 * @author mizitch
 *
 */
@Component
public class AnalyzerSpecificationHelperImpl implements AnalyzerSpecificationHelper {

	@Autowired
	private AnalyzerParameterControlFactory parameterControlFactory;

	@Override
	public <T extends AnalyzerType<T>> Analyzer<T> cloneAnalyzer(final Analyzer<T> existingAnalyzer) {
		final AnalyzerSpec<T> originalSpec = existingAnalyzer.extractAnalyzerSpec();
		final AnalyzerSpec<T> clonedSpec = new AnalyzerSpec<T>("Clone of " + originalSpec.getName(), originalSpec.getDescription(),
				originalSpec.getAnalyzerType(), originalSpec.isCommentRecordingSuppressed(), originalSpec.getAnalyzerParameterValues());
		final AnalyzerSpecPaneController<T> controller = new AnalyzerSpecPaneController<>(clonedSpec, this.parameterControlFactory, true);
		return openAnalyzerPane(controller, "Edit Cloned Analyzer");
	}

	@Override
	public <T extends AnalyzerType<T>> Analyzer<T> editAnalyzer(final Analyzer<T> existingAnalyzer) {
		final AnalyzerSpecPaneController<T> controller = new AnalyzerSpecPaneController<>(existingAnalyzer.extractAnalyzerSpec(),
				this.parameterControlFactory, false);
		return openAnalyzerPane(controller, "Edit Analyzer");
	}

	@Override
	public <T extends AnalyzerType<T>> Analyzer<T> newAnalyzer(final T analyzerType) {
		final AnalyzerSpecPaneController<T> controller = new AnalyzerSpecPaneController<>(analyzerType, this.parameterControlFactory);
		return openAnalyzerPane(controller, "New Analyzer");
	}

	private <T extends AnalyzerType<T>> Analyzer<T> openAnalyzerPane(final AnalyzerSpecPaneController<T> controller, final String title) {
		ControllerUtils.openFxmlInWindowWithController(controller, "/fxml/AnalyzerSpecPane.fxml", title);

		return controller.getResult();
	}
}
