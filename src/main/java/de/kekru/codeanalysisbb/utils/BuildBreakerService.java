package de.kekru.codeanalysisbb.utils;

import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.interf.ReporterConfig;
import de.kekru.codeanalysisbb.serviceregistry.Service;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class BuildBreakerService {

  private static final Logger LOG = LoggerFactory.getLogger(BuildBreakerService.class);
  private final Config config;

  public void failOnQualityGateIfRequested(List<BitbucketReport> reports) {

    List<String> failedReports = reports.stream()
        .filter(r -> RESULT.FAIL.equals(r.getResult()))
        .map(BitbucketReport::getReporterConfig)
        .map(ReporterConfig::getKey)
        .collect(Collectors.toList());

    if (!failedReports.isEmpty()) {
      String message = "There are failed reports: " + failedReports.stream()
          .collect(Collectors.joining(","));

      if (config.isBreakExecutionOnQualityGate()) {
        throw new CodeAnalysisBitbucketException(message);
      } else {
        LOG.debug(message);
      }
    }
  }
}
