package com.pluto.pluto_interview.config;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {
	@Bean
	public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
		return PgVectorEmbeddingStore.builder()
			  .host("localhost")
			  .port(5432)
			  .database("pluto_interview")
			  .user("pluto")
			  .password("Chen@0216")
			  .table("question_embedding")
			  .dimension(embeddingModel.embed("dimension check").content().dimension())
			  .build();
	}
}
