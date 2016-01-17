(*Hashowanie, dehashowanie i szukanie następnych możliwych ruchów*)

module type MaxArray =
  sig
    val capacities: int array
  end

module type H =
  sig
    val hash: int array -> int
    val dehash: int -> int array
    val next: int -> int list
  end

module Make(MA: MaxArray) =
  struct
    let maxarray = MA.capacities
    let dim = Array.length maxarray

    let hash x =
      assert (dim = Array.length x);
      let multiplier = ref 1 in
      let result = ref 0 in
      let iterator index value =
        result := !result + value * !multiplier;
        multiplier := (maxarray.(index) + 1) * !multiplier
      in
      Array.iteri iterator x;
      !result

    let dehash x =
      let remainder = ref x in
      let initialiser index =
        let result = !remainder mod (maxarray.(index) + 1) in
        remainder := (!remainder - result) / (maxarray.(index) + 1);
        result
      in
      Array.init dim initialiser

    let next x =
      let result = ref [] in
      let curstate = dehash x in
      (* Dodawanie stanów po wylaniu wybranej szklanki oraz napałenieniu jej do pełna *)
      for index = 0 to dim - 1 do
        let after_bounce = Array.init dim (fun i -> if i = index then 0 else curstate.(i)) |> hash in
        let after_full =  Array.init dim (fun i -> if i = index then maxarray.(i) else curstate.(i)) |> hash in
        result := after_full :: after_bounce :: !result
      done;
      (* Dodawanie stanów po przelewaniu z jednej szklanki do drugiej*)
      for index = 0 to dim - 1 do
        for i = 0 to dim - 1 do
          if i = index then ()
          else
            let topour = maxarray.(i) - curstate.(i) in
            let after_pour =
              Array.init dim (fun it ->
                if it = i then maxarray.(it)
                else if it = index then curstate.(it) - topour
                else curstate.(it))
              |> hash
            in
            result := after_pour :: !result
        done
      done;
      (*Zwracanie wyniku*)
      !result
  end
