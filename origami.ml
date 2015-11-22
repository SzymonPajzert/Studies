type point = float * float

type kartka = point -> int

let prostokat (x1, y1) (x2, y2) =
  function (x, y) ->
    if x1 <= x && x <= x2 && y1 <= y && y <= y2
    then 1
    else 0

let kolko (x1, y1) r =
  function (x, y) ->
    if (x -. x1) *. (x -. x1) +. (y -. y1) *. (y -. y1) <= r *. r
    then 1
    else 0

let zloz (x1, y1) (x2, y2) k = k

let skladaj l k = k
  (*Do sprawdzenia w którą stronę foldować
  List.fold_left (fun x y -> (zloz y x)) k l *)
