#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "structure.h"

/* TODO reference count bug in disease*/

/* Declaration of PatientList */
typedef struct PatientListElement *PatientList;

/* Hospital structure with counter for descriptions. Its functions defined at the end.*/
struct Hospital {
    PatientList patientList;
    int allRefCount;
};

/* Disease structure, able to change number of its references and delete itself
   when there aren't any of them. It needs to know its hospital as it has to
   change its global counter. Constructor included. */
struct Disease {
    char* name;
    int referenceCount;
    struct Hospital* hospital;
};

void addReference(struct Disease *disease) {
    if(disease->referenceCount++ == 0) {
        disease->hospital->allRefCount++;
    }
}

int removeReference(struct Disease *disease){
    disease->referenceCount--;
    if (disease->referenceCount <= 0) {
        disease->hospital->allRefCount--;
        free(disease->name);
        free(disease);
    }
    return 0;
}

struct Disease* createDisease(struct Hospital* hospital, char* name) {
    struct Disease* newDisease = (struct Disease*) malloc(sizeof(struct Disease));
    newDisease->name = (char*) malloc((strlen(name)+1)*sizeof(char));
    strcpy(newDisease->name, name);
    newDisease->referenceCount = 0;
    newDisease->hospital = hospital;
    return newDisease;
}

/* Definition of Disease List. It's implemented as FILO list, with const time
access to the last element. */
struct DiseaseListElement {
    struct Disease* disease;
    int num;
    struct DiseaseListElement* prevElement;
};

typedef struct DiseaseListElement *DiseaseList;

DiseaseList createDiseaseList(void) { return NULL; }

void deleteDiseaseList(DiseaseList diseaseList) {
    struct DiseaseListElement *current, *prev;
    current = diseaseList;

    while(current) {
        prev = current -> prevElement;
        removeReference(current -> disease);
        free(current);
        current = prev;
    }
}

struct Disease* getLastDisease(DiseaseList diseaseList) {
    if (diseaseList) { return diseaseList->disease; }
    else return NULL;
}

struct DiseaseListElement* getNthElement(DiseaseList diseaseList, int n) {
    struct DiseaseListElement *current;
    current = diseaseList;

    while(current && current -> num > n) {
        current = current -> prevElement;
    }

    if (current && current->num == n) { return current; }
    else { return NULL; }
}

struct Disease* getNthDisease(DiseaseList diseaseList, int n) {
    struct DiseaseListElement *current;
    current = getNthElement(diseaseList, n);
    if (current) { return current -> disease; }
    else { return NULL; }
}

int replaceNthDisease(DiseaseList diseaseList, int n,
                      char* diseaseName, struct Hospital* hospital) {

    struct DiseaseListElement *query;
    query = getNthElement(diseaseList, n);

    if(query) {
        struct Disease* disease = createDisease(hospital, diseaseName);
        removeReference(query -> disease);
        query -> disease = disease;
        addReference(query -> disease);
        return 1;
    }
    else { return 0; }
}

DiseaseList addDisease(DiseaseList diseaseList, struct Disease* disease){
    int num;
    struct DiseaseListElement* newElement;

    if(diseaseList) { num = diseaseList -> num + 1; }
    else { num = 1; }
    newElement = (struct DiseaseListElement*) malloc(sizeof(struct DiseaseListElement));
    newElement -> disease = disease;
    newElement -> num = num;
    newElement -> prevElement = diseaseList;

    addReference(disease);
    return newElement;
}

/* Patient structure with its list below. */
struct Patient {
    char* name;
    DiseaseList diseaseList;
};

struct Patient* createPatient(char* name) {
    struct Patient* newPatient;
    newPatient = (struct Patient*) malloc(sizeof(struct Patient));
    newPatient -> name = (char*) malloc((strlen(name)+1)*sizeof(char));
    newPatient -> diseaseList = NULL;
    strcpy(newPatient -> name, name);
    return newPatient;
}

void deletePatient(struct Patient* patient) {
    free(patient -> name);
    deleteDiseaseList(patient->diseaseList);
    free(patient);
}

/* Patient List structure with add, get and deleteAll operations.
   it's implemented as FILO list. Patient list points to last inserted element */

struct PatientListElement {
    struct Patient* patient;
    struct PatientListElement* prevElement;
};

PatientList createPatientList(void) { return NULL; }

void deletePatientList(PatientList patientList){
    struct PatientListElement *current, *prev;
    current = patientList;

    while(current) {
        prev = current -> prevElement;
        deletePatient(current -> patient);
        free(current);
        current = prev;
    }
    return;
}

struct Patient* addPatient(PatientList *patientList, char* name) {
    struct Patient* newPatient;
    struct PatientListElement* newElement;

    newPatient = createPatient(name);
    newElement = (struct PatientListElement*) malloc(sizeof(struct PatientListElement));
    newElement -> prevElement = *patientList;
    newElement -> patient = newPatient;
    *patientList = newElement;

    return newPatient;
}

struct Patient* getPatient(PatientList patientList, char* name) {
    struct PatientListElement* current;

    current = patientList;
    while(current && strcmp(current->patient->name, name)){
        current = current -> prevElement;
    }
    /* At this point patient is found or there is no such patient. */

    if (current) { return current -> patient; }
    else { return NULL; }
}

/* Hospital functions */
struct Hospital* createHospital(void){
    struct Hospital* result;

    result = (struct Hospital*) malloc(sizeof(struct Hospital));
    result -> patientList = createPatientList();
    result -> allRefCount = 0;
    return result;
}

void deleteHospital(struct Hospital* hospital) {
    deletePatientList(hospital -> patientList);
    free(hospital);
    return;
}

/* Top level functions. */
void newDiseaseEnterDescription(struct Hospital *hospital, char* name, char* diseaseDescription){
    struct Patient *patientPtr;
    struct Disease* diseasePtr;

    patientPtr = getPatient(hospital->patientList, name);
    if (patientPtr == NULL) {
        patientPtr = addPatient(&(hospital->patientList), name);
    }

    diseasePtr = createDisease(hospital, diseaseDescription);
    patientPtr->diseaseList = addDisease(patientPtr->diseaseList, diseasePtr);
    printf("OK\n");
    return;
}

void newDiseaseCopyDescription(struct Hospital *hospital, char* name1, char* name2){
    struct Patient *patient1Ptr, *patient2Ptr;
    struct Disease* copiedDiseasePtr;

    patient1Ptr = getPatient(hospital->patientList, name1);
    if (patient1Ptr == NULL) {
        patient1Ptr = addPatient(&(hospital->patientList), name1);
    }

    patient2Ptr = getPatient(hospital->patientList, name2);
    if (patient2Ptr == NULL) {
        printf("IGNORED\n");
        return;
    }

    copiedDiseasePtr = getLastDisease(patient2Ptr->diseaseList);
    if (copiedDiseasePtr == NULL) {
        printf("IGNORED\n");
        return;
    }

    patient1Ptr->diseaseList = addDisease(patient1Ptr->diseaseList, copiedDiseasePtr);
    printf("OK\n");
    return;
}


void changeDescription(struct Hospital *hospital, char* name,
                       int n, char *diseaseDescription) {
    struct Patient *patientPtr;

    patientPtr = getPatient(hospital->patientList, name);
    if (patientPtr == NULL) {
        printf("IGNORED\n");
        return;
    }

    if(replaceNthDisease(patientPtr->diseaseList, n, name, hospital)){
        printf("OK\n");
    }
    else {
        printf("IGNORED\n");
    }


    return;
}

void printDescription(struct Hospital *hospital, char* name, int n) {
    struct Patient *patientPtr;
    struct Disease *diseasePtr;

    patientPtr = getPatient(hospital->patientList, name);
    if (patientPtr == NULL) {
        printf("IGNORED\n");
        return;
    }

    diseasePtr = getNthDisease(patientPtr->diseaseList, n);
    if (diseasePtr == NULL) {
        printf("IGNORED\n");
        return;
    }

    printf("%s\n", diseasePtr->name);
    return;
}

void printDescriptionNumber(struct Hospital *hospital) {
    fprintf(stderr, "DESCRIPTIONS %d\n", hospital->allRefCount);
}

void deletePatientData(struct Hospital *hospital, char* name) {
    struct Patient *patientPtr;

    patientPtr = getPatient(hospital->patientList, name);
    if (patientPtr == NULL) {
        printf("IGNORED\n");
        return;
    }

    deleteDiseaseList(patientPtr->diseaseList);
    patientPtr->diseaseList = createDiseaseList();
    printf("OK\n");
    return;
}
