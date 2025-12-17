package com.pluto.pluto_interview.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionEmbeddingService implements EmbeddingService{
	private final EmbeddingModel embeddingModel;
	private final EmbeddingStore<TextSegment> embeddingStore;

	@Override
	public void embedAndStore(String answer, String questionId) {
		// Create a TextSegment for the Vector DB
		// Embed the question content and attach metadata.
		TextSegment segment = TextSegment.from(
			  answer,
			  Metadata.from("questionId", questionId)
		);

		// Generate embedding
		Embedding embedding = embeddingModel.embed(segment).content();

		// Store in Vector DB
		embeddingStore.add(embedding, segment);
	}
}
