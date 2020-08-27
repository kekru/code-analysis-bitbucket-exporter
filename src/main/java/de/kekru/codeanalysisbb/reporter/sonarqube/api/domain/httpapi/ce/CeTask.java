package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.ce;

import lombok.Data;

@Data
public class CeTask {

   private String id;
   private String type;
   private String componentId;
   private String componentKey;
   private String componentName;
   private String componentQualifier;
   private String analysisId;
   private String status;
   private String submittedAt;
   private String submitterLogin;
   private String startedAt;
   private String executedAt;
   private int executionTimeMs;
   private boolean logs;
   private boolean hasScannerContext;
   private String organization;
   private int warningCount;
}
