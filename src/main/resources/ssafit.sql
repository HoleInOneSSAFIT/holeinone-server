DROP DATABASE IF EXISTS ssafit;

CREATE DATABASE ssafit
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ssafit;

CREATE TABLE `user` (
                        `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AUTO_INCREMENT',
                        `username` VARCHAR(40) NOT NULL,
                        `password` VARCHAR(255) NOT NULL COMMENT '해시값으로 저장',
                        `name` VARCHAR(50) NOT NULL,
                        `nickname` VARCHAR(40) NOT NULL,
                        `profile_image` VARCHAR(500) NOT NULL COMMENT '경로 저장',
                        `gender` CHAR(1) NOT NULL COMMENT '남성(M), 여성(F)',
                        `birthdate` DATE NOT NULL,
                        `height` DECIMAL(5,2) NULL COMMENT '선택',
                        `weight` DECIMAL(5,2) NULL COMMENT '선택',
                        `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
                        `is_active` BOOLEAN NOT NULL DEFAULT true COMMENT 'false면 탈퇴 등 부정 상태',
                        `role` VARCHAR(20) NOT NULL COMMENT 'USER, ADMIN',
                        `refresh_token` TEXT NULL,
                        PRIMARY KEY (`user_id`),
                        UNIQUE KEY uk_user_username (`username`),
                        UNIQUE KEY uk_user_nickname (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 루틴 테이블 먼저 생성 (youtube_video, uploaded_video가 참조하므로)
CREATE TABLE `routine` (
                           `routine_id` BIGINT NOT NULL AUTO_INCREMENT,
                           `is_shared` BOOLEAN,
                           `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `routine_title` VARCHAR(150),
                           `routine_content` VARCHAR(500),
                           `user_id` BIGINT NOT NULL,
                           PRIMARY KEY (`routine_id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
);

CREATE TABLE `youtube_video` (
                                 `youtube_video_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AUTO_INCREMENT',
                                 `source_type` ENUM('RECOMMENDED', 'MY_UPLOAD') NOT NULL,
                                 `video_url` VARCHAR(255) NOT NULL,
                                 `title` VARCHAR(100),
                                 `duration_seconds` INT NOT NULL,
                                 `channel_name` VARCHAR(100),
                                 `rest_seconds_after` INT,
                                 `part` VARCHAR(50),
                                 `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `youtube_sequence` INT NOT NULL,
                                 `user_id` BIGINT,
                                 PRIMARY KEY (`youtube_video_id`),
                                 FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `uploaded_video` (
                                  `uploaded_video_id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `video_url` VARCHAR(500) NOT NULL,
                                  `original_filename` VARCHAR(255) NOT NULL,
                                  `title` VARCHAR(255),
                                  `rest_seconds_after` INT,
                                  `part` VARCHAR(100),
                                  `duration_seconds` INT,
                                  `user_id` BIGINT NOT NULL,
                                  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  `uploaded_sequence` INT NOT NULL,
                                  PRIMARY KEY (`uploaded_video_id`),
                                  FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `routine_video` (
                                 `routine_video_id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `sequence_order` INT NOT NULL,
                                 `routine_id` BIGINT,
                                 `youtube_video_id` BIGINT,
                                 `uploaded_video_id` BIGINT,
                                 PRIMARY KEY (`routine_video_id`),
                                 FOREIGN KEY (`routine_id`) REFERENCES `routine`(`routine_id`) ON DELETE CASCADE, -- 루틴 삭제 시 해당 루틴에 포함된 영상 연결 정보 자동 삭제,
                                 FOREIGN KEY (`youtube_video_id`) REFERENCES `youtube_video`(`youtube_video_id`) ON DELETE CASCADE,
                                 FOREIGN KEY (`uploaded_video_id`) REFERENCES `uploaded_video`(`uploaded_video_id`) ON DELETE CASCADE
);

CREATE TABLE `post` (
                        `post_id` BIGINT NOT NULL AUTO_INCREMENT,
                        `title` VARCHAR(100) NOT NULL,
                        `content` TEXT,
                        `thumbnail_url` VARCHAR(255),
                        `view_count` INT DEFAULT 0,
                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `user_id` BIGINT NOT NULL,
                        `routine_id` BIGINT,
                        PRIMARY KEY (`post_id`),
                        FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`),
                        FOREIGN KEY (`routine_id`) REFERENCES `routine`(`routine_id`) ON DELETE CASCADE -- 루틴 삭제 시 해당 루틴을 공유한 게시글 자동 삭제
);

CREATE TABLE `post_file` (
                             `post_file_id` BIGINT NOT NULL AUTO_INCREMENT,
                             `file_url` VARCHAR(500) NOT NULL,
                             `original_filename` VARCHAR(255) NOT NULL,
                             `file_type` VARCHAR(50) NOT NULL,
                             `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                             `post_id` BIGINT,
                             PRIMARY KEY (`post_file_id`),
                             FOREIGN KEY (`post_id`) REFERENCES `post`(`post_id`) ON DELETE CASCADE -- 게시글 삭제 시 첨부된 파일 자동 삭제
);

CREATE TABLE `comment` (
                           `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
                           `content` TEXT NOT NULL,
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` DATETIME,
                           `is_deleted` BOOLEAN NOT NULL DEFAULT false,
                           `depth` INT NOT NULL DEFAULT 0,
                           `user_id` BIGINT NOT NULL,
                           `post_id` BIGINT NOT NULL,
                           `parent_id` BIGINT,
                           `reply_to_id` BIGINT,
                           PRIMARY KEY (`comment_id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`),
                           FOREIGN KEY (`post_id`) REFERENCES `post`(`post_id`) ON DELETE CASCADE, -- 게시글 삭제 시 해당 게시글의 댓글도 자동 삭제,
                           FOREIGN KEY (`parent_id`) REFERENCES `comment`(`comment_id`),
                           FOREIGN KEY (`reply_to_id`) REFERENCES `comment`(`comment_id`)
);

CREATE TABLE `comment_like` (
                                `like_id` BIGINT NOT NULL AUTO_INCREMENT,
                                `user_id` BIGINT NOT NULL,
                                `comment_id` BIGINT NOT NULL,
                                UNIQUE (`user_id`, `comment_id`),
                                PRIMARY KEY (`like_id`),
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`),
                                FOREIGN KEY (`comment_id`) REFERENCES `comment`(`comment_id`)
);

CREATE TABLE post_like (
                           post_like_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           post_id BIGINT NOT NULL,
                           user_id BIGINT NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE (post_id, user_id), -- 중복 좋아요 방지
                           FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                           FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE
);

INSERT INTO `user` (
    username, password, name, nickname, profile_image,
    gender, birthdate, height, weight,
    is_active, role, refresh_token
) VALUES (
             'testuser01', 'hashedpassword123', '홍길동', '길동이', '/images/profile/test01.png',
             'M', '1995-05-20', 175.50, 68.20,
             true, 'USER', 'sample-refresh-token-abc123'
         );

-- 확인용 조회문들
select * from user;
select * from youtube_video;
select * from uploaded_video;
select * from routine;
select * from routine_video;
select * from post;
select * from post_file;
select * from comment;

-- 테스트용 더미 데이터