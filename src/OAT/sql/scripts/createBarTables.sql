/*
    Open Auto Trading : A fully automatic equities trading platform with machine learning capabilities
    Copyright (C) 2015 AnyObject Ltd.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

drop table `bar_values`;
drop table `indicators`;
drop table `indicator_types`;
drop table `bars`;


CREATE  TABLE IF NOT EXISTS `bars` (
  `bar_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `bar_time` BIGINT NOT NULL ,
  `bar_interval` BIGINT NOT NULL ,
  `bar_complete` TINYINT(1) NULL ,
  `m_conId` INT NOT NULL ,
  PRIMARY KEY (`bar_id`) ,
  UNIQUE INDEX `bar_id_UNIQUE` (`bar_id` ASC) )
ENGINE=INNODB;

CREATE  TABLE IF NOT EXISTS `indicator_types` (
  `indicator_type` SMALLINT UNSIGNED NOT NULL ,
  `name` VARCHAR(20) NOT NULL ,
  `long_name` VARCHAR(90) NULL ,
  `desc` VARCHAR(255) NULL ,
  `license` TINYINT NULL ,
  PRIMARY KEY (`name`) ,
  UNIQUE INDEX `indicator_type_UNIQUE` (`indicator_type` ASC) )
ENGINE=INNODB;

CREATE  TABLE IF NOT EXISTS `indicators` (
  `indicator_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `indicator_type` SMALLINT UNSIGNED NOT NULL ,
  `parameters` VARCHAR(90) NULL ,
  PRIMARY KEY (`indicator_type`, `parameters`) ,
  UNIQUE INDEX `indicator_id_UNIQUE` (`indicator_id` ASC),
  FOREIGN KEY (`indicator_type`) REFERENCES indicator_types(`indicator_type`) ON UPDATE CASCADE ON DELETE RESTRICT )
ENGINE=INNODB;

CREATE  TABLE IF NOT EXISTS `bar_values` (
  `bar_id` BIGINT UNSIGNED NOT NULL ,
  `indicator_id` BIGINT UNSIGNED NOT NULL ,
  `value` DOUBLE NULL ,
  PRIMARY KEY (`bar_id`, `indicator_id`) ,
  FOREIGN KEY (`indicator_id`) REFERENCES indicators(`indicator_id`)  ON UPDATE CASCADE ON DELETE RESTRICT ,
  FOREIGN KEY (`bar_id`) REFERENCES bars(`bar_id`) ON UPDATE CASCADE ON DELETE CASCADE )
ENGINE=INNODB;

INSERT INTO `indicator_types` 
  (`indicator_type`, `name`, `long_name`)
VALUES 
  (0, 'PREV_CLOSE', NULL),
  (1, 'OPEN', NULL),
  (2, 'HIGH', NULL),
  (3, 'LOW', NULL),
  (4, 'CLOSE', NULL),
  (5, 'WAP', 'Weighted Average Price'),
  (6, 'MA', 'Moving Average'),
  (7, 'CHA', 'Channel'),
  (8, 'RSI', 'Relative Strength Index'),
  (9, 'STO', 'Stochastic'),
  (10, 'STAR', 'Candlestick Star'),
  (11, 'ENGULFING', 'Candlestick Engulfing')
;

