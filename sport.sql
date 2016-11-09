drop table UczestnicyZawodow;
drop table Zawody;
drop table Sportowcy;

create table Sportowcy (
  id number primary key,
  imie varchar(50) NOT NULL,
  nazwisko varchar(50) NOT NULL,
  trener number references Sportowcy
);

create table Zawody (
  id number primary key,
  nazwa varchar(30) not null,
  poczatek date NOT NULL,
  koniec date NOT NULL
);

create table UczestnicyZawodow (
  zawody references Zawody not null,
  zawodnik references Zawodnicy not null,
  dyscyplina varchar(20),
  miejsce number,
  primary key (zawody,zawodnik,dyscyplina)
);
