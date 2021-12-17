package systems.comodal.collision.benchmarks;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
@Threads(Threads.MAX)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 2)
@Fork(1)
public class LoadMovingZipfBenchmark {

  private final Long[] keys = new Long[LoadStaticZipfBenchmark.SIZE];
  private final ScrambledZipfGenerator generator = new ScrambledZipfGenerator(
      LoadStaticZipfBenchmark.ITEMS);
  @Param({
          "Cache2k",
          "Caffeine",
          "Collision",
          "Collision_Packed",
          "Collision_Aggressive"
  })
  private LoadStaticZipfBenchmark.BenchmarkFunctionFactory cacheType;
  private UnaryOperator<Long> benchmarkFunction;

  @Setup(Level.Iteration)
  public void setup() {
    if (benchmarkFunction == null) {
      this.benchmarkFunction = cacheType.create();
    }
    IntStream.range(0, keys.length).parallel().forEach(i -> keys[i] = generator.nextValue());
  }

  @Benchmark
  public Long getSpread(final LoadStaticZipfBenchmark.ThreadState threadState) {
    return benchmarkFunction.apply(keys[threadState.index++ & LoadStaticZipfBenchmark.MASK]);
  }
}
