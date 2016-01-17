module type MaxArray =
  sig
    val capacities: int array
  end

module type H =
  sig
    val hash: int array -> int
    (** Przedstawienie danego stanu napełnienia w jednowymiarowej postaci*)

    val dehash: int -> int array
    (** Wyciągnięcie spowrotem stanu napełnienia z zahashowanej wartości*)

    val next: int -> int list
    (** Dla danego zahashowanego stanu zwraca wszystkie możliwe następne zahashowane stany*)
  end

module Make (MA : MaxArray) : H
(** Funktor tworzący moduł hashujący dla otrzymanej tablicy maksymalnych pojemności*)
