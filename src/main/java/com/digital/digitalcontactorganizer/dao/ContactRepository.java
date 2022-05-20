package com.digital.digitalcontactorganizer.dao;

import com.digital.digitalcontactorganizer.entities.Contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ContactRepository extends JpaRepository<Contact,Integer>{
  //Pegination Method Implementation
  //will put two value in pageable object
  //Contact per page--> 5
  //Current page
  @Query("from Contact as c where c.user.id =:userId")
  public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);
  
}
