SHOP2 (see http://www.cs.umd.edu/projects/shop/) is a hierarchical task network planning algorithm.

The original JSHOP2 project is a high-performance precompiled port of the SHOP2 LISP implementation.

## Forking rationale ##

The JSHOP2-RT branch is a fork of the original JSHOP2 code that contains optimizations towards real-time planning, primarily intended for use in games.

The fork is primarily because the motivation is drawn away from research and off-line, long-running planning tasks, and towards a short, tight planning/replanning loop.

As a part of this, the planner has been modified to support (highlights)
  * multithreaded environments (i.e. threadsafe code),
  * non-preemptive scheduling with explicit time-slicing, as well as
  * dynamical updating of the domain classes at run-time.

All of these changes introduce complexity that is unnecessary in a long-running planning application, which justifies forking the project.

The choice of package name (com.gamalocus.jshop2rt) is due to the fact that the project is currently maintained by employees of Gamalocus ApS (http://gamalocus.com/).

## Tools used ##

JSHOP2-RT is currently run as an eclipse project, with maven build support. We are using the YourKit Java Profiler (provided free of charge by YourKit) for profiling:

> YourKit is kindly supporting open source projects with its full-featured Java Profiler. YourKit, LLC is creator of innovative and intelligent tools for profiling Java and .NET applications. Take a look at YourKit's leading software products:
> [YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and [YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).