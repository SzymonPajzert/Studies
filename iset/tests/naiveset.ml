(*Funkcje dla przedziałów*)
type interval = int * int

let is_interval (x, y) = x <= y

let cmp_el x (xin, yin) =
  if xin <= x && x <= yin then 0
  else if x < xin then -1
  else 1

(*Jeśli zbiory się przecinają lub są sąsiadami, to porównanie zwraca 0. W przeciwnym wypadku
zwraca relację między dowolnymi elementami przedziałów*)
let cmp (a1, b1) (a2, b2) =
  if b1 + 1 < a2 then -1
  else if b2 + 1 < a1 then 1
  else 0

(*Łączenie dwóch przedziałów przy założeniu, że ich cmp jest 0*)
let inmerge i1 i2 =
  assert (cmp i1 i2 = 0);
  min (fst i1) (fst i2), max (snd i1) (snd i2)

let ( +/ ) a b =
  if b < max_int - a then a + b else max_int

(*Funkcje naiwnego zbioru*)
type t = interval list

let empty = []

let is_empty set =
  set = []

let add i set =
  let f x acc =
    let c = cmp (fst acc) x in
    if c < 0 then (fst acc, x::(snd acc))
    else if c = 0 then (inmerge (fst acc) x, snd acc)
    else (x, (fst acc)::(snd acc))
  in
  let (hd, tl) = List.fold_right f set (i, []) in
  hd::tl

let split x set =
  let f i acc =
    let (l, b, r) = acc in
    let c = cmp_el x i in
    if c = 0 then
      ((if is_interval (fst i, x-1) then (fst i, x-1)::l else l),
      true,
      (if is_interval (x+1, snd i) then (x+1, snd i)::r else r))
    else if c < 0 then (l, b, i::r)
    else (i::l, b, r)
  in
  List.fold_right f set ([], false, [])

let remove i set =
  let (l, _, cr) = split (fst i) set in
  let (_, _, r) = split (snd i) set in
  l @ r

let mem x set =
  List.fold_left (fun acc i -> (cmp_el x i = 0) || acc ) false set

let iter = List.iter

let fold f set acc =
  List.fold_left (fun x y -> f y x) acc set

let elements = function x -> x

let below x set =
  (*rem to zbiór mniejszych od x*)
  let (rem, _, _) = split (x+1) set in
  fold (fun i acc -> (snd i - fst i + 1) +/ acc) rem 0
