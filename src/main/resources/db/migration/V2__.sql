drop table staffs;
drop table customers;

CREATE TABLE IF NOT EXISTS shipping_contact
(
    id             BIGSERIAL PRIMARY KEY,
    address_detail VARCHAR(255),
    recipient_name VARCHAR(255),
    phone          VARCHAR(20),
    is_default     BOOLEAN NOT NULL         DEFAULT FALSE,
    user_id        BIGINT  NOT NULL,

    -- Các trường từ BaseEntity
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted     BOOLEAN NOT NULL         DEFAULT FALSE,

    -- Khóa ngoại liên kết tới bảng users
    CONSTRAINT fk_shipping_contact_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);