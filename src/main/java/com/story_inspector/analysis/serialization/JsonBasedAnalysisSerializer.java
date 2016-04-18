package com.story_inspector.analysis.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.story_inspector.analysis.Analyzer;
import com.story_inspector.analysis.AnalyzerType;
import com.story_inspector.analysis.reports.ReportSpec;

/**
 * Implementation of {@link AnalysisSerializer} that uses Jackson to serialize and deserialize entities into JSON.
 * 
 * @author mizitch
 *
 */
@Component
public class JsonBasedAnalysisSerializer implements AnalysisSerializer {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public void writeAnalyzer(final Analyzer<?> analyzer, final OutputStream outputStream) throws IOException {
		this.mapper.writeValue(outputStream, analyzer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AnalyzerType<T>> Analyzer<T> readAnalyzer(final InputStream inputStream) throws IOException {
		return this.mapper.readValue(inputStream, Analyzer.class);
	}

	@Override
	public void writeAnalyzers(final List<Analyzer<?>> analyzers, final OutputStream outputStream) throws IOException {
		this.mapper.writeValue(outputStream, analyzers);
	}

	@Override
	public List<Analyzer<?>> readAnalyzers(final InputStream inputStream) throws IOException {
		return this.mapper.readValue(inputStream, new TypeReference<List<Analyzer<?>>>() {
		});
	}

	@Override
	public void writeReportSpec(final ReportSpec spec, final OutputStream outputStream) throws IOException {
		this.mapper.writeValue(outputStream, spec);

	}

	@Override
	public ReportSpec readReportSpec(final InputStream inputStream) throws IOException {
		return this.mapper.readValue(inputStream, ReportSpec.class);
	}

}
