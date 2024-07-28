# api-oha-benchmarker

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.3.2/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.3.2/maven-plugin/reference/html/#build-image)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/docs/3.3.2/reference/html/features.html#features.testing.testcontainers)
* [Validation](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#io.validation)
* [Testcontainers](https://java.testcontainers.org/)

### Guides
The following guides illustrate how to use some features concretely:

* [Validation](https://spring.io/guides/gs/validating-form-input/)

### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/docs/3.3.2/reference/html/features.html#features.testing.testcontainers.at-development-time).

Testcontainers has been configured to use the following Docker images:


Please review the tags of the used images and set them to the same as you're running in production.

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

