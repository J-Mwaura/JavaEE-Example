--------------------------------------------------------
-- Schema soekmdb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `soekmdb` DEFAULT CHARACTER SET utf8 ;
USE `soekmdb` ;

-- -----------------------------------------------------
-- Table `soekmdb`.`articlecategory_table`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`articlecategory_table` (
  `ARTICLECATEGORY_ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ARTICLECATEGORY_NAME` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`ARTICLECATEGORY_ID`))
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`person`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`person` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `FIRSTNAME` VARCHAR(50) NOT NULL,
  `LASTNAME` VARCHAR(100) NULL DEFAULT NULL,
  `EMAIL` VARCHAR(45) NOT NULL,
  `ADDRESS` VARCHAR(45) NULL DEFAULT NULL,
  `CITY` VARCHAR(45) NULL DEFAULT NULL,
  `PASSWORD` VARCHAR(100) NULL DEFAULT NULL,
  `ENABLED` TINYINT(1) NULL DEFAULT NULL,
  `SECRET` VARCHAR(45) NULL DEFAULT NULL,
  `EXPIRYDATE` DATE NULL DEFAULT NULL,
  `TOKEN` VARCHAR(200) NULL DEFAULT NULL,
  `EMAIL_VERIFICATION_ATTEMPTS` INT(2) NULL DEFAULT NULL,
  `EMAIL_VERIFICATION_HASH` VARCHAR(45) NULL DEFAULT NULL,
  `STATUS` VARCHAR(15) NOT NULL,
  `DTYPE` VARCHAR(31) NULL DEFAULT NULL,
  `DATE_REGISTERED` TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00',
  `AVATAR` BLOB NULL DEFAULT NULL,
  `IMAGE` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`, `EMAIL`),
  UNIQUE INDEX `EMAIL` (`EMAIL` ASC),
  UNIQUE INDEX `SQL_PERSON_EMAIL_INDEX` (`EMAIL` ASC),
  UNIQUE INDEX `SQL_PERSON_ID_INDEX` (`ID` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 57
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`article_table`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`article_table` (
  `ARTICLEI_ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ARTICLE_TITLE` VARCHAR(300) NULL DEFAULT NULL,
  `ARTICLE_PREFACE` VARCHAR(300) NULL DEFAULT NULL,
  `ARTICLE_BODY` LONGTEXT NOT NULL,
  `VIEWS` INT(11) NULL DEFAULT NULL,
  `IMAGE_MAIN` LONGBLOB NULL DEFAULT NULL,
  `IMAGE` VARCHAR(45) NULL DEFAULT NULL,
  `DATE_CREATED` TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00',
  `DATE_UPDATED` DATETIME NOT NULL DEFAULT '2000-01-01 00:00:00',
  `PERSON_ID` INT(11) NOT NULL,
  `ARTICLECATEGORY_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ARTICLEI_ID`),
  UNIQUE INDEX `ARTICLEI_ID_UNIQUE` (`ARTICLEI_ID` ASC),
  INDEX `fk_article_table_person_idx` (`PERSON_ID` ASC),
  INDEX `fk_article_table_articleCategory_table1_idx` (`ARTICLECATEGORY_ID` ASC),
  CONSTRAINT `fk_article_table_articleCategory_table1`
    FOREIGN KEY (`ARTICLECATEGORY_ID`)
    REFERENCES `soekmdb`.`articlecategory_table` (`ARTICLECATEGORY_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_article_table_person`
    FOREIGN KEY (`PERSON_ID`)
    REFERENCES `soekmdb`.`person` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`comment_table`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`comment_table` (
  `COMMENT_ID` INT(11) NOT NULL AUTO_INCREMENT,
  `COMMENT_BODY` VARCHAR(800) NULL DEFAULT NULL,
  `DATE_CREATED` TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00',
  `URL` VARCHAR(200) NULL DEFAULT NULL,
  `PERSON_ID` INT(11) NOT NULL,
  `ARTICLEI_ID` INT(11) NOT NULL,
  PRIMARY KEY (`COMMENT_ID`),
  INDEX `fk_comment_table_person1_idx` (`PERSON_ID` ASC),
  INDEX `fk_comment_table_article_table1_idx` (`ARTICLEI_ID` ASC),
  CONSTRAINT `fk_comment_table_article_table1`
    FOREIGN KEY (`ARTICLEI_ID`)
    REFERENCES `soekmdb`.`article_table` (`ARTICLEI_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_comment_table_person1`
    FOREIGN KEY (`PERSON_ID`)
    REFERENCES `soekmdb`.`person` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`comment_reply`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`comment_reply` (
  `REPLY_ID` INT(11) NOT NULL AUTO_INCREMENT,
  `REPLY_BODY` VARCHAR(800) NULL DEFAULT NULL,
  `DATE_CREATED` TIMESTAMP NULL DEFAULT '2000-01-01 00:00:00',
  `URL` VARCHAR(200) NULL DEFAULT NULL,
  `COMMENT_ID` INT(11) NOT NULL,
  PRIMARY KEY (`REPLY_ID`),
  INDEX `fk_comment_replie_comment_table_idx` (`COMMENT_ID` ASC),
  CONSTRAINT `fk_comment_replie_comment_table`
    FOREIGN KEY (`COMMENT_ID`)
    REFERENCES `soekmdb`.`comment_table` (`COMMENT_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`commentstatus_table`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`commentstatus_table` (
  `COMMENTSTATUS_ID` INT(11) NOT NULL AUTO_INCREMENT,
  `COMMENT_STATUS` VARCHAR(45) NULL DEFAULT NULL,
  `COMMENT_DESCRIPTION` VARCHAR(200) NULL DEFAULT NULL,
  PRIMARY KEY (`COMMENTSTATUS_ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`groups`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`groups` (
  `ID` INT(11) NOT NULL,
  `NAME` VARCHAR(50) NOT NULL,
  `DESCRIPTION` VARCHAR(300) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`person_groups`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`person_groups` (
  `GROUPS_ID` INT(11) NOT NULL,
  `EMAIL` VARCHAR(45) NOT NULL,
  INDEX `SQL_PERSONGROUPS_EMAIL_INDEX` (`EMAIL` ASC),
  INDEX `SQL_PERSONGROUPS_ID_INDEX` (`GROUPS_ID` ASC),
  CONSTRAINT `FK_PERSON_GROUPS_GROUPS`
    FOREIGN KEY (`GROUPS_ID`)
    REFERENCES `soekmdb`.`groups` (`ID`),
  CONSTRAINT `FK_PERSON_GROUPS_PERSON`
    FOREIGN KEY (`EMAIL`)
    REFERENCES `soekmdb`.`person` (`EMAIL`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `soekmdb`.`profile`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `soekmdb`.`profile` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `AVATAR` BLOB NULL DEFAULT NULL,
  `IMG` VARCHAR(45) NULL DEFAULT NULL,
  `PERSON_ID` INT(11) NOT NULL,
  `EMAIL` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`ID`, `PERSON_ID`, `EMAIL`),
  INDEX `fk_profile_person_idx` (`PERSON_ID` ASC, `EMAIL` ASC),
  CONSTRAINT `fk_profile_person`
    FOREIGN KEY (`PERSON_ID` , `EMAIL`)
    REFERENCES `soekmdb`.`person` (`ID` , `EMAIL`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;
