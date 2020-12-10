package com.cuckoo.BackendServer.service.remoteServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.cuckoo.BackendServer.exceptions.FirebaseException;
import com.cuckoo.BackendServer.exceptions.InvalidArgumentsException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class NotificationsRemoteService {

  @Value("${firebase.key}")
  private String keyPath;

  /** 
   * Standard configurations for the Firebase SDK 
   */
  @PostConstruct
  public void initGoogleSettings() throws IOException{

    InputStream serviceAccount = new FileInputStream(new File(this.keyPath));

    FirebaseOptions options = FirebaseOptions.builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccount))
      .setDatabaseUrl("https://cuckoocovid.firebaseio.com")
      .build();
      
    FirebaseApp.initializeApp(options);
  }

  /**
   * Sends a notification to the devices subscribed to the topic "main"
   * @param information information to be sent
   */
  public void sendDataNotification(Map<String,String> information) {
    if(information == null)
      throw new InvalidArgumentsException("information can not be null");

    Message message = Message.builder() 
      .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
      .putAllData(information)
      .setTopic("main")
      .build();

    try{
      FirebaseMessaging.getInstance().send(message);
    } catch(FirebaseMessagingException e) {
      throw new FirebaseException(e.getMessage());
    }
  }
}
