#standardSQL
CREATE TEMPORARY FUNCTION sect(x FLOAT64) RETURNS INT64 AS (CAST(TRUNC(SIGN(x) * (ABS(x)-(100))/(200)) AS INT64));
CREATE TEMPORARY FUNCTION long(x FLOAT64, y FLOAT64) AS ((y + 73.985) * 111316.66 * COS(3.141592 * x / 180));
CREATE TEMPORARY FUNCTION lat(x FLOAT64) AS ((x - 40.748) * (111316.66));
CREATE TEMPORARY FUNCTION areFar(x1 INT64, y1 INT64, x2 INT64, y2 INT64) RETURNS BOOL AS (
  ABS(x1 - x2) > 1 OR ABS(y1 - y2) > 1
);
  
SELECT
  b.tripduration AS bike_trip_tripduration,
  b.starttime AS bike_trip_starttime,
  b.stoptime AS bike_trip_stoptime,
  b.start_station_id AS bike_trip_start_station_id,
  b.start_station_name AS bike_trip_start_station_name,
  b.start_station_latitude AS bike_trip_start_station_latitude,
  b.start_station_longitude AS bike_trip_start_station_longitude,
  b.end_station_id AS bike_trip_end_station_id,
  b.end_station_name AS bike_trip_end_station_name,
  b.end_station_latitude AS bike_trip_end_station_latitude,
  b.end_station_longitude AS bike_trip_end_station_longitude,
  b.bikeid AS bike_trip_bikeid,
  b.usertype AS bike_trip_usertype,
  b.birth_year AS bike_trip_birth_year,
  b.gender AS bike_trip_gender,
  t.vendor_id AS taxi_trip_vendor_id,
  t.pickup_datetime AS taxi_trip_pickup_datetime,
  t.dropoff_datetime AS taxi_trip_dropoff_datetime,
  t.pASsenger_count AS taxi_trip_pASsenger_count,
  t.trip_distance AS taxi_trip_trip_distance,
  t.pickup_longitude AS taxi_trip_pickup_longitude,
  t.pickup_latitude AS taxi_trip_pickup_latitude,
  t.dropoff_longitude AS taxi_trip_dropoff_longitude,
  t.dropoff_latitude AS taxi_trip_dropoff_latitude
FROM 
  `tag2016-bd.new_york.citibike_trips` AS b CROSS JOIN `tag2016-bd.new_york.tlc_yellow_trips_2016` AS t
WHERE DATE(pickup_datetime) = DATE(b.stoptime) AND
DATE(b.starttime) = DATE(t.dropoff_datetime) AND
(UNIX_SECONDS(stoptime)- UNIX_SECONDS(starttime)) < (UNIX_SECONDS(dropoff_datetime) - UNIX_SECONDS(pickup_datetime)) AND
sect(lat(b.start_station_latitude)) = sect(lat(t.pickup_latitude)) AND
sect(lat(b.end_station_latitude)) = sect(lat(t.dropoff_latitude)) AND
sect(long(b.start_station_latitude, b.start_station_longitude)) = sect(long(t.pickup_latitude, t.pickup_longitude)) AND
sect(long(b.end_station_latitude, b.end_station_longitude)) = sect(long(t.dropoff_latitude, t.dropoff_longitude)) AND
areFar(sect(lat(b.start_station_latitude)), sect(long(b.start_station_latitude, b.start_station_longitude)), sect(lat(b.end_station_latitude)), sect(long(b.end_station_latitude, b.end_station_longitude)))
  
  
