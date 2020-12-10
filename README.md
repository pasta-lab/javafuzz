# Javafuzz: coverage-guided fuzz testing for Java

Javafuzz is coverage-guided [fuzzer](https://developer.mozilla.org/en-US/docs/Glossary/Fuzzing) 
for testing Java packages.

Fuzzing for safe languages like nodejs is a powerful strategy for finding bugs like unhandled exceptions, logic bugs,
security bugs that arise from both logic bugs and Denial-of-Service caused by hangs and excessive memory usage.

Fuzzing can be seen as a powerful and efficient strategy in real-world software in addition to classic unit-tests.

## Usage

### Fuzz Target

The first step is to implement the following function (also called a fuzz target):

```java
public class FuzzExample extends AbstractFuzzTarget {
    public void fuzz(byte[] data) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            // ignore as we expect this exception
        }
    }
}
```

Features of the fuzz target:

* Javafuzz will call the fuzz target in an infinite loop with random data (according to the coverage guided algorithm) passed to `buf`.
* The function must catch and ignore any expected only (dont catch Exception) exceptions that arise when passing invalid input to the tested package.
* The fuzz target must call the test function/library with with the passed buffer or a transformation on the test buffer 
if the structure is different or from different type.
* Fuzz functions can also implement application level checks to catch application/logical bugs - For example: 
decode the buffer with the testable library, encode it again, and check that both results are equal. To communicate the results
the result/bug the function should throw an exception.
* Javafuzz will report any unhandled exceptions as crashes as well as inputs that hit the memory limit specified to javafuzz
or hangs/they run more the the specified timeout limit per testcase.

### Installing
Add this to your `pom.xml`

```yaml
  <dependencies>
    <dependency>
      <groupId>com.gitlab.javafuzz</groupId>
      <artifactId>javafuzz-maven-plugin</artifactId>
      <version>1.24</version>
    </dependency>    
  </dependencies>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>com.gitlab.javafuzz</groupId>
          <artifactId>javafuzz-maven-plugin</artifactId>
          <version>1.24</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>


<repositories>
  <repository>
    <id>gitlab-maven</id>
    <url>https://gitlab.com/api/v4/projects/19871573/packages/maven</url>
  </repository>
</repositories>

<pluginRepositories>
  <pluginRepository>
    <id>gitlab-maven</id>
    <url>https://gitlab.com/api/v4/projects/19871573/packages/maven</url>
  </pluginRepository>
</pluginRepositories>

<distributionManagement>
  <repository>
    <id>gitlab-maven</id>
    <url>https://gitlab.com/api/v4/projects/19871573/packages/maven</url>
  </repository>

  <snapshotRepository>
    <id>gitlab-maven</id>
    <url>https://gitlab.com/api/v4/projects/19871573/packages/maven</url>
  </snapshotRepository>
</distributionManagement>
```


### Running

The next step is to javafuzz with your fuzz target function


```bash
docker run -it maven:3.6.3-jdk-11 /bin/bash
git clone https://gitlab.com/gitlab-org/security-products/demos/coverage-fuzzing/javafuzz-fuzzing-example
cd javafuzz-fuzzing-example
mvn install
wget -O jacocoagent.jar https://gitlab.com/gitlab-org/security-products/analyzers/fuzzers/javafuzz/-/raw/master/javafuzz-maven-plugin/src/main/resources/jacocoagent-exp.jar
MAVEN_OPTS="-javaagent:jacocoagent.jar" mvn javafuzz:fuzz -DclassName=com.gitlab.javafuzz.examples.FuzzParseComplex
```


```bash
...
# Output:
#135 NEW     cov: 25419 corp: 118 exec/s: 0 rss: 62 MB
#139 NEW     cov: 25421 corp: 119 exec/s: 0 rss: 62 MB
#140 NEW     cov: 25432 corp: 120 exec/s: 0 rss: 62 MB
#143 NEW     cov: 25433 corp: 121 exec/s: 0 rss: 62 MB
#185 NEW     cov: 25434 corp: 122 exec/s: 0 rss: 62 MB
#190 NEW     cov: 25435 corp: 123 exec/s: 0 rss: 62 MB
#192 NEW     cov: 25437 corp: 124 exec/s: 0 rss: 62 MB
#203 NEW     cov: 25438 corp: 125 exec/s: 0 rss: 63 MB
#245 NEW     cov: 25439 corp: 126 exec/s: 1000 rss: 63 MB
#293 NEW     cov: 25440 corp: 127 exec/s: 1000 rss: 63 MB
#310 NEW     cov: 25441 corp: 128 exec/s: 2000 rss: 63 MB
#4187 NEW     cov: 25442 corp: 129 exec/s: 2000 rss: 87 MB
#13285 NEW     cov: 25443 corp: 130 exec/s: 4000 rss: 69 MB
java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
	at com.gitlab.javafuzz.examples.App.parseComplex(App.java:11)
	at com.gitlab.javafuzz.examples.FuzzParseComplex.fuzz(FuzzParseComplex.java:13)
	at com.gitlab.javafuzz.core.Fuzzer.start(Fuzzer.java:72)
	at com.gitlab.javafuzz.maven.FuzzGoal.execute(FuzzGoal.java:63)
...
```

This example quickly finds an out-of-bound-array in the parsecomplex example program.

### Corpus

Javafuzz will generate and test various inputs in an infinite loop. `corpus` is optional directory and will be used to
save the generated testcases so later runs can be started from the same point and provided as seed corpus.

Javafuzz can also start with an empty directory (i.e no seed corpus) though some valid test-cases in the seed corpus
may speed up the fuzzing substantially. you can pass a seed directory via `-Ddirs=corpus_dir`

Javafuzz tries to mimic some of the arguments and output style from [libFuzzer](https://llvm.org/docs/LibFuzzer.html).

More fuzz targets examples (for real and popular libraries) are located under the [examples](https://gitlab.com/gitlab-org/security-products/demos/coverage-fuzzing/javafuzz-fuzzing-example/-/tree/master/src/test/java/com/gitlab/javafuzz/examples) directory and
bugs that were found using those targets are listed in the trophies section.


### Coverage

For coverage instrumentation we use [JaCoCo library](https://github.com/jacoco/jacoco)


## Other languages

Currently this library also exists for the following languages:
* javascript [jsfuzz](https://github.com/fuzzitdev/jsfuzz)
* python via [pythonfuzz](https://github.com/fuzzitdev/pythonfuzz)

## Credits & Acknowledgments

Javafuzz is a port of [fuzzitdev/jsfuzz](https://github.com/fuzzitdev/jsfuzz).

Which in turn based based on [go-fuzz](https://github.com/dvyukov/go-fuzz) originally developed by [Dmitry Vyukov's](https://twitter.com/dvyukov).
Which is in turn heavily based on [Michal Zalewski](https://twitter.com/lcamtuf) [AFL](http://lcamtuf.coredump.cx/afl/).

Another solid fuzzing with coverage library for java is [JQF](https://github.com/rohanpadhye/jqf) but is more
focused on semantic fuzzing (i.e structure aware) and thus depends on quickcheck. JavaFuzz does not 
depends on any framework an focuses on mutations producing buffer array and using coverage to find more bugs.

## Contributions

Contributions are welcome!:) There are still a lot of things to improve, and tests and features to add. We will slowly post those in the
issues section. Before doing any major contribution please open an issue so we can discuss and help guide the process before
any unnecessary work is done.

