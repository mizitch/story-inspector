package com.story_inspector.ioProcessing.docx;

import static com.story_inspector.ioProcessing.docx.DocXHelper.extractText;
import static com.story_inspector.ioProcessing.docx.DocXHelper.isTextElement;

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.R.CommentReference;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.springframework.stereotype.Component;

import com.story_inspector.analysis.Comment;
import com.story_inspector.analysis.reports.Report;
import com.story_inspector.analysis.summary.ReportSummaryWriter;
import com.story_inspector.ioProcessing.ExtractedDocument;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph;
import com.story_inspector.ioProcessing.ExtractedDocument.ExtractedParagraph.ParagraphType;
import com.story_inspector.ioProcessing.ReportTranscriber;
import com.story_inspector.ioProcessing.StoryIOException;
import com.story_inspector.ioProcessing.StoryIOException.ProcessingExceptionType;
import com.story_inspector.progress.ProgressMonitor;
import com.story_inspector.progress.TaskCanceledException;
import com.story_inspector.story.Paragraph;

/**
 * Implementation of {@link ReportTranscriber} for DOCX files.
 *
 * @author mizitch
 *
 */
@Component
public class DocXReportTranscriber implements ReportTranscriber {

	private final ObjectFactory wmlObjectFactory = new ObjectFactory();

	@Override
	public void transcribeReport(final Report report, final ExtractedDocument extractedDocument, final OutputStream destination,
			final ProgressMonitor progressMonitor) throws StoryIOException, TaskCanceledException {

		if (!(extractedDocument instanceof DocXExtractedDocument))
			throw new IllegalArgumentException("DocXReportTranscriber only supports stories extracted from DocX files");

		final DocXExtractedDocument docXExtractedDocument = (DocXExtractedDocument) extractedDocument;

		try {
			addCommentsToSourceDocument(report, docXExtractedDocument.getDocumentPackage().getMainDocumentPart(),
					docXExtractedDocument.getParagraphs(), progressMonitor.subMonitor(0.0f, 0.5f, "Adding comments to document"));

			progressMonitor.reportProgress(0.5f, "Adding report summary");
			addSummaryToSourceDocument(report, docXExtractedDocument);

			progressMonitor.reportProgress(0.55f, "Writing document file");
			writeResult(docXExtractedDocument.getDocumentPackage(), destination);

			progressMonitor.reportProgress(1.0f, "Complete");
		} catch (final Docx4JException e) {
			throw new StoryIOException(ProcessingExceptionType.UNKNOWN_ERROR, "Unknown error attempting to write annotated story", e);
		}
	}

	private void addSummaryToSourceDocument(final Report report, final DocXExtractedDocument docXExtractedDocument) {
		final ReportSummaryWriter summaryWriter = new DocXReportSummaryWriter(docXExtractedDocument);
		report.writeSummary(summaryWriter);
	}

	/**
	 * Add report comments to document.
	 */
	private void addCommentsToSourceDocument(final Report report, final MainDocumentPart sourceDocument,
			final List<ExtractedParagraph> sourceParagraphs, final ProgressMonitor progressMonitor) throws Docx4JException, TaskCanceledException {

		final Map<Comment, Integer> commentsToIds = extractComments(report);
		final NavigableMap<Integer, Set<Comment>> commentsByStartIndex = generateCommentsByStartIndex(commentsToIds.keySet());
		final NavigableMap<Integer, Set<Comment>> commentsByEndIndex = generateCommentsByEndIndex(commentsToIds.keySet());

		int parsedStoryParagraphIndex = 0;
		for (int i = 0; i < sourceParagraphs.size(); ++i) {
			progressMonitor.reportProgress(i * 1.0f / sourceParagraphs.size(), i + " / " + sourceParagraphs.size() + " paragraphs");
			if (sourceParagraphs.get(i).getType() == ParagraphType.TEXT) {
				final P paragraphNode = (P) sourceDocument.getContent().get(i);
				final Paragraph parsedParagraph = report.getStory().getChildrenAtLevel(Paragraph.class).get(parsedStoryParagraphIndex);
				insertCommentsIntoParagraph(paragraphNode, parsedParagraph, commentsByStartIndex, commentsByEndIndex, commentsToIds);
				++parsedStoryParagraphIndex;
			}
		}

		addCommentContents(sourceDocument, commentsToIds);
	}

	private void writeResult(final WordprocessingMLPackage documentPackage, final OutputStream destination) throws Docx4JException {
		final SaveToZipFile saver = new SaveToZipFile(documentPackage);
		saver.save(destination);
	}

	/**
	 * Adds text contents of comments to comment section of document.
	 */
	private void addCommentContents(final MainDocumentPart sourceDocument, final Map<Comment, Integer> commentsToIds) throws Docx4JException {
		// Generate the date we will use for all comments (current date)
		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		XMLGregorianCalendar commentDate;
		try {
			commentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (final DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}

		// Generate the docx comment elements for all comments
		final List<Comments.Comment> docComments = new ArrayList<>();
		for (final Comment comment : commentsToIds.keySet()) {
			final Comments.Comment docComment = this.wmlObjectFactory.createCommentsComment();
			docComment.setAuthor("StoryInspector-Analyzer-" + comment.getAnalyzer().getName());
			docComment.setDate(commentDate);
			// Analyzer with a name of "Example" gets the initials SIAE for Story Inspector Analyzer Example
			docComment.setInitials("SIA" + comment.getAnalyzer().getName().substring(0, 1).toUpperCase());
			docComment.setId(BigInteger.valueOf(commentsToIds.get(comment)));
			docComment.getEGBlockLevelElts().add(generateCommentContent(comment.getContent()));
			docComments.add(docComment);
		}
		// Sort the comment elements. Should probably work without this, but not taking any chances
		docComments.sort((c, o) -> c.getId().intValue() - o.getId().intValue());

		// Create comments "part" of docx if it doesn't exist
		if (sourceDocument.getCommentsPart() == null) {
			final CommentsPart commentsPart = new CommentsPart();
			sourceDocument.addTargetPart(commentsPart);
		}

		// And stick it all together
		final Comments docCommentsElement = this.wmlObjectFactory.createComments();
		for (final Comments.Comment docComment : docComments) {
			docCommentsElement.getComment().add(docComment);
		}
		sourceDocument.getCommentsPart().setJaxbElement(docCommentsElement);
	}

	private P generateCommentContent(final String content) {
		final P paragraph = this.wmlObjectFactory.createP();
		final PPr paragraphProperties = this.wmlObjectFactory.createPPr();
		paragraph.setPPr(paragraphProperties);

		paragraph.getContent().add(createRunWithContent(Collections.singletonList(createTextElement(content)), this.wmlObjectFactory.createRPr()));

		return paragraph;
	}

	/**
	 * Adds comment start and end objects in paragraph node. Removes comments from input comment maps and adds to result comment map as comments are
	 * processed.
	 *
	 * @param paragraphNode
	 *            Paragraph to insert comment start and end objects into
	 * @param parsedParagraph
	 *            The parsed paragraph
	 * @param commentsByStartIndex
	 *            A map of story character start index to comments.
	 * @param commentsByEndIndex
	 *            A map of story character end index to comments.
	 * @param commentsToIds
	 *            A map from comment to id for all comments whose start and end ranges have been added
	 */
	private void insertCommentsIntoParagraph(final P paragraphNode, final Paragraph parsedParagraph,
			final NavigableMap<Integer, Set<Comment>> commentsByStartIndex, final NavigableMap<Integer, Set<Comment>> commentsByEndIndex,
			final Map<Comment, Integer> commentsToIds) {
		// Filter comment starts and comment ends to only those contained within this paragraph, we don't care about the others at the moment
		final NavigableMap<Integer, Set<Comment>> paragraphCommentStarts = commentsByStartIndex.subMap(parsedParagraph.getRange().getStartIndex(),
				true, parsedParagraph.getRange().getEndIndex(), false);
		final NavigableMap<Integer, Set<Comment>> paragraphCommentEnds = commentsByEndIndex.subMap(parsedParagraph.getRange().getStartIndex(), true,
				parsedParagraph.getRange().getEndIndex(), false);

		// No comment range markers to insert in this paragraph, so we're done!
		if (paragraphCommentStarts.isEmpty() && paragraphCommentEnds.isEmpty())
			return;

		int currentLocation = parsedParagraph.getRange().getStartIndex();

		final NavigableSet<Integer> partitionLocations = Stream
				.concat(paragraphCommentStarts.keySet().stream(), paragraphCommentEnds.keySet().stream()).sorted().distinct()
				.collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

		final LinkedList<Object> partitionedParagraphContent = splitParagraphChildren(paragraphNode.getContent(), partitionLocations,
				currentLocation);

		final ListIterator<Object> itr = partitionedParagraphContent.listIterator();

		while (itr.hasNext()) {
			if (paragraphCommentStarts.containsKey(currentLocation)) {
				for (final Comment comment : paragraphCommentStarts.get(currentLocation)) {
					itr.add(generateCommentStart(comment, commentsToIds.get(comment), paragraphNode));
				}
				// Noting that we have finished with this location
				// Otherwise may insert duplicates if the doc has elements with no text at this location
				paragraphCommentStarts.remove(currentLocation);
			}
			if (paragraphCommentEnds.containsKey(currentLocation)) {
				for (final Comment comment : paragraphCommentEnds.get(currentLocation)) {
					itr.add(generateCommentEnd(comment, commentsToIds.get(comment), paragraphNode));
					itr.add(generateCommentReference(comment, commentsToIds.get(comment), paragraphNode));
				}
				// Noting that we have finished with this location
				// Otherwise may insert duplicates if the doc has elements with no text at this location
				paragraphCommentEnds.remove(currentLocation);
			}

			final Object paragraphChild = itr.next();
			if (paragraphChild instanceof R) {
				currentLocation += extractText((R) paragraphChild).length();
			}

		}
		paragraphNode.getContent().clear();
		paragraphNode.getContent().addAll(partitionedParagraphContent);
	}

	/**
	 * Take all paragraph children and split as necessary so the provided partition locations do not lie within a child.
	 *
	 * @param paragraphChildren
	 *            The children of the paragraph
	 * @param partitionLocations
	 *            The locations of partitions
	 * @param paragraphStartCharacterIndex
	 *            Starting character index of paragraph
	 * @return
	 */
	private LinkedList<Object> splitParagraphChildren(final List<Object> paragraphChildren, final NavigableSet<Integer> partitionLocations,
			final int paragraphStartCharacterIndex) {
		final LinkedList<Object> result = new LinkedList<>();
		int position = paragraphStartCharacterIndex;

		for (final Object paragraphChild : paragraphChildren) {
			// We've got a run, so split that thing up
			if (paragraphChild instanceof R) {
				final R run = (R) paragraphChild;
				final int runLength = extractText(run).length();
				result.addAll(splitRun(run, partitionLocations, position));

				position += runLength;
			}
			// Not a run, so just add it to the list
			else {
				result.add(paragraphChild);
			}
		}
		return result;
	}

	/**
	 * Split text run so that the provided partition locations do not lie within a run or text element.
	 *
	 * @param run
	 *            The text run
	 * @param partitionLocations
	 *            The locations of partitions
	 * @param runStartCharacterIndex
	 *            Starting character index of run
	 * @return List of split runs
	 */
	@SuppressWarnings("unchecked")
	private List<R> splitRun(final R run, final NavigableSet<Integer> partitionLocations, final int runStartCharacterIndex) {
		final List<List<Object>> runContents = new ArrayList<>();
		runContents.add(new ArrayList<>());

		int location = runStartCharacterIndex;

		for (final Object runChild : run.getContent()) {
			// If not a text element, just add to current partition
			if (!isTextElement(runChild)) {
				runContents.get(runContents.size() - 1).add(runChild);

			}
			// It is a text element, so may need to split it
			else {
				final int textLength = extractText((JAXBElement<Text>) runChild).length();
				// This will only split if necessary
				final List<JAXBElement<Text>> splitText = splitText((JAXBElement<Text>) runChild, partitionLocations, location);
				location += textLength;

				// We didn't have to split the text and we are not currently on a partition border, so just add the text and move on
				if (splitText.size() == 1 && !partitionLocations.contains(location)) {
					runContents.get(runContents.size() - 1).add(runChild);
				}
				// We do have to add more partitions, either because we're on a boundary or the text has been split
				else {
					for (final JAXBElement<Text> text : splitText) {
						runContents.get(runContents.size() - 1).add(text);
						runContents.add(new ArrayList<>());
					}
				}
			}
		}
		return runContents.stream().map(rc -> createRunWithContent(rc, run.getRPr())).collect(Collectors.toList());
	}

	/**
	 * Split text element according to provided partition locations
	 *
	 * @param text
	 *            Text to partition
	 * @param partitionLocations
	 *            Partition locations
	 * @param textStartCharacterIndex
	 *            Starting character index of text
	 * @return List of split text elements
	 */
	private List<JAXBElement<Text>> splitText(final JAXBElement<Text> text, final NavigableSet<Integer> partitionLocations,
			final int textStartCharacterIndex) {
		final List<String> splitStrings = splitString(text.getValue().getValue(), partitionLocations, textStartCharacterIndex);
		if (splitStrings.size() == 1) {
			return Collections.singletonList(text);
		} else {
			return splitStrings.stream().map(this::createTextElement).collect(Collectors.toList());
		}
	}

	/**
	 * Split string according to provided partition locations
	 *
	 * @param original
	 *            String to partition
	 * @param partitionLocations
	 *            Partition locations
	 * @param stringStartCharacterIndex
	 *            Starting character index of string
	 * @return List of split strings
	 */
	private List<String> splitString(final String original, final NavigableSet<Integer> partitionLocations, final int stringStartCharacterIndex) {
		final NavigableSet<Integer> filteredLocations = partitionLocations.subSet(stringStartCharacterIndex, true,
				stringStartCharacterIndex + original.length(), false);

		final List<String> result = new ArrayList<>();
		int pos = 0;
		for (final int loc : filteredLocations) {
			final int localLoc = loc - stringStartCharacterIndex;
			result.add(original.substring(pos, localLoc));
			pos = localLoc;
		}
		result.add(original.substring(pos));
		return result;
	}

	private R createRunWithContent(final List<Object> content, final RPr runProperties) {
		final R run = this.wmlObjectFactory.createR();
		run.setRPr(runProperties);
		run.getContent().addAll(content);
		return run;
	}

	private JAXBElement<Text> createTextElement(final String content) {
		final Text textElement = this.wmlObjectFactory.createText();
		textElement.setValue(content);
		final JAXBElement<Text> wrappedText = this.wmlObjectFactory.createRT(textElement);
		return wrappedText;
	}

	private CommentRangeStart generateCommentStart(final Comment comment, final int id, final Object parent) {
		final CommentRangeStart start = this.wmlObjectFactory.createCommentRangeStart();
		start.setId(BigInteger.valueOf(id));
		start.setParent(parent);
		return start;
	}

	private CommentRangeEnd generateCommentEnd(final Comment comment, final int id, final Object parent) {
		final CommentRangeEnd end = new CommentRangeEnd();
		end.setId(BigInteger.valueOf(id));
		end.setParent(parent);
		return end;
	}

	private R generateCommentReference(final Comment comment, final int id, final Object parent) {
		final CommentReference commentRef = this.wmlObjectFactory.createRCommentReference();
		commentRef.setId(BigInteger.valueOf(id));
		final JAXBElement<CommentReference> wrappedCommentRef = this.wmlObjectFactory.createRCommentReference(commentRef);

		final R range = this.wmlObjectFactory.createR();
		range.getContent().add(wrappedCommentRef);
		range.setParent(parent);

		return range;
	}

	private NavigableMap<Integer, Set<Comment>> generateCommentsByEndIndex(final Set<Comment> comments) {
		return comments.stream().collect(TreeMap::new, (m, c) -> {
			if (!m.containsKey(c.getSelection().getEndIndex()))
				m.put(c.getSelection().getEndIndex(), new HashSet<>());
			m.get(c.getSelection().getEndIndex()).add(c);
		}, TreeMap::putAll);
	}

	private NavigableMap<Integer, Set<Comment>> generateCommentsByStartIndex(final Set<Comment> comments) {
		return comments.stream().collect(TreeMap::new, (m, c) -> {
			if (!m.containsKey(c.getSelection().getStartIndex()))
				m.put(c.getSelection().getStartIndex(), new HashSet<>());
			m.get(c.getSelection().getStartIndex()).add(c);
		}, TreeMap::putAll);
	}

	private Map<Comment, Integer> extractComments(final Report report) {
		final List<Comment> commentList = report.getReportSections().stream() // get all the report tabs
				.flatMap(rt -> rt.getAnalyzerResults().stream()) // for each tab, get all analyzer results
				.flatMap(ar -> ar.getComments().stream()) // for each analyzer result, get all comments
				.sorted().collect(Collectors.toList()); // sort all of the comments and dump them in a list

		// Map each comment to its position in the list, which will be its "id" for the purposes of the docx file
		final Map<Comment, Integer> result = new HashMap<>();
		for (int i = 0; i < commentList.size(); ++i) {
			result.put(commentList.get(i), i);
		}
		return result;
	}

	@Override
	public Set<String> getSupportedFileTypes() {
		return Collections.singleton("DOCX");
	}

}
