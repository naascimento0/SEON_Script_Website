package nemo.seon.model.dto;

/** DTO for upload response JSON serialized by Jackson. */
public record UploadResponse(boolean success, String message, Long timestamp) {

    public static UploadResponse ok(String message) {
        return new UploadResponse(true, message, System.currentTimeMillis());
    }

    public static UploadResponse error(String message) {
        return new UploadResponse(false, message, null);
    }
}
