#standardSQL
# https://www.quora.com/How-can-you-calculate-the-length-of-the-day-on-Earth-at-a-given-latitude-on-a-given-date-of-the-year
CREATE TEMPORARY FUNCTION days(d DATE)
RETURNS INT64 AS (
		CASE
			WHEN 
        (MOD(EXTRACT(YEAR FROM d),4) = 0 AND MOD((EXTRACT(YEAR FROM d)),100) != 0)
				OR MOD((EXTRACT(YEAR FROM d)),400) = 0 
        THEN 365
			ELSE 366
		END
);

CREATE TEMPORARY FUNCTION rad(ang FLOAT64)
RETURNS FLOAT64 AS (3.141592 * ang / 180);

CREATE TEMPORARY FUNCTION delta(d DATE)
RETURNS FLOAT64 AS (
		rad(-23.45)  * cos(rad((360 / days(d)) * (EXTRACT(DAYOFYEAR FROM d) + 10)))
);

CREATE TEMPORARY FUNCTION psi()
RETURNS FLOAT64 AS (rad(40.748433));

CREATE TEMPORARY FUNCTION sun_equation(d DATE)
RETURNS FLOAT64 AS (ACOS(- TAN(psi()) * TAN(delta(d))));

CREATE TEMPORARY FUNCTION hour(f FLOAT64)
RETURNS INT64 AS (CAST(TRUNC(f) AS INT64));

CREATE TEMPORARY FUNCTION minute(f FLOAT64)
RETURNS INT64 AS (CAST(TRUNC((f - hour(f)) * 60) AS INT64));

CREATE TEMPORARY FUNCTION second(f FLOAT64)
RETURNS INT64 AS (CAST(TRUNC(((f - hour(f)) * 60 - minute(f)) * 60) AS INT64));

CREATE TEMPORARY FUNCTION float_to_time(f FLOAT64)
RETURNS TIME AS (TIME(hour(f), minute(f), second(f)));

CREATE TEMPORARY FUNCTION time_to_float(t TIME)
RETURNS FLOAT64 AS (
		EXTRACT(HOUR FROM t) + EXTRACT(MINUTE FROM t) / 60 + EXTRACT(SECOND FROM t) / 3600
);

CREATE TEMPORARY FUNCTION noon(d DATE)
RETURNS FLOAT64 AS (
		CASE
			WHEN EXTRACT(MONTH FROM d) < 3 OR EXTRACT(MONTH FROM d) > 10 THEN 12
			ELSE 13
		END
);

CREATE TEMPORARY FUNCTION halfday(d DATE)
RETURNS FLOAT64 AS (sun_equation(d) * 12 / 3.1415);

CREATE TEMPORARY FUNCTION sunrise(d DATE)
RETURNS FLOAT64 AS (noon(d) - halfday(d));

CREATE TEMPORARY FUNCTION sunset(d DATE)
RETURNS FLOAT64 AS (noon(d) + halfday(d));

CREATE TEMPORARY FUNCTION closest(t FLOAT64, a FLOAT64, b FLOAT64)
RETURNS FLOAT64 AS (
		CASE
			WHEN t < a THEN t - a
			WHEN t > b THEN b - t
			ELSE 
        (CASE WHEN t - a < b - t THEN t - a ELSE b - t END)
		END
);

WITH   
  times AS (
  SELECT * FROM 
    (SELECT DATE(starttime) as d, starttime as timestamp, time_to_float(TIME(starttime, "America/New_York")) as time FROM `bigquery-public-data.new_york.citibike_trips`)
    UNION ALL
    (SELECT DATE(starttime) as d, stoptime as timestamp, time_to_float(TIME(stoptime, "America/New_York")) as time FROM `bigquery-public-data.new_york.citibike_trips`)),
  
  data AS (
  SELECT d, timestamp, time, CAST(TRUNC(12 * closest(time, sunrise(d), sunset(d))) AS INT64) as offset
  FROM times),
  
  offsets AS (
  SELECT count(*) as number, offset FROM data
  GROUP by offset),
  
  gradation AS ( 
  SELECT offset, CAST(offset > 0 AS INT64) * number as day, CAST(offset <= 0 AS INT64) * number as night
  FROM offsets)
    
  SELECT sum(day), sum(night) from gradation;


--  Result
-- day: 48142817, night: 18495221
