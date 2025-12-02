-- 1. 사용자 (비밀번호는 '1234'를 암호화한 값이라고 가정)
INSERT INTO users (username, password, nickname, role, settings) VALUES
('developer', '$2b$12$YJ099dVH9gEjm3Y3tUD8iOHIAChWQRICXc6xTsldsjH9pnTO27.5G', 'KimDev', 'DEVELOPER', '{"darkMode": true}'),
('hacker', '$2b$12$7/lNu0TfF2bOkWHT1k8lruEPcSMb9xQP5JFTvzgWmzSHC4ojMjFXO', 'LeeHacker', 'HACKER', '{"darkMode": false}'),
('admin', '$2b$12$LINn7WRJGSmdfYlHvg//beQp8Kz8ItXgbKY/7p27INkYE98d2eye2', 'AdminUser', 'ADMIN', '{}');

-- 2. 태그 (이미 V1에 INSERT가 있다면 생략 가능하지만, afterMigrate는 항상 실행되므로 중복 에러 조심)
INSERT INTO tag (name, category) VALUES
	('Spring Boot', 'STACK'),
	('React', 'STACK'),
	('Node.js', 'STACK'),
	('PostgreSQL', 'STACK'),
	('SQL Injection', 'VULNERABILITY'),
	('XSS', 'VULNERABILITY'),
	('RCE', 'VULNERABILITY');

-- 3. 개발자 랩 (데모용)
INSERT INTO dev_lab (developer_id, title, description, fe_image, be_image, public_url, is_active) VALUES
INSERT INTO dev_lab (developer_id, title, description, fe_image, be_image, db_type, db_source, public_url, is_active)
VALUES
-- Case 1: 완성된 랩 (모든 정보 있음 -> Active 가능)
(
    1,
    'SQL Injection Practice',
    '완성된 실습 예제입니다.',
    'ghcr.io/sunshinetoyou/home-server-frontend:latest',
    'ghcr.io/sunshinetoyou/home-server-backend:latest',
    'CONTAINER_IMAGE',
    'postgres:15',
    'http://lab-1-public.server.io',
    true -- 활성화됨
),
-- Case 2: 작성 중인 랩 (이미지 없음 -> Inactive 고정)
(
    1,
    'Draft Lab',
    '아직 이미지를 준비 중인 랩입니다.',
    NULL, -- [중요] 아직 입력 안 함
    NULL,
    NULL,
    NULL,
    NULL, -- URL 없음
    false -- 비활성 상태
);
-- 4. 랩-태그 연결
-- 1번 랩(SQLi)에 태그(4: SQL Injection, 1: Spring Boot) 연결 가정
INSERT INTO lab_tag (lab_id, tag_id) VALUES
(1, 4), (1, 1),
(2, 5), (2, 3);

-- 5. 리포트 (데모용)
INSERT INTO report (author_id, lab_id, title, content, severity, status) VALUES
(2, 1, '로그인 우회 취약점 발견', 'admin / '' OR 1=1 -- 로 로그인 됩니다.', 'CRITICAL', 'PENDING');

-- 6. 댓글
INSERT INTO comments (report_id, author_id, content) VALUES
(1, 1, '확인했습니다. 패치하겠습니다.');