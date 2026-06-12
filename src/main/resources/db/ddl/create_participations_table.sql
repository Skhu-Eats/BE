-- participations 테이블
-- 주의: user_id는 users.id 타입(VARCHAR(255))에 맞춤. CHAR(36)으로 두면 FK errno 150 발생.
-- (users.id가 엔티티 @Id String에 길이 미지정이라 VARCHAR(255)로 생성됨)
-- FK는 charset/collation도 일치해야 하므로 utf8mb4_unicode_ci 지정.

CREATE TABLE IF NOT EXISTS participations (
    id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'JOINED',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_participations_user_status_created_at
    ON participations (user_id, status, created_at);

CREATE INDEX idx_participations_post_status
    ON participations (post_id, status);
