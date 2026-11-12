CREATE TABLE topic_progress (
    topic_id                  VARCHAR(200) PRIMARY KEY,
    quiz_best                 INT NOT NULL DEFAULT 0,
    quiz_attempts             INT NOT NULL DEFAULT 0,
    tests_passed              BOOLEAN NOT NULL DEFAULT FALSE,
    failed_runs               INT NOT NULL DEFAULT 0,
    hints_used                INT NOT NULL DEFAULT 0,
    failed_runs_at_last_hint  INT NOT NULL DEFAULT 0,
    solution_viewed           BOOLEAN NOT NULL DEFAULT FALSE,
    mastered_at               TIMESTAMP WITH TIME ZONE,
    last_seen                 TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE test_run (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id     VARCHAR(200) NOT NULL,
    source       VARCHAR(10) NOT NULL,
    outcome      VARCHAR(20) NOT NULL,
    passed       INT NOT NULL,
    total        INT NOT NULL,
    duration_ms  BIGINT NOT NULL,
    result_json  CLOB NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX test_run_topic ON test_run (topic_id, id);

CREATE TABLE quiz_attempt (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id       VARCHAR(200) NOT NULL,
    seed           BIGINT NOT NULL,
    score          INT NOT NULL,
    passed         BOOLEAN NOT NULL,
    weak_concepts  VARCHAR(2000) NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX quiz_attempt_topic ON quiz_attempt (topic_id, id);
