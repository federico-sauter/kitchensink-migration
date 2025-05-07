package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.model.Member;
import java.util.List;

public interface MemberService {
  List<Member> findAllOrderedByNameAsc();

  boolean existsByEmail(String email);

  Member save(Member m);
}
