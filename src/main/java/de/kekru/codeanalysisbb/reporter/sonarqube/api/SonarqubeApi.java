package de.kekru.codeanalysisbb.reporter.sonarqube.api;

import java.io.Closeable;
import org.jclouds.rest.annotations.Delegate;

public interface SonarqubeApi extends Closeable {

    @Delegate
    QualityGatesApi qualityGatesApi();

    @Delegate
    IssuesApi issuesApi();
}
