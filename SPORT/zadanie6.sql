SELECT trenerzy.imie, trenerzy.nazwisko, sportowcy.imie, sportowcy.nazwisko
FROM
	sportowcy JOIN (SELECT * FROM sportowcy) AS trenerzy
	ON  sportowcy.trener = trenerzy.id
