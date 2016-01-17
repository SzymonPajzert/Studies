(*Hashowanie, dehashowanie i szukanie następnych możliwych ruchów do zadania przelewanka*)

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

    let hash x =
      assert (Array.length maxarray = Array.length x);
      let multiplier = ref 1 in
      let result = ref 0 in
      let iterator index value =
        result := !result + value * !multiplier;
        multiplier := (maxarray.(index) + 1) * !multiplier
      in
      Array.iteri iterator x;
      !result

    let dehash x =
      let divisor = ref 1 in
      let remainder = ref x in
      let initialiser index =
        divisor := (maxarray.(index) + 1) * !divisor;
        let result = !remainder mod !divisor in
        remainder := (!remainder - result) / (maxarray.(index) + 1);
        result
      in
      Array.init (Array.length maxarray) initialiser

    let next x = [x]
  end
