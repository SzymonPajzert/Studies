SELECT kluby.id, kluby.nazwa
FROM kluby
	LEFT JOIN bokserzy
	ON kluby.id = bokserzy.klub
WHERE bokserzy.id IS NULL;

SELECT kluby.id, kluby.nazwa
FROM kluby
	FULL JOIN bokserzy
	ON kluby.id = bokserzy.klub
WHERE bokserzy.id IS NULL;

SELECT QQ.id, QQ.nazwa
FROM (
	SELECT 
		kluby.id, 
		kluby.nazwa,
		COALESCE(Z.count, 0) AS LiczbaUczestnikow
	FROM kluby LEFT JOIN (
		SELECT klub, count(*) FROM bokserzy GROUP BY klub
	) AS Z
	ON kluby.id = Z.klub
) AS QQ
WHERE QQ.LiczbaUczestnikow = 0;

SELECT kluby.id, kluby.nazwa
FROM kluby
	LEFT JOIN (
	SELECT kluby.id 
	FROM kluby 
		JOIN bokserzy
		ON bokserzy.klub = kluby.id
	) AS Q
ON kluby.id = Q.id
WHERE Q.id IS NULL


