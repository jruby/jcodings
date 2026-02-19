jcodings
========

Java-based codings helper classes for Joni and JRuby

## License

JCodings is released under the [MIT License](http://www.opensource.org/licenses/MIT).

## Publishing

Builds can be published using `mvn deploy`.

See https://central.sonatype.org/publish/publish-portal-maven/#publishing

## Releasing

The version in pom.xml should be updated to remove `-SNAPSHOT` when deploying a release and bumped to the next snapshot version after.

The `release` profile must be specified with `mvn deploy -Prelease` to include sources, javadocs, and artifact signing required by Maven Central.
