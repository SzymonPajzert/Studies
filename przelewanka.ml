(*rekurencyjne NWD*)
let rec gcd x y =
  if x > y then gcd y x
  else
    if x = 0 then y
    else gcd (y mod x) x

let przelewanka input =
  (*Znajduje NWD wartości w danych wejściowych w celu przyspieszenia obliczeń*)
  (*Tworzy tablice zawierające pojemności szklanek i inicjalizuje moduł haszowań*)
  let dim = Array.length input in
  let gcd = max (Array.fold_left (fun acc (x, _) -> gcd acc x) 0 input) 1 in
  let max_values = Array.init (Array.length input) (fun i -> (fst input.(i)) / gcd) in
  let results = Hashtbl.create (Array.fold_left ( * ) 1 max_values) in
  (* q przetrzymuje tablice do przeszukania, results przechowuje wyniki *)
  let q = Queue.create () in
  let beg = (Array.make dim 0) in
  Queue.add beg q;
  Hashtbl.add results beg 0;

  (*Haszuje szukany stan i tworzy tablicę wyników z przetrzymywaną liczbą kroków*)
  (*W tablicy tej -1 oznacza nie bycie jeszcze odpwiedzonym*)
  let is_good = ref true in
  let target =
    let init i =
      is_good := !is_good && ((snd input.(i)) mod gcd = 0);
      snd input.(i) / gcd
    in
    Array.init (Array.length input) init
  in
  if not (!is_good) then Hashtbl.add results target (-1);

  (* Dla danego stanu zwraca obicza listę zawierającą następne możliwe i dodaje je do kolejki*)
  let next curstate num =
    let add x =
      if not (Hashtbl.mem results x) then
        (Hashtbl.add results x (num + 1);
        Queue.add x q)
    in
    (* Dodawanie stanów po wylaniu wybranej szklanki oraz napałenieniu jej do pełna *)
    for index = 0 to dim - 1 do
      Array.init dim (fun i -> if i = index then 0 else curstate.(i)) |> add;
      Array.init dim (fun i -> if i = index then max_values.(i) else curstate.(i)) |> add
    done;
    (* Dodawanie stanów po przelewaniu z jednej szklanki do drugiej*)
    for index = 0 to dim - 1 do
      for i = 0 to dim - 1 do
        if i = index then ()
        else
          let to_pour = min (max_values.(i) - curstate.(i)) curstate.(index) in
          let after_pour =
            Array.init dim (fun it ->
              if it = i then curstate.(i) + to_pour
              else if it = index then curstate.(it) - to_pour
              else curstate.(it))
          in
          add after_pour
      done
    done
  in

  (*Pętla while przechodząca wszystkie wartości z kolejki*)
  while not (Queue.is_empty q || Hashtbl.mem results target) do
    let curstate = Queue.take q in
    let num = Hashtbl.find results curstate in
    next curstate num
  done;

  (*Zwracanie zapisanego wyniku*)
  try Hashtbl.find results target
  with Not_found -> -1
