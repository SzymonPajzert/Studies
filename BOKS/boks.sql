drop table bokserzy;
drop table kluby;

create table kluby(
  nazwa varchar(50) unique not null,
  id int primary key,
  ranking int unique not null
);

create table bokserzy(
  id int primary key,
  nazwisko varchar(20) not null,
  klub int references kluby
);
