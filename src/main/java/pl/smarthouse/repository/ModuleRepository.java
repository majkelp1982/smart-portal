package pl.smarthouse.repository;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ModuleRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public <T> Mono<T> getLastModuleData(final String tableName, final Class<T> tClass) {
    final Query query = new Query();
    query.limit(1);
    query.with(Sort.by(Sort.Direction.DESC, "saveTimestamp"));
    return reactiveMongoTemplate.findOne(query, tClass, tableName);
  }

  public Flux<String> getValues(
      final String tableName,
      final String fieldPath,
      final LocalDateTime fromTimestamp,
      final LocalDateTime toTimestamp) {
    final Query query = new Query();
    query.addCriteria(Criteria.where("saveTimestamp").gte(fromTimestamp).lt(toTimestamp));
    query.fields().include(fieldPath).include("saveTimestamp");
    return reactiveMongoTemplate.find(query, String.class, tableName);
  }
}
