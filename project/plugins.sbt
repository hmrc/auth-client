resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc"     % "sbt-auto-build"      % "3.21.0")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"       % "2.0.9" exclude ("org.scala-lang.modules", "scala-xml_2.12"))
addSbtPlugin("org.scalariform" % "sbt-scalariform"     % "1.8.3" exclude ("org.scala-lang.modules", "scala-xml_2.12"))
