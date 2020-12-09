package com.cuckoo.BackendServer.models.hashes;

public class HashDto {
  
  private String hashValue;
  private String note;
  private Integer id;
  private Long date;


  public HashDto(){
    /** for java Bean */
  }

  public HashDto(Integer id, String hashValue, String note){
    this.id = id;
    this.hashValue = hashValue;
    this.note = note;
  }

  public HashDto(Integer id, String note){
    this.id = id;
    this.note = note;
  }

  public String getHashValue() {
    return hashValue;
  }

  public void setHashValue(String hashValue) {
    this.hashValue = hashValue;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Long getDate() {
    return date;
  }

  public void setDate(Long date) {
    this.date = date;
  }
}
