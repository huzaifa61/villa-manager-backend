-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    villa_id BIGINT,
    invite_token VARCHAR(255) UNIQUE,
    invite_expires_at TIMESTAMP,
    invite_accepted_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Villas table
CREATE TABLE IF NOT EXISTS villas (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    total_apartments INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Apartments table
CREATE TABLE IF NOT EXISTS apartments (
    id SERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL,
    apartment_number VARCHAR(50) NOT NULL,
    owner_name VARCHAR(255),
    tenant_name VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    opening_balance DECIMAL(10, 2) DEFAULT 0,
    current_balance DECIMAL(10, 2) DEFAULT 0,
    apartment_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (villa_id) REFERENCES villas(id)
);

-- Payment categories table
CREATE TABLE IF NOT EXISTS payment_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(10, 2),
    payment_date DATE,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING',
    notes TEXT,
    is_split BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (villa_id) REFERENCES villas(id),
    FOREIGN KEY (apartment_id) REFERENCES apartments(id),
    FOREIGN KEY (category_id) REFERENCES payment_categories(id)
);

-- Expense categories table
CREATE TABLE IF NOT EXISTS expense_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true
);

-- Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id SERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL,
    apartment_id BIGINT,
    category_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(10, 2),
    expense_date DATE,
    is_split BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (villa_id) REFERENCES villas(id),
    FOREIGN KEY (apartment_id) REFERENCES apartments(id),
    FOREIGN KEY (category_id) REFERENCES expense_categories(id)
);

-- Recurring expense templates table
CREATE TABLE IF NOT EXISTS recurring_expense_templates (
    id SERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL,
    apartment_id BIGINT,
    category_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    day_of_month INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT true,
    last_generated_for_month VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (villa_id) REFERENCES villas(id),
    FOREIGN KEY (apartment_id) REFERENCES apartments(id),
    FOREIGN KEY (category_id) REFERENCES expense_categories(id)
);

-- Vendors table
CREATE TABLE IF NOT EXISTS vendors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    address TEXT,
    service_type VARCHAR(100),
    is_active BOOLEAN DEFAULT true
);

-- Service requests table
CREATE TABLE IF NOT EXISTS service_requests (
    id SERIAL PRIMARY KEY,
    villa_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    vendor_id BIGINT,
    status VARCHAR(50) DEFAULT 'OPEN',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (villa_id) REFERENCES villas(id),
    FOREIGN KEY (apartment_id) REFERENCES apartments(id),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
);

-- Activity logs table
CREATE TABLE IF NOT EXISTS activity_logs (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes
CREATE INDEX idx_apartments_villa_id ON apartments(villa_id);
CREATE INDEX idx_payments_villa_id ON payments(villa_id);
CREATE INDEX idx_payments_apartment_id ON payments(apartment_id);
CREATE INDEX idx_expenses_villa_id ON expenses(villa_id);
CREATE INDEX idx_expenses_apartment_id ON expenses(apartment_id);
CREATE INDEX idx_recurring_expense_templates_villa_id ON recurring_expense_templates(villa_id);
CREATE INDEX idx_recurring_expense_templates_active ON recurring_expense_templates(is_active);
CREATE INDEX idx_service_requests_villa_id ON service_requests(villa_id);
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
