package de.kekru.codeanalysisbb.reporter.sonarqube.api;

import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarComponent;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarIssue;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SonarIssuesAndComponents {

  private final List<SonarIssue> issues;
  private final List<SonarComponent> components;
}
