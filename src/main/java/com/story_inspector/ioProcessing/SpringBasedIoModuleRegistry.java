package com.story_inspector.ioProcessing;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link IoModuleRegistry} that retrieves IO modules from spring context.
 * 
 * @author mizitch
 *
 */
@Component
class SpringBasedIoModuleRegistry implements IoModuleRegistry, ApplicationContextAware {
	private Set<String> supportedFileTypes;

	private Map<String, Set<DocumentExtractor>> extractorRegistry = null;
	private Map<String, Set<ReportTranscriber>> transcriberRegistry = null;
	private Set<StoryParser> parserRegistry = null;

	@Override
	public Set<String> getSupportedStoryFileTypes() {
		return Collections.unmodifiableSet(this.supportedFileTypes);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.extractorRegistry = new HashMap<>();

		final Map<String, DocumentExtractor> extractorBeans = applicationContext.getBeansOfType(DocumentExtractor.class);

		for (final DocumentExtractor extractor : extractorBeans.values()) {
			for (final String fileType : extractor.getSupportedFileTypes()) {
				if (!this.extractorRegistry.containsKey(fileType.toUpperCase()))
					this.extractorRegistry.put(fileType.toUpperCase(), new HashSet<>());
				this.extractorRegistry.get(fileType.toUpperCase()).add(extractor);
			}
		}

		this.transcriberRegistry = new HashMap<>();

		final Map<String, ReportTranscriber> transcriberBeans = applicationContext.getBeansOfType(ReportTranscriber.class);

		for (final ReportTranscriber transcriber : transcriberBeans.values()) {
			for (final String fileType : transcriber.getSupportedFileTypes()) {
				if (!this.transcriberRegistry.containsKey(fileType.toUpperCase()))
					this.transcriberRegistry.put(fileType.toUpperCase(), new HashSet<>());
				this.transcriberRegistry.get(fileType.toUpperCase()).add(transcriber);
			}
		}

		this.parserRegistry = new HashSet<>(applicationContext.getBeansOfType(StoryParser.class).values());
	}

	@Override
	public Set<DocumentExtractor> getDocumentExtractorsForFileType(final String fileType) {
		Validate.notNull(this.extractorRegistry, "Cannot call this method before application context is initialized");
		final Set<DocumentExtractor> result = Collections.unmodifiableSet(this.extractorRegistry.get(fileType.toUpperCase()));
		return result != null ? result : Collections.emptySet();
	}

	@Override
	public Set<ReportTranscriber> getReportTranscribersForFileType(final String fileType) {
		Validate.notNull(this.transcriberRegistry, "Cannot call this method before application context is initialized");
		final Set<ReportTranscriber> result = Collections.unmodifiableSet(this.transcriberRegistry.get(fileType.toUpperCase()));
		return result != null ? result : Collections.emptySet();
	}

	@Override
	public Set<StoryParser> getStoryParsers() {
		return Collections.unmodifiableSet(this.parserRegistry);
	}
}
