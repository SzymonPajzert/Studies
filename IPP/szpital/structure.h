#ifndef STRUCTURE_H
#define STRUCTURE_H

struct Hospital;
struct Hospital* createHospital(void);
void deleteHospital(struct Hospital* hospital);

/*Top level functions*/
void newDiseaseEnterDescription(struct Hospital *hospital, char* name, char* diseaseDescription);
void newDiseaseCopyDescription(struct Hospital *hospital, char* name1, char* name2);
void changeDescription(struct Hospital *hospital, char* name, int n, char *diseaseDescription);
void printDescription(struct Hospital *hospital, char* name, int n);
void printDescriptionNumber(struct Hospital *hospital);
void deletePatientData(struct Hospital *hospital, char* name);

#endif
