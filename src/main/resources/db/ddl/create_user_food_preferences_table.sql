CREATE TABLE IF NOT EXISTS user_food_preferences (
                                                     id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    category VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_food_preferences_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    );
