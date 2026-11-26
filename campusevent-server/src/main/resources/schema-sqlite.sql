PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS student (
    student_id    TEXT NOT NULL,
    name          TEXT NOT NULL,
    grade         INTEGER NULL,
    password_hash TEXT NOT NULL,
    PRIMARY KEY (student_id)
);

CREATE TABLE IF NOT EXISTS admin (
    admin_id      TEXT NOT NULL,
    name          TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    PRIMARY KEY (admin_id)
);

CREATE TABLE IF NOT EXISTS activity (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT    NOT NULL,
    category    TEXT    NOT NULL,
    start_at    TEXT    NOT NULL,
    location    TEXT    NOT NULL,
    capacity    INTEGER NOT NULL,
    policy_type TEXT    NOT NULL,
    created_at  TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL,
    deleted_at  TEXT    NULL
);

CREATE INDEX IF NOT EXISTS idx_activity_deleted_start ON activity (deleted_at, start_at);
CREATE INDEX IF NOT EXISTS idx_activity_policy ON activity (policy_type);

CREATE TABLE IF NOT EXISTS registration (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id  TEXT    NOT NULL,
    activity_id INTEGER NOT NULL,
    status      TEXT    NOT NULL,
    created_at  TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL,
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    FOREIGN KEY (activity_id) REFERENCES activity (id)
);

CREATE INDEX IF NOT EXISTS idx_registration_student_activity ON registration (student_id, activity_id);
CREATE INDEX IF NOT EXISTS idx_registration_activity_status ON registration (activity_id, status);

CREATE TABLE IF NOT EXISTS message (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id  TEXT    NOT NULL,
    activity_id INTEGER NOT NULL,
    content     TEXT    NOT NULL,
    created_at  TEXT    NOT NULL,
    is_read     INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    FOREIGN KEY (activity_id) REFERENCES activity (id)
);

CREATE INDEX IF NOT EXISTS idx_message_student_read ON message (student_id, is_read);

CREATE TABLE IF NOT EXISTS email_log (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    registration_id INTEGER NOT NULL,
    event_type      TEXT    NOT NULL,
    recipient       TEXT    NOT NULL,
    subject         TEXT    NOT NULL,
    content         TEXT    NOT NULL,
    status          TEXT    NOT NULL,
    error_message   TEXT    NULL,
    created_at      TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_email_log_registration ON email_log (registration_id);

CREATE TABLE IF NOT EXISTS app_meta (
    meta_key   TEXT NOT NULL,
    meta_value TEXT NOT NULL,
    PRIMARY KEY (meta_key)
);