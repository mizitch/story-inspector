package com.story_inspector.ioProcessing.docx;

import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.story_inspector.ioProcessing.ExtractedDocument;

class DocXExtractedDocument implements ExtractedDocument {

	private final List<ExtractedParagraph> paragraphs;
	private final WordprocessingMLPackage documentPackage;

	DocXExtractedDocument(final WordprocessingMLPackage documentPackage, final List<ExtractedParagraph> paragraphs) {
		super();
		this.documentPackage = documentPackage;
		this.paragraphs = paragraphs;
	}

	@Override
	public List<ExtractedParagraph> getParagraphs() {
		return this.paragraphs;
	}

	WordprocessingMLPackage getDocumentPackage() {
		return this.documentPackage;
	}
}
