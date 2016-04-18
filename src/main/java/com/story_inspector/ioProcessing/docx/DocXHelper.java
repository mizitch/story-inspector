package com.story_inspector.ioProcessing.docx;

import javax.xml.bind.JAXBElement;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

class DocXHelper {

	private DocXHelper() {
		throw new UnsupportedOperationException("Static method collection class, don't instantiate");
	}

	static String extractText(final P paragraph) {
		final StringBuilder builder = new StringBuilder();
		for (final Object paragraphChild : paragraph.getContent()) {
			if (paragraphChild instanceof R)
				builder.append(extractText((R) paragraphChild));
		}
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	static String extractText(final R run) {
		final StringBuilder builder = new StringBuilder();
		for (final Object runChild : run.getContent()) {
			if (runChild instanceof JAXBElement && ((JAXBElement<?>) runChild).getDeclaredType() == Text.class)
				builder.append(extractText((JAXBElement<Text>) runChild));
		}
		return builder.toString();
	}

	static String extractText(final JAXBElement<Text> textElement) {
		if (textElement.getValue() != null && textElement.getValue().getValue() != null)
			return textElement.getValue().getValue();
		else
			return "";
	}

	static boolean isTextElement(final Object element) {
		return element instanceof JAXBElement && ((JAXBElement<?>) element).getDeclaredType() == Text.class;
	}
}
