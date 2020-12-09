package com.cuckoo.BackendServer.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.cuckoo.BackendServer.exceptions.InvalidArgumentsException;
import com.cuckoo.BackendServer.exceptions.UnathorizedRequestException;
import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.HashesRepository;
import com.cuckoo.BackendServer.repository.LoginRepository;
import com.cuckoo.BackendServer.service.HashManagementService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@ExtendWith(SpringExtension.class)
public class HashManagementTest {

  private String medic1_id;
  private String medic2_id;

  @Autowired
  private HashManagementService hashManagementService;

  @Autowired
  private HashesRepository hashesRepository;

  @Autowired
  private PasswordEncoder passEncoder;

  @Autowired
  private LoginRepository dbAPI;

  @BeforeEach
  public void initUsers(){
    String email1 = "filipe.m.cunha@gmail.com";
    String email2 = "pedro.carrott@gmail.com";
    
    UserType user = new UserType();
    user.setEmail(email1);
    user.setPassword(this.passEncoder.encode("1234"));
    user.setFirstName("Filipe");
    user.setLastName("Cunha");

    this.dbAPI.createUserInDatabase(user);

    this.medic1_id = this.dbAPI.getUserByEmail(email1).getUsername();


    user = new UserType();
    user.setEmail(email2);
    user.setPassword(this.passEncoder.encode("1234"));
    user.setFirstName("Pedro");
    user.setLastName("Carrott");

    this.dbAPI.createUserInDatabase(user);

    this.medic2_id = this.dbAPI.getUserByEmail(email2).getUsername();
  }

  @AfterEach
  public void deleteUsersAndHashes(){
    this.hashesRepository.clearHashes();

    UserType user = new UserType();
    user.setUsername(UUID.fromString(this.medic1_id));
    user.setEmail("filipe.m.cunha@gmail.com");
    user.setPassword("1234");
    this.dbAPI.removeUserInDatabase(user);

    user.setUsername(UUID.fromString(medic2_id));
    user.setEmail("pedro.carrott@gmail.com");
    user.setPassword("1234");
    this.dbAPI.removeUserInDatabase(user);
  } 


  @Test
  public void medicInsertsHashWithSuccess(){
    HashDto hash = new HashDto();
    HashDto res;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    this.hashManagementService.addPatient(hash, this.medic1_id);
    res = this.hashManagementService.getHashes(this.medic1_id).get(0);

    assertEquals("a note", res.getNote());
  }

  
  @Test
  public void medicSubmitsHashWithoutHash(){
    HashDto hash = new HashDto();
    String idAsString = this.medic1_id;
    hash.setNote("a note");
    
    assertThrows(InvalidArgumentsException.class, () -> {
      this.hashManagementService.addPatient(hash, idAsString);
    });
  }

  @Test
  public void medicSubmitsHashWithoutNote(){
    HashDto hash = new HashDto();
    String idAsString = this.medic1_id;
    hash.setHashValue("a note");
    
    assertThrows(InvalidArgumentsException.class, () -> {
      this.hashManagementService.addPatient(hash, idAsString);
    });
  }
  
  @Test
  public void medicGetsHashesWithSuccess(){
    HashDto hash = new HashDto();
    List<HashDto> res;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    this.hashManagementService.addPatient(hash, this.medic1_id);
    this.hashManagementService.addPatient(hash, this.medic1_id);
    res = this.hashManagementService.getHashes(this.medic1_id);

    assertEquals(2, res.size());

    this.hashesRepository.clearHashes();
    res = this.hashManagementService.getHashes(this.medic1_id);
    assertEquals(0, res.size());
  }

  @Test
  public void medicDoesNotGetHashesFromOtherMedics(){
    HashDto hash = new HashDto();
    List<HashDto> res;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    this.hashManagementService.addPatient(hash, this.medic1_id);
    this.hashManagementService.addPatient(hash, this.medic2_id);
    res = this.hashManagementService.getHashes(this.medic1_id);

    assertEquals(1, res.size());
  }

  @Test
  public void medicMarksPatientAsPositive(){
    HashDto hash = new HashDto();
    int num;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    Date date = new Date();
    Long dateTimestamp = date.getTime();
    
    this.hashManagementService.addPatient(hash, this.medic1_id);
    hash = this.hashManagementService.getHashes(this.medic1_id).get(0);
    hash.setDate(dateTimestamp);
    this.hashManagementService.markPatientAsPositive(hash, this.medic1_id);
    num = this.hashManagementService.getHashes(this.medic1_id).size();
    assertEquals(0, num);
    List<HashDto> positiveHashes = this.hashesRepository.getPositiveHashes();
    num = positiveHashes.size();
    assertEquals(1, num);
    assertEquals(dateTimestamp, positiveHashes.get(0).getDate());
  }

  @Test
  public void medicMarksPatientAsPositiveFromOtherMedic(){
    HashDto hash = new HashDto();
    String medicId = this.medic2_id;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    Date date = new Date();
    Long dateTimestamp = date.getTime();
    this.hashManagementService.addPatient(hash, this.medic1_id);
    HashDto hash2 = this.hashManagementService.getHashes(this.medic1_id).get(0);
    hash2.setDate(dateTimestamp);
    
    assertThrows(UnathorizedRequestException.class, () -> {
      this.hashManagementService.markPatientAsPositive(hash2, medicId);
    });
  }

  @Test
  public void medicMarksPatientAsPositiveThatDoesNotExist(){
    HashDto hash = new HashDto();
    String medicId = this.medic1_id;
    Date date = new Date();
    Long dateTimestamp = date.getTime();
    hash.setNote("a note");
    hash.setDate(dateTimestamp);
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    hash.setId(1);

    assertThrows(UnathorizedRequestException.class , () -> {
      this.hashManagementService.markPatientAsPositive(hash, medicId);
    });
  }

  @Test
  public void medicMarksPatientAsNegative(){
    HashDto hash = new HashDto();
    int num;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    this.hashManagementService.addPatient(hash, this.medic1_id);
    hash = this.hashManagementService.getHashes(this.medic1_id).get(0);
    this.hashManagementService.markPatientAsNegative(hash, this.medic1_id);
    num = this.hashManagementService.getHashes(this.medic1_id).size();
    assertEquals(0, num);
    num = this.hashesRepository.getPositiveHashes().size();
    assertEquals(0, num);
  }


  @Test
  public void medicMarksPatientAsNegativeFromOtherMedic(){
    HashDto hash = new HashDto();
    String medicId = this.medic2_id;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    this.hashManagementService.addPatient(hash, this.medic1_id);
    HashDto hash2 = this.hashManagementService.getHashes(this.medic1_id).get(0);
    
    assertThrows(UnathorizedRequestException.class, () -> {
      this.hashManagementService.markPatientAsNegative(hash2, medicId);
    });
  }

  @Test
  public void medicMarksPatientAsNegativeThatDoesNotExist(){
    HashDto hash = new HashDto();
    String medicId = this.medic1_id;
    hash.setNote("a note");
    hash.setHashValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    hash.setId(1);

    assertThrows(UnathorizedRequestException.class , () -> {
      this.hashManagementService.markPatientAsNegative(hash, medicId);
    });
  }

}
