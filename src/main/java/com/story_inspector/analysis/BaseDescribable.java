package com.story_inspector.analysis;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base implementation of a {@link Describable}.
 *
 * @author mizitch
 *
 */
public abstract class BaseDescribable implements Describable {

	private final String name;
	private final String description;

	/**
	 * Creates a new instance.
	 *
	 * @param name
	 *            The name for the new instance
	 * @param description
	 *            The description for the new instance.
	 */
	public BaseDescribable(final String name, final String description) {
		super();
		Validate.notNull(name);
		Validate.notNull(description);
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

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof BaseDescribable))
			return false;

		final BaseDescribable otherDescribable = (BaseDescribable) other;

		return new EqualsBuilder().append(this.name, otherDescribable.name).append(this.description, otherDescribable.description).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).append(this.description).toHashCode();
	}
}
