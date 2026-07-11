-- ============================================================
-- Migration: Add push_token to users + create notifications table
-- Run this ONCE on the Railway PostgreSQL database
-- ============================================================

-- 1. Add push_token column to users (if not exists)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS push_token VARCHAR(500);

-- 2. Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    villa_id BIGINT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (villa_id) REFERENCES villas(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Indexes for performance
CREATE INDEX IF NOT EXISTS idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_villa_id   ON notifications(villa_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read    ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);

-- 4. Fix existing indexes to use IF NOT EXISTS (safe to re-run)
CREATE INDEX IF NOT EXISTS idx_apartments_villa_id                    ON apartments(villa_id);
CREATE INDEX IF NOT EXISTS idx_payments_villa_id                      ON payments(villa_id);
CREATE INDEX IF NOT EXISTS idx_payments_apartment_id                  ON payments(apartment_id);
CREATE INDEX IF NOT EXISTS idx_expenses_villa_id                      ON expenses(villa_id);
CREATE INDEX IF NOT EXISTS idx_expenses_apartment_id                  ON expenses(apartment_id);
CREATE INDEX IF NOT EXISTS idx_recurring_expense_templates_villa_id   ON recurring_expense_templates(villa_id);
CREATE INDEX IF NOT EXISTS idx_recurring_expense_templates_active     ON recurring_expense_templates(is_active);
CREATE INDEX IF NOT EXISTS idx_service_requests_villa_id              ON service_requests(villa_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user_id                  ON activity_logs(user_id);

-- Done!
SELECT 'Migration complete: notifications table and push_token column added.' AS status;
