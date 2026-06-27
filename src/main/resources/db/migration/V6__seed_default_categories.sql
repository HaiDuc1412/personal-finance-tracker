-- V6__seed_default_categories.sql

INSERT INTO categories (id, name, icon, color, type, is_default, user_id) VALUES
                                                                              (uuid_generate_v4(), 'Ăn uống',        'food',          '#FF6B6B', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Di chuyển',       'car',           '#4ECDC4', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Nhà ở',           'home',          '#45B7D1', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Giải trí',        'entertainment', '#96CEB4', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Sức khoẻ',        'health',        '#88D8B0', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Mua sắm',         'shopping',      '#FFEAA7', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Giáo dục',        'education',     '#DDA0DD', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Uncategorized',   'folder',        '#B0B0B0', 'EXPENSE', true, NULL),
                                                                              (uuid_generate_v4(), 'Lương',           'salary',        '#55EFC4', 'INCOME',  true, NULL),
                                                                              (uuid_generate_v4(), 'Thu nhập khác',   'income',        '#74B9FF', 'INCOME',  true, NULL);