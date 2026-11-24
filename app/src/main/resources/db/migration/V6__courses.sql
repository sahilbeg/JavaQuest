-- Several courses in one progress folder. Only adds: every existing row belongs to the Java course,
-- and a learner who already has progress keeps the Java course as their active course.

ALTER TABLE player ADD COLUMN active_course VARCHAR(60);
UPDATE player SET active_course = 'java-swe'
 WHERE EXISTS (SELECT 1 FROM topic_progress) OR EXISTS (SELECT 1 FROM xp_event) OR EXISTS (SELECT 1 FROM lesson_position);

-- XP and badges are earned per course, so each course has its own level and ladder.
ALTER TABLE xp_event ADD COLUMN course_id VARCHAR(60) DEFAULT 'java-swe' NOT NULL;
CREATE INDEX xp_event_course ON xp_event (course_id);

ALTER TABLE kudos ADD COLUMN course_id VARCHAR(60) DEFAULT 'java-swe' NOT NULL;
ALTER TABLE kudos DROP PRIMARY KEY;
ALTER TABLE kudos ADD PRIMARY KEY (course_id, id);
