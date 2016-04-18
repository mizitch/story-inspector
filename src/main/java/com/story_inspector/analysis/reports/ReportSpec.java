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
 * Specification for generating a particular report. Contains a list of {@link ReportSectionSpec}s which contain a list of {@link Analyzer}s.
 *
 * This class is designed to be immutable.
 *
 * @author mizitch
 *
 */
public class ReportSpec extends BaseDescribable {
	private final List<ReportSectionSpec> sectionSpecs;
	private transient final int numAnalyzers;

	/**
	 * Creates a new instance.
	 *
	 * @param name
	 *            The name of this report spec.
	 * @param description
	 *            The description of this report spec.
	 * @param sectionSpecs
	 *            The specifications for each section of the report.
	 */
	@JsonCreator
	public ReportSpec(@JsonProperty("name") final String name, @JsonProperty("description") final String description,
			@JsonProperty("sectionSpecs") final List<ReportSectionSpec> sectionSpecs) {
		super(name, description);
		Validate.noNullElements(sectionSpecs);
		this.sectionSpecs = new ArrayList<ReportSectionSpec>(sectionSpecs);
		this.numAnalyzers = this.sectionSpecs.stream().map(ts -> ts.getAnalyzers().size()).mapToInt(Integer::intValue).sum();
	}

	/**
	 * Returns the specifications for the sections of the generated {@link Report}.
	 *
	 * @return The specifications for the sections of the generated {@link Report}.
	 */
	public List<ReportSectionSpec> getSectionSpecs() {
		return this.sectionSpecs;
	}

	/**
	 * Returns the total number of {@link Analyzer}s in this {@link ReportSpec}.
	 *
	 * @return
	 */
	public int getNumAnalyzers() {
		return this.numAnalyzers;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ReportSpec))
			return false;

		final ReportSpec otherSpec = (ReportSpec) other;

		return new EqualsBuilder().appendSuper(super.equals(otherSpec)).append(this.sectionSpecs, otherSpec.sectionSpecs).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.sectionSpecs).toHashCode();
	}
}
