DROP TABLE IF EXISTS `ballot`;
DROP TABLE IF EXISTS `candidate`;
DROP TABLE IF EXISTS `token`;

CREATE TABLE `token` (
    `id_hash` CHAR(64) NOT NULL PRIMARY KEY
);

CREATE TABLE `candidate` (
    `id` INT NOT NULL PRIMARY KEY,

    `name` VARCHAR(256) NOT NULL
);

CREATE TABLE `ballot` (
    `id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,

    `token` INT NOT NULL,
    `candidate_id` INT NOT NULL,
    `agent_id` INT NOT NULL,
    `hash` CHAR(64) NOT NULL,
    `prev_hash` CHAR(64) NULL,
    
    UNIQUE (`token`),
    UNIQUE (`hash`),
    FOREIGN KEY (`candidate_id`) REFERENCES `candidate` (`id`)
);
