OCAMLC=ocamlc
OCAMLOPT=ocamlopt
OCAMLDEP=ocamldep
INCLUDES=
OCAMLFLAGS=$(INCLUDES) -g
OCAMLOPTFLAGS=$(INCLUDES)

#tu nalezy wpisac wszystkie moduly do skompilowania w kolejnosci zgodnej z zaleznosciami
HASH= hash.mli hash.ml
PRZEL = przelewanka.ml
TEST = test.ml


all:
	$(OCAMLOPT) -o test $(OCAMLOPTFLAGS) $(HASH) $(PRZEL) $(TEST)

hash:
	$(OCAMLOPT) -o test $(OCAMLOPTFLAGS) $(HASH) $(TEST)

.SUFFIXES: .mll .mly .ml .mli .cmo .cmi .cmx

.mll.ml:
	ocamllex $<

.mly.mli:
	ocamlyacc $<

.mly.ml:
	ocamlyacc $<

.ml.cmo:
	$(OCAMLC) $(OCAMLFLAGS) -c $<

.mli.cmi:
	$(OCAMLC) $(OCAMLFALGS) -c $<

.ml.cmx:
	$(OCAMLOPT) $(OCAMLOPTFLAGS) -c $<

clean:
	rm -f *~ *.cm[oix] *.o *.a
