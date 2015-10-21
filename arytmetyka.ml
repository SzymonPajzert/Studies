module Arytmetyka = struct
   type wartosc = (bool * float * float)
   let wartosc_dokladnosc x p = (true, x-.x*.p/.100., x+.x*.p/.100.)
   let wartosc_dokladna x = (true, x, x)
   let wartosc_od_do x y = (true, x, y)
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
end;;

open Arytmetyka;;
let x = Arytmetyka.wartosc_od_do 2. 10.;;
min_wartosc x;;
max_wartosc x;;
