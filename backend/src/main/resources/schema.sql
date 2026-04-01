-- [MIGRATION] AS-IS: Nexcore DB 스키마
-- [TO-BE]: Spring Boot + H2/MySQL DDL

-- 부서 테이블 (departments)
CREATE TABLE IF NOT EXISTS departments (
    dept_id     VARCHAR(20)  NOT NULL PRIMARY KEY,
    dept_name   VARCHAR(100) NOT NULL,
    dept_head   VARCHAR(50),
    location    VARCHAR(100),
    emp_count   INT          DEFAULT 0,
    use_yn      CHAR(1)      DEFAULT 'Y',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 사원 테이블 (employees)
CREATE TABLE IF NOT EXISTS employees (
    emp_id      VARCHAR(20)  NOT NULL PRIMARY KEY,
    emp_name    VARCHAR(50)  NOT NULL,
    dept_id     VARCHAR(20),
    dept_name   VARCHAR(100),
    position    VARCHAR(30),
    hire_date   DATE,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    status      VARCHAR(10)  DEFAULT '재직',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
);

-- 매출 테이블 (sales)
CREATE TABLE IF NOT EXISTS sales (
    sales_id      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dept_id       VARCHAR(20),
    dept_name     VARCHAR(100),
    year          INT          NOT NULL,
    quarter       INT          NOT NULL,
    month         INT,
    product_name  VARCHAR(200),
    sales_amount  DECIMAL(18, 2) DEFAULT 0,
    sales_count   INT           DEFAULT 0,
    sales_avg     DECIMAL(18, 2) DEFAULT 0,
    growth_rate   DECIMAL(5, 2)  DEFAULT 0,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 게시판 테이블 (boards)
CREATE TABLE IF NOT EXISTS boards (
    board_id    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(500) NOT NULL,
    content     LONGTEXT,
    writer      VARCHAR(50),
    view_count  INT          DEFAULT 0,
    attach_count INT         DEFAULT 0,
    use_yn      CHAR(1)      DEFAULT 'Y',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 공통코드 테이블 (codes)
CREATE TABLE IF NOT EXISTS codes (
    code_group      VARCHAR(50)  NOT NULL,
    code_group_name VARCHAR(100),
    code_value      VARCHAR(50)  NOT NULL,
    code_name       VARCHAR(200) NOT NULL,
    sort_order      INT          DEFAULT 0,
    use_yn          CHAR(1)      DEFAULT 'Y',
    remark          VARCHAR(500),
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (code_group, code_value)
);
