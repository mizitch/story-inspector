package com.story_inspector.ioProcessing.docx;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;

import com.story_inspector.analysis.summary.ReportSummaryWriter;

/**
 * Implementation of {@link ReportSummaryWriter} for DOCX files.
 *
 * @author mizitch
 *
 */
public class DocXReportSummaryWriter implements ReportSummaryWriter {

	private static final String SANS_SERIF_FONT_NAME = "Calibri";

	private static final String SERIF_FONT_NAME = "Times New Roman";

	private static final String KEY_CHARACTER_STYLE = "StoryInspectorKey";

	private static final String KEY_VALUE_STYLE = "StoryInspectorKeyValue";

	private static final String TEXT_STYLE = "StoryInspectorText";

	private static final String HEADING2_STYLE = "StoryInspectorHeading2";

	private static final String HEADING1_STYLE = "StoryInspectorHeading1";

	private static final String TITLE_STYLE = "StoryInspectorTitle";

	private final DocXExtractedDocument extractedDocument;

	private final List<P> summaryParagraphs = new ArrayList<>();

	private boolean pageBreakNext = false;

	private int nextUniqueImageId = 1;

	private final ObjectFactory wmlObjectFactory = new ObjectFactory();

	private boolean summaryEnded = false;

	/**
	 * Creates a new instance
	 *
	 * @param extractedDocument
	 *            The original story document.
	 */
	DocXReportSummaryWriter(final DocXExtractedDocument extractedDocument) {
		this.extractedDocument = extractedDocument;
	}

	/**
	 * Add the paragraph to the report summary.
	 */
	private void addParagraph(final P paragraph) {
		if (this.summaryEnded)
			throw new RuntimeException("Summary is ended, cannot add to summary");

		if (this.pageBreakNext) {
			setPageBreakBefore(paragraph);
			this.pageBreakNext = false;
		}
		this.summaryParagraphs.add(paragraph);
	}

	@Override
	public void writeHeading(final String heading, final int headingLevel) {
		Validate.isTrue(headingLevel >= 0, "heading level cannot be negative");

		final String styleId;
		switch (headingLevel) {
		case 0:
			styleId = TITLE_STYLE;
			break;
		case 1:
			styleId = HEADING1_STYLE;
			break;
		default:
			styleId = HEADING2_STYLE;
			break;
		}
		addParagraph(createParagraph(styleId, heading));
	}

	@Override
	public void writeText(final String text) {
		addParagraph(createParagraph(TEXT_STYLE, text));
	}

	@Override
	public void writeKeyValuePairs(final List<Pair<String, String>> keyValuePairs) {
		for (final Pair<String, String> pair : keyValuePairs) {
			addParagraph(createParagraph(KEY_VALUE_STYLE, createRun(KEY_CHARACTER_STYLE, pair.getKey() + ": "), createRun(null, pair.getValue())));
		}
	}

	@Override
	public void addPageBreak() {
		if (this.summaryEnded)
			throw new RuntimeException("Summary is ended, cannot add to summary");
		this.pageBreakNext = true;
	}

	@Override
	public void writeImage(final BufferedImage image) {
		final P imageParagraph = this.wmlObjectFactory.createP();

		final R imageRun = this.wmlObjectFactory.createR();
		imageParagraph.getContent().add(imageRun);

		final Drawing drawing = this.wmlObjectFactory.createDrawing();
		imageRun.getContent().add(drawing);

		try {
			final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", byteOutputStream);
			final BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(this.extractedDocument.getDocumentPackage(),
					byteOutputStream.toByteArray());
			final Inline imageInline = imagePart.createImageInline("Filename", "An image", nextUniqueImageId(), nextUniqueImageId(), false);

			drawing.getAnchorOrInline().add(imageInline);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		addParagraph(imageParagraph);
	}

	@Override
	public void endReportSummary() {
		if (this.summaryEnded)
			throw new RuntimeException("Summary is already ended");

		// Add styles to document
		this.extractedDocument.getDocumentPackage().getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle()
				.addAll(generateStyles());

		// Add report summary paragraphs before main document
		final List<Object> paragraphList = this.extractedDocument.getDocumentPackage().getMainDocumentPart().getContent();
		if (paragraphList.size() > 0) {
			// page break between report summary and main document
			setPageBreakBefore((P) paragraphList.get(0));
		}
		paragraphList.addAll(0, this.summaryParagraphs);

		// Mark summary as ended
		this.summaryEnded = true;
	}

	private int nextUniqueImageId() {
		return this.nextUniqueImageId++;
	}

	private void setPageBreakBefore(final P paragraph) {
		final BooleanDefaultTrue pageBreakBefore = this.wmlObjectFactory.createBooleanDefaultTrue();
		pageBreakBefore.setVal(true);
		paragraph.getPPr().setPageBreakBefore(pageBreakBefore);
	}

	private P createParagraph(final String styleId, final R... runs) {
		final P p = this.wmlObjectFactory.createP();

		final PPr ppr = this.wmlObjectFactory.createPPr();
		p.setPPr(ppr);

		final PStyle pstyle = this.wmlObjectFactory.createPPrBasePStyle();
		pstyle.setVal(styleId);
		ppr.setPStyle(pstyle);

		p.getContent().addAll(Arrays.asList(runs));

		return p;
	}

	private P createParagraph(final String styleId, final String text) {
		return createParagraph(styleId, createRun(null, text));
	}

	private R createRun(final String styleId, final String text) {
		final R r = this.wmlObjectFactory.createR();

		final RPr rpr = this.wmlObjectFactory.createRPr();
		r.setRPr(rpr);

		if (styleId != null) {
			final RStyle rstyle = this.wmlObjectFactory.createRStyle();
			rstyle.setVal(styleId);
			rpr.setRStyle(rstyle);
		}

		final Text textElement = this.wmlObjectFactory.createText();
		textElement.setValue(text);
		final JAXBElement<Text> wrappedText = this.wmlObjectFactory.createRT(textElement);
		r.getContent().add(wrappedText);

		return r;
	}

	private List<Style> generateStyles() {
		final List<Style> styles = new ArrayList<>();

		styles.add(createParagraphStyle(TITLE_STYLE, paragraphProperties(headingSpacing(), lowerBorder(), centerAligned(), joinNext()),
				runProperties(SERIF_FONT_NAME, 28, notBold())));
		styles.add(createParagraphStyle(HEADING1_STYLE, paragraphProperties(headingSpacing(), lowerBorder(), leftAligned(), joinNext()),
				runProperties(SERIF_FONT_NAME, 22, notBold())));
		styles.add(createParagraphStyle(HEADING2_STYLE, paragraphProperties(headingSpacing(), noBorder(), leftAligned(), joinNext()),
				runProperties(SANS_SERIF_FONT_NAME, 15, bold())));
		styles.add(createParagraphStyle(TEXT_STYLE, paragraphProperties(bodySpacing(), noBorder(), leftAligned(), noJoinNext()),
				runProperties(SANS_SERIF_FONT_NAME, 12, notBold())));
		styles.add(createParagraphStyle(KEY_VALUE_STYLE, paragraphProperties(noSpacing(), noBorder(), leftAligned(), noJoinNext()),
				runProperties(SANS_SERIF_FONT_NAME, 12, notBold())));

		styles.add(createCharacterStyle(KEY_CHARACTER_STYLE, runProperties(SANS_SERIF_FONT_NAME, 12, bold())));

		return styles;
	}

	private Style createParagraphStyle(final String styleId, final PPr ppr, final RPr rpr) {
		final Style style = this.wmlObjectFactory.createStyle();

		style.setType("paragraph");

		style.setStyleId(styleId);
		final Style.Name name = this.wmlObjectFactory.createStyleName();
		name.setVal(styleId);
		style.setName(name);

		style.setPPr(ppr);
		style.setRPr(rpr);

		return style;
	}

	private Style createCharacterStyle(final String styleId, final RPr rpr) {
		final Style style = this.wmlObjectFactory.createStyle();

		style.setType("character");

		style.setStyleId(styleId);
		final Style.Name name = this.wmlObjectFactory.createStyleName();
		name.setVal(styleId);
		style.setName(name);

		style.setRPr(rpr);

		return style;
	}

	private PPr paragraphProperties(final Spacing spacing, final PBdr border, final Jc alignment, final boolean keepNext) {
		final PPr ppr = this.wmlObjectFactory.createPPr();

		ppr.setPBdr(border);
		ppr.setSpacing(spacing);
		ppr.setJc(alignment);

		final BooleanDefaultTrue trueElement = this.wmlObjectFactory.createBooleanDefaultTrue();
		trueElement.setVal(keepNext);
		ppr.setKeepNext(trueElement);
		ppr.setContextualSpacing(trueElement);

		return ppr;
	}

	private Spacing headingSpacing() {
		final Spacing spacing = this.wmlObjectFactory.createPPrBaseSpacing();
		spacing.setBefore(BigInteger.valueOf(240));
		spacing.setAfter(BigInteger.valueOf(240));
		spacing.setLine(BigInteger.valueOf(240));
		spacing.setLineRule(STLineSpacingRule.AUTO);
		return spacing;
	}

	private Spacing bodySpacing() {
		final Spacing spacing = this.wmlObjectFactory.createPPrBaseSpacing();
		spacing.setBefore(BigInteger.valueOf(0));
		spacing.setAfter(BigInteger.valueOf(144));
		spacing.setLine(BigInteger.valueOf(240));
		spacing.setLineRule(STLineSpacingRule.AUTO);
		return spacing;
	}

	private Spacing noSpacing() {
		return null;
	}

	private boolean joinNext() {
		return true;
	}

	private boolean noJoinNext() {
		return false;
	}

	private RPr runProperties(final String fontName, final int fontSize, final boolean bold) {
		final RPr rpr = this.wmlObjectFactory.createRPr();

		final RFonts font = this.wmlObjectFactory.createRFonts();
		font.setAscii(fontName);
		font.setHAnsi(fontName);
		rpr.setRFonts(font);

		final HpsMeasure size = this.wmlObjectFactory.createHpsMeasure();
		size.setVal(BigInteger.valueOf(fontSize * 2));
		rpr.setSz(size);

		final BooleanDefaultTrue isBold = this.wmlObjectFactory.createBooleanDefaultTrue();
		isBold.setVal(bold);
		rpr.setB(isBold);

		return rpr;
	}

	private PBdr lowerBorder() {
		final PBdr pbdr = this.wmlObjectFactory.createPPrBasePBdr();

		final CTBorder border = this.wmlObjectFactory.createCTBorder();
		border.setColor("000000");
		border.setSz(BigInteger.valueOf(2));
		border.setSpace(BigInteger.valueOf(6));
		border.setVal(STBorder.SINGLE);

		pbdr.setBottom(border);
		return pbdr;
	}

	private PBdr noBorder() {
		return null;
	}

	private Jc centerAligned() {
		final Jc jc = this.wmlObjectFactory.createJc();
		jc.setVal(JcEnumeration.CENTER);
		return jc;
	}

	private Jc leftAligned() {
		final Jc jc = this.wmlObjectFactory.createJc();
		jc.setVal(JcEnumeration.LEFT);
		return jc;
	}

	private boolean bold() {
		return true;
	}

	private boolean notBold() {
		return false;
	}
}
