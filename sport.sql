drop table UczestnicyZawodow;
drop table Zawody;
drop table Sportowcy;

CREATE TABLE Sportowcy (
  id INT PRIMARY KEY,
  imie varchar(50) NOT NULL,
  nazwisko varchar(50) NOT NULL,
  trener INT references Sportowcy
);

create table Zawody (
  id INT PRIMARY KEY,
  nazwa VARCHAR(30) not null,
  poczatek DATE NOT NULL,
  koniec DATE NOT NULL
);

create table UczestnicyZawodow (
  zawody INT REFERENCES Zawody NOT NULL,
  zawodnik INT REFERENCES Sportowcy not null,
  dyscyplina VARCHAR(20),
  miejsce INT,
  PRIMARY KEY (zawody, zawodnik, dyscyplina)
);
