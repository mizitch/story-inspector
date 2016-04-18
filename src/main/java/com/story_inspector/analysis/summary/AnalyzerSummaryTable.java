package com.story_inspector.analysis.summary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;

//TODO: finish this class
/**
 *
 * @author mizitch
 *
 */
public class AnalyzerSummaryTable implements AnalyzerSummaryComponent {

	private final List<String> rowHeaders;
	private final List<String> columnHeaders;
	private final String[][] data;

	public AnalyzerSummaryTable(final List<String> rowHeaders, final List<String> columnHeaders, final String[][] data) {
		super();
		this.rowHeaders = rowHeaders;
		this.columnHeaders = columnHeaders;
		this.data = data;

		validate();
	}

	private void validate() {
		Validate.notEmpty(this.data);
		Validate.isTrue(Arrays.stream(this.data).map(r -> r.length).distinct().count() == 1, "Data 2D array cannot be jagged");
		if (this.rowHeaders != null) {
			Validate.isTrue(this.data.length == this.rowHeaders.size(),
					String.format("Number of row headers: %d must match number of rows: %d in data", this.rowHeaders.size(), this.data.length));
		}
		if (this.columnHeaders != null) {
			Validate.isTrue(this.data[0].length == this.columnHeaders.size(), String
					.format("Number of column headers: %d must match number of columns: %d in data", this.columnHeaders.size(), this.data[0].length));
		}
	}

	public List<String> getRowHeaders() {
		return Collections.unmodifiableList(this.rowHeaders);
	}

	public List<String> getColumnHeaders() {
		return Collections.unmodifiableList(this.columnHeaders);
	}

	public String[][] getData() {
		return Arrays.stream(this.data).map(row -> Arrays.copyOf(row, row.length)).toArray(length -> new String[length][]);
	}

	@Override
	public void write(final ReportSummaryWriter writer) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not written yet");
	}

}
