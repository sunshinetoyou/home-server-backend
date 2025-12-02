-- 1. 사용자 (User)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL, -- HACKER, DEVELOPER, ADMIN
    settings JSONB,            -- 설정 (JSONB)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 기술 스택 & 태그 (Tag)
CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(20)       -- STACK, VULNERABILITY
);

-- 3. 개발자 랩 (DevLab)
CREATE TABLE dev_lab (
    id BIGSERIAL PRIMARY KEY,
    developer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,

    -- 3-Tier Images
    fe_image VARCHAR(255),
    be_image VARCHAR(255),
    db_type VARCHAR(20),
    db_source TEXT,

    public_url VARCHAR(255),
    is_active BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_devlab_developer FOREIGN KEY (developer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. 랩-태그 연결 (LabTag - N:M)
CREATE TABLE lab_tag (
    lab_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (lab_id, tag_id),
    CONSTRAINT fk_labtag_lab FOREIGN KEY (lab_id) REFERENCES dev_lab(id) ON DELETE CASCADE,
    CONSTRAINT fk_labtag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- 5. 해커 랩 (HackLab)
CREATE TABLE hack_lab (
    id BIGSERIAL PRIMARY KEY,
    lab_id BIGINT NOT NULL,
    hacker_id BIGINT NOT NULL,

    url VARCHAR(255),
    status VARCHAR(50),

    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_hacklab_lab FOREIGN KEY (lab_id) REFERENCES dev_lab(id) ON DELETE CASCADE,
    CONSTRAINT fk_hacklab_hacker FOREIGN KEY (hacker_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 중복 할당 방지 (1인 1랩 정책이므로, hacker_id만 유니크여도 되지만 안전하게 복합키)
    CONSTRAINT uq_hacklab_hacker UNIQUE (hacker_id)
);

-- 6. 취약점 리포트 (Report)
CREATE TABLE report (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL,
    lab_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL, -- CRITICAL, HIGH...
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ACCEPTED...
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_report_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_report_lab FOREIGN KEY (lab_id) REFERENCES dev_lab(id)
);

-- 7. 댓글 (Comments)
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_report FOREIGN KEY (report_id) REFERENCES report(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id)
);

-- 8. [Optional] 실행 이력 로그 (LabExecutionLog)
CREATE TABLE lab_execution_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    lab_id BIGINT,
    lab_title VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);