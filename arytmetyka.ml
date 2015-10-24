type wartosc =
   Pusty |
   Pojedynczy of (float * float) |
   Podwojny of (float * float)
(* czy zbior jest jednoprzedzialowy * mniejszy koniec * wiekszy koniec *)

let wartosc_dokladnosc (x:float) (p:float) = Pojedynczy (x-.x*.p/.100., x+.x*.p/.100.)
(* zbior jest jednoprzedzialowy, konce sa wyznaczone jako pesymistyczna dokladnosc *)

let wartosc_dokladna (x:float) = Pojedynczy (x, x)
(* zbior jest jednoprzedzialowy, konce sa podana liczba *)

let wartosc_od_do (x:float) (y:float) = Pojedynczy (x, y)
(* zbior jest jednoprzedzialowy, konce sa podane*)

let min_wartosc (x:wartosc) =
   match x with
   | Pusty -> nan
   | Pojedynczy (res, _) -> res
   | Podwojny -> neg_infinity
(*Jesli zbior jest jednoprzedzialowy to minimum jest jego mniejszy koniec, w przeciwnym wypadku neg_infinity *)

let max_wartosc (x:wartosc)=
   match x with
   | Pusty -> nan
   | Pojedynczy (_, res) -> res
   | Podwojny -> infinity
(*Jesli zbior jest jednoprzedzialowy to maximum jest jego wiekszy koniec, w przeciwnym wypadku infinity *)

let przeciwienstwo (x:wartosc) =
   match x with
   | Pusty -> Pusty
   | Pojedynczy (a, b) -> Pojedynczy (-.a, -.b)
   | Podwojny (a, b) -> Podwojny (-.a, -.b)
(* Przedzialowa liczebnosc sie nie zmienia, zmieniamy jedynie znaki koncowek *)

let plus (x:wartosc) (y:wartosc) =
   match x with
   | Pusty -> Pusty
   | Pojedynczy (xb, xe) ->
      match y with
      | Pusty -> Pusty
      | Pojedynczy (yb, ye) -> Pojedynczy (xb+.yb, xe+.ye)
      | Podwojny (yb, ye) -> Podwojny ((max yb (yb+xe)), (min ye (ye+xb)))
      (*Jesli poczatek x jest ujemny, to moze polepszyc nam koniec, jesli koniec x jest ujemny, to moze polepszyc nam poczatek *)
   | Podwojny (xb, xe) ->
      match y with
      | Pusty -> Pusty
      | Pojedynczy (yb, ye) ->
      | Podwojny (yb, ye) ->

let minus x y = plus x (przeciwienstwo y)
*)
