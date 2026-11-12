CREATE TABLE xp_event (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    kind        VARCHAR(40) NOT NULL,
    amount      INT NOT NULL,
    topic_id    VARCHAR(200),
    dedupe_key  VARCHAR(300) UNIQUE,
    earned_on   DATE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX xp_event_earned_on ON xp_event (earned_on);

CREATE TABLE kudos (
    id         VARCHAR(60) PRIMARY KEY,
    earned_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE player (
    id                 INT PRIMARY KEY,
    streak             INT NOT NULL,
    longest_streak     INT NOT NULL,
    last_active        DATE,
    freezes            INT NOT NULL,
    spot_bugs_correct  INT NOT NULL
);
INSERT INTO player VALUES (1, 0, 0, NULL, 0, 0);

CREATE TABLE review_card (
    topic_id       VARCHAR(200) PRIMARY KEY,
    ease           DOUBLE PRECISION NOT NULL,
    interval_days  INT NOT NULL,
    repetitions    INT NOT NULL,
    due_on         DATE NOT NULL,
    last_result    VARCHAR(10)
);
