package com.story_inspector.controllers.analyzerRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.serialization.AnalysisSerializer;

/**
 * Default implementation of {@link DefaultAnalyzerRegistry}. Contains a list of resource paths to analyzer files bundled within the application.
 * 
 * @author mizitch
 *
 */
@Component
public class ResourceBasedDefaultAnalyzerRegistry implements DefaultAnalyzerRegistry {

	private static final List<String> resources = Arrays.asList("/defaultAnalyzers/mitch_word_search.sia");

	private List<Analyzer<?>> analyzers;

	@Autowired
	private AnalysisSerializer analysisSerializer;

	@PostConstruct
	public void initialize() throws IOException {
		this.analyzers = new ArrayList<>();

		for (final String resource : resources) {
			this.analyzers.add(this.analysisSerializer.readAnalyzer(getClass().getResourceAsStream(resource)));
		}
		this.analyzers.sort((a, b) -> a.getName().compareTo(b.getName()));
	}

	@Override
	public List<Analyzer<?>> getAllDefaultAnalyzers() {
		return Collections.unmodifiableList(this.analyzers);
	}

}
