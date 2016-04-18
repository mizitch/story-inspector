package com.story_inspector.story;

import java.util.List;

/**
 * Default implementation of {@link Sentence}
 *
 * @author mizitch
 *
 */
public class SentenceImpl extends BaseParentalTextNode<TokenImpl> implements Sentence {

	/**
	 * Creates a new instance
	 *
	 * @param range
	 *            Range this node covers
	 * @param tokens
	 *            Tokens this sentence contains
	 */
	public SentenceImpl(final TextRange range, final List<TokenImpl> tokens) {
		super(range, TokenImpl.class, tokens);
		for (final TokenImpl token : tokens) {
			token.setParent(this);
		}
	}

	@Override
	public boolean isFragment() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean isQuestion() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
