package com.story_inspector.analysis.summary;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * {@link AnalyzerSummaryComponent} that represents an ordered list of key-value pairs.
 *
 * @author mizitch
 *
 */
public class KeyValueListSummaryComponent implements AnalyzerSummaryComponent {
	private final List<ImmutablePair<String, String>> keyValuePairs;

	/**
	 * Create a new instance.
	 *
	 * @param keyValuePairs
	 *            The list of key-value pairs.
	 */
	public KeyValueListSummaryComponent(final List<ImmutablePair<String, String>> keyValuePairs) {
		this.keyValuePairs = new ArrayList<ImmutablePair<String, String>>(keyValuePairs);
	}

	@Override
	public void write(final ReportSummaryWriter writer) {
		writer.writeKeyValuePairs(new ArrayList<>(this.keyValuePairs));
	}

}
