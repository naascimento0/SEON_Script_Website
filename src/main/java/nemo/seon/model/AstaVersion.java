package nemo.seon.model;

/**
 * One archived {@code .asta} upload.
 *
 * <p>Like {@link Publication}, versions are not parsed from the model: they live in a JSON index
 * on disk (see {@code nemo.seon.service.AstaVersionService}) next to the archived files themselves,
 * so the history survives a rebuild/redeploy.
 *
 * @param id             opaque identifier, also used in the admin URLs
 * @param storedFilename name of the archived copy inside the versions directory
 * @param originalFilename name of the file as the admin uploaded it
 * @param uploadedAt     ISO-8601 instant, UTC
 * @param uploadedBy     username of the admin who uploaded it
 * @param sizeBytes      size of the archived file
 * @param sha256         hex digest, used to detect a re-upload of an identical file
 * @param note           free-text label written by the admin ("version used in the SBES paper")
 * @param ontologyCount  ontologies found when the file was parsed
 * @param conceptCount   concepts found when the file was parsed
 */
public record AstaVersion(
        String id,
        String storedFilename,
        String originalFilename,
        String uploadedAt,
        String uploadedBy,
        long sizeBytes,
        String sha256,
        String note,
        int ontologyCount,
        int conceptCount
) {
    /** Returns a copy carrying a new note, keeping every other field. */
    public AstaVersion withNote(String newNote) {
        return new AstaVersion(id, storedFilename, originalFilename, uploadedAt, uploadedBy,
                sizeBytes, sha256, newNote, ontologyCount, conceptCount);
    }
}
