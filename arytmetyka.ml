type wartosc =
   float * float
(*Reprezentowany przez parę float (fst, snd).
Jeśli fst=neg_infinity && snd = infinity, to jest to zbiór liczb rzeczywistych.
Jeśli fst<=b, to zbiór jest jednoprzeziałowy
Jeśli fst>b, to jest to dwuprzedziałowy zbiór,
 będący dopełnieniem jednoprzedziałowego reprezentowanego przez (snd,fst)
Jeśli fst=snd=nan, to jest to zbiór pusty*)

let wartosc_dokladnosc (x:float) (p:float) =
   let dokladnosc = abs_float (x *. p /. 100.)
   in
      ((x -. dokladnosc, x +. dokladnosc):wartosc)
(* zbior jest jednoprzedzialowy, konce sa wyznaczone jako pesymistyczna dokladnosc *)

let wartosc_dokladna (x:float) =
   ((x,x):wartosc)
(* zbior jest jednoprzedzialowy, konce sa podana liczba *)
let wartosc_od_do (x:float) (y:float) =
   ((x, y):wartosc)
(* zbior jest jednoprzedzialowy, konce sa podane*)

let in_wartosc ((fst, snd):wartosc) (x:float) =
   let czynalezy = (fst <= x) && (x<=snd )
   in
      if fst > snd
      then not czynalezy
      else czynalezy

let min_wartosc (x:wartosc) =
   if fst x > snd x
   then neg_infinity
   else fst x
(*Jeśli jest to pełen zbiór, to odpowiedzią jest neg_infinity
Jeśli jest to zbiór jednoprzeziałowy to podajemy najmniejszy koniec
Jeśli jest to dwuprzedziałowy zbiór to zwracamy neg_infinity
Jeśli fst=b=nan, to zwracamy fst, czyli nan*)

let max_wartosc (x:wartosc)=
   if fst x > snd x
   then infinity
   else snd x
(*Jeśli jest to pełen zbiór, to odpowiedzią jest neg_infinity
Jeśli jest to zbiór jednoprzeziałowy to podajemy najmniejszy koniec
Jeśli jest to dwuprzedziałowy zbiór to zwracamy neg_infinity
Jeśli fst=b=nan, to zwracamy fst, czyli nan*)

let sr_wartosc (x:wartosc) =
   if fst x > snd x
   then nan
   else (fst x +. snd x)/.2.

let przeciwienstwo (x:wartosc) =
   ((-.snd x,-.fst x):wartosc)

(*Branie przeciwieństwa zdefiniowanego jako różnica Mińkowskiego ze zbiorem {0}
Jeśli między fst i snd istnieje jakaś relacja r ze zbioru {<, =, >} to relacja ta
zachodzi także dla -snd r -fst. Nie zmieniają się więc liczebności przedziałów.
Zbiór pusty pozostaje zbiorem pustym, cały - całym*)

let plus (x:wartosc) (y:wartosc) =
   if fst x > snd x && fst y > snd y
   then ((neg_infinity,infinity):wartosc)
   else ((fst x +. fst y,snd x +. snd y):wartosc)
(*Jeśli oba zbiory są dwuprzedziałowe, to otrzymujemy zbiór pełen*)


let minus (x:wartosc) (y:wartosc) = plus x (przeciwienstwo y)

let odwrotnosc (x:wartosc) =
   match x with
   | (a,b) when a=neg_infinity && b=infinity -> ((neg_infinity,infinity):wartosc)
   | (0., 0.) -> ((nan,nan):wartosc)
   | (a, 0.) -> (neg_infinity, 1. /. a)
   | (a,b) -> ((1. /. b, 1. /. a):wartosc)

let razy (x:wartosc) (y:wartosc) =
   match(x,y) with
   | ((a1, b1), (a2, b2)) when a1 <= b1 && a2 <= b2 ->
      ((min (min (b1 *. b2) (a1 *. a2)) (min (a1 *. b2) (a2 *. b1))), (max (max (b1 *. b2) (a1 *. a2)) (max (a1 *. b2) (a2 *. b1))))
   (*Trywialny przypadek dla jednoprzedziałowych zbiorów.*)

   | ((a1, b1), (a2, b2)) when (a1 *. b1 *. (a1 -. b1) > 0.) && a2 > b2 ->
      ((neg_infinity, infinity):wartosc)
   (*Pierwszy zbiór zawiera otoczenie zera, drugi zawiera wystarczająco duże i małe liczby*)

   | ((a1, b1), (a2, b2)) when (a2 *. b2 *. (a2 -. b2) > 0.) && a1 > b1 ->
      ((neg_infinity, infinity):wartosc)
   (*Pierwszy zbiór zawiera wystarczająco duże i małe liczby, drugi zawiera otoczenie zera*)

   | ((a1, b1), (a2, b2)) when a1 > b1 && a2 > b2 ->
      odwrotnosc (razy (odwrotnosc x) (odwrotnosc y))
   (*Jeśli oba dwuprzedziałowe zbiory są bez otoczenia zera, to możemy wziąć odwrotność iloczynu ich odwrotności*)
   | _ -> ((nan, nan):wartosc)
   (*Jeśli żadna poprzednia opcja nie została złapana, to znaczy, że mnożyliśmy zbiór pusty*)


let podzielic (x:wartosc) (y:wartosc) = razy x (odwrotnosc y)
