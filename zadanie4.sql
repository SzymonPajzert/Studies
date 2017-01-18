#standardSQL
WITH 
  times AS (
    (SELECT starttime as t, DATE(starttime) as day, 1 as c FROM `bigquery-public-data.new_york.citibike_trips`)
    UNION ALL
    (SELECT stoptime as t, DATE(stoptime) as day, -1 as c FROM `bigquery-public-data.new_york.citibike_trips`)),
  
  day_accum AS (
    SELECT day, sum(c) as s
    from times
    group by day),
  
  daily_accum AS (
    SELECT t, day, SUM(c) OVER (partition by day ORDER BY t) as s
    FROM times),
    
  result AS (
    SELECT t, (daily_accum.s + coalesce(day_accum.s, 0)) as coun
    FROM daily_accum LEFT JOIN day_accum
    ON daily_accum.day = DATE_ADD(day_accum.day, INTERVAL 1 DAY)
    ORDER BY coun DESC
    LIMIT 1
  )
    
  select t from result;
