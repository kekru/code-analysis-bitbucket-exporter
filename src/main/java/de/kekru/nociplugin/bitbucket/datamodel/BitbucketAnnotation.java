package de.kekru.nociplugin.bitbucket.datamodel;

import com.cdancy.bitbucket.rest.domain.insights.Annotation;
import com.cdancy.bitbucket.rest.domain.insights.Annotation.AnnotationSeverity;
import com.cdancy.bitbucket.rest.domain.insights.Annotation.AnnotationType;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@Builder
public class BitbucketAnnotation {

  @Builder.Default private String reportKey = UUID.randomUUID().toString();
  @Builder.Default private String externalId = UUID.randomUUID().toString();
  private int line;
  @Builder.Default private String link = "";
  private String message;
  private String path;
  @Builder.Default private BitbucketSeverity severity = BitbucketSeverity.LOW;
  @Builder.Default private BitbucketType type = BitbucketType.CODE_SMELL;

  @RequiredArgsConstructor
  public enum BitbucketSeverity {
    LOW(AnnotationSeverity.LOW),
    MEDIUM(AnnotationSeverity.MEDIUM),
    HIGH(AnnotationSeverity.HIGH);

    @Getter
    private final AnnotationSeverity annotationSeverity;
  }

  @RequiredArgsConstructor
  public enum BitbucketType {
    VULNERABILITY(AnnotationType.VULNERABILITY),
    CODE_SMELL(AnnotationType.CODE_SMELL),
    BUG(AnnotationType.BUG);

    @Getter
    private final AnnotationType annotationType;
  }

  public Annotation toAnnotation() {
    return Annotation.create(
        reportKey,
        externalId,
        line,
        link,
        message,
        path,
        severity.getAnnotationSeverity(),
        type.getAnnotationType()
    );
  }
}
