package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain;

import lombok.Data;

@Data
public class SonarTextRange {

  private int startLine;
  private int endLine;
  private int startOffset;
  private int endOffset;
}
