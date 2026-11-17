-- The read-aloud voice and speed, kept with the progress so they stay the same in every browser and in backups.
-- Older backups have no such columns; restoring one gives the browser's default voice at normal speed.
ALTER TABLE player ADD COLUMN voice_name VARCHAR(200);
ALTER TABLE player ADD COLUMN voice_rate DOUBLE PRECISION DEFAULT 1.0 NOT NULL;
