OCAMLC=ocamlc
OCAMLOPT=ocamlopt
OCAMLDEP=ocamldep
INCLUDES=
OCAMLFLAGS=$(INCLUDES) -g
OCAMLOPTFLAGS=$(INCLUDES)
OCAMLCFLAGS=$(INCLUDES)
MODS= pMap.mli pMap.ml topol.mli topol.ml
TEST= test.ml

all: $(MODS)
	$(OCAMLOPT) -c $(OCAMLOPTFLAGS) $(MODS)

test: $(MODS) $(TEST)
	$(OCAMLOPT) -o test $(OCAMLOPTFLAGS) $(MODS) $(TEST)

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
