INSERT INTO student_user (id, phone, nickname, password, status)
VALUES (1, '13800000001', '学生甲', '{noop}123456', 1)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), password = VALUES(password), status = VALUES(status);

INSERT INTO merchant (id, merchant_name, contact_name, contact_phone, status, settle_type, campus_code)
VALUES (1, '一食堂轻食档口', '张老板', '13900000001', 1, 1, 'MAIN')
ON DUPLICATE KEY UPDATE merchant_name = VALUES(merchant_name), contact_name = VALUES(contact_name), contact_phone = VALUES(contact_phone);

INSERT INTO merchant_user (id, merchant_id, username, password, real_name, status)
VALUES (1, 1, 'merchant01', '{noop}123456', '张老板', 1)
ON DUPLICATE KEY UPDATE merchant_id = VALUES(merchant_id), password = VALUES(password), real_name = VALUES(real_name), status = VALUES(status);

INSERT INTO sys_admin (id, username, password, real_name, status)
VALUES (1, 'admin', '{noop}123456', '平台管理员', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), real_name = VALUES(real_name), status = VALUES(status);

INSERT INTO delivery_user (id, username, password, real_name, phone, status)
VALUES (1, 'rider01', '{noop}123456', '配送员王一', '13700000001', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), real_name = VALUES(real_name), phone = VALUES(phone), status = VALUES(status);

INSERT INTO store (id, merchant_id, store_name, address, delivery_type, delivery_scope_desc, delivery_radius_km, min_order_amount, delivery_fee, business_status, notice, deleted)
VALUES (1, 1, '一食堂轻食档口', '主校区一食堂二楼', 2, '主校区,一食堂,二食堂,东区宿舍', 3.50, 15.00, 2.00, 1, '满 15 元起送', 0)
ON DUPLICATE KEY UPDATE store_name = VALUES(store_name), address = VALUES(address), delivery_type = VALUES(delivery_type),
                        delivery_scope_desc = VALUES(delivery_scope_desc), delivery_radius_km = VALUES(delivery_radius_km),
                        min_order_amount = VALUES(min_order_amount), delivery_fee = VALUES(delivery_fee),
                        business_status = VALUES(business_status), notice = VALUES(notice), deleted = VALUES(deleted);

INSERT INTO store_business_hours (store_id, day_of_week, start_time, end_time, status)
VALUES (1, 1, '07:00:00', '22:00:00', 1),
       (1, 2, '07:00:00', '22:00:00', 1),
       (1, 3, '07:00:00', '22:00:00', 1),
       (1, 4, '07:00:00', '22:00:00', 1),
       (1, 5, '07:00:00', '22:00:00', 1),
       (1, 6, '07:00:00', '22:00:00', 1),
       (1, 7, '07:00:00', '22:00:00', 1)
ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = now();

INSERT INTO product_category (id, store_id, category_name, sort_no, status)
VALUES (1, 1, '热销套餐', 1, 1)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), sort_no = VALUES(sort_no), status = VALUES(status);

INSERT INTO product_spu (id, store_id, category_id, product_name, product_type, image_url, description, sale_status, sort_no)
VALUES (1, 1, 1, '黄焖鸡米饭', 1, 'https://example.com/images/hmj.jpg', '招牌爆款，适合午餐晚餐', 1, 1),
       (2, 1, 1, '香辣鸡腿饭', 1, 'https://example.com/images/jtr.jpg', '学生热销口味', 1, 2),
       (3, 1, 1, '双拼超值套餐', 2, 'https://example.com/images/combo.jpg', '黄焖鸡 + 香辣鸡腿双拼，适合双人分享', 1, 3)
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name), product_type = VALUES(product_type),
                        image_url = VALUES(image_url), description = VALUES(description),
                        sale_status = VALUES(sale_status), sort_no = VALUES(sort_no);

INSERT INTO product_sku (id, spu_id, sku_name, price, stock, sold_num, version, status)
VALUES (1, 1, '标准份', 18.00, 100, 0, 0, 1),
       (2, 2, '标准份', 16.00, 120, 0, 0, 1),
       (3, 3, '套餐标准份', 29.00, 80, 0, 0, 1)
ON DUPLICATE KEY UPDATE sku_name = VALUES(sku_name), price = VALUES(price), stock = VALUES(stock),
                        sold_num = VALUES(sold_num), version = VALUES(version), status = VALUES(status);

INSERT INTO combo (id, spu_id, combo_desc, status)
VALUES (1, 3, '黄焖鸡米饭 x1 + 香辣鸡腿饭 x1', 1)
ON DUPLICATE KEY UPDATE spu_id = VALUES(spu_id), combo_desc = VALUES(combo_desc), status = VALUES(status);

INSERT INTO combo_item (combo_id, sku_id, quantity, sort_no)
VALUES (1, 1, 1, 1),
       (1, 2, 1, 2)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), sort_no = VALUES(sort_no), updated_at = now();

INSERT INTO seckill_activity (id, store_id, sku_id, activity_name, seckill_price, stock, start_time, end_time, status)
VALUES (1, 1, 1, '午高峰秒杀黄焖鸡', 9.90, 30, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1)
ON DUPLICATE KEY UPDATE store_id = VALUES(store_id), sku_id = VALUES(sku_id), activity_name = VALUES(activity_name),
                        seckill_price = VALUES(seckill_price), stock = VALUES(stock),
                        start_time = VALUES(start_time), end_time = VALUES(end_time), status = VALUES(status);

INSERT INTO coupon (id, coupon_name, coupon_type, discount_amount, threshold_amount, store_id, start_time, end_time, total_count, receive_count, status)
VALUES (1, '全场通用立减5元', 1, 5.00, 20.00, NULL, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 10000, 0, 1),
       (2, '食堂档口满30减8', 1, 8.00, 30.00, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 5000, 0, 1)
ON DUPLICATE KEY UPDATE coupon_name = VALUES(coupon_name), coupon_type = VALUES(coupon_type),
                        discount_amount = VALUES(discount_amount), threshold_amount = VALUES(threshold_amount),
                        store_id = VALUES(store_id), start_time = VALUES(start_time), end_time = VALUES(end_time),
                        total_count = VALUES(total_count), status = VALUES(status);

INSERT INTO user_address (id, user_id, campus_name, building_name, room_no, detail_address, contact_name, contact_phone, is_default, status)
VALUES (1, 1, '主校区', '东区宿舍', '3-402', '东区宿舍3号楼402', '学生甲', '13800000001', 1, 1)
ON DUPLICATE KEY UPDATE campus_name = VALUES(campus_name), building_name = VALUES(building_name),
                        room_no = VALUES(room_no), detail_address = VALUES(detail_address),
                        contact_name = VALUES(contact_name), contact_phone = VALUES(contact_phone),
                        is_default = VALUES(is_default), status = VALUES(status);
