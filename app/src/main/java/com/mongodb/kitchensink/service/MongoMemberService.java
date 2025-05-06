package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.model.Member;
import com.mongodb.kitchensink.repository.MongoMemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MongoMemberService implements MemberService {
  private final MongoMemberRepository repo;

  public MongoMemberService(MongoMemberRepository repo) {
    this.repo = repo;
  }

  @Override
  public List<Member> findAllOrderedByNameAsc() {
    return repo.findAllByOrderByNameAsc().stream().collect(Collectors.toList());
  }

  @Override
  public boolean existsByEmail(String email) {
    return repo.existsByEmail(email);
  }

  @Override
  public Member save(Member m) {
    Member saved = repo.save(m);
    m.setId(saved.getId());
    return m;
  }
}
