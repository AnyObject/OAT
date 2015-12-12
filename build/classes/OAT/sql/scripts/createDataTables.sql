/*
    Open Auto Trading : A fully automated equities trading platform
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

CREATE  TABLE IF NOT EXISTS `contracts` (
  `m_conId` INT NOT NULL ,
  `m_symbol` VARCHAR(255) NOT NULL ,
  `m_secType` VARCHAR(45) NULL ,
  `m_expiry` VARCHAR(45) NULL ,
  `m_strike` DOUBLE NULL ,
  `m_right` VARCHAR(45) NULL ,
  `m_multiplier` VARCHAR(45) NULL ,
  `m_exchange` VARCHAR(255) NULL ,
  `m_currency` VARCHAR(45) NULL ,
  `m_localSymbol` VARCHAR(255) NULL ,
  `m_primaryExch` VARCHAR(255) NULL ,
  `m_includeExpired` TINYINT(1) UNSIGNED NULL ,
  `m_comboLegsDescrip` TEXT NULL ,
  `m_comboLegs` TEXT NULL ,
  `m_secIdType` VARCHAR(255) NULL ,
  `m_secId` VARCHAR(255) NULL ,
  PRIMARY KEY (`m_conId`)
) ENGINE = InnoDB;

CREATE  TABLE IF NOT EXISTS `contract_details` (
  `m_conId` INT NOT NULL ,
  `m_marketName` VARCHAR(45) NULL ,
  `m_tradingClass` VARCHAR(45) NULL ,
  `m_minTick` DOUBLE NULL ,
  `m_priceMagnifier` INT NULL ,
  `m_orderTypes` TEXT NULL ,
  `m_validExchanges` TEXT NULL ,
  `m_underConId` VARCHAR(255) NULL ,
  `m_longName` VARCHAR(255) NULL ,
  `m_cusip` VARCHAR(255) NULL ,
  `m_ratings` VARCHAR(255) NULL ,
  `m_descAppend` VARCHAR(255) NULL ,
  `m_bondType` VARCHAR(255) NULL ,
  `m_couponType` VARCHAR(255) NULL ,
  `m_callable` TINYINT(1) UNSIGNED NULL ,
  `m_putable` TINYINT(1) UNSIGNED NULL ,
  `m_coupon` DOUBLE NULL ,
  `m_convertible` TINYINT(1) UNSIGNED NULL ,
  `m_maturity` VARCHAR(255) NULL ,
  `m_issueDate` VARCHAR(255) NULL ,
  `m_nextOptionDate` VARCHAR(255) NULL ,
  `m_nextOptionType` VARCHAR(255) NULL ,
  `m_nextOptionPartial` TINYINT(1) UNSIGNED NULL ,
  `m_notes` TEXT NULL ,
  `m_contractMonth` VARCHAR(45) NULL ,
  `m_industry` TEXT NULL ,
  `m_category` TEXT NULL ,
  `m_subcategory` TEXT NULL ,
  `m_timeZoneId` VARCHAR(45) NULL ,
  `m_tradingHours` TEXT NULL ,
  `m_liquidHours` TEXT NULL ,
  PRIMARY KEY (`m_conId`)
) ENGINE = InnoDB;

CREATE  TABLE IF NOT EXISTS `trading_hours` (
  `m_exchange` VARCHAR(255) NOT NULL ,
  `open_long` BIGINT NOT NULL ,
  `close_long` BIGINT NOT NULL ,
  `timeZone` VARCHAR(255) NULL ,
  `gmt_offset` FLOAT NULL ,
  `open_local` DATETIME NULL ,
  `close_local` DATETIME NULL ,
  PRIMARY KEY (`m_exchange`, `open_long`)
) ENGINE = InnoDB;

CREATE  TABLE IF NOT EXISTS `ticks` (
  `m_conId` INT NOT NULL ,
  `tick_time_long` BIGINT NOT NULL ,  
  `tick_price` DOUBLE UNSIGNED NOT NULL ,
  `tick_size` BIGINT UNSIGNED NOT NULL ,
  `tick_newBar` TINYINT(1) UNSIGNED NULL ,
  PRIMARY KEY (`m_conId`, `tick_time_long`, `tick_price`, `tick_size`)
) ENGINE = InnoDB;

CREATE  TABLE IF NOT EXISTS `rates` (
  `rate_time` BIGINT NULL , 
  `base` VARCHAR(5) NOT NULL ,
  `forex` VARCHAR(5) NOT NULL ,  
  `rate` DOUBLE NOT NULL , 
  PRIMARY KEY (`rate_time`, `base`, `forex`)
) ENGINE = InnoDB;
