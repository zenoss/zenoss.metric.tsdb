# zenoss.metric.tsdb

Zenoss OpenTSDB client library

# Releasing

Use git flow to release a new version to the `master` branch.

The artifact version number is defined in the [pom.xml](./pom.xml) files, but you should use `mvn versions:set` to modify the version number rather than editting the POM files by hand.

For Zenoss employees, the details on using git-flow to release a version is documented
on the Zenoss Engineering
[web site](https://sites.google.com/a/zenoss.com/engineering/home/faq/developer-patterns/using-git-flow).
After the git flow process is complete, a jenkins job can be triggered manually to build and
publish the artifact.
