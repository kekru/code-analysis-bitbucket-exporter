package de.kekru.codeanalysisbb.config.interf;

import de.kekru.codeanalysisbb.config.Config.QualityGateThreshold;
import java.util.List;

public interface LocalXmlReporterConfig extends ReporterConfig {

  List<String> getInputXmls();
  QualityGateThreshold getQualityGate();
}
