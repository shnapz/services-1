resolvers += Resolver.bintrayRepo("impulse-io", "maven")
resolvers += Resolver.bintrayRepo("impulse-io", "sbt-plugins")
resolvers += Resolver.url("impulse bintray", url("https://dl.bintray.com/impulse-io/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("impulse-io" % "sbt-pulse" % "1.0.13")
