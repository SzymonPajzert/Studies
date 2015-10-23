type wartosc = (bool * float * float)

let wartosc_dokladnosc x p = ((true, x-.x*.p/.100., x+.x*.p/.100.):wartosc)

let wartosc_dokladna x = ((true, x, x):wartosc)

let wartosc_od_do x y = ((true, x, y):wartosc)

let min_wartosc x =
   match x with
   | (a, b, _) ->
      if a
      then b
      else neg_infinity

let max_wartosc x =
   match x with
   | (a, _, b) ->
      if a
      then b
      else infinity

let przeciwienstwo (x:wartosc) =
   match x with
   | (a, b, c) ->
      (a, - b, - c)

let plus x y = 
   let ((xa, xb, xc), (ya, yb, yc)) = (x, y)
   in
      if xa && xb
      then
         (true, xb +. yb, xc +. yc)
         (*Jesli oba zbiory sa jednoprzedzialowe, nowe konce sa dodanymi koncami *)
      else
            

let minus x y = plus x (przeciwienstwo y)
