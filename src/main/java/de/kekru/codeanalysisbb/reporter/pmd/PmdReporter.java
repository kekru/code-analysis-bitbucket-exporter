package de.kekru.codeanalysisbb.reporter.pmd;

import static com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT.FAIL;
import static com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT.PASS;

import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketSeverity;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketType;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.generated.pmd.Pmd;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.serviceregistry.Service;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Service
@RequiredArgsConstructor
public class PmdReporter implements Reporter {

  private final Config config;

  @Override
  public BitbucketReport getBitbucketReport(){

    List<BitbucketAnnotation> annotations = getBitbucketAnnotations();

    final long hasHighSeverityErrors = annotations.stream()
        .map(BitbucketAnnotation::getSeverity)
        .filter(severity -> BitbucketSeverity.HIGH.equals(severity))
        .count();

    return BitbucketReport.builder()
        .reporterConfig(config.getReporter().getPmd())
        .details("PMD Report")
        .annotations(annotations)
        .result(hasHighSeverityErrors == 0 ? PASS : FAIL)
        .link("https://pmd.github.io")
        .logoUrl("https://pmd.github.io/img/pmd_logo.png")
        .build();
  }

  private List<BitbucketAnnotation> getBitbucketAnnotations() {
    return config.getReporter().getPmd().getInputXmls()
        .stream()
        .map(File::new)
        .map(this::readPmd)
        .flatMap(pmd -> pmd.getFile().stream())
        .flatMap(pmdFile -> toAnnotation(pmdFile).stream())
        .collect(Collectors.toList());
  }

  private List<BitbucketAnnotation> toAnnotation(de.kekru.codeanalysisbb.generated.pmd.File pmdFile) {
    return pmdFile.getViolation()
        .stream()
        .map(v ->
            BitbucketAnnotation.builder()
                .line(v.getEndline().intValue())
                .message(createMessage(v.getRule(), v.getValue()))
                .path(pmdFile.getName())
                .severity(priorityToSeverity(v.getPriority()))
                .type(rulesetToType(v.getRuleset()))
                .link(v.getExternalInfoUrl())
                .build()
        )
        .collect(Collectors.toList());
  }

  private String createMessage(String rule, String value) {
    return String.format("%s (%s)", StringUtils.trimToEmpty(value), StringUtils.trimToEmpty(rule))
        .replace("\r", "")
        .replace("\n", "");
  }

  private BitbucketSeverity priorityToSeverity(String priority) {
    switch (priority) {
      case "1": // fall through
      case "2":
        return BitbucketSeverity.HIGH;
      case "3":
        return BitbucketSeverity.MEDIUM;
      default:
        return BitbucketSeverity.LOW;
    }
  }

  private BitbucketType rulesetToType(String ruleset) {
    ruleset = ruleset.trim().toLowerCase();
    switch (ruleset) {
      case "best practices": // fall through
      case "code style":
        return BitbucketType.CODE_SMELL;
      case "security":
        return BitbucketType.VULNERABILITY;
      default:
        return BitbucketType.BUG;
    }
  }

  private Pmd readPmd(File reportFile) {
    if (!reportFile.exists()) {
      throw new RuntimeException("File not found: " + reportFile);
    }

    try {
      return (Pmd) JAXBContext.newInstance(Pmd.class)
          .createUnmarshaller()
          .unmarshal(reportFile);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read PMD report: " + reportFile, e);
    }
  }
}
