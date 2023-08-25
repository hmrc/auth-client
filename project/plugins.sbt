resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc"     % "sbt-auto-build"      % "3.9.0")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"       % "2.0.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform"     % "1.8.3")
addSbtPlugin("uk.gov.hmrc"     % "sbt-service-manager" % "0.11.0")
