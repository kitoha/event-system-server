ALTER TABLE tickets ADD COLUMN request_id VARCHAR(36);
CREATE UNIQUE INDEX idx_tickets_request_id ON tickets(request_id);
