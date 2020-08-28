package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates;

import java.util.List;
import lombok.Data;

@Data
public class SonarProjectStatus {

  private String status;
  private List<SonarCondition> conditions;
  private List<SonarPeriod> periods;
  private String ignoredConditions;
}
