package nemo.seon.model;

/** Which table of the Publications page a {@link Publication} belongs to. */
public enum PublicationCategory {
    /** Papers associated with a specific SEON ontology. */
    SEON_ONTOLOGY,
    /** Papers that address SEON broadly, without focusing on a single ontology. */
    GENERAL
}
