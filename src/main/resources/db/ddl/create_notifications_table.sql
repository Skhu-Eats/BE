CREATE TABLE notifications (
    id CHAR(36) NOT NULL,
    recipient_id CHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(255) NOT NULL,
    target_type VARCHAR(30) NULL,
    target_id CHAR(36) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id) REFERENCES users (id)
);

CREATE INDEX idx_notifications_recipient_created_at
    ON notifications (recipient_id, created_at);

CREATE INDEX idx_notifications_recipient_is_read
    ON notifications (recipient_id, is_read);
