-- Where the learner is inside each ticket: the card on screen, the furthest card reached, and the card's title
-- (so a lesson that gains or loses cards in a rebuild still opens on the right one). Kept with the progress,
-- so a rebuild, a hard reload or another browser opens the same screen.
CREATE TABLE lesson_position (
    topic_id    VARCHAR(200) PRIMARY KEY,
    step        INT NOT NULL,
    furthest    INT NOT NULL,
    total       INT NOT NULL,
    label       VARCHAR(300) NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);
