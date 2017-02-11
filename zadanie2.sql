#standardSQL
CREATE TEMPORARY FUNCTION sect(x FLOAT64)
RETURNS INT64 AS (CAST(TRUNC(SIGN(x) * (ABS(x)-(100))/(200)) AS INT64)); 

CREATE TEMPORARY FUNCTION long(x FLOAT64, y FLOAT64)
AS ((y + 73.985) * 111316.66 * COS(3.141592 * x / 180));

CREATE TEMPORARY FUNCTION lat(x FLOAT64)
AS ((x - 40.748) * (111316.66));

CREATE TEMPORARY FUNCTION areFar(x1 INT64, y1 INT64, x2 INT64, y2 INT64)
RETURNS BOOL AS (ABS(x1 - x2) > 1 OR ABS(y1 - y2) > 1);
  
SELECT
  tripduration AS bike_trip_tripduration,
  starttime AS bike_trip_starttime,
  stoptime AS bike_trip_stoptime,
  start_station_id AS bike_trip_start_station_id,
  start_station_name AS bike_trip_start_station_name,
  start_station_latitude AS bike_trip_start_station_latitude,
  start_station_longitude AS bike_trip_start_station_longitude,
  end_station_id AS bike_trip_end_station_id,
  end_station_name AS bike_trip_end_station_name,
  end_station_latitude AS bike_trip_end_station_latitude,
  end_station_longitude AS bike_trip_end_station_longitude,
  bikeid AS bike_trip_bikeid,
  usertype AS bike_trip_usertype,
  birth_year AS bike_trip_birth_year,
  gender AS bike_trip_gender,
  vendor_id AS taxi_trip_vendor_id,
  pickup_datetime AS taxi_trip_pickup_datetime,
  dropoff_datetime AS taxi_trip_dropoff_datetime,
  pASsenger_count AS taxi_trip_pASsenger_count,
  trip_distance AS taxi_trip_trip_distance,
  pickup_longitude AS taxi_trip_pickup_longitude,
  pickup_latitude AS taxi_trip_pickup_latitude,
  dropoff_longitude AS taxi_trip_dropoff_longitude,
  dropoff_latitude AS taxi_trip_dropoff_latitude
FROM 
  ( SELECT * FROM (
	SELECT *, sect(lat(start_station_latitude)) as by1, sect(lat(end_station_latitude)) as by2,
	sect(long(start_station_latitude, start_station_longitude)) as bx1, sect(long(end_station_latitude, end_station_longitude)) as bx2
  	FROM `tag2016-bd.new_york.citibike_trips`
	) WHERE areFar(bx1, by1, bx2, by2) ) INNER JOIN ( SELECT * FROM (
	  SELECT *, sect(lat(pickup_latitude)) as ty1, sect(lat(dropoff_latitude)) as ty2,
	  sect(long(pickup_latitude, pickup_longitude)) as tx1, sect(long(dropoff_latitude, dropoff_longitude)) as tx2
	  FROM `tag2016-bd.new_york.tlc_yellow_trips_2016`
    ) WHERE areFar(tx1, ty1, tx2, ty2) ) 
    
	  ON DATE(pickup_datetime) = DATE(stoptime)
	  	 AND DATE(starttime) = DATE(dropoff_datetime)
		 AND (UNIX_SECONDS(stoptime) - UNIX_SECONDS(starttime)) < (UNIX_SECONDS(dropoff_datetime) - UNIX_SECONDS(pickup_datetime))
		 AND bx1 = tx1
		 AND bx2 = tx2
		 AND by1 = ty1
		 AND by2 = ty2
  
  
  
