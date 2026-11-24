# Operating Systems in Java

Java code examples for the undergraduate **Operating Systems** course at Giresun University, Department of Computer Engineering. Most of the code on the course slides is C and lives in [operating-systems-labs-en](https://github.com/sercankulcu/operating-systems-labs-en); the slides that use Java are collected here. Each `ChapterNN/src` folder holds the Java code of chapter NN as small runnable programs. Class, method and variable names are the ones used in the English edition of the course (lecture notes and slides), so each program can be read side by side with them.

Course page, lecture notes, slides and study questions: <https://sercankulcu.github.io/teaching/operating-systems/?lang=en>

The same programs with Turkish identifiers, for the Turkish edition of the course, are in [operating-systems-java](https://github.com/sercankulcu/operating-systems-java).

## Chapters

| Folder | Chapter | Programs |
|---|---|---|
| `Chapter04` | Threads | `Thread`, `start` and `join`; virtual threads (Java 21); `ExecutorService`; fork/join sum; thread-local storage; observing a race condition |
| `Chapter06` | Synchronization | Peterson's algorithm with `volatile`, `CyclicBarrier`, `AtomicInteger` versus `synchronized`, `synchronized`/`wait`/`notifyAll`, a bounded buffer |
| `Chapter07` | Deadlocks | A deadlock and its detection with `ThreadMXBean`, lock order hidden by encapsulation, `tryLock` with back-off, lock ordering |
| `Chapter12` | Multiprocessor Systems | Ticket lock |

The other chapters have no Java code on the slides.

## Running the examples

Requires **JDK 21**. Every file is a standalone program with a `main` method in the default package, and no program waits for keyboard input. Programs that demonstrate a deadlock detect it and finish. To compile and run one chapter:

```bash
cd Chapter06/src
javac -d ../bin *.java
java -cp ../bin Peterson
```

Each folder is also an Eclipse project (`File > Import > Existing Projects into Workspace`).

## Automatic check

`ci/run_examples.py` compiles every `ChapterNN` folder with JDK 21, runs every program and compares its output with the expected output at the end of the file. GitHub Actions runs it on every push. A program whose output changes from run to run (timings, thread order) says so above its expected output and only has to run. To run the check locally:

```bash
python ci/run_examples.py             # every chapter
python ci/run_examples.py Chapter06   # one chapter
```

## Contact

For questions or suggestions, contact [Sercan Külcü](https://github.com/sercankulcu) or open an issue on GitHub.
