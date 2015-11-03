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
   if fst > snd
   then x >= fst || x <= snd
   else x >= fst && x <= snd

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
   else ((fst x +. fst y, snd x +. snd y):wartosc)
(*Jeśli oba zbiory są dwuprzedziałowe, to otrzymujemy zbiór pełen*)


let minus (x:wartosc) (y:wartosc) = plus x (przeciwienstwo y)

let odwrotnosc (x:wartosc) =
   match x with
   | (a,b) when a=neg_infinity && b=infinity -> ((neg_infinity,infinity):wartosc)
   | (0., 0.) -> ((nan,nan):wartosc)
   | (a, 0.) -> (neg_infinity, 1. /. a)
   | (a,b) -> ((1. /. b, 1. /. a):wartosc)

let isnan (x : float) = x <> x

let mnoz x y =
   match (x, y) with
   | (n, _) when isnan n -> nan
   | (_, n) when isnan n -> nan
   | (0., _) -> 0.
   | (_, 0.) -> 0.
   | _ -> x *. y

let rec razy (a1, b1) (a2, b2) =
      match ((a1 > b1), (a2 > b2)) with
      | (false, false) ->
         let a = min (min (mnoz a1 a2) (mnoz b1 b2)) (min (mnoz a1 b2) (mnoz a2 b1)) in
         let b = max (max (mnoz a1 a2) (mnoz b1 b2)) (max (mnoz a1 b2) (mnoz a2 b1))
         in (a, b)
      (*Trywialny przypadek dla jednoprzedziałowych zbiorów.*)
      (*Jeśli jeden ze zbiorów jest pusty, otrzymujemy zbiór pusty*)

      | (false, true) ->
         if a1 *. b1 < 0.
         then ((neg_infinity, infinity):wartosc)
         else
            if b1<=0.
            then przeciwienstwo (razy (przeciwienstwo(a1, b1)) (a2, b2))
            else
            (*Więc a1<=b1*)
            (
               match (a1 > 0., b1 > 0.) with
               (*Sprawdzam trzy rodzaje przedziału [a1,b1]*)
               | (true, true) ->
                  if a1 *. a2 <= b1 *. b2
                  then (neg_infinity, infinity)
                  else (a1 *. a2, b1 *. b2)
                  (*Jeżeli drugi zbiór ma lukę zawierającą się w liczbach dodatnich to*)
                  (**)
               | (true, false) ->
                  if a1 = 0.
                  then (neg_infinity, infinity)
                  else (a1 *. a2, a1 *. b2)
               | (false, false) ->
                  przeciwienstwo (razy (a1, b1) (przeciwienstwo(a2, b2)))
            )
      (*Jeśli zbiór jednoprzedziałowy zawiera okolice zera, to otrzymujemy zbiór liczb rzeczywistych*)
      (*Jeśli zbiór jednoprzedziałowy zawiera się w R- to bierzemy przeciwieństwo iloczynu przeciwienstwa i drugiego zbioru*)
      | (true, false) ->
         razy (a2, b2) (a1, b1)

      | (true, true) ->
         if a1 *. b1 > 0.
         then (neg_infinity, infinity)
         (*Jeśli pierwszy przedział zawiera okolice zera, to otrzymujemy zbiór liczb rzeczywistych*)
         else
            if a2 *. b2 > 0.
            then (neg_infinity, infinity)
            (*Jeśli drugi przedział zawiera okolice zera, to otrzymujemy zbiór liczb rzeczywistych*)
            else odwrotnosc (razy (odwrotnosc (a1, b1)) (odwrotnosc (a2, b2)))
            (*Jeśli oba zbiory nie zawierają oklicy zera, to są owracalne. Liczę więc odwrotnośc iloczynu odwrotności*)

let podzielic (x:wartosc) (y:wartosc) = razy x (odwrotnosc y)
