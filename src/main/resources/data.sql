INSERT INTO department (id, code, name, status) VALUES
(1, 'RD', '研发部', 1),
(2, 'OPS', '运维部', 1),
(3, 'HR', '人力资源部', 1);

INSERT INTO employee (id, name, phone, email, department_id, position, hire_date, salary, status) VALUES
(1, '张三', '13900000001', 'zhangsan@example.com', 1, 'Java开发工程师', '2024-03-01', 15000.00, 1),
(2, '李四', '13900000002', 'lisi@example.com', 1, '测试工程师', '2024-07-15', 11000.00, 1),
(3, '王五', '13900000003', 'wangwu@example.com', 2, '运维工程师', '2023-11-20', 13000.00, 1);

