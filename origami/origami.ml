(*Author: Szymon Pajzert
Code Reviewer: Konrad Majewski*)

type point = float * float

type kartka = point -> int

let prostokat (x1, y1) (x2, y2) =
  function (x, y) ->
    (*jeśli punkt jest wewnątrz, zwracam 1*)
    if x1 <= x && x <= x2 && y1 <= y && y <= y2
    then 1
    else 0

let kolko (x1, y1) r =
  function (x, y) ->
    (*jeśli punkt jest wewnątrz, zwracam 1*)
    if (x -. x1) *. (x -. x1) +. (y -. y1) *. (y -. y1) <= r *. r
    then 1
    else 0

(*funkcja pola iloczynu wektoroweg*)
(*Znak det to znak sinusa między wektorami [x2-x1, y2-y1] [x3-x1,y3-y1]*)
let det (x1, y1) (x2, y2) (x3, y3) =
  (x2 -. x1) *. (y3 -. y1) -. (y2 -. y1) *. (x3 -. x1)

(*funkcja iloczynu skalarnego*)
let skal (x1, y1) (x2, y2) (x3, y3) =
  (x2 -. x1) *. (x3 -. x1) +. (y2 -. y1) *. (y3 -. y1)

(*funkcja symetrii środkowej (x,y) względem (xs, ys)*)
let sym (x, y) (xs, ys) = ((2. *. xs -. x), (2. *. ys -. y))

let zloz (x1, y1) (x2, y2) (k:kartka) =
  fun (x, y) ->
    let d = det (x1, y1) (x, y) (x2, y2) in
    (*W zależności od strony po której znaduje się punkt zwracam wynik*)
    (*zakładam nieskończoną dokładność float*)
    if d = 0 then k (x, y)
    else if d > 0. then 0
    else
      let s = skal (x1, y1) (x, y) (x2, y2) /. skal (x1, y1) (x2, y2) (x2, y2) in
      (*Stosunek długości rzutu wektora [x - x1, y - y1] na [x2 - x1, y2 - x1]
      do długości wektora [x2 - x1, y2 - x1]*)
      let (xs, ys) = ((x1 +. (x2 -. x1) *. s), (y1 +. (y2 -. y1) *. s)) in
      (*środek symetrii*)
      let (xo, yo) = sym (x, y) (xs, ys)
      in
        k (x,y) + k (xo, yo)

let skladaj l k =
  List.fold_left (fun x y -> (zloz (fst y) (snd y) x)) k l
