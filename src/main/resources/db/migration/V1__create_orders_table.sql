CREATE TABLE orders
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        VARCHAR(100)   NOT NULL,
    ticker         VARCHAR(20)    NOT NULL,
    quantity       DECIMAL(19, 4) NOT NULL,
    price          DECIMAL(19, 4) NOT NULL,
    executed_price DECIMAL(19, 4),
    side           ENUM('BUY', 'SELL') NOT NULL,
    status         ENUM('PENDING', 'FILLED', 'REJECTED', 'CANCELLED')  DEFAULT 'PENDING',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_user_id ON orders (user_id);
CREATE INDEX idx_order_status ON orders (status);