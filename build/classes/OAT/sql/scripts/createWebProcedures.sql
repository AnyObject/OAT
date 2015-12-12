/*    Open Auto Trading : A fully automated equities trading platform    Copyright (C) 2015 AnyObject Ltd.    This program is free software: you can redistribute it and/or modify    it under the terms of the GNU General Public License as published by    the Free Software Foundation, either version 3 of the License, or    (at your option) any later version.    This program is distributed in the hope that it will be useful,    but WITHOUT ANY WARRANTY; without even the implied warranty of    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    GNU General Public License for more details.    You should have received a copy of the GNU General Public License    along with this program.  If not, see <http://www.gnu.org/licenses/>. */DELIMITER //DROP FUNCTION IF EXISTS `toTimeLong`//CREATE FUNCTION `toTimeLong`(dt DATETIME)  RETURNS BIGINT  DETERMINISTIC  RETURN UNIX_TIMESTAMP(dt) * 1000//DROP FUNCTION IF EXISTS `toTimeStamp`//CREATE FUNCTION `toTimeStamp`(timeLong BIGINT)  RETURNS DATETIME  DETERMINISTIC  RETURN FROM_UNIXTIME(FLOOR(timeLong / 1000))//DROP FUNCTION IF EXISTS `side`//CREATE FUNCTION `side`(side VARCHAR(9))  RETURNS VARCHAR(50)  DETERMINISTIC  RETURN   (CASE     WHEN side = 'BOT' THEN 1    WHEN side = 'SLD' THEN -1    ELSE  0  END)//DROP PROCEDURE IF EXISTS `web`.`strategies`//CREATE PROCEDURE `web`.`strategies` (acctNumber CHAR(20))  SELECT     IF(CHAR_LENGTH(Symbol) > 8, CONCAT(SUBSTR(Symbol, 1, 7), '.'), Symbol) As Symbol,      IF(Pos IS NULL, 0, Pos) AS `Pos`,    IF(Pos != 0, `Enter`, NULL) AS `Enter`,    IF(Pos != 0, `Stop`, `Exit`) AS `Exit`,      addSign(FORMAT((IF(Last, Pos * Last, 0) + Value) * m_multiplier / m_priceMagnifier - IF(Cost, Size * Cost, 0), 0)) AS `Net`,    `m_currency` AS `Cur`  FROM    (SELECT       *,      SUM(side(`Action`) * `Size`) AS Pos,      SUM(-side(`Action`) * `Size` * `Price`) AS Value,      SUM(commission) AS Cost      FROM `web`.`executions`      WHERE INSTR(m_acctNumber, acctNumber) > 0      GROUP BY `ID`        ) AS t1 RIGHT JOIN (SELECT * FROM `web`.`symbols` JOIN `web`.`quotes` USING (`ID`)) AS t2 USING (`ID`)  WHERE INSTR(m_group, acctNumber) > 0  ORDER BY `ID`//DROP PROCEDURE IF EXISTS `web`.`quotes`//CREATE PROCEDURE `web`.`quotes` (acctNumber CHAR(20))  SELECT     IF(CHAR_LENGTH(Symbol) > 9, CONCAT(LEFT(Symbol, 8), '.'), Symbol) As Symbol,      High,    Low,    Last,    addSign(ROUND(Chg, IF(ABS(Chg) > 100, 0, 2))) AS Chg,    roundVol(Vol) AS Vol  FROM `web`.`symbols` JOIN `web`.`quotes` USING (`ID`)  WHERE INSTR(m_group, acctNumber) > 0  ORDER BY `ID`//DROP PROCEDURE IF EXISTS `web`.`executions`//CREATE  PROCEDURE `web`.`executions` (acctNumber CHAR(20))  SELECT    IF(CHAR_LENGTH(Symbol) > 9, CONCAT(LEFT(Symbol, 8), '.'), Symbol) As Symbol,      DATE_FORMAT(`execTime`,'%a %H:%i:%s') AS HKT,    Action,    Size,    Price,    CONCAT(SUBSTR(m_acctNumber, 1, 4), '*', RIGHT(m_acctNumber, 1)) AS Acct  FROM `web`.`symbols` JOIN `web`.`executions` USING (`ID`)  WHERE INSTR(m_acctNumber, acctNumber) > 0  ORDER BY `ID`, `execTime`, `Acct`//DROP PROCEDURE IF EXISTS `web`.`clearWebExecutions`//CREATE PROCEDURE `web`.`clearWebExecutions` (strategyId INT, since BIGINT)  DELETE LOW_PRIORITY FROM `web`.`executions`  WHERE `ID` = strategyId  AND `execTime` < toTimeStamp(since)//DROP PROCEDURE IF EXISTS `web`.`clearWebQuote`//CREATE PROCEDURE `web`.`clearWebQuote` (strategyId INT)  UPDATE `web`.`quotes`  SET High = null,      Low = null,      Chg = null,      Vol = null  WHERE `ID` = strategyId//DROP FUNCTION IF EXISTS `web`.`addSign`//CREATE FUNCTION `web`.`addSign`(num VARCHAR(50))  RETURNS VARCHAR(50)  RETURN CONCAT(IF(REPLACE(num, ',', '') > 0, '+', ''), num)//DROP FUNCTION IF EXISTS `web`.`roundVol`//CREATE FUNCTION `web`.`roundVol`(n1 DOUBLE)  RETURNS VARCHAR(50)  BEGIN    DECLARE u VARCHAR(1);    DECLARE v DOUBLE;    DECLARE r INT;    IF n1 >= 1000000 THEN       SET v = n1 / 1000000;      SET u = 'M';    ELSEIF n1 >= 1000 THEN       SET v = n1 / 1000;      SET u = 'K';        ELSE      SET v = n1;      SET u = '';          END IF;    IF u = '' OR v >= 1000 THEN       SET r = 0;    ELSEIF v >= 100 THEN       SET r = 1;        ELSE      SET r = 2;    END IF;    RETURN CONCAT(ROUND(v, r), u);  END //DELIMITER ;