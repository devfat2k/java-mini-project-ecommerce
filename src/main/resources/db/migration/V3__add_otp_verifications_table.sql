ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE otp_verifications(
                                  id           BIGSERIAL PRIMARY KEY,
                                  user_id      BIGINT NOT NULL REFERENCES users(id),
                                  otp_hash     VARCHAR(255) NOT NULL,
                                  purpose      VARCHAR(150) NOT NULL CHECK (purpose IN ('REGISTER_VERIFICATION', 'RESET_PASSWORD', 'CHANGE_PASSWORD_CONFIRMATION')),
                                  expires_at   TIMESTAMP NOT NULL,
                                  attempts     INTEGER NOT NULL DEFAULT 0,
                                  consumed     BOOLEAN NOT NULL DEFAULT FALSE,
                                  created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_user_purpose ON otp_verifications(user_id, purpose);