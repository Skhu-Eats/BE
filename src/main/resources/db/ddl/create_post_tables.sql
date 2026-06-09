CREATE TABLE IF NOT EXISTS posts (
                                     id CHAR(36) NOT NULL,
    host_id CHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    meeting_time DATETIME NOT NULL,
    deadline DATETIME NOT NULL,
    max_participants INT NOT NULL,
    current_participants INT NOT NULL DEFAULT 1,
    memo VARCHAR(255),
    kakao_link VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_posts_host
    FOREIGN KEY (host_id)
    REFERENCES users(id),

    CONSTRAINT chk_posts_max_participants
    CHECK (max_participants BETWEEN 2 AND 4),

    CONSTRAINT chk_posts_current_participants
    CHECK (current_participants >= 1 AND current_participants <= max_participants)
    );

CREATE TABLE IF NOT EXISTS post_food_categories (
                                                    id BIGINT AUTO_INCREMENT NOT NULL,
                                                    post_id CHAR(36) NOT NULL,
    category VARCHAR(30) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_post_food_categories_post
    FOREIGN KEY (post_id)
    REFERENCES posts(id)
    ON DELETE CASCADE,

    CONSTRAINT uk_post_food_category
    UNIQUE (post_id, category)
    );
