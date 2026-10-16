# Student Knowledge Base

A JavaFX desktop app that helps faculty keep track of students: their programming skills, career goals, job status, and faculty comments. Data is stored locally in SQLite.

> **About this project:** Team 33 built this app for **CS151 – Object-Oriented Programming with Java** at **San José State University**. The team wrote the core application by hand: the screens, controllers, data model and database layer. Afterward, [Claude](https://claude.com/claude-code) was used as a support tool to polish it: the UI theme, the Maven Wrapper setup, and this documentation.

<p align="center">
  <img src="docs/screenshots/home.png" width="32%" alt="Home screen"/>
  <img src="docs/screenshots/define-profile.png" width="32%" alt="Define Student Profile screen"/>
  <img src="docs/screenshots/reports.png" width="32%" alt="Reports screen"/>
</p>

## Features

- **Programming languages**: manage the list of languages students can pick from.
- **Student profiles**: record each student's preferred role, academic status, job status, languages and databases.
- **Search & edit**: filter students by name, role, language or status, then edit or delete them.
- **Whitelist / blacklist**: flag students and filter by flag on the Reports page.
- **Comments**: add comments to a student and read them in the profile details pop-up.

## Getting started

**You need:** JDK 23. Maven is optional, because the included Maven Wrapper downloads it the first time you run it.

```bash
git clone https://github.com/Evie0007/Student-Knowledge-Base.git
cd Student-Knowledge-Base

# Windows
mvnw.cmd clean javafx:run

# macOS / Linux
./mvnw clean javafx:run
```

On first launch the app creates `studentkb.db` in the current folder and adds Java, Python and C++ to the language list. To use a different database file, pass `-Dstudentkb.db=/path/to/file.db` to the JVM.

## How it works

### Architecture

The app has three layers. FXML files describe each screen, a controller handles each screen's events, and one `Database` class does all the SQL.

```mermaid
flowchart LR
    U([Faculty user]) --> V

    subgraph UI["UI layer"]
        V["FXML views<br/>+ app.css"]
        C["Controllers<br/>(one per screen)"]
        V <--> C
    end

    subgraph Data["Data layer"]
        D["Database<br/>(static JDBC methods)"]
        M["Models<br/>StudentProfile, ProgrammingLanguage"]
    end

    C --> D
    D --> M
    C --> M
    D <--> DB[("SQLite<br/>studentkb.db")]
```

### Class diagram

```mermaid
classDiagram
    direction LR

    class Main {
        +start(Stage)
        +main(String[])
    }
    class Views {
        <<constants>>
        +HOME
        +DEFINE_LANGUAGES
        +DEFINE_STUDENT
        +SEARCH_STUDENTS
        +EDIT_STUDENT
        +REPORTS
        +...
    }
    class FxUtil {
        <<utility>>
        +loader(fxml) FXMLLoader
        +scene(root) Scene
        +switchScene(event, fxml, title)
        +info(title, msg)
        +error(title, msg)
    }
    class Database {
        <<utility>>
        +initialize()
        +getAllLanguages() List~ProgrammingLanguage~
        +addLanguage(name) boolean
        +deleteLanguage(id) boolean
        +getAllStudents() List~StudentProfile~
        +insertStudentReturningId(sp) int
        +updateStudent(sp) boolean
        +deleteStudent(id) boolean
        +addStudentComment(studentId, text)
        +getCommentsForStudentDesc(studentId) List~String~
    }
    class StudentProfile {
        -int id
        -String studentName
        -String preferredRole
        -String academicStatus
        -String jobStatus
        -String jobDetails
        -String languagesKnown
        -String knownDatabases
        -String commentsFaculty
        -boolean whitelisted
        -boolean blacklisted
    }
    class ProgrammingLanguage {
        -int id
        -String name
    }

    class HomeController
    class DefineLanguagesController
    class DefineStudentProfileController
    class ViewStudentProfilesController
    class SearchStudentController
    class EditStudentProfileController
    class ReportsController
    class ProfileDetailsController
    class CommentViewController

    Main ..> Database : initialize()
    Main ..> FxUtil : loads home
    HomeController ..> FxUtil : switchScene
    DefineLanguagesController ..> Database
    DefineStudentProfileController ..> Database
    ViewStudentProfilesController ..> Database
    SearchStudentController ..> Database
    EditStudentProfileController ..> Database
    ReportsController ..> Database
    ProfileDetailsController ..> Database
    SearchStudentController ..> EditStudentProfileController : opens
    ReportsController ..> ProfileDetailsController : opens
    ProfileDetailsController ..> CommentViewController : opens
    Database ..> StudentProfile : creates
    Database ..> ProgrammingLanguage : creates
```

### Database schema

```mermaid
erDiagram
    STUDENTS ||--o{ COMMENTS : has
    STUDENTS {
        int id PK
        text student_name "unique, case-insensitive"
        text preferred_role
        text academic_status
        text job_status
        text job_details
        text languages_known "comma-separated names"
        text databases_known "comma-separated names"
        text comments_faculty
        int whitelisted "0 or 1"
        int blacklisted "0 or 1"
    }
    COMMENTS {
        int id PK
        int student_id FK
        text content "yyyy-MM-dd HH:mm — comment text"
    }
    LANGUAGES {
        int id PK
        text name "unique, case-insensitive"
    }
```

`LANGUAGES` is the list shown on the profile forms. The names a student picks are saved as comma-separated text in `students.languages_known`, not through a join table. Each comment's date is stored at the start of its `content`, and the Profile Details pop-up splits it into its own column.

### Screen navigation

```mermaid
flowchart TD
    Home[Home]
    Home --> Lang[Define Programming Languages]
    Home --> Def[Define Student Profile]
    Home --> View[View Student Profiles]
    Home --> Search[Search / Edit Student Profiles]
    Home --> Rep[Reports]

    Search -- "Edit Selected" --> Edit[Edit Student Profile]
    Edit -- "Save / Cancel" --> Search
    Rep -. "double-click a row" .-> Details[[Profile Details pop-up]]
    Details -. "click a comment" .-> Comment[[Full Comment pop-up]]

    Lang & Def & View & Search & Rep -- Back --> Home
```

### Example: saving a new student

```mermaid
sequenceDiagram
    actor F as Faculty
    participant C as DefineStudentProfileController
    participant D as Database
    participant S as SQLite

    F->>C: fill in form, click Save
    C->>D: studentExistsByFullNameIgnoreCase(name)
    D->>S: SELECT 1 FROM students WHERE ...
    S-->>D: no match
    C->>D: insertStudentReturningId(profile)
    D->>S: INSERT INTO students ...
    S-->>D: new id
    loop each comment added
        C->>D: addStudentComment(id, text)
        D->>S: INSERT INTO comments ...
    end
    C->>F: show "Student profile saved." and go back to Home
```

## Project layout

```
src/main/java/
  module-info.java            module "studentkb"
  studentkb/app/              Main (entry point), Views (FXML names + window titles)
  studentkb/controller/       one JavaFX controller per screen
  studentkb/db/               Database (SQLite access)
  studentkb/model/            StudentProfile, ProgrammingLanguage
  studentkb/util/             FxUtil (scene switching, FXML loading, alerts)
src/main/resources/studentkb/
  views/                      FXML screens
  styles/app.css              app theme (SJSU blue and gold)
```

To add a new screen:
1. Put the `.fxml` file in `studentkb/views/`.
2. Add its name to `Views`.
3. Open it with `FxUtil.switchScene(...)`, or with `FxUtil.loader(...)` and `FxUtil.scene(...)` for a pop-up, so it gets the app stylesheet.

## Tech stack

Java 23 · JavaFX 23 · SQLite (xerial `sqlite-jdbc`) · Maven
