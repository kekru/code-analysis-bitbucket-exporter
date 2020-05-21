package de.kekru.codeanalysisbb.serviceregistry;

public interface ServiceProvider<T> {
  T getService();
}
