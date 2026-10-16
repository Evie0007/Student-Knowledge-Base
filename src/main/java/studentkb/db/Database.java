package studentkb.db;

import studentkb.model.ProgrammingLanguage;
import studentkb.model.StudentProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Database {

    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("studentkb.db", "studentkb.db");

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS languages (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE COLLATE NOCASE
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_name     TEXT NOT NULL,
                    preferred_role   TEXT,
                    academic_status  TEXT,
                    job_status       TEXT,
                    job_details      TEXT,
                    languages_known  TEXT NOT NULL,
                    whitelisted      INTEGER NOT NULL DEFAULT 0,
                    blacklisted      INTEGER NOT NULL DEFAULT 0,
                    databases_known  TEXT,
                    comments_faculty TEXT
                )
            """);


            try (Statement s2 = c.createStatement()) {
                try { s2.execute("ALTER TABLE students ADD COLUMN databases_known TEXT"); }
                catch (SQLException ignore) { /* already exists */ }

                try { s2.execute("ALTER TABLE students ADD COLUMN comments_faculty TEXT"); }
                catch (SQLException ignore) { /* already exists */ }
            }

            s.execute("""
                CREATE TABLE IF NOT EXISTS comments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    content TEXT NOT NULL
                )
            """);


            s.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS idx_students_name_nocase
                ON students (LOWER(TRIM(student_name)))
            """);

            if (countLanguages() == 0) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO languages(name) VALUES (?)")) {
                    ps.setString(1, "Java");   ps.executeUpdate();
                    ps.setString(1, "Python"); ps.executeUpdate();
                    ps.setString(1, "C++");    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database init failed: " + e.getMessage(), e);
        }
    }

    public static int countLanguages() throws SQLException {
        try (Connection c = getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) AS n FROM languages")) {
            return rs.next() ? rs.getInt("n") : 0;
        }
    }

    public static List<ProgrammingLanguage> getAllLanguages() throws SQLException {
        List<ProgrammingLanguage> list = new ArrayList<>();
        try (Connection c = getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, name FROM languages ORDER BY name COLLATE NOCASE ASC")) {
            while (rs.next()) {
                list.add(new ProgrammingLanguage(rs.getInt("id"), rs.getString("name")));
            }
        }
        return list;
    }

    public static boolean addLanguage(String name) throws SQLException {
        if (name == null || name.isBlank()) return false;

        String trimmed = name.trim();

        try (Connection c = getConnection()) {

            try (PreparedStatement check = c.prepareStatement(
                    "SELECT COUNT(*) FROM languages WHERE LOWER(name) = LOWER(?)")) {
                check.setString(1, trimmed);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Duplicate found
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement("INSERT INTO languages(name) VALUES (?)")) {
                ps.setString(1, trimmed);
                return ps.executeUpdate() == 1;
            }
        }
    }

    public static boolean deleteLanguage(int id) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM languages WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean insertStudent(StudentProfile sp) throws SQLException {
        String sql = """
          INSERT INTO students (student_name, preferred_role, academic_status, job_status, job_details,
                                languages_known, whitelisted, blacklisted, databases_known, comments_faculty)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sp.getStudentName());
            ps.setString(2, sp.getPreferredRole());
            ps.setString(3, sp.getAcademicStatus());
            ps.setString(4, sp.getJobStatus());
            ps.setString(5, sp.getJobDetails());
            ps.setString(6, sp.getLanguagesKnown());
            ps.setInt(7, sp.isWhitelisted() ? 1 : 0);
            ps.setInt(8, sp.isBlacklisted() ? 1 : 0);
            ps.setString(9, sp.getKnownDatabases());
            ps.setString(10, sp.getCommentsFaculty());
            return ps.executeUpdate() == 1;
        }
    }


    public static List<StudentProfile> getAllStudents() throws SQLException {
        List<StudentProfile> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_name COLLATE NOCASE ASC";
        try (Connection c = getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new StudentProfile(
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("preferred_role"),
                        rs.getString("academic_status"),
                        rs.getString("job_status"),
                        rs.getString("job_details"),
                        rs.getString("languages_known"),
                        rs.getInt("whitelisted") == 1,
                        rs.getInt("blacklisted") == 1,
                        rs.getString("databases_known"),
                        rs.getString("comments_faculty")
                ));
            }
        }
        return list;
    }

    public static boolean studentExistsByFullNameIgnoreCase(String name) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE LOWER(TRIM(student_name)) = LOWER(TRIM(?))";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public static boolean updateStudent(studentkb.model.StudentProfile sp) throws java.sql.SQLException {
        String sql = """
        UPDATE students
           SET student_name    = ?,
               preferred_role  = ?,
               academic_status = ?,
               job_status      = ?,
               job_details     = ?,
               languages_known = ?,
               whitelisted     = ?,
               blacklisted     = ?,
               databases_known = ?,
               comments_faculty= ?
         WHERE id = ?
    """;
        try (java.sql.Connection c = getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,  sp.getStudentName());
            ps.setString(2,  sp.getPreferredRole());
            ps.setString(3,  sp.getAcademicStatus());
            ps.setString(4,  sp.getJobStatus());
            ps.setString(5,  sp.getJobDetails());
            ps.setString(6,  sp.getLanguagesKnown());
            ps.setInt(7,     sp.isWhitelisted() ? 1 : 0);
            ps.setInt(8,     sp.isBlacklisted() ? 1 : 0);
            ps.setString(9,  sp.getKnownDatabases());
            ps.setString(10, sp.getCommentsFaculty());
            ps.setInt(11,    sp.getId());
            return ps.executeUpdate() == 1;
        }
    }
    public static int insertStudentReturningId(StudentProfile sp) throws SQLException {
        String sql = """
          INSERT INTO students (student_name, preferred_role, academic_status, job_status, job_details,
                                languages_known, whitelisted, blacklisted, databases_known, comments_faculty)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sp.getStudentName());
            ps.setString(2, sp.getPreferredRole());
            ps.setString(3, sp.getAcademicStatus());
            ps.setString(4, sp.getJobStatus());
            ps.setString(5, sp.getJobDetails());
            ps.setString(6, sp.getLanguagesKnown());
            ps.setInt(7, sp.isWhitelisted() ? 1 : 0);
            ps.setInt(8, sp.isBlacklisted() ? 1 : 0);
            ps.setString(9, sp.getKnownDatabases());
            ps.setString(10, sp.getCommentsFaculty());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No generated key returned");
    }

    public static void setStudentDatabasesCsv(int studentId, String csv) throws SQLException {
        String upd = "UPDATE students SET databases_known=? WHERE id=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(upd)) {
            ps.setString(1, csv);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    public static void addStudentComment(int studentId, String content) throws SQLException {
        String ins = "INSERT INTO comments(student_id, content) VALUES(?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(ins)) {
            ps.setInt(1, studentId);
            ps.setString(2, content);
            ps.executeUpdate();
        }
    }

    public static boolean deleteStudent(int id) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }


    public static List<String> getCommentsForStudentDesc(int studentId) throws SQLException {
        String sql = "SELECT content FROM comments WHERE student_id = ? ORDER BY id DESC";
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }


    public static Map<Integer, String> getLatestCommentsForAllStudents() throws SQLException {
        String sql = """
            SELECT s.id,
                   (SELECT content FROM comments c
                      WHERE c.student_id = s.id
                      ORDER BY c.id DESC
                      LIMIT 1) AS latest
            FROM students s
        """;
        Map<Integer, String> map = new HashMap<>();
        try (Connection c = getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("latest"));
            }
        }
        return map;
    }
}
