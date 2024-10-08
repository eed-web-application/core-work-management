package edu.stanford.slac.core_work_management.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document()
public class Attachment {
    public enum PreviewProcessingState{
        Waiting,
        Processing,
        Error,
        PreviewNotAvailable,
        Completed
    }
    @Id
    private String id;
    private String fileName;
    private String contentType;
    private String hasPreview;
    private String previewId;
    private String originalId;
    private byte[] miniPreview;
    @Builder.Default
    private Boolean inUse = false;
    @Builder.Default
    private PreviewProcessingState previewState = PreviewProcessingState.Waiting;
    @CreatedDate
    private LocalDateTime creationData;
}
