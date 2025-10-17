-- Insert sample queues
INSERT INTO queues (name, description, type, status, workspace_id, average_service_time_minutes, created_at)
VALUES
    ('Customer Service Queue', 'General customer service inquiries', 'FIFO', 'ACTIVE', 1, 10, CURRENT_TIMESTAMP),
    ('VIP Customer Queue', 'Priority service for VIP customers', 'VIP', 'ACTIVE', 1, 5, CURRENT_TIMESTAMP),
    ('Technical Support Queue', 'Technical support and troubleshooting', 'FIFO', 'ACTIVE', 1, 15, CURRENT_TIMESTAMP);

-- Insert sample clients
INSERT INTO clients (name, email, phone, queue_id, position, status, joined_at)
VALUES
    ('John Doe', 'john.doe@example.com', '+1234567890', 1, 1, 'WAITING', CURRENT_TIMESTAMP),
    ('Jane Smith', 'jane.smith@example.com', '+1234567891', 1, 2, 'WAITING', CURRENT_TIMESTAMP),
    ('Bob Johnson', 'bob.johnson@example.com', '+1234567892', 1, 3, 'WAITING', CURRENT_TIMESTAMP);