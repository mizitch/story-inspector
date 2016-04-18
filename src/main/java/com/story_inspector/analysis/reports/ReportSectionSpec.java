package com.story_inspector.analysis.reports;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.BaseDescribable;

/**
 * Specification for generating a particular report tab. Contains a list of {@link Analyzer}s to execute.
 *
 * This class is designed to be immutable.
 *
 * @author mizitch
 *
 */
public class ReportSectionSpec extends BaseDescribable {
	private final List<Analyzer<?>> analyzers;

	/**
	 * Create a new instance.
	 *
	 * @param name
	 *            The name of the spec.
	 * @param description
	 *            The description of the spec.
	 * @param analyzers
	 *            The list of analyzers to execute as a part of this tab.
	 */
	@JsonCreator
	public ReportSectionSpec(@JsonProperty("name") final String name, @JsonProperty("description") final String description,
			@JsonProperty("analyzers") final List<Analyzer<?>> analyzers) {
		super(name, description);
		this.analyzers = new ArrayList<Analyzer<?>>(analyzers);

		Validate.noNullElements(analyzers);
	}

	/**
	 * Returns the list of analyzers to execute as a part of this tab.
	 *
	 * @return The list of analyzers to execute as a part of this tab.
	 */
	public List<Analyzer<?>> getAnalyzers() {
		return this.analyzers;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ReportSectionSpec))
			return false;

		final ReportSectionSpec otherSectionSpec = (ReportSectionSpec) other;

		return new EqualsBuilder().appendSuper(super.equals(otherSectionSpec)).append(this.analyzers, otherSectionSpec.analyzers).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.analyzers).toHashCode();
	}
}
