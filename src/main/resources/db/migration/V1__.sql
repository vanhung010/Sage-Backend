CREATE SEQUENCE IF NOT EXISTS menus_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS order_items_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS orders_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS shipping_contact_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE menu_categories
(
    category_id BIGINT NOT NULL,
    menu_id     BIGINT NOT NULL,
    CONSTRAINT pk_menu_categories PRIMARY KEY (category_id, menu_id)
);

CREATE TABLE menus
(
    id           BIGINT       NOT NULL,
    created_at   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP(6) WITHOUT TIME ZONE,
    is_deleted   BOOLEAN,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    image_url    VARCHAR(255),
    is_available BOOLEAN      NOT NULL,
    CONSTRAINT pk_menus PRIMARY KEY (id)
);

CREATE TABLE order_items
(
    id         BIGINT  NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    is_deleted BOOLEAN,
    quantity   INTEGER NOT NULL,
    unit_price DECIMAL NOT NULL,
    sub_total  DECIMAL NOT NULL,
    order_id   BIGINT  NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id           BIGINT NOT NULL,
    created_at   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP(6) WITHOUT TIME ZONE,
    is_deleted   BOOLEAN,
    total_amount DECIMAL,
    status       VARCHAR(255),
    customer_id  BIGINT NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE revchanges
(
    rev        BIGINT NOT NULL,
    entityname VARCHAR(255)
);

CREATE TABLE revinfo
(
    rev      BIGINT NOT NULL,
    revtstmp BIGINT,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE TABLE shipping_contact
(
    id             BIGINT NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP(6) WITHOUT TIME ZONE,
    is_deleted     BOOLEAN,
    address_detail VARCHAR(255),
    recipient_name VARCHAR(255),
    phone          VARCHAR(255),
    is_default     BOOLEAN,
    user_id        BIGINT NOT NULL,
    CONSTRAINT pk_shipping_contact PRIMARY KEY (id)
);

ALTER TABLE categories
    ADD CONSTRAINT pk_categories PRIMARY KEY (id);

ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES users (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE shipping_contact
    ADD CONSTRAINT FK_SHIPPING_CONTACT_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE menu_categories
    ADD CONSTRAINT fk_mencat_on_category FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE menu_categories
    ADD CONSTRAINT fk_mencat_on_menu FOREIGN KEY (menu_id) REFERENCES menus (id);

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);

DROP TABLE customers CASCADE;

DROP TABLE staffs CASCADE;

DROP SEQUENCE customers_seq CASCADE;

DROP SEQUENCE staffs_seq CASCADE;

ALTER SEQUENCE categories_seq INCREMENT BY 50;

ALTER SEQUENCE users_seq INCREMENT BY 50;

ALTER TABLE categories
DROP
COLUMN created_at;

ALTER TABLE categories
DROP
COLUMN description;

ALTER TABLE categories
DROP
COLUMN id;

ALTER TABLE categories
DROP
COLUMN is_deleted;

ALTER TABLE categories
DROP
COLUMN name;

ALTER TABLE categories
DROP
COLUMN updated_at;

ALTER TABLE categories
    ADD created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL;

ALTER TABLE categories
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE users
DROP
COLUMN created_at;

ALTER TABLE users
DROP
COLUMN email;

ALTER TABLE users
DROP
COLUMN full_name;

ALTER TABLE users
DROP
COLUMN id;

ALTER TABLE users
DROP
COLUMN is_deleted;

ALTER TABLE users
DROP
COLUMN password;

ALTER TABLE users
DROP
COLUMN role;

ALTER TABLE users
DROP
COLUMN updated_at;

ALTER TABLE users
    ADD created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL;

ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE categories
    ADD description VARCHAR(255);

ALTER TABLE users
    ADD email VARCHAR(255) NOT NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD full_name VARCHAR(255) NOT NULL;

ALTER TABLE users
    ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE categories
    ADD id BIGINT NOT NULL PRIMARY KEY;

ALTER TABLE categories
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE users
    ADD id BIGINT NOT NULL PRIMARY KEY;

ALTER TABLE users
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE categories
    ADD is_deleted BOOLEAN;

ALTER TABLE users
    ADD is_deleted BOOLEAN;

ALTER TABLE categories
    ADD name VARCHAR(255);

ALTER TABLE users
    ADD password VARCHAR(255) NOT NULL;

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE users
    ADD role VARCHAR(255) NOT NULL;

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE categories
    ADD updated_at TIMESTAMP(6) WITHOUT TIME ZONE;

ALTER TABLE users
    ADD updated_at TIMESTAMP(6) WITHOUT TIME ZONE;