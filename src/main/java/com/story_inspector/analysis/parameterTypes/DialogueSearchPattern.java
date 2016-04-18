package com.story_inspector.analysis.parameterTypes;

import com.story_inspector.analysis.Describable;

/**
 * Whether to search only inside dialogue, only outside dialogue, or everywhere.
 *
 * @author mizitch
 *
 */
public enum DialogueSearchPattern implements Describable {
	ALL_TEXT("All text", "Searches both dialogue and non dialogue text."),
	DIALOGUE_ONLY("Dialogue only", "Only searches dialogue"),
	ALL_BUT_DIALOGUE("All but dialogue", "Searches all text that is not dialogue");

	private final String name;
	private final String description;

	private DialogueSearchPattern(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

}
