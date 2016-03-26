#include "parse.h"
#include "structure.h"
#include <string.h>
#include <stdlib.h>

int main(int argc, char const *argv[]) {
    int shouldPrintDescription, i;
    struct Hospital *hospital;
    struct Request* request;

    shouldPrintDescription = 0;
    for(i=1; i<argc; i++){
        shouldPrintDescription = shouldPrintDescription || (strcmp(argv[i], "-v")==0);
    }

    hospital = createHospital();
    request = createRequest();
    while(parseLine(request) > 0) {
        switch (request -> command) {
            case NEW_DISEASE_ENTER_DESCRIPTION:
                newDiseaseEnterDescription(hospital, request->str1, request->str2);
            break;

            case NEW_DISEASE_COPY_DESCRIPTION:
                newDiseaseCopyDescription(hospital, request->str1, request->str2);
            break;

            case CHANGE_DESCRIPTION:
                changeDescription(
                    hospital,
                    request->str1,
                    request->num,
                    request->str2
                );
            break;

            case PRINT_DESCRIPTION:
                printDescription(hospital, request->str1, request->num);
            break;

            case DELETE_PATIENT_DATA:
                deletePatientData(hospital, request->str1);
            break;

            case NO_LINE_AVAILABLE:
                exit(1);
            break;
        }

        if (shouldPrintDescription) { printDescriptionNumber(hospital); }
    }

    deleteHospital(hospital);
    deleteRequest(request);
    return 0;
}
