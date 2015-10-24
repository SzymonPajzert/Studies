type wartosc =
   Pusty |
   Przedzial of (bool * float * float)
(* czy zbior jest jednoprzedzialowy * mniejszy koniec * wiekszy koniec *)

let wartosc_dokladnosc x p = Przedzial of (true, x-.x*.p/.100., x+.x*.p/.100)
(* zbior jest jednoprzedzialowy, konce sa wyznaczone jako pesymistyczna dokladnosc *)

let wartosc_dokladna x = Przedzial of (true, x, x)
(* zbior jest jednoprzedzialowy, konce sa podana liczba *)

let wartosc_od_do x y = Przedzial of (true, x, y)
(* zbior jest jednoprzedzialowy, konce sa podane*)

let min_wartosc = function
   Pusty -> nan
   Przedzial (xa, xb, xc) ->
      if xa
      then xb
      else neg_infinity

(*Jesli zbior jest jednoprzedzialowy to minimum jest jego mniejszy koniec, w przeciwnym wypadku neg_infinity *)

let max_wartosc = function
   Pusty -> nan
   Przedzial (xa, xb, xc)
      if xa
      then xc
      else infinity
(*Jesli zbior jest jednoprzedzialowy to maximum jest jego wiekszy koniec, w przeciwnym wypadku infinity *)

(*let przeciwienstwo (x:wartosc) =
   let (a, b, c) = x
   in
      ((a, -. b, -. c):wartosc)
(* Przedzialowa liczebnosc sie nie zmienia, zmieniamy jedynie znaki koncowek *)

let plus (x:wartosc) (y:wartosc) =
   let ((xa, xb, xc), (ya, yb, yc)) = (x, y)
   in
      if (xa && ya)
      then
         (true, (xb +. yb), (xc +. yc))
         (*Jesli oba zbiory sa jednoprzedzialowe, nowe konce sa dodanymi koncami *)
      else
         (false, 0., 0.)
	 (*UWAGA heura*)

let minus x y = plus x (przeciwienstwo y)
*)
