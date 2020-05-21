package de.kekru.nociplugin.serviceregistry;

public interface ServiceProvider<T> {
  T getService();
}
