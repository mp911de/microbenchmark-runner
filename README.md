Microbenchmark Runner
=====================

[![](https://jitpack.io/v/mp911de/microbenchmark-runner.svg)](https://jitpack.io/#mp911de/microbenchmark-runner) [![CI](https://github.com/mp911de/microbenchmark-runner/actions/workflows/ci.yml/badge.svg)](https://github.com/mp911de/microbenchmark-runner/actions/workflows/ci.yml)

Microbenchmark Runner is a JUnit (JUnit 4.12/JUnit 5.5) extension to launch JMH benchmarks using JUnit directly by using existing JUnit integrations.

### Microbenchmark Runner is and what it isn't

This project is an aid during development to launch JMH benchmarks while developing these. Properly launching JMH benchmarks requires ideally the command line, an Uber-JAR and nothing else running to get good results.

During development time, we want quick turnaround times and an IDE that makes it simple to test-run benchmarks and that's what Microbenchmark Runner aims for.

**:warning: Do not use this launcher to run your benchmarks during CI or for actual measurements. Use it during development only. There are too many things that can blur actual results.** 

Here is a quick teaser of a what Microbenchmark Runner can do for you:

### JUnit 4.x

```java
@RunWith(Microbenchmark.class)
public class SimpleBenchmark {

    @State
    static class MyParameters {

        @Param({"1", "10", "100"}) // renders parametrized benchmarks as sub-tests
        String batchSize;
    }
    
    @Benchmark
    public void foo(MyParameters myParameters) {}
}
```

Decorate your JMH benchmark with `@RunWith(Microbenchmark.class)`. Now you're able to leverage your IDE to start JMH benchmarks without fighting the command line. 

### JUnit 5.10.x

```java
@Microbenchmark
public class SimpleBenchmark {

    @State
	static class MyParameters {

		@Param({"1", "10", "100"})  // renders parametrized benchmarks as sub-tests
		String batchSize;
	}

	@Benchmark
	public void foo(MyParameters myParameters) {
	}
}
```

**Compatibility matrix**

| MBR Version | JUnit 5 Version |
|-------------|----------------|
| 0.2.x       | 5.5+           |
| 0.3.x       | 5.8+           |
| 0.4.x       | 5.10+          |

Annotate your JMH benchmark with `@Microbenchmark`. Now you're able to leverage your IDE
to start JMH benchmarks without fighting the command line.

# Integrate it in your project

The easiest way is to
use [jitpack.io](https://jitpack.io/#mp911de/microbenchmark-runner/master) to include
Microbenchmark Runner in your project:

Add the following repository to your `pom.xml` (when using Maven):

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

and one of the dependencies:

```xml
<dependency>
    <groupId>com.github.mp911de.microbenchmark-runner</groupId>
    <artifactId>microbenchmark-runner-junit4</artifactId>
    <version>${version}.RELEASE</version>
</dependency>
```

```xml
<dependency>
    <groupId>com.github.mp911de.microbenchmark-runner</groupId>
    <artifactId>microbenchmark-runner-junit5</artifactId>
    <version>${version}.RELEASE</version>
</dependency>
```

# Configuration

You can configure the runner by using System Properties and Environment Variables to control behavior of the following parameters:

* `benchmarksEnabled` (`boolean`, defaults to `true`) Controls whether benchmarks should be executed. Setting `benchmarksEnabled=false` can be useful for conditional execution of benchmarks.
* `benchmarkReportDir` (`File`, defaults to `none`) Writes JMH benchmark results to this directory.
* `warmupIterations` (`integer`, defaults to `-1`) Global override of warmup iterations. Uses `@Warmup` or JMH defaults if set to `-1`
* `warmupTime` (`integer`, defaults to `-1`) Global override of warmup time. Uses `@Warmup` or JMH defaults if set to `-1`. 
* `measurementIterations` (`integer`, defaults to `-1`) Global override of measurement iterations. Uses `@Measurement` or JMH defaults if set to `-1`. 
* `measurementTime` (`integer`, defaults to `-1`) Global override of measurement time. Uses `@Measurement` or JMH defaults if set to `-1`. 
* `forks` (`integer`, defaults to `-1`) Global override of number of forks. Uses `@Fork` or JMH defaults if set to `-1`.
* `publishTo` URL to configure one or more result publishers. `jmh.mbr.core.ResultsWriterFactory` implementations are discovered using the Java ServiceLoader mechanism. See `Result Writers` for further details.

# Limitations

Microbenchmark Runner uses JUnit infrastructure to select Benchmarks to run and JUnit's progress reporting. Benchmarks are delegated to JMH's Runner Engine for execution. In consequence, JUnit annotations such as `@Before`, `@BeforeEach`, `@BeforeAll`, and others do not have any effect as they are not considered by the execution engine.

# Result Writers

Microbenchmark Runner comes with pluggable support for result output. Results are published using the `jmh.mbr.core.ResultsWriterFactory` SPI.

Bundled publishers in `microbenchmark-runner-extras` are:

* CSV reporting to System.out (enabled by default or with `-Djmh.mbr.report.publishTo=sysout`)
* CSV reporting to a file (enabled with `-Djmh.mbr.report.publishTo=csv:location/to/file`)
* Elasticsearch reporting (enabled with `-Djmh.mbr.report.publishTo=elasticsearch://[username]:[password]@[host]:[port]/`). The index name is controlled through an external property `jmh.mbr.project`.     

# Reporting Issues

Microbenchmark Runner uses GitHub’s integrated issue tracking system to record bugs and feature requests. If you want to raise an issue, please follow the recommendations below:

* Before you log a bug, please search the issue tracker to see if someone has already reported the problem.
* If the issue doesn’t already exist, create a new issue.
* Please provide as much information as possible with the issue report, we like to know the version of Microbenchmark Runner that you are using, as well as your Operating System and JVM version.
* If you need to paste code, or include a stack trace use Markdown \`\`\` escapes before and after your text.
* If possible try to create a test-case or project that replicates the issue. 

# Building from Source

If you want to try out the latest and greatest, Microbenchmark Runner can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need JDK 1.8.

```
$ ./mvnw clean install
```

# License

Microbenchmark Runner is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
