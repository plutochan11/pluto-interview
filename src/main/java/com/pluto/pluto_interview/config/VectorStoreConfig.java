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
	private final PgVectorProperties pgVectorProperties;

	public VectorStoreConfig(PgVectorProperties pgVectorProperties) {
		this.pgVectorProperties = pgVectorProperties;
	}

	@Bean
	public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
		return PgVectorEmbeddingStore.builder()
			  .host(pgVectorProperties.getHost())
			  .port(pgVectorProperties.getPort())
			  .database(pgVectorProperties.getDatabase())
			  .user(pgVectorProperties.getUser())
			  .password(pgVectorProperties.getPassword())
			  .table(pgVectorProperties.getTable())
			  .dimension(embeddingModel.embed("dimension check").content().dimension())
			  .build();
	}
}
