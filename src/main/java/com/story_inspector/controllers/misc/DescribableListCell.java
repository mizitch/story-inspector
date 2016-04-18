package com.story_inspector.controllers.misc;

import com.story_inspector.analysis.Describable;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

/**
 * Base class for a list cell that holds values of a type that implements {@link Describable}. Sets the text of the {@link ListCell} to the name of
 * the entity and the tooltip to the description.
 *
 * Provides hooks for setting a mouse click handler and a context menu.
 *
 * @author mizitch
 *
 * @param <T>
 *            The type of the value this list cell contains.
 */
public abstract class DescribableListCell<T extends Describable> extends ListCell<T> {
	@Override
	protected void updateItem(final T item, final boolean empty) {
		super.updateItem(item, empty);

		if (empty || item == null) {
			setText("");
			setTooltip(null);
			setGraphic(null);
			setContextMenu(null);
			setOnMouseClicked(null);
		} else {
			setText(item != null ? item.getName() : "");

			if (getTooltip() == null) {
				setTooltip(new Tooltip());
			}
			getTooltip().setText(item.getDescription());

			final ContextMenu contextMenu = generateContextMenu(item);
			if (contextMenu != null)
				setContextMenu(contextMenu);

			final EventHandler<? super MouseEvent> mouseClickedHandler = getMouseClickedHandler(item);
			if (mouseClickedHandler != null)
				setOnMouseClicked(mouseClickedHandler);
		}
	}

	/**
	 * Generate the context menu to be used for this list cell.
	 *
	 * @param item
	 *            The value of this list cell
	 * @return The context menu for this list cell.
	 */
	protected abstract ContextMenu generateContextMenu(T item);

	/**
	 * Generate the mouse click handler for this list cell.
	 * 
	 * @param item
	 *            The value of this list cell
	 * @return The mouse click handler for this list cell.
	 */
	protected abstract EventHandler<? super MouseEvent> getMouseClickedHandler(T item);

}
