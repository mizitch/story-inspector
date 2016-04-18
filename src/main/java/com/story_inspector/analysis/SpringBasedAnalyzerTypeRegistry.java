package com.story_inspector.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * {@link AnalyzerTypeRegistry} that uses spring to manage {@link AnalyzerType} singleton instances.
 *
 * @author mizitch
 *
 */
@Component
public class SpringBasedAnalyzerTypeRegistry implements AnalyzerTypeRegistry, ApplicationContextAware {
	/**
	 * Analyzer types by id and version
	 */
	private Map<String, SortedMap<Integer, AnalyzerType<?>>> analyzerTypeRegistry;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.analyzerTypeRegistry = new HashMap<>();
		for (final AnalyzerType<?> analyzerType : applicationContext.getBeansOfType(AnalyzerType.class).values()) {
			if (!this.analyzerTypeRegistry.containsKey(analyzerType.getId()))
				this.analyzerTypeRegistry.put(analyzerType.getId(), new TreeMap<>());

			this.analyzerTypeRegistry.get(analyzerType.getId()).put(analyzerType.getVersion(), analyzerType);
		}
	}

	@Override
	public Set<AnalyzerType<?>> getCurrentAnalyzerTypes() {
		// Get latest version of each analyzer type
		return this.analyzerTypeRegistry.values().stream().map(m -> m.get(m.lastKey())).collect(Collectors.toSet());
	}

	@Override
	public AnalyzerType<?> getAnalyzerTypeById(final String analyzerTypeId) {
		final SortedMap<Integer, AnalyzerType<?>> analyzerTypesWithId = this.analyzerTypeRegistry.get(analyzerTypeId);

		if (analyzerTypesWithId != null)
			return analyzerTypesWithId.get(analyzerTypesWithId.lastKey());
		else
			return null;
	}

	@Override
	public AnalyzerType<?> getAnalyzerTypeByIdAndVersion(final String analyzerTypeId, final int analyzerTypeVersion) {
		final SortedMap<Integer, AnalyzerType<?>> analyzerTypesWithId = this.analyzerTypeRegistry.get(analyzerTypeId);

		if (analyzerTypesWithId != null)
			return analyzerTypesWithId.get(analyzerTypeVersion);
		else
			return null;

	}

}
