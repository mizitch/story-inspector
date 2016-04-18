package com.story_inspector.ioProcessing;

import java.util.Set;

/**
 * Registry for IO-related modules.
 *
 * @author mizitch
 *
 */
public interface IoModuleRegistry {

	/**
	 * Get all {@link DocumentExtractor}s that can handle the provided file type.
	 *
	 * @param fileType
	 *            File type, specified as the extension without a leading "." Not case-sensitive.
	 * @return All {@link DocumentExtractor}s that can handle the provided file type.
	 */
	public Set<DocumentExtractor> getDocumentExtractorsForFileType(String fileType);

	/**
	 * Get all {@link StoryParser}s.
	 *
	 * @return All {@link StoryParser}s.
	 */
	public Set<StoryParser> getStoryParsers();

	/**
	 * Get all supported file types for stories.
	 *
	 * @return All supported file types, specified as the file extension without a leading "."
	 */
	public Set<String> getSupportedStoryFileTypes();

	/**
	 * Get all {@link ReportTranscriber}s that can handle the provided file type.
	 *
	 * @param fileType
	 *            File type, specified as the extension without a leading "." Not case-sensitive.
	 * @return All {@link ReportTranscriber}s that can handle the provided file type.
	 */
	public Set<ReportTranscriber> getReportTranscribersForFileType(String fileType);
}
