#standardSQL
CREATE TEMPORARY FUNCTION extractLatitude(x FLOAT64) AS ((x - 40.748) * (111316.66));
CREATE TEMPORARY FUNCTION extractLongitude(x FLOAT64, y FLOAT64) AS ((y + 73.985655556) * 111316.66 * COS(3.141592 * x / 180));
CREATE TEMPORARY FUNCTION getSector(x FLOAT64) AS (TRUNC(SIGN(x) * (ABS(x)-100)/200));
  
WITH 
  taxi_p AS (SELECT pickup_latitude AS x, pickup_longitude as y FROM `tag2016-bd.new_york.tlc_yellow_trips_2016`),
  taxi_d AS (SELECT dropoff_latitude AS x, dropoff_longitude as y FROM `tag2016-bd.new_york.tlc_yellow_trips_2016`),
  bike_p AS (SELECT  start_station_latitude AS x, start_station_longitude as y FROM `tag2016-bd.new_york.citibike_trips`),
  bike_d AS (SELECT end_station_latitude AS x, end_station_longitude as y FROM `tag2016-bd.new_york.citibike_trips`),
  coord AS (select * from taxi_p union all select * from taxi_d union all select * from bike_p union all select * from bike_d)
SELECT DISTINCT getSector(extractLatitude(x)) as x, getSector(extractLongitude(x, y)) as y
FROM coord
ORDER BY x, y
