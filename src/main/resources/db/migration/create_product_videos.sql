-- SQL Script to manually create the product_videos table
-- This is a fallback in case Hibernate auto-ddl is disabled.

CREATE TABLE IF NOT EXISTS product_videos (
    video_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    video_url VARCHAR(1000) NOT NULL,
    video_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_product_video FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);
