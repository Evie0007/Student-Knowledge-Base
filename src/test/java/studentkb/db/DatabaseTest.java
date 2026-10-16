package studentkb.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studentkb.model.ProgrammingLanguage;
import studentkb.model.StudentProfile;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Database class.
 *
 * These run against a separate test database file (set in pom.xml),
 * and every test starts from a clean, freshly initialized database.
 */
class DatabaseTest {

    @BeforeEach
    void resetDatabase() throws SQLException {
        Database.initialize();
        try (Connection c = Database.getConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM comments");
            s.execute("DELETE FROM students");
            s.execute("DELETE FROM languages");
        }
        Database.initialize(); // re-adds the default languages
    }

    /** Helper: builds a student profile with sensible defaults. */
    private static StudentProfile student(int id, String name, boolean whitelisted, boolean blacklisted) {
        return new StudentProfile(id, name, "Backend Developer", "Junior", "Not Employed", "",
                "Java, Python", whitelisted, blacklisted, "MySQL", "");
    }

    // ---------- Languages ----------

    @Test
    @DisplayName("A new database starts with Java, Python and C++")
    void seedsDefaultLanguages() throws SQLException {
        List<String> names = Database.getAllLanguages().stream()
                .map(ProgrammingLanguage::getName)
                .toList();
        assertEquals(List.of("C++", "Java", "Python"), names);
    }

    @Test
    @DisplayName("Adding a language trims spaces and saves it")
    void addLanguageTrimsAndSaves() throws SQLException {
        assertTrue(Database.addLanguage("  Rust  "));
        assertTrue(Database.getAllLanguages().stream().anyMatch(l -> l.getName().equals("Rust")));
    }

    @Test
    @DisplayName("Duplicate languages are rejected, ignoring upper/lower case")
    void rejectsDuplicateLanguageIgnoringCase() throws SQLException {
        assertFalse(Database.addLanguage("java"));
        assertFalse(Database.addLanguage("PYTHON"));
        assertEquals(3, Database.countLanguages());
    }

    @Test
    @DisplayName("Blank language names are rejected")
    void rejectsBlankLanguage() throws SQLException {
        assertFalse(Database.addLanguage("   "));
        assertFalse(Database.addLanguage(null));
        assertEquals(3, Database.countLanguages());
    }

    @Test
    @DisplayName("Deleting a language removes it")
    void deleteLanguage() throws SQLException {
        int id = Database.getAllLanguages().get(0).getId();
        assertTrue(Database.deleteLanguage(id));
        assertEquals(2, Database.countLanguages());
    }

    // ---------- Students ----------

    @Test
    @DisplayName("A saved student can be read back with the same details")
    void insertAndReadStudent() throws SQLException {
        int id = Database.insertStudentReturningId(student(0, "Ada Lovelace", true, false));

        List<StudentProfile> all = Database.getAllStudents();
        assertEquals(1, all.size());
        StudentProfile saved = all.get(0);
        assertEquals(id, saved.getId());
        assertEquals("Ada Lovelace", saved.getStudentName());
        assertEquals("Java, Python", saved.getLanguagesKnown());
        assertTrue(saved.isWhitelisted());
        assertFalse(saved.isBlacklisted());
    }

    @Test
    @DisplayName("Duplicate student names are detected, ignoring case and extra spaces")
    void detectsDuplicateStudentName() throws SQLException {
        Database.insertStudentReturningId(student(0, "Grace Hopper", false, false));

        assertTrue(Database.studentExistsByFullNameIgnoreCase("grace hopper"));
        assertTrue(Database.studentExistsByFullNameIgnoreCase("  GRACE HOPPER  "));
        assertFalse(Database.studentExistsByFullNameIgnoreCase("Grace Brewster"));
    }

    @Test
    @DisplayName("The database itself blocks a second student with the same name")
    void uniqueIndexBlocksDuplicateStudent() throws SQLException {
        Database.insertStudentReturningId(student(0, "Alan Turing", false, false));
        assertThrows(SQLException.class,
                () -> Database.insertStudentReturningId(student(0, " alan turing ", false, false)));
    }

    @Test
    @DisplayName("Updating a student changes their saved details")
    void updateStudent() throws SQLException {
        int id = Database.insertStudentReturningId(student(0, "Katherine Johnson", false, false));

        assertTrue(Database.updateStudent(student(id, "Katherine Johnson", false, true)));

        StudentProfile updated = Database.getAllStudents().get(0);
        assertFalse(updated.isWhitelisted());
        assertTrue(updated.isBlacklisted());
    }

    @Test
    @DisplayName("Deleting a student removes them")
    void deleteStudent() throws SQLException {
        int id = Database.insertStudentReturningId(student(0, "Margaret Hamilton", false, false));
        assertTrue(Database.deleteStudent(id));
        assertTrue(Database.getAllStudents().isEmpty());
    }

    @Test
    @DisplayName("Students are listed in alphabetical order, ignoring case")
    void studentsSortedByName() throws SQLException {
        Database.insertStudentReturningId(student(0, "charlie", false, false));
        Database.insertStudentReturningId(student(0, "Alice", false, false));
        Database.insertStudentReturningId(student(0, "bob", false, false));

        List<String> names = Database.getAllStudents().stream()
                .map(StudentProfile::getStudentName)
                .toList();
        assertEquals(List.of("Alice", "bob", "charlie"), names);
    }

    // ---------- Comments ----------

    @Test
    @DisplayName("Comments come back newest first")
    void commentsNewestFirst() throws SQLException {
        int id = Database.insertStudentReturningId(student(0, "Barbara Liskov", false, false));
        Database.addStudentComment(id, "2025-10-01 09:00 — first");
        Database.addStudentComment(id, "2025-10-02 09:00 — second");

        assertEquals(List.of("2025-10-02 09:00 — second", "2025-10-01 09:00 — first"),
                Database.getCommentsForStudentDesc(id));
    }

    @Test
    @DisplayName("The reports page gets each student's latest comment")
    void latestCommentPerStudent() throws SQLException {
        int a = Database.insertStudentReturningId(student(0, "Student A", false, false));
        int b = Database.insertStudentReturningId(student(0, "Student B", false, false));
        Database.addStudentComment(a, "old note");
        Database.addStudentComment(a, "new note");

        Map<Integer, String> latest = Database.getLatestCommentsForAllStudents();
        assertEquals("new note", latest.get(a));
        assertNull(latest.get(b)); // no comments yet
    }
}
