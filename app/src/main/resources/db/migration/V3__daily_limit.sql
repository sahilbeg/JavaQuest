-- Study settings live on the player row, so a progress backup carries them.
-- Older backups have no such columns; restoring one falls back to these defaults.
ALTER TABLE player ADD COLUMN daily_limit_on BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE player ADD COLUMN daily_limit INT DEFAULT 3 NOT NULL;
