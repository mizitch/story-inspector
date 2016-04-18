package com.story_inspector.analysis.summary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.Validate;

import com.story_inspector.story.Story;
import com.story_inspector.story.TextRange;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.image.WritableImage;
import javafx.util.StringConverter;

/**
 * {@link AnalyzerSummaryComponent} that creates a "heat map" that visually represents where and how often a given entity is encountered throughout a
 * {@link Story}.
 *
 * @author mizitch
 *
 */
public class HeatMapSummaryComponent implements AnalyzerSummaryComponent {

	private static final int numDivisions = 20;

	private final Collection<TextRange> ranges;
	private final Story story;
	private final XYChart<Number, Number> chart;

	public HeatMapSummaryComponent(final Story story, final Collection<TextRange> ranges) {
		this.ranges = new ArrayList<>(ranges);
		this.story = story;

		Validate.noNullElements(ranges);
		Validate.notNull(story);

		this.chart = createChart();
	}

	@Override
	public void write(final ReportSummaryWriter writer) {

		// Snapshots must be taken on the application thread, so tell the application thread to take care of this when it has a chance...
		final Task<WritableImage> getImageSnapshotTask = new Task<WritableImage>() {

			@Override
			protected WritableImage call() throws Exception {
				new Scene(HeatMapSummaryComponent.this.chart, -1, -1);
				return HeatMapSummaryComponent.this.chart.snapshot(new SnapshotParameters(), null);
			}
		};
		Platform.runLater(getImageSnapshotTask);

		// Wait for the application thread to do it's stuff, then write the image
		try {
			final WritableImage image = getImageSnapshotTask.get();
			writer.writeImage(SwingFXUtils.fromFXImage(image, null));
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private XYChart<Number, Number> createChart() {
		final XYChart.Series<Number, Number> series = new XYChart.Series<>();

		final double divisionWidth = (this.story.getRange().getLength() * 1.0) / (numDivisions * 1.0);

		for (int i = 0; i < numDivisions; ++i) {
			final TextRange range = new TextRange((int) Math.round(i * divisionWidth), (int) Math.round((i + 1) * divisionWidth));
			final double count = getNumOverlaps(range, this.ranges);
			series.getData().add(new Data<Number, Number>(range.getStartIndex(), count));
		}

		final StringConverter<Number> tickLabelFormatter = new StringConverter<Number>() {

			@Override
			public String toString(final Number n) {
				return String.valueOf((int) Math.round(n.doubleValue()));
			}

			@Override
			public Number fromString(final String string) {
				return Double.valueOf(string);
			}
		};

		final NumberAxis xAxis = new NumberAxis(0, this.story.getRange().getLength() - divisionWidth, divisionWidth);
		xAxis.setLabel("Text Position");
		xAxis.setTickLabelsVisible(true);
		xAxis.setTickLabelFormatter(tickLabelFormatter);
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setTickUnit(1);
		yAxis.setLabel("# Found");
		yAxis.setTickLabelsVisible(true);
		yAxis.setTickLabelFormatter(tickLabelFormatter);

		final AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
		chart.setAnimated(false);
		chart.getData().add(series);
		chart.setLegendVisible(false);
		chart.setPrefWidth(600);
		chart.setPrefHeight(400);
		chart.setTitle("Heat Map");

		return chart;
	}

	private double getNumOverlaps(final TextRange range, final Collection<TextRange> otherRanges) {
		double count = 0;
		for (final TextRange otherRange : otherRanges) {
			if (range.contains(otherRange) || otherRange.contains(range)) {
				++count;
			} else if (range.intersects(otherRange)) {
				if (range.getStartIndex() < otherRange.getStartIndex()) {
					count += (range.getEndIndex() - otherRange.getStartIndex()) / (1.0 * otherRange.getLength());
				} else {
					count += (otherRange.getEndIndex() - range.getStartIndex()) / (1.0 * otherRange.getLength());
				}
			}
		}
		return count;
	}

}
