package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.model.Member;
import com.mongodb.kitchensink.repository.MemberRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping
  public ResponseEntity<?> register(@Valid @RequestBody Member m) {
    if (repo.existsByEmail(m.getEmail())) {
      return ResponseEntity.status(409).body(Map.of("email", "Email taken"));
    }
    repo.save(m);
    // legacy test expects empty body + 200
    return ResponseEntity.ok().build();
  }
}
