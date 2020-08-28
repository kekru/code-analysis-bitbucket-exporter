package de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig;

import com.google.common.net.HttpHeaders;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import javax.inject.Singleton;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpRequestFilter;

@Singleton
public class SonarqubeAuthenticationFilter implements HttpRequestFilter {

  private final String accessTokenBase64;

  public SonarqubeAuthenticationFilter(String accessToken) {
    try {
      this.accessTokenBase64 = new String(
          Base64.getEncoder().encode((accessToken + ":").getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public HttpRequest filter(final HttpRequest request) throws HttpException {
    return request.toBuilder()
        .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + accessTokenBase64)
        .build();
  }
}
