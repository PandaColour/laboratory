drop database IF EXISTS small_bank;
-- 创建数据库
CREATE DATABASE IF NOT EXISTS small_bank
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE small_bank;

-- 账户表
DROP TABLE IF EXISTS account_tab;
CREATE TABLE account_tab (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    account_code VARCHAR(50) NOT NULL COMMENT '账号号,客户可见',
    internal_code VARCHAR(50) NOT NULL UNIQUE COMMENT '内部唯一标识,内部代号',
    pre_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '上一次余额',
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '当前余额',
    latest_seq_no VARCHAR(50) NOT NULL DEFAULT '' COMMENT '最后一次修改的流水号',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（用于乐观锁）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uniq_account_code (account_code),
	UNIQUE KEY uniq_internal_code (internal_code)
) ENGINE=InnoDB COMMENT '账户信息表';

INSERT INTO account_tab (account_code, internal_code,current_balance,latest_seq_no) VALUES
    ('660011001', 'IN000001', 1000.00, ''),
    ('660011002', 'IN000002', 2000.00, ''),
    ('660011003', 'IN000003', 3000.00, ''),
    ('660011004', 'IN000004', 4000.00, '');


-- 交易历史表
DROP TABLE IF EXISTS tran_history_tab;
CREATE TABLE tran_history_tab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    seq_no VARCHAR(50) NOT NULL COMMENT '最新流水号',
    pre_seq_no VARCHAR(50) COMMENT '前一个流水号',
    from_account INT NOT NULL COMMENT '转出账户ID',
    to_account INT NOT NULL COMMENT '转入账户ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    tran_type VARCHAR(20) NOT NULL COMMENT '交易类型',
    tran_direction ENUM('D', 'C') NOT NULL COMMENT '资金方向, D-Debit贷, C-Credit借',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：PROCESS-交易中, SUCCESS-成功',
    reverse VARCHAR(20) default NULL COMMENT '冲正标志, NULL-未冲正, X-已冲正',
    reverse_seq_no VARCHAR(50) default NULL COMMENT '冲正流水号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY idx_seq_no (seq_no,tran_direction,tran_type)
) ENGINE=InnoDB COMMENT '资金变动流水表';

-- 交易表
DROP TABLE IF EXISTS tran_info_tab;
CREATE TABLE tran_info_tab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    seq_no VARCHAR(50) NOT NULL COMMENT '流水号',
    from_account INT NOT NULL COMMENT '转出账户ID',
    to_account INT NOT NULL COMMENT '转入账户ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    tran_type VARCHAR(20) NOT NULL COMMENT '交易类型',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：BEGIN-开始,PROCESS-交易中, SUCCESS-成功, FAILED-失败',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY idx_seq_no(seq_no)
) ENGINE=InnoDB COMMENT '资金变动流水表';



















