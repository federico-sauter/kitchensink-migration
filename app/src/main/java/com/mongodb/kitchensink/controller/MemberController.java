package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.model.Member;
import com.mongodb.kitchensink.service.MemberService;
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

  private final MemberService service;

  public MemberController(MemberService service) {
    this.service = service;
  }

  @GetMapping
  public List<Member> list() {
    return service.findAllOrderedByNameAsc();
  }

  @PostMapping
  public ResponseEntity<?> register(@Valid @RequestBody Member m) {
    if (service.existsByEmail(m.getEmail())) {
      return ResponseEntity.status(409).body(Map.of("email", "Email taken"));
    }
    service.save(m);
    return ResponseEntity.ok().build();
  }
}
