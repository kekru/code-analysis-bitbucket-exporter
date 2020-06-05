package de.kekru.codeanalysisbb.utils;

public class CodeAnalysisBitbucketException extends RuntimeException {

  public CodeAnalysisBitbucketException(final String message) {
    super(message);
  }

  public CodeAnalysisBitbucketException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
