package com.story_inspector.analysis.summary;

import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Represents an entity that can write report summaries. Different file formats (or other output types) would have different implementations.
 *
 * @author mizitch
 *
 */
public interface ReportSummaryWriter {

	/**
	 * Write a heading for the report summary.
	 *
	 * @param heading
	 *            The text of the heading.
	 * @param headingLevel
	 *            The level of the heading. The higher the value, the less prominent the heading. Zero represents the title of the report summary.
	 */
	public void writeHeading(String heading, int headingLevel);

	/**
	 * Write basic text into the report summary.
	 *
	 * @param text
	 *            The text to write.
	 */
	public void writeText(String text);

	/**
	 * Write an ordered list of key-value pairs into the report summary.
	 *
	 * @param keyValuePairs
	 *            The pairs to write.
	 */
	public void writeKeyValuePairs(List<Pair<String, String>> keyValuePairs);

	/**
	 * Write an image into the report summary.
	 *
	 * @param image
	 *            An image to write.
	 */
	public void writeImage(BufferedImage image);

	/**
	 * Add a page break to the report summary.
	 */
	public void addPageBreak();

	/**
	 * End the report summary. Should be called after the summary is finished. After this is called, further operations on this writer will throw
	 * exceptions.
	 */
	public void endReportSummary();
}
