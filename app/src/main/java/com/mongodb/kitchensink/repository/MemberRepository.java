package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.model.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  List<Member> findAllByOrderByNameAsc();

  boolean existsByEmail(String email);
}
