package de.kekru.codeanalysisbb.reporter.spotbugs;

import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketSeverity;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketType;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.SpotbugsConfig;
import de.kekru.codeanalysisbb.generated.spotbugs.BugCollection;
import de.kekru.codeanalysisbb.generated.spotbugs.BugCollection.BugInstance.Class;
import de.kekru.codeanalysisbb.generated.spotbugs.SourceLine;
import de.kekru.codeanalysisbb.qualitygate.QualityGateService;
import de.kekru.codeanalysisbb.reporter.ReporterUtilsService;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.serviceregistry.Service;
import de.kekru.codeanalysisbb.utils.CodeAnalysisBitbucketException;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Service
@RequiredArgsConstructor
public class SpotbugsReporter implements Reporter {

  private final Config config;
  private final ReporterUtilsService reporterUtils;
  private final QualityGateService qualityGateService;

  @Override
  public BitbucketReport getBitbucketReport() {
    SpotbugsConfig spotbugsConfig = config.getReporter().getSpotbugs();

    final List<BitbucketAnnotation> annotations = getBitbucketAnnotations();

    return BitbucketReport.builder()
        .reporterConfig(spotbugsConfig)
        .details(reporterUtils.getDetailsStringFromAnnotations(annotations))
        .annotations(annotations)
        .result(qualityGateService.getQualityGateResult(annotations, spotbugsConfig.getQualityGate()))
        .link("https://spotbugs.github.io")
        .logoUrl("https://spotbugs.github.io/images/logos/spotbugs_icon_only_zoom_256px.png")
        .build();
  }

  private List<BitbucketAnnotation> getBitbucketAnnotations() {
    return config.getReporter().getSpotbugs().getInputXmls()
        .stream()
        .map(File::new)
        .map(this::readSpotbugs)
        .flatMap(this::toAnnotations)
        .collect(Collectors.toList());
  }

  private Stream<BitbucketAnnotation> toAnnotations(BugCollection bugCollection) {
    final List<String> sourceDirs = bugCollection.getProject().getSrcDir();
    return bugCollection
        .getBugInstance()
        .stream()
        .flatMap(bug -> toAnnotations(bug, sourceDirs));
  }

  private Stream<BitbucketAnnotation> toAnnotations(BugCollection.BugInstance bug,
      List<String> sourceDirs) {

    final Optional<SourceLine> sourceLineOptional = bug.getClazzOrTypeOrMethod()
        .stream()
        .filter(classOrTypeOrMethod ->
            classOrTypeOrMethod instanceof Class)
        .map(Class.class::cast)
        .map(Class::getSourceLine)
        .findFirst();

    if (!sourceLineOptional.isPresent()) {
      return Stream.empty();
    }

    final SourceLine sourceLine = sourceLineOptional.get();
    final int line = Optional.ofNullable(sourceLine.getEnd()).orElse(0);

    /**
     * Spotbugs scans class files. So it can happen that there are multiple
     * sourcedirs and we dont know where the sources come from
     * we just create annotations for all dirs and bitbucket will filter files,
     * that have a wrong path
     */
    return sourceDirs.stream()
        .map(dir ->

            BitbucketAnnotation.builder()
                .line(line)
                .message(StringUtils.trimToEmpty(bug.getLongMessage()))
                .path(dir + "/" + sourceLine.getSourcepath())
                .severity(prioritytoSeverity(bug.getPriority()))
                .type(categoryToType(bug.getCategory()))
                .link(categoryToLink(bug.getCategory()))
                .build()
        );
  }

  /**
   * See https://spotbugs.readthedocs.io/en/stable/filter.html
   */
  private BitbucketSeverity prioritytoSeverity(short priority) {

    switch (priority) {
      case 1: return BitbucketSeverity.HIGH;
      case 2: return BitbucketSeverity.MEDIUM;
      case 3:
      default: return BitbucketSeverity.LOW;
    }
  }

  /**
   * See https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html
   */
  private BitbucketType categoryToType(String category) {
    category = category.trim();
    switch (category) {
      case "BAD_PRACTICE":
      case "EXPERIMENTAL":
      case "STYLE":
        return BitbucketType.CODE_SMELL;
      case "MALICIOUS_CODE":
      case "SECURITY":
        return BitbucketType.VULNERABILITY;
      case "CORRECTNESS":
      case "I18N":
      case "NOISE":
      case "PERFORMANCE":
      case "MT_CORRECTNESS":
      default:
        return BitbucketType.BUG;
    }
  }

  /**
   * See https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html
   */
  private String categoryToLink(String category) {
    final String linkBase = "https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html";

    category = category.trim();
    switch (category) {
      case "BAD_PRACTICE":
        return linkBase + "#bad-practice-bad-practice";
      case "EXPERIMENTAL":
        return linkBase + "#experimental-experimental";
      case "STYLE":
        return linkBase + "#dodgy-code-style";
      case "MALICIOUS_CODE":
        return linkBase + "#malicious-code-vulnerability-malicious-code";
      case "SECURITY":
        return linkBase + "#security-security";
      case "CORRECTNESS":
        return linkBase + "correctness-correctness";
      case "I18N":
        return linkBase + "#internationalization-i18n";
      case "NOISE":
        return linkBase + "#bogus-random-noise-noise";
      case "PERFORMANCE":
        return linkBase + "#performance-performance";
      case "MT_CORRECTNESS":
        return linkBase + "#multithreaded-correctness-mt-correctness";
      default:
        return linkBase;
    }
  }


  private BugCollection readSpotbugs(File reportFile) {
    if (!reportFile.exists()) {
      throw new CodeAnalysisBitbucketException("File not found: " + reportFile);
    }

    try {
      return (BugCollection) JAXBContext.newInstance(BugCollection.class)
          .createUnmarshaller()
          .unmarshal(reportFile);
    } catch (Exception e) {
      throw new CodeAnalysisBitbucketException("Failed to read Spotbugs report: " + reportFile, e);
    }
  }
}
