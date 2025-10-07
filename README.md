## Exam Result Management Application
This is a web application designed to streamline the process of managing, publishing, and officially recording (verbalizing) exam results for university courses. It provides distinct interfaces and functionality for Faculty (Docente) and Students (Studente).

## 👨‍🏫 Faculty Features
The faculty member's workflow centers around the ISCRITTI (Enrolled Students) page for a selected exam session (appello).

* Result Entry & Modification: A MODIFICA button allows the faculty to enter or change a student's grade, setting the status to "inserito" (inserted).

* Grade Publishing: The PUBBLICA button makes all "inserito" grades visible to the student and locks them from further faculty modification, changing the status to "pubblicato" (published).

* Official Verbalization: The VERBALIZZA button finalizes the results.

  * It changes the status of "pubblicato" and "rifiutato" grades to "verbalizzato" (verbalized).

  * It creates a final official record (Verbale).

  * A rejected grade is recorded as "rimandato" (postponed/failed).

* Record Viewing: A separate VERBALI page lists all official records created by the faculty.

## 👩‍🎓 Student Features

The student's main interaction is with the ESITO (Result) page.

* View Published Grades: Students can view their assigned grade once it's been published by the faculty.

* Reject a Grade: For passing grades (18 to 30 e lode), a RIFIUTA (Reject) button is displayed.

  * Pressing this button changes the grade's status to "rifiutato" (rejected) in the faculty's view.

  * This option is disabled once the results are officially verbalized.



