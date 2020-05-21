package de.kekru.codeanalysisbb.config.interf;

import de.kekru.codeanalysisbb.reporter.interf.Reporter;

public interface ReporterConfig {

  boolean isEnabled();
  String getKey();
  String getTitle();
  String getReporter();
  Class<? extends Reporter> getReporterService();
}
