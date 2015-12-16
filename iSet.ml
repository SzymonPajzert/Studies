(*Autor: Szymon Pajzert
Code Reviewer: Dominik Klemba
Testy w paczce i manualtest.ml*)

(*Funkcje dla przedziałów*)
type interval = int * int

(*Sprawdzenie, czy nie jest to zdegenerowany przedział*)
let is_interval (x, y) =
  x <= y

(*Jeśli x należy do interwału zwraca 0, w przeciwnym wypadku zwraca z której strony leży*)
let cmp_el x (begin_interval, end_interval) =
  if begin_interval <= x && x <= end_interval then 0
  else if x < begin_interval then -1
  else 1

(*Jeśli zbiory się przecinają lub są sąsiadami, to porównanie zwraca 0. W przeciwnym wypadku
zwraca relację między dowolnymi elementami przedziałów*)
let cmp (a1, b1) (a2, b2) =
  if b1 + 1 < a2 then -1
  else if b2 + 1 < a1 then 1
  else 0

(*Łączenie dwóch przedziałów przy założeniu, że ich cmp jest 0*)
let interval_merge i1 i2 =
  assert (cmp i1 i2 = 0);
  min (fst i1) (fst i2), max (snd i1) (snd i2)

(*Dodawanie, wykorzystywane tylko do dodawania nieujemnych wartości liczebności*)
let ( +/ ) a b =
  assert (a >= 0 && b >= 0);
  if b < max_int - a then a + b else max_int

(*Funckje zbioru*)

(*Lewy syn, interwał, prawy syn, wysokość, moc elementów przedziałów w poddrzewie*)
type t =
  | Empty
  | Node of t * interval * t * int * int

let empty = Empty

(*Sprawdzanie, czy jest pusty zbiór*)
let is_empty set =
  set = Empty

(*Zwraca przechowywaną w wierzchołkach wysokość*)
let height = function
  | Node (_, _, _, h, _) -> h
  | Empty -> 0

(*Zwraca przechowywaną w wierzchołkach liczebność zbioru*)
let cardinality = function
  | Node (_, _, _, _, c) -> c
  | Empty -> 0

(*Tworzy drzewo*)
let make l k r =
  let newcardinality = cardinality l +/ cardinality r +/ (snd k - fst k + 1) in
  Node (l, k, r, max (height l) (height r) + 1, newcardinality)

(*Jednorazowo balansuje i zwraca AVL z dwóch drzew AVL i korzenia*)
let bal l k r =
  let hl = height l in
  let hr = height r in
  if hl > hr + 2 then
    match l with
    | Empty -> assert false
    | Node (ll, lk, lr, _, _) ->
        if height ll >= height lr then make ll lk (make lr k r)
        else
          (match lr with
          | Empty -> assert false
          | Node (lrl, lrk, lrr, _, _) ->
              make (make ll lk lrl) lrk (make lrr k r))
  else if hr > hl + 2 then
    match r with
    | Empty -> assert false
    | Node (rl, rk, rr, _, _) ->
        if height rr >= height rl then make (make l k rl) rk rr
        else
          (match rl with
          | Empty -> assert false
          | Node (rll, rlk, rlr, _, _) ->
              make (make l k rll) rlk (make rlr rk rr))
  else make l k r

(*Prosta funckja skopiowana z STD*)
let rec min_elt = function
  | Empty -> raise Not_found
  | Node (Empty, k, _, _, _) -> k
  | Node (l, _, _, _, _) -> min_elt l

(*Prosta funkcja skopiowana z STD*)
let rec remove_min_elt = function
  | Empty -> invalid_arg "ISet.remove_min_elt"
  | Node (Empty, _, r, _, _) -> r
  | Node (l, k, r, _, _) -> bal (remove_min_elt l) k r

(*Dodawanie przedziału do zbioru, przy założeniu, że cmp tego przedziału i każdego innego
jest różne od zera. Jeśli jest to zgenerowany przedział, zwraca identyczność*)
let rec add_one x =
  if not (is_interval x) then function set -> set
  else
    function
    | Empty -> Node (Empty, x, Empty, 1, (snd x - fst x) +/ 1)
    | Node (l, k, r, h, _) ->
        let c = cmp x k in
        if c = 0 then assert false
        else if c < 0 then
          let nl = add_one x l in
          bal nl k r
        else
          let nr = add_one x r in
          bal l k nr

(*Tworzy drzewo AVL z dwóch drzew AVL i wartości dla korzenia, przy założeniu,
że pierwsze ma mniejsze przedziały od drugiego*)
let rec join l v r =
  match (l, r) with
  | Empty, _ -> add_one v r
  | _, Empty -> add_one v l
  | Node(ll, lv, lr, lh, _), Node(rl, rv, rr, rh, _) ->
      if lh > rh + 2 then bal ll lv (join lr v r)
      else if rh > lh + 2 then bal (join l v rl) rv rr
      else make l v r

(*Prosta funkcja zmodyfikowana z STD*)
let split x set =
  let rec loop x = function
    | Empty -> (Empty, false, Empty)
    | Node (l, v, r, _, _) ->
        let c = cmp_el x v in
        if c = 0 then (add_one (fst v, x - 1) l, true, add_one (x + 1, snd v) r)
        else if c < 0 then
          let (ll, pres, rl) = loop x l in (ll, pres, join rl v r)
        else
          let (lr, pres, rr) = loop x r in (join l v lr, pres, rr)
  in
    loop x set

(*Prosta pętla skopiowana z STD*)
let mem x set =
  let rec loop = function
    | Empty -> false
    | Node (l, k, r, _, _) ->
        let c = cmp_el x k in
        c = 0 || loop (if c < 0 then l else r)
  in
  loop set

(*Prosta pętla skopiowana z STD*)
let iter f set =
  let rec loop = function
    | Empty -> ()
    | Node (l, k, r, _, _) -> loop l; f k; loop r in
  loop set

(*Prosta pętla skopiowana z STD*)
let fold f set acc =
  let rec loop acc = function
    | Empty -> acc
    | Node (l, k, r, _, _) ->
          loop (f k (loop acc l)) r in
  loop acc set

(*Prosta pętla skopiowana z STD*)
let elements set =
  let rec loop acc = function
    | Empty -> acc
    | Node(l, k, r, _, _) -> loop (k :: loop acc r) l in
  loop [] set

(*Dzielę splitem i sprawdzam liczebność zbioru mniejszego*)
let below x set =
  let (rem, mem, _) = split x set in
  cardinality rem +/ (if mem then 1 else 0)

(*Jak nazwa wskasuje, join bez wskazanego korzenia*)
let nonrootjoin l r =
  match (l, r) with
  | Empty, _ -> r
  | _, Empty -> l
  | _, _ ->
    let k = min_elt r in
    join l k (remove_min_elt r)

(*Używam splita do otrzymania dwóch drzew, które łączę*)
let remove i set =
  let (l, _, cr) = split (fst i) set in
  let (_, _, r) = split (snd i) cr in
  nonrootjoin l r

(*zwracam parę: zbiór z usuniętymi przedziałami których cmp z przedziałem
wejściowym wynosi 0 oraz sumę tych usuniętych przedziałów i tego przedziału*)
let intersectremove i set =
  let rec loop x = function
    | Empty -> (Empty, x)
    | Node (l, v, r, _, _) ->
        let c = cmp x v in
        if c = 0 then
          let m = interval_merge x v in
          let (nl, ul) = loop m l in
          let (nr, ur) = loop m r in
          (nonrootjoin nl nr, interval_merge ul ur)
        else if c < 0 then
          let (nl, u) = loop x l in (join nl v r, u)
        else
          let (nr, u) = loop x r in (join l v nr, u)
  in
    loop i set

(*Zapewniam niezmiennik dla add_one, dodaję*)
let add i set =
  let (nset, u) = intersectremove i set in
  add_one u nset
