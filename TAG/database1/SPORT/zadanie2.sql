SELECT QQ.imie, QQ.nazwisko, QQ.liczba
FROM
(
	SELECT 
		Sportowcy.id,  
		Sportowcy.imie,
		Sportowcy.nazwisko,
		COALESCE(Z.liczba, 0) AS liczba
	FROM Sportowcy 
		LEFT JOIN (
			SELECT UczestnicyZawodow.zawodnik, count(UczestnicyZawodow.*) AS liczba
			FROM UczestnicyZawodow 
				JOIN (
					SELECT Zawody.id 
					FROM Zawody
					WHERE poczatek < CURRENT_DATE AND CURRENT_DATE < koniec 
				) AS Q
				ON UczestnicyZawodow.zawody = Q.id
			GROUP BY UczestnicyZawodow.zawodnik
		) AS Z
		ON Sportowcy.id = Z.zawodnik
) AS QQ
WHERE QQ.liczba = (SELECT MAX(Z.liczba) FROM (
			SELECT UczestnicyZawodow.zawodnik, count(UczestnicyZawodow.*) AS liczba
			FROM UczestnicyZawodow 
				JOIN (
					SELECT Zawody.id 
					FROM Zawody
					WHERE poczatek < CURRENT_DATE AND CURRENT_DATE < koniec 
				) AS Q
				ON UczestnicyZawodow.zawody = Q.id
			GROUP BY UczestnicyZawodow.zawodnik
		) AS Z)
