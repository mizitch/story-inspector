package com.story_inspector.controllers.analyzerRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.serialization.AnalysisSerializer;

import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Implementation of {@link CustomAnalyzerRegistry} that stores custom analyzers in a file directory.
 *
 * @author mizitch
 *
 */
@Component
public class DirectoryBasedCustomAnalyzerRegistry implements CustomAnalyzerRegistry {
	private static final Logger log = LoggerFactory.getLogger(DirectoryBasedCustomAnalyzerRegistry.class);

	// TODO: set this based on installer, probably defaulting to something under user's documents folder
	private final File directory = new File("target/custom_analyzers/");

	@Autowired
	private AnalysisSerializer analysisSerializer;

	private final ObservableMap<String, Analyzer<?>> customAnalyzersMap = FXCollections.observableHashMap();

	@PostConstruct
	private void initialize() {
		// TODO: probably remove this when this is set up via installation
		if (!this.directory.exists()) {
			this.directory.mkdirs();
		}
		refreshMap();
	}

	@Override
	public ObservableMap<String, Analyzer<?>> getAllCustomAnalyzers() {
		return new ReadOnlyMapWrapper<>(this.customAnalyzersMap);
	}

	@Override
	public void deleteCustomAnalyzer(final String analyzerId) {
		final boolean success = generateFileForId(analyzerId).delete();
		if (!success)
			throw new RuntimeException("Failed to delete file " + generateFileForId(analyzerId));
		refreshMap();
	}

	@Override
	public void addCustomAnalyzer(final Analyzer<?> analyzer) {
		final String id = generateIdForAnalyzer(analyzer);

		final File file = generateFileForId(id);
		try {
			this.analysisSerializer.writeAnalyzer(analyzer, new FileOutputStream(file));
			refreshMap();
		} catch (final IOException e) {
			log.error("Could not write custom analyzer to file: " + file, e);
			throw new RuntimeException("Could not write custom analyzer to file: " + file, e);
		}
	}

	@Override
	public void setCustomAnalyzer(final String id, final Analyzer<?> newAnalyzer) {
		final File file = generateFileForId(id);
		try {
			this.analysisSerializer.writeAnalyzer(newAnalyzer, new FileOutputStream(file));
			refreshMap();
		} catch (final IOException e) {
			log.error("Could not write custom analyzer to file: " + file, e);
			throw new RuntimeException("Could not write custom analyzer to file: " + file, e);
		}
	}

	/**
	 * Sets the map contents based on the directory. Called whenever a change occurs to custom analyzer registry.
	 */
	private void refreshMap() {
		final File[] analyzerFiles = this.directory.listFiles((d, n) -> n.toLowerCase().endsWith(".sia"));
		final Map<String, Analyzer<?>> currentAnalyzers = Arrays.stream(analyzerFiles)
				.collect(Collectors.toMap(f -> f.getName(), f -> createAnalyzerFromFile(f)));
		this.customAnalyzersMap.clear();
		this.customAnalyzersMap.putAll(currentAnalyzers);
	}

	/**
	 * Generates a file for the provided analyzer id.
	 */
	private File generateFileForId(final String id) {
		return new File(this.directory.getPath() + File.separator + id);
	}

	/**
	 * Generates an id for the provided analyzer. First converts the name to something file name friendly, then adds numbers to the end of the name if
	 * that file already exists. After 10,000 attempts to increment the number it throws an exception.
	 */
	private String generateIdForAnalyzer(final Analyzer<?> analyzer) {
		final String baseName = analyzer.getName().chars().mapToObj(c -> (char) c) // convert name to something more file-friendly
				.filter(c -> Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '_') // discard all punctuation, symbols, etc
				.map(c -> Character.isWhitespace(c) ? '_' : c) // convert whitespace to underscores
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
		for (int i = 0; i < 100000; ++i) {
			final String id = idFromBaseName(baseName, i);
			final File attemptedFile = generateFileForId(id);
			if (!attemptedFile.exists())
				return id;
		}
		log.error("Could not generate id for analyzer with name " + analyzer.getName());
		throw new RuntimeException("Could not generate id for analyzer with name " + analyzer.getName());
	}

	private String idFromBaseName(final String baseName, final int i) {
		if (i == 0)
			return baseName + ".sia";
		return baseName + "_" + i + ".sia";
	}

	private <T extends AnalyzerType<T>> Analyzer<T> createAnalyzerFromFile(final File f) {
		try {
			return this.analysisSerializer.readAnalyzer(new FileInputStream(f));
		} catch (final IOException e) {
			log.error("Could not load custom analyzer from file: " + f, e);
			return null;
		}
	}
}
