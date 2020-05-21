package de.kekru.nociplugin.config.interf;

import de.kekru.nociplugin.reporter.interf.Reporter;

public interface ReporterConfig {

  boolean isEnabled();
  String getKey();
  String getTitle();
  String getReporter();
  Class<? extends Reporter> getReporterService();
}
