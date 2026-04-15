package com.theMs.sakany.shared.firebase;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FcmService {

  public void sendNotification(String token, String title, String body, Map<String, String> data) {
    try {
      Message message = Message.builder()
          .setToken(token)
          .setNotification(Notification.builder()
              .setTitle(title)
              .setBody(body)
              .build())
          .putAllData(data != null ? data : Map.of())
          .build();

      String response = FirebaseMessaging.getInstance().send(message);
      System.out.println("Successfully sent message: " + response);
    } catch (FirebaseMessagingException e) {
      System.err.println("Error sending FCM message: " + e.getMessage());
      throw new RuntimeException("Failed to send notification", e);
    }
  }

  public void sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, String> data) {
    try {
      MulticastMessage message = MulticastMessage.builder()
          .addAllTokens(tokens)
          .setNotification(Notification.builder()
              .setTitle(title)
              .setBody(body)
              .build())
          .putAllData(data != null ? data : Map.of())
          .build();

      BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
      System.out.println("Successfully sent " + response.getSuccessCount() + " messages");

      if (response.getFailureCount() > 0) {
        System.err.println("Failed to send " + response.getFailureCount() + " messages");
      }
    } catch (FirebaseMessagingException e) {
      System.err.println("Error sending multicast FCM message: " + e.getMessage());
      throw new RuntimeException("Failed to send notifications", e);
    }
  }

  public void sendToTopic(String topic, String title, String body, Map<String, String> data) {
    try {
      Message message = Message.builder()
          .setTopic(topic)
          .setNotification(Notification.builder()
              .setTitle(title)
              .setBody(body)
              .build())
          .putAllData(data != null ? data : Map.of())
          .build();

      String response = FirebaseMessaging.getInstance().send(message);
      System.out.println("Successfully sent message to topic: " + response);
    } catch (FirebaseMessagingException e) {
      System.err.println("Error sending FCM message to topic: " + e.getMessage());
      throw new RuntimeException("Failed to send notification to topic", e);
    }
  }
}
