- [x] Zakładamy, że nazwiska jednoznacznie identyfikują pacjentów.  
- [x] Choroby pacjenta numerujemy w kolejności wprowadzania, poczynając od 1.  
- [x] Dostęp do ostatniej choroby danej osoby.  
- [x] Dodawanie choroby dla danej osoby  
- [x] Usuwanie ostatniej choroby danej osoby  
- [x] Aktualizowanie ostatniej choroby danej osoby.  
- [x] Pacjenci nie są usuwani z listy.  
- [x] Wypisywanie OK oraz IGNORED na stdout.  
- [x] Pamiętanie liczby wskaźników.  
- [x] Wypisywanie na stderr ```DESCRIPTIONS n``` z opcją ```-v```  
- [ ] Skrypt: ```./test.sh prog directory``` porównanie ```.in``` z ```.out```  
- [ ] Skrypt: ```./test.sh -v prog directory``` porównanie ```.in``` z ```.err``` i ```.out```  
- [ ] Żadne dwa białe znaki nie sąsiadują ze sobą.  
- [ ] Przed zakończeniem należy zwolnić całą zaalokowaną pamięć.  
- [ ] ```make debug```  tworzy ```hospital.dbg``` z opcją ```-g``` kompilacji  
- [ ] makefile osobno kompiluje każdy plik ```.c``` i osobno linkuje  
- [ ] tylko zmienione pliki kompilują się w makefile  
- [ ] ```make clean``` powoduje usunięcie wszystkich plików wykonywalnych i dodatkowych plików kompilacji.  
- [ ] Brak wycieków pamięci.  
- [ ] Obsługa parametrów i wyjście diagnostyczne  
- [ ] Dobra struktura plików.  
- [ ] Poprawne ignorowanie plików.  

### Funkcje do napisania  
#### Disease List
- [x] ```DiseaseList createDiseaseList(void)```  
- [x] ```void deleteDiseaseList(DiseaseList diseaseList)```  
- [x] ```Disease* getLastDisease(DiseaseList diseaseList)```  
- [x] ```Disease* getNthDisease(DiseaseList diseaseList, int n)```  
- [ ] ```int replaceNthDisease(DiseaseList diseaseList, int n, Disease* disease)```   
- [ ] ```void addDisease(DiseaseList diseaseList, Disease* disease)```

#### Patient
- [ ] ```Patient* createPatient(char* name);```  
- [ ] ```void deletePatient(Patient* patient);```  

#### Patient List
- [x] ```PatientList createPatientList(void)```  
- [x] ```void deletePatientList(PatientList *patientList)```  
- [x] ```Patient* addPatient(PatientList *patientList, char* name)```  
- [x] ```Patient* getPatient(PatientList patientList, char* name)```    

#### Hospital
- [x] ```Hospital* createHospital(void)```  
- [x] ```void deleteHospital(Hospital* hospital)```  
- [x] ```int getRefCount(Hospital *hospital)```  
- [x] ```void newDiseaseEnterDescription(Hospital *hospital, char* name, char* diseaseDescription)```  
- [x] ```void newDiseaseCopyDescription(Hospital *hospital, char* name1, char* name2)```  
- [x] ```void changeDescription(Hospital *hospital, char* name, int n, char *diseaseDescription)```

### Operacje na strukturze  

- [ ] ```NEW_DISEASE_ENTER_DESCRIPTION name disease description```  
- [ ] ```NEW_DISEASE_COPY_DESCRIPTION name1 name2```  
- [ ] ```CHANGE_DESCRIPTION name n disease description```  
- [ ] ```PRINT_DESCRIPTION name n```  
- [ ] ```DELETE_PATIENT_DATA name```  


### Podział na pliki

- [x]  ```structure.h```Plik nagłówkowy biblioteki wykonującej operacje na strukturze danych.  
- [x]  ```structure.c```Implementacja biblioteki wykonującej operacje na strukturze danych.  
- [x]  ```parse.h```Plik nagłówkowy biblioteki wczytującej i parsującej polecenia.  
- [x]  ```parse.c```Implementacja biblioteki wczytującej i parsującej polecenia.  
- [ ]  ```hospital.c```Główny plik programu, w którym wczytujemy wejście i wywołujemy funkcje z pliku ```structure.h```. Plik ten nie powinien znać typów danych, użytych do implementacji struktury danych.  
- [ ]   ```test.sh``` Patrz sekcja "skrypt testujący".  
- [ ]   ```Makefile``` W wyniku wywołania polecenia ```make``` powinien zostać wytworzony program wykonywalny ```hospital```.  

### Przykład

Dla danych wejściowych:

1. NEW_DISEASE_ENTER_DESCRIPTION Kowalski Bardzo ciężka choroba
2. NEW_DISEASE_ENTER_DESCRIPTION Nowak delikatne przeziębienie...
3. NEW_DISEASE_COPY_DESCRIPTION Kowalski nowak
4. NEW_DISEASE_COPY_DESCRIPTION Kowalski Nowak
5. PRINT_DESCRIPTION Kowalski 3
6. PRINT_DESCRIPTION Kowalski 2
7. CHANGE_DESCRIPTION Kowalski 2 przeziębienie z powikłaniami !
8. NEW_DISEASE_COPY_DESCRIPTION van_Beethoven Kowalski
9. NEW_DISEASE_COPY_DESCRIPTION van_Beethoven Nowak
10. DELETE_PATIENT_DATA Kowalski
11. NEW_DISEASE_COPY_DESCRIPTION Nowak Kowalski
12. PRINT_DESCRIPTION van_Beethoven 1



Poprawnym wynikiem jest:   
1. OK
2. OK
3. IGNORED
4. OK
5. IGNORED
6. delikatne przeziębienie...
7. OK
8. OK
9. OK
10. OK
11. IGNORED
12. przeziębienie z powikłaniami !

Zaś poprawny wynik na wyjście diagnostyczne to:
1. DESCRIPTIONS: 1
2. DESCRIPTIONS: 2
3. DESCRIPTIONS: 2
4. DESCRIPTIONS: 2
5. DESCRIPTIONS: 2
6. DESCRIPTIONS: 2
7. DESCRIPTIONS: 3
8.  DESCRIPTIONS: 3  
9. DESCRIPTIONS: 3
10. DESCRIPTION: 2
11. DESCRIPTION: 2
12. DESCRIPTIONS: 2

- [ ] Dodawanie pacjentów do listy  
- [ ] Usuwanie pacjentów z listy
