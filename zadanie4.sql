#standardSQL
WITH 
  times AS ((SELECT starttime as t, 1 as c
  FROM `tag2016-bd.new_york.citibike_trips`)
  UNION ALL
  (SELECT stoptime as t, -1 as c
  FROM `tag2016-bd.new_york.citibike_trips`)),
  
  result AS (
    SELECT t, SUM(c) OVER (ORDER BY t) as s
    FROM times
    ORDER BY s DESC
    LIMIT 1)
    
  select t from result
