(*rekurencyjne NWD*)
let rec gcd x y =
  if x > y then gcd y x
  else
    if x = 0 then y
    else gcd (y mod x) x

let przelewanka input =
  (*Znajduje NWD wartości w danych wejściowych w celu przyspieszenia obliczeń*)
  (*Tworzy tablice zawierające pojemności szklanek i inicjalizuje moduł haszowań*)
  let gcd = Array.fold_left (fun acc (x, y) -> gcd acc (gcd x y)) 0 input in
  let max_values = Array.init (Array.length input) (fun i -> fst input.(i) / gcd) in
  let module Hasher =  Hash.Make(struct let capacities = max_values end) in
  let open Hasher in

  (*Haszuje szukany stan i tworzy tablicę wyników z przetrzymywaną liczbą kroków*)
  (*W tablicy tej -1 oznacza nie bycie jeszcze odpwiedzonym*)
  let target = Array.init (Array.length input) (fun i -> snd input.(i) / gcd) |> hash in
  let results = Array.init ((max_values |> hash) + 1) (fun _ -> -1) in

  (*Kolejka q przetrzymuje pary do przeszukania zawierające hash wartości i liczbę kroków*)
  let q = Queue.create () in
  Queue.add (0, 0) q;

  (*Pętla while przechodząca wszystkie wartości z kolejki*)
  while not (Queue.is_empty q) && results.(target) = -1 do
    let (h, num) = Queue.take q in
    if results.(h) <> -1 then ()
    else
      results.(h) <- num;
      List.iter (fun x -> if results.(x) = -1 then Queue.add (x, num+1) q else ()) (next h)
  done;

  (*Zwracanie zapisanego wyniku*)
  results.(target)
