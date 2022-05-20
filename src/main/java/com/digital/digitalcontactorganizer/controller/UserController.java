package com.digital.digitalcontactorganizer.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.digital.digitalcontactorganizer.dao.ContactRepository;
import com.digital.digitalcontactorganizer.dao.UserRepository;
import com.digital.digitalcontactorganizer.entities.Contact;
import com.digital.digitalcontactorganizer.entities.User;
import com.digital.digitalcontactorganizer.helper.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ContactRepository contactRepository;

  //This will commonly send values to all pages we have under /user
  @ModelAttribute
  public void addCommonData(Model model, Principal principal){
    String email=principal.getName();
    User user=userRepository.getUserByUserName(email);
    System.out.println(user);
    model.addAttribute("user", user);
  }

  @RequestMapping("/index")
  public String dashboard(Model model, Principal principal){
    model.addAttribute("title", "Dashboard");
    return "normal/user_dashboard";
  }  

  //Handler for Add contact
  @GetMapping("/add-contact")
  public String openAddContactForm(Model model){
    model.addAttribute("title", "Add Contact");
    model.addAttribute("contact", new Contact());
    return "normal/add_contact";
  }

  //Processing add contact form
  @PostMapping("/process-contact")
  public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session){
    try{
    String name=principal.getName();
    User user=userRepository.getUserByUserName(name);
    
    //Processing and Uploading file
    if(file.isEmpty()){
      //If file is empty try your message
      contact.setImage("login.png");
      System.out.println("File is empty");
    }else{
      //upload file to Folder and update name to contact
      contact.setImage(file.getOriginalFilename());

      //To upload file in folder

      File saveFile=new ClassPathResource("static/img").getFile();
      Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
      Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
      System.out.println("Image is Uploded");
    }

    contact.setUser(user);
    user.getContacts().add(contact);
    
    this.userRepository.save(user);
    
    // System.out.println(contact);
    System.out.println("added to database");

    //Success Message
    session.setAttribute("message", new Message("Your Contact is added!! Add More..", "success"));
    }catch(Exception e){
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
      //Error Message
      session.setAttribute("message", new Message("Something went wrong!!", "danger"));
    }

    return "normal/add_contact";
  }


  //Per Page = 5[n] Contacts
  //current page = 0 [page]
  @GetMapping("/show-contacts/{page}")
  public String showContacts(@PathVariable("page") Integer page,Model m, Principal principal){
    m.addAttribute("title", "Show User Contacts");

    //To send whole contact List
    //One of the way using Principal
    // String userName=principal.getName();  
    // User user=this.userRepository.getUserByUserName(userName);
    // List<Contact> contacts = user.getContacts();

    //Using Repository

    String userName=principal.getName();  
    User user=this.userRepository.getUserByUserName(userName);

    //to create pageablt object
    Pageable pageable = PageRequest.of(page,7);
    Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
    m.addAttribute("contacts", contacts);
    m.addAttribute("currentPage", page);
    m.addAttribute("totalPages", contacts.getTotalPages());
    return "normal/show_contacts";
  }


  //Showing perticular contact details
  @GetMapping("/{cId}/contacts")
  public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal){
    System.out.println("CId "+cId);

    String name = principal.getName();
    User user=userRepository.getUserByUserName(name);

    Optional<Contact> contactOptional = contactRepository.findById(cId);
    Contact contact = contactOptional.get();
    //if to make sure contact related tologged in user will get details
    if(user.getId()==contact.getUser().getId())
    model.addAttribute("contact", contact);
    return "normal/contact_details";
  }

  //Delete Contact Handler
  @GetMapping("/delete/{cid}")
  public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal,HttpSession session){
    Optional<Contact> contactOptional = this.contactRepository.findById(cId);
    Contact contact=contactOptional.get();
    String name = principal.getName();
    User user = this.userRepository.getUserByUserName(name);
    user.getContacts().remove(contact);
    this.userRepository.save(user);
    //Unlinking contact from user

    this.contactRepository.delete(contact);
    session.setAttribute("message", new Message("Contact deleted Successfully....", "success"));
    
    return "redirect:/user/show-contacts/0";
  }


  //Open update form handler
  @PostMapping("/update-contact/{cid}")
  public String updateForm(@PathVariable("cid") Integer cid,Model m){
    m.addAttribute("title", "Update Contact");
    Contact contact=this.contactRepository.findById(cid).get();
    m.addAttribute("contact", contact);
    return "normal/updateForm";
  }



  //Updating contact handler
  @PostMapping("/process-update")
  public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal){
    try{

      //old Contact details
      Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
      if(!file.isEmpty()){
        //file work--Rewirte
        //delete old photo and upload new photo
        File deleteFile=new ClassPathResource("static/img").getFile();
        File file1=new File(deleteFile, oldContactDetails.getImage());
        file1.delete();

        //Upload new photo
        File saveFile=new ClassPathResource("static/img").getFile();
        Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        contact.setImage(file.getOriginalFilename());
        session.setAttribute("message", new Message("Your contact is updated...","success"));

        
      }else{
        contact.setImage(oldContactDetails.getImage());
      }

      String name = principal.getName();
      User user=this.userRepository.getUserByUserName(name);
      contact.setUser(user);
      this.contactRepository.save(contact);
    System.out.println("Contact Name: "+contact.getName());
    System.out.println("Contact Id: "+contact.getcId());
    }catch(Exception e){
      e.printStackTrace();
    }
    return "redirect:/user/"+contact.getcId()+"/contacts";
  }


  //YOUR PROFILE HANDLER
  @GetMapping("/profile")
  public String yourProfile(Model model){
    model.addAttribute("title","Profile Page");
    return "normal/profile";
  }


}
