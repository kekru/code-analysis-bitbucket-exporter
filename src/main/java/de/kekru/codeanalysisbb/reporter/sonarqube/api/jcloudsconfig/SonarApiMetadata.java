package de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig;

import com.google.auto.service.AutoService;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarqubeApi;
import java.net.URI;
import java.util.Properties;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.rest.internal.BaseHttpApiMetadata;

@AutoService(ApiMetadata.class)
public class SonarApiMetadata extends BaseHttpApiMetadata<SonarqubeApi> {

  @Override
  public Builder toBuilder() {
    return new Builder().fromApiMetadata(this);
  }

  protected SonarApiMetadata(final Builder builder) {
    super(builder);
  }

  public static Properties defaultProperties() {
    return BaseHttpApiMetadata.defaultProperties();
  }

  public static class Builder extends BaseHttpApiMetadata.Builder<SonarqubeApi, Builder> {

    public Builder() {
      super(SonarqubeApi.class);
      id("sonarqube").name("Sonarqube Web API")
          .identityName("")
          .documentation(URI.create("https://sonarcloud.io/web_api"))
          .defaultEndpoint("https://127.0.0.1:443")
          .defaultProperties(SonarApiMetadata.defaultProperties());
    }

    @Override
    public SonarApiMetadata build() {
      return new SonarApiMetadata(this);
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public Builder fromApiMetadata(final ApiMetadata in) {
      return this;
    }
  }
}
