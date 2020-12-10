package com.cuckoo.BackendServer.service.remoteServices;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
import com.google.firebase.messaging.Notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationsRemoteService {

  /** 
   * Standard configurations for the Firebase SDK 
   */
  @PostConstruct
  public void initGoogleSettings() throws IOException{

    ClassLoader classLoader = NotificationsRemoteService.class.getClassLoader();
    InputStream serviceAccount =  classLoader.getResourceAsStream("keys/cuckoocovid-firebase-key.json");

    FirebaseOptions options = FirebaseOptions.builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccount))
      .setDatabaseUrl("https://cuckoocovid.firebaseio.com")
      .build();
      
    FirebaseApp.initializeApp(options);

    Map<String, String> send = new HashMap();
    send.put("title", "there");
    send.put("body", "brother");
    this.sendDataNotification(send);
  }

  /**
   * Sends a notification to the devices subscribed to the topic "main"
   * @param information information to be sent
   */
  public void sendDataNotification(Map<String,String> information) {
    if(information == null)
      throw new InvalidArgumentsException("information can not be null");

    Message message = Message.builder() 
      .setNotification(Notification.builder()
        .setTitle("$GOOG up 1.43% on the day")
        .setBody("$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.")
      .build())
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

  /**
   * how to add a Notification to the message
   * Add this to the builder()
   * 
   *  .setNotification(Notification.builder()
        .setTitle("$GOOG up 1.43% on the day")
        .setBody("$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.")
      .build())
   */

}
