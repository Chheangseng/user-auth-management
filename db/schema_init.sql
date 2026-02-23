
CREATE TABLE user_auth (
    -- Primary Key
     id uuid DEFAULT uuidv7() PRIMARY KEY,

    -- User Info
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,

    -- Status Flags
    activate BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    risk INTEGER DEFAULT 0,

    -- Audit Timestamps (Always use TIMESTAMPTZ for UTC)
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for faster email lookups
CREATE INDEX idx_user_auth_email ON user_auth(email);

-----------------------------------------------------------
drop table if exists user_session;
CREATE TABLE user_session (
    -- Primary Key using your uuidv7 function
    id uuid DEFAULT uuidv7() PRIMARY KEY,

    -- Foreign Key linking to UserAuth
    user_auth_id UUID NOT NULL,

    -- Session specific fields
    expiry_date TIMESTAMPTZ,
    invoked BOOLEAN DEFAULT FALSE,

    -- Audit Timestamps (Using TIMESTAMPTZ for UTC)
       created_at TIMESTAMPTZ DEFAULT NOW(),
       updated_at TIMESTAMPTZ DEFAULT NOW(),

    -- Foreign Key Constraint (Fixed table name to user_auth)
    CONSTRAINT fk_user_auth
      FOREIGN KEY(user_auth_id)
      REFERENCES user_auth(id)
      ON DELETE CASCADE
);

-- 3. Performance Indexes
CREATE INDEX idx_user_session_auth_id ON user_session(user_auth_id);

-----------------------------------------------------------

drop table if exists cache_store;
CREATE UNLOGGED TABLE IF NOT EXISTS cache_store (
    cache_key TEXT PRIMARY KEY,
    cache_value JSONB NOT NULL,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cache_store_expires_at ON cache_store(expires_at);
CREATE INDEX idx_cache_value_gin ON cache_store USING GIN (cache_value);

-----------------------------------------------------------

CREATE TABLE IF NOT EXISTS login_audit (
    id BIGSERIAL PRIMARY KEY,
    user_auth_id UUID NOT NULL,

    -- Use INET for IP addresses. It handles both IPv4 and IPv6,
    -- validates format, and allows for subnet searching.
    ip_address VARCHAR(45),

    user_agent TEXT,

    -- Using a VARCHAR or SMALLINT for Enums is usually faster than the native ENUM type
    -- if you expect the statuses to change or be added to frequently.
    status VARCHAR(20) NOT NULL,

    login_time TIMESTAMP WITH TIME ZONE,
    logout_time TIMESTAMP WITH TIME ZONE,

    country_name TEXT,
    country_code CHAR(2), -- ISO 3166-1 alpha-2 codes are always 2 chars
    region TEXT,
    city TEXT,

    -- FLOAT8 is the Postgres equivalent of Java's 'double'
    latitude FLOAT8 DEFAULT 0.0,
    longitude FLOAT8 DEFAULT 0.0,

    time_zone TEXT,

    -- Using TIMESTAMPTZ (with time zone) is best practice for global apps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);