package io.github.ivankurchenko.blogoteldecoupledmonitoring.repository;

import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
    List<ProductDocument> findByName(String name);
}
