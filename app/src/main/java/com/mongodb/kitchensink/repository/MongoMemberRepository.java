package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.model.Member;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoMemberRepository extends MongoRepository<Member, String> {
  List<Member> findAllByOrderByNameAsc();

  boolean existsByEmail(String email);
}
