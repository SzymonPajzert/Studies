(*Dla danej tablicy maksymalnych pojemności hashuje aktualną tablicę pewnej ilości wody*)
let hash_array ~array x =
  assert (Array.length array = Array.length x);
  let multiplier = ref 1 in
  let result = ref 0 in
  let iterator index value =
    result := !result + value * !multiplier;
    multiplier := (array.(index) + 1) * !multiplier
  in
  Array.iteri iterator x;
  !result

(*Dla danej tablicy maksymalnych pojemności odhashowuje aktualną wartość do tablicy*)
let dehash_array ~array x =
  let divisor = ref 1 in
  let remainder = ref x in
  let initialiser index =
    divisor := (array.(index) + 1) * !divisor;
    let result = !remainder mod !divisor in
    remainder := (!remainder - result) / (array.(index) + 1);
    result
  in
  Array.init (Array.length array) initialiser

(*rekurencyjne NWD*)
let rec gcd x y =
  if x > y then gcd y x
  else
    if x = 0 then y
    else gcd (y mod x) x

(*
let przelewanka input =
  (*Znajduje NWD wartości w danych wejściowych w celu przyspieszenia obliczeń*)
  (*Tworzy tablice zawierające pojemności szklanek oraz hashowaną szukaną wartość*)
  let gcd = Array.fold_left (fun acc (x, y) -> gcd acc (gcd x y)) 0 input in
  let max_values = Array.init (Array.length input) (fun i -> fst input.(i) / gcd) in
  let hash = hash_array ~array:max_values in
  let target = Array.init (Array.length input) (fun i -> snd input.(i) / gcd) |> hash
*)

let hash = hash_array ~array:[| 5; 6 |]
let dehash = dehash_array ~array:[| 5; 6 |]

let t x =
  Array.iter (Printf.printf "%d ") x;
  Printf.printf "\n";
  Array.iter (Printf.printf "%d ") (x |> hash |> dehash)

let () =
  t [| 1; 3 |]
