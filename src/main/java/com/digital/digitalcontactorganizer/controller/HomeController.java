package com.digital.digitalcontactorganizer.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.digital.digitalcontactorganizer.dao.UserRepository;
import com.digital.digitalcontactorganizer.entities.User;
import com.digital.digitalcontactorganizer.helper.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  public UserRepository userRepository;


  //Home handler
  @RequestMapping("/")
  public String home(Model model){
    model.addAttribute("title", "Home-Digital Contact Organizer");
    return "home";
  }

  //About handler
  @RequestMapping("/about")
  public String about(Model model){
    model.addAttribute("title", "About-Digital Contact Organizer");
    return "about";
  }

  //signup handler
  @RequestMapping("/signup")
  public String signup(Model model){
    model.addAttribute("title", "Register-Digital Contact Organizer");
    model.addAttribute("user", new User());
    return "signup";
  }

  @PostMapping("/do_register")
  public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session){

    try {
      if(!agreement){
        throw new Exception("You have not agreed the Terms and Conditions");
      }
      if(result1.hasErrors()){
        model.addAttribute("user", user);
        return "signup";
      }
      user.setRole("ROLE_USER");
      user.setEnabled(true);
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      User result=this.userRepository.save(user);
      System.out.println("Agreement "+agreement);
      System.out.println("user: "+user);
      System.out.println("Result in Database:"+result);
      model.addAttribute("user", new User());
      session.setAttribute("message", new Message("Successfully registered", "alert-success"));
      return "signup";

    } catch (Exception e) {
      e.printStackTrace();
      session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
      return "signup";
    }
    
  }

  @GetMapping("/signin")
  public String cutomLogin(Model model){
    model.addAttribute("title","Login Page");
    return "login";
  }
}
