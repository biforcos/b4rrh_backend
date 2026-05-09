package com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model;

public enum DisplayNameFormatCode {
    /** Juan Antonio Biforcos Amor */
    FULL_TITLE_CASE,
    /** JUAN ANTONIO BIFORCOS AMOR */
    FULL_UPPER,
    /** BIFORCOS AMOR, JUAN ANTONIO */
    SURNAME_FIRST_UPPER,
    /** Juan Antonio Biforcos  (firstName + lastName1 only, title case) */
    SHORT_TITLE,
    /** JUAN ANTONIO BIFORCOS  (firstName + lastName1 only, uppercase) */
    SHORT_UPPER,
    /** BIFORCOS AMOR, J.A.  (surnames + initials of firstName) */
    SURNAME_ABBREV_UPPER
}
