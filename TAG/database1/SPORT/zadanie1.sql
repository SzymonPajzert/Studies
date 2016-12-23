SELECT 
	Sportowcy.imie, 
	Sportowcy.nazwisko, 
	COALESCE(Q.count, 0) AS LiczbaZawodow
FROM Sportowcy LEFT JOIN (
	SELECT zawodnik, count(*) 
	FROM UczestnicyZawodow 
	GROUP BY zawodnik
) AS Q
ON Sportowcy.id = Q.zawodnik;

/*
SELECT 
	Sportowcy.imie, 
	Sportowcy.nazwisko, 
	COALESCE(Q.count, 0) AS LiczbaZawodow
FROM Sportowcy LEFT JOIN (
	SELECT zawodnik, count(*) 
	FROM (
		UczestnicyZawodow 
			JOIN ( SELECT Zawody.id FROM Zawody WHERE Zawody.poczatek < CURRENT_DATE AND CURRENT_DATE < Zawody.koniec ) AS AktywneZawody
			ON AktywneZawody.id = UczestnicyZawodow.zawody
	)
	GROUP BY zawodnik
) AS Q
ON Sportowcy.id = Q.zawodnik;
*/
