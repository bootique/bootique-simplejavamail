<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->

[![build test deploy](https://github.com/bootique/bootique-simplejavamail/actions/workflows/maven.yml/badge.svg)](https://github.com/bootique/bootique-simplejavamail/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.simplejavamail/bootique-simplejavamail.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.simplejavamail/bootique-simplejavamail/)

# bootique-simplejavamail

Provides [Simple Java Mail](http://www.simplejavamail.org/) integration with [Bootique](https://bootique.io).

## Usage

Include ```bootique-bom```:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>3.0-M4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Import Simple Java Mail integration:
```xml
<dependency>
	<groupId>io.bootique.simplejavamail</groupId>
	<artifactId>bootique-simplejavamail</artifactId>
</dependency>
```

Configure the app:
```yaml
simplejavamail:

  # Mail delivery is disabled by default to prevent inadvertent spamming when the app is in development. This property
  # needs to be flipped to "false" if you want mail to be actually sent to someone.
  disabled: false

  # Configure named mailers. If "mailers" section is absent, the default mailer is created pointing to "localhost:25"
  mailers:
    main: # arbitrary name of the mailer
      smtpServer: example.org
      smtpPort: 3025
      transportStrategy: SMTPS
```

Send mail:
```java
@Inject
Mailers mailers;

public void sendMail() {
    Email email = EmailBuilder.startingBlank()
        .from("me@example.org")
        .to("you@example.org")
        .withSubject("Hi!")
        .withPlainText("Hello world..")
        .buildEmail();

    // if only one mailer is configured in YAML, it is assumed to be the default mailer
    mailers.getDefaultMailer().sendMail(email, false);
}
```

