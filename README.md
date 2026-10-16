# Student Knowledge Base

A JavaFX desktop app that helps faculty keep track of students. It records their programming skills, career goals and job status, plus faculty comments about them.

## Features
- **Programming languages**: keep the list of languages that students can select.
- **Student profiles**: create profiles with each student's preferred role, academic and job status, known languages and databases.
- **Search & edit**: filter students by name, role, languages and status, then edit or delete them.
- **Whitelist / blacklist**: flag students and view the filtered lists on the Reports page.
- **Comments**: add dated comments to a student and view them in the profile details pop-up.

## Requirements
- JDK 23

Maven doesn't need to be installed. The included Maven Wrapper downloads Maven 3.9.11 to `~/.m2/wrapper` the first time it runs.

## Running
```bash
# Windows
mvnw.cmd clean javafx:run

# macOS / Linux
./mvnw clean javafx:run
```

On first launch, the app creates a SQLite database named `studentkb.db` in the working directory and seeds it with Java, Python and C++. To use a different file, set the JVM option `-Dstudentkb.db=/path/to/my.db`. Set it in your IDE run configuration, or add it under `<options><option>...</option></options>` in the `javafx-maven-plugin` configuration in `pom.xml`.

## Project layout
```
src/main/java/
  module-info.java            module "studentkb"
  studentkb/app/              Main (entry point), Views (FXML names + window titles)
  studentkb/controller/       one JavaFX controller per screen
  studentkb/db/               Database (SQLite access)
  studentkb/model/            StudentProfile, ProgrammingLanguage
  studentkb/util/             FxUtil (scene switching, FXML loading, alerts)
src/main/resources/
  studentkb/views/            FXML screens
```

To add a new screen, put the `.fxml` file in `studentkb/views/`, add its name to `Views` and load it with `FxUtil.loader(...)` or `FxUtil.switchScene(...)`.
