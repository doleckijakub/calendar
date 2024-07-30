CREATE TABLE event (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1023),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    author_id UUID NOT NULL,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES "user" (id)
);