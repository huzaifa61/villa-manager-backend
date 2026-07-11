-- Villa Manager Pro database schema (PostgreSQL)
-- Keep in sync with JPA entities under src/main/java/com/villamanager/entity

CREATE TABLE IF NOT EXISTS villas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    property_type VARCHAR(50),
    property_number VARCHAR(100),
    region VARCHAR(255),
    whatsapp_link VARCHAR(500),
    location VARCHAR(255),
    description TEXT,
    total_apartments INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    villa_id BIGINT REFERENCES villas(id),
    invite_token VARCHAR(255) UNIQUE,
    invite_expires_at TIMESTAMP,
    invite_accepted_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    push_token VARCHAR(500),
    subscription_expires_at TIMESTAMP,
    max_viewers INTEGER DEFAULT 5
);

CREATE TABLE IF NOT EXISTS apartments (
    id BIGSERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL REFERENCES villas(id),
    apartment_number VARCHAR(100) NOT NULL,
    owner_name VARCHAR(255),
    tenant_name VARCHAR(255),
    phone_number VARCHAR(50),
    email VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    opening_balance NUMERIC(10, 2) DEFAULT 0,
    current_balance NUMERIC(10, 2) DEFAULT 0,
    apartment_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expense_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL REFERENCES villas(id),
    apartment_id BIGINT REFERENCES apartments(id),
    category_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(10, 2),
    expense_date DATE,
    is_split BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL REFERENCES villas(id),
    apartment_id BIGINT REFERENCES apartments(id),
    category_id BIGINT NOT NULL,
    amount NUMERIC(10, 2),
    payment_date DATE,
    payment_method VARCHAR(100),
    reference_number VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    is_split BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(50),
    address TEXT,
    service_type VARCHAR(255),
    region VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS service_requests (
    id BIGSERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL REFERENCES villas(id),
    apartment_id BIGINT NOT NULL REFERENCES apartments(id),
    description VARCHAR(255) NOT NULL,
    vendor_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recurring_expense_templates (
    id BIGSERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL REFERENCES villas(id),
    apartment_id BIGINT REFERENCES apartments(id),
    category_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    day_of_month INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_generated_for_month VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    villa_id BIGINT REFERENCES villas(id),
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_apartments_villa_id ON apartments(villa_id);
CREATE INDEX IF NOT EXISTS idx_expenses_villa_id ON expenses(villa_id);
CREATE INDEX IF NOT EXISTS idx_payments_villa_id ON payments(villa_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_villa_id ON service_requests(villa_id);
CREATE INDEX IF NOT EXISTS idx_vendors_region ON vendors(region);

-- Migration helpers for existing databases
ALTER TABLE vendors ADD COLUMN IF NOT EXISTS region VARCHAR(255);
ALTER TABLE payments ALTER COLUMN apartment_id DROP NOT NULL;
