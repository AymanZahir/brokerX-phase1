CREATE DATABASE brokerx_monolith;
CREATE DATABASE brokerx_auth;
CREATE DATABASE brokerx_portfolio;
CREATE DATABASE brokerx_orders;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT FROM pg_roles WHERE rolname = 'brokerx'
  ) THEN
    CREATE ROLE brokerx LOGIN PASSWORD 'brokerx';
  END IF;
END$$;

GRANT ALL PRIVILEGES ON DATABASE brokerx_monolith TO brokerx;
GRANT ALL PRIVILEGES ON DATABASE brokerx_auth TO brokerx;
GRANT ALL PRIVILEGES ON DATABASE brokerx_portfolio TO brokerx;
GRANT ALL PRIVILEGES ON DATABASE brokerx_orders TO brokerx;
