SELECT Sportowcy.imie, Sportowcy.nazwisko, Sportowcy.id, Q.id
FROM Sportowcy 
	LEFT JOIN (
	SELECT Sportowcy.id 
	FROM Sportowcy 
		JOIN UczestnicyZawodow
		ON Sportowcy.id = UczestnicyZawodow.zawodnik
		WHERE UczestnicyZawodow.miejsce = 1
	) AS Q
ON Sportowcy.id = Q.id
WHERE Q.id IS NULL
