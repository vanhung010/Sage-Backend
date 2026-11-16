-- V2 chạy sau V1__create_sage_tables.sql trong schema sage.
-- Đích: TiDB Cloud Starter PostgreSQL-compatible.
-- Dựa trên đầy đủ các cột của V1 đã gửi, kể cả thông tin người nhận và snapshot món.
-- Dữ liệu NULL/trùng/không hợp lệ có sẵn phải được xử lý trước khi chạy V2.
-- Không tự điền giá, địa chỉ, lịch sử thanh toán hay xóa bản ghi để vượt ràng buộc.
-- PRIMARY KEY và FOREIGN KEY đã có trong V1.
-- UNIQUE tạo index nội bộ để bảo đảm không trùng. Không thêm index truy vấn.
-- Backend chuẩn hóa email bằng trim + lowercase trước khi INSERT/UPDATE.
-- Thời hạn và gửi lại OTP, phân quyền, quyền sở hữu, điều kiện đánh giá,
-- chuyển trạng thái, tổng tiền đơn và khoản hoàn cần được kiểm tra ở service.

-- users
ALTER TABLE sage.users ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE sage.users ALTER COLUMN email SET NOT NULL;
ALTER TABLE sage.users ALTER COLUMN password SET NOT NULL;
ALTER TABLE sage.users ALTER COLUMN role SET NOT NULL;
ALTER TABLE sage.users ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.users ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.users ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE sage.users
    ADD CONSTRAINT uq_users_email UNIQUE (email);
ALTER TABLE sage.users
    ADD CONSTRAINT ck_users_email_normalized CHECK (email <> '' AND email = lower(btrim(email)));
ALTER TABLE sage.users
    ADD CONSTRAINT ck_users_full_name CHECK (btrim(full_name) <> '');
ALTER TABLE sage.users
    ADD CONSTRAINT ck_users_password CHECK (btrim(password) <> '');
ALTER TABLE sage.users
    ADD CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'CUSTOMER', 'STAFF'));

-- categories
ALTER TABLE sage.categories ALTER COLUMN name SET NOT NULL;
ALTER TABLE sage.categories ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.categories ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.categories ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE sage.categories
    ADD CONSTRAINT ck_categories_name CHECK (btrim(name) <> '');

-- menus
ALTER TABLE sage.menus ALTER COLUMN name SET NOT NULL;
ALTER TABLE sage.menus ALTER COLUMN price SET NOT NULL;
ALTER TABLE sage.menus ALTER COLUMN is_available SET NOT NULL;
ALTER TABLE sage.menus ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.menus ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.menus ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE sage.menus
    ADD CONSTRAINT ck_menus_name CHECK (btrim(name) <> '');
ALTER TABLE sage.menus
    ADD CONSTRAINT ck_menus_price CHECK (price BETWEEN 0 AND 999999999999);

-- shipping_contact
ALTER TABLE sage.shipping_contact ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE sage.shipping_contact ALTER COLUMN phone SET NOT NULL;
ALTER TABLE sage.shipping_contact ALTER COLUMN address_detail SET NOT NULL;
ALTER TABLE sage.shipping_contact ALTER COLUMN is_default SET NOT NULL;
ALTER TABLE sage.shipping_contact ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.shipping_contact ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.shipping_contact ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE sage.shipping_contact
    ADD CONSTRAINT ck_shipping_contact_phone CHECK (btrim(phone) <> '');
ALTER TABLE sage.shipping_contact
    ADD CONSTRAINT ck_shipping_contact_address CHECK (btrim(address_detail) <> '');
-- Khóa sinh chỉ có giá trị với địa chỉ mặc định còn hiệu lực.
-- Nhiều địa chỉ thường/xóa mềm có NULL, nên vẫn cùng tồn tại được.
ALTER TABLE sage.shipping_contact
    ADD COLUMN active_default_user_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN is_default AND NOT is_deleted THEN user_id ELSE NULL END
    ) STORED;
ALTER TABLE sage.shipping_contact
    ADD CONSTRAINT uq_shipping_contact_active_default UNIQUE (active_default_user_id);

-- orders
ALTER TABLE sage.orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN order_code SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN delivery_address SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN total_amount SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN status SET NOT NULL;

ALTER TABLE sage.orders ALTER COLUMN version SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.orders ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE sage.orders
    ADD CONSTRAINT uq_orders_order_code UNIQUE (order_code);
ALTER TABLE sage.orders
    ADD CONSTRAINT ck_orders_order_code CHECK (btrim(order_code) <> '');
ALTER TABLE sage.orders
    ADD CONSTRAINT ck_orders_total_amount CHECK (total_amount BETWEEN 0 AND 999999999999);
ALTER TABLE sage.orders
    ADD CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DENIED', 'DELIVERING', 'SUCCESSFUL', 'PREPARING', 'REFUND'));

ALTER TABLE sage.orders
    ADD CONSTRAINT ck_orders_reason CHECK (status <> 'DENIED' OR (reason IS NOT NULL AND btrim(reason) <> ''));
ALTER TABLE sage.orders
    ADD CONSTRAINT ck_orders_version CHECK (version >= 0);
ALTER TABLE sage.orders
    ADD CONSTRAINT ck_orders_completed_at CHECK (completed_at IS NULL OR completed_at >= created_at);

-- order_items
ALTER TABLE sage.order_items ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN menu_id SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN quantity SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN unit_price SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN sub_total SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.order_items ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE sage.order_items
    ADD CONSTRAINT ck_order_items_quantity CHECK (quantity > 0);
ALTER TABLE sage.order_items
    ADD CONSTRAINT ck_order_items_unit_price CHECK (unit_price BETWEEN 0 AND 999999999999);
ALTER TABLE sage.order_items
    ADD CONSTRAINT ck_order_items_sub_total CHECK (sub_total BETWEEN 0 AND 999999999999 AND sub_total = quantity * unit_price);

-- carts
ALTER TABLE sage.carts ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE sage.carts ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.carts ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.carts
    ADD CONSTRAINT uq_carts_customer UNIQUE (customer_id);

-- cart_items
ALTER TABLE sage.cart_items ALTER COLUMN cart_id SET NOT NULL;
ALTER TABLE sage.cart_items ALTER COLUMN menu_id SET NOT NULL;
ALTER TABLE sage.cart_items ALTER COLUMN quantity SET NOT NULL;
ALTER TABLE sage.cart_items ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.cart_items ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE sage.cart_items
    ADD CONSTRAINT uq_cart_items_cart_menu UNIQUE (cart_id, menu_id);
ALTER TABLE sage.cart_items
    ADD CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0);

-- order_status_history
ALTER TABLE sage.order_status_history ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE sage.order_status_history ALTER COLUMN to_status SET NOT NULL;
ALTER TABLE sage.order_status_history ALTER COLUMN changed_by SET NOT NULL;
ALTER TABLE sage.order_status_history ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.order_status_history
    ADD CONSTRAINT ck_order_history_from_status CHECK (from_status IN ('PENDING', 'ACCEPTED', 'DENIED', 'DELIVERING', 'SUCCESSFUL', 'PREPARING', 'REFUND'));
ALTER TABLE sage.order_status_history
    ADD CONSTRAINT ck_order_history_to_status CHECK (to_status IN ('PENDING', 'ACCEPTED', 'DENIED', 'DELIVERING', 'SUCCESSFUL', 'PREPARING', 'REFUND'));
ALTER TABLE sage.order_status_history
    ADD CONSTRAINT ck_order_history_initial_status CHECK (from_status IS NOT NULL OR to_status = 'PENDING');
ALTER TABLE sage.order_status_history
    ADD CONSTRAINT ck_order_history_changed CHECK (from_status IS NULL OR from_status <> to_status);
ALTER TABLE sage.order_status_history
    ADD CONSTRAINT ck_order_history_reason CHECK (to_status <> 'DENIED' OR (reason IS NOT NULL AND btrim(reason) <> ''));

-- payment_transactions
ALTER TABLE sage.payment_transactions ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE sage.payment_transactions ALTER COLUMN type SET NOT NULL;
ALTER TABLE sage.payment_transactions ALTER COLUMN method SET NOT NULL;
ALTER TABLE sage.payment_transactions ALTER COLUMN status SET NOT NULL;
ALTER TABLE sage.payment_transactions ALTER COLUMN amount SET NOT NULL;
ALTER TABLE sage.payment_transactions ALTER COLUMN reference SET NOT NULL;
ALTER TABLE sage.payment_transactions ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT uq_payment_transactions_reference UNIQUE (reference);
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_reference CHECK (btrim(reference) <> '');
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_type CHECK (type IN ('PAYMENT', 'REFUND'));
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_method CHECK (method IN ('COD', 'VNPAY'));
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_amount CHECK (amount > 0 AND amount <= 999999999999);
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_original CHECK (
        (type = 'PAYMENT' AND original_payment_id IS NULL)
        OR (type = 'REFUND' AND original_payment_id IS NOT NULL AND original_payment_id <> id)
    );
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_confirmation CHECK ((confirmed_by IS NULL) = (confirmed_at IS NULL));
ALTER TABLE sage.payment_transactions
    ADD CONSTRAINT ck_payment_confirmed_at CHECK (confirmed_at IS NULL OR confirmed_at >= created_at);

-- reviews
ALTER TABLE sage.reviews ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE sage.reviews ALTER COLUMN menu_id SET NOT NULL;
ALTER TABLE sage.reviews ALTER COLUMN source_order_item_id SET NOT NULL;
ALTER TABLE sage.reviews ALTER COLUMN rating SET NOT NULL;
ALTER TABLE sage.reviews ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.reviews
    ADD CONSTRAINT uq_reviews_customer_menu UNIQUE (customer_id, menu_id);
ALTER TABLE sage.reviews
    ADD CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5);

-- verification_challenges
ALTER TABLE sage.verification_challenges ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE sage.verification_challenges ALTER COLUMN purpose SET NOT NULL;
ALTER TABLE sage.verification_challenges ALTER COLUMN code_hash SET NOT NULL;
ALTER TABLE sage.verification_challenges ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.verification_challenges ALTER COLUMN expires_at SET NOT NULL;
ALTER TABLE sage.verification_challenges ALTER COLUMN attempt_count SET NOT NULL;
ALTER TABLE sage.verification_challenges
    ADD CONSTRAINT ck_verification_purpose CHECK (purpose IN ('PASSWORD_RESET', 'EMAIL_CHANGE'));
ALTER TABLE sage.verification_challenges
    ADD CONSTRAINT ck_verification_target_email CHECK (
        (purpose = 'PASSWORD_RESET' AND target_email IS NULL)
        OR (purpose = 'EMAIL_CHANGE' AND target_email IS NOT NULL
            AND target_email <> '' AND target_email = lower(btrim(target_email)))
    );
ALTER TABLE sage.verification_challenges
    ADD CONSTRAINT ck_verification_code_hash CHECK (btrim(code_hash) <> '');
ALTER TABLE sage.verification_challenges
    ADD CONSTRAINT ck_verification_expiry CHECK (expires_at > created_at AND expires_at <= created_at + INTERVAL '5 minutes');
ALTER TABLE sage.verification_challenges
    ADD CONSTRAINT ck_verification_attempt_count CHECK (attempt_count >= 0);
ALTER TABLE sage.verification_challenges
    ADD CONSTRAINT ck_verification_used_at CHECK (used_at IS NULL OR (used_at >= created_at AND used_at < expires_at));

-- notifications
ALTER TABLE sage.notifications ALTER COLUMN recipient_id SET NOT NULL;
ALTER TABLE sage.notifications ALTER COLUMN type SET NOT NULL;
ALTER TABLE sage.notifications ALTER COLUMN title SET NOT NULL;
ALTER TABLE sage.notifications ALTER COLUMN body SET NOT NULL;
ALTER TABLE sage.notifications ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE sage.notifications
    ADD CONSTRAINT ck_notifications_content CHECK (btrim(type) <> '' AND btrim(title) <> '' AND btrim(body) <> '');
ALTER TABLE sage.notifications
    ADD CONSTRAINT ck_notifications_read_at CHECK (read_at IS NULL OR read_at >= created_at);

-- revinfo/revchanges là bảng từ database cũ, chưa có nghiệp vụ Envers trong main.
-- Không bổ sung ràng buộc cho hai bảng này ở bước hiện tại.
