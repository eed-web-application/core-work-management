package edu.stanford.slac.core_work_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ActivityType model
 *
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class WorkType {
    /**
     * The unique identifier for the activity type.
     */
    @Id
    private String id;
    /**
     * The unique identifier for the domain.
     */
    private String domainId;
    /**
     * The title of the activity type. This field stores the title or name of the activity type.
     */
    private String title;
    /**
     * The detailed description of the activity type. This field provides a comprehensive description of what the activity type entails.
     */
    private String description;
    /**
     * The list of the custom fields associated with the activity type.
     * The custom fields are used to store additional information about the activity.
     */
    @Builder.Default
    private List<WATypeCustomField> customFields = new ArrayList<>();

    /**
     * The list of the work types that can be child of this one
     */
    @Builder.Default
    private Set<String> childWorkTypeIds = new HashSet<>();

    /**
     * The id of the workflow that rule the life cycle of the work that refer to this type
     */
    private String workflowId;

    /**
     * The name of the validator that validate the work that refer to this type
     */
    private String validatorName;

    /**
     * The creation date of the activity type.
     */
    @CreatedDate
    private LocalDateTime createdDate;
    /**
     * The user that created the activity type.
     */
    @CreatedBy
    private String createdBy;
    /**
     * The last modification date of the activity type.
     */
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    /**
     * The user that last modified the activity type.
     */
    @LastModifiedBy
    private String lastModifiedBy;
    /**
     * The version of the activity type.
     */
    @Version
    private Long version;
}
