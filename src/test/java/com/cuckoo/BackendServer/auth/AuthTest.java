package com.cuckoo.BackendServer.auth;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;

import com.cuckoo.BackendServer.models.usertype.UserType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AuthTest {

  private static String host = "http://localhost:";
  private static String loginEndpoint = "/login/authenticate";
  private static String userEndpoint = "/login/user-info";

  @LocalServerPort
	private int port;


  @Test
  public void logsInWithSuccess() throws URISyntaxException{
    RestTemplate restTemplate = new RestTemplate();
    String baseUrl = host + this.port + loginEndpoint;
    UserType user = new UserType();
    user.setEmail("david.dm2008@gmail.com");
    user.setPassword("aPassword");
    
    URI uri = new URI(baseUrl);
    HttpEntity<UserType> entity = new HttpEntity<>(user);

    ResponseEntity<Void> result = restTemplate.exchange(uri, 
                                          HttpMethod.POST,entity, Void.class);


    assertEquals(HttpStatus.OK, result.getStatusCode());
    String token = result.getHeaders().get("Authorization").get(0);
    assertEquals("Bearer ", token.substring(0, 7));
    assertEquals(true, token.length() > 7);
  }

  @Test
  public void loginFailsWithPassword() throws URISyntaxException{
    RestTemplate restTemplate = new RestTemplate();
    String baseUrl = host + this.port + loginEndpoint;
    UserType user = new UserType();
    user.setEmail("david.dm2008@gmail.com");
    user.setPassword("aPasswrd");
    
    URI uri = new URI(baseUrl);
    HttpEntity<UserType> entity = new HttpEntity<>(user);

    RestClientException exception = assertThrows(RestClientException.class, () -> {
      restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
    });

    assertEquals("403",exception.getMessage().substring(0, 3));
  }

  @Test
  public void loginFailsWithEmail() throws URISyntaxException{
    RestTemplate restTemplate = new RestTemplate();
    String baseUrl = host + this.port + loginEndpoint;
    UserType user = new UserType();
    user.setEmail("david.d2008@gmail.com");
    user.setPassword("aPassword");
    
    URI uri = new URI(baseUrl);
    HttpEntity<UserType> entity = new HttpEntity<>(user);

    RestClientException exception = assertThrows(RestClientException.class, () -> {
      restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
    });

    assertEquals("403",exception.getMessage().substring(0, 3));
  }


  @Test
  public void accessEndpointWithSuccess() throws URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    String baseUrl = host + this.port + loginEndpoint;
    String baseUserUrl = host + this.port + userEndpoint;
    UserType user = new UserType();
    user.setEmail("david.dm2008@gmail.com");
    user.setPassword("aPassword");
    
    URI uri = new URI(baseUrl);
    HttpEntity<UserType> entity = new HttpEntity<>(user);

    ResponseEntity<Void> result = restTemplate.exchange(uri, 
                                          HttpMethod.POST,entity, Void.class);
    
    String token = result.getHeaders().get("Authorization").get(0);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);

    uri = new URI(baseUserUrl);
    HttpEntity<?> headers2 = new HttpEntity<>(headers);
    
    ResponseEntity<UserType> userResponse = restTemplate.exchange(uri,
                                                            HttpMethod.GET, 
                                                            headers2,
                                                            UserType.class);
    UserType userRes = userResponse.getBody();
    assertEquals("david.dm2008@gmail.com", userRes.getEmail());
    assertEquals("David", userRes.getFirstName());
    assertEquals("Martins", userRes.getLastName());
  }

  @Test
  public void accessEndpointWithDiferentAlgorithm() throws URISyntaxException {
    String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJzdWIiOiIwZjEwOWI5ZS02MTM3LTQ1MzctOGVhYy0zNjg1NTMyMTU4NzUiLCJleHAiOjMzMTk1NzMzMjE0LCJpYXQiOjE2MDY2NTU3NjcsImp0aSI6IjRjNDIxMzNjLTlkZTUtNGIxZS1iYzE5LWQ5YjFhMGFlZDU0YiJ9.";

    String baseUserUrl = host + this.port + userEndpoint;
    HttpHeaders headers = new HttpHeaders();
    RestTemplate restTemplate = new RestTemplate();
    headers.set("Authorization", token);

    URI uri = new URI(baseUserUrl);
    HttpEntity<?> headers2 = new HttpEntity<>(headers);
    
    RestClientException exception = assertThrows(RestClientException.class, () -> {
            restTemplate.exchange(uri,
            HttpMethod.GET, 
            headers2,
            UserType.class);
    });

    //spring apparently returns 403
    assertEquals("403",exception.getMessage().substring(0, 3));
  }

  @Test
  public void accessEndpointWithoutToken() throws URISyntaxException {
    String baseUserUrl = host + this.port + userEndpoint;
    HttpHeaders headers = new HttpHeaders();
    RestTemplate restTemplate = new RestTemplate();
    headers.set("Authorization", "");

    URI uri = new URI(baseUserUrl);
    HttpEntity<?> headers2 = new HttpEntity<>(headers);
    
    RestClientException exception = assertThrows(RestClientException.class, () -> {
            restTemplate.exchange(uri,
            HttpMethod.GET, 
            headers2,
            UserType.class);
    });

    //spring apparently returns 403
    assertEquals("403",exception.getMessage().substring(0, 3));
  }

  @Test
  public void accessEndpointWithDifferentSignature() throws URISyntaxException{
    //token will be valid for a 1000 years, but is signed with a different key
    String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwZjEwOWI5ZS02MTM3LTQ1MzctOGVhYy0zNjg1NTMyMTU4NzUiLCJleHAiOjE2MDcyODU5NTAsImlhdCI6MTYwNjY1NTc2NywianRpIjoiNGM0MjEzM2MtOWRlNS00YjFlLWJjMTktZDliMWEwYWVkNTRiIn0.qL4DC7mYKb51Un7NWtED2fxJ3fizMAMveaLNo2tdkUs";

    String baseUserUrl = host + this.port + userEndpoint;
    HttpHeaders headers = new HttpHeaders();
    RestTemplate restTemplate = new RestTemplate();
    headers.set("Authorization", token);

    URI uri = new URI(baseUserUrl);
    HttpEntity<?> headers2 = new HttpEntity<>(headers);
    
    RestClientException exception = assertThrows(RestClientException.class, () -> {
            restTemplate.exchange(uri,
            HttpMethod.GET, 
            headers2,
            UserType.class);
    });

    //spring apparently returns 403
    assertEquals("403",exception.getMessage().substring(0, 3));
  }

}
