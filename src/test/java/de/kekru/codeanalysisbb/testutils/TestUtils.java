package de.kekru.codeanalysisbb.testutils;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

public class TestUtils {

  public static Stubber doAnswer(Runnable mockImplementation) {
    return Mockito.doAnswer(toAnswer(mockImplementation));
  }

  public static <A> Stubber doAnswer(Supplier<A> mockImplementation) {
    return Mockito.doAnswer(toAnswer(mockImplementation));
  }

  public static <A> Stubber doAnswer(Consumer<A> mockImplementation) {
    return Mockito.doAnswer(toAnswer(mockImplementation));
  }

  public static <A, R> Stubber doAnswer(Function<A, R> mockImplementation) {
    return Mockito.doAnswer(toAnswer(mockImplementation));
  }

  public static <A, B, R> Stubber doAnswer(BiFunction<A, B, R> mockImplementation) {
    return Mockito.doAnswer(toAnswer(mockImplementation));
  }


  public static Answer<Void> toAnswer(Runnable mockImplementation) {
    return invocation -> {
      mockImplementation.run();
      return null;
    };
  }

  public static <A> Answer<A> toAnswer(Supplier<A> mockImplementation) {
    return invocation -> mockImplementation.get();
  }

  public static <A> Answer<Void> toAnswer(Consumer<A> mockImplementation) {
    return invocation -> {
      mockImplementation.accept((A) invocation.getArguments()[0]);
      return null;
    };
  }

  public static <A, R> Answer<R> toAnswer(Function<A, R> mockImplementation) {
    return invocation -> mockImplementation.apply((A) invocation.getArguments()[0]);
  }

  public static <A, B, R> Answer<R> toAnswer(BiFunction<A, B, R> mockImplementation) {
    return invocation -> mockImplementation.apply(
        (A) invocation.getArguments()[0],
        (B) invocation.getArguments()[1]);
  }

}
