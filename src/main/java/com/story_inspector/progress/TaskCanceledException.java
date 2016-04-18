package com.story_inspector.progress;

/**
 * Thrown when the user cancels a currently running task.
 * 
 * @author mizitch
 *
 */
public class TaskCanceledException extends Exception {
	private static final long serialVersionUID = 1L;

	public TaskCanceledException() {
		super();
	}

	public TaskCanceledException(final String message) {
		super(message);
	}
}
