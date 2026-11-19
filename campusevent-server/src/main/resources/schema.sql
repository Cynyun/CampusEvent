CREATE TABLE IF NOT EXISTS student (
    student_id   VARCHAR(32)  NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    password_hash CHAR(64)    NOT NULL,
    PRIMARY KEY (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS admin (
    admin_id     VARCHAR(32)  NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    password_hash CHAR(64)    NOT NULL,
    PRIMARY KEY (admin_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS activity (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    start_at    DATETIME     NOT NULL,
    location    VARCHAR(100) NOT NULL,
    capacity    INT          NOT NULL,
    policy_type VARCHAR(16)  NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    deleted_at  DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_activity_deleted_start (deleted_at, start_at),
    KEY idx_activity_policy (policy_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS registration (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    student_id  VARCHAR(32) NOT NULL,
    activity_id BIGINT      NOT NULL,
    status      VARCHAR(16) NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_registration_student_activity (student_id, activity_id),
    KEY idx_registration_activity_status (activity_id, status),
    CONSTRAINT fk_registration_student FOREIGN KEY (student_id) REFERENCES student (student_id),
    CONSTRAINT fk_registration_activity FOREIGN KEY (activity_id) REFERENCES activity (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS message (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  VARCHAR(32)  NOT NULL,
    activity_id BIGINT       NOT NULL,
    content     VARCHAR(255) NOT NULL,
    created_at  DATETIME     NOT NULL,
    is_read     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_message_student_read (student_id, is_read),
    CONSTRAINT fk_message_student FOREIGN KEY (student_id) REFERENCES student (student_id),
    CONSTRAINT fk_message_activity FOREIGN KEY (activity_id) REFERENCES activity (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
