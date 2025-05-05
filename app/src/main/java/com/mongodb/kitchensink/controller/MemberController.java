package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.model.Member;
import com.mongodb.kitchensink.repository.MemberRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/members")
public class MemberController {

  private final MemberRepository repo;

  public MemberController(MemberRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Member> list() {
    return repo.findAllByOrderByNameAsc();
  }
}
