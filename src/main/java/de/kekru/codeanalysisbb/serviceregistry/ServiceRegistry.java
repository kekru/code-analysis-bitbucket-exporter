package de.kekru.codeanalysisbb.serviceregistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistry {

  private static final int MAX_CONSTRUCTOR_PARAMETER_COUNT = 1;
  private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
  private transient Map<Class<?>, Object> services = new HashMap<>();

  /**
   * Instantiiert ein Objekt der übergebenen Klasse, oder liefert es, falls es vorher schon erstellt
   * wurde. Wenn der Konstruktor Parameter erwartet, dann werden diese Objekte wiederrum rekursiv
   * instatiiert. (-> Dependency injection per Konstruktor-Parameter) Effektiv sind die Objekte
   * Singletons, da sie in der "services"-Map gecached werden. Objekte, die nicht einfach über den
   * Konstruktor erstellt werden, haben creator-Methoden in der "serviceCreators"-Map.
   */
  public <T> T get(Class<T> type) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Getting Service: " + type);
    }

    if (services.containsKey(type)) {
      // Service wurde bereits erzeugt -> dieser wird zurück geliefert
      // (alle Services sind somit Singletons)
      return (T) services.get(type);
    }

    Service serviceAnnotation = type.getAnnotation(Service.class);

    if (serviceAnnotation == null && !ServiceProvider.class.isAssignableFrom(type)) {
      throw new RuntimeException("Class is not annotated with @Service: " + type);
    }

    Object service;

    if (serviceAnnotation == null || serviceAnnotation.providedBy().equals(ServiceRegistry.class)) {

      // Dieser Service wird über seinen Konstruktor erzeugt
      service = instantiateServiceGeneric(type);

    } else {

      service = getServiceByProvider(type);
    }

    // Service in Cache setzen, um ihn später wieder nutzen zu können
    services.put(type, service);
    return (T) service;
  }

  private <T> Object getServiceByProvider(Class<T> type) {
    Class<?> providingService = type.getAnnotation(Service.class).providedBy();

    if (!ServiceProvider.class.isAssignableFrom(providingService)) {
      throw new RuntimeException(String
          .format("%s must implement %s, because it is referenced as providedBy in %s",
              providingService.getName(), ServiceProvider.class.getName(), type.getName()));
    }

    return ((ServiceProvider) get(providingService)).getService();
  }

  /**
   * Instanntiiert ein Object der übergebenen Klasse. Wenn der Konstruktor Parameter erwartet, dann
   * werden diese Objekte wiederrum rekursiv instatiiert.
   */
  private <T> T instantiateServiceGeneric(Class<T> type) {
    if (type.getConstructors().length > MAX_CONSTRUCTOR_PARAMETER_COUNT) {
      // Fehler werfen, wenn es mehrere Konstruktoren gibt
      throw new RuntimeException(
          "Type has multiple constructors. Dont know which to choose. " + type);
    }

    // Konstruktor für den Service holen
    Constructor<T> constructor = (Constructor<T>) type.getConstructors()[0];

    // Jeder Parameter des Konstruktors wird mit this.getService(...) instantiiert (-> Rekursion)
    List<?> parameters = Stream.of(constructor.getParameterTypes())
        .map(this::get)
        .collect(Collectors.toList());

    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Creating %s with parameters [%s]", type.getName(),
          Stream.of(constructor.getParameterTypes()).map(Class::getName)
              .collect(Collectors.joining(", "))));
    }

    try {
      // Service mit instantiierten Parameter-Services instatiieren
      // -> Dependency Injection über Konstruktor
      return constructor.newInstance(parameters.toArray());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException("Could not create " + type, e);
    }
  }
}
