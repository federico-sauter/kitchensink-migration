package com.mongodb.kitchensink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
  @GetMapping("/ui/members")
  public String membersPage() {
    return "members"; // resolves to templates/members.html
  }
}
