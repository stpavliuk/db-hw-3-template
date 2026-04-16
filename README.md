# Spring MVC Database Template

Use this repository as a starting point for a database homework. It contains one generic entity, `Item`, and a simple server-rendered CRUD flow.

If you want to see a more complete reference application, use [flights-db-hw-3](https://github.com/stpavliuk/flights-db-hw-3).

Use it to:
- run the app quickly
- inspect one complete MVC flow
- rename `Item` to your own domain model
- replace the schema and build your own application from the same structure

## Stack

- Java 21+
- Gradle
- Spring Boot
- Spring Data JDBC
- Thymeleaf
- MySQL

## Project Structure

- [TemplateApplication.java](src/main/java/org/example/app/TemplateApplication.java): Spring Boot entry point
- [IndexController.java](src/main/java/org/example/app/IndexController.java): redirects `/` to `/item`
- [ItemController.java](src/main/java/org/example/app/item/ItemController.java): CRUD controller
- [Item.java](src/main/java/org/example/app/item/Item.java): entity
- [ItemRepository.java](src/main/java/org/example/app/item/ItemRepository.java): repository
- [schema.sql](src/main/resources/schema.sql): minimal schema
- [data.sql](src/main/resources/data.sql): small seed dataset
- [templates/item](src/main/resources/templates/item): Thymeleaf pages

## Local Setup

Update `src/main/resources/application.properties` with matching values:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/<your_database_name>?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=secret
```

Run the project:

```bash
./gradlew bootRun
```

Or

1. Open the project folder in IntelliJ IDEA
2. Go to the *File -> Project Structure -> Project* and verify that you're using **SDK 21+**
3. Open *Settings -> Build, Execution, Deployment -> Build Tools -> Gradle* select **Gradle JVM** to be **Project SDK**
4. And finally run the project:
  - Go to the `org/example/app/TemplateApplication.java` 
  - Click the Run button :)

Open:

```text
http://localhost:8080
```

> [!NOTE]
> The application will create a new MySQL docker container automatically, you just have to have the `docker` and `docker-compose` installed on the machine.
> By default, application restart will restart the container and wipe all your data. Overall, it should not be a problem, since you'll have your schema and data (re)populated automatically through [schema.sql](src/main/resources/schema.sql) and [data.sql](src/main/resources/data.sql).

However, if you still want to persist your data between sessions, uncomment these lines in [docker-compose.yml](./docker-compose.yml)
```
  # Uncomment to enable persistence between sessions
  # 
  # volumes:
  #   - ./data/mysql:/var/lib/mysql
```
With the next restart, MySQL will persist the database files in the `data/mysql` folder.

## How To Use It

1. Rename `Item` to the first real entity in the homework domain.
2. Replace the `item` table in `schema.sql` with the real schema.
3. Expand from the existing controller, repository, and templates.
4. Remove or adapt the sample records in `data.sql`.
