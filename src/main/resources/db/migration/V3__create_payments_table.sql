CREATE TABLE payments (
                          id                        BIGSERIAL PRIMARY KEY,
                          order_id                  INTEGER NOT NULL REFERENCES orders(id),
                          amount                    NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
                          provider                  VARCHAR(20) NOT NULL CHECK (provider IN ('VNPAY', 'MOMO', 'ZALOPAY', 'ACB', 'VCB')),
                          payment_method            VARCHAR(20) NOT NULL CHECK (payment_method IN ('BANK', 'WALLET', 'CASH')),
                          status                    VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED')),
                          provider_transaction_id   VARCHAR(100) UNIQUE,
                          paid_at                   TIMESTAMP,
                          created_at                TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at                TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);