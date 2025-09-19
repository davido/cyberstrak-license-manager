# License Manager (Spring Boot)

A full-featured [Rhino Cloud Zoo license management backend](https://developer.rhino3d.com/guides/rhinocommon/cloudzoo/cloudzoo-implement-http-callbacks/) migrated from Python Flask to Spring Boot, with REST API, Basic Auth, Docker, PostgreSQL/MySQL, Liquibase, Swagger, and HTTPS support via NGINX.

---

## Features

- License creation, upgrade, assignment, and validation
- Basic Authentication (custom credentials via issuer ID/secret)
- RESTful endpoints with Swagger UI
- CLI interface (`create_license`, `list_licenses`, `show_license`)
- PostgreSQL, MySQL, or SQLite (profiles)
- Docker and Docker Compose support with persistent volumes
- Liquibase migrations for schema management
- HTTPS termination via NGINX reverse proxy
- GitHub Actions CI/CD
- JavaDocs and `package-info.java` documentation

---

## Getting Started

### 1. Build the App

To build rest application run:
```bash
mvn clean package -Prest
```
to build shell application run:

```bash
mvn clean package -Pshell
```

### 2. Run with H2 (default)
```bash
java -jar target/rest-runner-1.0.0.jar
```

### 3. Run with PostgreSQL
```bash
docker-compose -f docker-compose-postgres.yml up --build
```

### 4. Run with MySQL
```bash
docker-compose -f docker-compose-mysql.yml up --build
```

### 5. Use CLI Commands
```bash
java -jar target/license-manager-1.0.0.jar create_license
java -jar target/license-manager-1.0.0.jar list_licenses
java -jar target/license-manager-1.0.0.jar show_license SERIAL_NO_1
```

---

## API Overview

- `GET /` – Hello world
- `GET /info` – License count
- `GET /get_license` – Get license details (**requires Basic Auth**)
- `POST /add_license` – Add license to entity (**requires Basic Auth**)
- `POST /remove_license` – Remove license cluster (**requires Basic Auth**)
- `GET /dump_licenses` – Get all licenses (**requires Basic Auth**)

---

## Authentication

All sensitive endpoints require **Basic Auth**:

- Username: `issuer.id` (from application properties)
- Password: `issuer.secret`

Example:
```bash
curl -u test_issuer:test_secret http://localhost:8080/get_license?key=...&aud=...
```

---

## HTTPS and NGINX Setup

### For Development (Self-Signed)
```bash
openssl req -x509 -newkey rsa:2048 -nodes -keyout dev.key -out dev.crt -days 365 -subj "/CN=localhost"
```

### Sample NGINX Config
```nginx
server {
  listen 443 ssl;
  server_name localhost;

  ssl_certificate     /etc/nginx/certs/dev.crt;
  ssl_certificate_key /etc/nginx/certs/dev.key;

  location / {
    proxy_pass http://host.docker.internal:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

Run NGINX:
```bash
docker run -d -p 443:443 \
  -v $(pwd)/dev.crt:/etc/nginx/certs/dev.crt \
  -v $(pwd)/dev.key:/etc/nginx/certs/dev.key \
  -v $(pwd)/nginx-dev.conf:/etc/nginx/conf.d/default.conf \
  nginx
```

---

### For Production (Trusted CA)
- Use `fullchain.pem` and `privkey.pem`
- Configure NGINX the same way with real domain and certificates

---

## Volumes (Data Persistence)

### PostgreSQL
```yaml
volumes:
  - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```

### MySQL
```yaml
volumes:
  - mysqldata:/var/lib/mysql
volumes:
  mysqldata:
```

---

## Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## Testing

Run tests:
```bash
mvn test
```

Includes:
- JUnit service tests
- Repository integration tests
- MockMvc controller tests

---

## CI/CD

A GitHub Actions workflow builds and tests on push to `main`. Coverage reports are uploaded.

---

## License

MIT

---

## Building and Testing the Project

### Option 1: Local Build with Maven

If you have Maven installed:

```bash
mvn clean verify
```

This compiles the project, runs unit and integration tests, and generates a JaCoCo coverage report in:
```
target/site/jacoco/index.html
```

---

### Option 2: Build Inside a Docker Container

If you don't want to install Maven or Java locally, you can use Docker:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9.5-eclipse-temurin-17 mvn clean verify
```

This builds the project and runs all tests in an isolated Maven+JDK 17 environment.

---

### What This Verifies

- Compilation of all source code and test files
- Spring Boot and dependency resolution
- Test results and code coverage

Ensure Docker is installed and running before using Option 2.

---

## Publishing to Maven Central

To publish your Spring Boot project to Maven Central via Sonatype:

### 1. Sign Up and Configure Access
- Register at [Sonatype OSSRH](https://central.sonatype.org/pages/ossrh-guide.html)
- Create a JIRA issue to request access (one-time)

### 2. Add Distribution Management
Edit your `pom.xml`:
```xml
<distributionManagement>
  <snapshotRepository>
    <id>ossrh</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
  </snapshotRepository>
  <repository>
    <id>ossrh</id>
    <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
  </repository>
</distributionManagement>
```

### 3. Add GPG Signing and Credentials
In `~/.m2/settings.xml`:
```xml
<servers>
  <server>
    <id>ossrh</id>
    <username>your_sonatype_username</username>
    <password>your_sonatype_password</password>
  </server>
</servers>
```

### 4. Enable Source and Javadoc Publishing
Add to `pom.xml`:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-source-plugin</artifactId>
      <version>3.2.1</version>
      <executions>
        <execution>
          <id>attach-sources</id>
          <goals><goal>jar</goal></goals>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-javadoc-plugin</artifactId>
      <version>3.4.1</version>
      <executions>
        <execution>
          <id>attach-javadocs</id>
          <goals><goal>jar</goal></goals>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-gpg-plugin</artifactId>
      <version>3.1.0</version>
      <executions>
        <execution>
          <id>sign-artifacts</id>
          <phase>verify</phase>
          <goals><goal>sign</goal></goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### 5. Deploy
Run:
```bash
mvn clean deploy -P release
```

This signs, packages, and uploads the artifact (JAR, sources, Javadoc) to Sonatype OSSRH for release.

---

## Swagger API Documentation

### Accessing Swagger UI
Once the app is running, visit:

```
http://localhost:8080/swagger-ui.html
```

Here you can explore and test all endpoints directly in your browser.

### Import into Postman

1. Go to Postman
2. Click **Import > Link**
3. Enter:
```
http://localhost:8080/v3/api-docs
```
4. Postman will fetch and display the full OpenAPI schema
5. You can now test endpoints with built-in authentication and request bodies

---

### Authentication in Postman

For protected endpoints:
- Go to **Authorization** tab
- Type: `Basic Auth`
- Username: your `issuer.id` from properties
- Password: your `issuer.secret`

All endpoints are auto-documented with required headers, parameters, and response models.

---

## Deploying with Helm on Kubernetes

You can deploy the license-manager Spring Boot app to a Kubernetes cluster using Helm.

### 1. Package and Deploy

```bash
helm install license-manager ./helm/license-manager
```

### 2. Customize Values

Edit `helm/license-manager/values.yaml` to:
- Set your Docker image repo + tag
- Adjust replica count or service type

### Example `values.yaml` Snippet:
```yaml
image:
  repository: your-dockerhub-username/license-manager
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080
```

### 3. Access the Service

```bash
kubectl get svc
```

Use `kubectl port-forward` or expose via an ingress controller for external access.

---

Ensure you have Helm installed and your `kubectl` context is pointing to a valid cluster before deploying.

---

## Optional: Build Native Image with GraalVM

To compile the application into a native executable using GraalVM:

### 1. Prerequisites
- Install [GraalVM](https://www.graalvm.org/) 21+ with `native-image` component:
```bash
gu install native-image
```

- Ensure you have Maven installed

### 2. Add GraalVM Plugin to `pom.xml`
Add the plugin under `<plugins>` section:
```xml
<plugin>
  <groupId>org.graalvm.buildtools</groupId>
  <artifactId>native-maven-plugin</artifactId>
  <version>0.10.2</version>
  <executions>
    <execution>
      <goals>
        <goal>native-image</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

### 3. Build Native Executable
```bash
mvn clean package -Pnative
```

The binary will be under:
```
target/license-manager
```

### 4. Run Native Binary
```bash
./target/license-manager
```

### Notes
- Native images have fast startup and small memory footprint
- Useful for containerized deployments (e.g. Alpine)
- Requires static analysis — avoid heavy reflection and dynamic proxies

For production-ready setup, consider using `spring-native` or Spring Boot 3+ AOT support.

---

## Database Migrations with Liquibase

Liquibase is configured to manage schema changes automatically on application startup.

### How It Works

- Change logs are located in:
  ```
  src/main/resources/db/changelog/
  ```
- Master file:
  ```
  db.changelog-master.yml
  ```
  includes:
  ```
  01-create-license-table.yml
  ```

Spring Boot automatically applies these migrations at runtime.

### Adding a New Migration

1. Create a new changelog file in `src/main/resources/db/changelog/`, for example:
   ```
   02-add-column-license-type.yml
   ```

2. Reference it in `db.changelog-master.yml`:
   ```yaml
   - include:
       file: db/changelog/02-add-column-license-type.yml
   ```

3. Example change file:
   ```yaml
   databaseChangeLog:
     - changeSet:
         id: 02
         author: yourname
         changes:
           - addColumn:
               tableName: licenses
               columns:
                 - column:
                     name: license_type
                     type: VARCHAR(50)
   ```

4. Restart the app or run with Docker to apply migrations.

### Run Liquibase Manually (Optional)

You can also run Liquibase manually via Maven:

```bash
mvn liquibase:update
```

Ensure your `application.properties` is configured for the correct database.
---

## Configuration: Environment Variables

The application uses the following environment variables, which can be set directly or via `application.properties`:

| Variable               | Description                                      | Default Value                         |
|------------------------|--------------------------------------------------|---------------------------------------|
| `issuer.id`           | Issuer identifier for license validation         | `test_issuer`                         |
| `issuer.secret`       | Secret used for Basic Auth with API              | `test_secret`                         |
| `issuer.name`         | Friendly issuer name for support messages        | `Dolfinito®`                          |
| `issuer.support.url`  | Support contact URL shown in error messages      | `https://www.dolfinito.com/support`   |
| `spring.datasource.url` | JDBC URL for database (SQLite/PostgreSQL/MySQL) | `jdbc:sqlite:test.db`                 |

Set them via:
```bash
export issuer.id=your_issuer
export issuer.secret=your_secret
```
Or define them in Docker Compose or Kubernetes Helm chart `values.yaml`.

---

## Production Hardening Tips

Here are a few recommendations for deploying this project in a production environment:

### 1. Use a Secure Database Connection
- For PostgreSQL/MySQL, use SSL-enabled JDBC URLs
- Store credentials in Kubernetes Secrets or Vault

### 2. HTTPS Everywhere
- Terminate TLS at NGINX with valid certs (e.g. Let's Encrypt)
- Use cert-manager for automated renewal in Kubernetes

### 3. Secure Secrets Management
- Never commit `.env` or credentials
- Use tools like HashiCorp Vault, AWS Secrets Manager, or Kubernetes secrets

### 4. Enable Audit Logging
- Integrate Spring Boot logging with ELK or Fluentd
- Enable access logs in NGINX

### 5. Container and Dependency Scanning
- Use tools like Trivy, Snyk, or GitHub Dependabot
- Scan image before publishing to GHCR or Docker Hub

### 6. Monitoring and Metrics
- Integrate Prometheus with Spring Boot Actuator
- Visualize with Grafana or Datadog

---

These tips help prepare the app for secure, scalable production deployment.# cyberstrak-license-manager
