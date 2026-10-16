package studentkb.model;

public class StudentProfile {
    private final int id;
    private final String studentName;
    private final String preferredRole;
    private final String academicStatus;
    private final String jobStatus;
    private final String jobDetails;
    private final String languagesKnown;
    private final boolean whitelisted;
    private final boolean blacklisted;
    private final String knownDatabases;
    private final String commentsFaculty;


    public StudentProfile(int id, String studentName, String preferredRole, String academicStatus,
                          String jobStatus, String jobDetails, String languagesKnown,
                          boolean whitelisted, boolean blacklisted, String knownDatabases, String commentsFaculty) {
        this.id = id;
        this.studentName = studentName;
        this.preferredRole = preferredRole;
        this.academicStatus = academicStatus;
        this.jobStatus = jobStatus;
        this.jobDetails = jobDetails;
        this.languagesKnown = languagesKnown;
        this.whitelisted = whitelisted;
        this.blacklisted = blacklisted;
        this.knownDatabases = knownDatabases;
        this.commentsFaculty = commentsFaculty;
    }

    public int getId() { return id; }
    public String getStudentName() { return studentName; }
    public String getPreferredRole() { return preferredRole; }
    public String getAcademicStatus() { return academicStatus; }
    public String getJobStatus() { return jobStatus; }
    public String getJobDetails() { return jobDetails; }
    public String getLanguagesKnown() { return languagesKnown; }
    public boolean isWhitelisted() { return whitelisted; }
    public boolean isBlacklisted() { return blacklisted; }
    public String getKnownDatabases() { return knownDatabases; }
    public String getCommentsFaculty() {return commentsFaculty;}
}
