# rv

Explorations in pure reasoning algorithms with Clojure.

## Including

### deps.edn

    me.fogus/rv {:mvn/version "0.0.10"}

OR

    io.github.fogus/rv {:git/tag "v0.0.10" :git/sha "..."}

### Leiningen

Modify your [Leiningen](http://github.com/technomancy/leiningen) dependencies to include:

    :dependencies [[me.fogus/rv "0.0.10"] ...]

### Maven

Add the following to your `pom.xml` file:

    <dependency>
      <groupId>me.fogus</groupId>
      <artifactId>rv</artifactId>
      <version>0.0.10</version>
    </dependency>

## Dev

Namespaces under the wip sub-ns are works in progress and should only be used for experimentation. It is expected that these implementations will change frequently and may disappear altogether.

    clj -X:dev:test

To generate the current API docs run the following:

    clj -Tquickdoc quickdoc '{:outfile "doc/API.md", :github/repo "https://github.com/fogus/rv", :toc false}'

The above requires that you install quickdocs as a CLI tool first.

## License

Copyright © 2017-2025 Fogus

Distributed under the Eclipse Public License version 2.0
