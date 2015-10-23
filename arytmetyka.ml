type wartosc = (bool * float * float)
(* czy zbior jest jednoprzedzialowy * mniejszy koniec * wiekszy koniec *)

let wartosc_dokladnosc x p = ((true, x-.x*.p/.100., x+.x*.p/.100.):wartosc)
(* zbior jest jednoprzedzialowy, konce sa wyznaczone jako pesymistyczna dokladnosc *)

let wartosc_dokladna x = ((true, x, x):wartosc)
(* zbior jest jednoprzedzialowy, konce sa podana liczba *)

let wartosc_od_do x y = ((true, x, y):wartosc)
(* zbior jest jednoprzedzialowy, konce sa podane*)

let min_wartosc x =
   let (a, b, _) = x
   in
      if a
      then b
      else neg_infinity
(*Jesli zbior jest jednoprzedzialowy to minimum jest jego mniejszy koniec, w przeciwnym wypadku neg_infinity *)

let max_wartosc x =
   let (a, _, b) = x
   in
      if a
      then b
      else infinity
(*Jesli zbior jest jednoprzedzialowy to maximum jest jego wiekszy koniec, w przeciwnym wypadku infinity *)

let przeciwienstwo (x:wartosc) =
   let (a, b, c) 
   in 
      (a, - b, - c)
(* Przedzialowa liczebnosc sie nie zmienia, zmieniamy jedynie znaki koncowek *)

let plus x y = 
   let ((xa, xb, xc), (ya, yb, yc)) = (x, y)
   in
      if xa && xb
      then
         (true, xb +. yb, xc +. yc)
         (*Jesli oba zbiory sa jednoprzedzialowe, nowe konce sa dodanymi koncami *)
      else
            

let minus x y = plus x (przeciwienstwo y)
