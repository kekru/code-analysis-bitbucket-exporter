package de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig;

import com.google.inject.AbstractModule;
import lombok.Data;

@Data
public class SonarqubeAuthenticationModule extends AbstractModule {

    private final String accessToken;

    @Override
    protected void configure() {
        bind(SonarqubeAuthenticationFilter.class)
            .toProvider(() -> new SonarqubeAuthenticationFilter(accessToken));
    }
}
