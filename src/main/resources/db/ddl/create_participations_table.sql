CREATE TABLE IF NOT EXISTS participations (
    id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'JOINED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_participations_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_participations_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_participations_post_user
        UNIQUE (post_id, user_id)
);

CREATE INDEX idx_participations_user_status_created_at
    ON participations (user_id, status, created_at);

CREATE INDEX idx_participations_post_status
    ON participations (post_id, status);
