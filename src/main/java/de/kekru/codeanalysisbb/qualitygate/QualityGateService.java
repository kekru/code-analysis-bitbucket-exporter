package de.kekru.codeanalysisbb.qualitygate;

import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketSeverity;
import de.kekru.codeanalysisbb.config.Config.QualityGateThreshold;
import de.kekru.codeanalysisbb.reporter.ReporterUtilsService;
import de.kekru.codeanalysisbb.serviceregistry.Service;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QualityGateService {

  private final ReporterUtilsService reporterUtilsService;

  public CreateInsightReport.RESULT getQualityGateResult(
      final List<BitbucketAnnotation> annotations,
      final QualityGateThreshold threshold) {

    Objects.requireNonNull(annotations, "annotations must not be null");
    Objects.requireNonNull(threshold, "threshold must not be null");

    final Map<BitbucketSeverity, List<BitbucketAnnotation>> severityMap = reporterUtilsService
        .groupBySeverity(annotations);

    final boolean qualityGateFailed =
        isFailed(BitbucketSeverity.LOW, severityMap, threshold.getLowCount()) ||
            isFailed(BitbucketSeverity.MEDIUM, severityMap, threshold.getMediumCount()) ||
            isFailed(BitbucketSeverity.HIGH, severityMap, threshold.getHighCount());

    return qualityGateFailed ? RESULT.FAIL : RESULT.PASS;
  }

  private boolean isFailed(BitbucketSeverity severity,
      Map<BitbucketSeverity, List<BitbucketAnnotation>> severityMap, Integer threshold) {
    final int countFindings = count(severity, severityMap);
    final int thresholdCleaned = cleanThreshold(threshold);
    return countFindings >= thresholdCleaned;
  }

  private int count(BitbucketSeverity severity,
      Map<BitbucketSeverity, List<BitbucketAnnotation>> severityMap) {
    return Optional.ofNullable(severityMap.get(severity)).map(List::size).orElse(0);
  }

  private int cleanThreshold(Integer threshold) {
    if (threshold == null || threshold < 0) {
      return Integer.MAX_VALUE;
    }

    if (threshold == 0) {
      return 1;
    }

    return threshold;
  }
}
